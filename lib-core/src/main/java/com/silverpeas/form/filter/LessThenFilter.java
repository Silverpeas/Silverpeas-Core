package com.silverpeas.form.filter;

import com.silverpeas.form.Field;

/**
 * A LessThenFilter test if a given field is less then a reference field.
 * 
 * @see Field
 * @see FieldDisplayer
 */
public class LessThenFilter implements FieldFilter {
  /**
   * An LessThen Filter is built upon a reference field
   */
  public LessThenFilter(Field reference) {
    this.reference = reference;
  }

  /**
   * Returns true if the given field is less then the reference.
   */
  public boolean match(Field tested) {
    if (reference.isNull())
      return true;
    else
      return tested.compareTo(reference) <= 0;
  }

  /**
   * The reference field against which tests will be performed.
   */
  private final Field reference;
}
