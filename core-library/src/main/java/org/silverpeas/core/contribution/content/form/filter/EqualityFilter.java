/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package org.silverpeas.core.contribution.content.form.filter;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;

/**
 * A EqualityFilter test equality of the given field to a reference field.
 * @see Field
 * @see FieldDisplayer
 */
public class EqualityFilter implements FieldFilter {
  /**
   * An Equality Filter is built upon a reference field
   */
  public EqualityFilter(Field reference) {
    this.reference = reference;
  }

  /**
   * Returns true if the given field equals the reference.
   */
  public boolean match(Field tested) {
    if (reference.isNull()) {
      return tested.isNull();
    } else {
      return tested.equals(reference);
    }
  }

  /**
   * The reference field against which equality is tested.
   */
  private final Field reference;
}
