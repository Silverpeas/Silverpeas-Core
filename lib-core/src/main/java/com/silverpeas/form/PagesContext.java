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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.form;

/**
 * The page context where a form is displayed.
 */
public class PagesContext {
  public static final int ON_UPDATE_REPLACE_EMPTY_VALUES = 0;
  public static final int ON_UPDATE_IGNORE_EMPTY_VALUES = 1;

  String formName = "";
  String formIndex = "0";
  String currentFieldIndex = "0";
  String language = "fr";
  boolean printTitle = false;
  String componentId = null;
  String userId = null;
  String objectId = null;
  boolean versioningUsed = false;
  boolean printBorder = true;
  String contentLanguage = "fr";
  int nbFields = 0;
  String nodeId = null;
  int lastFieldIndex;
  boolean useMandatory = true; // used to modify several objects at the same
  // time.
  boolean useBlankFields = false; // display all fields blank
  boolean ignoreDefaultValues = false; // do not display default value
  String xmlFormName = "";
  int updatePolicy = ON_UPDATE_REPLACE_EMPTY_VALUES;
  String encoding = "UTF-8";

  public PagesContext() {
  }

  public PagesContext(PagesContext pc) {
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

  public String getFormName() {
    return formName;
  }

  public void setFormName(String formName) {
    this.formName = formName;
  }

  public String getFormIndex() {
    return formIndex;
  }

  public void setFormIndex(String formIndex) {
    this.formIndex = formIndex;
  }

  public String getCurrentFieldIndex() {
    return currentFieldIndex;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public boolean getPrintTitle() {
    return printTitle;
  }

  public void setPrintTitle(boolean printTitle) {
    this.printTitle = printTitle;
  }

  public void setCurrentFieldIndex(String currentFieldIndex) {
    this.currentFieldIndex = currentFieldIndex;
  }

  public void incCurrentFieldIndex(int increment) {
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

  public void setComponentId(String string) {
    componentId = string;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String string) {
    userId = string;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String string) {
    objectId = string;
  }

  public boolean isVersioningUsed() {
    return versioningUsed;
  }

  public void setVersioningUsed(boolean b) {
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
  public void setBorderPrinted(boolean b) {
    printBorder = b;
  }

  public String getContentLanguage() {
    return contentLanguage;
  }

  public void setContentLanguage(String contentLanguage) {
    this.contentLanguage = contentLanguage;
  }

  public int getNbFields() {
    return this.nbFields;
  }

  public void setNbFields(int nbFields) {
    this.nbFields = nbFields;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public int getLastFieldIndex() {
    return lastFieldIndex;
  }

  public void setLastFieldIndex(int lastFieldIndex) {
    this.lastFieldIndex = lastFieldIndex;
  }

  public boolean isBlankFieldsUse() {
    return useBlankFields;
  }

  public void setUseBlankFields(boolean useBlankFields) {
    this.useBlankFields = useBlankFields;
  }

  public boolean useMandatory() {
    return useMandatory;
  }

  public void setUseMandatory(boolean ignoreMandatory) {
    this.useMandatory = ignoreMandatory;
  }

  public int getUpdatePolicy() {
    return updatePolicy;
  }

  public void setUpdatePolicy(int updatePolicy) {
    this.updatePolicy = updatePolicy;
  }

  public boolean isIgnoreDefaultValues() {
    return ignoreDefaultValues;
  }

  public void setIgnoreDefaultValues(boolean ignoreDefaultValues) {
    this.ignoreDefaultValues = ignoreDefaultValues;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
}