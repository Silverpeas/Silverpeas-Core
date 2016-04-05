/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.jstl.view;

import org.silverpeas.core.web.glossary.HighlightGlossaryTerms;
import org.silverpeas.core.contribution.content.wysiwyg.dynamicvalue.control.DynamicValueReplacement;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Iterator;

/**
 *
 * @author ehugonnet
 */
public class WysiwygDisplayerTag extends TagSupport {

  private static final long serialVersionUID = 1L;
  private String objectId;
  private String componentId;
  private String language;
  private String axisId;
  private boolean highlightFirst;

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getAxisId() {
    return axisId;
  }

  public void setAxisId(String axisId) {
    this.axisId = axisId;
  }

  public String getHighlightFirst() {
    return String.valueOf(highlightFirst);
  }

  public void setHighlightFirst(String highlightFirst) {
    this.highlightFirst = StringUtil.getBooleanValue(highlightFirst);
  }

  @Override
  public int doStartTag() throws JspException {
    try {
      String currentLang = getLanguage();
      String content = WysiwygController.loadForReadOnly(getComponentId(), getObjectId(),
          currentLang);
      //if content not found in specified language, check other ones
      if (!StringUtil.isDefined(content) && I18NHelper.isI18nContentActivated) {
        Iterator<String> languages = I18NHelper.getLanguages();
        while (languages.hasNext() && !StringUtil.isDefined(content)) {
          currentLang = languages.next();
          content = WysiwygController.loadForReadOnly(getComponentId(), getObjectId(), currentLang);
        }
      }

      //dynamic value functionnality : check if active and try to replace the keys by their values
      if (DynamicValueReplacement.isActivate()) {
        DynamicValueReplacement replacement = new DynamicValueReplacement();
        content = replacement.replaceKeyByValue(content);
      }

      //highlight glossary term
      if (StringUtil.isDefined(getAxisId())) {
        HighlightGlossaryTerms highlightGlossaryTerms =
            ServiceProvider.getService(HighlightGlossaryTerms.class);
        content = highlightGlossaryTerms.searchReplace(content, "highlight-silver",
            getAxisId(), highlightFirst, currentLang);
      }
      pageContext.getOut().println(content);
    } catch (Exception e) {
      throw new JspException("Can't display wysiwyg player", e);
    }
    return SKIP_BODY;
  }
}
