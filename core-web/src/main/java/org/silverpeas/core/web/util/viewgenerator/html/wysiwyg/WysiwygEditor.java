/*
 * Copyright (C) 2000 - 2017 Silverpeas
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

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.WysiwygEditorConfig;

public class WysiwygEditor {

  private String replace;
  private WysiwygEditorConfig config;

  public WysiwygEditor(final String componentInstanceId) {
    String componentName = "";
    if (StringUtil.isDefined(componentInstanceId)) {
      ComponentInstLight componentInst =
          OrganizationController.get().getComponentInstLight(componentInstanceId);
      if (componentInst != null) {
        componentName = componentInst.getName();
      }
    }
    this.config = new WysiwygEditorConfig(componentName);
  }

  /**
   * Prints out the Javascript instruction to display the WYSIWYG editor.
   * @return the String representation of the Javascript instruction.
   */
  public String print() {
    StringBuilder builder = new StringBuilder(100);
    builder.append("CKEDITOR.replace('")
        .append(getReplace())
        .append("', ")
        .append(this.config.toJSON())
        .append(");\n");
    return builder.toString();
  }

  public String getLanguage() {
    return this.config.getLanguage();
  }

  public String getReplace() {
    return replace;
  }

  public void setReplace(String replace) {
    this.replace = replace;
  }

  public void setHeight(String height) {
    this.config.setHeight(height);
  }

  public void setWidth(String width) {
    this.config.setWidth(width);
  }

  public void setLanguage(String language) {
    this.config.setLanguage(language);
  }

  public void setToolbar(String toolbar) {
    this.config.setToolbar(toolbar);
  }

  public void setToolbarStartExpanded(boolean toolbarStartExpanded) {
    this.config.setToolbarStartExpanded(toolbarStartExpanded);
  }

  public void setCustomCSS(String css) {
    this.config.setStylesheet(css);
  }

  public void setDisplayFileBrowser(boolean displayFileBrowser) {
    this.config.setFileBrowserDisplayed(displayFileBrowser);
  }
}
