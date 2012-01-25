package de.siteof.resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.siteof.cache.IObjectCache;
import de.siteof.cache.ObjectCache;
import de.siteof.resource.event.IResourceListener;
import de.siteof.resource.event.ResourceLoaderEvent;
import de.siteof.resource.util.IOUtil;
import de.siteof.task.ITaskManager;

public class FileCachedResourceLoader extends AbstractResourceLoader {

	private static interface ICacheGarbageCollector {

		void triggerBeforeWrite(Object key, long size);
		void triggerAfterWrite(Object key, long size);
	}

	private static class FileCacheGarbageCollector implements ICacheGarbageCollector {

		private static class FileInfo {
			private final String name;
			private long size;
			private long timestamp;

			public FileInfo(String name) {
				this.name	= name;
			}

			/**
			 * @return the size
			 */
			public long getSize() {
				return size;
			}
			/**
			 * @param size the size to set
			 */
			public void setSize(long size) {
				this.size = size;
			}
			/**
			 * @return the timestamp
			 */
			public long getTimestamp() {
				return timestamp;
			}
			/**
			 * @param timestamp the timestamp to set
			 */
			public void setTimestamp(long timestamp) {
				this.timestamp = timestamp;
			}
			/**
			 * @return the name
			 */
			public String getName() {
				return name;
			}
		}

		private static class FileInfoTimestampComparator implements Comparator<FileInfo> {

			private static Comparator<FileInfo> comparator	= new FileInfoTimestampComparator();

			public static Comparator<FileInfo> getInstance() {
				return comparator;
			}

			@Override
			public int compare(FileInfo fileInfo1, FileInfo fileInfo2) {
				long diff	= fileInfo1.getTimestamp() - fileInfo2.getTimestamp();
				if (diff < 0) {
					return -1;
				} else if (diff > 0) {
					return 1;
				} else {
					return 0;
				}
			}

		}

		private final String cacheDirectory;
		private final long maxTotalSize;
		private final int maxFileCount;
//		private SortedSet fileInfoSet;
		private LinkedList<FileInfo> fileInfoList;
		private Map<String, FileInfo> fileInfoByNameMap;
		private long totalSize;

		private static final Log log	= LogFactory.getLog(FileCachedResourceLoader.class);

		public FileCacheGarbageCollector(String cacheDirectory, long maxTotalSize, int maxFileCount) {
			this.cacheDirectory	= cacheDirectory;
			this.maxTotalSize		= maxTotalSize;
			this.maxFileCount		= maxFileCount;
		}

		private void updateFileInfo(String name, long size) {
			FileInfo fileInfo	= (FileInfo) fileInfoByNameMap.get(name);
			if (fileInfo != null) {
				totalSize	= totalSize - fileInfo.getSize() + size;
				fileInfo.setSize(size);
				fileInfo.setTimestamp(System.currentTimeMillis());
			}
		}

		@Override
		public void triggerBeforeWrite(Object key, long size) {
			synchronized (this) {
				String thisCacheFileName	= (String) key;
				if (fileInfoList == null) {
					File cacheDirectory	= new File(this.cacheDirectory);
					if (!cacheDirectory.exists()) {
						return;
					}
					log.debug("initialising file cache garbage collector");
					//fileInfoSet			= new TreeSet(new FileInfoTimestampComparator());
					fileInfoList		= new LinkedList<FileInfo>();
					fileInfoByNameMap	= new HashMap<String, FileInfo>();
					totalSize		= 0;
					File[] files	= cacheDirectory.listFiles();
					List<FileInfo> list		= new ArrayList<FileInfo>();
					for (int i = 0; i < files.length; i++) {
						String name	= files[i].getName();
						FileInfo fileInfo	= new FileInfo(name);
						fileInfo.setSize(files[i].length());
						fileInfo.setTimestamp(files[i].lastModified());
						totalSize	+= fileInfo.getSize();
						list.add(fileInfo);
						fileInfoByNameMap.put(name, fileInfo);
					}
					//fileInfoSet.addAll(list);
					Collections.sort(list, FileInfoTimestampComparator.getInstance());
					fileInfoList.addAll(list);
				}

				updateFileInfo(thisCacheFileName, size);

				while (((maxTotalSize > 0) && (totalSize > maxTotalSize)) ||
						((maxFileCount > 0) && (fileInfoList.size() > maxFileCount))) {
					if (fileInfoList.isEmpty()) {
						log.error("illegal file cache garbage collector state: totalSize=" + totalSize +
								", fileInfoList=" + fileInfoList + ", fileInfoByNameMap=" + fileInfoByNameMap);
						totalSize	= 0;
						fileInfoList.clear();	// clear all
						fileInfoByNameMap.clear();
						fileInfoList	= null;
						return;
					} else {
						FileInfo fileInfo	= (FileInfo) fileInfoList.get(0);
						if (log.isDebugEnabled()) {
							log.debug("removing file from cache: " + fileInfo.getName());
						}
						fileInfoByNameMap.remove(fileInfo.getName());
//						fileInfoSet.remove(fileInfo);
						fileInfoList.remove(0);
						totalSize	-= fileInfo.getSize();
						File file	= new File(cacheDirectory, fileInfo.getName());
						file.delete();
					}
				}
			}
		}

		@Override
		public void triggerAfterWrite(Object key, long size) {
			synchronized (this) {
				String thisCacheFileName	= (String) key;
				updateFileInfo(thisCacheFileName, size);
			}
		}
	}

	private static interface IFileCacheContext {

		ICacheGarbageCollector getCacheGarbageCollector();
	}


	private static class FileCacheContext implements IFileCacheContext {

		private final ICacheGarbageCollector cacheGarbageCollector;

		public FileCacheContext(ICacheGarbageCollector cacheGarbageCollector) {
			this.cacheGarbageCollector	= cacheGarbageCollector;
		}

		@Override
		public ICacheGarbageCollector getCacheGarbageCollector() {
			return cacheGarbageCollector;
		}
	}



	private static class FileCachedResource extends AbstractResource {

		private boolean cached;
		private boolean exists;
		private final File cacheFile;
		private final IFileCacheContext cacheContext;

		private static final Log log	= LogFactory.getLog(FileCachedResourceLoader.class);


		public FileCachedResource(IResource resource, File cacheFile, IFileCacheContext cacheContext,
				ITaskManager taskManager) {
			super(resource, taskManager);
			this.cacheFile		= cacheFile;
			this.cacheContext	= cacheContext;
			this.cached		= cacheFile.exists();
			this.exists		= this.cached;
		}

		private File getCacheFile() {
			return this.cacheFile;
		}

		@Override
		public void clearCache() {
			cached	= false;
			exists	= false;
			super.clearCache();
			setModifier(getModifier() & (~MODIFIER_FILE_CACHED));
			getCacheFile().delete();
		}

		@Override
		public boolean exists() throws IOException {
			if (cached) {
				return (exists);
			} else {
				exists	= super.exists();
				cached	= true;
			}
			return (exists);
		}

		@Override
		public InputStream getResourceAsStream() throws IOException {
			File cacheFile	= this.getCacheFile();
			if (cacheFile.exists()) {
				return new FileInputStream(cacheFile);
			} else {
				byte[] data	= this.getResourceBytes();
				if (data != null) {
					return new ByteArrayInputStream(data);
				}
			}
			return null;
		}

		private void updateCache(byte[] data) {
			if (data != null) {
				File cacheFile	= this.getCacheFile();
				try {
					cacheContext.getCacheGarbageCollector().triggerBeforeWrite(cacheFile.getName(), data.length);
					OutputStream out	= new FileOutputStream(cacheFile);
					try {
						out.write(data);
					} finally {
						out.close();
					}
					cacheContext.getCacheGarbageCollector().triggerAfterWrite(cacheFile.getName(), data.length);
					this.cached	= true;
					setModifier(getModifier() | MODIFIER_FILE_CACHED);
				} catch (IOException e) {
					log.error("cache could not be written to - " + e, e);
					cacheFile.delete();
				}
			}
		}

		@Override
		public byte[] getResourceBytes() throws IOException {
			byte[] result	= null;
			File cacheFile	= this.getCacheFile();
			if (cacheFile.exists()) {
				InputStream in	= new FileInputStream(cacheFile);
				try {
					result	= IOUtil.readAllFromStream(in);
				} finally {
					in.close();
				}
			}
			if (result == null) {
				result	= super.getResourceBytes();
				if (result != null) {
					updateCache(result);
				}
			}
			return result;
		}

		@Override
		public void getResourceAsStream(
				IResourceListener<ResourceLoaderEvent<InputStream>> listener,
				ResourceRequestParameters parameters)
				throws IOException {
			if (parameters.isNoChache()) {
				this.getParentResource().getResourceAsStream(listener, parameters);
			} else {
				final IResourceListener<ResourceLoaderEvent<InputStream>> finalListener = listener;
				File cacheFile	= this.getCacheFile();
				if (cacheFile.exists()) {
					InputStream in	= new FileInputStream(cacheFile);
					try {
						byte[] data	= IOUtil.readAllFromStream(in);
						ResourceLoaderEvent<InputStream> event = new ResourceLoaderEvent<InputStream>(
								this, new ByteArrayInputStream(data), true);
						listener.onResourceEvent(event);
					} finally {
						in.close();
					}
	//				this.getTaskManager().addTask(new AbstractTask() {
	//					public void execute() throws Exception {
	//						ResourceLoaderEvent<InputStream> event;
	//						try {
	//							event = new ResourceLoaderEvent<InputStream>(
	//									FileCachedResource.this, getResourceAsStream(), true);
	//						} catch (Throwable e) {
	//							event = new ResourceLoaderEvent<InputStream>(
	//									FileCachedResource.this, e);
	//						}
	//						finalListener.onResourceEvent(event);
	//					}});
				} else {
					this.getParentResource().getResourceBytes(new IResourceListener<ResourceLoaderEvent<byte[]>>() {
						@Override
						public void onResourceEvent(
								ResourceLoaderEvent<byte[]> event) {
							if (event.isComplete()) {
								// TODO this doesn't handle chunks
								byte[] data	= event.getResult();
								updateCache(data);
								finalListener.onResourceEvent(new ResourceLoaderEvent<InputStream>(
										FileCachedResource.this, new ByteArrayInputStream(data), true));
							} else if (event.isFailed()) {
								finalListener.onResourceEvent(new ResourceLoaderEvent<InputStream>(
										FileCachedResource.this, event.getCause()));
							}
						}}, parameters);
				}
			}
		}

		@Override
		public void getResourceBytes(
				IResourceListener<ResourceLoaderEvent<byte[]>> listener,
				ResourceRequestParameters parameters)
				throws IOException {
			if (parameters.isNoChache()) {
				this.getParentResource().getResourceBytes(listener, parameters);
			} else {
				final IResourceListener<ResourceLoaderEvent<byte[]>> finalListener = listener;
				File cacheFile	= this.getCacheFile();
				if (cacheFile.exists()) {
					InputStream in	= new FileInputStream(cacheFile);
					try {
						byte[] data	= IOUtil.readAllFromStream(in);
						ResourceLoaderEvent<byte[]> event = new ResourceLoaderEvent<byte[]>(
								this, data, true);
						listener.onResourceEvent(event);
					} finally {
						in.close();
					}
	//				this.getTaskManager().addTask(new AbstractTask() {
	//					public void execute() throws Exception {
	//						ResourceLoaderEvent<InputStream> event;
	//						try {
	//							event = new ResourceLoaderEvent<InputStream>(
	//									FileCachedResource.this, getResourceAsStream(), true);
	//						} catch (Throwable e) {
	//							event = new ResourceLoaderEvent<InputStream>(
	//									FileCachedResource.this, e);
	//						}
	//						finalListener.onResourceEvent(event);
	//					}});
				} else {
					this.getParentResource().getResourceBytes(new IResourceListener<ResourceLoaderEvent<byte[]>>() {
						@Override
						public void onResourceEvent(
								ResourceLoaderEvent<byte[]> event) {
							if (event.isComplete()) {
								byte[] data	= event.getResult();
								updateCache(data);
							}
							finalListener.onResourceEvent(event.cloneFor(FileCachedResource.this));
						}}, parameters);
				}
	//			final IResourceListener<ResourceLoaderEvent<byte[]>> finalListener = listener;
	//			byte[] data	= this.data;
	//			if (data != null) {
	//				ResourceLoaderEvent<byte[]> event = new ResourceLoaderEvent<byte[]>(
	//						this, data, true);
	//				listener.onResourceEvent(event);
	//			} else {
	//				this.getParentResource().getResourceBytes(new IResourceListener<ResourceLoaderEvent<byte[]>>() {
	//					public void onResourceEvent(
	//							ResourceLoaderEvent<byte[]> event) {
	//						if (event.isComplete()) {
	//							MemoryCachedResource.this.data = event.getResult();
	//						} else if (event.isFailed()) {
	//						}
	//						finalListener.onResourceEvent(event);
	//					}});
	//			}
			}
		}

		@Override
		public long getSize() {
			long result	= 0;
			try {
				byte[] data	= getResourceBytes();
				if (data != null) {
					result	= data.length;
				}
			} catch (IOException e) {
			}
			return result;
		}

	}


	private static final String FILENAME_CHARSET_NAME = "UTF-8";

	private static final Log log	= LogFactory.getLog(FileCachedResourceLoader.class);


	private final IResourceLoader parentResourceLoader;
	private final IObjectCache<String, IResource>	resourceCache	= new ObjectCache<String, IResource>();
	private final String cacheDirectory;
	private final ICacheGarbageCollector cacheGarbageCollector;
	private final IFileCacheContext cacheContext;

	public FileCachedResourceLoader(IResourceLoader parentResourceLoader, String cacheDirectory, long maxTotalSize, int maxFileCount,
			ITaskManager taskManager) {
		super(parentResourceLoader, taskManager);
		this.parentResourceLoader	= parentResourceLoader;
		this.cacheDirectory		= cacheDirectory;
		this.cacheGarbageCollector	= new FileCacheGarbageCollector(cacheDirectory, maxTotalSize, maxFileCount);
		this.cacheContext	= new FileCacheContext(cacheGarbageCollector);
	}


	private static String getFileNameByResourceName(String resourceName) {
		String result;
		try {
			result = URLEncoder.encode(resourceName, FILENAME_CHARSET_NAME);
		} catch (UnsupportedEncodingException e) {
			log.warn("Failed to convert to filename - " + e, e);
			result = resourceName;
		}
		//result	= StringUtil.replace(result, "/", "%2F%");
		return result;
	}


	@Override
	public IResource getResource(String name) throws IOException {
		if ((name.indexOf("://") < 0) || (name.startsWith("file://"))) {
			return parentResourceLoader.getResource(name);
		}
		IResource resource	= (IResource) resourceCache.get(name);
		if (resource == null) {
			resource	= parentResourceLoader.getResource(name);
			int modifier	= resource.getModifier();
			if ((resource != null) && ((modifier & IResource.MODIFIER_MEMORY_CACHED) == 0) &&
					((modifier & IResource.MODIFIER_FILE_CACHED) == 0)) {
				File cacheFile	= new File(cacheDirectory, getFileNameByResourceName(name));
				resource	= new FileCachedResource(
						resource, cacheFile, cacheContext,
						this.getTaskManager());
				resourceCache.put(name, resource);
			}
		}
		return resource;
	}

}
