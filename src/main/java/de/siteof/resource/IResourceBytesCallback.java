package de.siteof.resource;

public interface IResourceBytesCallback {

	void onCompleteResourceBytes(IResource resource, byte[] bytes);

	void onFailedResourceBytes(IResource resource, Throwable cause);

}
