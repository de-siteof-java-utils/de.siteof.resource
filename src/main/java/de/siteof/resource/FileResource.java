package de.siteof.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.siteof.task.ITaskManager;

public class FileResource extends AbstractResource {

	public FileResource(String name, ITaskManager taskManager) {
		super(name, taskManager);
	}

	private File getFile() {
		return new File(getName());
	}

	public boolean exists() throws IOException {
		File file	= getFile();
		return file.exists();
	}

	public InputStream getResourceAsStream() throws IOException {
		InputStream in	= null;
		File file	= getFile();
		if (file.exists()) {
			in	= new FileInputStream(file);
			setModifier(getModifier() | MODIFIER_FILE_CACHED);
		}
		return in;
	}


	public long getLastCached() {
		File file	= getFile();
		return file.lastModified();
	}

	public long getLastModified() {
		File file	= getFile();
		return file.lastModified();
	}

	public long getSize() {
		File file	= getFile();
		return file.length();
	}

}
