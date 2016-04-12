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
package org.silverpeas.core.persistence.datasource.model.jpa;

import org.silverpeas.core.persistence.datasource.model.AbstractCustomEntity;
import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;
import org.silverpeas.core.persistence.datasource.model.ExternalEntityIdentifier;
import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.persistence.EmbeddedId;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.ParameterizedType;

import static org.silverpeas.core.util.annotation.AnnotationUtil.searchClassThatDeclaresAnnotation;

/**
 * This abstract class must be extended by all Basic JPA entity definitions.
 * All technical data, excepted the identifier, are handled at this level.
 * <p>
 * The {@link org.silverpeas.core.persistence.datasource.model.AbstractEntity#performBeforePersist()} and {@link
 * org.silverpeas.core.persistence.datasource.model.AbstractEntity#performBeforeUpdate()}
 * method calls are handled at this level for JPA.
 * <p>
 * Please be careful into the child entity classes about the use of @PrePersist and @PreUpdate
 * annotations. In most of cases you don't need to use them, but to override {@link
 * org.silverpeas.core.persistence.datasource.model.AbstractEntity#performBeforePersist} or {@link
 * org.silverpeas.core.persistence.datasource.model.AbstractEntity#performBeforeUpdate} methods
 * without forgetting to play the super call.
 * <p>
 * @param <ENTITY> specify the class name of the entity itself which is handled by a repository
 * manager.
 * @param <IDENTIFIER_TYPE> the identifier class name used by {@link ENTITY} for its primary key
 * definition.
 * @author ebonnet
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AbstractJpaCustomEntity<ENTITY extends IdentifiableEntity,
    IDENTIFIER_TYPE extends EntityIdentifier>
    extends AbstractCustomEntity<ENTITY, IDENTIFIER_TYPE> {

  private static final long serialVersionUID = 3955905287437500278L;

  @Transient
  private String tableName;

  @Transient
  private Class<IDENTIFIER_TYPE> entityIdentifierClass;

  @EmbeddedId
  private IDENTIFIER_TYPE id;

  protected IDENTIFIER_TYPE getNativeId() {
    return id;
  }

  /**
   * Gets the identifier class of the entity managed by the repository.
   * @return
   */
  protected Class<IDENTIFIER_TYPE> getEntityIdentifierClass() {
    initializeEntityClasses();
    return entityIdentifierClass;
  }

  /**
   * Gets the identifier class of the entity.
   * @return
   */
  @SuppressWarnings("unchecked")
  private void initializeEntityClasses() {
    if (entityIdentifierClass == null) {
      try {
        Class<?> classThatDeclaresTable =
            searchClassThatDeclaresAnnotation(Table.class, this.getClass());
        entityIdentifierClass =
            ((Class<IDENTIFIER_TYPE>) ((ParameterizedType) classThatDeclaresTable.
                getGenericSuperclass()).getActualTypeArguments()[1]);

        tableName = classThatDeclaresTable.getAnnotation(Table.class).name();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public boolean isPersisted() {
    return StringUtil.isDefined(getId());
  }

  @Override
  public String getId() {
    return id == null ? null : id.asString();
  }

  protected IDENTIFIER_TYPE newIdentifierInstance() {
    try {
      return getEntityIdentifierClass().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public ENTITY setId(final String id) {
    if (StringUtil.isDefined(id)) {
      try {
        this.id = newIdentifierInstance();
        this.id.fromString(id);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      this.id = null;
    }
    return (ENTITY) this;
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
      this.id = (IDENTIFIER_TYPE) newIdentifierInstance().generateNewId(tableName, "id");
    }
    performBeforePersist();
  }

  @PreUpdate
  private void beforeUpdate() {
    performBeforeUpdate();
  }
}
