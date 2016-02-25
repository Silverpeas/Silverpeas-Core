/**
 * Copyright (C) 2000 - 2015 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.util.logging;

import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.test.rule.CommonAPI4Test;
import org.silverpeas.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.util.lang.SystemWrapper;
import org.silverpeas.util.logging.LoggerConfigurationManager.LoggerConfiguration;

import javax.inject.Inject;
import javax.inject.Provider;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Unit test on the loading of the logging configurations (stored in properties files).
 * @author miguel
 */
@RunWith(CdiRunner.class)
public class LoggerConfigurationManagerTest {

  private CommonAPI4Test commonAPI4Test = new CommonAPI4Test();
  private MavenTargetDirectoryRule mavenTargetDirectory = new MavenTargetDirectoryRule(this);

  @Rule
  public CommonAPI4Test getCommonAPI4Test() {
    return commonAPI4Test;
  }

  @Rule
  public MavenTargetDirectoryRule getMavenTargetDirectory() {
    return mavenTargetDirectory;
  }

  @Inject
  private Provider<LoggerConfigurationManager> managerProvider;

  private LoggerConfigurationManager manager;

  @Before
  public void initEnvVariables() {
    SystemWrapper.get()
        .getenv()
        .put("SILVERPEAS_HOME", mavenTargetDirectory.getResourceTestDirFile().getPath());
    System.out.println("INJECT THE TRUE LOGGER CONFIGURATION MANAGER");
    commonAPI4Test.injectIntoMockedBeanContainer(managerProvider.get());
  }

  @Test
  public void allConfigurationFilesAreLoaded() {
    manager = LoggerConfigurationManager.get();
    assertThat(manager.getLoggerConfigurationsByModule().size(), is(4));
    assertThat(manager.getLoggerConfigurationsByLogger().size(), is(4));
  }

  @Test
  public void getLoggerConfigurationWithoutLevelOfAnExistingModule() {
    final String module = "attachment";
    manager = LoggerConfigurationManager.get();
    LoggerConfiguration configuration = manager.getLoggerConfiguration(module);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getModuleName(), is(module));
    assertThat(configuration.getNamespace(), is("Silverpeas.Bus.attachment"));
    assertThat(configuration.getLevel(), is(nullValue()));
  }

  @Test
  public void getLoggerConfigurationWithoutLevelOfAnExistingNamespace() {
    final String namespace = "Silverpeas.Bus.attachment";
    manager = LoggerConfigurationManager.get();
    LoggerConfiguration configuration = manager.getLoggerConfiguration(namespace);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getModuleName(), is("attachment"));
    assertThat(configuration.getNamespace(), is(namespace));
    assertThat(configuration.getLevel(), is(nullValue()));
  }

  @Test
  public void getLoggerConfigurationWithLevelOfAnExistingModule() {
    final String module = "authentication";
    manager = LoggerConfigurationManager.get();
    LoggerConfiguration configuration = manager.getLoggerConfiguration(module);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getModuleName(), is(module));
    assertThat(configuration.getNamespace(), is("Silverpeas.Bus.Authentication"));
    assertThat(configuration.getLevel(), is(Level.ERROR));
  }

  @Test
  public void getLoggerConfigurationOfANonExistingModuleOrNamespace() {
    final String module = "toto";
    manager = LoggerConfigurationManager.get();
    LoggerConfiguration configuration = manager.getLoggerConfiguration(module);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getModuleName(), is(module));
    assertThat(configuration.getNamespace(), is("Silverpeas.Other.toto"));
    assertThat(configuration.getLevel(), is(nullValue()));
  }

  @Test
  public void updateLoggerConfigurationOfAnExistingModulePersistsTheChange() {
    final String module = "calendar";
    final Level level = Level.DEBUG;
    manager = LoggerConfigurationManager.get();
    LoggerConfiguration configuration = manager.getLoggerConfiguration(module);
    assertThat(configuration, not(nullValue()));

    // update configuration and ensures the change is done
    configuration.setLevel(level);
    manager.saveLoggerConfiguration(configuration);
    configuration = manager.getLoggerConfiguration(module);
    assertThat(configuration.getLevel(), is(level));

    // reload configuration files to ensure the configuration was really persisted
    manager.loadAllConfigurationFiles();
    configuration = manager.getLoggerConfiguration(module);
    assertThat(configuration.getLevel(), is(level));
  }

  @Test
  public void updateLoggerConfigurationOfAnUnexistingModuleDoesNothing() {
    final String module = "toto";
    final Level level = Level.DEBUG;
    manager = LoggerConfigurationManager.get();
    LoggerConfiguration configuration = manager.getLoggerConfiguration(module);
    assertThat(configuration, not(nullValue()));
    assertThat(configuration.getLevel(), is(nullValue()));

    // the configuration cannot be persisted as no such module exist and hence no change can be
    // done.
    configuration.setLevel(level);
    manager.saveLoggerConfiguration(configuration);
    configuration = manager.getLoggerConfiguration(module);
    assertThat(configuration.getLevel(), is(nullValue()));
  }
}
