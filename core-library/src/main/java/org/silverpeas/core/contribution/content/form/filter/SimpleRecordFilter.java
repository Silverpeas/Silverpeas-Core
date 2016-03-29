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

import java.util.ArrayList;
import java.util.List;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FormException;

/**
 * A simple record filter built from a list of fieldFilter whose criteria must all match to accept a
 * DataRecord.
 */
public class SimpleRecordFilter implements RecordFilter {
  /**
   * Builds a SimpleRecordFilter. Before any call to the match method, you must use the
   * addFieldFilter method to add each needed field filter.
   */
  public SimpleRecordFilter() {
  }

  /**
   * Add a new filter on the specified field.
   */
  public void addFieldFilter(String fieldName, FieldFilter fieldFilter) {
    filters.add(new FieldAssignedFilter(fieldName, fieldFilter));
  }

  /**
   * Returns true if the fields of the specified record match all the field filter of this Filter.
   */
  public boolean match(DataRecord testedRecord) throws FormException {
    for (FieldAssignedFilter filter : filters) {
      String fieldName = filter.fieldName;
      FieldFilter fieldFilter = filter.fieldFilter;

      if (!fieldFilter.match(testedRecord.getField(fieldName))) {
        return false;
      }
    }

    return true;
  }

  /**
   * The list of (fieldName, filter) which must all match.
   */
  private final List<FieldAssignedFilter> filters = new ArrayList<>();
}

final class FieldAssignedFilter {
  final String fieldName;
  final FieldFilter fieldFilter;

  public FieldAssignedFilter(String fieldName, FieldFilter fieldFilter) {
    this.fieldName = fieldName;
    this.fieldFilter = fieldFilter;
  }
}
