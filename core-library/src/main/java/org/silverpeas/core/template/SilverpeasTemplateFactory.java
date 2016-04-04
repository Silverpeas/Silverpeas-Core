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

package org.silverpeas.core.template;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class SilverpeasTemplateFactory {

  private static final String separator = "/";

  public static SilverpeasTemplate createSilverpeasTemplate(final Properties configuration) {
    return new SilverpeasStringTemplate(configuration);
  }

  public static SilverpeasTemplate createSilverpeasTemplateOnComponents() {
    return createSilverpeasTemplateOnComponents(null);
  }

  public static SilverpeasTemplate createSilverpeasTemplateOnComponents(final String pathSuffix) {
    final Properties config = new Properties();
    config.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR,
        computePath(SilverpeasStringTemplateUtil.defaultComponentsDir, pathSuffix));
    config.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR,
        computePath(SilverpeasStringTemplateUtil.customComponentsDir, pathSuffix));
    return createSilverpeasTemplate(config);
  }

  public static SilverpeasTemplate createSilverpeasTemplateOnCore(final String pathSuffix) {
    final Properties config = new Properties();
    config.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR,
        computePath(SilverpeasStringTemplateUtil.defaultCoreDir, pathSuffix));
    config.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR,
        computePath(SilverpeasStringTemplateUtil.customCoreDir, pathSuffix));
    return createSilverpeasTemplate(config);
  }

  private static String computePath(final String pathBase, final String pathSuffix) {
    final StringBuilder sb = new StringBuilder(pathBase);
    if (StringUtils.isNotBlank(pathSuffix)) {
      sb.append(separator);
      sb.append(pathSuffix);
      sb.append(separator);
    }
    return sb.toString().replaceAll("[/]{1,}", "/");
  }
}
