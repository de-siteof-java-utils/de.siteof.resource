package de.siteof.resource;

import java.io.IOException;
import java.io.InputStream;

import de.siteof.task.ITaskManager;

public abstract class AbstractResourceLoaderProxy implements IResourceLoader {

	protected abstract IResourceLoader getResourceLoader(String name);

	public IResource getResource(String name) throws IOException {
		return getResourceLoader(name).getResource(name);
	}

	public InputStream getResourceAsStream(String name) throws IOException {
		return getResourceLoader(name).getResourceAsStream(name);
	}

	public byte[] getResourceBytes(String name) throws IOException {
		return getResourceLoader(name).getResourceBytes(name);
	}

	/* (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.IResourceLoader#getTaskManager()
	 */
	public ITaskManager getTaskManager() {
		return null;
	}

}
