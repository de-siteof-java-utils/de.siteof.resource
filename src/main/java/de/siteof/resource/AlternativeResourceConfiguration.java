package de.siteof.resource;

public class AlternativeResourceConfiguration {

	private int maxTryCount = 10;
	private int maxAlternativeCount = maxTryCount;
	private IResourceLoader alternativeResourceLoader;

	/**
	 * @return the maxTryCount
	 */
	public int getMaxTryCount() {
		return maxTryCount;
	}
	/**
	 * @param maxTryCount the maxTryCount to set
	 */
	public void setMaxTryCount(int maxTryCount) {
		this.maxTryCount = maxTryCount;
	}
	/**
	 * @return the maxAlternativeCount
	 */
	public int getMaxAlternativeCount() {
		return maxAlternativeCount;
	}
	/**
	 * @param maxAlternativeCount the maxAlternativeCount to set
	 */
	public void setMaxAlternativeCount(int maxAlternativeCount) {
		this.maxAlternativeCount = maxAlternativeCount;
	}
	/**
	 * @return the alternativeResourceLoader
	 */
	public IResourceLoader getAlternativeResourceLoader() {
		return alternativeResourceLoader;
	}
	/**
	 * @param alternativeResourceLoader the alternativeResourceLoader to set
	 */
	public void setAlternativeResourceLoader(
			IResourceLoader alternativeResourceLoader) {
		this.alternativeResourceLoader = alternativeResourceLoader;
	}

}
