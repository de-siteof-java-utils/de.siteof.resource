package de.siteof.resource;

import java.io.IOException;
import java.io.InputStream;

public class ClassLoaderResource extends AbstractResource {

	private final ClassLoader classLoader;

	public ClassLoaderResource(String name) {
		this(null, name);
	}

	public ClassLoaderResource(ClassLoader classLoader, String name) {
		super(name);
		if (classLoader != null) {
			this.classLoader	= classLoader;
		} else {
			this.classLoader	= this.getClass().getClassLoader();
		}
	}


	public boolean exists() throws IOException {
		boolean result;
		InputStream in	= this.getResourceAsStream();
		if (in != null) {
			in.close();
			result	= true;
		} else {
			result	= false;
		}
		return result;
	}


	public InputStream getResourceAsStream() throws IOException {
		InputStream in	= classLoader.getResourceAsStream(getName());
		if (in != null) {
			setModifier(getModifier() | MODIFIER_FILE_CACHED);
		}
		return in;
	}

	public long getSize() {
		return 0;
	}

}
