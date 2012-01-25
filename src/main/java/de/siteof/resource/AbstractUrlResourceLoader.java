package de.siteof.resource;

import java.io.IOException;

import de.siteof.task.ITaskManager;

public abstract class AbstractUrlResourceLoader extends AbstractResourceLoader {

	public AbstractUrlResourceLoader(IResourceLoader parent) {
		super(parent);
	}

	public AbstractUrlResourceLoader(IResourceLoader parent, ITaskManager taskManager) {
		super(parent, taskManager);
	}

	protected abstract IResource getUrlResource(String name) throws IOException;

	@Override
	public final IResource getResource(String name) throws IOException {
		if (name.indexOf("://") >= 0) {
			return getUrlResource(name);
		} else {
			return super.getResource(name);
		}
	}

}
