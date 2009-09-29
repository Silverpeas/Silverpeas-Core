package com.silverpeas.form.filter;

import com.silverpeas.form.Field;

/**
 * A EqualityFilter test equality of the given field to a reference field.
 * 
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
    if (reference.isNull())
      return tested.isNull();
    else
      return tested.equals(reference);
  }

  /**
   * The reference field against which equality is tested.
   */
  private final Field reference;
}
