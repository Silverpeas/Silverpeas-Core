/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.persistence.datasource.model.identifier;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base and common implementation of the entity identifiers.
 *
 * @author mmoquillon
 */
@MappedSuperclass
public abstract class BaseEntityIdentifier<T extends Serializable> implements EntityIdentifier {

  @Column(name = "id", nullable = false)
  private T id;

  public T getId() {
    return id;
  }

  public void setId(T id) {
    this.id = id;
  }

  @Override
  public boolean isNull() {
    return this.id == null;
  }

  @Override
  public String asString() {
    return isNull() ? null : getId().toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BaseEntityIdentifier)) return false;
    BaseEntityIdentifier<?> that = (BaseEntityIdentifier<?>) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return asString();
  }
}
  