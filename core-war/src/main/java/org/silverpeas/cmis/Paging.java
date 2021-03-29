/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.cmis;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Paging parameters to apply to the collection of items to return.
 * @author mmoquillon
 */
public class Paging {

  /**
   * Default value for no paging.
   */
  public static final Paging NO_PAGING = new Paging(null, null);

  private final long maxItems;
  private final long skipCount;

  /**
   * Constructs a new {@link Paging} instance with the specified paging parameters.
   * @param skipCount the number of items to skip in the collection to return.
   * @param maxItems the number of items to return in the collection.
   */
  public Paging(final BigInteger skipCount, final BigInteger maxItems) {
    if (maxItems == null) {
      this.maxItems = Integer.MAX_VALUE;
    } else {
      final long max = maxItems.longValue();
      this.maxItems = max < 0 ? Integer.MAX_VALUE : max;
    }
    if (skipCount == null) {
      this.skipCount = 0;
    } else {
      this.skipCount = Math.max(skipCount.longValue(), 0);
    }
  }

  /**
   * Gets the maximum number of items to return.
   * @return the maximum items count in the collection to return.
   */
  public BigInteger getMaxItems() {
    return BigInteger.valueOf(maxItems);
  }

  /**
   * Gets the number of items to skip before returning the items.
   * @return the items count to skip in the collection to return.
   */
  public BigInteger getSkipCount() {
    return BigInteger.valueOf(skipCount);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Paging)) {
      return false;
    }
    final Paging paging = (Paging) o;
    return Objects.equals(maxItems, paging.maxItems) && Objects.equals(skipCount, paging.skipCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(maxItems, skipCount);
  }
}
  