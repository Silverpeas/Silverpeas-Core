/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * ArrayPaneWA.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.browseBars;

import java.util.ArrayList;
import java.util.List;

import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NBean;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

/**
 * The default implementation of ArrayPane interface
 * @author squere
 * @version 1.0
 */
public abstract class AbstractBrowseBar implements BrowseBar {
  private String domainName = null;
  private String componentName = null;
  private String componentLink = null;
  private String information = null;
  private String path = null;

  private I18NBean i18nBean = null;
  private String language = null;
  private String url = null;
  private List<String> languages = null;

  private String spaceId = null;
  private String componentId = null;
  private MainSessionController mainSessionController = null;

  private List<BrowseBarElement> elements = new ArrayList<BrowseBarElement>();

  private String spaceJavascriptCallback = null;
  private String componentJavascriptCallback = null;
  private boolean clickable = true;

  /**
   * Constructor declaration
   * @see
   */
  public AbstractBrowseBar() {
  }

  /**
   * Constructor declaration
   * @param domainName
   * @param componentName
   * @param information
   * @param path
   * @see
   */
  public AbstractBrowseBar(String domainName, String componentName,
      String information, String path) {
    this.domainName = domainName;
    this.componentName = componentName;
    this.information = information;
    this.path = path;
  }

  /**
   * Method declaration
   * @param domainName
   * @see
   */
  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getDomainName() {
    return domainName;
  }

  /**
   * Method declaration
   * @param componentName
   * @see
   */
  public void setComponentName(String componentName) {
    this.componentName = componentName;
    componentLink = null;
  }

  /**
   * Method declaration
   * @param componentName
   * @param link
   * @see
   */
  public void setComponentName(String componentName, String link) {
    this.componentName = componentName;
    componentLink = link;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getComponentName() {
    return componentName;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getComponentLink() {
    return componentLink;
  }

  /**
   * Method declaration
   * @param information
   * @see
   */
  public void setExtraInformation(String information) {
    if (information != null) {
      if (information.length() > 0) {
        this.information = EncodeHelper.javaStringToHtmlString(information);
      }
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getExtraInformation() {
    return information;
  }

  /**
   * Method declaration
   * @param path
   * @see
   */
  public void setPath(String path) {
    if (StringUtil.isDefined(path)) {
      this.path = path;
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getPath() {
    return path;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }

  public I18NBean getI18NBean() {
    return i18nBean;
  }

  public String getLanguage() {
    return language;
  }

  public String getUrl() {
    return url;
  }

  public List<String> getLanguages() {
    return languages;
  }

  public void addElement(BrowseBarElement element) {
    elements.add(element);
  }

  public void addElements(List<BrowseBarElement> elements) {
    this.elements.addAll(elements);
  }

  public void setElements(List<BrowseBarElement> elements) {
    this.elements.clear();
    this.elements.addAll(elements);
  }

  public List<BrowseBarElement> getElements() {
    return elements;
  }

  public void setI18N(I18NBean bean, String language) {
    i18nBean = bean;
    this.language = language;
  }

  public void setI18N(String url, String language) {
    this.url = url;
    this.language = language;
  }

  public void setI18N(List<String> languages, String language) {
    this.languages = languages;
    this.language = language;
  }

  public boolean isI18N() {
    return getI18NBean() != null || getUrl() != null || getLanguages() != null;
  }

  public String getI18NHTMLLinks() {
    if (getI18NBean() != null)
      return I18NHelper.getHTMLLinks(getI18NBean(), getLanguage());
    else if (getUrl() != null)
      return I18NHelper.getHTMLLinks(getUrl(), getLanguage());
    else
      return I18NHelper.getHTMLLinks(getLanguages(), getLanguage());
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String print();

  public String getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public MainSessionController getMainSessionController() {
    return mainSessionController;
  }

  public void setMainSessionController(MainSessionController mainSessionController) {
    this.mainSessionController = mainSessionController;
  }

  public abstract String getBreadCrumb();

  public void setSpaceJavascriptCallback(String callback) {
    spaceJavascriptCallback = callback;
  }

  public void setComponentJavascriptCallback(String callback) {
    componentJavascriptCallback = callback;
  }

  public String getSpaceJavascriptCallback() {
    return spaceJavascriptCallback;
  }

  public String getComponentJavascriptCallback() {
    return componentJavascriptCallback;
  }
  
  public void setClickable(boolean clickable)
  {
    this.clickable = clickable;
  }
  
  public boolean isClickable() {
    return clickable;
  }

}
