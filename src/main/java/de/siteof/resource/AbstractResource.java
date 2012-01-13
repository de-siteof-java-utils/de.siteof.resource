package de.siteof.resource;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.siteof.resource.event.IResourceListener;
import de.siteof.resource.event.ResourceLoaderEvent;
import de.siteof.resource.util.IOUtil;
import de.siteof.task.AbstractTask;
import de.siteof.task.ITaskManager;
import de.siteof.task.SynchronousTaskManager;

public abstract class AbstractResource implements IResource {

	private final ITaskManager taskManager;
	private final IResource resource;
	private final String name;
	private int modifier;

	private static final Log log	= LogFactory.getLog(AbstractResource.class);


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


	/*
	 * (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.IResource#clearCache()
	 */
	public void clearCache() {
		if (resource != null) {
			resource.clearCache();
		}
	}


	/*
	 * (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.IResource#exists()
	 */
	public boolean exists() throws IOException {
		if (resource != null) {
			return resource.exists();
		}
		return false;
	}


	/*
	 * (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.IResource#getContext()
	 */
	public IResourceContext getContext() {
		if (resource != null) {
			return resource.getContext();
		}
		return null;
	}


	/*
	 * (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.IResource#getLastCached()
	 */
	public long getLastCached() {
		if (resource != null) {
			return resource.getLastCached();
		}
		return 0;
	}


	/*
	 * (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.IResource#getLastModified()
	 */
	public long getLastModified() {
		if (resource != null) {
			return resource.getLastModified();
		}
		return 0;
	}


	/*
	 * (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.IResource#getName()
	 */
	public String getName() {
		if (name != null) {
			return name;
		}
		if (resource != null) {
			return resource.getName();
		}
		return null;
	}


	/*
	 * (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.IResource#getResourceBytes()
	 */
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


	/*
	 * (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.IResource#getModifier()
	 */
	public int getModifier() {
		if (resource != null) {
			return resource.getModifier() | modifier;
		}
		return modifier;
	}

	/* (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.IResource#abort()
	 */
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


	public void getResourceAsStream(
			IResourceListener<ResourceLoaderEvent<InputStream>> listener)
			throws IOException {
		log.warn("default asynchronous getResourceAsStream implementation, class=[" +
				this.getClass().getName() + "], name=[" + this.getName() + "]");
		final IResourceListener<ResourceLoaderEvent<InputStream>> finalListener = listener;
		taskManager.addTask(new AbstractTask() {
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

	public void getResourceBytes(
			IResourceListener<ResourceLoaderEvent<byte[]>> listener)
			throws IOException {
		log.warn("default asynchronous getResourceBytes implementation, class=[" +
				this.getClass().getName() + "], name=[" + this.getName() + "]");
		final IResourceListener<ResourceLoaderEvent<byte[]>> finalListener = listener;
		taskManager.addTask(new AbstractTask() {
			public void execute() throws Exception {
				ResourceLoaderEvent<byte[]> event;
				try {
					event = new ResourceLoaderEvent<byte[]>(
							AbstractResource.this, getResourceBytes(), true);
				} catch (Throwable e) {
					event = new ResourceLoaderEvent<byte[]>(
							AbstractResource.this, e);
				}
				finalListener.onResourceEvent(event);
			}});
	}

	/* (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.IResource#getTaskManager()
	 */
	public ITaskManager getTaskManager() {
		return taskManager;
	}

}
