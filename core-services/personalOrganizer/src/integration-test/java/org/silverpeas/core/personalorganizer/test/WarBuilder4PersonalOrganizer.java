/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.personalorganizer.test;

import org.silverpeas.core.test.BasicWarBuilder;

/**
 * A War archive builder for the integration tests on the Personal Organizer service.
 * @author mmoquillon
 */
public class WarBuilder4PersonalOrganizer extends BasicWarBuilder {

  public static <T> WarBuilder4PersonalOrganizer onWarForTestClass(Class<T> test) {
    return (WarBuilder4PersonalOrganizer) new WarBuilder4PersonalOrganizer(test)
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
        .createMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud")
        .testFocusedOn(war -> {
          war.addPackages(true, "org.silverpeas.core.personalorganizer")
              .addAsResource("org/silverpeas/core/personalorganizer");
        });
  }
  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param test the class of the test for which a war archive will be build.
   */
  protected <T> WarBuilder4PersonalOrganizer(final Class<T> test) {
    super(test);
  }
}
