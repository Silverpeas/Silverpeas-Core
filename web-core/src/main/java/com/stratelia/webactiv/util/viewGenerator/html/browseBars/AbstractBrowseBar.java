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

import java.util.List;

import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.i18n.I18NBean;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

/**
 * The default implementation of ArrayPane interface
 * 
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
  private List languages = null;

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public AbstractBrowseBar() {
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param domainName
   * @param componentName
   * @param information
   * @param path
   * 
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
   * 
   * 
   * @param domainName
   * 
   * @see
   */
  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getDomainName() {
    return this.domainName;
  }

  /**
   * Method declaration
   * 
   * 
   * @param componentName
   * 
   * @see
   */
  public void setComponentName(String componentName) {
    this.componentName = componentName;
    this.componentLink = null;
  }

  /**
   * Method declaration
   * 
   * 
   * @param componentName
   * @param link
   * 
   * @see
   */
  public void setComponentName(String componentName, String link) {
    this.componentName = componentName;
    this.componentLink = link;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getComponentName() {
    return this.componentName;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getComponentLink() {
    return this.componentLink;
  }

  /**
   * Method declaration
   * 
   * 
   * @param information
   * 
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
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getExtraInformation() {
    return this.information;
  }

  /**
   * Method declaration
   * 
   * 
   * @param path
   * 
   * @see
   */
  public void setPath(String path) {
    if (path != null) {
      if (path.length() > 0) {
        this.path = path;
      }
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getPath() {
    return this.path;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }

  public I18NBean getI18NBean() {
    return this.i18nBean;
  }

  public String getLanguage() {
    return this.language;
  }

  public String getUrl() {
    return url;
  }

  public List getLanguages() {
    return languages;
  }

  public void setI18N(I18NBean bean, String language) {
    this.i18nBean = bean;
    this.language = language;
  }

  public void setI18N(String url, String language) {
    this.url = url;
    this.language = language;
  }

  public void setI18N(List languages, String language) {
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
   * 
   * 
   * @return
   * 
   * @see
   */
  public abstract String print();
}
