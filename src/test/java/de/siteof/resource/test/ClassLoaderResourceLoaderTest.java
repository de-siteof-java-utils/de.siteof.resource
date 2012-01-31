package de.siteof.resource.test;

import java.io.IOException;

import org.junit.Test;

import de.siteof.resource.ClassLoaderResourceLoader;
import de.siteof.resource.IResourceLoader;

public class ClassLoaderResourceLoaderTest extends AbstractResourceLoaderTest {

	@Override
	protected IResourceLoader createResourceLoader(IResourceLoader parent) {
		return new ClassLoaderResourceLoader(parent);
	}

	@Test
	public void test() throws IOException {
		this.doTest("http://dummy/");
	}

}
