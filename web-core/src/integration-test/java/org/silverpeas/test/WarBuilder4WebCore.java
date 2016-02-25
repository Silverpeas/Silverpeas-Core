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

import com.silverpeas.authentication.SilverpeasSessionOpener;
import com.silverpeas.peasUtil.AccessForbiddenException;
import org.silverpeas.servlet.HttpRequest;

/**
 * This builder extends the {@link org.silverpeas.test.WarBuilder} in order to centralize the
 * definition of common archive part definitions.
 * @author Yohann Chastagnier
 */
public class WarBuilder4WebCore extends BasicCoreWarBuilder {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param test the class of the test for which a war archive will be build.
   */
  protected <T> WarBuilder4WebCore(final Class<T> test) {
    super(test);
    addMavenDependencies("org.silverpeas.core:ws-test-core");
    addMavenDependencies("org.silverpeas.core.services:silverstatistics");
    addMavenDependencies("org.silverpeas.core.services:clipboard");
    addPackages(true, "com.stratelia.silverpeas.peasCore");
    addPackages(true, "com.silverpeas.peasUtil");
    addClasses(AccessForbiddenException.class, SilverpeasSessionOpener.class);
    addPackages(true, "org.silverpeas.servlet");
    addPackages(true, "org.silverpeas.subscription");
    // Bundles & Settings
    addAsResource("org/silverpeas/publication/publicationSettings.properties");
    addAsResource("org/silverpeas/classifyEngine/ClassifyEngine.properties");
    addAsResource("org/silverpeas/clipboard/settings/clipboardSettings.properties");
    addAsResource("org/silverpeas/peasCore/SessionManager.properties");
    addAsResource("org/silverpeas/search/indexEngine/StopWords.properties");
    addAsResource("org/silverpeas/searchEngine/searchEngineSettings.properties");
    addAsResource("org/silverpeas/silverstatistics/SilverStatistics.properties");
    addAsResource("org/silverpeas/util/attachment/Attachment.properties");
  }

  /**
   * Gets an instance of a war archive builder for the specified test class with the
   * following common stuffs:
   * <ul>
   * <li>ws-test-core</li>
   * <li>silverpeas-core</li>
   * <li>all the necessary to handle http request ({@link HttpRequest} for example)</li>
   * </ul>
   * @return the instance of the war archive builder.
   */
  public static <T> WarBuilder4WebCore onWarForTestClass(Class<T> test) {
    return new WarBuilder4WebCore(test);
  }

  /**
   * Sets REST Web Service environment.
   * @return the instance of the war archive builder.
   */
  public WarBuilder4WebCore addRESTWebServiceEnvironment() {
    addPackages(true, "com.silverpeas.web");
    addPackages(true, "com.silverpeas.annotation");
    addPackages(true, "org.silverpeas.web.token");
    return this;
  }

  /**
   * Sets string template features.
   * @return the instance of the war builder.
   */
  public WarBuilder4WebCore addStringTemplateFeatures() {
    addAsResource("org/silverpeas/util/stringtemplate.properties");
    return this;
  }
}
