package com.silverpeas.workflow.engine.model;

import java.io.Serializable;

import com.silverpeas.workflow.api.model.ContextualDesignation;
import com.silverpeas.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the following elements of a Process
 * Model:
 * <ul>
 * <li>&lt;activity&gt;</li>
 * <li>&lt;description&gt;</li>
 * <li>&lt;label&gt;</li>
 * <li>&lt;title&gt;</li>
 * </ul>
 **/
public class SpecificLabel extends AbstractReferrableObject implements
    Serializable, ContextualDesignation {

  private String content = "";
  private String language = "default";
  private String role = "default";

  /**
   * Constructor
   */
  public SpecificLabel() {
    super();
  }

  /**
   * Constructor
   */
  public SpecificLabel(String role, String lang) {
    this.language = lang;
    this.role = role;
  }

  /**
   * Get the content of specific label
   */
  public String getContent() {
    return this.content;
  }

  /**
   * Get the language of specific label
   */
  public String getLanguage() {
    return this.language;
  }

  /**
   * Get the role for which this specific label is
   */
  public String getRole() {
    return this.role;
  }

  /**
   * Set the content of specific label
   * 
   * @param content
   *          content of specific label
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Set the language of specific label
   * 
   * @param language
   *          language of specific label
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Set the role for which this specific label is
   * 
   * @param role
   *          role
   */
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * Get the unique key, used by equals method
   * 
   * @return unique key
   */
  public String getKey() {
    return (this.role + "|" + this.language);
  }
}