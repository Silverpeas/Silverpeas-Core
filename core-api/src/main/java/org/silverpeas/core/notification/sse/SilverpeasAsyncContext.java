/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.silverpeas.core.notification.sse.ServerEventDispatcherTask.unregisterAsyncContext;

/**
 * This is a wrap of a {@link AsyncContext} instance.
 * @author Yohann Chastagnier
 */
public class SilverpeasAsyncContext implements AsyncContext {

  private final AsyncContext wrappedInstance;
  private final User user;
  private Long lastServerEventId;

  /**
   * Hidden constructor.
   */
  private SilverpeasAsyncContext(final AsyncContext wrappedInstance, final User user) {
    this.wrappedInstance = wrappedInstance;
    this.user = user;
  }

  /**
   * Wraps the given instance. Nothing is wrapped if the given instance is a wrapped one.
   * @param asyncContext the instance to wrap.
   * @param user the identifier of th user linked to the async request.
   * @return the wrapped given instance.
   */
  public static SilverpeasAsyncContext wrap(AsyncContext asyncContext, User user) {
    if (asyncContext instanceof SilverpeasAsyncContext) {
      return (SilverpeasAsyncContext) asyncContext;
    }
    final SilverpeasAsyncContext context = new SilverpeasAsyncContext(asyncContext, user);
    context.addListener(new AsyncListener() {
      @Override
      public void onComplete(final AsyncEvent event) throws IOException {
        SilverLogger.getLogger(this).debug("Async context is completed ({0})", context.toString());
        unregisterAsyncContext(context);
      }

      @Override
      public void onTimeout(final AsyncEvent event) throws IOException {
        SilverLogger.getLogger(this).debug("Async context is timed out ({0})", context.toString());
        unregisterAsyncContext(context);
      }

      @Override
      public void onError(final AsyncEvent event) throws IOException {
        SilverLogger.getLogger(this)
            .debug("Async context thrown an error ({0})", context.toString());
        unregisterAsyncContext(context);
      }

      @Override
      public void onStartAsync(final AsyncEvent event) throws IOException {
        SilverLogger.getLogger(this).debug("Async context is starting ({0})", context.toString());
      }
    });
    return context;
  }

  /**
   * Gets the request URI behind the async context.
   * @return a request URI as string.
   */
  public String getRequestURI() {
    return ((HttpServletRequest) getRequest()).getRequestURI();
  }

  /**
   * Gets the session if linked to this async request.
   * @return a session identifier as string.
   */
  public String getSessionId() {
    return ((HttpServletRequest) getRequest()).getSession(false).getId();
  }

  /**
   * Gets the user identifier linked to this async request.
   * @return a user identifier as string.
   */
  public User getUser() {
    return user;
  }

  /**
   * Gets the last server event identifier known before a network breakdown.
   * @return an identifier as string.
   */
  public Long getLastServerEventId() {
    return lastServerEventId;
  }

  public void setLastServerEventId(final Long lastServerEventId) {
    this.lastServerEventId = lastServerEventId;
  }

  @Override
  public ServletRequest getRequest() {
    return wrappedInstance.getRequest();
  }

  @Override
  public ServletResponse getResponse() {
    return wrappedInstance.getResponse();
  }

  @Override
  public boolean hasOriginalRequestAndResponse() {
    return wrappedInstance.hasOriginalRequestAndResponse();
  }

  @Override
  public void dispatch() {
    wrappedInstance.dispatch();
  }

  @Override
  public void dispatch(String path) {
    wrappedInstance.dispatch(path);
  }

  @Override
  public void dispatch(ServletContext context, String path) {
    wrappedInstance.dispatch(context, path);
  }

  @Override
  public void complete() {
    wrappedInstance.complete();

  }

  @Override
  public void start(Runnable run) {
    wrappedInstance.start(run);
  }

  @Override
  public void addListener(AsyncListener listener) {
    wrappedInstance.addListener(listener);
  }

  @Override
  public void addListener(AsyncListener listener, ServletRequest servletRequest,
      ServletResponse servletResponse) {
    wrappedInstance.addListener(listener, servletRequest, servletResponse);
  }

  @Override
  public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
    return wrappedInstance.createListener(clazz);
  }

  @Override
  public long getTimeout() {
    return wrappedInstance.getTimeout();
  }

  @Override
  public void setTimeout(long timeout) {
    wrappedInstance.setTimeout(timeout);
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
    tsb.append("sessionId", getSessionId());
    tsb.append("userId", getUser());
    tsb.append("timeout", getTimeout());
    if (getLastServerEventId() != null) {
      tsb.append("lastServerEventId", getLastServerEventId());
    }
    return tsb.toString();
  }
}
