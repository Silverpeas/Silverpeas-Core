package com.silverpeas.form.filter;

import com.silverpeas.form.*;

/**
 * RecordFilter
 * 
 * @see DataRecord
 * @see FieldFilter
 */
public interface RecordFilter {
  /**
   * Returns true if the given record match this Filter criteria.
   */
  public boolean match(DataRecord testedRecord) throws FormException;
}
