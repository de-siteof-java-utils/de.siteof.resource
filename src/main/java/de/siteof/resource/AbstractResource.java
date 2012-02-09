package de.siteof.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.siteof.resource.event.IResourceListener;
import de.siteof.resource.event.ResourceLoaderEvent;
import de.siteof.resource.util.IOUtil;
import de.siteof.task.AbstractTask;
import de.siteof.task.ITaskManager;
import de.siteof.task.SynchronousTaskManager;

public abstract class AbstractResource implements IResource {

	private static final Log log = LogFactory.getLog(AbstractResource.class);

	private final ITaskManager taskManager;
	private final IResource resource;
	private final String name;
	private int modifier;

	public AbstractResource(String name) {
		this(null, name, null);
	}

	public AbstractResource(IResource resource) {
		this(resource, null, null);
	}


	public AbstractResource(String name, ITaskManager taskManager) {
		this(null, name, taskManager);
	}

	public AbstractResource(IResource resource, ITaskManager taskManager) {
		this(resource, null, taskManager);
	}

	private AbstractResource(IResource resource, String name, ITaskManager taskManager) {
		this.resource	= resource;
		this.name		= name;
		if ((taskManager == null) && (resource != null)) {
			taskManager = resource.getTaskManager();
		}
		if (taskManager == null) {
			this.taskManager = getDefaultTaskManager();
		} else {
			this.taskManager = taskManager;
		}
	}


	protected static ITaskManager getDefaultTaskManager() {
		return SynchronousTaskManager.getInstance();
	}

	protected IResource getParentResource() {
		return resource;
	}


	protected void setModifier(int modifier) {
		this.modifier	= modifier;
	}

	@Override
	public void clearCache() {
		if (resource != null) {
			resource.clearCache();
		}
	}

	@Override
	public boolean exists() throws IOException {
		if (resource != null) {
			return resource.exists();
		}
		return false;
	}

	@Override
	public IResourceContext getContext() {
		if (resource != null) {
			return resource.getContext();
		}
		return null;
	}

	@Override
	public long getLastCached() {
		if (resource != null) {
			return resource.getLastCached();
		}
		return 0;
	}

	@Override
	public long getLastModified() {
		if (resource != null) {
			return resource.getLastModified();
		}
		return 0;
	}

	@Override
	public String getName() {
		if (name != null) {
			return name;
		}
		if (resource != null) {
			return resource.getName();
		}
		return null;
	}

	@Override
	public byte[] getResourceBytes() throws IOException {
		if (resource != null) {
			return resource.getResourceBytes();
		}
		return getResourceBytesFromStream();
	}


	protected byte[] getResourceBytesFromStreamAndClose(InputStream in) throws IOException {
		if (in == null) {
			log.warn("input stram is null");
			return null;
		}
		try {
			log.debug("loading data from resource, name=" + name);
			return IOUtil.readAllFromStream(in);
		} finally {
			in.close();
		}
	}


	protected byte[] getResourceBytesFromEvent(ResourceLoaderEvent<InputStream> event) throws IOException {
		return this.getResourceBytesFromStreamAndClose(event.getResult());
	}


	protected byte[] getResourceBytesFromStream() throws IOException {
		InputStream in	= getResourceAsStream();
		if (in == null) {
			log.warn("resource not found: " + name);
			return null;
		}
		try {
			log.debug("loading data from resource, name=" + name);
			return IOUtil.readAllFromStream(in);
		} finally {
			in.close();
		}
	}

	@Override
	public int getModifier() {
		if (resource != null) {
			return resource.getModifier() | modifier;
		}
		return modifier;
	}

	@Override
	public void abort() {
		if (resource != null) {
			resource.abort();
		}
	}

	protected <T> void notifyStatusMessage(
			IResourceListener<ResourceLoaderEvent<T>> listener,
			String message) {
		listener.onResourceEvent(new ResourceLoaderEvent<T>(
				this,
				message));
	}

	@Override
	public final void getResourceAsStream(
			IResourceListener<ResourceLoaderEvent<InputStream>> listener) throws IOException {
		this.getResourceAsStream(listener, new ResourceRequestParameters());
	}

	@Override
	public void getResourceAsStream(
			IResourceListener<ResourceLoaderEvent<InputStream>> listener,
			ResourceRequestParameters parameters)
			throws IOException {
		log.warn("default asynchronous getResourceAsStream implementation, class=[" +
				this.getClass().getName() + "], name=[" + this.getName() + "]");
		final IResourceListener<ResourceLoaderEvent<InputStream>> finalListener = listener;
		taskManager.addTask(new AbstractTask() {
			@Override
			public void execute() throws Exception {
				ResourceLoaderEvent<InputStream> event;
				try {
					event = new ResourceLoaderEvent<InputStream>(
							AbstractResource.this, getResourceAsStream(), true);
				} catch (Throwable e) {
					event = new ResourceLoaderEvent<InputStream>(
							AbstractResource.this, e);
				}
				finalListener.onResourceEvent(event);
			}});
	}

	@Override
	public final void getResourceBytes(
			IResourceListener<ResourceLoaderEvent<byte[]>> listener) throws IOException {
		this.getResourceBytes(listener, new ResourceRequestParameters());
	}

	@Override
	public void getResourceBytes(
			final IResourceListener<ResourceLoaderEvent<byte[]>> listener,
			ResourceRequestParameters parameters)
			throws IOException {
		log.warn("default asynchronous getResourceBytes implementation, class=[" +
				this.getClass().getName() + "], name=[" + this.getName() + "]");
		taskManager.addTask(new AbstractTask() {
			@Override
			public void execute() throws Exception {
				InputStream in = null;
				try {
					in = AbstractResource.this.getResourceAsStream();
					byte[] buffer = new byte[4096];
					while (true) {
						int bytesRead = in.read(buffer);
						if (bytesRead < 0) {
							break;
						} else if (bytesRead > 0) {
							byte[] chunk = Arrays.copyOf(buffer, bytesRead);
							ResourceLoaderEvent<byte[]> event = new ResourceLoaderEvent<byte[]>(
									AbstractResource.this, chunk, false);
							listener.onResourceEvent(event);
						}
					}
					// complete event
					ResourceLoaderEvent<byte[]> event = new ResourceLoaderEvent<byte[]>(
							AbstractResource.this, new byte[0], true);
					listener.onResourceEvent(event);
				} catch (Throwable e) {
					// failed event
					ResourceLoaderEvent<byte[]> event = new ResourceLoaderEvent<byte[]>(
							AbstractResource.this, e);
					listener.onResourceEvent(event);
				} finally {
					IOUtil.close(in);
				}
			}});
	}

	@Override
	public ITaskManager getTaskManager() {
		return taskManager;
	}

}
