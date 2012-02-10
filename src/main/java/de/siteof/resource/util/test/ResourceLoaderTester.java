package de.siteof.resource.util.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.siteof.resource.AbstractResource;
import de.siteof.resource.AbstractResourceLoader;
import de.siteof.resource.IResource;
import de.siteof.resource.IResourceLoader;
import de.siteof.resource.ResourceRequestParameters;
import de.siteof.resource.event.IResourceListener;
import de.siteof.resource.event.MetaResourceLoaderEvent;
import de.siteof.resource.event.ResourceLoaderEvent;
import de.siteof.resource.util.IOUtil;
import de.siteof.task.ITaskManager;

public class ResourceLoaderTester {

	private class TestResource extends AbstractResource {

		private final byte[] data;
		private final AtomicInteger requestCount = new AtomicInteger();

		public TestResource(String name, byte[] data) {
			super(name);
			this.data = data;
		}

		@Override
		public long getSize() {
			return data.length;
		}

		@Override
		public InputStream getResourceAsStream() throws IOException {
			requestCount.incrementAndGet();
			return new ByteArrayInputStream(this.data);
		}

		public byte[] getData() {
			return data;
		}

		public void resetRequestCount() {
			requestCount.set(0);
		}

		public int getRequestCount() {
			return requestCount.get();
		}

//		public void setData(byte[] data) {
//			this.data = data;
//		}

	}

	private class TestResourceLoader extends AbstractResourceLoader {

		private final Map<String, TestResource> resourceMap = new HashMap<String, TestResource>();

		public TestResourceLoader(IResourceLoader parent,
				ITaskManager taskManager) {
			super(parent, taskManager);
		}

		@Override
		public IResource getResource(String name) throws IOException {
			return resourceMap.get(name);
		}

		public Map<String, TestResource> getResourceMap() {
			return resourceMap;
		}

	}


	private static final Log log = LogFactory.getLog(ResourceLoaderTester.class);

	private final TestResourceLoader parent;
	private IResourceLoader resourceLoader;

	public ResourceLoaderTester() {
		parent = new TestResourceLoader(null, null);
	}

	protected IResourceLoader createResourceLoader(IResourceLoader parent) {
		throw new UnsupportedOperationException("createResourceLoader not implemented");
	}

	public List<ResourceLoaderTestParameter> allTests() {
		List<ResourceLoaderTestParameter> result = new LinkedList<ResourceLoaderTestParameter>();
		Method[] methods = ResourceLoaderTester.class.getDeclaredMethods();
		for (Method method: methods) {
			String name = method.getName();
			if ((name.startsWith("test")) && (name.length() > 4)) {
				result.add(new ResourceLoaderTestParameter(name));
			}
		}
		if (result.isEmpty()) {
			throw new IllegalStateException("no tests found");
		}
		return result;
	}

	public List<Object[]> allTestsArrays() {
		List<ResourceLoaderTestParameter> tests = this.allTests();
		List<Object[]> result = new ArrayList<Object[]>(tests.size());
		for (ResourceLoaderTestParameter test: tests) {
			result.add(new Object[] {test});
		}
		return result;
	}

	protected byte[] getBinaryData(int length) {
		byte[] data = new byte[length];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) i;
		}
		return data;
	}

	protected byte[] getStringData(int length) {
		return getStringData("abcdefghij012345689", length);
	}

	protected byte[] getStringData(String s, int length) {
		byte[] data = new byte[length];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) s.charAt(i % s.length());
		}
		return data;
	}

	public static void assertEquals(String message, byte[] expected, byte[] actual) {
		if (!Arrays.equals(expected, actual)) {
			Assert.fail(message + ".length, expected=[" + expected.length +
					"], actual=[" + actual.length + "]");
			Assert.fail(message + ", expected=[" + new String(expected) + "], actual=[" + new String(actual) + "]");
		}
	}

	private void testResourceBytes(String name, TestResource testResource) throws IOException {
		parent.getResourceMap().put(name, testResource);
		testResource.resetRequestCount();

		IResource resource = resourceLoader.getResource(name);
		Assert.assertNotNull("resource", resource);

		resource.clearCache();

		byte[] actualData = resource.getResourceBytes();
		Assert.assertNotNull("actualData", actualData);
		assertEquals("actualData", testResource.getData(), actualData);

		Assert.assertEquals("requestCount", 1, testResource.getRequestCount());
	}

	private void testResourceAsStream(String name, TestResource testResource) throws IOException {
		parent.getResourceMap().put(name, testResource);
		testResource.resetRequestCount();

		IResource resource = resourceLoader.getResource(name);
		Assert.assertNotNull("resource", resource);

		resource.clearCache();

		byte[] actualData;
		InputStream in = resource.getResourceAsStream();
		try {
			actualData = IOUtil.readAllFromStream(in);
		} finally {
			in.close();
		}
		Assert.assertNotNull("actualData", actualData);
		assertEquals("actualData", testResource.getData(), actualData);

		Assert.assertEquals("requestCount", 1, testResource.getRequestCount());
	}

	private void testResourceBytesAsynch(String name, TestResource testResource) throws IOException {
		parent.getResourceMap().put(name, testResource);
		testResource.resetRequestCount();

		IResource resource = resourceLoader.getResource(name);
		Assert.assertNotNull("resource", resource);

		ResourceRequestParameters parameters = new ResourceRequestParameters();
		parameters.setNoChache(true);

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final AtomicReference<String> filenameHolder = new AtomicReference<String>();
		final CountDownLatch lock = new CountDownLatch(1);
		final AtomicBoolean downloadResult = new AtomicBoolean();

		IResourceListener<ResourceLoaderEvent<byte[]>> listener = new IResourceListener<ResourceLoaderEvent<byte[]>>() {
			@Override
			public void onResourceEvent(ResourceLoaderEvent<byte[]> event) {
				if (event instanceof MetaResourceLoaderEvent) {
					String filename = ((MetaResourceLoaderEvent<?>) event).getMetaData().getName();
					filenameHolder.set(filename);
				} else if (event.isComplete()) {
					byte[] data = event.getResult();
					if ((data != null) && (data.length > 0)) {
						try {
							out.write(data);
							downloadResult.set(true);
						} catch (IOException e) {
							log.error("failed", e);
						}
					} else {
						downloadResult.set(true);
					}
					lock.countDown();
				} else if (event.isFailed()) {
					downloadResult.set(false);
					lock.countDown();
				} else {
					byte[] data = event.getResult();
					if ((data != null) && (data.length > 0)) {
						try {
							out.write(data);
						} catch (IOException e) {
							log.error("failed", e);
						}
					}
				}
			}
		};
		resource.getResourceBytes(listener, parameters);

		try {
			log.info("wait");
			lock.await();
			log.info("wait done");
		} catch (InterruptedException e) {
			log.warn("wait interrupted", e);
		}

		byte[] actualData = out.toByteArray();
		Assert.assertNotNull("actualData", actualData);
		Assert.assertTrue("downloadResult", downloadResult.get());
		assertEquals("actualData", testResource.getData(), actualData);
//		Assert.assertEquals("filename", name, filenameHolder.get());

		Assert.assertEquals("requestCount", 1, testResource.getRequestCount());
	}

	private void testResourceAsStreamAsynch(String name, TestResource testResource) throws IOException {
		parent.getResourceMap().put(name, testResource);
		testResource.resetRequestCount();

		IResource resource = resourceLoader.getResource(name);
		Assert.assertNotNull("resource", resource);

		ResourceRequestParameters parameters = new ResourceRequestParameters();
		parameters.setNoChache(true);

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final AtomicReference<String> filenameHolder = new AtomicReference<String>();
		final CountDownLatch lock = new CountDownLatch(1);
		final AtomicBoolean downloadResult = new AtomicBoolean();

		IResourceListener<ResourceLoaderEvent<InputStream>> listener = new IResourceListener<ResourceLoaderEvent<InputStream>>() {
			@Override
			public void onResourceEvent(ResourceLoaderEvent<InputStream> event) {
				if (event instanceof MetaResourceLoaderEvent) {
					String filename = ((MetaResourceLoaderEvent<?>) event).getMetaData().getName();
					filenameHolder.set(filename);
				} else if (event.isComplete()) {
					InputStream in = event.getResult();
					try {
						try {
							byte[] data = IOUtil.readAllFromStream(in);
							out.write(data);
							downloadResult.set(true);
						} finally {
							in.close();
						}
					} catch (IOException e) {
						log.error("failed", e);
					}
					lock.countDown();
				} else if (event.isFailed()) {
					downloadResult.set(false);
					lock.countDown();
				}
			}
		};
		resource.getResourceAsStream(listener, parameters);

		try {
			log.info("wait");
			lock.await();
			log.info("wait done");
		} catch (InterruptedException e) {
			log.warn("wait interrupted", e);
		}

		byte[] actualData = out.toByteArray();
		Assert.assertNotNull("actualData", actualData);
		Assert.assertTrue("downloadResult", downloadResult.get());
		assertEquals("actualData", testResource.getData(), actualData);
//		Assert.assertEquals("filename", name, filenameHolder.get());

		Assert.assertEquals("requestCount", 1, testResource.getRequestCount());
	}

	private void test(String name, TestResource testResource) throws IOException {
		testResourceBytes(name, testResource);
		testResourceAsStream(name, testResource);
		testResourceBytesAsynch(name, testResource);
		testResourceAsStreamAsynch(name, testResource);
	}

	public void test(String name) throws IOException {
		TestResource testResource = new TestResource(name, this.getBinaryData(1024 * 1024));
		this.test(name, testResource);
	}

	public void test(ResourceLoaderTestParameter test, String name) throws IOException {
//		if (this.resourceLoader == null) {
		this.resourceLoader = this.createResourceLoader(parent);
//		}
		TestResource testResource = new TestResource(name, this.getBinaryData(1024 * 1024));
		try {
			Method method = ResourceLoaderTester.class.getDeclaredMethod(test.getTestName(), new Class<?>[] {
				String.class, TestResource.class
			});
			method.invoke(this, new Object[] {name, testResource});
		} catch (RuntimeException e) {
			throw e;
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof IOException) {
				throw (IOException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				throw new IOException("unexpected exception - " + cause, cause);
			}
		} catch (Exception e) {
			throw new IllegalStateException("unexpected exception - " + e, e);
		}
	}

	public void test(ResourceLoaderTestParameter test) throws IOException {
		this.test(test, "dummy");
	}

	protected String getDefaultResourceName() {
		return "dummy";
	}

	public IResourceLoader getParent() {
		return parent;
	}

	public IResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public void setResourceLoader(IResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

}
