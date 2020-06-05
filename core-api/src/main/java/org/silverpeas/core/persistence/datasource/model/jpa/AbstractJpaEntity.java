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
package org.silverpeas.core.persistence.datasource.model.jpa;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;
import org.silverpeas.core.persistence.datasource.model.ExternalEntityIdentifier;
import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.persistence.AttributeOverride;
import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.silverpeas.core.util.annotation.ClassAnnotationUtil
    .searchClassThatDeclaresAnnotation;

/**
 * Abstract implementation of the {@link IdentifiableEntity} interface that uses the JPA API.
 * This implementation defines all the common methods that can be required by the more concrete
 * entities and that puts in place the JPA mechanical required for their persistence according
 * to the basic JPA related rules in the Silverpeas Persistence API such as the unique identifier
 * management.
 *
 * Please be careful with the child entity classes about the use of @PrePersist and @PreUpdate
 * annotations. In most of cases you don't need to use them, but to override {@link
 * AbstractJpaEntity#performBeforePersist} or {@link AbstractJpaEntity#performBeforeUpdate} methods.
 * @param <T> the class name of the represented entity.
 * @param <U> the unique identifier class used by the entity to identify it uniquely in the
 * persistence context.
 * @author mmoquillon
 */
@MappedSuperclass
public abstract class AbstractJpaEntity<T extends IdentifiableEntity, U extends EntityIdentifier>
    implements IdentifiableEntity, Cloneable {

  @EmbeddedId
  private U id;

  @Override
  public String getId() {
    return id == null ? null : id.asString();
  }

  @Override
  public boolean isPersisted() {
    if (this.id != null) {
      EntityManager entityManager = EntityManagerProvider.get().getEntityManager();
      return entityManager.find(getClass(), id) != null;
    }
    return false;
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
    final T other = (T) obj;
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
  public T clone() {
    AbstractJpaEntity entity;
    try {
      entity = (AbstractJpaEntity) super.clone();
      entity.setId(null);
    } catch (final CloneNotSupportedException e) {
      SilverLogger.getLogger(this).error(e);
      entity = null;
    }
    return (T) entity;
  }

  /**
   * Gets the native representation of the entity identifier.
   * @return the native representation of the unique identifier of this entity.
   */
  protected U getNativeId() {
    return id;
  }

  /**
   * Sets the specified unique identifier to this entity.
   * @param id the new unique identifier of the entity.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  protected T setId(final String id) {
    if (StringUtil.isDefined(id)) {
      try {
        this.id = newIdentifierInstance();
        this.id.fromString(id);
      } catch (Exception e) {
        throw new SilverpeasRuntimeException(e);
      }
    } else {
      this.id = null;
    }
    return (T) this;
  }

  /**
   * Performs some treatments before this entity is persisted into a repository.
   */
  protected abstract void performBeforePersist();

  /**
   * Performs some treatments before its counterpart in a repository is updated with the changes in
   * this entity.
   */
  protected abstract void performBeforeUpdate();

  /**
   * Performs some treatments before this entity is removed from a repository.
   */
  protected abstract void performBeforeRemove();

  private U newIdentifierInstance() {
    try {
      return getEntityIdentifierClass().newInstance();
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @PrePersist
  private void beforePersist() {
    boolean isExternalIdentifier =
        ExternalEntityIdentifier.class.isAssignableFrom(getEntityIdentifierClass());
    if (!isExternalIdentifier) {
      if (this.id != null && StringUtil.isDefined(this.id.asString())) {
        SilverLogger.getLogger(this)
            .warn("As the entity identifier is not a ForeignEntityIdentifier one, " +
                "identifier value should not exist on a persist operation... (ID=" + getId() +
                ")");
      }
      Class<?> classThatDeclaresTable =
          searchClassThatDeclaresAnnotation(Table.class, this.getClass());
      String tableName = classThatDeclaresTable.getAnnotation(Table.class).name();
      String primaryKey = "id";
      final AttributeOverride attributeOverride =
          classThatDeclaresTable.getAnnotation(AttributeOverride.class);
      if (attributeOverride != null && primaryKey.equals(attributeOverride.name())) {
        primaryKey = attributeOverride.column().name();
      }
      this.id = (U) newIdentifierInstance().generateNewId(tableName, primaryKey);
    }
    performBeforePersist();
  }

  @PreUpdate
  private void beforeUpdate() {
    performBeforeUpdate();
  }

  @PreRemove
  private void beforeRemove() {
    performBeforeRemove();
  }

  @SuppressWarnings("unchecked")
  private Class<U> getEntityIdentifierClass() {
    Type parent = this.getClass().getGenericSuperclass();
    while (!(parent instanceof ParameterizedType)) {
      parent = this.getClass().getSuperclass().getGenericSuperclass();
    }
    return (Class<U>) ((ParameterizedType) parent).getActualTypeArguments()[1];
  }
}
