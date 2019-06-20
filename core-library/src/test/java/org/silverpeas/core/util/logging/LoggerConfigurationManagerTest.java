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
package org.silverpeas.core.util.logging;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestedBean;
import org.silverpeas.core.util.logging.LoggerConfigurationManager.LoggerConfiguration;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Unit test on the loading of the logging configurations (stored in properties files).
 * @author miguel
 */
@EnableSilverTestEnv
public class LoggerConfigurationManagerTest {

  @TestedBean
  private LoggerConfigurationManager manager;

  @Test
  public void allConfigurationFilesAreLoaded() {
    manager = LoggerConfigurationManager.get();
    assertThat(manager.getLoggerConfigurations().size(), is(5));
  }

  @Test
  public void getLoggerConfigurationWithoutLevelOfAnExistingNamespace() {
    final String namespace = "silverpeas.core.contribution.attachment";
    manager = LoggerConfigurationManager.get();
    LoggerConfiguration configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getNamespace(), is(namespace));
    assertThat(configuration.getLevel(), is(nullValue()));
    assertThat(configuration.hasConfigurationFile(), is(true));
  }

  @Test
  public void getLoggerConfigurationWithLevelOfAnExistingNamespace() {
    final String namespace = "silverpeas.core.security.authentication";
    manager = LoggerConfigurationManager.get();
    LoggerConfiguration configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getNamespace(), is(namespace));
    assertThat(configuration.getLevel(), is(Level.ERROR));
    assertThat(configuration.hasConfigurationFile(), is(true));
  }

  @Test
  public void getLoggerConfigurationOfANonExistingNamespace() {
    final String namespace = "org.tartempion.toto";
    manager = LoggerConfigurationManager.get();
    LoggerConfiguration configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getNamespace(), is(namespace));
    assertThat(configuration.getLevel(), is(nullValue()));
    assertThat(configuration.hasConfigurationFile(), is(false));
  }

  @Test
  public void updateLoggerConfigurationOfAnExistingNamespacePersistsTheChange() {
    final String namespace = "silverpeas.core.calendar";
    final Level level = Level.DEBUG;
    manager = LoggerConfigurationManager.get();
    LoggerConfiguration configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration, not(nullValue()));

    // update configuration and ensures the change is done
    configuration.setLevel(level);
    manager.saveLoggerConfiguration(configuration);
    configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration.getLevel(), is(level));

    // reload configuration files to ensure the configuration was really persisted
    manager.loadAllConfigurationFiles();
    configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration.getLevel(), is(level));
  }

  @Test
  public void updateLoggerConfigurationOfAnUnexistingNamespaceDoesNothing() {
    final String namespace = "org.tartempion.toto";
    final Level level = Level.DEBUG;
    manager = LoggerConfigurationManager.get();
    LoggerConfiguration configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getLevel(), is(nullValue()));
    assertThat(configuration.hasConfigurationFile(), is(false));

    // the logger is changed
    configuration.setLevel(level);
    manager.saveLoggerConfiguration(configuration);
    configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration.getLevel(), is(level));
    assertThat(configuration.hasConfigurationFile(), is(false));

    // but the change isn't persisted as the logger has no config file
    manager.loadAllConfigurationFiles();
    configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration.getLevel(), is(nullValue()));
    assertThat(configuration.hasConfigurationFile(), is(false));
  }
}
