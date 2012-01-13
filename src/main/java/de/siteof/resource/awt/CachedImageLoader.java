package de.siteof.resource.awt;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.siteof.cache.IObjectCache;
import de.siteof.cache.ObjectCacheFactory;
import de.siteof.resource.awt.event.IImageResultListener;

public class CachedImageLoader implements IImageLoader {

	private IImageLoader	parentImageLoader;
	private IObjectCache<String, IImageResult>	imageCache	= ObjectCacheFactory.getNewSoftObjectCache();

	private static final Log log	= LogFactory.getLog(CachedImageLoader.class);


	public CachedImageLoader(IImageLoader parentImageLoader) {
		this.parentImageLoader	= parentImageLoader;
	}

	public Image getImage(String name) throws IOException {
		Image image	= null;
		IImageResult imageResult	= this.getImage(name, null, null);
		if (imageResult != null) {
			image	= imageResult.getImage();
		}
//		Image image	= (Image) imageCache.get(name);
//		if (image == null) {
//			image	= parentImageLoader.getImage(name);
//			if (image != null) {
//				imageCache.put(name, image);
//			}
//		}
		return image;
	}

	public IImageResult getImage(String name, IImageResultListener imageResultListener, ImageObserver imageObserver) throws IOException {
		IImageResult imageResult	= (IImageResult) imageCache.get(name);
		if (imageResult == null) {
			log.debug("loading image (not cached): " + name);
			imageResult	= parentImageLoader.getImage(name, imageResultListener, imageObserver);
			if (imageResult != null) {
				log.debug("storing image result in the cache: " + name);
				imageCache.put(name, imageResult);
			}
		} else {
			log.debug("image returned from cache: " + name);
		}
		return imageResult;
	}


	public void waitForImage(IImageResult imageResult) {
		parentImageLoader.waitForImage(imageResult);
	}



}
