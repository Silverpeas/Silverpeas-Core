/**
 * 
 */
package com.silverpeas.glossary;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.Axis;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.treeManager.model.TreeNode;

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
   * search all the term contain in the given glossary and highlight, one or all the occurrences found in the given text.
   * The regular expression used to retrieve the occurrence works only in HTML code, not in plain text. This code is designed to parse HTML code.
   * @param publicationContent the publication text used to perform the highlight operation
   * @param className the css class name used to "mark" the highlighted term and format his displaying
   * @param axisId The pdc axis identifier used to load the glossary
   * @param onlyFirst indicates if all the occurrence of a term must be highlight or only the first
   * @param language indicates the language use to load information from glossary
   * @return a String which contain given publication with highlight term. 
   */
  public static String searchReplace(String publicationContent, String className, String axisId,
      boolean onlyFirst,
      String language) {

    PdcBmImpl pdc = new PdcBmImpl();
    List<TreeNode> glossary = null;
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
      for (TreeNode node : glossary) {
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
    
    //escape HTML character
    term = StringEscapeUtils.escapeHtml(term);
    // regular expression which allows to search all the term except the HTML tag
    // Searches the exact term
    String regex = "((?i)\\b" + term  + "\\b)(?=[^>]*<)";

    // highlights the term
    String replacement =
        "<a href=\"#\" class=\"" + className + "\" title =\"" + definition + "\"> " + term +
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
