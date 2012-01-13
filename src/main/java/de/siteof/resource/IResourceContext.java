package de.siteof.resource;

public interface IResourceContext {

	void dispose();

	Object getMeta(Object key);

	void setMeta(Object key, Object value);

}
