/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.notification.user.model;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaCustomEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "st_notificationresource")
@NamedQueries({
    @NamedQuery(name = "NotificationResourceData.deleteResources",
        query = "delete from NotificationResourceData r where not exists (from DelayedNotificationData d where d.resource.id = r.id)")
})

public class NotificationResourceData
    extends AbstractJpaCustomEntity<NotificationResourceData, UniqueLongIdentifier>
    implements Cloneable {

  public final static String LOCATION_SEPARATOR = "@#@#@";

  @Column(name = "resourceId", nullable = false)
  private String resourceId;

  @Column(name = "resourceType", nullable = false)
  private String resourceType;

  @Column(name = "resourceName", nullable = false)
  private String resourceName;

  @Column(name = "resourceDescription", nullable = true)
  private String resourceDescription;

  @Column(name = "resourceLocation", nullable = false)
  private String resourceLocation;

  @Column(name = "resourceUrl", nullable = true)
  private String resourceUrl;

  @Column(name = "componentInstanceId", nullable = false)
  private String componentInstanceId;

  /**
   * Copying all data from the given resource excepted the id
   * @param notificationResourceData
   */
  public void fillFrom(final NotificationResourceData notificationResourceData) {
    setResourceId(notificationResourceData.getResourceId());
    setResourceType(notificationResourceData.getResourceType());
    setResourceName(notificationResourceData.getResourceName());
    setResourceDescription(notificationResourceData.getResourceDescription());
    setResourceLocation(notificationResourceData.getResourceLocation());
    setResourceUrl(notificationResourceData.getResourceUrl());
    setComponentInstanceId(notificationResourceData.getComponentInstanceId());
  }

  @Override
  protected void performBeforePersist() {
    super.performBeforePersist();
    forcesNullValues();
  }

  @PreUpdate
  public void beforeUpdate() {
    forcesNullValues();
  }

  private void forcesNullValues() {
    if (isBlank(resourceDescription)) {
      resourceDescription = null;
    }
    if (isBlank(resourceUrl)) {
      resourceUrl = null;
    }
  }

  public boolean isValid() {
    return isNotBlank(resourceId) && isNotBlank(resourceType) && isNotBlank(resourceName) &&
        isNotBlank(resourceLocation) && isNotBlank(resourceUrl) && isNotBlank(componentInstanceId);
  }

  public void setId(final Long id) {
    setId(id != null ? Long.toString(id) : null);
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

  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  public void setComponentInstanceId(final String componentInstanceId) {
    this.componentInstanceId = componentInstanceId;
  }
}
