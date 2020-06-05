/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.contribution.converter;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.silverpeas.core.contribution.converter.openoffice.OpenOfficeService;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.ServiceProvider;

import java.io.File;
import java.nio.file.Paths;

/**
 * Abstract class that prepares all the resources required by the integration tests on the
 * Conversion API.
 * @author mmoquillon
 */
public class AbstractConverterIntegrationTest {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);
  public File document;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(AbstractConverterIntegrationTest.class)
        .addCommonBasicUtilities()
        .addSilverpeasExceptionBases()
        .addFileRepositoryFeatures()
        .addMavenDependencies("org.apache.commons:commons-exec")
        .testFocusedOn(warBuilder ->
            warBuilder.addMavenDependencies("org.jodconverter:jodconverter-local")
                .addPackages(true, "org.silverpeas.core.contribution.converter")
                .addAsResource("org/silverpeas/converter"))
        .build();
  }

  protected File getDocumentNamed(String name) throws Exception {
    File resourceTestDir = mavenTargetDirectoryRule.getResourceTestDirFile();
    return Paths.get(resourceTestDir.getPath(), "org", "silverpeas", "core", "contribution",
        "converter", name).toFile();
  }

  @Before
  public void startOpenOfficeService() throws Exception {
    OpenOfficeService service = ServiceProvider.getService(OpenOfficeService.class);
    service.init();
  }

  @After
  public void stopOpenOfficeService() throws Exception {
    OpenOfficeService service = ServiceProvider.getService(OpenOfficeService.class);
    service.release();
  }
}
