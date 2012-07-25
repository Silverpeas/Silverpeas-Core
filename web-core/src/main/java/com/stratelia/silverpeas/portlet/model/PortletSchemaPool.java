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

package com.stratelia.silverpeas.portlet.model;

import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.SchemaPool;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * The PortletSchemaPool class manages a pool of PortletSchema shared by all the client (admin
 * classes). All the public methods are static and the calls are deferred to a singleton.
 */
public class PortletSchemaPool extends SchemaPool {
  /**
   * The unique PortletSchemaPool built to serve all the requests.
   */
  static private PortletSchemaPool singleton = new PortletSchemaPool();

  /**
   * The constructor is private, so we can ensure that only one pool will be created in the JVM.
   */
  private PortletSchemaPool() {
  }

  /**
   * Returns an Shema. The returned schema must be released after use.
   */
  static public PortletSchema getPortletSchema() throws UtilException {
    return (PortletSchema) singleton.getInstance();
  }

  /**
   * Release an Scheme previously returned by the pool.
   */
  static public void releasePortletSchema(PortletSchema s) {
    singleton.release(s);
  }

  static public void releaseConnections() {
    singleton.releaseSchemas();
  }

  @Override
  protected Schema newSchema() throws UtilException {
    return new PortletSchema();
  }
}
