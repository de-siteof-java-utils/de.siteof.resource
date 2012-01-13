package de.siteof.resource;

import java.io.IOException;
import java.io.InputStream;

import de.siteof.task.ITaskManager;

public interface IResourceLoader {

	IResource getResource(String name) throws IOException;

	byte[] getResourceBytes(String name) throws IOException;

	InputStream getResourceAsStream(String name) throws IOException;

	ITaskManager getTaskManager();

}
