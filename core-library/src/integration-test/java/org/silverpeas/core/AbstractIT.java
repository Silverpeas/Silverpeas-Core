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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Rule;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author mmoquillon
 */
public abstract class AbstractIT {

  private static Properties mavenProperties = new Properties();

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("/create_database.sql");

  protected static WebArchive createTestArchive(final Class<?> testClass) {
    loadMavenProperties(testClass);
    Path buildDir = getBuildDirPath();
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war");
    List<String> mavenDependencies = Arrays.asList("com.ninja-squad:DbSetup",
        "org.apache.commons:commons-lang3",
        "commons-codec:commons-codec", "commons-io:commons-io",
        "org.silverpeas.core:silverpeas-core-test");
    // Add Maven dependencies. Compile and runtime
    File[] libs = Maven.resolver()
        .loadPomFromFile("pom.xml")
        .importCompileAndRuntimeDependencies()
        .resolve(mavenDependencies)
        .withTransitivity()
        .asFile();
    archive.addAsLibraries(libs);
    // Add all regular source classes from the given package
    archive.addPackage(testClass.getPackage());
    archive.addAsResource("META-INF/services/test-org.silverpeas.core.util.BeanContainer",
        "META-INF/services/org.silverpeas.core.util.BeanContainer");
    archive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    //archive.addAsWebInfResource("test-ds.xml", "test-ds.xml");
    // Resources
    archive.addAsResource("maven.properties");
    archive.addAsResource(
        "META-INF/services/test-org.silverpeas.core.util.logging.SilverLoggerFactory",
        "META-INF/services/org.silverpeas.core.util.logging.SilverLoggerFactory");
    // Add all files from target/classes
    archive.addAsResource(buildDir.resolve("classes").toFile(), "");

    setUpPersistence(testClass, archive, libs);

    return archive;
  }

  private static void setUpPersistence(final Class<?> testClass, final WebArchive archive,
      final File[] libs) {
    Set<String> jarLibsForPersistence = Stream.of(libs)
        .filter(l -> l.getName().startsWith("silverpeas-") && l.getName().endsWith(".jar"))
        .map(l -> "<jar-file>lib/" + l.getName() + "</jar-file>")
        .collect(Collectors.toSet());
    if (!jarLibsForPersistence.isEmpty()) {
      try (InputStream is = AbstractIT.class.getResourceAsStream(
          "/META-INF/core-test-persistence.xml")) {
        String persistenceXmlTemplateContent = IOUtils.toString(is, StandardCharsets.UTF_8);
        File persistenceXml = FileUtils.getFile(
            testClass.getProtectionDomain().getCodeSource().getLocation().getFile(), "META-INF",
            "dynamic-test-persistence.xml");
        String persistenceXmlContent = persistenceXmlTemplateContent.replace("<!-- @JAR_FILES@ -->",
            String.join("\n", jarLibsForPersistence));
        FileUtils.writeStringToFile(persistenceXml, persistenceXmlContent, StandardCharsets.UTF_8);
        archive.addAsResource("META-INF/" + persistenceXml.getName(), "META-INF/persistence.xml");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      archive.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml");
    }
  }

  private static Properties loadMavenProperties(Class<?> testClass) {
    try (InputStream is = testClass.getResourceAsStream("/maven.properties")) {
      mavenProperties.load(is);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return mavenProperties;
  }

  protected static Path getBuildDirPath() {
    final String buildDirKey = "project.build.directory";
    String buildDir = mavenProperties.getProperty(buildDirKey);
    if (StringUtil.isDefined(buildDir)) {
      return Paths.get(buildDir);
    } else {
      throw new RuntimeException(String.format("No such property %s", buildDirKey));
    }
  }
}
  