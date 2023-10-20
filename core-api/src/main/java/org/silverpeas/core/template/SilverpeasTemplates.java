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
package org.silverpeas.core.template;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class SilverpeasTemplates {

  static final String DEFAULT_COMPONENTS_DIR;
  static final String CUSTOM_COMPONENTS_DIR;
  static final String DEFAULT_CORE_DIR;
  static final String CUSTOM_CORE_DIR;
  private static final String SEPARATOR = "/";

  static {
    SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.util.stringtemplate");
    DEFAULT_COMPONENTS_DIR = settings.getString("template.dir.components.default");
    CUSTOM_COMPONENTS_DIR = settings.getString("template.dir.components.custom");
    DEFAULT_CORE_DIR = settings.getString("template.dir.core.default");
    CUSTOM_CORE_DIR = settings.getString("template.dir.core.custom");
  }

  private SilverpeasTemplates() {
  }

  /**
   * Is the specified template exists in the component templates home directory.
   * @param rootPath the root path into components of the template file.
   * @param template the name of the template to check the existence (without the language and
   * without the extension).
   * @return true if  the specified template exist for the specified Silverpeas component in the
   * component templates home directory. False otherwise.
   */
  public static boolean isComponentTemplateExist(final String rootPath, final String template) {
    String templateFileName = template + "_" + DisplayI18NHelper.getDefaultLanguage() + ".st";
    return Files.exists(Paths.get(DEFAULT_COMPONENTS_DIR, rootPath, templateFileName)) ||
        Files.exists(Paths.get(CUSTOM_COMPONENTS_DIR, rootPath, templateFileName));
  }

  public static SilverpeasTemplate createSilverpeasTemplate(final Properties configuration) {
    return new SilverpeasStringTemplate(configuration);
  }

  public static SilverpeasTemplate createSilverpeasTemplateOnComponents() {
    return createSilverpeasTemplateOnComponents(null);
  }

  public static SilverpeasTemplate createSilverpeasTemplateOnComponents(final String pathSuffix) {
    final Properties config = new Properties();
    config.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR,
        computePath(DEFAULT_COMPONENTS_DIR, pathSuffix));
    config.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR,
        computePath(CUSTOM_COMPONENTS_DIR, pathSuffix));
    return createSilverpeasTemplate(config);
  }

  public static SilverpeasTemplate createSilverpeasTemplateOnCore(final String pathSuffix) {
    final Properties config = new Properties();
    config.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR,
        computePath(DEFAULT_CORE_DIR, pathSuffix));
    config.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR,
        computePath(CUSTOM_CORE_DIR, pathSuffix));
    return createSilverpeasTemplate(config);
  }

  private static String computePath(final String pathBase, final String pathSuffix) {
    final StringBuilder sb = new StringBuilder(pathBase);
    if (StringUtils.isNotBlank(pathSuffix)) {
      sb.append(SEPARATOR);
      sb.append(pathSuffix);
      sb.append(SEPARATOR);
    }
    return sb.toString().replaceAll("/+", "/");
  }
}
