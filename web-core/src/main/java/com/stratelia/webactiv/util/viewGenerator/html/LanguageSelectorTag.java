/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.util.viewGenerator.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.i18n.I18NLanguage;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;

/**
 *
 * @author ehugonnet
 */
public class LanguageSelectorTag extends SimpleTagSupport {

  private String currentLangCode = I18NHelper.defaultLanguage;
  private String elementId;
  private String elementName;

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public void setElementName(String elementName) {
    this.elementName = elementName;
  }

  public String getLangCode() {
    return currentLangCode;
  }

  @Override
  public void doTag() throws JspException, IOException {
    ElementContainer xhtml = new ElementContainer();
    if (I18NHelper.isI18N) {
      Select langSelector = new Select();
      langSelector.setID(elementId);
      langSelector.setName(elementName);
      List<Option> options = new ArrayList<Option>(I18NHelper.getNumberOfLanguages());
      for (I18NLanguage language : I18NHelper.getAllLanguages(getLangCode())) {
        Option option = new Option(language.getLabel(), language.getCode());
        option.addElement(language.getLabel());
        if (getLangCode().equalsIgnoreCase(language.getCode())) {
          option.setSelected(true);
        }
        options.add(option);
      }
      langSelector.addElement(options.toArray(new Option[options.size()]));
      xhtml.addElement(langSelector);
    } else {
      Input hidden = new Input();
      hidden.setID(elementId);
      hidden.setName(elementName);
      hidden.setType("hidden");
      xhtml.addElement(hidden);
    }
    xhtml.output(getJspContext().getOut());
  }

  public void setLangCode(String currentLang) {
    currentLangCode = I18NHelper.checkLanguage(currentLang);
  }
}
