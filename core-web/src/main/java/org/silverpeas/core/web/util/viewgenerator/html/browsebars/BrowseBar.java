/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.browsebars;

import org.silverpeas.core.i18n.I18NBean;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

import java.util.List;

/**
 * The Browse interface gives us the skeleton for all funtionnalities we need to display typical WA
 * browse bar
 * @author neysseri
 * @version 1.0
 */
public interface BrowseBar extends SimpleGraphicElement {

  /**
   * Method declaration
   * @param domainName
   *
   */
  void setDomainName(String domainName);

  /**
   * Method declaration
   * @param componentName
   *
   */
  void setComponentName(String componentName);

  /**
   * Method declaration
   * @param componentName
   * @param link
   *
   */
  void setComponentName(String componentName, String link);

  /**
   * Method declaration
   * @param information
   *
   */
  void setExtraInformation(String information);

  /**
   * Method declaration
   * @param path
   *
   */
  void setPath(String path);

  void setI18N(I18NBean bean, String language);

  void setI18N(String url, String language);

  void setI18N(List<String> languages, String language);

  void setSpaceId(String spaceId);

  void setComponentId(String componentId);

  void setMainSessionController(MainSessionController mainSessionController);

  void setLook(LookHelper look);

  /**
   * Print the browseBar in an html format.
   * @return The html based line code
   */
  @Override
  String print();

  void addElement(BrowseBarElement element);

  /**
   * add given elements to existing elements
   * @param elements to add to breadscrumb
   */
  void addElements(List<BrowseBarElement> elements);

  /**
   * remove existing elements and add given elements
   * @param elements to add to breadscrumb
   */
  void setElements(List<BrowseBarElement> elements);

  String getBreadCrumb();

  void setSpaceJavascriptCallback(String callback);

  void setComponentJavascriptCallback(String callback);

  void setClickable(boolean clickable);

  void setIgnoreComponentLink(boolean ignore);

}
