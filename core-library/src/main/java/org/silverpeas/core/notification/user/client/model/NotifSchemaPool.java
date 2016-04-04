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

package org.silverpeas.core.notification.user.client.model;

import org.silverpeas.core.persistence.jdbc.Schema;
import org.silverpeas.core.persistence.jdbc.SchemaPool;
import org.silverpeas.core.exception.UtilException;

/**
 * The NotifSchemaPool class manages a pool of NotifSchema shared by all the client (admin classes).
 * All the public methods are static and the calls are deferred to a singleton.
 */
public class NotifSchemaPool extends SchemaPool {
  /**
   * The unique NotifSchemaPool built to serve all the requests.
   */
  static private NotifSchemaPool singleton = new NotifSchemaPool();

  /**
   * The constructor is private, so we can ensure that only one pool will be created in the JVM.
   */
  private NotifSchemaPool() {
  }

  /**
   * Returns an Shema. The returned schema must be released after use.
   */
  static public NotifSchema getNotifSchema() throws UtilException {
    return (NotifSchema) singleton.getInstance();
  }

  /**
   * Release an Scheme previously returned by the pool.
   */
  static public void releaseNotifSchema(NotifSchema s) {
    singleton.release(s);
  }

  static public void releaseConnections() {
    singleton.releaseSchemas();
  }

  @Override
  protected Schema newSchema() throws UtilException {
    return new NotifSchema();
  }
}
