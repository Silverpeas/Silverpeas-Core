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
package org.silverpeas.persistence.model.identifier;

import com.stratelia.webactiv.util.DBUtil;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.silverpeas.persistence.model.EntityIdentifier;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * User: Yohann Chastagnier
 * Date: 25/11/13
 */
@Embeddable
public class UniqueLongIdentifier implements EntityIdentifier {
  private static final long serialVersionUID = 8570844400186460258L;

  @Column(name = "id", nullable = false)
  private Long id;

  public Long getId() {
    return id;
  }

  public UniqueLongIdentifier setId(final Long id) {
    this.id = id;
    return this;
  }

  @Override
  public String asString() {
    return getId().toString();
  }

  @Override
  public UniqueLongIdentifier fromString(final String id) {
    return setId(Long.valueOf(id));
  }

  @Override
  public UniqueLongIdentifier generateNewId(final String tableName,
      final String tableColumnIdName) {
    this.id = (long) DBUtil.getNextId(tableName, tableColumnIdName);
    return this;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getId()).toHashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final UniqueLongIdentifier other = (UniqueLongIdentifier) obj;
    return new EqualsBuilder().append(getId(), other.getId()).isEquals();
  }
}
