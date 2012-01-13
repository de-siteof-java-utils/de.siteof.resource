package de.siteof.resource;

import java.io.IOException;

import de.siteof.task.ITaskManager;

public class FileResourceLoader extends AbstractResourceLoader {


	public FileResourceLoader(IResourceLoader parent, ITaskManager taskManager) {
		super(parent, taskManager);
	}


	public IResource getResource(String name) throws IOException {
		if (name.indexOf("://") < 0) {
			return new FileResource(name, getTaskManager());
		} else {
			return super.getResource(name);
		}
	}

}
