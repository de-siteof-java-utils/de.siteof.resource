package de.siteof.resource;

public class ResourceMetaData implements IResourceMetaData {

	private String name;
	private long length;
	private String contentType;

	@Override
	public String toString() {
		return "ResourceMetaData [name=" + name + ", length=" + length
				+ ", contentType=" + contentType + "]";
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
