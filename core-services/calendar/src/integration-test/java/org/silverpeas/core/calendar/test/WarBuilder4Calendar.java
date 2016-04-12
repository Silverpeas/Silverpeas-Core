/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.calendar.test;

import org.silverpeas.core.test.BasicWarBuilder;

/**
 * A War archive builder for the integration tests on the Calendar service.
 * @author mmoquillon
 */
public class WarBuilder4Calendar extends BasicWarBuilder {

  public static <T> WarBuilder4Calendar onWarForTestClass(Class<T> test) {
    return (WarBuilder4Calendar) new WarBuilder4Calendar(test)
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
        .addMavenDependencies("org.apache.tika:tika-core")
        .addMavenDependencies("org.apache.tika:tika-parsers")
        .createMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud")
        .testFocusedOn(war -> {
          war.addPackages(true, "org.silverpeas.core.calendar")
              .addAsResource("org/silverpeas/core/calendar")
              .addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
        });
  }
  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param test the class of the test for which a war archive will be build.
   */
  protected <T> WarBuilder4Calendar(final Class<T> test) {
    super(test);
  }
}
