/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
package org.silverpeas.core.test;

import org.silverpeas.core.cache.VolatileResourceCleaner;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.test.jcr.SilverpeasJcrInitialization;
import org.silverpeas.core.test.office.OfficeServiceInitializationListener;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.wbe.StubbedWbeHostManager;

/**
 * This builder extends the {@link WarBuilder} in order to centralize the definition of common
 * archive part definitions.
 *
 * @author Yohann Chastagnier
 */
public class WarBuilder4LibCore extends WarBuilder<WarBuilder4LibCore> {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   *
   * @param test the class of the test for which a war archive will be build.
   */
  protected <T> WarBuilder4LibCore(final Class<T> test) {
    super(test);
  }

  /**
   * Gets an instance of a war archive builder for the specified test class with the following
   * common stuffs:
   * <ul>
   * <li>the resources located in the same package of the specified test class,</li>
   * <li>{@link ServiceProvider} features.</li>
   * <li>the base i18n bundle loaded.</li>
   * </ul>
   *
   * @return the instance of the war archive builder.
   */
  public static <T> WarBuilder4LibCore onWarForTestClass(Class<T> test) {
    return new WarBuilder4LibCore(test)
        .addMavenDependencies("net.htmlparser.jericho:jericho-html")
        .addMavenDependencies("commons-fileupload:commons-fileupload")
        .addMavenDependencies("com.novell.ldap:jldap")
        .addMavenDependencies("org.forgerock.ce.opendj:opendj-server")
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core-api")
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
        .addMavenDependenciesWithPersistence("org.silverpeas.core.services:silverpeas-core-pdc")
        .addMavenDependencies("org.silverpeas.core.services:silverpeas-core-comment")
        .addMavenDependencies("org.silverpeas.core.services:silverpeas-core-silverstatistics")
        .addMavenDependencies("org.silverpeas.core.services:silverpeas-core-personalorganizer")
        .addMavenDependencies("org.silverpeas.core.services:silverpeas-core-contact")
        .addMavenDependencies("org.silverpeas.core.services:silverpeas-core-mylinks")
        .addMavenDependencies("org.silverpeas.core.services:silverpeas-core-importexport")
        .addMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud")
        .addMavenDependencies("org.silverpeas.core.services:silverpeas-core-viewer")
        .addMavenDependencies("org.silverpeas.core.services:silverpeas-core-sharing")
        .addMavenDependencies("org.silverpeas.core.services:silverpeas-core-chat")
        .addMavenDependencies("org.silverpeas.core.services:silverpeas-core-workflow")
        .addMavenDependencies("org.silverpeas.core:silverpeas-core-rs")
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core-web")
        .addAsResource("maven.properties");
  }

  @Override
  public WarBuilder4LibCore addMavenDependencies(String... mavenDependencies) {
    super.addMavenDependencies(mavenDependencies);
    return this;
  }

  @Override
  public WarBuilder4LibCore addMavenDependenciesWithPersistence(String... mavenDependencies) {
    super.addMavenDependenciesWithPersistence(mavenDependencies);
    return this;
  }

  @Override
  public WarBuilder4LibCore addAsResource(String resourceName) throws IllegalArgumentException {
    super.addAsResource(resourceName);
    return this;
  }

  /**
   * Adds all the base resources required by many of the Silverpeas features.
   * @return itself.
   */
  public WarBuilder4LibCore addBaseResources() {
    return addAsResource("org/silverpeas/general.properties")
        .addAsResource("org/silverpeas/multilang/generalMultilang.properties")
        .addAsResource("org/silverpeas/lookAndFeel/generalLook.properties")
        .addAsResource("org/silverpeas/util/i18n.properties")
        .addAsResource("org/silverpeas/util/multilang/i18n_fr.properties")
        .addAsResource("org/silverpeas/util/multilang/i18n_en.properties")
        .addAsResource("org/silverpeas/util/multilang/util.properties")
        .addAsResource("org/silverpeas/util/multilang/util_fr.properties")
        .addAsResource("org/silverpeas/util/multilang/util_en.properties")
        .addAsResource(
            "org/silverpeas/notificationManager/settings/notificationManagerSettings.properties");
  }

  /**
   * Enables the office feature.
   *
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore enableOfficeFeature() {
    addWebListener(OfficeServiceInitializationListener.class);
    addMavenDependencies("org.apache.commons:commons-exec", "org.jodconverter:jodconverter-local");
    addAsResource("org/silverpeas/converter");
    return this;
  }

  /**
   * Enables the JCR feature of Silverpeas. This will enable also the following features:
   * <ul>
   *   <li>The administration feature;</li>
   *   <li>The publication templating feature.</li>
   * </ul>
   *
   * @return itself.
   */
  public WarBuilder4LibCore enableJcrFeature() {
    addMavenDependencies("org.silverpeas.core:silverpeas-core-jcr");
    addMavenDependencies("commons-beanutils:commons-beanutils");
    addMavenDependencies("org.im4java:im4java");
    addWebListener(SilverpeasJcrInitialization.class);
    enableAdministrationFeatures();
    enablePublicationTemplateFeatures();
    addClasses(StubbedWbeHostManager.class);
    addClasses(VolatileResourceCleaner.class);
    addAsResource("silverpeas-oak.properties");
    addAsResource("org/silverpeas/util/attachment/Attachment.properties");
    applyManually(war ->
        war.deletePackages(true, "org.silverpeas.core.contribution.attachment.mock"));

    return this;
  }

  /**
   * Adds the resources required for the index engine of Silverpeas.
   *
   * @return itself.
   */
  public WarBuilder4LibCore addIndexEngineResources() {
    addAsResource("org/silverpeas/index/indexing");
    return this;
  }

  /**
   * Enable the Silverpeas publication templating feature.
   * @return itself.
   */
  public WarBuilder4LibCore enablePublicationTemplateFeatures() {
    addAsResource("org/silverpeas/publicationTemplate/settings");
    return this;
  }

  /**
   * Enable the Silverpeas templating feature.
   *
   * @return itself.
   */
  public WarBuilder4LibCore enableStringTemplateFeatures() {
    if (!contains(SilverpeasTemplate.class)) {
      addMavenDependencies("org.antlr:ST4");
      addAsResource("org/silverpeas/util/stringtemplate.properties");
    }
    return this;
  }

  /**
   * Enables the Silverpeas administration features. It adds by default all the base resources as
   * well as those required by the Silverpeas index engine. It enables also both the
   * Silverpeas templating and scheduling features.
   *
   * @return itself.
   */
  public WarBuilder4LibCore enableAdministrationFeatures() {
    addAsResource("xmlcomponents");
    addAsResource("org/silverpeas/admin");

    // Centralized features
    addBaseResources();
    addIndexEngineResources();
    enableStringTemplateFeatures();
    enableSchedulingFeatures();
    return this;
  }

  /**
   * Enables the scheduling feature in Silverpeas.
   *
   * @return itself.
   */
  public WarBuilder4LibCore enableSchedulingFeatures() {
    addMavenDependencies("org.quartz-scheduler:quartz");
    addAsResource("org/silverpeas/scheduler/settings/persistent-scheduler.properties");
    return this;
  }

  /**
   * Add benchmark test features in the web archive (war).
   *
   * @return itself.
   */
  public WarBuilder4LibCore addBenchmarkTestFeatures() {
    addMavenDependencies("com.carrotsearch:junit-benchmarks");
    return this;
  }

}
