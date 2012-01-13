package de.siteof.resource.awt;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.siteof.resource.IResource;
import de.siteof.resource.IResourceLoader;
import de.siteof.resource.awt.event.IImageResultListener;
import de.siteof.resource.awt.event.ImageResultEvent;
import de.siteof.resource.event.IResourceListener;
import de.siteof.resource.event.ResourceLoaderEvent;
import de.siteof.task.AbstractTask;
import de.siteof.task.ITask;
import de.siteof.task.ITaskManager;

public class ImageLoader implements IImageLoader {

	private ITaskManager taskManager;
	private IResourceLoader resourceLoader;
	private Toolkit toolkit;

	private static final Log log	= LogFactory.getLog(ImageLoader.class);


	public ImageLoader(IResourceLoader resourceLoader, ITaskManager taskManager) {
		this.resourceLoader	= resourceLoader;
		this.taskManager		= taskManager;
	}

	private final boolean isAsynchEnabled() {
		return true;
	}

	public Image getImage(String name) throws IOException {
		Image image	= null;
		IImageResult imageResult	= this.getImage(name, null, null);
		if (imageResult != null) {
			image	= imageResult.getImage();
		}
		return image;
	}

	public IImageResult getImage(String name, IImageResultListener imageResultListener, ImageObserver imageObserver) throws IOException {
		ImageResult result;
		if ((imageResultListener != null) && (isAsynchEnabled())) {
			log.debug("loading image in the background1: " + name);
			result	= new ImageResult();
			result.setName(name);
			final String finalName	= name;
			final ImageResult finalImageResult	= result;
			final ImageObserver finalImageObserver	= imageObserver;
			final IImageResultListener finalImageResultListener	= imageResultListener;
			IResource resource = resourceLoader.getResource(name);
			resource.getResourceBytes(new IResourceListener<ResourceLoaderEvent<byte[]>>() {
				public void onResourceEvent(ResourceLoaderEvent<byte[]> event) {
					ImageResult imageResult	= finalImageResult;
					Image image		= null;
					try {
						if (event.isComplete()) {
							byte[] data = event.getResult();
							image	= getImage2(finalName, data);
							imageResult.setImage(image);
							if (image == null) {
								imageResult.setFailed(true);
							}
							if ((image != null) && (finalImageObserver != null)) {
								finalImageObserver.imageUpdate(image, ImageObserver.ALLBITS,
										0, 0,
										image.getWidth(finalImageObserver),
										image.getHeight(finalImageObserver));
							}
						} else if (event.isFailed()) {
							imageResult.setFailed(true);
						} else {
							// this is only an intermediate event
							return;
						}
					} catch (Exception e) {
						finalImageResult.setFailed(true);
//						finalImageObserver.imageUpdate(image, ImageObserver.ALLBITS, 0, 0, -1, -1);
						log.warn("failed loading image - " + e, e);
					}
					if ((!imageResult.isFailed()) && (imageResult.getImage() != null)) {
						finalImageResultListener.handleImageResultEvent(
								new ImageResultEvent(imageResult, ImageResultEvent.LOADED, imageResult));
					} else {
						finalImageResultListener.handleImageResultEvent(
								new ImageResultEvent(imageResult, ImageResultEvent.FAILED, imageResult));
					}
					imageResult.setFinished(true);
				}

			});

//			result.setLoadingTask(new AbstractTask() {
//				public void execute() throws Exception {
//					ImageResult imageResult	= finalImageResult;
//					Image image		= null;
//					try {
//						image	= getImage2(finalName);
//						imageResult.setImage(image);
//						if (image == null) {
//							imageResult.setFailed(true);
//						}
//						if ((image != null) && (finalImageObserver != null)) {
//							finalImageObserver.imageUpdate(image, ImageObserver.ALLBITS,
//									0, 0,
//									image.getWidth(finalImageObserver),
//									image.getHeight(finalImageObserver));
//						}
//					} catch (Exception e) {
//						finalImageResult.setFailed(true);
////						finalImageObserver.imageUpdate(image, ImageObserver.ALLBITS, 0, 0, -1, -1);
//						log.warn("failed loading image - " + e, e);
//					}
//					if (!imageResult.isFailed()) {
//						finalImageResultListener.handleImageResultEvent(
//								new ImageResultEvent(imageResult, ImageResultEvent.LOADED, imageResult));
//					} else {
//						finalImageResultListener.handleImageResultEvent(
//								new ImageResultEvent(imageResult, ImageResultEvent.FAILED, imageResult));
//					}
//					imageResult.setFinished(true);
//				}} );
//			taskManager.addTask(result.getLoadingTask());
		} else if ((imageResultListener != null) && (taskManager != null)) {
			log.debug("loading image in the background: " + name);
			result	= new ImageResult();
			result.setName(name);
			final String finalName	= name;
			final ImageResult finalImageResult	= result;
			final ImageObserver finalImageObserver	= imageObserver;
			final IImageResultListener finalImageResultListener	= imageResultListener;
			result.setLoadingTask(new AbstractTask() {
				public void execute() throws Exception {
					ImageResult imageResult	= finalImageResult;
					Image image		= null;
					try {
						image	= getImage2(finalName);
						imageResult.setImage(image);
						if (image == null) {
							imageResult.setFailed(true);
						}
						if ((image != null) && (finalImageObserver != null)) {
							finalImageObserver.imageUpdate(image, ImageObserver.ALLBITS,
									0, 0,
									image.getWidth(finalImageObserver),
									image.getHeight(finalImageObserver));
						}
					} catch (Exception e) {
						finalImageResult.setFailed(true);
//						finalImageObserver.imageUpdate(image, ImageObserver.ALLBITS, 0, 0, -1, -1);
						log.warn("failed loading image - " + e, e);
					}
					if (!imageResult.isFailed()) {
						finalImageResultListener.handleImageResultEvent(
								new ImageResultEvent(imageResult, ImageResultEvent.LOADED, imageResult));
					} else {
						finalImageResultListener.handleImageResultEvent(
								new ImageResultEvent(imageResult, ImageResultEvent.FAILED, imageResult));
					}
					imageResult.setFinished(true);
				}} );
			taskManager.addTask(result.getLoadingTask());
		} else {
			log.debug("loading image directly: " + name + ", imageResultListener=" + imageResultListener + ", taskManager=" + taskManager);
			try {
//				throw new RuntimeException("dummy exception to get the stack trace");
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			result	= new ImageResult();
			result.setImage(this.getImage2(name));
			if (result.getImage() == null) {
				result.setFailed(true);
			}
			result.setFinished(true);
		}
		return result;
	}

	public void waitForImage(IImageResult imageResult) {
		if ((imageResult instanceof ImageResult) && (taskManager != null)) {
			ITask task	= ((ImageResult) imageResult).getLoadingTask();
			if (task != null) {
				taskManager.waitForTask(task);
			}
		}
	}

	public Image getImage1(String name) throws IOException {
		InputStream in	= resourceLoader.getResourceAsStream(name);
		if (in != null) {
			try {
				if (log.isDebugEnabled()) {
					log.debug("creating image from data: " + name);
				}
				if (toolkit == null) {
					toolkit	= Toolkit.getDefaultToolkit();
				}
				Image image	= javax.imageio.ImageIO.read(in);
				if (image == null) {
					log.warn("failed to load image: " + name);
				} else {
					toolkit.prepareImage(image, -1, -1, null);
	//				toolkit.prepareImage(image, null);
				}
				if (log.isDebugEnabled()) {
					log.debug("created image from data: " + name + ", image=" + image);
				}
				return image;
			} finally {
				in.close();
			}
		} else {
			log.warn("failed to load image resource: " + name);
		}
		return null;
	}

	public Image getImage2(String name) throws IOException {
		byte[] data	= resourceLoader.getResourceBytes(name);
		if (data != null) {
			return this.getImage2(name, data);
		} else {
			log.warn("failed to load image resource: " + name);
		}
		return null;
	}

	public Image getImage2(String name, byte[] data) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("creating image from data: " + name);
		}
		if (toolkit == null) {
			toolkit	= Toolkit.getDefaultToolkit();
		}
		Image image	= toolkit.createImage(data);
		if (image == null) {
			log.warn("failed to load image: " + name);
		} else {
			toolkit.prepareImage(image, -1, -1, null);
//			toolkit.prepareImage(image, null);
		}
		if (log.isDebugEnabled()) {
			log.debug("created image from data: " + name + ", image=" + image);
		}
		return image;
	}

	public Toolkit getToolkit() {
		return toolkit;
	}

	public void setToolkit(Toolkit toolkit) {
		this.toolkit = toolkit;
	}

}
