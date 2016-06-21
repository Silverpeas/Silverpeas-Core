package org.silverpeas.core.persistence.datasource.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author ebonnet
 */
public abstract class AbstractCustomEntity<ENTITY extends IdentifiableEntity, IDENTIFIER_TYPE>
    implements Cloneable, IdentifiableEntity {

  /**
   * Sets the id of the entity.
   * @param id the new id of the entity.
   * @return the entity instance.
   */
  protected abstract ENTITY setId(String id);

  @Override
  public boolean isPersisted() {
    return getId() != null;
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
    AbstractCustomEntity entity;
    try {
      entity = (AbstractCustomEntity) super.clone();
      entity.setId(null);
    } catch (final CloneNotSupportedException e) {
      entity = null;
    }
    return (ENTITY) entity;
  }

  /**
   * This method contains all (technical) informations that must be performed on a entity create.
   */
  protected void performBeforePersist() {
  }

  /**
   * This method contains all (technical) informations that must be performed on a entity update.
   */
  protected void performBeforeUpdate() {
  }
}
