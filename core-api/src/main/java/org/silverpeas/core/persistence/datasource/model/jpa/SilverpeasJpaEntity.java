/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.persistence.datasource.model.jpa;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.model.Entity;
import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;
import org.silverpeas.core.util.ArgumentAssertion;
import org.silverpeas.core.util.StringUtil;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.util.Date;

/**
 * This abstract class must be extended by all Silverpeas JPA entities that satisfy all the rules
 * of the Silverpeas Persistence API.
 * All the creation and update related data are managed by this class.
 * <p>
 * Please be careful into the child entity classes about the use of @PrePersist and @PreUpdate
 * annotations. In most of cases you don't need to use them, but to override {@link
 * SilverpeasJpaEntity#performBeforePersist} or {@link SilverpeasJpaEntity#performBeforeUpdate}
 * methods without forgetting to invoke the super call.
 * @param <E> the class name of the represented entity.
 * @param <I> the unique identifier class used by the entity to identify it uniquely
 * in the persistence context.
 * @author Yohann Chastagnier
 */
@MappedSuperclass
public abstract class SilverpeasJpaEntity<E extends Entity<E, I>, I extends EntityIdentifier>
    extends AbstractJpaEntity<E, I> implements Entity<E, I> {

  private static final long serialVersionUID = 5862667014447543891L;

  @Transient
  private User creator;

  @Transient
  private User lastUpdater;

  @Column(name = "createdBy", nullable = false, insertable = true, updatable = false, length = 40)
  private String creatorId;

  @Column(name = "createDate", nullable = false, insertable = true, updatable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date creationDate;

  @Column(name = "lastUpdatedBy", nullable = false, length = 40)
  private String lastUpdaterId;

  @Column(name = "lastUpdateDate", nullable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date lastUpdateDate;

  @Version
  @Column(name = "version", nullable = false)
  private Long version = 0L;

  @Override
  public boolean hasBeenModified() {
    return isPersisted() && getVersion() > 0;
  }

  @Override
  public final User getCreator() {
    if (StringUtil.isDefined(getCreatorId())) {
      if (creator == null || !getCreatorId().equals(creator.getId())) {
        creator = User.getById(getCreatorId());
      }
    } else {
      creator = null;
    }
    return creator;
  }

  @Override
  public E createdBy(final User creator) {
    return createdBy(creator, new Date());
  }

  @SuppressWarnings("unchecked")
  @Override
  public E createdBy(final User creator, final Date creationDate) {
    ArgumentAssertion.assertNotNull(creator, "creator must exist");
    ArgumentAssertion.assertNotNull(creationDate, "create date must exist");
    if (!isPersisted() && creator != null && creationDate != null) {
      OperationContext.getFromCache()
          .getPersistenceOperation(JpaPersistOperation.class)
          .setManuallyTechnicalDataFor(this, creator, creationDate);
    } else {
      setCreationDate(creationDate);
      this.creator = creator;
      this.creatorId = creator != null ? creator.getId() : null;
    }
    return (E) this;
  }

  @Override
  public final User getLastUpdater() {
    if (lastUpdater == null ||
        (getLastUpdaterId() != null && !getLastUpdaterId().equals(lastUpdater.getId()))) {
      lastUpdater = User.getById(getLastUpdaterId());
    }
    return lastUpdater;
  }

  @Override
  public E updatedBy(final User updater) {
    return updatedBy(updater, new Date());
  }

  @SuppressWarnings("unchecked")
  @Override
  public E updatedBy(final User updater, Date updateDate) {
    ArgumentAssertion.assertNotNull(updater, "updater must exist");
    ArgumentAssertion.assertNotNull(updateDate, "update date must exist");
    if (isPersisted()) {
      OperationContext.getFromCache()
          .getPersistenceOperation(JpaUpdateOperation.class)
          .setManuallyTechnicalDataFor(this, updater, updateDate);
    } else {
      setLastUpdateDate(updateDate);
      this.lastUpdater = updater;
      this.lastUpdaterId = updater.getId();
    }
    return (E) this;
  }

  @Override
  public String getCreatorId() {
    return creatorId;
  }

  @SuppressWarnings("unchecked")
  public final E createdBy(final String creatorId) {
    return createdBy(User.getById(creatorId), new Date());
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @SuppressWarnings("unchecked")
  protected final SilverpeasJpaEntity setCreationDate(final Date creationDate) {
    this.creationDate = creationDate;
    return this;
  }

  protected final SilverpeasJpaEntity setCreator(final User creator) {
    this.creator = creator;
    this.creatorId = creator == null ? null : creator.getId();
    return this;
  }

  @Override
  public Date getLastUpdateDate() {
    return lastUpdateDate == null ? creationDate : lastUpdateDate;
  }

  @SuppressWarnings("unchecked")
  protected final SilverpeasJpaEntity setLastUpdateDate(final Date lastUpdateDate) {
    this.lastUpdateDate = lastUpdateDate;
    return this;
  }

  protected final SilverpeasJpaEntity setLastUpdater(final User updater) {
    this.lastUpdater = updater;
    this.lastUpdaterId = updater == null ? null : updater.getId();
    return this;
  }

  @Override
  public String getLastUpdaterId() {
    return this.lastUpdaterId;
  }

  @SuppressWarnings("unchecked")
  public final E lastUpdatedBy(final String lastUpdaterId) {
    return updatedBy(User.getById(lastUpdaterId), new Date());
  }

  @Override
  public Long getVersion() {
    return version;
  }

  @SuppressWarnings("unchecked")
  protected final E setVersion(final Long version) {
    this.version = version;
    return (E) this;
  }

  @Override
  public final void markAsModified() {
    if (getLastUpdateDate() != null) {
      setLastUpdateDate(new Date(getLastUpdateDate().getTime() + 1));
    }
  }

  @Override
  public int hashCode() {
    HashCodeBuilder hash = new HashCodeBuilder();
    hash.append(getId() != null ? getId() : super.hashCode());
    return hash.toHashCode();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (super.equals(obj)) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final E other = (E) obj;
    if (getId() != null && other.getId() != null) {
      EqualsBuilder matcher = new EqualsBuilder();
      matcher.append(getId(), other.getId());
      return matcher.isEquals();
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @SuppressWarnings({"unchecked", "CloneDoesntDeclareCloneNotSupportedException"})
  @Override
  public E clone() {
    SilverpeasJpaEntity clone = (SilverpeasJpaEntity) super.clone();
    if (clone != null) {
      clone.setCreator(null);
      clone.setCreationDate(null);
      clone.setLastUpdater(null);
      clone.setLastUpdateDate(null);
      clone.setVersion(0L);
      clone.clearSystemData();
    }
    return (E) clone;
  }

  @Override
  protected void performBeforePersist() {
    OperationContext.getFromCache()
        .getPersistenceOperation(JpaPersistOperation.class)
        .applyTechnicalDataTo(this);
    ArgumentAssertion.assertDefined(getCreatorId(),
        "createdBy attribute of entity " + getClass().getName() + " must exists on insert");
    ArgumentAssertion.assertDefined(getLastUpdaterId(),
        "lastUpdateBy attribute of entity " + getClass().getName() + " must exists on insert");
    clearSystemData();
  }

  @Override
  protected void performBeforeUpdate() {
    OperationContext.getFromCache()
        .getPersistenceOperation(JpaUpdateOperation.class)
        .applyTechnicalDataTo(this);
    ArgumentAssertion.assertDefined(getLastUpdaterId(),
        "lastUpdatedBy attribute of entity " + getClass().getName() + " must exists on update");
    clearSystemData();
  }

  @Override
  protected void performBeforeRemove() {
  }

  private void clearSystemData() {
    OperationContext.getFromCache()
        .getPersistenceOperation(JpaPersistOperation.class)
        .clear(this);
    OperationContext.getFromCache()
        .getPersistenceOperation(JpaUpdateOperation.class)
        .clear(this);
  }
}
