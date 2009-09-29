/**
 * @author ludovic Bertin
 * @version 1.0
 */

/**
 * The SpecificLabelList class inherits from Hashtable
 */

package com.silverpeas.workflow.engine.model;

public class SpecificLabelList extends java.util.Hashtable {
  /**
   * Constructor
   */
  public SpecificLabelList() {
    super();
  }

  /*
   * Get label in specific language for the given role
   * 
   * @param role role for which the label is
   * 
   * @param lang label's language
   * 
   * @return wanted label as a String object. If label is not found, search
   * label with given role and default language, if not found again, return the
   * default label in given language, if not found again, return the default
   * label in default language, if not found again, return empty string.
   */
  public String getLabel(String role, String lang) {
    // 1st search
    SpecificLabel search = new SpecificLabel(role, lang);
    SpecificLabel result = (SpecificLabel) get(search);
    if (result != null)
      return result.getContent();

    // 1st search failed ==> search with default language
    search.setLanguage("default");
    search.setRole(role);
    result = (SpecificLabel) get(search);
    if (result != null)
      return result.getContent();

    // 2nd search failed ==> search for default label in given language
    search.setLanguage(lang);
    search.setRole("default");
    result = (SpecificLabel) get(search);
    if (result != null)
      return result.getContent();

    // 3nd search failed --> search for default label in default language
    search.setLanguage("default");
    search.setRole("default");
    result = (SpecificLabel) get(search);
    if (result != null)
      return result.getContent();

    // No default label found, return empty string
    return "";
  }
}