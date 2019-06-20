/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.WysiwygEditorConfig;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.silverpeas.core.cache.service.VolatileCacheServiceProvider
    .getSessionVolatileResourceCacheService;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.StringUtil.isLong;

public class WysiwygEditor {

  private String replace;
  private WysiwygEditorConfig config;
  private boolean activateWysiwygBackupManager;
  private String componentInstanceId;
  private String resourceType;
  private String resourceId;
  private static final String DD_UPLOAD_TEMPLATE_SCRIPT =
      "whenSilverpeasReady(function() '{'" +
        "configureCkEditorDdUpload('{'" +
          "componentInstanceId : ''{0}''," +
          "resourceId : ''{1}'', " +
          "indexIt : {2}" +
        "'}');" +
      "'}');\n";

  public WysiwygEditor(final String componentInstanceId, final String resourceType,
      final String resourceId, final boolean activateWysiwygBackupManager) {
    this.componentInstanceId = componentInstanceId;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.activateWysiwygBackupManager = activateWysiwygBackupManager;
    String componentName = "";
    if (StringUtil.isDefined(componentInstanceId)) {
      ComponentInstLight componentInst =
          OrganizationController.get().getComponentInstLight(componentInstanceId);
      if (componentInst != null) {
        componentName = componentInst.getName();
      }
    }
    this.config = new WysiwygEditorConfig(componentName);
    this.config.setComponentId(componentInstanceId);
    this.config.setObjectId(resourceId);
  }

  /**
   * Prints out the Javascript instruction to display the WYSIWYG editor.
   * @return the String representation of the Javascript instruction.
   */
  public String print() {
    // Be careful to not change the first Javascript line as some JSP are initializing editor
    // variables (for example: var editor = <view:wysiwyg .../>).
    String js = "CKEDITOR.replace('" + getReplace() + "', " + this.config.toJSON() + ");\n";
    js += "sp.editor.wysiwyg.fullScreenOnMaximize('" + getReplace() + "');\n";
    if (activateWysiwygBackupManager) {
      final boolean notVolatileId =
          !getSessionVolatileResourceCacheService().contains(resourceId, componentInstanceId);
      js += "sp.editor.wysiwyg.backupManager(" + JSONCodec.encodeObject(o -> {
        o.put("componentInstanceId", defaultStringIfNotDefined(componentInstanceId));
        o.put("resourceType", defaultStringIfNotDefined(resourceType));
        o.put("resourceId", defaultStringIfNotDefined(notVolatileId ? resourceId : EMPTY));
        return o;
      }) + ");\n";
    }
    if (componentInstanceId != null &&
        !(componentInstanceId.startsWith(WysiwygController.WYSIWYG_WEBSITES) && isLong(resourceId))) {
      js += format(DD_UPLOAD_TEMPLATE_SCRIPT, componentInstanceId, resourceId, false);
    }
    return js;
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
