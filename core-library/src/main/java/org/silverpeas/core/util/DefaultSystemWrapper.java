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
package org.silverpeas.core.util;

import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.util.SystemWrapper;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation that is nothing more than a delegate of {@link System} class.
 *
 * @author Yohann Chastagnier
 */
public class DefaultSystemWrapper implements SystemWrapper {

  private void loadSystemSettings() {
    // we don't use the ResourceLocator API here in order to avoid cyclic dependency between
    // SystemWrapper and ResourceLocator
    Properties systemSettings = new Properties();
    Path systemSettingsPath =
        Paths.get(System.getenv("SILVERPEAS_HOME"), "properties", "org", "silverpeas",
            "systemSettings.properties");
    if (!Files.exists(systemSettingsPath)) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING,
          "System settings file not found at {0}", systemSettingsPath);
      return;
    }
    try (FileReader reader = new FileReader(systemSettingsPath.toFile())) {
      systemSettings.load(reader);
    } catch (IOException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
    systemSettings.stringPropertyNames()
        .forEach(key -> setProperty(key, systemSettings.getProperty(key)));
  }

  public DefaultSystemWrapper() {
    loadSystemSettings();
  }
}
