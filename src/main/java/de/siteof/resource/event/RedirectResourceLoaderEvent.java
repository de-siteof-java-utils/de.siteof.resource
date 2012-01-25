package de.siteof.resource.event;

import de.siteof.resource.IResource;

public class RedirectResourceLoaderEvent<T> extends ResourceLoaderEvent<T> {

	private static final long serialVersionUID = 1L;

	private final String redirectUrl;

	public RedirectResourceLoaderEvent(IResource resource,
			String redirectUrl) {
		super(resource, (String) null);
		this.redirectUrl = redirectUrl;
	}

	@Override
	public String toString() {
		return "RedirectResourceLoaderEvent [redirectUrl=" + redirectUrl
				+ ", toString()=" + super.toString() + "]";
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	@Override
	public <TR> RedirectResourceLoaderEvent<TR> cloneFor(IResource resource, TR result) {
		return new RedirectResourceLoaderEvent<TR>(resource, this.redirectUrl);
	}

}
