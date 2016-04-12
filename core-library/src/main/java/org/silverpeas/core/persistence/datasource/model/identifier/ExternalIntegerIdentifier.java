/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.datasource.model.identifier;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.persistence.datasource.model.ExternalEntityIdentifier;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This external entity identifier implementation handles external integer identifier.
 * @author ebonnet
 */
@Embeddable
public class ExternalIntegerIdentifier implements ExternalEntityIdentifier {

  @Column(name = "id")
  private Integer id;

  public static ExternalIntegerIdentifier from(String value) {
    return new ExternalIntegerIdentifier().fromString(value);
  }

  public static List<ExternalIntegerIdentifier> fromStrings(Collection<String> values) {
    return values.stream().map(ExternalIntegerIdentifier::from).collect(Collectors.toList());
  }

  @Override
  public ExternalIntegerIdentifier fromString(final String id) {
    return setId(Integer.valueOf(id));
  }

  @Override
  public String asString() {
    return getId().toString();
  }

  public Integer getId() {
    return id;
  }

  public ExternalIntegerIdentifier setId(final Integer id) {
    this.id = id;
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
    final ExternalStringIdentifier other = (ExternalStringIdentifier) obj;
    return new EqualsBuilder().append(getId(), other.getId()).isEquals();
  }

  @Override
  public String toString() {
    return asString();
  }

}
