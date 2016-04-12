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
package org.silverpeas.core.persistence.datasource.model.identifier;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ebonnet
 */
@Embeddable
public class UniqueIntegerIdentifier implements EntityIdentifier {
  private static final long serialVersionUID = 8570844400186460258L;

  @Column(name = "id", nullable = false)
  private Integer id;

  public static UniqueIntegerIdentifier from(String value) {
    return new UniqueIntegerIdentifier().fromString(value);
  }

  public static List<UniqueIntegerIdentifier> fromStrings(Collection<String> values) {
    return values.stream().map(UniqueIntegerIdentifier::from).collect(Collectors.toList());
  }

  public static UniqueIntegerIdentifier from(int value) {
    return new UniqueIntegerIdentifier().setId(value);
  }

  public static List<UniqueIntegerIdentifier> fromIntegers(Collection<Integer> values) {
    return values.stream().map(UniqueIntegerIdentifier::from).collect(Collectors.toList());
  }

  public Integer getId() {
    return id;
  }

  private UniqueIntegerIdentifier setId(final Integer id) {
    this.id = id;
    return this;
  }

  @Override
  public String asString() {
    return getId().toString();
  }

  @Override
  public UniqueIntegerIdentifier fromString(final String id) {
    return setId(Integer.valueOf(id));
  }

  @Override
  public UniqueIntegerIdentifier generateNewId(final String tableName,
      final String tableColumnIdName) {
    try {
      this.id = DBUtil.getNextId(tableName, tableColumnIdName);
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
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
    final UniqueIntegerIdentifier other = (UniqueIntegerIdentifier) obj;
    return new EqualsBuilder().append(getId(), other.getId()).isEquals();
  }

  @Override
  public String toString() {
    return asString();
  }
}
