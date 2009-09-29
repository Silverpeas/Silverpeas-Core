package com.stratelia.webactiv.util.indexEngine.model;

import java.io.Serializable;

/**
 * A XMLFieldDescription pack all the needed information to parse and index a
 * xml field.
 * 
 * We need :
 * <UL>
 * <LI>the name of the field</LI>
 * <LI>its content</LI>
 * <LI>its language</LI>
 * </UL>
 */
public final class XMLFieldDescription extends TextDescription implements
    Serializable {
  public XMLFieldDescription(String fieldName, String content, String lang) {
    super(content, lang);
    this.fieldName = fieldName;
  }

  /**
   * Return the fieldName
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * All the attributes are private and final.
   */
  private final String fieldName;
}
