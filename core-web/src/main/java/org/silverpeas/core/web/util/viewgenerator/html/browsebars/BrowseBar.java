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

package org.silverpeas.core.web.util.viewgenerator.html.browsebars;

import java.util.List;

import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.i18n.I18NBean;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

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
   * @see
   */
  public void setDomainName(String domainName);

  /**
   * Method declaration
   * @param componentName
   * @see
   */
  public void setComponentName(String componentName);

  /**
   * Method declaration
   * @param componentName
   * @param link
   * @see
   */
  public void setComponentName(String componentName, String link);

  /**
   * Method declaration
   * @param information
   * @see
   */
  public void setExtraInformation(String information);

  /**
   * Method declaration
   * @param path
   * @see
   */
  public void setPath(String path);

  public void setI18N(I18NBean bean, String language);

  public void setI18N(String url, String language);

  public void setI18N(List<String> languages, String language);

  public void setSpaceId(String spaceId);

  public void setComponentId(String componentId);

  public void setMainSessionController(MainSessionController mainSessionController);

  public void setLook(LookHelper look);

  /**
   * Print the browseBar in an html format.
   * @return The html based line code
   */
  @Override
  public String print();

  public void addElement(BrowseBarElement element);

  /**
   * add given elements to existing elements
   * @param elements to add to breadscrumb
   */
  public void addElements(List<BrowseBarElement> elements);

  /**
   * remove existing elements and add given elements
   * @param elements to add to breadscrumb
   */
  public void setElements(List<BrowseBarElement> elements);

  public String getBreadCrumb();

  public void setSpaceJavascriptCallback(String callback);

  public void setComponentJavascriptCallback(String callback);

  public void setClickable(boolean clickable);

  public void setIgnoreComponentLink(boolean ignore);

}
