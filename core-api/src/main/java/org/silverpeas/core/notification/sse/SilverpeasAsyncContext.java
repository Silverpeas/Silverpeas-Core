/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.sse;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.silverpeas.core.notification.sse.ServerEventDispatcherTask.unregisterContext;

/**
 * This is a wrap of a {@link AsyncContext} instance.
 * <p>
 *   All Server Event requests performed from a HTTP Server Sent Event WEB API MUST be wrapped by
 *   this implementation and registered by {@link SilverpeasServerEventContextManager}.
 * </p>
 * @author Yohann Chastagnier
 */
public class SilverpeasAsyncContext extends AbstractServerEventContext<AsyncContext>
    implements AsyncContext {

  private static final int CLIENT_RETRY = 5000;

  private boolean heartbeat = false;
  private boolean complete = false;
  private boolean timeout = false;
  private boolean error = false;

  /**
   * Hidden constructor.
   */
  SilverpeasAsyncContext(final SilverpeasServerEventContextManager manager,
      final AsyncContext wrappedInstance, final String sessionId, final User user) {
    super(manager, wrappedInstance, sessionId, user);
  }

  /**
   * Wraps the given instance. Nothing is wrapped if the given instance is a wrapped one.
   * @param silverLogger the sse silverpeas logger.
   * @param asyncContext the instance to wrap.
   * @param userSessionId the identifier or the user session.
   * @param user the identifier of th user linked to the async request.
   * @return the wrapped given instance.
   */
  public static SilverpeasAsyncContext wrap(final SilverLogger silverLogger,
      AsyncContext asyncContext, final String userSessionId, User user) throws ServletException {
    if (asyncContext instanceof SilverpeasAsyncContext) {
      return (SilverpeasAsyncContext) asyncContext;
    }

    final SilverpeasAsyncContext context = new SilverpeasAsyncContext(
        SilverpeasServerEventContextManager.get(), asyncContext, userSessionId, user);
    final AsyncListener listener = context.createListener(SilverpeasAsyncListener.class)
        .init(silverLogger, context);
    context.addListener(listener);
    return context;
  }

  @Override
  public void closeOnPreviousCheckFailure() {
    if (isSendPossible()) {
      safeWrite(() -> {
        try {
          SseLogger.get().debug("send check to {0}", this);
          SessionPreviousCheckServerEvent.createFor(getSessionId())
              .send(this, getSessionId(), getUser());
        } catch (Exception e) {
          complete();
        }
      });
    }
  }

  @Override
  public void sendHeartbeatIfEnabled() {
    if (isHeartbeatBehavior()) {
      safeWrite(() -> {
        try {
          SseLogger.get().debug("send heartbeat to {0}", this);
          HeartbeatServerEvent.createFor(getSessionId())
              .send(this, getSessionId(), getUser());
        } catch (Exception e) {
          SseLogger.get().error(e);
          unregisterContext(this);
        }
      });
    }
  }

  @Override
  public void performEventSend(final String name, final long id, final String data)
      throws IOException {
    final int capacity = 100 + name.length() + data.length();
    StringBuilder sb = new StringBuilder(capacity);
    sb.append("retry: ").append(CLIENT_RETRY);
    sb.append("\nid: ").append(id);
    if (StringUtil.isDefined(name)) {
      sb.append("\nevent: ").append(name);
    }
    sb.append("\ndata: ");
    if (StringUtil.isDefined(data)) {
      for (int i = 0; i < data.length(); i++) {
        char currentChar = data.charAt(i);
        if (currentChar == '\n') {
          sb.append("\ndata: ");
        } else {
          sb.append(currentChar);
        }
      }
    }
    sb.append("\n\n");
    final PrintWriter writer = getResponse().getWriter();
    writer.append(sb.toString());
    writer.flush();
  }

  /**
   * Indicates if sending is possible according to the state of the context.
   * @return true if send is possible, false otherwise.
   */
  @Override
  public boolean isSendPossible() {
    return !isComplete() && !isTimeoutReached() && !hasErrorOccurred();
  }

  /**
   * Gets the request URI behind the async context.
   * @return a request URI as string.
   */
  @Override
  public String getRequestURI() {
    return getRequest().getRequestURI();
  }

  @Override
  public HttpServletRequest getRequest() {
    return (HttpServletRequest) getWrappedInstance().getRequest();
  }

  @Override
  public HttpServletResponse getResponse() {
    return (HttpServletResponse) getWrappedInstance().getResponse();
  }

  @Override
  public boolean hasOriginalRequestAndResponse() {
    return getWrappedInstance().hasOriginalRequestAndResponse();
  }

  @Override
  public void dispatch() {
    getWrappedInstance().dispatch();
  }

  @Override
  public void dispatch(String path) {
    getWrappedInstance().dispatch(path);
  }

  @Override
  public void dispatch(ServletContext context, String path) {
    getWrappedInstance().dispatch(context, path);
  }

  @Override
  public void complete() {
    markAsComplete(true);
  }

  private boolean isHeartbeatBehavior() {
    return safeRead(() -> heartbeat);
  }

  public void setHeartbeat(final boolean heartbeat) {
    safeWrite(() -> this.heartbeat = heartbeat);
  }

  public boolean isComplete() {
    return safeRead(() -> complete);
  }

  void markAsComplete(final boolean performRealComplete) {
    safeWrite(() -> {
      if (!this.complete) {
        this.complete = true;
        if (performRealComplete) {
          getWrappedInstance().complete();
        }
        getManager().unregister(this);
      }
    });
  }

  @Override
  public void close() {
    complete();
  }

  boolean isTimeoutReached() {
    return safeRead(() -> timeout);
  }

  void markTimeoutAsReached() {
    safeWrite(() -> this.timeout = true);
  }

  boolean hasErrorOccurred() {
    return safeRead(() -> error);
  }

  void markAsErrorOccurred() {
    safeWrite(() -> this.error = true);
  }

  @Override
  public void start(Runnable run) {
    getWrappedInstance().start(run);
  }

  @Override
  public void addListener(AsyncListener listener) {
    getWrappedInstance().addListener(listener);
  }

  @Override
  public void addListener(AsyncListener listener, ServletRequest servletRequest,
      ServletResponse servletResponse) {
    getWrappedInstance().addListener(listener, servletRequest, servletResponse);
  }

  @Override
  public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
    return getWrappedInstance().createListener(clazz);
  }

  @Override
  public long getTimeout() {
    return getWrappedInstance().getTimeout();
  }

  @Override
  public void setTimeout(long timeout) {
    getWrappedInstance().setTimeout(timeout);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  @Override
  public String toString() {
    ToStringBuilder tsb = new ToStringBuilder(this, SHORT_PREFIX_STYLE);
    tsb.append(super.toString());
    tsb.append("timeout", getTimeout());
    return tsb.toString();
  }

  private static class SilverpeasAsyncListener implements AsyncListener {
    private SilverLogger silverLogger;
    private SilverpeasAsyncContext context;

    protected SilverpeasAsyncListener() {

    }

    private SilverpeasAsyncListener init(final SilverLogger silverLogger, final SilverpeasAsyncContext context) {
      this.silverLogger = silverLogger;
      this.context = context;
      return this;
    }

    @Override
    public void onComplete(final AsyncEvent event) throws IOException {
      context.markAsComplete(false);
      silverLogger.debug("Async context is completed ({0})", context);
    }

    @Override
    public void onTimeout(final AsyncEvent event) throws IOException {
      context.markTimeoutAsReached();
      silverLogger.debug("Async context is timed out ({0})", context);
      context.complete();
    }

    @Override
    public void onError(final AsyncEvent event) throws IOException {
      context.markAsErrorOccurred();
      silverLogger.debug("Async context thrown an error ({0})", context);
      context.markAsComplete(false);
    }

    @Override
    public void onStartAsync(final AsyncEvent event) throws IOException {
      silverLogger.debug("Async context is starting ({0})", context);
    }
  }
}
