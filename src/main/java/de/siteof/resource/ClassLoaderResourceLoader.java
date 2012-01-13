package de.siteof.resource;

import java.io.IOException;

public class ClassLoaderResourceLoader extends AbstractResourceLoader {


	public ClassLoaderResourceLoader(IResourceLoader parent) {
		super(parent);
	}


	public IResource getResource(String name) throws IOException {
		if ((name.indexOf("://") < 0) &&
				(!name.startsWith(".")) &&
				(!name.startsWith("/")) &&
				(name.indexOf(":\\") < 0) &&
				(name.indexOf(":/") < 0)) {
			return new ClassLoaderResource(name);
		} else {
			return super.getResource(name);
		}
	}

}
