/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describin
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.cmis;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The CMIS settings to customize some properties of the implementation of CMIS by Silverpeas.
 * @author mmoquillon
 */
@Bean
@Singleton
public class SilverpeasCmisSettings {

  private static final String REPO_ID = "cmis.repository.id";
  private static final String REPO_NAME = "cmis.repository.name";
  private static final String REPO_DESC = "cmis.repository.description";

  private final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.cmis.cmisSettings");
  private final Properties defaultSettings = new Properties();

  private SilverpeasCmisSettings() {
  }

  public static SilverpeasCmisSettings get() {
    return ServiceProvider.getService(SilverpeasCmisSettings.class);
  }

  @PostConstruct
  private void load() {
    final InputStream stream = getClass().getResourceAsStream("/repository.properties");
    try {
      defaultSettings.load(stream);
    } catch (IOException e) {
      SilverLogger.getLogger(this)
          .error("Unable to load the cmis-repository.properties CMIS configuration file", e);
    }
  }

  /**
   * Gets the unique identifier of the CMIS repository in Silverpeas. Should be unique among all
   * of the customers of Silverpeas.
   * @return the unique identifier of the CMIS repository.
   */
  public String getRepositoryId() {
    return settings.getString(REPO_ID, defaultSettings.getProperty(REPO_ID));
  }

  /**
   * Gets the name of the CMIS repository in Silverpeas.
   * @return the repository's name.
   */
  public String getRepositoryName() {
    return settings.getString(REPO_NAME, defaultSettings.getProperty(REPO_NAME));
  }

  /**
   * Gets a short description of the CMIS repository in Silverpeas.
   * @return a short description of the repository.
   */
  public String getRepositoryDescription() {
    return settings.getString(REPO_DESC, defaultSettings.getProperty(REPO_DESC));
  }

}
  