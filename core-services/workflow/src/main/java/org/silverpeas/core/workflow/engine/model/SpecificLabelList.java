/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author ludovic Bertin
 * @version 1.0
 */

/**
 * The SpecificLabelList class inherits from Hashtable
 */

package org.silverpeas.core.workflow.engine.model;

public class SpecificLabelList extends java.util.Hashtable {
  private static final long serialVersionUID = 3458090091785243469L;

  /**
   * Constructor
   */
  public SpecificLabelList() {
    super();
  }

  /*
   * Get label in specific language for the given role
   * @param role role for which the label is
   * @param lang label's language
   * @return wanted label as a String object. If label is not found, search label with given role
   * and default language, if not found again, return the default label in given language, if not
   * found again, return the default label in default language, if not found again, return empty
   * string.
   */
  public String getLabel(String role, String lang) {
    // 1st search
    SpecificLabel search = new SpecificLabel(role, lang);
    SpecificLabel result = (SpecificLabel) get(search);
    if (result != null) {
      return result.getContent();
    }
    // 1st search failed ==> search with default language
    search.setLanguage("default");
    search.setRole(role);
    result = (SpecificLabel) get(search);
    if (result != null) {
      return result.getContent();
    }
    // 2nd search failed ==> search for default label in given language
    search.setLanguage(lang);
    search.setRole("default");
    result = (SpecificLabel) get(search);
    if (result != null) {
      return result.getContent();
    }
    // 3nd search failed --> search for default label in default language
    search.setLanguage("default");
    search.setRole("default");
    result = (SpecificLabel) get(search);
    if (result != null) {
      return result.getContent();
    }
    // No default label found, return empty string
    return "";
  }
}