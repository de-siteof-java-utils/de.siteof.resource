package de.siteof.resource.awt;

import java.awt.Image;

public interface IImageResult {

	boolean isLoading();
	boolean isFailed();
	boolean isFinished();

	String getName();

	Image getImage();

}
