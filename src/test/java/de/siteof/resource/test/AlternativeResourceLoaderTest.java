package de.siteof.resource.test;

import java.io.IOException;

import org.junit.Test;

import de.siteof.resource.AlternativeResourceLoader;
import de.siteof.resource.IResourceLoader;

public class AlternativeResourceLoaderTest extends AbstractResourceLoaderTest {

	@Override
	protected IResourceLoader createResourceLoader(IResourceLoader parent) {
		return new AlternativeResourceLoader(parent);
	}

	@Test
	public void test() throws IOException {
		this.doTest("http://dummy/");
	}

}
