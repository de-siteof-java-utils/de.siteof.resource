package de.siteof.resource.test;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import de.siteof.resource.ClassLoaderResourceLoader;
import de.siteof.resource.IResourceLoader;
import de.siteof.resource.util.test.ResourceLoaderTestParameter;
import de.siteof.resource.util.test.ResourceLoaderTester;
import de.siteof.test.LabelledParameterized;

@RunWith(LabelledParameterized.class)
public class ClassLoaderResourceLoaderTest {

	private static ResourceLoaderTester tester = new ResourceLoaderTester() {
		@Override
		protected IResourceLoader createResourceLoader(IResourceLoader parent) {
			return new ClassLoaderResourceLoader(parent);
		}
	};

	private final ResourceLoaderTestParameter test;

	public ClassLoaderResourceLoaderTest(ResourceLoaderTestParameter test) {
		this.test = test;
	}

	@Parameters
    public static Collection<Object[]> getTests() {
    	return tester.allTestsArrays();
    }

	@Test
	public void test() throws IOException {
		tester.test(test, "http://dummy/");
	}

}
