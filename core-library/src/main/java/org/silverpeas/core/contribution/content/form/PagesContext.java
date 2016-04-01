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

package org.silverpeas.core.contribution.content.form;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.contribution.attachment.util.SharingContext;

import javax.servlet.http.HttpServletRequest;

/**
 * The page context where a form is displayed.
 */
public class PagesContext {
  public static final int ON_UPDATE_REPLACE_EMPTY_VALUES = 0;
  public static final int ON_UPDATE_IGNORE_EMPTY_VALUES = 1;

  RenderingContext context = RenderingContext.WEB;
  String formName = "";
  String formIndex = "0";
  String currentFieldIndex = "0";
  String language = "fr";
  boolean printTitle;
  String componentId;
  String userId;
  String objectId;
  boolean versioningUsed;
  boolean printBorder = true;
  String contentLanguage = "fr";
  int nbFields;
  String nodeId;
  int lastFieldIndex;
  boolean useMandatory = true; // used to modify several objects at the same
  // time.
  boolean useBlankFields; // display all fields blank
  boolean ignoreDefaultValues; // do not display default value
  String xmlFormName = "";
  int updatePolicy;
  String encoding = "UTF-8";
  boolean creation;
  String serverURL;
  boolean designMode;
  SharingContext sharingContext;

  public PagesContext() {
  }

  public PagesContext(PagesContext pc) {
    setRenderingContext(pc.getRenderingContext());
    setFormIndex(pc.getFormIndex());
    setFormName(pc.getFormName());
    setLanguage(pc.getLanguage());
    setCurrentFieldIndex(pc.getCurrentFieldIndex());
    setPrintTitle(pc.getPrintTitle());
    setComponentId(pc.getComponentId());
    setUserId(pc.getUserId());
    setVersioningUsed(pc.isVersioningUsed());
    setObjectId(pc.getObjectId());
    setContentLanguage(pc.getContentLanguage());
    setNbFields(pc.getNbFields());
    setNodeId(pc.getNodeId());
    setLastFieldIndex(pc.getLastFieldIndex());
    setUseMandatory(pc.useMandatory());
    setUseBlankFields(pc.isBlankFieldsUse());
    setIgnoreDefaultValues(pc.isIgnoreDefaultValues());
    setUpdatePolicy(pc.getUpdatePolicy());
    setCreation(pc.isCreation());
    setSharingContext(pc.getSharingContext());
  }

  public PagesContext(String formIndex, String language) {
    setFormIndex(formIndex);
    setLanguage(language);
  }

  public PagesContext(String formName, String formIndex, String language) {
    setFormIndex(formIndex);
    setFormName(formName);
    setLanguage(language);
  }

  public PagesContext(String formName, String formIndex, String language,
      String userId) {
    setFormIndex(formIndex);
    setFormName(formName);
    setLanguage(language);
    setUserId(userId);
  }

  public PagesContext(String formIndex, String language, boolean printTitle) {
    setFormIndex(formIndex);
    setLanguage(language);
    setPrintTitle(printTitle);
  }

  public PagesContext(String formName, String formIndex, String language,
      boolean printTitle) {
    setFormIndex(formIndex);
    setFormName(formName);
    setLanguage(language);
    setPrintTitle(printTitle);
  }

  public PagesContext(String formName, String formIndex, String language,
      boolean printTitle, String componentId, String userId) {
    setFormIndex(formIndex);
    setFormName(formName);
    setLanguage(language);
    setPrintTitle(printTitle);
    setComponentId(componentId);
    setUserId(userId);
  }

  public PagesContext(String formName, String formIndex, String language,
      boolean printTitle, String componentId, String userId, String nodeId) {
    setFormIndex(formIndex);
    setFormName(formName);
    setLanguage(language);
    setPrintTitle(printTitle);
    setComponentId(componentId);
    setUserId(userId);
    setNodeId(nodeId);
  }

  public RenderingContext getRenderingContext() {
    return context;
  }

  public final void setRenderingContext(final RenderingContext context) {
    this.context = context;
  }

  public String getFormName() {
    return formName;
  }

  public final void setFormName(String formName) {
    this.formName = formName;
  }

  public String getFormIndex() {
    return formIndex;
  }

  public final void setFormIndex(String formIndex) {
    this.formIndex = formIndex;
  }

  public String getCurrentFieldIndex() {
    return currentFieldIndex;
  }

  public String getLanguage() {
    return language;
  }

  public final void setLanguage(String language) {
    this.language = language;
  }

  public boolean getPrintTitle() {
    return printTitle;
  }

  public final void setPrintTitle(boolean printTitle) {
    this.printTitle = printTitle;
  }

  public final void setCurrentFieldIndex(String currentFieldIndex) {
    this.currentFieldIndex = currentFieldIndex;
  }

  public final void incCurrentFieldIndex(int increment) {
    int currentFieldIndexInt = 0;
    if (currentFieldIndex != null) {
      currentFieldIndexInt = Integer.parseInt(currentFieldIndex);
    }
    currentFieldIndexInt = currentFieldIndexInt + increment;
    this.currentFieldIndex = Integer.toString(currentFieldIndexInt);
  }

  public String getComponentId() {
    return componentId;
  }

  public final void setComponentId(String string) {
    componentId = string;
  }

  public String getUserId() {
    return userId;
  }

  public final void setUserId(String string) {
    userId = string;
  }

  public String getObjectId() {
    return objectId;
  }

  public final void setObjectId(String string) {
    objectId = string;
  }

  public boolean isVersioningUsed() {
    return versioningUsed;
  }

  public final void setVersioningUsed(boolean b) {
    versioningUsed = b;
  }

  public boolean isBorderPrinted() {
    return printBorder;
  }

  /**
   * Used by Form. If parameter equals true, encapsulated border (around the form) will be written.
   * Else no border will be displayed. Default value = true.
   * @param b
   */
  public final void setBorderPrinted(boolean b) {
    printBorder = b;
  }

  public String getContentLanguage() {
    return contentLanguage;
  }

  public final void setContentLanguage(String contentLanguage) {
    this.contentLanguage = contentLanguage;
  }

  public int getNbFields() {
    return this.nbFields;
  }

  public final void setNbFields(int nbFields) {
    this.nbFields = nbFields;
  }

  public String getNodeId() {
    return nodeId;
  }

  public final void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public int getLastFieldIndex() {
    return lastFieldIndex;
  }

  public final void setLastFieldIndex(int lastFieldIndex) {
    this.lastFieldIndex = lastFieldIndex;
  }

  public boolean isBlankFieldsUse() {
    return useBlankFields;
  }

  public final void setUseBlankFields(boolean useBlankFields) {
    this.useBlankFields = useBlankFields;
  }

  public boolean useMandatory() {
    return useMandatory;
  }

  public final void setUseMandatory(boolean ignoreMandatory) {
    this.useMandatory = ignoreMandatory;
  }

  public int getUpdatePolicy() {
    return updatePolicy;
  }

  public final void setUpdatePolicy(int updatePolicy) {
    this.updatePolicy = updatePolicy;
  }

  public boolean isIgnoreDefaultValues() {
    return ignoreDefaultValues;
  }

  public final void setIgnoreDefaultValues(boolean ignoreDefaultValues) {
    this.ignoreDefaultValues = ignoreDefaultValues;
  }

  public String getEncoding() {
    return encoding;
  }

  public final void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public boolean isCreation() {
    return creation;
  }

  public void setCreation(boolean creation) {
    this.creation = creation;
  }

  public String getServerURL() {
    return serverURL;
  }

  public void setRequest(HttpServletRequest request) {
    this.serverURL = URLUtil.getServerURL(request);
  }

  public boolean isDesignMode() {
    return designMode;
  }

  public void setDesignMode(boolean designMode) {
    this.designMode = designMode;
  }

  public SharingContext getSharingContext() {
    return sharingContext;
  }

  public void setSharingContext(SharingContext sharingContext) {
    this.sharingContext = sharingContext;
  }

  public boolean isSharingContext() {
    return sharingContext != null;
  }

}