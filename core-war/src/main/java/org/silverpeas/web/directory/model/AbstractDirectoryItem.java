/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.directory.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.util.StringUtil;

public abstract class AbstractDirectoryItem implements DirectoryItem {

  @Override
  public int compareTo(DirectoryItem other) {
    CompareToBuilder compareToBuilder = new CompareToBuilder();
    compareToBuilder.append(getLastName() != null ? getLastName().toLowerCase() : "",
        other.getLastName() != null ? other.getLastName().toLowerCase() : "");
    compareToBuilder.append(getFirstName() != null ? getFirstName().toLowerCase() : "",
        other.getFirstName() != null ? other.getFirstName().toLowerCase() : "");
    compareToBuilder.append(getType(), other.getType());
    compareToBuilder.append(other.getOriginalId(), getOriginalId());
    return compareToBuilder.toComparison();
  }

  @Override
  public String getUniqueId() {
    return getType().toString() + getOriginalId();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (super.equals(obj)) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DirectoryItem other = (DirectoryItem) obj;
    if (StringUtil.isDefined(getUniqueId()) && StringUtil.isDefined(other.getUniqueId())) {
      return new EqualsBuilder().append(getUniqueId(), other.getUniqueId()).isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getUniqueId()).build();
  }
}
