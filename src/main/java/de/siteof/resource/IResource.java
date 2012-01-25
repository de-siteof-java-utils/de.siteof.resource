package de.siteof.resource;

import java.io.IOException;
import java.io.InputStream;

import de.siteof.resource.event.IResourceListener;
import de.siteof.resource.event.ResourceLoaderEvent;
import de.siteof.task.ITaskManager;

public interface IResource {

	int MODIFIER_CACHED	= 1 << 0;
	int MODIFIER_LOADED	= 2 << 0;
	int MODIFIER_MEMORY_CACHED	= (1 << 3) | MODIFIER_CACHED;
	int MODIFIER_FILE_CACHED	= (1 << 4) | MODIFIER_CACHED;
	int MODIFIER_MEMORY_LOADED	= MODIFIER_MEMORY_CACHED | MODIFIER_LOADED;
	int MODIFIER_FILE_LOADED	= MODIFIER_FILE_CACHED | MODIFIER_LOADED;

	IResourceContext getContext();

	String getName();

	boolean exists() throws IOException;

	int getModifier();

	long getSize();

	long getLastModified();

	long getLastCached();

	void clearCache();

	byte[] getResourceBytes() throws IOException;

	void getResourceBytes(IResourceListener<ResourceLoaderEvent<byte[]>> listener) throws IOException;

	void getResourceBytes(IResourceListener<ResourceLoaderEvent<byte[]>> listener,
			ResourceRequestParameters parameters) throws IOException;

	InputStream getResourceAsStream() throws IOException;

	void getResourceAsStream(IResourceListener<ResourceLoaderEvent<InputStream>> listener) throws IOException;

	void getResourceAsStream(IResourceListener<ResourceLoaderEvent<InputStream>> listener,
			ResourceRequestParameters parameters) throws IOException;


	/**
	 * Aborts any pending requests (if supported).
	 */
	void abort();

	ITaskManager getTaskManager();

}
