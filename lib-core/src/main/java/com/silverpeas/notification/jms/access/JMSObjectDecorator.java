/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.notification.jms.access;

import javax.jms.Session;

/**
 * A decorator of JMS objects to provide to them, as additional attribute, the JMS session in which
 * they are involved.
 * @param <T> the type of the JMS object this decorator works on.
 */
public abstract class JMSObjectDecorator<T> {

  private Session session;
  private T decoratedObject;

  /**
   * Sets the session from which this JMS object is created.
   * @param session the JMS session to set.
   */
  public void setSession(final Session session) {
    this.session = session;
  }

  /**
   * Gets the JMS session within which this object is living.
   * @return the JMS session.
   */
  public Session getSession() {
    return session;
  }

  /**
   * Gets the JMS object decorated by this.
   * @return the decorated JMS object.
   */
  protected T getDecoratedObject() {
    return decoratedObject;
  }

  /**
   * Sets the JMS object to decorate.
   * @param object the JMS object to decorate.
   */
  protected void setDecoratedObject(final T object) {
    this.decoratedObject = object;
  }
}
