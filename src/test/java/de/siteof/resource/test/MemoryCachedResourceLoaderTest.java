package de.siteof.resource.test;

import java.io.IOException;

import org.junit.Test;

import de.siteof.resource.IResourceLoader;
import de.siteof.resource.MemoryCachedResourceLoader;

public class MemoryCachedResourceLoaderTest extends AbstractResourceLoaderTest {

	@Override
	protected IResourceLoader createResourceLoader(IResourceLoader parent) {
		return new MemoryCachedResourceLoader(parent);
	}

	@Test
	public void test() throws IOException {
		this.doTest();
	}

}
