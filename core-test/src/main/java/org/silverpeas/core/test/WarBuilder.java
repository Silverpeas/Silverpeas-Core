/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.asset.AssetUtil;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.test.integration.SilverpeasLoggerInitializationListener;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.Charsets;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * This class permits to setup an integration test
 * @author Yohann Chastagnier
 */
public abstract class WarBuilder<T extends WarBuilder<T>>
    implements Builder<WebArchive>, CommonWebArchive<T> {

  /**
   * Path to the WEB-INF inside of the Archive.
   */
  private static final ArchivePath PATH_WEB_INF = ArchivePaths.create("WEB-INF");

  /**
   * Path to the classes inside of the Archive.
   */
  private static final ArchivePath PATH_CLASSES = ArchivePaths.create(PATH_WEB_INF, "classes");

  protected Collection<String> mavenDependencies = new HashSet<>(Arrays
      .asList("com.ninja-squad:DbSetup", "org.apache.commons:commons-lang3",
          "commons-codec:commons-codec", "commons-io:commons-io",
          "org.silverpeas.core:silverpeas-core-test"));

  protected Collection<String> jarLibForPersistence = new HashSet<>();

  protected Collection<String> webParts = new HashSet<>();

  private final WebArchive war;

  private final Class<?> classOfTest;
  private final MavenTargetDirectoryRule testCoreClassMavenTargetDirectoryRule;

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param classOfTest the class of the test for which a war archive will be build.
   * @param <U> the type of the test.
   */
  protected <U> WarBuilder(Class<U> classOfTest) {
    this.war = ShrinkWrap.create(WebArchive.class, "test-" + classOfTest.getSimpleName() + ".war");
    this.classOfTest = classOfTest;
    logInfo("Building archive for " + classOfTest.getSimpleName());
    testCoreClassMavenTargetDirectoryRule = new MavenTargetDirectoryRule(WarBuilder.class);
    String resourcePath = classOfTest.getPackage().getName().replace('.', '/');
    logInfo("Adding resources from path: " + resourcePath);
    war.addAsResource(resourcePath);
    war.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
    logInfo("Adding initialization listener");
    addWebListener(SilverpeasLoggerInitializationListener.class);
  }

  /**
   * Creates a maven dependencies (when not declared in pom.xml of current project).
   * @param mavenDependencies the canonical maven dependencies to add.
   * @return the instance of the configurator.
   */
  @SuppressWarnings("unchecked")
  public WarBuilder<T> createMavenDependencies(String... mavenDependencies) {
    for (String mavenDependency : mavenDependencies) {
      addMavenDependencies(
          mavenDependency + ":" + testCoreClassMavenTargetDirectoryRule.getSilverpeasVersion());
    }
    return this;
  }

  /**
   * Creates a maven dependencies (when not declared in pom.xml of current project).
   * @param mavenDependencies the canonical maven dependencies to add.
   * @return the instance of the configurator.
   */
  @SuppressWarnings("unchecked")
  public WarBuilder<T> createMavenDependenciesWithPersistence(String... mavenDependencies) {
    for (String mavenDependency : mavenDependencies) {
      addMavenDependenciesWithPersistence(
          mavenDependency + ":" + testCoreClassMavenTargetDirectoryRule.getSilverpeasVersion());
    }
    return this;
  }

  /**
   * Adds maven dependencies.
   * @param mavenDependencies the canonical maven dependencies to add.
   * @return the instance of the configurator.
   */
  @SuppressWarnings("unchecked")
  public WarBuilder<T> addMavenDependencies(String... mavenDependencies) {
    Collections.addAll(this.mavenDependencies, mavenDependencies);
    return this;
  }

  /**
   * Adds maven dependencies.
   * @param mavenDependencies the canonical maven dependencies to add.
   * @return the instance of the configurator.
   */
  @SuppressWarnings("unchecked")
  public WarBuilder<T> addMavenDependenciesWithPersistence(String... mavenDependencies) {
    addMavenDependencies(mavenDependencies);
    for (String mavenDependency : mavenDependencies) {
      String jarLib = "lib/" + mavenDependency.split(":")[1] + "-" +
          testCoreClassMavenTargetDirectoryRule.getSilverpeasVersion() +
          ".jar";
      logInfo("Adding persistence reference for: " + jarLib);
      jarLibForPersistence.add("<jar-file>" + jarLib + "</jar-file>");
    }
    return this;
  }

  /**
   * Adds web listener.
   * @param webListenerClass the class of the web listener to add.
   * @return the instance of the configurator.
   */
  @SuppressWarnings("unchecked")
  public WarBuilder<T> addWebListener(Class<? extends EventListener> webListenerClass) {
    war.addClass(webListenerClass);
    webParts.add(
        "<listener><listener-class>" + webListenerClass.getName() + "</listener-class></listener>");
    return this;
  }

  @Override
  public boolean contains(final Class<?> aClass) throws IllegalArgumentException {
    return war.contains(new BasicPath(PATH_CLASSES, AssetUtil.getFullPathForClassResource(aClass)));
  }

  @Override
  public boolean contains(final String path) throws IllegalArgumentException {
    return war.contains(path);
  }

  @Override
  public WarBuilder<T> deleteClasses(final Class<?>... classes) {
    war.deleteClasses(classes);
    return this;
  }

  @Override
  public WarBuilder<T> addClasses(final Class<?>... classes) throws IllegalArgumentException {
    war.addClasses(classes);
    return this;
  }

  @Override
  public WarBuilder<T> addPackages(final boolean recursive, final String... packages)
      throws IllegalArgumentException {
    war.addPackages(recursive, packages);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public WarBuilder<T> addAsResource(final String resourceName) throws IllegalArgumentException {
    war.addAsResource(resourceName);
    return this;
  }

  @Override
  public WarBuilder<T> addAsResource(String resourceName, String target)
      throws IllegalArgumentException {
    war.addAsResource(resourceName, target);
    return this;
  }

  @Override
  public WarBuilder<T> addAsWebInfResource(final String resourceName, final String target)
      throws IllegalArgumentException {
    war.addAsWebInfResource(resourceName, target);
    return this;
  }

  @Override
  public WarBuilder<T> addAsWebInfResource(final Asset resource, final String target)
      throws IllegalArgumentException {
    war.addAsWebInfResource(resource, target);
    return this;
  }

  /**
   * Builds the final WAR archive. The following stuffs are automatically added :
   * <ul>
   * <li>The <b>beans.xml</b> in order to activate CDI,</li>
   * <li>The <b>META-INF/services/org.silverpeas.core.util.BeanContainer</b> to load the CDI-based bean
   * container,</li>
   * <li>The <b>test-ds.xml</b> resource in order to define a data source for tests.</li>
   * </ul>
   * @return the built WAR archive.
   */
  @Override
  public final WebArchive build() {
    try {
      if (!jarLibForPersistence.isEmpty()) {
        String persistenceXmlContent;
        try (InputStream is = WarBuilder.class.getResourceAsStream(
            "/META-INF/core-test-persistence.xml")) {
          persistenceXmlContent = IOUtils.toString(is, Charsets.UTF_8);
        }
        File persistenceXml = FileUtils.getFile(
            classOfTest.getProtectionDomain().getCodeSource().getLocation().getFile(), "META-INF",
            "dynamic-test-persistence.xml");
        logInfo("Setting persistence xml descriptor: " + persistenceXml.getPath());
        persistenceXmlContent = persistenceXmlContent.replace("<!-- @JAR_FILES@ -->",
            String.join("\n", jarLibForPersistence));
        FileUtils.writeStringToFile(persistenceXml, persistenceXmlContent, Charsets.UTF_8);
        logInfo(
            "Filling '" + persistenceXml.getPath() + "'\nwith content:\n" + persistenceXmlContent);
        logInfo("Adding completed META-INF/persistence.xml");
        addAsResource("META-INF/" + persistenceXml.getName(), "META-INF/persistence.xml");
      } else {
        addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml");
      }
      if (!webParts.isEmpty()) {
        String webXmlContent;
        try (InputStream is = WarBuilder.class.getResourceAsStream("/META-INF/core-web-test.xml")) {
          webXmlContent = IOUtils.toString(is, Charsets.UTF_8);
        }
        File webXml = FileUtils
            .getFile(classOfTest.getProtectionDomain().getCodeSource().getLocation().getFile(),
                "META-INF", "dynamic-test-web.xml");
        logInfo("Setting web xml descriptor: " + webXml.getPath());
        webXmlContent = webXmlContent.replace("<!-- @WEB_PARTS@ -->", String.join("\n", webParts));
        FileUtils.writeStringToFile(webXml, webXmlContent, Charsets.UTF_8);
        logInfo("Filling '" + webXml.getPath() + "'\nwith content:\n" + webXmlContent);
        logInfo("Adding completed web.xml");
        war.setWebXML(webXml);
      }
      File[] libs = Stream.of(Maven.resolver().loadPomFromFile("pom.xml").resolve(mavenDependencies).withTransitivity()
              .asFile()).filter(onLibsToInclude()).toArray(File[]::new);
      war.addAsLibraries(libs);
      war.addAsResource("META-INF/services/test-org.silverpeas.core.util.BeanContainer",
          "META-INF/services/org.silverpeas.core.util.BeanContainer");
      war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
      //war.addAsWebInfResource("test-ds.xml", "test-ds.xml");
      // Resources
      war.addAsResource("maven.properties");
      return war;
    } catch (Exception e) {
      final String message = "WAR BUILD PROBLEM...";
      Logger.getAnonymousLogger().log(Level.SEVERE, message, e);
      throw new SilverpeasRuntimeException(message, e);
    }
  }

  /**
   * Gets a predicate on libraries (represented each of them by a file) to indicate if a library
   * is accepted or not to be included among the dependencies of a WAR to build for integration
   * tests ran by Arquillian.
   * @return a predicate on a file (a given library)
   */
  protected Predicate<File> onLibsToInclude() {
    return f -> !f.getName().startsWith("resteasy") && !f.getName().startsWith("javax") &&
        !f.getName().contains("hibernate");
  }

  private void logInfo(String info) {
    Logger.getLogger(WarBuilder.class.getName()).info(info);
  }

  /**
   * Applies the configuration that the test is focused on.
   * @param testFocus the instance of an anonymous implementation of {@link TestFocus} interface.
   * @return the instance of the war builder.
   */
  @SuppressWarnings("unchecked")
  public final WarBuilder<T> testFocusedOn(TestFocus testFocus) {
    testFocus.focus(this);
    return this;
  }

  /**
   * Applies a configuration by using directly the {@link WebArchive} instance provided by the
   * ShrinkWrap API.
   * @param onShrinkWrapWar the instance of an anonymous implementation of {@link org.silverpeas
   * .test.WarBuilder.OnShrinkWrapWar}
   * interface.
   * @return the instance of the war builder.
   */
  @Override
  public final WarBuilder<T> applyManually(OnShrinkWrapWar onShrinkWrapWar) {
    onShrinkWrapWar.applyManually(war);
    return this;
  }

  /**
   * In order to highlight the specification of tested classes, packages or resources.
   */
  public interface TestFocus<T extends WarBuilder<T>> {
    void focus(WarBuilder<T> warBuilder);
  }
}
