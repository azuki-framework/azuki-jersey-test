package org.glassfish.jersey.servlet.internal;

import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.servlet.spi.AsyncContextDelegate;

public class CustomResponseWriter extends ResponseWriter {

	/**
	 * Creates a new instance to write a single Jersey response.
	 * 
	 * @param useSetStatusOn404 true if status should be written explicitly when
	 *        404 is returned
	 * @param configSetStatusOverSendError if {@code true} method
	 *        {@link HttpServletResponse#setStatus} is used over
	 *        {@link HttpServletResponse#sendError}
	 * @param response original HttpResponseRequest
	 * @param asyncExt delegate to use for async features implementation
	 * @param timeoutTaskExecutor Jersey runtime executor used for background
	 *        execution of timeout handling tasks.
	 */
	public CustomResponseWriter(final boolean useSetStatusOn404, final boolean configSetStatusOverSendError, final HttpServletResponse response,
			final AsyncContextDelegate asyncExt, final ScheduledExecutorService timeoutTaskExecutor) {
		super(useSetStatusOn404, configSetStatusOverSendError, response, asyncExt, timeoutTaskExecutor);
	}

	@Override
	public void commit() {
		// Http error throw
	}
}
