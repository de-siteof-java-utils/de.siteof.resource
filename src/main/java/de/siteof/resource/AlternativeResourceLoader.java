package de.siteof.resource;

import java.io.IOException;

public class AlternativeResourceLoader extends AbstractResourceLoader {

	private final IResourceLoader resourceLoader;
	private final AlternativeResourceConfiguration configuration;

	public AlternativeResourceLoader(IResourceLoader resourceLoader) {
		super(resourceLoader);
		this.resourceLoader	= resourceLoader;
		configuration = new AlternativeResourceConfiguration();
		configuration.setAlternativeResourceLoader(resourceLoader);
	}

	public IResource getResource(String name) throws IOException {
		IResource resource = resourceLoader.getResource(name);
		if (resource != null) {
			resource	= new AlternativeResourceProxy(resource, configuration);
		}
		return resource;
	}

}
