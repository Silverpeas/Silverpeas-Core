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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.persistence.datasource.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.datasource.repository.OperationContext;
import org.silverpeas.core.util.ArgumentAssertion;
import org.silverpeas.core.util.StringUtil;

import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.Date;

/**
 * This abstract class must be extended by all Silverpeas entity definitions that have to be
 * persisted.
 * <p/>
 * The entity extensions must handle {@link #performBeforePersist()} and {@link
 * #performBeforeUpdate()} methods.
 * <p/>
 * @param <ENTITY> specify the class name of the entity itself which is handled by a repository
 * manager.
 * @param <IDENTIFIER_TYPE> the identifier class name used by {@link ENTITY} for its primary key
 * definition.
 * @author Yohann Chastagnier
 */
public abstract class AbstractEntity<ENTITY extends Entity<ENTITY, IDENTIFIER_TYPE>, IDENTIFIER_TYPE>
    implements Cloneable, Entity<ENTITY, IDENTIFIER_TYPE> {

  @Transient
  private User createdByUser;

  @Transient
  private User lastUpdatedByUser;

  /**
   * Sets the id of the entity.
   * @param id the new id of the entity.
   * @return the entity instance.
   */
  protected abstract ENTITY setId(String id);

  /**
   * By default, if not implemented by child classes, an exception is thrown.
   */
  @Override
  public String getComponentInstanceId() {
    throw new UnsupportedOperationException();
  }

  /**
   * Sets the version of the entity.
   * @param version
   * @return
   */
  protected abstract ENTITY setVersion(Long version);

  /**
   * This method contains all (technical) information that must be performed on a entity create.
   */
  protected void performBeforePersist() {
    OperationContext.getFromCache().applyToPersistOperation(this);
    ArgumentAssertion.assertDefined(getCreatedBy(),
        "createdBy attribute of entity " + getClass().getName() + " must exists on insert");
    ArgumentAssertion.assertDefined(getLastUpdatedBy(),
        "lastUpdateBy attribute of entity " + getClass().getName() + " must exists on insert");
    Timestamp timestamp = new Timestamp((new Date()).getTime());
    setCreateDate(timestamp);
    setLastUpdateDate(timestamp);
  }

  /**
   * This method contains all (technical) information that must be performed on a entity update.
   */
  protected void performBeforeUpdate() {
    OperationContext.getFromCache().applyToUpdateOperation(this);
    ArgumentAssertion.assertDefined(getLastUpdatedBy(),
        "lastUpdatedBy attribute of entity " + getClass().getName() + " must exists on update");
    setLastUpdateDate(new Timestamp((new Date()).getTime()));
  }

  public abstract ENTITY createdBy(String createdBy);

  public abstract ENTITY createdBy(User creator);

  protected abstract ENTITY setCreateDate(Date createDate);

  public abstract ENTITY setLastUpdatedBy(String lastUpdatedBy);

  protected abstract ENTITY setLastUpdateDate(Date lastUpdateDate);

  @Override
  public User getCreator() {
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
  public ENTITY setCreator(final User creator) {
    createdByUser = creator;
    return createdBy((createdByUser != null) ? createdByUser.getId() : null);
  }

  @Override
  public User getLastUpdater() {
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
  public ENTITY setLastUpdater(final User updater) {
    lastUpdatedByUser = updater;
    return setLastUpdatedBy((lastUpdatedByUser != null) ? lastUpdatedByUser.getId() : null);
  }

  @Override
  public boolean isPersisted() {
    return getId() != null;
  }

  @Override
  public boolean hasBeenModified() {
    return isPersisted() && getVersion() > 0;
  }

  @Override
  public final int hashCode() {
    HashCodeBuilder hash = new HashCodeBuilder();
    hash.append(getId() != null ? getId() : super.hashCode());
    return hash.toHashCode();
  }

  @SuppressWarnings("unchecked")
  @Override
  public final boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (super.equals(obj)) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ENTITY other = (ENTITY) obj;
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
  public ENTITY clone() {
    AbstractEntity entity;
    try {
      entity = (AbstractEntity) super.clone();
      entity.setId(null);
      entity.setCreator(null);
      entity.setCreateDate(null);
      entity.setLastUpdater(null);
      entity.setLastUpdatedBy(null);
      entity.setLastUpdateDate(null);
      entity.setVersion(0L);
    } catch (final CloneNotSupportedException e) {
      entity = null;
    }
    return (ENTITY) entity;
  }
}
