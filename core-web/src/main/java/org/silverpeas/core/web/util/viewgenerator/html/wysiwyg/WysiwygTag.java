/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html.wysiwyg;

import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class WysiwygTag extends TagSupport {

  private static final long serialVersionUID = -7606894410387540973L;

  private String replace;
  private String height;
  private String width;
  private String language;
  private String toolbar;
  private String spaceLabel;
  private String componentId;
  private String componentLabel;
  private String browseInfo;
  private String objectId;
  private String objectType;
  private boolean displayFileBrowser = true;
  private boolean activateWysiwygBackupManager = false;

  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getToolbar() {
    return toolbar;
  }

  public void setToolbar(String toolbar) {
    this.toolbar = toolbar;
  }

  public String getReplace() {
    return replace;
  }

  public void setReplace(String replace) {
    this.replace = replace;
  }

  public String getSpaceLabel() {
    return spaceLabel;
  }

  public void setSpaceLabel(String spaceLabel) {
    this.spaceLabel = spaceLabel;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getComponentLabel() {
    return componentLabel;
  }

  public void setComponentLabel(String componentLabel) {
    this.componentLabel = componentLabel;
  }

  public String getBrowseInfo() {
    return browseInfo;
  }

  public void setBrowseInfo(String browseInfo) {
    this.browseInfo = browseInfo;
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

  public void setObjectType(final String objectType) {
    this.objectType = objectType;
  }

  public boolean getDisplayFileBrowser() {
    return displayFileBrowser;
  }

  public void setDisplayFileBrowser(boolean displayFileBrowser) {
    this.displayFileBrowser = displayFileBrowser;
  }

  public boolean isActivateWysiwygBackupManager() {
    return activateWysiwygBackupManager;
  }

  public void setActivateWysiwygBackupManager(final boolean activateWysiwygBackupManager) {
    this.activateWysiwygBackupManager = activateWysiwygBackupManager;
  }

  @Override
  public int doEndTag() throws JspException {
    WysiwygEditor wysiwygEditor = new WysiwygEditor(
        getComponentId(), getObjectType(), getObjectId(), isActivateWysiwygBackupManager());
    wysiwygEditor.setReplace(getReplace());
    if (StringUtil.isDefined(getWidth())) {
      wysiwygEditor.setWidth(getWidth());
    }
    if (StringUtil.isDefined(getHeight())) {
      wysiwygEditor.setHeight(getHeight());
    }
    if (StringUtil.isDefined(getLanguage())) {
      wysiwygEditor.setLanguage(getLanguage());
    }
    if (StringUtil.isDefined(getToolbar())) {
      wysiwygEditor.setToolbar(getToolbar());
    }

    HttpSession session = pageContext.getSession();
    GraphicElementFactory gef =
        (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    session.setAttribute("WYSIWYG_SpaceLabel", getSpaceLabel());
    session.setAttribute("WYSIWYG_ComponentId", getComponentId());
    session.setAttribute("WYSIWYG_ComponentLabel", getComponentLabel());
    session.setAttribute("WYSIWYG_BrowseInfo", getBrowseInfo());
    session.setAttribute("WYSIWYG_ObjectId", getObjectId());
    session.setAttribute("WYSIWYG_ObjectType", getObjectType());
    session.setAttribute("WYSIWYG_Language", wysiwygEditor.getLanguage());

    SettingBundle settings = gef.getFavoriteLookSettings();
    if (settings != null) {
      wysiwygEditor.setCustomCSS(settings.getString("StyleSheet", ""));
    }

    wysiwygEditor.setDisplayFileBrowser(getDisplayFileBrowser());

    try {
      pageContext.getOut().println(wysiwygEditor.print());
    } catch (IOException e) {
      throw new JspException("WysiwygTag tag", e);
    }
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    return EVAL_BODY_INCLUDE;
  }


}