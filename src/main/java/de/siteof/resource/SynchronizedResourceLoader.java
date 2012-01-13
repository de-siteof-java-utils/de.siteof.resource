package de.siteof.resource;

import java.io.IOException;

public class SynchronizedResourceLoader extends AbstractResourceLoader {

	private final IResourceLoader resourceLoader;

	public SynchronizedResourceLoader(IResourceLoader resourceLoader) {
		super(resourceLoader);
		this.resourceLoader	= resourceLoader;
	}

	public IResource getResource(String name) throws IOException {
		IResource resource;
		synchronized (this) {
			resource	= resourceLoader.getResource(name);
		}
		if (resource != null) {
			resource	= new SynchronizedResourceProxy(resource);
		}
		return resource;
	}

}
