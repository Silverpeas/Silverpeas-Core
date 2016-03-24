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

package org.silverpeas.web.test;

import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.test.WarBuilder;

/**
 * This builder extends the {@link WarBuilder} in order to centralize the
 * definition of common archive part definitions.
 */
public class WarBuilder4WarCore extends WarBuilder<WarBuilder4WarCore> {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param test the class of the test for which a war archive will be build.
   */
  protected <T> WarBuilder4WarCore(final Class<T> test) {
    super(test);
  }

  /**
   * Gets an instance of a war archive builder for the specified test class with the
   * following common stuffs:
   * <ul>
   * <li>silverpeas-core-web-test</li>
   * <li>silverpeas-core</li>
   * <li>all the necessary to handle http request ({@link HttpRequest} for example)</li>
   * </ul>
   * @return the instance of the war archive builder.
   */
  public static <T> WarBuilder4WarCore onWarForTestClass(Class<T> test) {
    WarBuilder4WarCore warBuilder = new WarBuilder4WarCore(test);
    warBuilder.addMavenDependencies("javax.jcr:jcr");
    warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
    warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.services:silverpeas-core-pdc");
    warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core-web");
    warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-comment");
    warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-silverstatistics");
    warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-calendar");
    warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-contact");
    warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-mylinks");
    warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-importexport");
    warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud");
    warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-viewer");
    warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-sharing");
    warBuilder.addMavenDependencies("org.apache.tika:tika-core");
    warBuilder.addMavenDependencies("org.apache.tika:tika-parsers");
    warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
    return warBuilder;
  }

}
