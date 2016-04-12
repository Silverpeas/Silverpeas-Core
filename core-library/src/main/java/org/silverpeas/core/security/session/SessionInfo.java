/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.session;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.InMemoryCacheService;
import org.silverpeas.core.cache.service.SimpleCacheService;

import java.util.HashMap;
import java.util.Map;

/**
 * It gathers information about an opened session of a user.
 */
public class SessionInfo {

  public static final SessionInfo NoneSession = new SessionInfo(null, null);
  public static final SessionInfo AnonymousSession = getAnonymousSession();

  private final String sessionId;
  private String ipAddress;
  private final UserDetail userDetail;
  private final long openingTimestamp;
  private long lastAccessTimestamp;
  private final Map<String, Object> attributes = new HashMap<String, Object>();
  private long idleTimestamp;
  private final InMemoryCacheService cache = new InMemoryCacheService();

  private static SessionInfo getAnonymousSession() {
    UserDetail anonymousUser = UserDetail.getAnonymousUser();
    if (anonymousUser != null) {
      return new SessionInfo(null, anonymousUser);
    }
    return NoneSession;
  }

  /**
   * Constructs a new instance about a given opened user session.
   *
   * @param sessionId the identifier of the opened session.
   * @param user the user for which a session was opened.
   */
  public SessionInfo(final String sessionId, final UserDetail user) {
    this.sessionId = sessionId;
    this.userDetail = user;
    this.openingTimestamp = this.lastAccessTimestamp = System.currentTimeMillis();
    this.idleTimestamp = 0;
    this.cache.put(UserDetail.CURRENT_REQUESTER_KEY, user);
    CacheServiceProvider.getRequestCacheService().put("@SessionCache@", cache);
  }

  /**
   * Sets the IP address of the remote client that requests a session opening with Silverpeas.
   *
   * @param ip the IP address of the remote client.
   */
  public void setIPAddress(String ip) {
    this.ipAddress = ip;
  }

  /**
   * Gets the IP address of the remote client that opened the session.
   *
   * @return the client remote IP address.
   */
  public String getIPAddress() {
    return ipAddress;
  }

  /**
   * Gets the timestamp of the last access by the client behind this session.
   *
   * @return timestamp of the last access.
   */
  public long getLastAccessTimestamp() {
    return lastAccessTimestamp;
  }

  /**
   * Gets the timestamp at which the session with Silverpeas was opened.
   *
   * @return the session opening timestamp.
   */
  public long getOpeningTimestamp() {
    return openingTimestamp;
  }

  /**
   * Gets the last duration of its idle time.
   *
   * @return the session alive timestamp.
   */
  public long getLastIdleDuration() {
    return System.currentTimeMillis() - idleTimestamp;
  }

  /**
   * Sets this session as currently idle. A session is idle if it is not used since a given time but
   * it is still in alive.
   */
  public void setAsIdle() {
    idleTimestamp = System.currentTimeMillis();
  }

  /**
   * Gets the unique identifier of the session.
   *
   * @return the session identifier.
   */
  public String getSessionId() {
    return sessionId;
  }

  /**
   * Gets the profile of the user that opened the session.
   *
   * @return a UserDetail instance with the profile information on the user.
   */
  public UserDetail getUserDetail() {
    return userDetail;
  }

  /**
   * Updates the last access timestamp.
   */
  public void updateLastAccess() {
    this.lastAccessTimestamp = System.currentTimeMillis();
    this.idleTimestamp = 0;
  }

  /**
   * Sets an attribute named by the specified name with the specified value.
   *
   * If no attribute exists with the specified name, then it is added to the session.
   *
   * @param <T> the type of the attribute value.
   * @param name the name of the attribute to set.
   * @param value the value of the attribute to set.
   */
  public <T> void setAttribute(String name, T value) {
    attributes.put(name, value);
  }

  /**
   * Gets the value of the attribute named by the specified name.
   *
   * @param <T> the type of the attribute value.
   * @param name the name of the attribute to get.
   * @return the value of the attribute or null if no such attribute exists.
   */
  public <T> T getAttribute(String name) {
    return (T) attributes.get(name);
  }

  /**
   * Unsets the specified attribute.
   *
   * The consequence of an unset is the attribute is then removed from the session.
   *
   * @param name the name of the attibute to unset.
   */
  public void unsetAttribute(String name) {
    attributes.remove(name);
  }

  /**
   * Frees the allocated resources used in the session management and carried by this session
   * information.
   *
   * This method must be called at session closing by the session management system.
   */
  public void onClosed() {
    attributes.clear();
  }

  /**
   * Is this session is defined? A session is defined if it's a session opened to a user in
   * Silverpeas.
   *
   * @return true if this session is defined, false otherwise.
   */
  public boolean isDefined() {
    return this != NoneSession && this.getUserDetail() != null;
  }

  /**
   * Provides a cache associated to the current session.
   * @return
   */
  public SimpleCacheService getCache() {
    return cache;
  }
}
