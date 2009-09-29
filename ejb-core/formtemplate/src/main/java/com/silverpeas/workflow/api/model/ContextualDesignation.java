package com.silverpeas.workflow.api.model;

/**
 * Interface describing a representation of one of the following elements of a
 * Process Model:
 * <ul>
 * <li>&lt;activity&gt;</li>
 * <li>&lt;description&gt;</li>
 * <li>&lt;label&gt;</li>
 * <li>&lt;title&gt;</li>
 * </ul>
 */
public interface ContextualDesignation {

  /**
   * Get the content of the designation
   * 
   * @return a string value
   */
  public String getContent();

  /**
   * Set the content of the designation
   * 
   * @param strContent
   *          new value
   */
  public void setContent(String strContent);

  /**
   * Get the role name for this designation
   */
  public String getRole();

  /**
   * Set the role name for this designation
   */
  public void setRole(String strRole);

  /**
   * Get the language of this designation
   */
  public String getLanguage();

  /**
   * Set the language of this designation
   */
  public void setLanguage(String strLang);
}
