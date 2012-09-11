/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.process.session;

import java.util.HashMap;
import java.util.Map;

/**
 * The abstract root implementation of <code>Session</code> interface.
 * All methods of the interface are implemented.
 * @author Yohann Chastagnier
 */
public abstract class AbstractSession implements Session {

  private final String id;
  private final Map<String, Object> attributes = new HashMap<String, Object>();

  /**
   * Default constructor
   * @param userId
   * @param userDetail
   * @param componentInstanceId
   */
  protected AbstractSession() {
    id = buildSessionId();
  }

  /**
   * Builds an unique identifier
   * @return
   */
  protected String buildSessionId() {
    return new StringBuilder().append(System.currentTimeMillis()).append("-")
        .append(System.identityHashCode(this)).toString();
  }

  /**
   * @return the sessionId
   */
  @Override
  public String getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.io.session.IOSession#setAttribute(java.lang.String, java.lang.Object)
   */
  @Override
  public void setAttribute(final String name, final Object value) {
    attributes.put(name, value);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.io.session.IOSession#getAttribute(java.lang.String)
   */
  @Override
  public Object getAttribute(final String name) {
    return attributes.get(name);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.io.session.IOSession#getAttribute(java.lang.String, java.lang.Class)
   */
  @Override
  public <C> C getAttribute(final String name, final Class<C> expectedReturnedClass) {
    return (C) attributes.get(name);
  }
}
