/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.web.form;

import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

/**
 * @author neysseric
 */
public class FormTemplateSessionController extends AbstractComponentSessionController {
  private String objectId;
  private String objectType;
  private String xmlFormName;
  private String objectLanguage;
  private String reloadOpener;
  private String urlToReload;

  public FormTemplateSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.form.multilang.formBundle", null, null);
  }

  public void setComponentId(String componentId) {
    this.context.setCurrentComponentId(componentId);
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String getObjectType() {
    return objectType;
  }

  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  public String getXmlFormName() {
    return xmlFormName;
  }

  public void setXmlFormName(String xmlFormName) {
    this.xmlFormName = xmlFormName;
  }

  public String getObjectLanguage() {
    return objectLanguage;
  }

  public void setObjectLanguage(String objectLanguage) {
    this.objectLanguage = objectLanguage;
  }

  public String getReloadOpener() {
    return reloadOpener;
  }

  public void setReloadOpener(String reloadOpener) {
    this.reloadOpener = reloadOpener;
  }

  public String getUrlToReload() {
    return urlToReload;
  }

  public void setUrlToReload(String urlToReload) {
    this.urlToReload = urlToReload;
  }
}