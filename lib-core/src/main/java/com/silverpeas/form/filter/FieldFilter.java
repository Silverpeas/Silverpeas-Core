package com.silverpeas.form.filter;

import com.silverpeas.form.*;

/**
 * FieldFilter
 * 
 * @see Field
 * @see RecordFilter
 */
public interface FieldFilter {
  /**
   * Returns true if the given field match this Filter criteria.
   */
  public boolean match(Field testedField) throws FormException;
}
