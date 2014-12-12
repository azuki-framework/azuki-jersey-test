package org.azkfw.jersey.test;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import junit.framework.TestCase;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.CustomServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

public abstract class AbstractJerseyTestCase extends TestCase {

	private JerseyTest jerseyTest;

	protected abstract Map<String, String> getInitParams();

	@Override
	public void setUp() {
		//super.setUp();

		//
		this.jerseyTest = new CustomJerseyTest();

		try {
			this.jerseyTest.setUp();
		} catch (Exception e) {
			throw new RuntimeException("failed to tear down JerseyTest.", e);
		}
	}

	@Override
	public void tearDown() {
		try {
			this.jerseyTest.tearDown();
		} catch (Exception e) {
			throw new RuntimeException("failed to tear down JerseyTest.", e);
		}
		//

		//super.tearDown();
	}

	protected abstract List<Class<?>> getTestClasses();

	private Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		for (Class<?> api : getTestClasses()) {
			classes.add(api);
		}
		return classes;
	}

	protected WebTarget target(final String aPath) {
		return this.jerseyTest.target(aPath);
	}

	public class CustomJerseyTest extends JerseyTest {

		@Override
		protected Application configure() {
			enable(TestProperties.LOG_TRAFFIC);
			ResourceConfig rc = new ResourceConfig(getClasses());
			return rc.registerClasses(MultiPartFeature.class);
		}

		@Override
		protected void configureClient(final ClientConfig config) {
			config.register(MultiPartFeature.class);
		}

		@Override
		protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
			return new TestContainerFactory() {
				@Override
				public TestContainer create(final URI baseUri, DeploymentContext deploymentContext) {
					return new TestContainer() {
						private HttpServer server;

						@Override
						public ClientConfig getClientConfig() {
							return null;
						}

						@Override
						public URI getBaseUri() {
							return baseUri;
						}

						@Override
						public void start() {
							try {
								this.server = GrizzlyWebContainerFactory.create(baseUri, CustomServletContainer.class, getInitParams());
							} catch (ProcessingException e) {
								throw new TestContainerException(e);
							} catch (IOException e) {
								throw new TestContainerException(e);
							}
						}

						@Override
						public void stop() {
							this.server.stop();
						}
					};

				}

			};
		}
	}
}
