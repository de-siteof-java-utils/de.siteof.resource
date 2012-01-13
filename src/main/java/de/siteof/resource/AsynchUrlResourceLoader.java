package de.siteof.resource;

import java.io.IOException;

import de.siteof.task.ITaskManager;

public class AsynchUrlResourceLoader extends AbstractResourceLoader {

	private final ICookieManager cookieManager;


	public AsynchUrlResourceLoader(IResourceLoader parent, ICookieManager cookieManager,
			ITaskManager taskManager) {
		super(parent, taskManager);
		this.cookieManager = cookieManager;
	}


	public IResource getResource(String name) throws IOException {
		if (name.indexOf("://") >= 0) {
			return new AsynchUrlResource(name, cookieManager, this.getTaskManager());
		} else {
			return super.getResource(name);
		}
	}

}
