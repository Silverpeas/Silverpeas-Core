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
package org.silverpeas.web.usercalendar.socialnetwork;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.component.PersonalComponentRegistry;
import org.silverpeas.core.admin.component.WAComponentRegistry;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.DefaultPersonalizationService;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.socialnetwork.provider.SocialEventProvider;
import org.silverpeas.core.socialnetwork.provider.SocialMediaCommentProvider;
import org.silverpeas.core.socialnetwork.provider.SocialMediaProvider;
import org.silverpeas.core.socialnetwork.provider.SocialNewsCommentProvider;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.web.test.WarBuilder4WarCore;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author silveryocha
 */
@RunWith(Arquillian.class)
public class DefaultUserCalendarEventOccurrenceSocialProviderIT {

  private static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/web/usercalendar/socialnetwork/create-database.sql";
  private static final String DATASET_SCRIPT =
      "/org/silverpeas/web/usercalendar/socialnetwork/calendar-dataset.sql";

  private static final String RDV_1_TITLE = "RDV1";
  private static final String RDV_3_TITLE = "RDV3";
  private static final String RDV_4_TITLE = "RDV4";

  private static final String DEFAULT_END_DATE = "2011/07/31";
  private static final String BEGIN_DATE_1 = "2011/07/01";
  private static final String BEGIN_DATE_9 = "2011/07/09";
  private static final String BEGIN_DATE_10 = "2011/07/10";
  public static final String USER_1 = "1";
  public static final String USER_2 = "2";

  @Inject
  private SocialEventProvider socialEventsInterface;

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule
      .createTablesFrom(TABLE_CREATION_SCRIPT)
      .loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WarCore
        .onWarForTestClass(DefaultUserCalendarEventOccurrenceSocialProviderIT.class)
        .addPackages(false, "org.silverpeas.web.usercalendar")
        .addPackages(true, "org.silverpeas.web.usercalendar.notification")
        .addPackages(true, "org.silverpeas.web.usercalendar.services")
        .testFocusedOn(war -> war
            .addPackages(true, "org.silverpeas.web.usercalendar.socialnetwork")
            .addAsResource("org/silverpeas/web/usercalendar/socialnetwork"))
        .build();
  }

  @Before
  public void setup() throws Exception {
    final File silverpeasHome = mavenTargetDirectoryRule.getResourceTestDirFile();
    SystemWrapper.get().getenv().put("SILVERPEAS_HOME", silverpeasHome.getPath());
    WAComponentRegistry.get().init();
    PersonalComponentRegistry.get().init();
    CacheServiceProvider.clearAllThreadCaches();
  }

  /*
   * Test of getSocialInformationsList.
   * This test is a migration of testing of JournalDAO.getNextEventsForUser signature which will
   * be soon removed.
   */
  @Test
  public void testGetSocialInformationsList() throws ParseException {
    setNowForTest(DateUtil.parse("2011/07/07"));

    Date begin = DateUtil.parse(BEGIN_DATE_1);
    final Date end = DateUtil.parse(DEFAULT_END_DATE);

    List<SocialInformation> list = socialEventsInterface
        .getSocialInformationsList(USER_1, null, begin, end);

    assertThat(list, notNullValue());
    assertThat(toTitles(list), contains(RDV_1_TITLE, RDV_4_TITLE, RDV_3_TITLE));
    assertThat(toSocialTypes(list).filter(SocialInformationType.EVENT::equals).count(), is(3L));
    assertCountOfMine(list, 3L);

    begin = DateUtil.parse(BEGIN_DATE_9);
    list = socialEventsInterface.getSocialInformationsList(USER_1, null, begin, end);
    assertThat(list, notNullValue());
    // Order by day and time  event 3et 3 have the same day but the hour of event3 before Event2
    assertThat(toTitles(list), contains(RDV_4_TITLE, RDV_3_TITLE));
    assertThat(toSocialTypes(list).filter(SocialInformationType.EVENT::equals).count(), is(2L));
    assertCountOfMine(list, 2L);

    begin = DateUtil.parse(BEGIN_DATE_10);
    list = socialEventsInterface.getSocialInformationsList(USER_1, null, begin, end);
    assertThat(list, notNullValue());
    assertThat(list, empty());
  }

  /*
   * Test of getSocialInformationListOfMyContacts.
   * This test is a migration of testing of JournalDAO.getNextEventsForMyContacts signature which
   * will be soon removed.
   */
  @Test
  public void testGetNextEventsForMyContacts() throws ParseException {
    setNowForTest(DateUtil.parse("2011/07/07"));

    final List<String> myContactIds = singletonList(USER_1);

    Date begin = DateUtil.parse(BEGIN_DATE_1);
    final Date end = DateUtil.parse(DEFAULT_END_DATE);

    List<SocialInformation> list = socialEventsInterface
        .getSocialInformationListOfMyContacts(USER_2, myContactIds, begin, end);

    assertThat(list, notNullValue());
    assertThat(toTitles(list), contains(RDV_1_TITLE, RDV_4_TITLE, RDV_3_TITLE));
    assertThat(toSocialTypes(list).filter(SocialInformationType.EVENT::equals).count(), is(3L));
    assertCountOfMine(list, 0L);

    begin = DateUtil.parse(BEGIN_DATE_9);
    list = socialEventsInterface
        .getSocialInformationListOfMyContacts(USER_2, myContactIds, begin, end);
    assertThat(list, notNullValue());
    // Order by day and time  event 3et 3 have the same day but the hour of event3 befor Event2
    assertThat(toTitles(list), contains(RDV_4_TITLE, RDV_3_TITLE));
    assertThat(toSocialTypes(list).filter(SocialInformationType.EVENT::equals).count(), is(2L));
    assertCountOfMine(list, 0L);

    begin = DateUtil.parse(BEGIN_DATE_10);
    list = socialEventsInterface
        .getSocialInformationListOfMyContacts(USER_2, myContactIds, begin, end);
    assertThat(list, notNullValue());
    assertThat(list, empty());
  }

  /*
   * Test of getLastSocialInformationsListOfMyContacts.
   * This test is a migration of testing of JournalDAO.getLastEventsForMyContacts signature which
   * will be soon removed.
   */
  @Test
  public void testGetLastEventsForMyContacts() throws ParseException {
    setNowForTest(DateUtil.parse("2012/07/07"));

    final List<String> myContactIds = singletonList(USER_1);

    Date begin = DateUtil.parse(BEGIN_DATE_1);
    final Date end = DateUtil.parse(DEFAULT_END_DATE);

    List<SocialInformation> list = socialEventsInterface
        .getLastSocialInformationsListOfMyContacts(USER_2, myContactIds, begin, end);

    //order S3,S2,S1
    assertThat(list, notNullValue());
    assertThat(toTitles(list), contains(RDV_3_TITLE, RDV_4_TITLE, RDV_1_TITLE));
    assertThat(toSocialTypes(list).filter(SocialInformationType.LASTEVENT::equals).count(), is(3L));
    assertCountOfMine(list, 0L);

    //test limit
    begin = DateUtil.parse(BEGIN_DATE_9);
    list = socialEventsInterface
        .getLastSocialInformationsListOfMyContacts(USER_2, myContactIds, begin, end);
    assertThat(list, notNullValue());
    // Order by day and time  event 3et 3 have the same day but the hour of event3 before Event2
    assertThat(toTitles(list), contains(RDV_3_TITLE, RDV_4_TITLE));
    assertThat(toSocialTypes(list).filter(SocialInformationType.LASTEVENT::equals).count(), is(2L));
    assertCountOfMine(list, 0L);

    begin = DateUtil.parse(BEGIN_DATE_10);
    list = socialEventsInterface
        .getLastSocialInformationsListOfMyContacts(USER_2, myContactIds, begin, end);
    assertThat(list, notNullValue());
    assertThat(list, empty());
  }

  /*
   * Test of getMyLastSocialInformationsList.
   * This test is a migration of testing of JournalDAO.getMyLastEvents signature which
   * will be soon removed.
   */
  @Test
  public void testGetMyLastEvents() throws ParseException {
    setNowForTest(DateUtil.parse("2012/07/07"));

    Date begin = DateUtil.parse(BEGIN_DATE_1);
    final Date end = DateUtil.parse(DEFAULT_END_DATE);

    List<SocialInformation> list = socialEventsInterface
        .getMyLastSocialInformationsList(USER_1, begin, end);

    //order S3,S2,S1
    assertThat(list, notNullValue());
    assertThat(toTitles(list), contains(RDV_3_TITLE, RDV_4_TITLE, RDV_1_TITLE));
    assertThat(toSocialTypes(list).filter(SocialInformationType.LASTEVENT::equals).count(), is(3L));
    assertCountOfMine(list, 3L);

    //test limit
    begin = DateUtil.parse(BEGIN_DATE_9);
    list = socialEventsInterface.getMyLastSocialInformationsList(USER_1, begin, end);
    assertThat(list, notNullValue());
    // Order by day and time  event 3et 3 have the same day but the hour of event3 before Event2
    assertThat(toTitles(list), contains(RDV_3_TITLE, RDV_4_TITLE));
    assertThat(toSocialTypes(list).filter(SocialInformationType.LASTEVENT::equals).count(), is(2L));
    assertCountOfMine(list, 2L);

    begin = DateUtil.parse(BEGIN_DATE_10);
    list = socialEventsInterface.getMyLastSocialInformationsList(USER_1, begin, end);
    assertThat(list, notNullValue());
    assertThat(list, empty());
  }

  private List<String> toTitles(final List<SocialInformation> list) {
    return list.stream().map(SocialInformation::getTitle).collect(Collectors.toList());
  }

  private Stream<SocialInformationType> toSocialTypes(final List<SocialInformation> list) {
    return list.stream().map(SocialInformation::getType).map(SocialInformationType::valueOf);
  }

  private void setNowForTest(final Date now) {
    ((StubbedUserCalendarEventOccurrenceSocialProvider) socialEventsInterface).setNow(now);
  }

  /**
   * Update information is here hacked and the real meaning of this information is:
   * <ul>
   *   <li>true = mine</li>
   *   <li>false = to an other user</li>
   * </ul>
   * @param list the list to verify.
   * @param expectedCount the expected count.
   */
  private void assertCountOfMine(final List<SocialInformation> list, final long expectedCount) {
    assertThat(list.stream().filter(SocialInformation::isUpdated).count(), is(expectedCount));
  }

  @Singleton
  @Alternative
  @Priority(APPLICATION + 100)
  public static class StubbedUserCalendarEventOccurrenceSocialProvider
      extends DefaultUserCalendarEventOccurrenceSocialProvider {

    private Date now = null;

    public void setNow(final Date now) {
      this.now = now;
    }

    @Override
    protected Date getLegacyNow() {
      return now;
    }
  }

  /**
   * An implementation is needed
   */
  @Singleton
  @Alternative
  @Priority(APPLICATION + 100)
  public static class StubbedSocialMediaCommentProvider implements SocialMediaCommentProvider {
    @Override
    public List<SocialInformation> getSocialInformationList(final String userId, final Date begin,
        final Date end) {
      return null;
    }

    @Override
    public List<SocialInformation> getSocialInformationListOfMyContacts(final String myId,
        final List<String> myContactsIds, final Date begin, final Date end) {
      return null;
    }
  }

  /**
   * An implementation is needed
   */
  @Singleton
  @Alternative
  @Priority(APPLICATION + 100)
  public static class StubbedSocialMediaProvider implements SocialMediaProvider {

    @Override
    public List<SocialInformation> getSocialInformationList(final String userId, final Date begin,
        final Date end) {
      return null;
    }

    @Override
    public List<SocialInformation> getSocialInformationListOfMyContacts(final String myId,
        final List<String> myContactsIds, final Date begin, final Date end) {
      return null;
    }
  }

  /**
   * An implementation is needed
   */
  @Singleton
  @Alternative
  @Priority(APPLICATION + 100)
  public static class StubbedSocialNewsCommentProvider implements SocialNewsCommentProvider {
    @Override
    public List<SocialInformation> getSocialInformationList(final String userId, final Date begin,
        final Date end) {
      return null;
    }

    @Override
    public List<SocialInformation> getSocialInformationListOfMyContacts(final String myId,
        final List<String> myContactsIds, final Date begin, final Date end) {
      return null;
    }
  }

  /**
   * After some trouble without understanding why
   * {@link org.silverpeas.core.personalization.UserPreferences} cannot be queried, the
   * personalization service is stubbed.
   */
  @Singleton
  @Alternative
  @Priority(APPLICATION + 100)
  public static class StubbedPersonalizationService extends DefaultPersonalizationService {

    @Override
    public UserPreferences getUserSettings(final String userId) {
      return new UserPreferences("fr", ZoneId.of("Europe/Paris"), "", "",
          false, false, false, UserMenuDisplay.DEFAULT);
    }
  }
}
