/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.azkfw.jersey.test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogManager;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.azkfw.business.test.AbstractBusinessTestCase;
import org.azkfw.context.Context;
import org.azkfw.test.context.TestContext;
import org.azkfw.util.StringUtility;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.CustomServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

/**
 * このクラスは、Jersey機能をサポートしたタスククラスです。
 * 
 * @since 1.0.0
 * @version 1.0.0 2015/01/27
 * @author Kawakicchi
 */
public abstract class AbstractJerseyTestCase extends AbstractBusinessTestCase {

	/** Jerseyテスト */
	private JerseyTest jerseyTest;

	protected String getJerseyLoggerFile() {
		return "conf/logger.properties";
	}

	@Override
	public void setUp() {
		super.setUp();

		String loggerFile = getJerseyLoggerFile();
		if (StringUtility.isNotEmpty(loggerFile)) {
			try {
				LogManager.getLogManager().readConfiguration(getContext().getResourceAsStream(loggerFile));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

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

		super.tearDown();
	}

	protected abstract List<Class<?>> getTestClasses();

	protected abstract Map<String, String> getInitParams();

	@Override
	protected Context getContext() {
		return new TestContext("./src/main/webapp/WEB-INF/");
	}

	protected WebTarget target(final String aPath) {
		return this.jerseyTest.target(aPath);
	}

	/**
	 * GETメドッド
	 * 
	 * @param path パス
	 * @return レスポンス情報
	 */
	protected final Response get(final String path) {
		return get(path, null);
	}

	/**
	 * GETメソッド
	 * 
	 * @param path パス
	 * @param params パラメータ
	 * @return レスポンス情報
	 */
	protected final Response get(final String path, final Map<String, Object> params) {
		WebTarget target = target(path);
		if (null != params) {
			for (String key : params.keySet()) {
				Object value = params.get(key);
				target = target.queryParam(key, value);
			}
		}
		Response response = target.request().get();
		return response;
	}

	/**
	 * POSTメソッド
	 * 
	 * @param path パス
	 * @param entity パラメータ
	 * @return レスポンス情報
	 */
	protected final Response post(final String path, final Entity<?> entity) {
		Response response = target(path).request().post(entity);
		return response;
	}

	/**
	 * POSTメソッド(Form形式)
	 * 
	 * @param path パス
	 * @param params パラメータ
	 * @return レスポンス情報
	 */
	protected final Response postForm(final String path, final Map<String, String> params) {
		Form form = new Form();
		for (String key : params.keySet()) {
			form.param(key, params.get(key));
		}
		return postForm(path, form);
	}

	/**
	 * POSTメソッド(Form形式)
	 * 
	 * @param path パス
	 * @param entity パラメータ
	 * @return レスポンス情報
	 */
	protected final Response postForm(final String path, final Form form) {
		Entity<Form> entity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
		return post(path, entity);
	}

	/**
	 * POSTメソッド(MultiPart形式)
	 * 
	 * @param path パス
	 * @param params パラメータ
	 * @return 結果
	 * @throws IOException
	 */
	protected final Response postMultiPart(final String path, final Map<String, Object> params) {
		FormDataMultiPart multiPart = new FormDataMultiPart();
		if (null != params) {
			for (String key : params.keySet()) {
				Object obj = params.get(key);
				if (null == obj) {

				} else if (obj instanceof File) {
					File file = (File) obj;
					FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(key, file);
					multiPart.bodyPart(fileDataBodyPart);
				} else {
					String string = obj.toString();
					FormDataBodyPart formDataBodyPart = new FormDataBodyPart(key, string);
					multiPart.bodyPart(formDataBodyPart);
				}
			}
		}
		Entity<FormDataMultiPart> entity = Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE);
		Response response = target(path).request().header("Content-type", MediaType.MULTIPART_FORM_DATA).post(entity);
		return response;
	}

	/**
	 * POSTメソッド(JSON形式)
	 * 
	 * @param path パス
	 * @param object パラメータ
	 * @return レスポンス情報
	 */
	protected final Response postJson(final String path, final Object object) {
		Entity<Object> entity = Entity.entity(object, MediaType.APPLICATION_JSON_TYPE);
		return post(path, entity);
	}

	protected final <REQUEST, RESPONSE> RESPONSE postJson(final String path, final REQUEST req, final Class<RESPONSE> res) {
		Entity<REQUEST> e = Entity.entity(req, MediaType.APPLICATION_JSON_TYPE);
		Response response = post(path, e);
		assertEquals("HTTPステータス", 200, response.getStatus());
		RESPONSE r = response.readEntity(res);
		return r;
	}

	private Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		for (Class<?> api : getTestClasses()) {
			classes.add(api);
		}
		return classes;
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

						@SuppressWarnings("deprecation")
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