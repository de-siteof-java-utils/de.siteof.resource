package de.siteof.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.siteof.resource.event.IResourceListener;
import de.siteof.resource.event.ResourceLoaderEvent;

public class AlternativeResourceProxy extends ResourceProxy {

	private final AlternativeResourceConfiguration configuration;
	private transient IResource currentAlternativeResource;
	private transient Set<String> failedResourceNames;


	private static final Log log	= LogFactory.getLog(AlternativeResourceProxy.class);


	public AlternativeResourceProxy(IResource resource, AlternativeResourceConfiguration configuration) {
		super(resource);
		this.configuration = configuration;
		reset();
	}

	private void reset() {
		currentAlternativeResource = super.getResource();
		failedResourceNames = null;
	}

	@Override
	public void clearCache() {
		reset();
		super.getResource().clearCache();
	}


	private IResource getCurrentAlternativeResource() {
		return currentAlternativeResource;
	}


	private final int getMaxTryCount() {
		return configuration.getMaxTryCount();
	}


	private final int getMaxAlternativeCount() {
		return configuration.getMaxAlternativeCount();
	}


	protected String getNextAlternativeResourceName(String currentName, Set<String> failedResourceNames) {
		String result = null;
		if ((failedResourceNames == null) || (failedResourceNames.size() + 1 < this.getMaxAlternativeCount())) {
			final String PROTOCOL_SEPARATOR = "://";
			int protocolSeparatorPos = currentName.indexOf(PROTOCOL_SEPARATOR);
			if (protocolSeparatorPos >= 0) {
				int subDomainPos = protocolSeparatorPos + PROTOCOL_SEPARATOR.length();
				int domainSeparatorPos = currentName.indexOf('.', subDomainPos);
				if (domainSeparatorPos >= 0) {
					int numericPos = domainSeparatorPos;
					while ((numericPos > subDomainPos) && (Character.isDigit(currentName.charAt(numericPos - 1)))) {
						numericPos--;
					}
					if (numericPos < domainSeparatorPos) {
						int currentNo = 1;
						while ((result == null) && (currentNo <= 10)) {
							String name = currentName.substring(0, numericPos) +
									Integer.toString(currentNo) + currentName.substring(domainSeparatorPos);
							if ((failedResourceNames == null) || (!failedResourceNames.contains(name))) {
								result = name;
							} else {
								currentNo++;
							}
						}
					}
				}
			}
		}
		return result;
	}


	protected IResource getAlternativeResourceByName(String name) throws IOException {
		return configuration.getAlternativeResourceLoader().getResource(name);
	}


	private IResource getNextAlternativeResource() throws IOException {
		if (currentAlternativeResource != null) {
			String currentName = currentAlternativeResource.getName();
			currentAlternativeResource = null;
			if (failedResourceNames == null) {
				failedResourceNames = new HashSet<String>();
			}
			failedResourceNames.add(currentName);
			String alternativeName = getNextAlternativeResourceName(currentName, failedResourceNames);
			if (alternativeName != null) {
				currentAlternativeResource = getAlternativeResourceByName(alternativeName);
			}
		}
		return currentAlternativeResource;
	}

	@Override
	public boolean exists() throws IOException {
		boolean result = false;
		IResource currentResource = this.getCurrentAlternativeResource();
		IOException firstException = null;
		final int maxTryCount = getMaxTryCount();
		int tryCount = 0;
		boolean succeeded = false;
		while ((!succeeded) && (tryCount < maxTryCount) && (currentResource != null)) {
			try {
				result = currentResource.exists();
				if (result) {
					succeeded = true;
				}
			} catch (IOException e) {
				if (firstException == null) {
					firstException = e;
				}
			}
			if (!succeeded) {
				tryCount++;
				if (tryCount < maxTryCount) {
					String failedName = currentResource.getName();
					currentResource = this.getNextAlternativeResource();
					if (currentResource != null) {
						log.info("Failed to check file for existence (" + failedName + "), will try " + currentResource.getName());
					} else {
						log.warn("Failed to check file for existence (" + failedName + "), no more alternatives available");
					}
				}
			}
		}
		if ((!succeeded) && (firstException != null)) {
			throw firstException;
		}
		return result;
	}

	@Override
	public InputStream getResourceAsStream() throws IOException {
		InputStream result = null;
		IResource currentResource = this.getCurrentAlternativeResource();
		IOException firstException = null;
		final int maxTryCount = getMaxTryCount();
		int tryCount = 0;
		boolean succeeded = false;
		while ((!succeeded) && (tryCount < maxTryCount) && (currentResource != null)) {
			try {
				result = currentResource.getResourceAsStream();
				if (result != null) {
					succeeded = true;
				}
			} catch (IOException e) {
				if (firstException == null) {
					firstException = e;
				}
			}
			if (!succeeded) {
				tryCount++;
				if (tryCount < maxTryCount) {
					String failedName = currentResource.getName();
					currentResource = this.getNextAlternativeResource();
					if (currentResource != null) {
						log.info("Failed to get resource stream (" + failedName + "), will try " + currentResource.getName());
					} else {
						log.warn("Failed to get resource stream (" + failedName + "), no more alternatives available");
					}
				}
			}
		}
		if ((!succeeded) && (firstException != null)) {
			throw firstException;
		}
		return result;
	}


	/*
	 * (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.ResourceProxy#getResourceBytes()
	 */
	@Override
	public byte[] getResourceBytes() throws IOException {
		byte[] result = null;
		IResource currentResource = this.getCurrentAlternativeResource();
		IOException firstException = null;
		final int maxTryCount = getMaxTryCount();
		int tryCount = 0;
		boolean succeeded = false;
		while ((!succeeded) && (tryCount < maxTryCount) && (currentResource != null)) {
			try {
				result = currentResource.getResourceBytes();
				if (result != null) {
					succeeded = true;
				}
			} catch (IOException e) {
				if (firstException == null) {
					firstException = e;
				}
			}
			if (!succeeded) {
				tryCount++;
				if (tryCount < maxTryCount) {
					String failedName = currentResource.getName();
					currentResource = this.getNextAlternativeResource();
					if (currentResource != null) {
						log.info("Failed to get resource bytes (" + failedName + "), will try " + currentResource.getName());
					} else {
						log.warn("Failed to get resource bytes (" + failedName + "), no more alternatives available");
					}
				}
			}
		}
		if ((!succeeded) && (firstException != null)) {
			throw firstException;
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.ResourceProxy#abort()
	 */
	@Override
	public void abort() {
//		super.abort();
		IResource currentResource = this.getCurrentAlternativeResource();
		if (currentResource != null) {
			currentResource.abort();
		}
	}

	@Override
	public void getResourceAsStream(
			IResourceListener<ResourceLoaderEvent<InputStream>> listener,
			ResourceRequestParameters parameters)
			throws IOException {
		IResource currentAlternativeResource = this.getCurrentAlternativeResource();
		if (currentAlternativeResource == null) {
			this.reset();
			currentAlternativeResource = this.getCurrentAlternativeResource();
		}
		getResourceAsStream(listener, parameters, currentAlternativeResource, 0, null);
	}

	@Override
	public void getResourceBytes(
			IResourceListener<ResourceLoaderEvent<byte[]>> listener,
			ResourceRequestParameters parameters)
			throws IOException {
		IResource currentAlternativeResource = this.getCurrentAlternativeResource();
		if (currentAlternativeResource == null) {
			this.reset();
			currentAlternativeResource = this.getCurrentAlternativeResource();
		}
		getResourceBytes(listener, parameters, currentAlternativeResource, 0, null);
	}

	private void getResourceAsStream(
			IResourceListener<ResourceLoaderEvent<InputStream>> listener,
			final ResourceRequestParameters parameters,
			final IResource currentResource,
			final int tryCount,
			final Throwable firstCause)
			throws IOException {
		final IResourceListener<ResourceLoaderEvent<InputStream>> finalListener = listener;
		currentResource.getResourceAsStream(new IResourceListener<ResourceLoaderEvent<InputStream>>() {
			@Override
			public void onResourceEvent(
					ResourceLoaderEvent<InputStream> event) {
				if (event.isComplete()) {
					InputStream data	= event.getResult();
					finalListener.onResourceEvent(new ResourceLoaderEvent<InputStream>(
							AlternativeResourceProxy.this, data, true));
				} else if (event.isFailed()) {
					final int maxTryCount = getMaxTryCount();
					IResource alternativeResource = null;
					final Throwable firstOrCurrentCause;
					if (firstCause != null) {
						firstOrCurrentCause = firstCause;
					} else {
						firstOrCurrentCause = event.getCause();
					}
					if (tryCount < maxTryCount) {
						String failedName = currentResource.getName();
						try {
							alternativeResource = AlternativeResourceProxy.this.getNextAlternativeResource();
						} catch (Throwable e) {
							log.warn("Failed to retrieve next alternative resource due to " + e, e);
						}
						if (alternativeResource != null) {
							if (log.isDebugEnabled()) {
								log.debug("Failed to get resource stream (" + failedName + "), will try " + currentResource.getName());
							}
							notifyStatusMessage(finalListener, "trying " + currentResource.getName());
						} else {
							log.warn("Failed to get resource stream (" + failedName + "), no more alternatives available (" + firstOrCurrentCause + ")");
						}
					}
					if (alternativeResource != null) {
						try {
							getResourceAsStream(finalListener, parameters, alternativeResource, tryCount + 1, event.getCause());
						} catch (Throwable e) {
							log.warn("Failed to retrieve next alternative resource bytes due to " + e, e);
							finalListener.onResourceEvent(new ResourceLoaderEvent<InputStream>(
									AlternativeResourceProxy.this, firstOrCurrentCause));
						}
					} else {
						finalListener.onResourceEvent(new ResourceLoaderEvent<InputStream>(
								AlternativeResourceProxy.this, firstOrCurrentCause));
					}
				} else if (event.hasStatusMessage()) {
					finalListener.onResourceEvent(new ResourceLoaderEvent<InputStream>(
							AlternativeResourceProxy.this, event.getStatusMessage()));
				} else {
					finalListener.onResourceEvent(event.cloneFor(
							AlternativeResourceProxy.this, (InputStream) null));
				}
			}}, parameters);
	}

	private void getResourceBytes(
			IResourceListener<ResourceLoaderEvent<byte[]>> listener,
			final ResourceRequestParameters parameters,
			final IResource currentResource,
			final int tryCount,
			final Throwable firstCause)
			throws IOException {
		final IResourceListener<ResourceLoaderEvent<byte[]>> finalListener = listener;
		currentResource.getResourceBytes(new IResourceListener<ResourceLoaderEvent<byte[]>>() {
			@Override
			public void onResourceEvent(
					ResourceLoaderEvent<byte[]> event) {
				if (event.isComplete()) {
					byte[] data	= event.getResult();
					finalListener.onResourceEvent(new ResourceLoaderEvent<byte[]>(
							AlternativeResourceProxy.this, data, true));
				} else if (event.isFailed()) {
					final int maxTryCount = getMaxTryCount();
					IResource alternativeResource = null;
					final Throwable firstOrCurrentCause;
					if (firstCause != null) {
						firstOrCurrentCause = firstCause;
					} else {
						firstOrCurrentCause = event.getCause();
					}
					if (tryCount < maxTryCount) {
						String failedName = currentResource.getName();
						try {
							alternativeResource = AlternativeResourceProxy.this.getNextAlternativeResource();
						} catch (Throwable e) {
							log.warn("Failed to retrieve next alternative resource due to " + e, e);
						}
						if (alternativeResource != null) {
							if (log.isDebugEnabled()) {
								log.debug("Failed to get resource stream (" + failedName + "), will try " + currentResource.getName());
							}
							notifyStatusMessage(finalListener, "trying " + currentResource.getName());
						} else {
							log.warn("Failed to get resource stream (" + failedName + "), no more alternatives available (" + firstOrCurrentCause + ")");
						}
					}
					if (alternativeResource != null) {
						try {
							getResourceBytes(finalListener, parameters, alternativeResource, tryCount + 1, event.getCause());
						} catch (Throwable e) {
							log.warn("Failed to retrieve next alternative resource bytes due to " + e, e);
							finalListener.onResourceEvent(new ResourceLoaderEvent<byte[]>(
									AlternativeResourceProxy.this, firstOrCurrentCause));
						}
					} else {
						finalListener.onResourceEvent(new ResourceLoaderEvent<byte[]>(
								AlternativeResourceProxy.this, firstOrCurrentCause));
					}
				} else if (event.hasStatusMessage()) {
					finalListener.onResourceEvent(new ResourceLoaderEvent<byte[]>(
							AlternativeResourceProxy.this, event.getStatusMessage()));
				}
			}}, parameters);
	}

}
