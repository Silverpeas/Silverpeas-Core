package com.silverpeas.form.filter;

import com.silverpeas.form.Field;

/**
 * A GreaterThenFilter test if a given field is greater then a reference field.
 * 
 * @see Field
 * @see FieldDisplayer
 */
public class GreaterThenFilter implements FieldFilter {
  /**
   * An GreaterThen Filter is built upon a reference field
   */
  public GreaterThenFilter(Field reference) {
    this.reference = reference;
  }

  /**
   * Returns true if the given field is greater then the reference.
   */
  public boolean match(Field tested) {
    if (reference.isNull())
      return true;
    else
      return tested.compareTo(reference) >= 0;
  }

  /**
   * The reference field against which tests will be performed.
   */
  private final Field reference;
}
