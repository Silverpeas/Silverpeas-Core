package org.silverpeas.persistence.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author: ebonnet
 */
public abstract class AbstractIdentifiableEntity<ENTITY extends IdentifiableEntity<ENTITY,
    IDENTIFIER_TYPE>, IDENTIFIER_TYPE>
    implements Cloneable, IdentifiableEntity<ENTITY, IDENTIFIER_TYPE> {

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
  public final int hashCode() {
    HashCodeBuilder hash = new HashCodeBuilder();
    hash.append(getId() != null ? getId() : super.hashCode());
    return hash.toHashCode();
  }

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

}
