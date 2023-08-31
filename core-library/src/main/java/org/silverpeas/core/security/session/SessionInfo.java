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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.session;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.SessionCacheAccessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of a user session in Silverpeas. It gathers information about an opened
 * session of a user. It is an abstract class providing a default implementation of a user session
 * in Silverpeas for whatever technical session in use. It is the responsibility of the concrete
 * class extending the {@link SessionInfo} abstract class to define on which technical session the
 * user sessions in Silverpeas have to be built.
 */
public abstract class SessionInfo implements SilverpeasUserSession {

  /**
   * A non defined session. To use instead of null or to represent a session info not bound to any
   * user session.
   */
  public static final SessionInfo NoneSession = new SessionInfo() {
  };

  // Object on which to synchronize (the instance indeed)
  private final Object mutex;

  private final String sessionId;
  private final User userDetail;
  private final long openingTimestamp;
  private final Map<String, Object> attributes = new ConcurrentHashMap<>();
  private String ipAddress;
  private long lastAccessTimestamp;
  private long idleTimestamp;
  private SimpleCache cache;

  private SessionInfo() {
    this(null, null);
  }

  /**
   * Constructs a new instance about a given opened user session.
   * @param sessionId the identifier of the opened session.
   * @param user the user for which a session was opened.
   */
  protected SessionInfo(final String sessionId, final User user) {
    this.mutex = this;
    this.sessionId = sessionId;
    this.userDetail = user;
    this.openingTimestamp = this.lastAccessTimestamp = System.currentTimeMillis();
    this.idleTimestamp = 0;
    if (user != null) {
      cache = ((SessionCacheAccessor) CacheAccessorProvider.getSessionCacheAccessor()).newSessionCache(
          user);
    }
  }

  @Override
  public String getId() {
    return getSessionId();
  }

  @Override
  public User getUser() {
    return getUserDetail();
  }

  /**
   * Gets the IP address of the remote client that opened the session.
   * @return the client remote IP address.
   */
  public String getIPAddress() {
    synchronized (mutex) {
      return ipAddress;
    }
  }

  /**
   * Sets the IP address of the remote client that requests a session opening with Silverpeas.
   * @param ip the IP address of the remote client.
   */
  public void setIPAddress(String ip) {
    synchronized (mutex) {
      this.ipAddress = ip;
    }
  }

  /**
   * Gets the timestamp of the last access by the client behind this session.
   * @return timestamp of the last access.
   */
  public long getLastAccessTimestamp() {
    synchronized (mutex) {
      return lastAccessTimestamp;
    }
  }

  /**
   * Gets the timestamp at which the session with Silverpeas was opened.
   * @return the session opening timestamp.
   */
  public long getOpeningTimestamp() {
    return openingTimestamp;
  }

  /**
   * Gets the last duration of its idle time. A one-shot session cannot be in idle state. Invoking
   * this method on such a session will throw an {@link IllegalStateException} exception.
   * @return the session alive timestamp.
   */
  public long getLastIdleDuration() {
    checkIsNotOneShot();
    synchronized (mutex) {
      return System.currentTimeMillis() - idleTimestamp;
    }
  }

  /**
   * Sets this session as currently idle. A one-shot session cannot be in idle state. Invoking
   * this method on such a session will throw an {@link IllegalStateException} exception.
   */
  public void setAsIdle() {
    checkIsNotOneShot();
    synchronized (mutex) {
      idleTimestamp = System.currentTimeMillis();
    }
  }

  /**
   * Sets this session as a one-shot one. A one-shot session cannot become a long-living session
   * after.
   */
  public void setAsOneShot() {
    synchronized (mutex) {
      idleTimestamp = -1;
    }
  }

  /**
   * Gets the unique identifier of the session.
   * @return the session identifier.
   */
  public String getSessionId() {
    return sessionId;
  }

  /**
   * Gets the profile of the user that opened the session.
   * @return a UserDetail instance with the profile information on the user.
   */
  public User getUserDetail() {
    return userDetail;
  }

  /**
   * Updates the last access timestamp. A one-shot session cannot used in several user calls.
   * Invoking this method on such a session will throw an {@link IllegalStateException} exception.
   */
  public void updateLastAccess() {
    checkIsNotOneShot();
    synchronized (mutex) {
      this.lastAccessTimestamp = System.currentTimeMillis();
      this.idleTimestamp = 0;
    }
  }

  /**
   * Sets an attribute named by the specified name with the specified value. If no attribute exists
   * with the specified name, then it is added to the session.
   * @param <T> the type of the attribute value.
   * @param name the name of the attribute to set.
   * @param value the value of the attribute to set.
   */
  @Override
  public <T> void setAttribute(String name, T value) {
    attributes.put(name, value);
  }

  /**
   * Gets the value of the attribute named by the specified name.
   * @param <T> the type of the attribute value.
   * @param name the name of the attribute to get.
   * @return the value of the attribute or null if no such attribute exists.
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAttribute(String name) {
    return (T) attributes.get(name);
  }

  /**
   * Unsets the specified attribute. The consequence of an unset is the attribute is then removed
   * from the session.
   * @param name the name of the attribute to unset.
   */
  @Override
  public void unsetAttribute(String name) {
    attributes.remove(name);
  }

  /**
   * Frees the allocated resources used in the session management and carried by this session
   * information. This method must be called at session closing by the session management system.
   */
  public void onClosed() {
    attributes.clear();
  }

  /**
   * Is this session defined? A session is defined if it's a session opened to a user in
   * Silverpeas.
   * @return true if this session is defined, false otherwise.
   */
  @Override
  public boolean isDefined() {
    return this != NoneSession && this.getUserDetail() != null;
  }

  /**
   * Is this session an anonymous one? A session is anonymous when no users are explicitly
   * authenticated and then identified and the users uses Silverpeas under the cover of a
   * transparent anonymous user account.
   * @return true if this session is a defined anonymous one, false otherwise.
   */
  @Override
  public boolean isAnonymous() {
    return this.getUserDetail() != null && this.getUserDetail().isAnonymous();
  }

  /**
   * Is this session a one-shot one? A one-shot session is a short time living session that doesn't
   * live over the scope of a functional call (id est over the scope of an HTTP request).
   * @return true if this session is a one-shot one, false otherwise.
   */
  @Override
  public boolean isOneShot() {
    return this.idleTimestamp == -1;
  }

  /**
   * Provides a cache associated to the current session.
   * @return the user session cache.
   */
  public SimpleCache getCache() {
    return cache;
  }

  private void checkIsNotOneShot() {
    if (isOneShot()) {
      throw new IllegalStateException("Such operation isn't allowed with one-shot session");
    }
  }
}
