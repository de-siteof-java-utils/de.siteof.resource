package de.siteof.resource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.siteof.resource.event.IResourceListener;
import de.siteof.resource.event.MetaResourceLoaderEvent;
import de.siteof.resource.event.ResourceLoaderEvent;
import de.siteof.resource.util.ObjectHolder;

public class ResourceDownloader {

	private static final Log log = LogFactory.getLog(ResourceDownloader.class);

	private final IResourceLoader resourceLoader;
	private final String mediaUrl;
	private final CountDownLatch lock = new CountDownLatch(1);
	private final AtomicBoolean downloading = new AtomicBoolean();
	private final AtomicBoolean downloadResult = new AtomicBoolean();
	private int minimumSize = 3 * 1024;
	private final int inspectHeaderLength = 1024;
	private final boolean ignoreHtmlResponse = true;
	private final ResourceRequestParameters requestParameters;
	private File downloadDirectory;
	private OutputStream outputStream;
	private String title;
	private String filename;
	private String contentType;
	private File targetFile;
	private File tempFile;

	private static final Map<String, String> mimeExtensionMap;

	static {
		// TODO use utility / complete map
		mimeExtensionMap = new HashMap<String, String>();
		mimeExtensionMap.put("video/mp4", "mp4");
		mimeExtensionMap.put("video/mpeg", "mpg");
		mimeExtensionMap.put("video/x-flv", "flv");
	}

	public ResourceDownloader(IResourceLoader resourceLoader, String mediaUrl) {
		this.resourceLoader = resourceLoader;
		this.mediaUrl = mediaUrl;
		requestParameters = new ResourceRequestParameters();
		requestParameters.setNoChache(true);
	}

	public String getResourceName() {
		String result = filename;
		if ((result == null) && (this.title != null) && (this.title.length() > 0)) {
			result = this.title;
			if (this.contentType != null) {
				String extension = mimeExtensionMap.get(contentType);
				if (extension == null) {
					log.warn("no extension mapped for contentType=[" + this.contentType + "]");
				} else {
					result = result + "." + extension;
				}
			}
		}
		if (result == null) {
			result = this.mediaUrl;
			int index = result.lastIndexOf('/');
			if (index >= 0) {
				result = result.substring(index + 1);
			}
			result = result.replace('?', '_').replace('&', '_').replace("%20", " ");
		} else {
			// ensure that the filename doesn't contain any path (as we can't trust it)
			result = result.replace('\\', '/');
			int index = result.lastIndexOf('/');
			if (index >= 0) {
				result = result.substring(index + 1);
			}
		}
		return result;
	}

	public OutputStream createOutputStream() throws IOException {
		String resourceName = getResourceName();
		File downloadDirectory = this.downloadDirectory;
		if (downloadDirectory == null) {
			downloadDirectory = new File(".");
		}
		File file = new File(downloadDirectory, resourceName);
		if (file.exists()) {
			if (log.isDebugEnabled()) {
				log.debug("file exists, trying alternative, file=" + file);
			}
			file = null;
			for (int i = 2; i <= 1000; i++) {
				File f = new File(downloadDirectory, resourceName + i);
				if (!f.exists()) {
					file = f;
					break;
				}
			}
			if (file == null) {
				throw new IOException("file already exists and no alternative found, file=" + resourceName);
			}
		}
		File tempFile = new File(downloadDirectory, file.getName() + ".part");
		if (tempFile.exists()) {
			if (tempFile.isFile()) {
				if (log.isDebugEnabled()) {
					log.debug("temp file exists, deleting it, file=" + tempFile);
				}
				tempFile.delete();
			} else {
				throw new IOException("temp file already exists and but appears to be a directory, file=" + tempFile);
			}
		}
		this.targetFile = file;
		this.tempFile = tempFile;
		return new FileOutputStream(tempFile);
	}

	protected void onComplete() {
		if ((this.tempFile != null) && (this.targetFile != null)) {
			if (this.targetFile.exists()) {
				log.warn("can't rename file, target file exists, targetFile=" + targetFile);
			} else {
				if (!this.tempFile.renameTo(this.targetFile)) {
					log.warn("failed to rename file, target file exists, targetFile=" + targetFile);
				}
			}
		}
	}

	protected void onFailed() {
	}

	public void download() throws IOException {
		IResource resource = resourceLoader.getResource(mediaUrl);
		if (resource == null) {
			log.error("resource is null");
			downloadResult.set(false);
			return;
		}
		final int bufferLength = Math.max(inspectHeaderLength, minimumSize);
		final ObjectHolder<Integer> tryIndexHolder = new ObjectHolder<Integer>(new Integer(0));
		final ObjectHolder<IResourceListener<ResourceLoaderEvent<byte[]>>> callbackListenerHolder = new ObjectHolder<IResourceListener<ResourceLoaderEvent<byte[]>>>();
		callbackListenerHolder.setObject(new IResourceListener<ResourceLoaderEvent<byte[]>>() {

			private final AtomicBoolean done = new AtomicBoolean();
			private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(bufferLength);
			private final AtomicBoolean bufferWritten = new AtomicBoolean();
			private final AtomicBoolean cancelled = new AtomicBoolean();
			private final AtomicReference<OutputStream> out = new AtomicReference<OutputStream>();
			private final AtomicLong bytesWritten = new AtomicLong();

			private void cancel() {
				cancelled.set(true);
			}

			private boolean isCancelled() {
				return cancelled.get();
			}

			private boolean checkHeader(byte[] data) {
				boolean result = true;
				String s = new String(data, 0, Math.min(1024, data.length));
				if ((ignoreHtmlResponse) && (s.indexOf("<html") >= 0)) {
					log.warn("data appears to be HTML, mediaUrl=" + mediaUrl);
					cancel();
					result = false;
				}
				return result;
			}

			private boolean checkBuffer() {
				return this.checkHeader(buffer.toByteArray());
			}

			private boolean checkMinimumSize(int size) {
				boolean result = true;
				if (size < minimumSize) {
					log.warn("to few bytes downloaded, minSize=" + minimumSize + ", size=" + size + ", mediaUrl=" + mediaUrl);
					result = false;
				}
				return result;
			}

			private boolean writeContent(byte[] data, boolean complete) {
				return writeContent(data, 0, data.length, complete);
			}

			private boolean writeContent(byte[] data, int offset, int length, boolean complete) {
				if (log.isDebugEnabled()) {
					log.debug("length=" + length + ", complete=" + complete);
				}
				boolean result = false;
				OutputStream out = this.out.get();
				try {
					if (out == null) {
						out = ResourceDownloader.this.outputStream;
						if (out == null) {
							out	= ResourceDownloader.this.createOutputStream();
						}
						this.out.set(out);
					}
					if (length > 0) {
						out.write(data, offset, length);
						bytesWritten.addAndGet(length);
					}
					result = true;
				} catch (IOException e) {
					int tryIndex = tryIndexHolder.getObject().intValue();
					log.error("failed to save media - " + e + " (tryIndex=" + tryIndex + ")", e);
					closeOutputStream(out);
				} finally {
					closeOutputStream(out);
				}
				return result;
			}

			private void onSuccess() {
				done.set(true);
				notifyDownloadEnd(true);
			}

			private void onFailed() {
				done.set(true);
				closeOutputStream();
				notifyDownloadEnd(false);
			}

			private void closeOutputStream() {
				closeOutputStream(this.out.get());
			}

			private void closeOutputStream(OutputStream out) {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						int tryIndex = tryIndexHolder.getObject().intValue();
						log.warn("failed to close file - " + e + " (tryIndex=" + tryIndex + ")", e);
					}
					this.out.set(null);
				}
			}

			@Override
			public void onResourceEvent(
					ResourceLoaderEvent<byte[]> event) {
				boolean error = false;
				int tryIndex = tryIndexHolder.getObject().intValue();
				try {
					if (log.isDebugEnabled()) {
						log.debug("event=" + event);
					}
					if (event instanceof MetaResourceLoaderEvent) {
						IResourceMetaData metaData = ((MetaResourceLoaderEvent<?>) event).getMetaData();
						filename = metaData.getName();
						contentType = metaData.getContentType();
					} else if (event.isComplete()) {
						byte[] data = event.getResult();
						if (!bufferWritten.get()) {
							// the buffer will only be written if it's above the minimum size
							// i.e. if we haven't written the buffer then we should check now.
							if (!checkMinimumSize(buffer.size() + (data != null ? data.length : 0))) {
								log.warn("length below the minimum size, mediaUrl=" + mediaUrl);
								error = true;
							} else {
								if (!writeContent(buffer.toByteArray(), false)) {
									error = true;
								}
								buffer.reset();
								bufferWritten.set(true);
							}
						}
						if (!error) {
							if (data != null) {
								if (!writeContent(data, true)) {
									error = true;
								}
							} else {
								log.warn("no data returned, mediaUrl=" + mediaUrl);
							}
							onSuccess();
						}
					} else if (event.isFailed()) {
						if (log.isDebugEnabled()) {
							log.debug("failed to resource - " + event.getCause(), event.getCause());
						}
						onFailed();
					} else {
						// this is only an intermediate event
						byte[] data = event.getResult();
						if ((data != null) && (data.length > 0)) {
							if (bufferWritten.get()) {
								if (!writeContent(data, false)) {
									error = true;
								}
							} else {
								int readBytes = Math.min(data.length, bufferLength - buffer.size());
								if (log.isDebugEnabled()) {
									log.info("readBytes=" + readBytes + ", data.length=" + data.length +
											", buffer.size=" + buffer.size());
								}
								if (readBytes > 0) {
									buffer.write(data, 0, readBytes);
								}
								if (buffer.size() >= inspectHeaderLength) {
									checkBuffer();
								}
								if ((!this.isCancelled()) && (buffer.size() >= bufferLength)) {
									if (!writeContent(buffer.toByteArray(), false)) {
										error = true;
									} else {
										buffer.reset();
										bufferWritten.set(true);
										if (readBytes < data.length) {
											if (!writeContent(data, readBytes, data.length - readBytes, false)) {
												error = true;
											}
										}
									}
								}
							}
						}
					}
				} catch (Throwable e) {
					log.error("unexpected error - " + e, e);
					error = true;
				}
				if (error) {
					if (tryIndex < 3) {
						if (bytesWritten.get() > 0) {
							this.closeOutputStream();
							bytesWritten.set(0);
						}
						try {
							IResource resource = resourceLoader.getResource(mediaUrl);
							if (tryIndex > 0) {
								resource.clearCache();
							}
							tryIndexHolder.setObject(new Integer(tryIndex + 1));
							resource.getResourceBytes(callbackListenerHolder.getObject());
						} catch (IOException e) {
							log.error("failed to save media - " + e + " (tryIndex=" + tryIndex + ")", e);
						}
					} else {
						if (!done.get()) {
							onFailed();
						}
					}
				}
			}});
		downloading.set(true);
		resource.getResourceBytes(callbackListenerHolder.getObject(), this.requestParameters);
	}

	private void notifyDownloadEnd(boolean status) {
		if (status) {
			onComplete();
		}
		downloadResult.set(status);
		downloading.set(false);
		lock.countDown();
//		synchronized (lock) {
//			downloadResult.set(status);
//			downloading.set(false);
//			lock.notify();
//		}
	}

	public boolean waitForDownload() throws InterruptedException {
		if (downloading.get()) {
			lock.await();
		}
//		synchronized (lock) {
//			if (downloading.get())
//			lock.wait();
//		}
		return downloadResult.get();
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public int getMinimumSize() {
		return minimumSize;
	}

	public void setMinimumSize(int minimumSize) {
		this.minimumSize = minimumSize;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public File getDownloadDirectory() {
		return downloadDirectory;
	}

	public void setDownloadDirectory(File downloadDirectory) {
		this.downloadDirectory = downloadDirectory;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
