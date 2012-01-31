package de.siteof.resource.test;

import java.io.IOException;

import de.siteof.resource.IResourceLoader;
import de.siteof.resource.util.test.ResourceLoaderTester;

public abstract class AbstractResourceLoaderTest {

	private final ResourceLoaderTester tester =  new ResourceLoaderTester();

	protected abstract IResourceLoader createResourceLoader(IResourceLoader parent);

	protected void doTest() throws IOException {
		this.doTest("dummy");
	}

	protected void doTest(String name) throws IOException {
		tester.setResourceLoader(this.createResourceLoader(tester.getParent()));
		tester.test(name);
	}

}
