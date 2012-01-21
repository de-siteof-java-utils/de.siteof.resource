package de.siteof.resource.event;

import de.siteof.resource.IResource;

public class NameResourceLoaderEvent<T> extends ResourceLoaderEvent<T> {
	
	private static final long serialVersionUID = 1L;
	
	private final String name;

	public NameResourceLoaderEvent(IResource resource,
			String name) {
		super(resource, (String) null);
		this.name = name;
	}

	@Override
	public String toString() {
		return "NameResourceLoaderEvent [name=" + name + ", toString()="
				+ super.toString() + "]";
	}

	public String getName() {
		return name;
	}

}
