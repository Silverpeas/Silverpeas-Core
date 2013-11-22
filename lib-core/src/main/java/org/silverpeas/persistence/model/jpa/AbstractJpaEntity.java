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
package org.silverpeas.persistence.model.jpa;

import com.silverpeas.util.StringUtil;
import org.silverpeas.persistence.model.AbstractEntity;
import org.silverpeas.persistence.model.Entity;
import org.silverpeas.persistence.model.EntityIdentifier;

import javax.persistence.*;
import java.lang.reflect.ParameterizedType;
import java.util.Date;

/**
 * This abstract class must be extended by all Silverpeas JPA entity definitions.
 * All technical data, excepted the identifier, are handled at this level.
 * <p/>
 * The {@link AbstractEntity#performBeforePersist()} and {@link
 * AbstractEntity#performBeforeUpdate()}
 * method calls are handled at this level for JPA.
 * <p/>
 * Please be careful into the child entity classes about the use of @PrePersist and @PreUpdate
 * annotations. In most of cases you don't need to use them, but to override {@link
 * AbstractEntity#performBeforePersist} or {@link AbstractEntity#performBeforeUpdate} methods
 * without forgetting to play the super call.
 * <p/>
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AbstractJpaEntity<ENTITY extends Entity<ENTITY, IDENTIFIER_TYPE>,
    IDENTIFIER_TYPE extends EntityIdentifier>
    extends AbstractEntity<ENTITY, IDENTIFIER_TYPE> {

  @Transient
  private String tableName;

  @Transient
  private Class<IDENTIFIER_TYPE> entityIdentifierClass;

  @EmbeddedId
  private IDENTIFIER_TYPE id;

  @Column(name = "createdBy", nullable = false, insertable = true, updatable = false, length = 40)
  private String createdBy;

  @Column(name = "createDate", nullable = false, insertable = true, updatable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date createDate;

  @Column(name = "lastUpdatedBy", insertable = false, updatable = true, length = 40)
  private String lastUpdatedBy;

  @Column(name = "lastUpdateDate", insertable = false, updatable = true)
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date lastUpdateDate;

  @Version
  @Column(name = "version", nullable = false)
  private Long version = 0L;

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
        entityIdentifierClass = ((Class<IDENTIFIER_TYPE>) ((ParameterizedType) this.getClass().
            getGenericSuperclass()).getActualTypeArguments()[1]);

        tableName = this.getClass().getAnnotation(Table.class).name();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public boolean isPersisted() {
    return super.isPersisted() && StringUtil.isDefined(getId());
  }

  @Override
  public String getId() {
    return id == null ? null : id.asString();
  }

  @SuppressWarnings("unchecked")
  protected IDENTIFIER_TYPE newIdentifierInstance() {
    try {
      return getEntityIdentifierClass().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected ENTITY setId(final String id) {
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
    this.id = (IDENTIFIER_TYPE) newIdentifierInstance().generateNewId(tableName, "id");
    performBeforePersist();
  }

  @PreUpdate
  private void beforeUpdate() {
    performBeforeUpdate();
  }

  @Override
  public String getCreatedBy() {
    return createdBy;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ENTITY setCreatedBy(final String createdBy) {
    this.createdBy = createdBy;
    return (ENTITY) this;
  }

  @Override
  public Date getCreateDate() {
    return createDate;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected ENTITY setCreateDate(final Date createDate) {
    this.createDate = createDate;
    return (ENTITY) this;
  }

  @Override
  public Date getLastUpdateDate() {
    return lastUpdateDate;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected ENTITY setLastUpdateDate(final Date lastUpdateDate) {
    this.lastUpdateDate = lastUpdateDate;
    return (ENTITY) this;
  }

  @Override
  public String getLastUpdatedBy() {
    return lastUpdatedBy;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ENTITY setLastUpdatedBy(final String lastUpdatedBy) {
    this.lastUpdatedBy = lastUpdatedBy;
    return (ENTITY) this;
  }

  @Override
  public Long getVersion() {
    return version;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected ENTITY setVersion(final Long version) {
    this.version = version;
    return (ENTITY) this;
  }
}
