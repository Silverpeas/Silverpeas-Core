/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.ui.DisplayI18NHelper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.silverpeas.core.util.JSONCodec.decode;
import static org.silverpeas.core.util.JSONCodec.encode;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * Data on the notification about an action operated on a resource in Silverpeas. A resource can
 * be a contribution, a business object, or any entities handled or managed in Silverpeas.
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "st_notificationresource")
@NamedQueries({
    @NamedQuery(name = "NotificationResourceData.deleteResources",
        query = "delete from NotificationResourceData r where not exists (from DelayedNotificationData d where d.resource.id = r.id)")
})
public class NotificationResourceData
    extends BasicJpaEntity<NotificationResourceData, UniqueLongIdentifier> {
  public static final String LOCATION_SEPARATOR = "@#@#@";
  private static final long serialVersionUID = -6720839869471833683L;
  private static final String EMPTY_JSON_DETAILS = "{}";
  private static final String TITLE_KEY = "title";
  private static final String DESCRIPTION_KEY = "description";
  private static final String LINK_KEY = "link";

  @Column(name = "resourceId", nullable = false)
  private String resourceId;

  @Column(name = "resourceType", nullable = false)
  private String resourceType;

  @Column(name = "resourceName", nullable = false)
  private String resourceName;

  @Column(name = "resourceDescription")
  private String resourceDescription;

  @Column(name = "resourceLocation", nullable = false)
  private String resourceLocation;

  @Column(name = "resourceUrl")
  private String resourceUrl;

  @Column(name = "componentInstanceId", nullable = false)
  private String componentInstanceId;

  @Column(name = "attachmentTargetId")
  private String attachmentTargetId;

  @Column(name = "resourceDetails")
  private String details;

  @Transient
  private transient NotificationResourceDataDetails transientDetails;

  @Transient
  private transient String currentLanguage = DisplayI18NHelper.getDefaultLanguage();

  /**
   * Constructs a new empty {@link NotificationResourceData} instance.
   */
  public NotificationResourceData() {
  }

  /**
   * Constructs a new instance as a copy of the specified notification resource data.
   * @param notificationResourceData the {@link NotificationResourceData} instance to copy.
   */
  public NotificationResourceData(final NotificationResourceData notificationResourceData) {
    fillFrom(notificationResourceData);
  }

  /**
   * Copying all data from the given resource excepted the id
   * @param notificationResourceData the data from which all is copied.
   */
  public final void fillFrom(final NotificationResourceData notificationResourceData) {
    this.resourceId = notificationResourceData.resourceId;
    this.resourceType = notificationResourceData.resourceType;
    this.resourceName = notificationResourceData.resourceName;
    this.resourceDescription = notificationResourceData.resourceDescription;
    this.resourceLocation = notificationResourceData.resourceLocation;
    this.resourceUrl = notificationResourceData.resourceUrl;
    getDetails().merge(notificationResourceData.transientDetails);
    this.componentInstanceId = notificationResourceData.componentInstanceId;
    this.attachmentTargetId = notificationResourceData.attachmentTargetId;
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
    final String labels = transientDetails != null ? encode(transientDetails) : null;
    details = defaultStringIfNotDefined(labels, null);
  }

  public boolean isValid() {
    return isNotBlank(resourceId) && isNotBlank(resourceType) && isNotBlank(getResourceName()) &&
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
    return getLocalizedDetail(TITLE_KEY, this.resourceName);
  }

  public void setResourceName(final String resourceName) {
    this.resourceName = defaultStringIfNotDefined(setLocalizedDetail(TITLE_KEY, resourceName, this.resourceName));
  }

  public String getResourceDescription() {
    return getLocalizedDetail(DESCRIPTION_KEY, this.resourceDescription);
  }

  public void setResourceDescription(final String resourceDescription) {
    this.resourceDescription = setLocalizedDetail(DESCRIPTION_KEY, resourceDescription, this.resourceDescription);
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

  public String getAttachmentTargetId() {
    return this.attachmentTargetId;
  }

  public void setAttachmentTargetId(final String targetId) {
    this.attachmentTargetId = targetId;
  }

  public boolean isFeminineGender() {
    return getDetails().isFeminineGenderResource();
  }

  public void setFeminineGender(final boolean gender) {
    getDetails().setFeminineGenderResource(gender);
  }

  public String getLinkLabel() {
    return getLocalizedDetail(LINK_KEY, "");
  }

  public void setLinkLabel(final String linkLabel) {
    getDetails().putLocalized(currentLanguage, LINK_KEY, linkLabel);
  }

  /**
   * Gets the current language into which the data are registered and provided by this entity.
   * @return a string.
   */
  public String getCurrentLanguage() {
    return currentLanguage;
  }

  /**
   * Sets the current language into which the data are registered and provided by this entity.
   */
  public void setCurrentLanguage(final String currentLanguage) {
    this.currentLanguage = currentLanguage;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  private NotificationResourceDataDetails getDetails() {
    if (transientDetails == null) {
      transientDetails = decode(defaultStringIfNotDefined(details, EMPTY_JSON_DETAILS),
          NotificationResourceDataDetails.class);
    }
    return transientDetails;
  }

  private String getLocalizedDetail(final String localizedKey, final String defaultData) {
    final String localizedData = getDetails().getLocalized(currentLanguage, localizedKey);
    return defaultStringIfNotDefined(localizedData, defaultData);
  }

  private String setLocalizedDetail(final String localizedKey, final String localizedData,
      final String defaultData) {
    final String result;
    if (DisplayI18NHelper.getDefaultLanguage().equals(currentLanguage)) {
      result = localizedData;
    } else {
      result = defaultData;
      getDetails().putLocalized(currentLanguage, localizedKey, localizedData);
    }
    return result;
  }
}
