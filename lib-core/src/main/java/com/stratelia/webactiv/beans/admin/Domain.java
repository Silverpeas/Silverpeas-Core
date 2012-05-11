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

package com.stratelia.webactiv.beans.admin;

import java.io.Serializable;

public class Domain implements Serializable {

  private static final long serialVersionUID = 7451639218436788229L;
  private String id;
  private String name;
  private String description;
  private String driverClassName;
  private String propFileName;
  private String authenticationServer;
  private String theTimeStamp = "0";
  private String silverpeasServerURL = "";

  public Domain() {
  }

  /**
   * @return String
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return String
   */
  public String getTheTimeStamp() {
    return this.theTimeStamp;
  }

  /**
   * @param id
   */
  public void setTheTimeStamp(String tt) {
    this.theTimeStamp = tt;
  }

  /**
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return String
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param descriptionId
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return String
   */
  public String getDriverClassName() {
    return driverClassName;
  }

  /**
   * @param className
   */
  public void setDriverClassName(String className) {
    this.driverClassName = className;
  }

  /**
   * @return String
   */
  public String getPropFileName() {
    return propFileName;
  }

  /**
   * @param className
   */
  public void setPropFileName(String propFileName) {
    this.propFileName = propFileName;
  }

  /**
   * @return String
   */
  public String getAuthenticationServer() {
    return authenticationServer;
  }

  /**
   * @param className
   */
  public void setAuthenticationServer(String authenticationServer) {
    this.authenticationServer = authenticationServer;
  }

  /**
   * @return String
   */
  public String getSilverpeasServerURL() {
    return silverpeasServerURL;
  }

  /**
   * @param className
   */
  public void setSilverpeasServerURL(String silverpeasServerURL) {
    this.silverpeasServerURL = silverpeasServerURL;
  }

  @Override
  public String toString() {
    return "Domain{" + "id=" + id + ", name=" + name + ", description=" + description
        + ", driverClassName=" + driverClassName + ", propFileName=" + propFileName
        + ", authenticationServer=" + authenticationServer + ", theTimeStamp=" + theTimeStamp
        + ", silverpeasServerURL=" + silverpeasServerURL + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Domain other = (Domain) obj;
    if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
      return false;
    }
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    if ((this.description == null) ? (other.description != null) : !this.description
        .equals(other.description)) {
      return false;
    }
    if ((this.driverClassName == null) ? (other.driverClassName != null) : !this.driverClassName
        .equals(other.driverClassName)) {
      return false;
    }
    if ((this.propFileName == null) ? (other.propFileName != null) : !this.propFileName
        .equals(other.propFileName)) {
      return false;
    }
    if ((this.authenticationServer == null) ? (other.authenticationServer != null)
        : !this.authenticationServer.equals(other.authenticationServer)) {
      return false;
    }
    if ((this.theTimeStamp == null) ? (other.theTimeStamp != null) : !this.theTimeStamp
        .equals(other.theTimeStamp)) {
      return false;
    }
    if ((this.silverpeasServerURL == null) ? (other.silverpeasServerURL != null)
        : !this.silverpeasServerURL.equals(other.silverpeasServerURL)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 73 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 73 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 73 * hash + (this.description != null ? this.description.hashCode() : 0);
    hash = 73 * hash + (this.driverClassName != null ? this.driverClassName.hashCode() : 0);
    hash = 73 * hash + (this.propFileName != null ? this.propFileName.hashCode() : 0);
    hash =
        73 * hash + (this.authenticationServer != null ? this.authenticationServer.hashCode() : 0);
    hash = 73 * hash + (this.theTimeStamp != null ? this.theTimeStamp.hashCode() : 0);
    hash = 73 * hash + (this.silverpeasServerURL != null ? this.silverpeasServerURL.hashCode() : 0);
    return hash;
  }

}
