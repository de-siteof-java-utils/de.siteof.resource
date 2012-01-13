package de.siteof.resource;

import java.io.IOException;
import java.io.InputStream;

import de.siteof.resource.event.IResourceListener;
import de.siteof.resource.event.ResourceLoaderEvent;
import de.siteof.task.ITaskManager;

public class ResourceProxy extends AbstractResource implements IResource {

	private final IResource resource;

	public ResourceProxy(IResource resource, ITaskManager taskManager) {
		super(resource, taskManager);
		this.resource = resource;
	}

	public ResourceProxy(IResource resource) {
		this(resource, null);
	}

	protected IResource getResource() {
		return resource;
	}

	public void clearCache() {
		resource.clearCache();
	}

	public boolean exists() throws IOException {
		return resource.exists();
	}

	public IResourceContext getContext() {
		return resource.getContext();
	}

	public String getName() {
		return resource.getName();
	}

	public InputStream getResourceAsStream() throws IOException {
		return resource.getResourceAsStream();
	}

	public byte[] getResourceBytes() throws IOException {
		return resource.getResourceBytes();
	}

	public long getLastCached() {
		return resource.getLastCached();
	}

	public long getLastModified() {
		return resource.getLastModified();
	}

	public int getModifier() {
		return resource.getModifier();
	}

	public long getSize() {
		return resource.getSize();
	}

	public void abort() {
		resource.abort();
	}

	public void getResourceAsStream(
			IResourceListener<ResourceLoaderEvent<InputStream>> listener)
			throws IOException {
		super.getResourceAsStream(listener);
	}

	public void getResourceBytes(
			IResourceListener<ResourceLoaderEvent<byte[]>> listener)
			throws IOException {
		super.getResourceBytes(listener);
	}

}
