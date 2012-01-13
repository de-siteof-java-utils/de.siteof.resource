package de.siteof.resource.awt;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.io.IOException;

import de.siteof.resource.awt.event.IImageResultListener;

public interface IImageLoader {

	Image getImage(String name) throws IOException;

	IImageResult getImage(String name, IImageResultListener imageResultListener, ImageObserver imageObserver) throws IOException;

	void waitForImage(IImageResult imageResult);

}
