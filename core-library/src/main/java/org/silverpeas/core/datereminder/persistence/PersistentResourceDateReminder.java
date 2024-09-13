/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.datereminder.persistence;

import org.silverpeas.core.datereminder.exception.DateReminderValidationException;
import org.silverpeas.core.persistence.EntityReference;
import org.silverpeas.core.persistence.ResourceBelonging;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.kernel.util.StringUtil;

import javax.persistence.*;
import java.util.Date;

/**
 * A persistent date reminder used to identify uniquely a resource. This date reminder has the
 * particularity to be persisted in a data source and to refer the resource it identifies uniquely
 * both by the resource identifier and by the resource type.
 *
 * @author Cécile Bonin
 */
@Entity
@Table(name = "st_dateReminder")
@NamedQuery(name = "getResource", query =
    "select p from PersistentResourceDateReminder p " +
        "where p.resourceType = :type and p.resourceId = :resourceId")
@NamedQuery(name = "getListResourceByDeadLine", query =
    "select p from PersistentResourceDateReminder p " +
        "where p.processStatus = 0 and p.dateReminder <= :dateReminder")
public class PersistentResourceDateReminder
    extends SilverpeasJpaEntity<PersistentResourceDateReminder, UuidIdentifier>
    implements ResourceBelonging {

  private static final long serialVersionUID = 5956074363457906409L;

  /**
   * Represents none dateReminder to replace in more typing way the null keyword.
   */
  public static final PersistentResourceDateReminder NONEDATEREMINDER =
      new PersistentResourceDateReminder();

  @Column(name = "resourceType", nullable = false)
  private String resourceType = EntityReference.UNKNOWN_TYPE;

  @Column(name = "resourceId", nullable = false)
  private String resourceId;

  @Column(name = "dateReminder", nullable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date dateReminder;

  @Column(name = "message")
  private String message;

  @Column(name = "processStatus")
  private int processStatus; //0 || 1

  public PersistentResourceDateReminder() {
    // nothing to initialize
  }

  /**
   * Indicates if the dateReminder is well registred
   *
   * @return a boolean indicating if this dateReminder exists in the data source.
   */
  public boolean exists() {
    return getId() != null;
  }

  /**
   * Indicates if the dateReminder isn't registered.
   *
   * @return a boolean indicating if this dateReminder doesn't exist in the data source.
   */
  public boolean notExists() {
    return getId() == null;
  }

  public void validate() throws DateReminderValidationException {
    if (this.resourceType == null || EntityReference.UNKNOWN_TYPE.equals(resourceType)
        || !StringUtil.isDefined(resourceId)) {
      throw new DateReminderValidationException("The dateReminder isn't valid! Missing resource " +
          "reference");
    }

    if (this.dateReminder == null) {
      throw new DateReminderValidationException("The dateReminder isn't valid! Missing the date");
    }
  }

  /**
   * Sets the resource to which this dateReminder belongs.
   *
   * @param resource an identifier of the resource for which this dateReminder is.
   */
  public void setResource(final EntityReference<?> resource) {
    if (resource != null) {
      this.resourceType = resource.getType();
      this.resourceId = resource.getId();
    }
  }

  @Override
  public String getResourceType() {
    return resourceType;
  }

  @Override
  public String getResourceId() {
    return resourceId;
  }

  /**
   * @return the dateReminder
   */
  public DateReminderDetail getDateReminder() {
    return new DateReminderDetail(this.dateReminder, this.message, this.processStatus,
        this.getCreatorId(), this.getLastUpdaterId());
  }

  private void setCreatorId(String userId) {
    super.createdBy(userId);
  }

  private void setUpdaterId(String userId) {
    super.lastUpdatedBy(userId);
  }

  /**
   * Sets the date, message, processStatus, creatorId and updaterId
   *
   * @param dateReminder the date reminder detail with which to set data.
   */
  public void setDateReminder(DateReminderDetail dateReminder) {
    if (dateReminder != null) {
      this.dateReminder = dateReminder.getDateReminder();
      this.message = dateReminder.getMessage();
      this.processStatus = dateReminder.getProcessStatus();
      setCreatorId(dateReminder.getCreatorId());
      setUpdaterId(dateReminder.getUpdaterId());
    }
  }

  /**
   * Return true if resource is defined
   *
   * @return boolean
   */
  public boolean isDefined() {
    return this.isPersisted() && this != NONEDATEREMINDER;
  }


  @Override
  public String toString() {
    return "PersistentResourceDateReminder{" + "resourceType='" + resourceType + '\'' + ", " +
        "resourceId='"
        + resourceId + '\'' + ", dateReminder ='" + dateReminder + '\'' + ", message ='" + message + '\'' + '}';
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }
}