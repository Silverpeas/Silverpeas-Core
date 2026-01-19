/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * FormLine.java
 *
 */

package org.silverpeas.core.web.util.viewgenerator.html.formpanes;

import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.PageContext;

/**
 * @author frageade
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

  public String getType() {
    return type;
  }

  public void setType(String typ) {
    type = typ;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public void setMandatory(boolean mand) {
    mandatory = mand;
  }

  public boolean isLocked() {
    return locked;
  }

  public void setLocked(boolean lock) {
    locked = lock;
  }

  public boolean isDBEntry() {
    return DBEntry;
  }

  public void setDBEntry(boolean dbe) {
    DBEntry = dbe;
  }

  public String getDBType() {
    return DBType;
  }

  public void setDBType(String typ) {
    DBType = typ;
  }

  public void setPane(FormPane fp) {
    pane = fp;
  }

  public void setValue(String val) {
    value = val;
    if (value == null) {
      value = "";
    }
  }

  public void setName(String nam) {
    name = nam;
    if (name == null) {
      name = "newFormLine";
    }
  }

  public void setLabel(String lab) {
    label = lab;
    if (label == null) {
      label = "";
    }
  }

  public void setId(String newId) {
    id = newId;
    if (id == null) {
      id = "newFormLine";
    }
  }

  public String getValue() {
    String retour = value;

    if (retour == null) {
      retour = "";
    }
    return retour;
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    String retour = label;

    if (retour == null) {
      retour = "";
    }
    return retour;
  }

  public String getId() {
    return id;
  }

  public String getDBColumnCreationRequest() {
    return "";
  }

  public void setLanguage(String language) {
    if (language != null) {
      message = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.util.viewGenerator.formPane.formPaneBundle",
          language);
    }
  }

  public abstract String print();

  public abstract String printDemo();

  public abstract String toXML();

  public abstract FormPane getDescriptor(String nam, String url, PageContext pc);

  public abstract void getConfigurationByRequest(HttpServletRequest req);

  public boolean validate() {
    return true;
  }

  public String noNull(String param) {
    if (param == null) {
      param = "";
    } else if (param.equalsIgnoreCase("null")) {
      param = "";
    }
    return param;
  }

}
