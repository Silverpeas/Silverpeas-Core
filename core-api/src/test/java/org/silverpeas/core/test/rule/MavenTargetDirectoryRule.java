/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.test.rule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.silverpeas.core.test.util.SilverProperties;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.fail;

/**
 * This rule permits to get some technical path information about maven target directory during a
 * test. This is useful for treatments that manipulates file creation, deletion, etc.
 * <p>
 * The resource {@code maven.properties} has to be in the test resource path in order to make
 * this rule functional.
 * @author Yohann Chastagnier
 */
public class MavenTargetDirectoryRule implements TestRule {


  private SilverProperties mavenProperties;

  private Class testInstanceClass;

  /**
   * Gets the silverpeas version.
   * @return the silverpeas version.
   */
  public String getSilverpeasVersion() {
    return getSilverpeasVersion(getMavenProperties());
  }

  /**
   * Gets the target directory of the test.
   * @return the target directory of the test.
   */
  public File getBuildDirFile() {
    return getBuildDirFile(getMavenProperties());
  }

  /**
   * Gets the resource path of test execution context.
   * @return the resource path.
   */
  public File getWildflyHomeFile() {
    return getWildflyHomeFile(getMavenProperties());
  }

  /**
   * Gets the path of the Wildfly home directory.
   * @return the Wildfly home directory.
   */
  public File getResourceTestDirFile() {
    return getResourceTestDirFile(getMavenProperties());
  }

  /**
   * Mandatory constructor.
   * @param testInstance the instance of the current test in order to use class loader mechanism in
   * right context.
   */
  public MavenTargetDirectoryRule(Object testInstance) {
    this(testInstance.getClass());
  }

  /**
   * Mandatory constructor.
   * @param testInstanceClass the instance class of the current test in order to use class loader
   * mechanism in right context.
   */
  public MavenTargetDirectoryRule(Class<?> testInstanceClass) {
    this.testInstanceClass = testInstanceClass;
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        base.evaluate();
      }
    };
  }

  /**
   * Gets a {@link File} instance according to the given proeprty key.
   * @param key the key from which the path value will be retrieved.
   * @return the aimed {@link File} instance.
   */
  private static File getPath(Properties mavenProperties, String key) {
    return new File(getMavenProperty(mavenProperties, key));
  }

  /**
   * Gets a property from the maven.properties file.
   * @return the value related to the given key.
   */
  private Properties getMavenProperties() {
    if (mavenProperties == null) {
      mavenProperties = loadPropertiesForTestClass(testInstanceClass);
    }
    return mavenProperties;
  }

  /**
   * Gets a property from the maven.properties file.
   * @param key the key of the property.
   * @return the value related to the given key.
   */
  private static String getMavenProperty(Properties mavenProperties, String key) {
    String mavenPropertyValue = mavenProperties.getProperty(key, null);
    if (mavenPropertyValue == null) {
      fail("The maven.properties file from resource tests is not complete...");
    }
    return mavenPropertyValue;
  }

  /**
   * Loads maven properties from a test class.<br/>
   * maven.properties has to exist into resources of the project.
   * @param testClass the test class for which the maven properties are requested.
   * @return an instance of {@link Properties} that containes requested maven properties.
   */
  public static SilverProperties loadPropertiesForTestClass(Class testClass) {
    SilverProperties mavenProperties = SilverProperties.load(testClass);
    try (InputStream is = testClass.getClassLoader().getResourceAsStream("maven.properties")) {
      Logger.getLogger(testClass.getName()).info("Reading maven properties from " + testClass);
      mavenProperties.load(is);
      Logger.getLogger(testClass.getName()).info("Content is:\n" + mavenProperties.toString());
    } catch (Exception ex) {
      Logger.getLogger(testClass.getName()).log(Level.SEVERE, "Class " + testClass, ex);
    }
    return mavenProperties;
  }

  /**
   * Gets the silverpeas version.
   * @return the silverpeas version.
   */
  public static String getSilverpeasVersion(Properties mavenProperties) {
    return getMavenProperty(mavenProperties, "silverpeas.version");
  }

  /**
   * Gets the target directory of the test.
   * @return the target directory of the test.
   */
  public static File getBuildDirFile(Properties mavenProperties) {
    return getPath(mavenProperties, "project.build.directory");
  }

  /**
   * Gets the resource path of test execution context.
   * @return the resource path.
   */
  public static File getResourceTestDirFile(Properties mavenProperties) {
    return getPath(mavenProperties, "test-resources.directory");
  }

  /**
   * Gets the path of the Wildfly home directory.
   * @param mavenProperties
   * @return the Wildfly home directory.
   */
  public static File getWildflyHomeFile(Properties mavenProperties) {
    return getPath(mavenProperties, "wildfly.home");
  }
}
