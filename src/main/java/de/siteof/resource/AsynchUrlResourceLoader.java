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

	protected IResource getUrlResource(String name) throws IOException {
		return new AsynchUrlResource(name, this.getCookieManager(), this.getTaskManager());
	}

	@Override
	public final IResource getResource(String name) throws IOException {
		if (name.indexOf("://") >= 0) {
			return getUrlResource(name);
		} else {
			return super.getResource(name);
		}
	}

	public ICookieManager getCookieManager() {
		return cookieManager;
	}

}
