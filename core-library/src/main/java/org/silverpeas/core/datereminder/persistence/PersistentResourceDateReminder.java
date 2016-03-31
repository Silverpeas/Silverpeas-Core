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
package org.silverpeas.core.datereminder.persistence;

import org.silverpeas.core.util.StringUtil;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.silverpeas.core.persistence.EntityReference;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaEntity;
import org.silverpeas.core.datereminder.exception.DateReminderValidationException;
import org.silverpeas.core.util.logging.SilverLogger;

/**
 * A persistent date reminder used to identify uniquely a resource.
 *
 * This date reminder has the particularity to be persisted in a data source and to refer the resource it
 * identifies uniquely both by the resource identifier and by the resource type.
 *
 * @author Cécile Bonin
 */
@Entity
@Table(name = "st_dateReminder")
@NamedQueries({
    @NamedQuery(name = "getResource", query = "from PersistentResourceDateReminder where " +
        "resourceType = :type and resourceId = :resourceId"),
    @NamedQuery(name = "getListResourceByDeadLine", query = "from PersistentResourceDateReminder " +
        "where processStatus = 0 and dateReminder <= :dateReminder")
})
public class PersistentResourceDateReminder
    extends AbstractJpaEntity<PersistentResourceDateReminder, UuidIdentifier> {

  private static final long serialVersionUID = 5956074363457906409L;

  /**
   * Represents none dateReminder to replace in more typing way the null keyword.
   */
  public static final PersistentResourceDateReminder NONEDATEREMINDER = new PersistentResourceDateReminder();

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

  /**
   * Validates data
   *
   * @throws DateReminderValidationException
   */
  public void validate() throws DateReminderValidationException {
    if (this.resourceType == null || EntityReference.UNKNOWN_TYPE.equals(resourceType)
        || !StringUtil.isDefined(resourceId)) {
      throw new DateReminderValidationException("The dateReminder isn't valid! Missing resource reference");
    }

    if (this.dateReminder == null) {
      throw new DateReminderValidationException("The dateReminder isn't valid! Missing the date");
    }
  }


  /**
   * Gets a reference to the resource this dateReminder is for.
   *
   * @param <E> the concrete type of the entity.
   * @param <R> the concrete type of the reference to the entity.
   * @param referenceClass the expected concrete class of the <code>EntityReference</code>. This
   * class must be conform to the type of the resource.
   * @return a reference to the resource that owns this dateReminder or null if there is neither no
   * resource defined for this dateReminder nor no reference defined for the targeted type of resource.
   */
  public <E, R extends EntityReference<E>> R getResource(Class<R> referenceClass) {
    R ref = null;
    if (resourceType != null && !resourceType.equals(EntityReference.UNKNOWN_TYPE) && StringUtil.
        isDefined(resourceId)) {
      try {
        ref = referenceClass.getConstructor(String.class).newInstance(resourceId);
        if (!ref.getType().equals(resourceType)) {
          ref = null;
        }
      } catch (Exception ex) {
        SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      }
    }
    return ref;
  }

  /**
   * Sets the resource to which this dateReminder belongs.
   *
   * @param resource an identifier of the resource for which this dateReminder is.
   */
  public void setResource(final EntityReference resource) {
    if (resource != null) {
      this.resourceType = resource.getType();
      this.resourceId = resource.getId();
    }
  }

  /**
   * Get the resource type if the resource.
   *
   * @return the resource type
   */
  public String getResourceType() {
    return resourceType;
  }

  /**
   * @return the dateReminder
   */
  public DateReminderDetail getDateReminder() {
    return new DateReminderDetail(this.dateReminder, this.message, this.processStatus,
        this.getCreatedBy(), this.getLastUpdatedBy());
  }

  /**
   * Sets the creatorId
   * @param userId
   */
  private void setCreatorId(String userId) {
    super.setCreatedBy(userId);
  }

  /**
   * Sets the updaterId
   * @param userId
   */
  private void setUpdaterId(String userId) {
    super.setLastUpdatedBy(userId);
  }

  /**
   * Sets the date, message, processStatus, creatorId and updaterId
   *
   * @param dateReminder
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
    return "PersistentResourceDateReminder{" + "resourceType='" + resourceType + '\'' + ", resourceId='"
        + resourceId + '\'' + ", dateReminder ='" + dateReminder + '\'' + ", message ='" + message + '\'' + '}';
  }

}