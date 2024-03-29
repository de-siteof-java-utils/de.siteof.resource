package de.siteof.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.siteof.cache.IObjectCache;
import de.siteof.cache.ObjectCacheFactory;
import de.siteof.resource.event.IResourceListener;
import de.siteof.resource.event.ResourceLoaderEvent;

public class MemoryCachedResourceLoader extends AbstractResourceLoader {

	private static class MemoryCachedResource extends AbstractResource {

		private boolean cached;
		private boolean exists;
		private byte[] data;

		public MemoryCachedResource(IResource resource) {
			super(resource);
		}

		@Override
		public void clearCache() {
			cached	= false;
			exists	= false;
			data	= null;
			super.clearCache();
			setModifier(getModifier() & (~MODIFIER_LOADED));
		}

		@Override
		public boolean exists() throws IOException {
			if (cached) {
				return (exists) || (data != null);
			} else {
				exists	= super.exists();
				cached	= true;
			}
			return (data != null);
		}

		@Override
		public InputStream getResourceAsStream() throws IOException {
			byte[] data	= this.getResourceBytes();
			if (data != null) {
				return new ByteArrayInputStream(data);
			}
			return null;
		}

		@Override
		public byte[] getResourceBytes() throws IOException {
			byte[] data	= this.data;
			if (data == null) {
				data	= super.getResourceBytes();
				if (data != null) {
					this.data		= data;
					this.cached	= true;
					setModifier(getModifier() | MODIFIER_MEMORY_LOADED);
				}
			}
			return data;
		}

		@Override
		public long getSize() {
			long result	= 0;
			try {
				byte[] data	= getResourceBytes();
				if (data != null) {
					result	= data.length;
				}
			} catch (IOException e) {
			}
			return result;
		}

		@Override
		public void getResourceAsStream(
				IResourceListener<ResourceLoaderEvent<InputStream>> listener,
				ResourceRequestParameters parameters)
				throws IOException {
			if (parameters.isNoChache()) {
				this.getParentResource().getResourceAsStream(listener, parameters);
			} else {
				final IResourceListener<ResourceLoaderEvent<InputStream>> finalListener = listener;
				byte[] data	= this.data;
				if (data != null) {
					ResourceLoaderEvent<InputStream> event = new ResourceLoaderEvent<InputStream>(
							this, new ByteArrayInputStream(data), true);
					listener.onResourceEvent(event);
				} else {
					this.getParentResource().getResourceBytes(new IResourceListener<ResourceLoaderEvent<byte[]>>() {
						@Override
						public void onResourceEvent(
								ResourceLoaderEvent<byte[]> event) {
							if (event.isComplete()) {
								MemoryCachedResource.this.data = event.getResult();
								finalListener.onResourceEvent(new ResourceLoaderEvent<InputStream>(
										MemoryCachedResource.this, new ByteArrayInputStream(MemoryCachedResource.this.data), true));
							} else if (event.isFailed()) {
								finalListener.onResourceEvent(new ResourceLoaderEvent<InputStream>(
										MemoryCachedResource.this, event.getCause()));
							}
						}});
				}
			}
		}

		@Override
		public void getResourceBytes(
				IResourceListener<ResourceLoaderEvent<byte[]>> listener,
				ResourceRequestParameters parameters)
				throws IOException {
			if (parameters.isNoChache()) {
				this.getParentResource().getResourceBytes(listener, parameters);
			} else {
				final IResourceListener<ResourceLoaderEvent<byte[]>> finalListener = listener;
				byte[] data	= this.data;
				if (data != null) {
					ResourceLoaderEvent<byte[]> event = new ResourceLoaderEvent<byte[]>(
							this, data, true);
					listener.onResourceEvent(event);
				} else {
					this.getParentResource().getResourceBytes(new IResourceListener<ResourceLoaderEvent<byte[]>>() {
						@Override
						public void onResourceEvent(
								ResourceLoaderEvent<byte[]> event) {
							if (event.isComplete()) {
								MemoryCachedResource.this.data = event.getResult();
							} else if (event.isFailed()) {
							}
							finalListener.onResourceEvent(event);
						}});
				}
			}
		}

	}


	private final IResourceLoader parentResourceLoader;
	private final IObjectCache<String, IResource>	resourceCache	= ObjectCacheFactory.getNewSoftObjectCache();

	public MemoryCachedResourceLoader(IResourceLoader parentResourceLoader) {
		super(parentResourceLoader);
		this.parentResourceLoader	= parentResourceLoader;
	}


	@Override
	public IResource getResource(String name) throws IOException {
		IResource resource	= (IResource) resourceCache.get(name);
		if (resource == null) {
			resource	= parentResourceLoader.getResource(name);
			int modifier	= resource.getModifier();
			if ((resource != null) && ((modifier & IResource.MODIFIER_MEMORY_CACHED) == 0)) {
				resource	= new MemoryCachedResource(resource);
				resourceCache.put(name, resource);
			}
		}
		return resource;
	}

}
