package de.siteof.resource;

import java.io.IOException;
import java.io.InputStream;

import de.siteof.resource.event.IResourceListener;
import de.siteof.resource.event.ResourceLoaderEvent;
import de.siteof.task.ITaskManager;

public class SynchronizedResourceProxy implements IResource {

	private final IResource resource;

	public SynchronizedResourceProxy(IResource resource) {
		this.resource = resource;
	}

	protected IResource getResource() {
		return resource;
	}

	public void clearCache() {
		synchronized (resource) {
			resource.clearCache();
		}
	}

	public boolean exists() throws IOException {
		synchronized (resource) {
			return resource.exists();
		}
	}

	public IResourceContext getContext() {
		synchronized (resource) {
			return resource.getContext();
		}
	}

	public String getName() {
		synchronized (resource) {
			return resource.getName();
		}
	}

	public InputStream getResourceAsStream() throws IOException {
		synchronized (resource) {
			return resource.getResourceAsStream();
		}
	}

	public byte[] getResourceBytes() throws IOException {
		synchronized (resource) {
			return resource.getResourceBytes();
		}
	}

	public long getLastCached() {
		synchronized (resource) {
			return resource.getLastCached();
		}
	}

	public long getLastModified() {
		synchronized (resource) {
			return resource.getLastModified();
		}
	}

	public int getModifier() {
		synchronized (resource) {
			return resource.getModifier();
		}
	}

	public long getSize() {
		synchronized (resource) {
			return resource.getSize();
		}
	}

	public void abort() {
		synchronized (resource) {
			resource.abort();
		}
	}

	public void getResourceAsStream(
			IResourceListener<ResourceLoaderEvent<InputStream>> listener)
			throws IOException {
		synchronized (resource) {
			resource.getResourceAsStream(listener);
		}
	}

	public void getResourceBytes(
			IResourceListener<ResourceLoaderEvent<byte[]>> listener)
			throws IOException {
		synchronized (resource) {
			resource.getResourceBytes(listener);
		}
	}

	/* (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.IResource#getTaskManager()
	 */
	public ITaskManager getTaskManager() {
		synchronized (resource) {
			return resource.getTaskManager();
		}
	}

}
