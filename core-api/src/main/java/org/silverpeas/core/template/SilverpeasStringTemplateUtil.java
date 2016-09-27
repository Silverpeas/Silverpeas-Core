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

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SilverpeasStringTemplateUtil {

  public final static String defaultComponentsDir;
  public final static String customComponentsDir;
  public final static String defaultCoreDir;
  public final static String customCoreDir;

  static {
    SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.util.stringtemplate");
    defaultComponentsDir = settings.getString("template.dir.components.default");
    customComponentsDir = settings.getString("template.dir.components.custom");
    defaultCoreDir = settings.getString("template.dir.core.default");
    customCoreDir = settings.getString("template.dir.core.custom");
  }

  /**
   * Is the specified template exists in the component templates home directory.
   * @param componentId the unique identifier of the component for which the template should exist.
   * @param template the name of the template to check the existence.
   * @return true if  the specified template exist for the specified Silverpeas component in the
   * component templates home directory. False otherwise.
   */
  public static boolean isComponentTemplateExist(final String componentId, final String template) {
    return Files.exists(Paths.get(defaultComponentsDir, componentId, template)) ||
        Files.exists(Paths.get(customComponentsDir, componentId, template));
  }
}
