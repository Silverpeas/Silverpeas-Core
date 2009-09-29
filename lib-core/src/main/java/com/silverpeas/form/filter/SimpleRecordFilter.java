package com.silverpeas.form.filter;

import java.util.*;
import com.silverpeas.form.*;

/**
 * A simple record filter built from a list of fieldFilter whose criteria must
 * all match to accept a DataRecord.
 */
public class SimpleRecordFilter implements RecordFilter {
  /**
   * Builds a SimpleRecordFilter.
   * 
   * Before any call to the match method, you must use the addFieldFilter method
   * to add each needed field filter.
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
   * Returns true if the fields of the specified record match all the field
   * filter of this Filter.
   */
  public boolean match(DataRecord testedRecord) throws FormException {
    Iterator filters = this.filters.iterator();
    FieldAssignedFilter filter;
    String fieldName;
    FieldFilter fieldFilter;

    while (filters.hasNext()) {
      filter = (FieldAssignedFilter) filters.next();
      fieldName = filter.fieldName;
      fieldFilter = filter.fieldFilter;

      if (!fieldFilter.match(testedRecord.getField(fieldName))) {
        return false;
      }
    }

    return true;
  }

  /**
   * The list of (fieldName, filter) which must all match.
   */
  private final ArrayList filters = new ArrayList();
}

final class FieldAssignedFilter {
  final String fieldName;
  final FieldFilter fieldFilter;

  public FieldAssignedFilter(String fieldName, FieldFilter fieldFilter) {
    this.fieldName = fieldName;
    this.fieldFilter = fieldFilter;
  }
}
