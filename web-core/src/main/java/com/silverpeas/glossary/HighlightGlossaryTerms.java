/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
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
 * 
 */
package com.silverpeas.glossary;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.Axis;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.Value;

/**
 * @author David Derigent
 */
public class HighlightGlossaryTerms {

  /**
   * 
   */
  private HighlightGlossaryTerms() {
  }

  /**
   * search all the term contain in the given glossary and highlight, one or all the occurrences
   * found in the given text. The regular expression used to retrieve the occurrence works only in
   * HTML code, not in plain text. This code is designed to parse HTML code.
   * @param publicationContent the publication text used to perform the highlight operation
   * @param className the css class name used to "mark" the highlighted term and format his
   * displaying
   * @param axisId The pdc axis identifier used to load the glossary
   * @param onlyFirst indicates if all the occurrence of a term must be highlight or only the first
   * @param language indicates the language use to load information from glossary
   * @return a String which contain given publication with highlight term.
   */
  public static String searchReplace(String publicationContent, String className, String axisId,
      boolean onlyFirst,
      String language) {

    PdcBmImpl pdc = new PdcBmImpl();
    List<Value> glossary = null;
    // get the glossary terms
    try {
      Axis axis = pdc.getAxisDetail(axisId);
      if (axis != null) {
        glossary = axis.getValues();
        Collections.sort(glossary, new TermComparator());
      }
    } catch (PdcException pdcEx) {
    }
    // highlight the term retrieved in the content
    if (glossary != null && !glossary.isEmpty()) {
      for (Value node : glossary) {
        publicationContent =
            highlight(node.getName(language), publicationContent, node.getDescription(language),
            className, onlyFirst);
      }
    }
    return publicationContent;

  }

  private static String highlight(String term, String
      publication, String definition, String className, boolean onlyFirst) {
    String highlightedAnswer = publication;

    // escape HTML character
    term = StringEscapeUtils.escapeHtml(term);
    // regular expression which allows to search all the term except the HTML tag
    // Searches the exact term
    String regex = "((?i)\\b" + term + "\\b)(?=[^>]*<)";

    // highlights the term
    String replacement =
        "<a href=\"#\" class=\"" + className + "\" title =\"" + definition.replaceAll("\"", "&quot;") + "\"> " + term +
        " </a>";

    if (onlyFirst) {
      // highlights only the first occurrence
      highlightedAnswer = highlightedAnswer.replaceFirst(regex, replacement);
    } else {
      // highlights all the retrieved occurrences of the word
      highlightedAnswer = highlightedAnswer.replaceAll(regex, replacement);
    }

    return highlightedAnswer;
  }

}
