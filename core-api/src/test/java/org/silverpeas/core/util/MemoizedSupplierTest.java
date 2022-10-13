/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author silveryocha
 */
class MemoizedSupplierTest {

  private int count;

  @BeforeEach
  void reset() {
    count = 0;
  }

  @Test
  void shouldBeMemoized() {
    final Supplier<Integer> supplier = () -> count++ == 1 ? count : count;
    final Supplier<Integer> memoizedSupplier = new MemoizedSupplier<>(supplier);
    assertThat(memoizedSupplier.get(), is(1));
    assertThat(supplier.get(), is(2));
    assertThat(supplier.get(), is(3));
    assertThat(memoizedSupplier.get(), is(1));
  }

  @Test
  void nullShouldAlsoBeMemoized() {
    final Supplier<Integer> supplier = () -> count++ == 0 ? null : count;
    final Supplier<Integer> memoizedSupplier = new MemoizedSupplier<>(supplier);
    assertThat(memoizedSupplier.get(), nullValue());
    assertThat(supplier.get(), is(2));
    assertThat(supplier.get(), is(3));
    assertThat(memoizedSupplier.get(), nullValue());
  }
}