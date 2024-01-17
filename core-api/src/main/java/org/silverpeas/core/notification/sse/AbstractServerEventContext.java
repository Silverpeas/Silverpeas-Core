/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.sse;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.silverpeas.core.admin.user.model.User;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.silverpeas.core.notification.sse.ServerEventDispatcherTask.unregisterContext;

/**
 * This is a common implementation od {@link SilverpeasServerEventContext} interface.
 * <p>
 * The aim is about to centralize common behaviors between the different kinds of context to handle.
 * </p>
 * @author silveryocha
 */
abstract class AbstractServerEventContext<W> implements SilverpeasServerEventContext {

  private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
  private final SilverpeasServerEventContextManager manager;
  private final W wrappedInstance;
  private final String sessionId;
  private final User user;
  private Long lastServerEventId;

  /**
   * Hidden constructor.
   */
  AbstractServerEventContext(final SilverpeasServerEventContextManager manager,
      final W wrappedInstance, final String sessionId, final User user) {
    this.manager = manager;
    this.wrappedInstance = wrappedInstance;
    this.sessionId = sessionId;
    this.user = user;
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

  @Override
  public String getSessionId() {
    return sessionId;
  }

  @Override
  public User getUser() {
    return user;
  }

  @Override
  public Long getLastServerEventId() {
    return safeRead(() -> lastServerEventId);
  }

  /**
   * Sets the last server event identifier.
   * @param lastServerEventId a last event identifier as long, can be null.
   */
  @Override
  public void setLastServerEventId(final Long lastServerEventId) {
    safeWrite(() -> this.lastServerEventId = lastServerEventId);
  }

  protected W getWrappedInstance() {
    return wrappedInstance;
  }

  protected SilverpeasServerEventContextManager getManager() {
    return manager;
  }

  @Override
  public boolean sendEvent(final String name, final long id, final String data) throws IOException {
    if (!isSendPossible()) {
      // This asynchronous context is no more usable
      SseLogger.get().debug(() -> format("No more usable {0}", this));
      unregisterContext(this);
      return false;
    }
    performEventSend(name, id, data);
    return true;
  }

  protected abstract void performEventSend(final String name, final long id, final String data)
      throws IOException;

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
    if (getLastServerEventId() != null) {
      tsb.append("lastServerEventId", getLastServerEventId());
    }
    return tsb.toString();
  }
}
