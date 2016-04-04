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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * FormLine.java
 *
 */

package org.silverpeas.core.web.util.viewgenerator.html.formpanes;

import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

/**
 * @author frageade
 * @version
 */
public abstract class FormLine implements SimpleGraphicElement {

  public static final String DEFAULT_LANGUAGE = "fr";

  protected String type;
  protected FormPane pane;
  protected String name;
  protected String label;
  protected boolean mandatory;
  protected boolean DBEntry;
  protected boolean locked;
  protected String value;
  protected String id;
  protected String DBType;
  protected LocalizationBundle message;

  /**
   * Constructor declaration
   * @see
   */
  public FormLine() {
    name = "newFormLine";
    id = "newFormLine";
    label = "";
    value = "";
    mandatory = false;
    locked = false;
    DBEntry = false;
    type = "undefined";
    DBType = "character varying";
    message = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.util.viewGenerator.formPane.formPaneBundle",
        DEFAULT_LANGUAGE);
  }

  /**
   * Constructor declaration
   * @param nam
   * @param val
   * @see
   */
  public FormLine(String nam, String val) {
    name = nam;
    id = nam;
    label = "";
    value = val;
    mandatory = false;
    locked = false;
    DBEntry = false;
    type = "undefined";
    DBType = "character varying";
    message = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.util.viewGenerator.formPane.formPaneBundle",
        DEFAULT_LANGUAGE);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getType() {
    return type;
  }

  /**
   * Method declaration
   * @param typ
   * @see
   */
  public void setType(String typ) {
    type = typ;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean isMandatory() {
    return mandatory;
  }

  /**
   * Method declaration
   * @param mand
   * @see
   */
  public void setMandatory(boolean mand) {
    mandatory = mand;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean isLocked() {
    return locked;
  }

  /**
   * Method declaration
   * @param lock
   * @see
   */
  public void setLocked(boolean lock) {
    locked = lock;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean isDBEntry() {
    return DBEntry;
  }

  /**
   * Method declaration
   * @param dbe
   * @see
   */
  public void setDBEntry(boolean dbe) {
    DBEntry = dbe;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getDBType() {
    return DBType;
  }

  /**
   * Method declaration
   * @param typ
   * @see
   */
  public void setDBType(String typ) {
    DBType = typ;
  }

  /**
   * Method declaration
   * @param fp
   * @see
   */
  public void setPane(FormPane fp) {
    pane = fp;
  }

  /**
   * Method declaration
   * @param val
   * @see
   */
  public void setValue(String val) {
    value = val;
    if (value == null) {
      value = "";
    }
  }

  /**
   * Method declaration
   * @param nam
   * @see
   */
  public void setName(String nam) {
    name = nam;
    if (name == null) {
      name = "newFormLine";
    }
  }

  /**
   * Method declaration
   * @param lab
   * @see
   */
  public void setLabel(String lab) {
    label = lab;
    if (label == null) {
      label = "";
    }
  }

  /**
   * Method declaration
   * @param newId
   * @see
   */
  public void setId(String newId) {
    id = newId;
    if (id == null) {
      id = "newFormLine";
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getValue() {
    String retour = value;

    if (retour == null) {
      retour = "";
    }
    return retour;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getName() {
    return name;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getLabel() {
    String retour = label;

    if (retour == null) {
      retour = "";
    }
    return retour;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getId() {
    return id;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getDBColumnCreationRequest() {
    return "";
  }

  /**
   * Method declaration
   * @param language
   * @see
   */
  public void setLanguage(String language) {
    if (language != null) {
      message = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.util.viewGenerator.formPane.formPaneBundle",
          language);
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String print();

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String printDemo();

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String toXML();

  /**
   * Method declaration
   * @param nam
   * @param url
   * @param pc
   * @return
   * @see
   */
  public abstract FormPane getDescriptor(String nam, String url, PageContext pc);

  /**
   * Method declaration
   * @param req
   * @see
   */
  public abstract void getConfigurationByRequest(HttpServletRequest req);

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean validate() {
    return true;
  }

  /**
   * Method declaration
   * @param param
   * @return
   * @see
   */
  public String noNull(String param) {
    if (param == null) {
      param = "";
    } else if (param.equalsIgnoreCase("null")) {
      param = "";
    }
    return param;
  }

}
