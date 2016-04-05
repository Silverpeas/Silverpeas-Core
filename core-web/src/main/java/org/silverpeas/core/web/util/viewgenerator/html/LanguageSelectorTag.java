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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Label;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.Span;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.i18n.I18NLanguage;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author ehugonnet
 */
public class LanguageSelectorTag extends TagSupport {
  private static final long serialVersionUID = -6521946554686125224L;

  private String currentLangCode = I18NHelper.defaultLanguage;
  private String elementId;
  private String elementName;
  private boolean includeLabel;
  private boolean readOnly = false;

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public void setElementName(String elementName) {
    this.elementName = elementName;
  }

  public void setIncludeLabel(boolean includeLabel) {
    this.includeLabel = includeLabel;
  }

  public String getLangCode() {
    return currentLangCode;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
  }

  @Override
  public int doStartTag() throws JspException {
    ElementContainer xhtml = new ElementContainer();
    if (I18NHelper.isI18nContentActivated) {
      String userLanguage = null;
      final Locale locale = (Locale) Config.find(pageContext, Config.FMT_LOCALE);
      if (locale != null) {
        userLanguage = locale.getLanguage();
      }
      final Element langElement;
      if (!isReadOnly()) {
        Select langSelector = new Select();
        langSelector.setID(elementId);
        langSelector.setName(elementName);
        List<Option> options = new ArrayList<Option>(I18NHelper.getNumberOfLanguages());
        for (I18NLanguage language : I18NHelper.getAllUserTranslationsOfContentLanguages(
            userLanguage)) {
          Option option = new Option(language.getLabel(), language.getCode());
          option.addElement(language.getLabel());
          if (getLangCode().equalsIgnoreCase(language.getCode())) {
            option.setSelected(true);
          }
          options.add(option);
        }
        langSelector.addElement(options.toArray(new Option[options.size()]));
        langElement = langSelector;
      } else {
        Span readOnlyLanguage = new Span();
        readOnlyLanguage.setID(elementId);
        readOnlyLanguage.addElement(I18NHelper.getLanguageLabel(getLangCode(), userLanguage));
        langElement = readOnlyLanguage;
      }
      if (includeLabel) {
        Label label = new Label(elementId);
        label.setStyle("margin-right: 5px");
        label.addElement(
            ResourceLocator.getGeneralLocalizationBundle(userLanguage).getString("GML.language"));
        xhtml.addElement(label);
      }
      xhtml.addElement(langElement);
    } else {
      Input hidden = new Input();
      hidden.setID(elementId);
      hidden.setName(elementName);
      hidden.setType("hidden");
      xhtml.addElement(hidden);
    }
    xhtml.output(pageContext.getOut());
    return SKIP_BODY;
  }

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  public void setLangCode(String currentLang) {
    currentLangCode = I18NHelper.checkLanguage(currentLang);
  }
}
