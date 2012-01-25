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

	@Override
	public void clearCache() {
		synchronized (resource) {
			resource.clearCache();
		}
	}

	@Override
	public boolean exists() throws IOException {
		synchronized (resource) {
			return resource.exists();
		}
	}

	@Override
	public IResourceContext getContext() {
		synchronized (resource) {
			return resource.getContext();
		}
	}

	@Override
	public String getName() {
		synchronized (resource) {
			return resource.getName();
		}
	}

	@Override
	public InputStream getResourceAsStream() throws IOException {
		synchronized (resource) {
			return resource.getResourceAsStream();
		}
	}

	@Override
	public byte[] getResourceBytes() throws IOException {
		synchronized (resource) {
			return resource.getResourceBytes();
		}
	}

	@Override
	public long getLastCached() {
		synchronized (resource) {
			return resource.getLastCached();
		}
	}

	@Override
	public long getLastModified() {
		synchronized (resource) {
			return resource.getLastModified();
		}
	}

	@Override
	public int getModifier() {
		synchronized (resource) {
			return resource.getModifier();
		}
	}

	@Override
	public long getSize() {
		synchronized (resource) {
			return resource.getSize();
		}
	}

	@Override
	public void abort() {
		synchronized (resource) {
			resource.abort();
		}
	}

	@Override
	public void getResourceAsStream(
			IResourceListener<ResourceLoaderEvent<InputStream>> listener)
			throws IOException {
		synchronized (resource) {
			resource.getResourceAsStream(listener);
		}
	}

	@Override
	public void getResourceBytes(
			IResourceListener<ResourceLoaderEvent<byte[]>> listener)
			throws IOException {
		synchronized (resource) {
			resource.getResourceBytes(listener);
		}
	}

	@Override
	public ITaskManager getTaskManager() {
		synchronized (resource) {
			return resource.getTaskManager();
		}
	}

	@Override
	public void getResourceBytes(
			IResourceListener<ResourceLoaderEvent<byte[]>> listener,
			ResourceRequestParameters parameters) throws IOException {
		synchronized (resource) {
			resource.getResourceBytes(listener, parameters);
		}
	}

	@Override
	public void getResourceAsStream(
			IResourceListener<ResourceLoaderEvent<InputStream>> listener,
			ResourceRequestParameters parameters) throws IOException {
		synchronized (resource) {
			resource.getResourceAsStream(listener, parameters);
		}
	}

}
