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

package org.silverpeas.test;

import com.silverpeas.ui.DisplayI18NHelper;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.persistence.TransactionProvider;
import org.silverpeas.util.*;
import org.silverpeas.util.exception.RelativeFileAccessException;

import java.io.File;

/**
 * This class centralizes common shrink wrap operations for integration tests.
 * @author Yohann Chastagnier
 */
public class ShrinkWrapUtil {

  /**
   * Gets a minimal configuration to create a War Container.
   * @return {@link WebArchive}
   */
  public static WebArchive getMinimalTestWar() {
    File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
        .resolve("com.ninja-squad:DbSetup", "org.apache.commons:commons-lang3",
            "commons-codec:commons-codec", "commons-io:commons-io").withTransitivity().asFile();
    return ShrinkWrap.create(WebArchive.class, "test.war").addAsLibraries(libs)
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
        .addAsWebInfResource("test-ds.xml", "test-ds.xml");
  }


  /**
   * Gets a minimal configuration to create a Jar Library.
   * @return {@link JavaArchive}
   */
  public static JavaArchive getMinimalTestJar() {
    return ShrinkWrap.create(JavaArchive.class, "test.jar")
        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
  }

  /**
   * Adds basic util classes.
   * @param javaArchive an instance of Jar Library configuration.
   * @return the given instance of {@link JavaArchive} completed
   */
  public static JavaArchive applyBasicUtils(JavaArchive javaArchive) {
    return javaArchive.addClasses(StringUtil.class).addClass(CollectionUtil.class);
  }

  /**
   * Adds bundle classes.
   * @param javaArchive an instance of Jar Library configuration.
   * @return the given instance of {@link JavaArchive} completed
   */
  public static JavaArchive applyBundle(JavaArchive javaArchive) {
    return javaArchive
        .addClasses(ResourceLocator.class, DisplayI18NHelper.class, ConfigurationClassLoader.class,
            ConfigurationControl.class, ResourceBundleWrapper.class, FileUtil.class,
            MimeTypes.class, RelativeFileAccessException.class, GeneralPropertiesManager.class)
        .addAsResource("org/silverpeas/general.properties")
        .addAsResource("org/silverpeas/multilang/generalMultilang.properties");
  }

  /**
   * Adds classes in order to perform persisted operations.
   * @param javaArchive an instance of Jar Library configuration.
   * @return the given instance of {@link JavaArchive} completed
   */
  public static JavaArchive applyPersistence(JavaArchive javaArchive) {
    return javaArchive.addPackages(true, "org.silverpeas.persistence.model")
        .addPackages(true, "org.silverpeas.persistence.repository")
        .addClasses(DBUtil.class, Transaction.class, TransactionProvider.class)
        .addAsManifestResource("META-INF/test-persistence.xml", "persistence.xml");
  }

  /**
   * Completes configuration to manipulate manually CDI ({@link ServiceProvider}).
   * @param webArchive an instance of Web Container configuration.
   * @return the given instance of {@link WebArchive} completed
   */
  public static WebArchive applyManualCDI(WebArchive webArchive) {
    return webArchive.addClass(ServiceProvider.class).addClass(BeanContainer.class)
        .addClass(CDIContainer.class)
        .addAsManifestResource("META-INF/services/test-org.silverpeas.util.BeanContainer",
            "services/org.silverpeas.util.BeanContainer")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
  }
}
