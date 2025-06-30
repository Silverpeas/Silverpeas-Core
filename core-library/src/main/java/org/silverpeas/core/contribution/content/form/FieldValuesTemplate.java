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

package org.silverpeas.core.contribution.content.form;

import java.util.*;
import java.util.function.Consumer;

/**
 * Template of the values of a given field in a form.
 *
 * @author mmoquillon
 */
public class FieldValuesTemplate {

  private final List<FieldValue> values = new ArrayList<>(10);
  private final String language;

  public FieldValuesTemplate(String language) {
    this.language = language;
  }

  @SuppressWarnings("UnusedReturnValue")
  public FieldValuesTemplate withAsValue(String key, String label) {
    String k = key.isBlank() && !label.isBlank() ? label : key;
    String l = label.isBlank() && !key.isBlank() ? key : label;
    if (!k.isBlank() && !l.isBlank()) {
      values.add(new FieldValue(k, l, language));
    }
    return this;
  }

  public void apply(Consumer<FieldValue> consumer) {
    values.forEach(consumer);
  }

  public Optional<FieldValue> get(String key) {
    return values.stream()
        .filter(value -> value.getKey().equals(key))
        .findFirst();
  }

  public boolean isEmpty() {
    return values.isEmpty();
  }

  public int size() {
    return values.size();
  }
}
  