/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * BrowseBar.java
 * 
 * Created on 07 decembre 2000, 11:26
 */

package com.stratelia.webactiv.util.viewGenerator.html.browseBars;

import java.util.List;

import com.silverpeas.util.i18n.I18NBean;
import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/**
 * The Browse interface gives us the skeleton for all funtionnalities we need to
 * display typical WA browse bar
 * 
 * @author neysseri
 * @version 1.0
 */
public interface BrowseBar extends SimpleGraphicElement {

  /**
   * Method declaration
   * 
   * 
   * @param domainName
   * 
   * @see
   */
  public void setDomainName(String domainName);

  /**
   * Method declaration
   * 
   * 
   * @param componentName
   * 
   * @see
   */
  public void setComponentName(String componentName);

  /**
   * Method declaration
   * 
   * 
   * @param componentName
   * @param link
   * 
   * @see
   */
  public void setComponentName(String componentName, String link);

  /**
   * Method declaration
   * 
   * 
   * @param information
   * 
   * @see
   */
  public void setExtraInformation(String information);

  /**
   * Method declaration
   * 
   * 
   * @param path
   * 
   * @see
   */
  public void setPath(String path);

  public void setI18N(I18NBean bean, String language);

  public void setI18N(String url, String language);

  public void setI18N(List languages, String language);

  /**
   * Print the browseBar in an html format.
   * 
   * @return The html based line code
   */
  public String print();
}
