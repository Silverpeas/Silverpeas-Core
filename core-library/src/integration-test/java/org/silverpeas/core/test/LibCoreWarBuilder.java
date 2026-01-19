/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
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

import org.silverpeas.core.calendar.*;
import org.silverpeas.core.exception.RelativeFileAccessException;
import org.silverpeas.core.i18n.*;
import org.silverpeas.core.notification.sse.ServerEventDispatcherTaskLoadTest;
import org.silverpeas.core.reminder.DefaultReminderRepository;
import org.silverpeas.core.reminder.ReminderIT;
import org.silverpeas.core.test.integration.DataSourceProvider;
import org.silverpeas.core.test.integration.IIOProviderContextListener;
import org.silverpeas.core.test.jcr.SilverpeasJcrInitialization;
import org.silverpeas.core.test.stub.*;
import org.silverpeas.core.wbe.StubbedWbeHostManager;

/**
 *
 * @author mmoquillon
 */
public class LibCoreWarBuilder extends WarBuilder<LibCoreWarBuilder> {

  /**
   * Constructs a war builder for the specified test class with as dependency the Silverpeas Core
   * API. It will load all the resources in the same packages of the specified test class. The war
   * builder will include by default the following packages:
   * <ul>
   *   <li>{@link org.silverpeas.core.util}</li>
   *   <li>{@link org.silverpeas.core.calendar.ical4j}</li>
   *   <li>{@link org.silverpeas.core.calendar.repository}</li>
   * </ul>
   * and the following web servlet listeners:
   * <ul>
   *   <li>{@link org.silverpeas.core.test.integration.SilverpeasLoggerInitializationListener}</li>
   *   <li>{@link org.silverpeas.core.test.integration.IIOProviderContextListener}</li>
   * </ul>
   *
   * @param classOfTest the class of the test for which a war archive will be built.
   */
  protected <U> LibCoreWarBuilder(Class<U> classOfTest) {
    super(classOfTest);
  }

  /**
   * Constructs an instance of the war archive builder for the specified test class with Silverpeas
   * Core API as the only dependency (with its own dependencies). Minimal resources and Java
   * packages required to run a test are included.
   *
   * @param test the test class for which a war will be built. Any resources located in the same
   * package of the test will be loaded into the war.
   * @param <T> the type of the test.
   * @return a basic builder of the war archive.
   */
  public static <T> LibCoreWarBuilder onWarForTestClass(Class<T> test) {
    var builder = new LibCoreWarBuilder(test);
    builder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core-api")
        .addPackages(true, "org.silverpeas.core.util")
        .addPackages(false, "org.silverpeas.core.calendar.ical4j")
        .addPackages(false, "org.silverpeas.core.calendar.repository")
        .addClasses(DefaultReminderRepository.class, RelativeFileAccessException.class)
        .addAsResource("org/silverpeas/general.properties")
        .addAsResource("org/silverpeas/multilang/generalMultilang.properties")
        .addAsResource("org/silverpeas/lookAndFeel/generalLook.properties")
        .addAsResource("org/silverpeas/util/multilang/util.properties")
        .addAsResource("org/silverpeas/util/multilang/util_fr.properties")
        .addAsResource("org/silverpeas/util/multilang/util_en.properties")
        .addAsResource("maven.properties");
    return builder;
  }

  /**
   * Constructs an instance of the war archive builder for the specified test class with the whole
   * code of Silverpeas Core API, Silverpeas Core JCR, Silverpeas Core Lib and of Silverpeas Core
   * Test. The required dependencies are included and all the stubbed classes in the code are
   * explicitly excluded. Usually, no others and external dependencies are needed. You have just to
   * add the resources your test requires.
   *
   * @param test the test class for which a war will be built. Any resources located in the same
   * package of the test will be loaded into the war.
   * @param <T> the type of the test.
   * @return a basic builder of the war archive.
   */
  public static <T> WarBuilder<LibCoreWarBuilder> onFullWarForTestClass(Class<T> test) {
    var builder = new LibCoreWarBuilder(test);
    // add all the packages of the Silverpeas Core subprojects declared as dependencies in the
    // pom.xml as well as the packages of this project
    return builder.addPackages(true, "org.silverpeas.core")
        // remove the stubs because the original classes are added (in order to avoid any conflicts)
        // remove also non-required web listeners
        .deletePackages(true, "org.silverpeas.core.stub",
            "org.silverpeas.core.test.stub",
            "org.silverpeas.core.subscription.stub",
            "org.silverpeas.core.test.office",
            "org.silverpeas.core.test.subscription")
        .deleteClasses(ReminderIT.StubbedPersonalizationService.class, StubbedWbeHostManager.class,
            StubbedWbeClientManager.class, ServerEventDispatcherTaskLoadTest.class,
            ServerEventDispatcherTaskLoadTest.VolatileScheduler4Test.class,
            DataSourceProvider.class)
        // add the required dependencies as declared in the pom.xml
        .addMavenDependencies("javax.jcr:jcr")
        .addMavenDependencies("org.apache.jackrabbit:jackrabbit-jcr-server:jar:jakarta:2.22.2")
        .addMavenDependencies("org.apache.jackrabbit:oak-jcr")
        .addMavenDependencies("org.apache.jackrabbit:oak-segment-tar")
        .addMavenDependencies("io.dropwizard.metrics:metrics-core")
        .addMavenDependencies("org.antlr:ST4")
        .addMavenDependencies("org.owasp.encoder:encoder")
        .addMavenDependencies("org.quartz-scheduler:quartz")
        .addMavenDependencies("org.apache.lucene:lucene-core")
        .addMavenDependencies("org.apache.lucene:lucene-queryparser")
        .addMavenDependencies("org.apache.lucene:lucene-queries")
        .addMavenDependencies("org.apache.lucene:lucene-suggest")
        .addMavenDependencies("org.apache.lucene:lucene-analysis-common")
        .addMavenDependencies("org.apache.tika:tika-core")
        .addMavenDependencies("org.apache.tika:tika-parsers-standard-package")
        .addMavenDependencies("com.drewnoakes:metadata-extractor")
        .addMavenDependencies("org.im4java:im4java")
        .addMavenDependencies("org.mnode.ical4j:ical4j")
        .addMavenDependencies("net.htmlparser.jericho:jericho-html")
        // don't forget to set up the JCR backend
        .addWebListener(SilverpeasJcrInitialization.class)
        // the imageIO drivers must be correctly loaded and then unloaded once the archive
        // undeployed
        .addWebListener(IIOProviderContextListener.class)
        // commons resources required by some of the Silverpeas code
        .addAsResource("org/silverpeas/index/indexing")
        .addAsResource("org/silverpeas/general.properties")
        .addAsResource("org/silverpeas/multilang/generalMultilang.properties")
        .addAsResource("org/silverpeas/lookAndFeel/generalLook.properties")
        .addAsResource("org/silverpeas/util/multilang/util.properties")
        .addAsResource("org/silverpeas/util/multilang/util_fr.properties")
        .addAsResource("org/silverpeas/util/multilang/util_en.properties")
        // JCR required stuffs
        .addAsResource("silverpeas-oak.properties")
        .addAsResource("silverpeas-jcr.cnd")
        .addAsResource("silverpeas-oak-index.properties")
        .addAsResource("META-INF/services/javax.jcr.RepositoryFactory")
        // required for the integration tests
        .addAsResource("maven.properties")
        .addAsWebInfResource("test-jms.xml", "test-jms.xml");
  }

  /**
   * Adds the classes that stub the User API in Silverpeas. This is useful to avoid to pull all the
   * core admin package with its boilerplate code.
   *
   * @return itself.
   */
  public LibCoreWarBuilder addStubbedUserAPI() {
    addClasses(StubbedUserProvider.class, UserImpl.class);
    return this;
  }

  /**
   * Adds the classes that stub the components part (aka applications part) of the Organization API.
   * This is useful to avoid to pull all the code admin package with its boilerplate code.
   *
   * @return itself.
   */
  public LibCoreWarBuilder addStubbedAppAPI() {
    addClasses(StubbedComponentInstanceProvider.class, ComponentInstImpl.class);
    return this;
  }

  /**
   * Adds the Scheduling Engine defined in Silverpeas Core Library. Its dependencies are also
   * added.
   *
   * @return itself.
   */
  public LibCoreWarBuilder addSchedulingEngine() {
    this.addMavenDependencies("org.quartz-scheduler:quartz")
        .addPackages(true, "org.silverpeas.core.scheduler")
        .addAsResource("org/silverpeas/scheduler/settings/persistent-scheduler.properties");
    return this;
  }

  /**
   * Adds the Indexing Engine defined in Silverpeas Core Library. Its dependencies are also added.
   *
   * @return itself.
   */
  public LibCoreWarBuilder addIndexingEngine() {
    this.addPackages(true, "org.silverpeas.core.index.indexing")
        .addMavenDependencies("org.apache.lucene:lucene-core")
        .addMavenDependencies("org.apache.lucene:lucene-queryparser")
        .addMavenDependencies("org.apache.lucene:lucene-queries")
        .addMavenDependencies("org.apache.lucene:lucene-suggest")
        .addMavenDependencies("org.apache.lucene:lucene-analysis-common")
        .addMavenDependencies("org.apache.tika:tika-core")
        .addMavenDependencies("org.apache.tika:tika-parsers-standard-package")
        .addMavenDependencies("com.drewnoakes:metadata-extractor")
        .addAsResource("org/silverpeas/index/indexing");
    return this;
  }

  /**
   * Adds the I18n Implementation API defined in Silverpeas Core Library and extending the I18n API
   * from Silverpeas Core API.
   *
   * @return itself
   */
  public LibCoreWarBuilder addI18n() {
    this.addClasses(AbstractI18NBean.class, AbstractBean.class, I18NBean.class,
            I18NLanguage.class, I18NHelper.class)
        .addAsResource("org/silverpeas/personalization/settings/personalizationPeasSettings" +
            ".properties")
        .addAsResource("org/silverpeas/util/i18n.properties")
        .addAsResource("org/silverpeas/util/multilang/i18n_fr.properties")
        .addAsResource("org/silverpeas/util/multilang/i18n_en.properties");
    return this;
  }

  /**
   * Add the Calendar API implementation provided by Silverpeas Core Library.
   *
   * @return itself
   */
  public LibCoreWarBuilder addCalendarEngine() {
    this.addStubbedUserAPI()
        .addPackages(true, "org.silverpeas.core.calendar.ical4j")
        .addPackages(true, "org.silverpeas.core.calendar.repository")
        .addPackages(true, "org.silverpeas.core.calendar.subscription")
        .addClasses(AbstractCalendarService.class,
            CalendarComponentPath.class,
            CalendarEventOccurrenceBuilder.class,
            CalendarEventUtil.class,
            CalendarIntegrityProcessor.class,
            CalendarPath.class,
            DefaultCalendarResourcePathProvider.class)
        .addClasses(StubbedWysiwygContentRepository.class);
    return this;
  }
}
  