package de.siteof.resource.event;

import de.siteof.resource.IResource;
import de.siteof.resource.IResourceMetaData;

public class MetaResourceLoaderEvent<T> extends ResourceLoaderEvent<T> {

	private static final long serialVersionUID = 1L;

	private final IResourceMetaData metaData;

	public MetaResourceLoaderEvent(IResource resource,
			IResourceMetaData metaData) {
		super(resource, (String) null);
		this.metaData = metaData;
	}

	@Override
	public String toString() {
		return "MetaResourceLoaderEvent [metaData=" + metaData + "]";
	}

	public IResourceMetaData getMetaData() {
		return metaData;
	}

}
