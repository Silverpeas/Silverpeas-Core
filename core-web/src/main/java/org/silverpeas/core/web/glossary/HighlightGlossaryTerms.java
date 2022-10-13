/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.glossary;

import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.pdc.model.Axis;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.text.MessageFormat.format;

/**
 * @author David Derigent
 */
public class HighlightGlossaryTerms {

  @Inject
  private PdcManager pdc;

  /**
   * default constructor
   */
  protected HighlightGlossaryTerms() {
  }

  /**
   * Search all the term contained in the given glossary and highlight, one or all of the
   * occurrences found in the given text. The regular expression used to retrieve the occurrence
   * works only in HTML code, not in plain text. This code is designed to parse HTML code.
   * @param publicationContent the publication text used to perform the highlight operation
   * @param className the css class name used to "mark" the highlighted term and format his
   * displaying
   * @param axisId The pdc axis identifier used to load the glossary
   * @param onlyFirst indicates if all the occurrence of a term must be highlight or only the first
   * @param language indicates the language use to load information from glossary
   * @return a String which contain given publication with highlight term.
   */
  public String searchReplace(String publicationContent, String className, String axisId,
      boolean onlyFirst, String language) {
    String replacedContent = publicationContent;

    List<Value> glossary = null;
    // get the glossary terms
    try {
      Axis axis = pdc.getAxisDetail(axisId);
      if (axis != null) {
        glossary = axis.getValues();
        Collections.sort(glossary, new TermComparator());
      }
    } catch (PdcException pdcEx) {
      SilverLogger.getLogger(this).warn(pdcEx);
    }
    // highlight the term retrieved in the content
    if (CollectionUtil.isNotEmpty(glossary)) {
      for (Value node : glossary) {
        String desc = node.getDescription(language);
        if (StringUtil.isDefined(desc)) {
          replacedContent =
              highlight(node.getName(language), replacedContent, desc, className, onlyFirst);
        }
      }
    }
    return replacedContent;
  }

  private String highlight(String term, String content, String definition, String className,
      boolean onlyFirst) {
    // escape HTML character
    String escapedTerm = WebEncodeHelper.convertHTMLEntities(term).replace("'", "&#39;");
    String groupName = "realTerm";
    // regular expression which allows to search all the term except the HTML tag Searches the
    // exact term
    Pattern pattern = Pattern.compile("(?i)(?<realTerm>\\b" + escapedTerm + "\\b)(?=[^>]*<(?!/a))");

    // highlights the term
    String template = "<span class=\"" + className + "\" title=\"" +
        definition.replace("\"", "&quot;").replace("'", "''") + "\">{0}</span>";

    StringBuilder sb = new StringBuilder();
    Matcher matcher = pattern.matcher(content);
    int lastBeginIndex = 0;
    while (matcher.find()) {
      sb.append(content.substring(lastBeginIndex, matcher.start(groupName)));
      String realTerm = matcher.group(groupName);
      sb.append(format(template, realTerm));
      lastBeginIndex = matcher.end(groupName);
      if (onlyFirst) {
        break;
      }
    }
    sb.append(content.substring(lastBeginIndex));
    return sb.toString();
  }
}