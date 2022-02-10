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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.cache.model;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * User: Yohann Chastagnier
 * Date: 25/10/13
 */
public abstract class AbstractSimpleCache implements SimpleCache {

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(final Object key, final Class<T> classType) {
    Object value = get(key);
    if (value == null || !classType.isAssignableFrom(value.getClass())) {
      return null;
    }
    return (T) value;
  }

  @Override
  public <T> T computeIfAbsent(final Object key, final Class<T> classType,
      final Supplier<T> valueSupplier) {
    Objects.requireNonNull(valueSupplier);
    T value = get(key, classType);
    if (value == null) {
      value = valueSupplier.get();
      put(key, value);
    }
    return value;
  }

  @Override
  public String add(final Object value) {
    String uniqueKey = UUID.randomUUID().toString();
    put(uniqueKey, value);
    return uniqueKey;
  }
}
