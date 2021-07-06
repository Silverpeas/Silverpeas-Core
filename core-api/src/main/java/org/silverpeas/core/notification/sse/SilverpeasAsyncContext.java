/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * This is a wrap of a {@link AsyncContext} instance.
 * @author Yohann Chastagnier
 */
public class SilverpeasAsyncContext implements AsyncContext {

  private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
  private final SilverpeasAsyncContextManager manager;
  private final AsyncContext wrappedInstance;
  private final String sessionId;
  private final User user;
  private Long lastServerEventId;
  private boolean heartbeat = false;
  private boolean complete = false;
  private boolean timeout = false;
  private boolean error = false;

  /**
   * Hidden constructor.
   */
  SilverpeasAsyncContext(final SilverpeasAsyncContextManager manager,
      final AsyncContext wrappedInstance, final String sessionId, final User user) {
    this.manager = manager;
    this.wrappedInstance = wrappedInstance;
    this.sessionId = sessionId;
    this.user = user;
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
        SilverpeasAsyncContextManager.get(), asyncContext, userSessionId, user);
    final AsyncListener listener = context.createListener(SilverpeasAsyncListener.class).init(silverLogger, context);
    context.addListener(listener);
    return context;
  }

  <T> T safeRead(final Supplier<T> process) {
    lock.readLock().lock();
    try {
      return process.get();
    } finally {
      lock.readLock().unlock();
    }
  }

  void safeWrite(final Runnable process) {
    lock.writeLock().lock();
    try {
      process.run();
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Indicates if the send is possible according to the state of the context.
   * @return true if send is possible, false otherwise.
   */
  public boolean isSendPossible() {
    return !isComplete() && !isTimeoutReached() && !hasErrorOccurred();
  }

  /**
   * Gets the request URI behind the async context.
   * @return a request URI as string.
   */
  public String getRequestURI() {
    return getRequest().getRequestURI();
  }

  /**
   * Gets the session if linked to this async request.
   * @return a session identifier as string.
   */
  public String getSessionId() {
    return sessionId;
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
  Long getLastServerEventId() {
    return safeRead(() -> lastServerEventId);
  }

  /**
   * Sets the last server event identifier.
   * @param lastServerEventId a last event identifier as long, can be null.
   */
  public void setLastServerEventId(final Long lastServerEventId) {
    safeWrite(() -> this.lastServerEventId = lastServerEventId);
  }

  @Override
  public HttpServletRequest getRequest() {
    return (HttpServletRequest) wrappedInstance.getRequest();
  }

  @Override
  public HttpServletResponse getResponse() {
    return (HttpServletResponse) wrappedInstance.getResponse();
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
    markAsComplete(true);
  }

  boolean isHeartbeat() {
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
          wrappedInstance.complete();
        }
        manager.unregister(this);
      }
    });
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
    tsb.append("on", getRequestURI());
    tsb.append("sessionId", getSessionId());
    tsb.append("userId", getUser().getId());
    tsb.append("timeout", getTimeout());
    if (getLastServerEventId() != null) {
      tsb.append("lastServerEventId", getLastServerEventId());
    }
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
