/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

/**
 * The minimal configuration to set in order to start an integration test with core
 * module dependencies.
 * @author Yohann Chastagnier
 */
public class BasicCoreWarBuilder extends BasicWarBuilder {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param <T> the concrete type of the test
   * @param test the class of the test for which a war archive will be build.
   */
  protected <T> BasicCoreWarBuilder(final Class<T> test) {
    super(test);
    addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core-api");
    addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
    addMavenDependenciesWithPersistence("org.silverpeas.core.services:silverpeas-core-pdc");
    addMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud");

    // Bundles & Settings
    addAsResource("org/silverpeas/util/attachment/mime_types.properties");
    addAsResource("org/silverpeas/general.properties");
  }

  /**
   * Constructs an instance of the basic core war archive builder for the specified test class.
   * @param test the test class for which a war will be built. Any resources located in the same
   * package of the test will be loaded into the war.
   * @param <T> the type of the test.
   * @return a basic builder of the war archive.
   */
  public static <T> BasicCoreWarBuilder onWarForTestClass(Class<T> test) {
    return new BasicCoreWarBuilder(test);
  }
}
