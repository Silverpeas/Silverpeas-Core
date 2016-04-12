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
package org.silverpeas.core.index.indexing.model;

import java.io.Serializable;

/**
 * An ExternalComponent object.
 */
public final class ExternalComponent implements Serializable {

  private static final long serialVersionUID = 7721088829719133520L;
  /**
   * The whole parts of an ExternalComponent are private and fixed at construction time.
   */
  private final String server;
  private final String component;
  private final String dataPath;
  private final String url;

  /**
   * @param server
   * @param component
   * @param dataPath
   * @param url
   */
  public ExternalComponent(String server, String component, String dataPath, String url) {
    super();
    this.server = server;
    this.component = component;
    this.dataPath = dataPath;
    this.url = url;
  }

  /**
   * Return the server name.
   * @return
   */
  public String getServer() {
    return server;
  }

  /**
   * Return the component name.
   * @return
   */
  public String getComponent() {
    return component;
  }

  /**
   * @return the dataPath
   */
  public String getDataPath() {
    return dataPath;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * The equals method is re-defined so that an ExternalComponent can be added in a Set or used as a
   * Map key.
   * @param o
   */
  @Override
  public boolean equals(Object o) {
    if (o != null && o instanceof ExternalComponent) {
      ExternalComponent p = (ExternalComponent) o;
      return component.equals(p.component);
    }
    return false;
  }

  /**
   * The hashCode method is re-defined so that an ExternalComponent can be added in a Set or used as
   * a Map key.
   */
  @Override
  public int hashCode() {
    String s = "*";
    String c = "*";

    if (server != null) {
      s = server;
    }
    if (component != null) {
      c = component;
    }

    return (s + "/" + c).hashCode();
  }
}
