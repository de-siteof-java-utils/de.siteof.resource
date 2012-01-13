package de.siteof.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MemoryResource extends AbstractResource {

	private final byte[] data;

	public MemoryResource(IResource resource, byte[] data) {
		super(resource);
		this.data = data;
		setModifier(getModifier() | MODIFIER_MEMORY_LOADED);
	}

	public MemoryResource(String name, byte[] data) {
		super(name);
		this.data = data;
		setModifier(getModifier() | MODIFIER_MEMORY_LOADED);
	}

	@Override
	public InputStream getResourceAsStream() throws IOException {
		return new ByteArrayInputStream(data);
	}

	@Override
	public byte[] getResourceBytes() throws IOException {
		return data;
	}

	@Override
	public long getSize() {
		return data.length;
	}

	@Override
	public boolean exists() throws IOException {
		return (data != null);
	}

}
