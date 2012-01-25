package de.siteof.resource.event;

import java.util.EventObject;

import de.siteof.resource.IResource;

public class ResourceLoaderEvent<T> extends EventObject implements Cloneable {

	private static final long serialVersionUID = 1L;

	private final T result;
	private final boolean complete;

	private final Throwable cause;
	private final CharSequence statusMessage;

	protected ResourceLoaderEvent(IResource resource, T result, boolean complete, Throwable cause,
			CharSequence statusMessage) {
		super(resource);
		this.result = result;
		this.complete = complete;
		this.cause = cause;
		this.statusMessage = statusMessage;
	}

	public ResourceLoaderEvent(IResource resource, T result, boolean complete) {
		this(resource, result, complete, null, null);
	}

	public ResourceLoaderEvent(IResource resource, Throwable cause) {
		this(resource, null, false, cause, null);
	}

	public ResourceLoaderEvent(IResource resource, CharSequence statusMessage) {
		this(resource, null, false, null, statusMessage);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResourceLoaderEvent<T> clone() {
		try {
			return (ResourceLoaderEvent<T>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("clone failed", e);
		}
	}

	public ResourceLoaderEvent<T> cloneFor(IResource resource) {
		ResourceLoaderEvent<T> event = this.clone();
		event.source = resource;
		return event;
	}

	public <TR> ResourceLoaderEvent<TR> cloneFor(IResource resource, TR result) {
		return new ResourceLoaderEvent<TR>(resource, result,
				this.complete, this.cause, this.statusMessage);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + ", complete=[" + complete +
		"], result=[" + result +
		"], cause=[" + cause +
		"], statusMessage=[" + statusMessage + "]";
	}

	public IResource getResource() {
		return (IResource) this.getSource();
	}

	public boolean isFailed() {
		return (cause != null);
	}

	public boolean hasStatusMessage() {
		return (statusMessage != null);
	}

	/**
	 * @return the result
	 */
	public T getResult() {
		return result;
	}

	/**
	 * @return the complete
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * @return the cause
	 */
	public Throwable getCause() {
		return cause;
	}

	/**
	 * @return the statusMessage
	 */
	public CharSequence getStatusMessage() {
		return statusMessage;
	}

}
