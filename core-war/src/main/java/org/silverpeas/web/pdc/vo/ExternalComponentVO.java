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

package org.silverpeas.web.pdc.vo;

/**
 * ExternalComponentVO represents an external component value object
 */
public class ExternalComponentVO {

  private String serverName = "";
  private String serverURL = "";
  private String componentId = "";

  /**
   * Default constructor
   */
  public ExternalComponentVO() {
    super();
  }

  /**
   * Constructor using fields
   * @param serverName
   * @param serverURL
   * @param componentId
   */
  public ExternalComponentVO(String serverName, String serverURL, String componentId) {
    super();
    this.serverName = serverName;
    this.serverURL = serverURL;
    this.componentId = componentId;
  }

  /**
   * @return the serverName
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * @param serverName the serverName to set
   */
  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  /**
   * @return the serverURL
   */
  public String getServerURL() {
    return serverURL;
  }

  /**
   * @param serverURL the serverURL to set
   */
  public void setServerURL(String serverURL) {
    this.serverURL = serverURL;
  }

  /**
   * @return the componentId
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * @param componentId the componentId to set
   */
  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

}
