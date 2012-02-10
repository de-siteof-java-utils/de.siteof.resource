package de.siteof.resource.util.test;

public class ResourceLoaderTestParameter {

	private final String testName;

	public ResourceLoaderTestParameter(String testName) {
		this.testName = testName;
	}

	@Override
	public String toString() {
		return testName;
	}

	public String getTestName() {
		return testName;
	}

}
