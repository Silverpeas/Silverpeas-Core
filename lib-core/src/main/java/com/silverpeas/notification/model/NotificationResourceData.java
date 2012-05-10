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
package com.silverpeas.notification.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.silverpeas.util.StringUtil;

/**
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "sp_notificationresource")
public class NotificationResourceData {

  @Id
  @TableGenerator(name = "UNIQUE_ID_GEN", table = "uniqueId", pkColumnName = "tablename",
      valueColumnName = "maxId", pkColumnValue = "sp_notificationresource", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "UNIQUE_ID_GEN")
  @Column(name = "id")
  private Long id;

  @Column(name = "resourceId", nullable = false)
  private String resourceId;

  @Column(name = "resourceType", nullable = false)
  private String resourceType;

  @Column(name = "resourceName", nullable = false)
  private String resourceName;

  @Column(name = "resourceDescription", nullable = false)
  private String resourceDescription;

  @Column(name = "resourceLocation", nullable = false)
  private String resourceLocation;

  @Column(name = "resourceUrl", nullable = false)
  private String resourceUrl;

  /**
   * Simple constructor
   */
  public NotificationResourceData() {
    // NTD
  }

  public boolean isValid() {
    return isDefined(resourceId) && isDefined(resourceType) && isDefined(resourceName) &&
        isDefined(resourceLocation) && isDefined(resourceUrl);
  }

  private static boolean isDefined(final String string) {
    return string != null && StringUtil.isDefined(string.trim());
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(final Number resourceId) {
    setResourceId((Object) resourceId);
  }

  public void setResourceId(final String resourceId) {
    setResourceId((Object) resourceId);
  }

  private void setResourceId(final Object resourceId) {
    if (resourceId != null) {
      this.resourceId = resourceId.toString();
    } else {
      this.resourceId = null;
    }
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(final String resourceType) {
    this.resourceType = resourceType;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(final String resourceName) {
    this.resourceName = resourceName;
  }

  public String getResourceDescription() {
    return resourceDescription;
  }

  public void setResourceDescription(final String resourceDescription) {
    this.resourceDescription = resourceDescription;
  }

  public String getResourceLocation() {
    return resourceLocation;
  }

  public void setResourceLocation(final String resourceLocation) {
    this.resourceLocation = resourceLocation;
  }

  public String getResourceUrl() {
    return resourceUrl;
  }

  public void setResourceUrl(final String resourceUrl) {
    this.resourceUrl = resourceUrl;
  }
}
