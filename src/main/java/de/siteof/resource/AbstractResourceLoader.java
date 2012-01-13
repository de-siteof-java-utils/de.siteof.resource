package de.siteof.resource;

import java.io.IOException;
import java.io.InputStream;

import de.siteof.task.ITaskManager;

public abstract class AbstractResourceLoader implements IResourceLoader {

	private final IResourceLoader parent;
	private final ITaskManager taskManager;



	public AbstractResourceLoader(IResourceLoader parent) {
		this(parent, (parent != null ? parent.getTaskManager() : null));
	}

	public AbstractResourceLoader(IResourceLoader parent, ITaskManager taskManager) {
		this.parent	= parent;
		this.taskManager = taskManager;
	}



	/* (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.IResourceLoader#getResource(java.lang.String)
	 */
	public IResource getResource(String name) throws IOException {
		if (parent != null) {
			return parent.getResource(name);
		}
		return null;
	}



	public InputStream getResourceAsStream(String name) throws IOException {
		IResource resource	= this.getResource(name);
		return (resource != null ? resource.getResourceAsStream() : null);
	}


	public byte[] getResourceBytes(String name) throws IOException {
		IResource resource	= this.getResource(name);
		return (resource != null ? resource.getResourceBytes() : null);
	}

	/**
	 * @return the taskManager
	 */
	public ITaskManager getTaskManager() {
		return taskManager;
	}

}
