/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.util;

import java.util.function.Supplier;

/**
 * Represents a memoized supplier of a result.
 *
 * <p>So same result is returned each time the supplier is invoked.
 *
 * <p>This is kind of wrapper of a
 * <a href="package-summary.html">functional interface implementation</a>
 * whose functional method is {@link #get()}.
 * @param <T> the type of results supplied by this supplier
 * @author silveryocha
 */
public class MemoizedSupplier<T> implements Supplier<T> {
  private final Supplier<T> supplier;
  private boolean memoized = false;
  private T value;

  public MemoizedSupplier(final Supplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  public T get() {
    if (!memoized) {
      value = supplier.get();
      memoized = true;
    }
    return value;
  }

  public void clear() {
    memoized = false;
    value = null;
  }
}
