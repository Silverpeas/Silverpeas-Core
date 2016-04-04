/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.web.util.viewgenerator.html.glossary;

import org.silverpeas.core.web.glossary.HighlightGlossaryTerms;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;

/**
 * @author David Derigent
 */
public class HighlightTermTag extends BodyTagSupport {

  private static final HighlightGlossaryTerms highlighter =
      ServiceProvider.getService(HighlightGlossaryTerms.class);
  private String className = null;
  private String axisId = null;
  private boolean onlyFirst = false;
  private String language = null;

  private static final long serialVersionUID = -2139344290604645123L;

  @Override
  public int doAfterBody() throws JspTagException {
    try {
      BodyContent bc = getBodyContent();
      String highlightedText = bc.getString();
      if (StringUtil.isDefined(axisId) && !"0".equals(axisId)) {
        highlightedText = highlighter.searchReplace(highlightedText, className, axisId, onlyFirst,
            language);
      }
      bc.clearBody();
      getPreviousOut().print(highlightedText);
    } catch (IOException e) {
      throw new JspTagException("HighlightTermTag: " + e.getMessage());
    }
    return SKIP_BODY;
  }

  /**
   * @return the className
   */
  public String getClassName() {
    return className;
  }

  /**
   * @param className the className to set
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * @return the glossary identifier
   */
  public String getAxisId() {
    return axisId;
  }

  /**
   * @param axisId the glossary identifier to set
   */
  public void setAxisId(String axisId) {
    this.axisId = axisId;
  }

  /**
   * @return the onlyFirst
   */
  public boolean isOnlyFirst() {
    return onlyFirst;
  }

  /**
   * @param onlyFirst the onlyFirst to set
   */
  public void setOnlyFirst(boolean onlyFirst) {
    this.onlyFirst = onlyFirst;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }
}
