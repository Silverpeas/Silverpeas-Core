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
package org.silverpeas.core.mylinks.test;

import org.silverpeas.core.test.BasicWarBuilder;

/**
 * This builder extends the {@link BasicWarBuilder} in order to centralize the definition of common
 * archive part definitions.
 * @author Yohann Chastagnier
 */
public class WarBuilder4MyLinks extends BasicWarBuilder {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param classOfTest the class of the test for which a war archive will be build.
   */
  protected <CLASS_TEST> WarBuilder4MyLinks(final Class<CLASS_TEST> classOfTest) {
    super(classOfTest);
  }

  /**
   * Gets an instance of a war archive builder for the specified test class with the common
   * dependencies for publications.
   * @return the instance of the war archive builder.
   */
  public static <T> WarBuilder4MyLinks onWarForTestClass(Class<T> test) {
    WarBuilder4MyLinks warBuilder = new WarBuilder4MyLinks(test);
    warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
    warBuilder.createMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud");
    warBuilder.testFocusedOn(war -> war
        .addPackages(true, "org.silverpeas.core.mylinks")
        .addAsResource("create-database.sql"));
    return warBuilder;
  }
}
