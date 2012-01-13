package de.siteof.resource.util;

public class ObjectHolder<T> {

	private T object;

	public ObjectHolder() {
	}

	public ObjectHolder(T object) {
		this.object = object;
	}

	/**
	 * @return the object
	 */
	public T getObject() {
		return object;
	}

	/**
	 * @param object the object to set
	 */
	public void setObject(T object) {
		this.object = object;
	}

}
