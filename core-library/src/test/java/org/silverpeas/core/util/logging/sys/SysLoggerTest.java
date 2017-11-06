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
package org.silverpeas.core.util.logging.sys;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit test on the SysLogger implementation of Logger.
 * @author miguel
 */
public class SysLoggerTest {

  private static String LOGGER_NAMESPACE = "silverpeas.test";

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectory = new MavenTargetDirectoryRule(this);

  @Before
  public void initEnvVariables() throws Exception {
    SystemWrapper.get()
        .getenv()
        .put("SILVERPEAS_HOME", mavenTargetDirectory.getResourceTestDirFile().getPath());
    // to clean up all the weaker references to the yet loaded loaders (to force to load them from
    // their configuration)
    Thread.sleep(1000);
    System.gc();
  }

  @Test
  public void getALogger() {
    Logger.getLogger(getClass().getSimpleName()).info("get a logger");
    SilverLogger logger = new SysLogger(LOGGER_NAMESPACE);
    assertThat(logger.getNamespace(), is(LOGGER_NAMESPACE));
    assertThat(logger.getLevel(), notNullValue());
    assertThat(logger.getLevel(), is(Level.INFO));
  }

  @Test
  public void changeLoggerLevel() {
    Logger.getLogger(getClass().getSimpleName()).info("change logger level");
    final String namespace = LOGGER_NAMESPACE + ".change";
    SilverLogger logger = new SysLogger(namespace);
    assertThat(logger.getNamespace(), is(namespace));
    assertThat(logger.getNamespace(), not(Level.DEBUG));
    logger.setLevel(Level.DEBUG);
    assertThat(logger.getLevel(), is(Level.DEBUG));
  }
}
