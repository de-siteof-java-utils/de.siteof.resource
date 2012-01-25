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

	@Override
	public void clearCache() {
		resource.clearCache();
	}

	@Override
	public boolean exists() throws IOException {
		return resource.exists();
	}

	@Override
	public IResourceContext getContext() {
		return resource.getContext();
	}

	@Override
	public String getName() {
		return resource.getName();
	}

	@Override
	public InputStream getResourceAsStream() throws IOException {
		return resource.getResourceAsStream();
	}

	@Override
	public byte[] getResourceBytes() throws IOException {
		return resource.getResourceBytes();
	}

	@Override
	public long getLastCached() {
		return resource.getLastCached();
	}

	@Override
	public long getLastModified() {
		return resource.getLastModified();
	}

	@Override
	public int getModifier() {
		return resource.getModifier();
	}

	@Override
	public long getSize() {
		return resource.getSize();
	}

	@Override
	public void abort() {
		resource.abort();
	}

	@Override
	public void getResourceAsStream(
			IResourceListener<ResourceLoaderEvent<InputStream>> listener,
			ResourceRequestParameters parameters)
			throws IOException {
		super.getResourceAsStream(listener, parameters);
	}

	@Override
	public void getResourceBytes(
			IResourceListener<ResourceLoaderEvent<byte[]>> listener,
			ResourceRequestParameters parameters)
			throws IOException {
		super.getResourceBytes(listener, parameters);
	}

}
