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
package org.silverpeas.core.util.time;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * A key for the conversion board.
 */
public class TimeConversionBoardKey {
  private final TimeUnit smallestUnit;
  private final TimeUnit largestUnit;

  TimeConversionBoardKey(TimeUnit from, TimeUnit to) {
    if (from.ordinal() < to.ordinal()) {
      smallestUnit = from;
      largestUnit = to;
    } else {
      smallestUnit = to;
      largestUnit = from;
    }
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(smallestUnit.name()).append(largestUnit.name())
        .toHashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TimeConversionBoardKey other = (TimeConversionBoardKey) obj;
    EqualsBuilder matcher = new EqualsBuilder();
    matcher.append(smallestUnit, other.smallestUnit);
    matcher.append(largestUnit, other.largestUnit);
    return matcher.isEquals();
  }
}
