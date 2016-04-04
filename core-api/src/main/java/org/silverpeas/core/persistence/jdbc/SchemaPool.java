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

package org.silverpeas.core.persistence.jdbc;

import org.silverpeas.core.exception.UtilException;

import java.util.HashSet;
import java.util.Set;

/**
 * The SchemaPool class manages a pool of Schema shared by all the client (admin classes). All the
 * public methods are static and the calls are deferred to a singleton.
 */
public abstract class SchemaPool {

  /**
   * The unique SchemaPool built to serve all the requests.
   */
  private final Set<Schema> pool = new HashSet<Schema>();

  abstract protected Schema newSchema() throws UtilException;

  /**
   * The constructor is private, so we can ensure that only one pool will be created in the JVM.
   */
  protected SchemaPool() {
  }

  protected void releaseSchemas() {
    synchronized (pool) {
      for (Schema schema : pool) {
        release(schema);
      }
    }
  }

  /**
   * Returns an Shema. The returned schema is removed from the pool : so, if our client forgets to
   * release this schema we don't keep a useless entry.
   */
  protected Schema getInstance() throws UtilException {
    Schema instance = newSchema();
    pool.add(instance);
    return instance;
  }

  /**
   * Release an Scheme previously returned by the pool. We put the released schema in the pool
   * unless this schema isn't ok.
   */
  protected void release(Schema s) {
    synchronized (pool) {
      pool.remove(s);
      s.close();
    }
  }
}
