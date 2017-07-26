/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
import org.silverpeas.core.persistence.datasource.model.Entity;
import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;
import org.silverpeas.core.persistence.datasource.repository.OperationContext;
import org.silverpeas.core.util.ArgumentAssertion;
import org.silverpeas.core.util.StringUtil;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.sql.Timestamp;
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
  private User createdByUser;

  @Transient
  private User lastUpdatedByUser;

  @Column(name = "createdBy", nullable = false, insertable = true, updatable = false, length = 40)
  private String createdBy;
  @Transient
  private boolean createdBySetManually = false;

  @Column(name = "createDate", nullable = false, insertable = true, updatable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date createDate;

  @Column(name = "lastUpdatedBy", nullable = false, length = 40)
  private String lastUpdatedBy;
  @Transient
  private boolean lastUpdatedBySetManually = false;

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
    if (StringUtil.isDefined(getCreatedBy())) {
      if (createdByUser == null || !getCreatedBy().equals(createdByUser.getId())) {
        createdByUser = User.getById(getCreatedBy());
      }
    } else {
      createdByUser = null;
    }
    return createdByUser;
  }

  @Override
  public E setCreator(final User creator) {
    createdByUser = creator;
    return createdBy((createdByUser != null) ? createdByUser.getId() : null);
  }

  @Override
  public final User getLastUpdater() {
    if (StringUtil.isDefined(getLastUpdatedBy())) {
      if (lastUpdatedByUser == null || !getLastUpdatedBy().equals(lastUpdatedByUser.getId())) {
        lastUpdatedByUser = User.getById(getLastUpdatedBy());
      }
    } else {
      lastUpdatedByUser = getCreator();
    }
    return lastUpdatedByUser;
  }

  @Override
  public E setLastUpdater(final User updater) {
    lastUpdatedByUser = updater;
    return setLastUpdatedBy((lastUpdatedByUser != null) ? lastUpdatedByUser.getId() : null);
  }

  @Override
  public String getCreatedBy() {
    return createdBy;
  }

  @SuppressWarnings("unchecked")
  public final E createdBy(final String createdBy) {
    this.createdBySetManually =
        this.createdBySetManually || !isPersisted() || this.createdBy == null;
    if (this.createdBySetManually) {
      this.lastUpdatedBySetManually = false;
    }
    this.createdBy = createdBy;
    return (E) this;
  }

  @SuppressWarnings("unchecked")
  public final E createdBy(final User creator) {
    setCreator(creator);
    return (E) this;
  }

  @Override
  public Date getCreateDate() {
    return createDate;
  }

  @SuppressWarnings("unchecked")
  protected final E setCreateDate(final Date createDate) {
    this.createDate = createDate;
    return (E) this;
  }

  @Override
  public Date getLastUpdateDate() {
    return lastUpdateDate;
  }

  @SuppressWarnings("unchecked")
  protected final E setLastUpdateDate(final Date lastUpdateDate) {
    this.lastUpdateDate = lastUpdateDate;
    return (E) this;
  }

  @Override
  public String getLastUpdatedBy() {
    return lastUpdatedBy;
  }

  @SuppressWarnings("unchecked")
  public final E setLastUpdatedBy(final String lastUpdatedBy) {
    this.lastUpdatedBySetManually =
        isPersisted() && this.createdBy != null && !this.createdBySetManually;
    this.lastUpdatedBy = lastUpdatedBy;
    return (E) this;
  }

  @SuppressWarnings("unchecked")
  public final E setLastUpdatedBy(final User lastUpdater) {
    setLastUpdater(lastUpdater);
    return (E) this;
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
      clone.setCreateDate(null);
      clone.setLastUpdater(null);
      clone.setLastUpdateDate(null);
      clone.setVersion(0L);
      clone.clearSystemData();
    }
    return (E) clone;
  }

  @Override
  protected void performBeforePersist() {
    OperationContext.getFromCache().applyToPersistOperation(this);
    ArgumentAssertion.assertDefined(getCreatedBy(),
        "createdBy attribute of entity " + getClass().getName() + " must exists on insert");
    ArgumentAssertion.assertDefined(getLastUpdatedBy(),
        "lastUpdateBy attribute of entity " + getClass().getName() + " must exists on insert");
    Timestamp timestamp = new Timestamp((new Date()).getTime());
    setCreateDate(timestamp);
    setLastUpdateDate(timestamp);
    clearSystemData();
  }

  @Override
  protected void performBeforeUpdate() {
    OperationContext.getFromCache().applyToUpdateOperation(this);
    ArgumentAssertion.assertDefined(getLastUpdatedBy(),
        "lastUpdatedBy attribute of entity " + getClass().getName() + " must exists on update");
    setLastUpdateDate(new Timestamp((new Date()).getTime()));
    clearSystemData();
  }

  @Override
  protected void performBeforeRemove() {
  }

  private void clearSystemData() {
    createdBySetManually = false;
    lastUpdatedBySetManually = false;
  }
}
