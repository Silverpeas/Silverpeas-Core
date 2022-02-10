/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.web.util;

import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.silverpeas.core.util.StringUtil.isLong;

/**
 * Configuration of the WYSIWYG editor. It depends on the implementation of such an editor.
 * Currently it depends on the CKEditor.
 * @author mmoquillon
 */
public class WysiwygEditorConfig {

  private static final SettingBundle DEFAULT_WYSIWYG_SETTINGS =
      ResourceLocator.getSettingBundle("org.silverpeas.wysiwyg.settings.wysiwygSettings");

  private SettingBundle wysiwygSettings;
  private String height = "500";
  private String width = "100%";
  private String language = I18NHelper.DEFAULT_LANGUAGE;
  private String toolbar = "Default";
  private boolean toolbarStartExpanded = true;
  private boolean fileBrowserDisplayed = true;
  private String skin = DEFAULT_WYSIWYG_SETTINGS.getString("skin", "");
  private String stylesheet;
  private String componentId;
  private String objectId;

  /**
   * Constructs a WYSIWYG editor configuration for the specified component. According to the
   * component, a peculiar configuration can be used for that component instead of default.
   * @param componentName the name of the component starting with a lower case for which the editor
   * has
   * to be initialized.
   */
  public WysiwygEditorConfig(final String componentName) {
    final Optional<SettingBundle> settings = StringUtil.isDefined(componentName) ?
        ResourceLocator.getOptionalSettingBundle(
            "org.silverpeas.wysiwyg.settings." + componentName + "Settings") : Optional.empty();
    this.wysiwygSettings = settings.orElse(DEFAULT_WYSIWYG_SETTINGS);
  }

  private WysiwygEditorConfig(final WysiwygEditorConfig config) {
    this.wysiwygSettings = config.wysiwygSettings;
    this.height = config.height;
    this.width = config.width;
    this.language = config.language;
    this.toolbar = config.toolbar;
    this.toolbarStartExpanded = config.toolbarStartExpanded;
    this.fileBrowserDisplayed = config.fileBrowserDisplayed;
    this.skin = config.skin;
    this.stylesheet = config.stylesheet;
    this.componentId = config.componentId;
    this.objectId = config.objectId;
  }

  /**
   * Gets a JSON representation of this configuration in order to be used in the Javascript land.
   * @return a JSON representation of this configuration.
   */
  public String toJSON() {
    return JSONCodec.encodeObject(builder -> {
      JSONCodec.JSONObject object = builder.put("height", getHeight())
          .put("width", getWidth())
          .put("language", getLanguage())
          .put("baseHref", getServerURL())
          .put("toolbarStartupExpanded", isToolbarStartExpanded())
          .put("customConfig", getConfigFile())
          .put("toolbar", getToolbar())
          .put("imagebank", true)
          .put("filebank", true)
          .put("silverpeasObjectId", getObjectId())
          .put("silverpeasComponentId", getComponentId());
      object = putFileBrowserUrls(object);
      object = putSkin(object);
      object = putCustomStylesheets(object);
      return object;
    });
  }

  private JSONCodec.JSONObject putCustomStylesheets(JSONCodec.JSONObject object) {
    JSONCodec.JSONObject json = object;
    List<String> stylesheets = getStylesheets();
    if (!stylesheets.isEmpty()) {
      json = object.putJSONArray("contentsCss", a -> a.addJSONArray(stylesheets));
    }
    return json;
  }

  private JSONCodec.JSONObject putSkin(JSONCodec.JSONObject object) {
    JSONCodec.JSONObject json = object;
    if (StringUtil.isDefined(getSkin())) {
      json = object.put("skin", getSkin());
    }
    return json;
  }

  private JSONCodec.JSONObject putFileBrowserUrls(JSONCodec.JSONObject object) {
    JSONCodec.JSONObject json = object;
    if (!isFileBrowserDisplayed()) {
      json = object.put("filebrowserImageBrowseUrl", "")
          .put("filebrowserFlashBrowseUrl", "")
          .put("filebrowserBrowseUrl", "")
          .put("imageUploadUrl", "");
    } else if (getComponentId() == null ||
        (getComponentId().startsWith(WysiwygController.WYSIWYG_WEBSITES) && isLong(objectId))) {
      json = object.put("imageUploadUrl", "");
    }
    return json;
  }

  /**
   * Gets the stylesheets to apply to the editor. By default, only the standard CSS of
   * Silverpeas is used except for some predefined Silverpeas applications for which either an
   * additional CSS is also used or no CSS has to be applied.
   * @return a list of stylesheets to apply to the editor.
   */
  public List<String> getStylesheets() {
    List<String> stylesheets = new ArrayList<>();
    boolean isStylesheetCanBeSet = !wysiwygSettings.getBoolean("noCss", false);
    if (isStylesheetCanBeSet) {
      stylesheets.add(URLUtil.addFingerprintVersionOn(URLUtil.getApplicationURL() + GraphicElementFactory.STANDARD_CSS));
      if (StringUtil.isDefined(stylesheet)) {
        stylesheets.add(URLUtil.addFingerprintVersionOn(stylesheet));
      }
    }
    return stylesheets;
  }

  /**
   * Gets the skin by default to use with the editor. It is a value coming from the settings in
   * {@code properties/org/silverpeas/wysiwyg/settings/wysiwygSettings.properties}.
   * @return the name of the skin to use.
   */
  public String getSkin() {
    return skin;
  }

  /**
   * Gets the path of the configuration file to use by the editor.
   * @return the path of the configuration file.
   */
  private String getConfigFile() {
    String configFile = wysiwygSettings.getString("configFile");
    if (!configFile.startsWith("/") && !configFile.toLowerCase().startsWith("http")) {
      configFile = URLUtil.getApplicationURL() + "/" + configFile;
    }
    return URLUtil.addFingerprintVersionOn(configFile);
  }

  /**
   * Gets the height of the edition area.
   * @return the edition area height.
   */
  public String getHeight() {
    return height;
  }

  /**
   * Sets the height of the edition area.
   * @param height the edition area height.
   */
  public void setHeight(final String height) {
    this.height = height;
  }

  /**
   * Gets the width of the edition area.
   * @return the edition area width.
   */
  public String getWidth() {
    return width;
  }

  /**
   * Sets the width of the edition area.
   * @param width the edition area width.
   */
  public void setWidth(final String width) {
    this.width = width;
  }

  /**
   * Gets the localization to use by the editor.
   * @return the localization code in ISO 631-1
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Sets the localization to use by the editor.
   * @param language the localization code in ISO 631-1
   */
  public void setLanguage(final String language) {
    this.language = language;
  }

  /**
   * Gets the name of the toolbar definition to use by the editor. By default "Default".
   * @return the toolbar definition name.
   */
  public String getToolbar() {
    return toolbar;
  }

  /**
   * Sets the name of the toolbar definition to use by the editor.
   * @param toolbar the toolbar definition name.
   */
  public void setToolbar(final String toolbar) {
    this.toolbar = toolbar;
  }

  /**
   * Sets a custom stylesheet to render the editor in a peculiar way. By default, the current
   * look stylesheet is used (that is no custom stylesheet is set).
   * @param stylesheet a custom stylesheet.
   */
  public void setStylesheet(final String stylesheet) {
    this.stylesheet = stylesheet;
  }

  /**
   * Is the editor spawn with its toolbar expanded? By default true.
   * @return true if the toolbar must be expanded at the editor startup, false otherwise.
   */
  private boolean isToolbarStartExpanded() {
    return toolbarStartExpanded;
  }

  /**
   * Indicates if the toolbar must be started with its toolbar expanded.
   * @param toolbarStartExpanded true if the toolbar must be expanded at the editor startup,
   * false otherwise.
   */
  public void setToolbarStartExpanded(final boolean toolbarStartExpanded) {
    this.toolbarStartExpanded = toolbarStartExpanded;
  }

  /**
   * Gets the URL of the server in which is running Silverpeas. Either it is taken by the Silverpeas
   * configuration or it is computed from the current HTTP request.
   * @return the URL of the server.
   */
  public String getServerURL() {
    return ResourceLocator.getGeneralSettingBundle()
        .getString("httpServerBase", URLUtil.getCurrentServerURL());
  }

  /**
   * Is the file browser must be displayed in the editor? By default true.
   * @return true if it has been displayed, false otherwise.
   */
  private boolean isFileBrowserDisplayed() {
    return fileBrowserDisplayed;
  }

  /**
   * Indicates if the file browser must be displayed in the editor.
   * @param fileBrowserDisplayed true if it has been displayed, false otherwise.
   */
  public void setFileBrowserDisplayed(final boolean fileBrowserDisplayed) {
    this.fileBrowserDisplayed = fileBrowserDisplayed;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(final String componentId) {
    this.componentId = componentId;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(final String objectId) {
    this.objectId = objectId;
  }

  public WysiwygEditorConfig copy() {
    WysiwygEditorConfig clone = new WysiwygEditorConfig(this);
    clone.wysiwygSettings = this.wysiwygSettings;
    return clone;
  }
}
