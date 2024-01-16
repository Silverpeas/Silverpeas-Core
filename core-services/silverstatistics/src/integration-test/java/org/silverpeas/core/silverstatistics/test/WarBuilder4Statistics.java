/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.silverstatistics.test;

import org.silverpeas.core.test.BasicWarBuilder;
import org.silverpeas.core.test.WarBuilder;

/**
 * This builder extends the {@link WarBuilder} in order to centralize the
 * definition of common archive part definitions.
 * @author Yohann Chastagnier
 */
public class WarBuilder4Statistics extends BasicWarBuilder {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param test the class of the test for which a war archive will be build.
   */
  protected <T> WarBuilder4Statistics(final Class<T> test) {
    super(test);
    addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
    createMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud");
  }

  /**
   * Gets an instance of a war archive builder for the specified test class.
   * @return the instance of the war archive builder.
   */
  public static <T> WarBuilder4Statistics onWarForTestClass(Class<T> test) {
    return new WarBuilder4Statistics(test);
  }
}
