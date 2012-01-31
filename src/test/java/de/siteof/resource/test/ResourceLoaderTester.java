package de.siteof.resource.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;
import de.siteof.resource.AbstractResource;
import de.siteof.resource.AbstractResourceLoader;
import de.siteof.resource.IResource;
import de.siteof.resource.IResourceLoader;
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

	private final TestResourceLoader parent;
	private IResourceLoader resourceLoader;

	public ResourceLoaderTester() {
		parent = new TestResourceLoader(null, null);
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

	private void test(String name, TestResource testResource) throws IOException {
		parent.getResourceMap().put(name, testResource);
		testResource.resetRequestCount();

		IResource resource = resourceLoader.getResource(name);
		Assert.assertNotNull("resource", resource);

		resource.clearCache();

		byte[] actualData = resource.getResourceBytes();
		Assert.assertNotNull("actualData", actualData);
		assertEquals("actualData", testResource.getData(), actualData);

		Assert.assertEquals("requestCount", 1, testResource.getRequestCount());

		resource.clearCache();

		InputStream in = resource.getResourceAsStream();
		try {
			actualData = IOUtil.readAllFromStream(in);
		} finally {
			in.close();
		}
		Assert.assertNotNull("actualData", actualData);
		assertEquals("actualData", testResource.getData(), actualData);

		Assert.assertEquals("requestCount", 2, testResource.getRequestCount());
	}

	public void test(String name) throws IOException {
		TestResource testResource = new TestResource(name, this.getBinaryData(1000));
		this.test(name, testResource);
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
