/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.core.workflow.engine.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.FieldMocker;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.workflow.api.UserManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.engine.user.ReplacementConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
@TestManagedBeans(ReplacementConstructor.class)
public class ReplacementUserNotificationTest {

  private static final String INSTANCE_ID = "workflow26";
  private static final String FR = "fr";
  private static final String EN = "en";
  private static final String DE = "de";
  private static final String COMPONENT_NAME = "componentNameTest";

  @RegisterExtension
  FieldMocker mocker = new FieldMocker();

  @TestManagedMock
  private PublicationService publicationService;
  @TestManagedMock
  private ComponentAccessControl componentAccessControl;
  @TestManagedMock
  private Administration administration;

  private final org.silverpeas.core.workflow.api.user.User userA = mock(
      org.silverpeas.core.workflow.api.user.User.class);
  private final org.silverpeas.core.workflow.api.user.User userB = mock(
      org.silverpeas.core.workflow.api.user.User.class);

  @SuppressWarnings("unchecked")
  @BeforeEach
  public void setup(@TestManagedMock UserProvider userProvider,
      @TestManagedMock UserManager userManager,
      @TestManagedMock OrganizationController organizationController,
      @TestManagedMock SilverpeasComponentInstanceProvider silverpeasComponentInstanceProvider)
      throws WorkflowException {
    ComponentInst componentInstance = mock(ComponentInst.class);

    when(silverpeasComponentInstanceProvider.getById(INSTANCE_ID))
        .thenReturn(Optional.of(componentInstance));

    when(userA.getUserId()).thenReturn("i");
    when(userA.getFullName()).thenReturn("incumbent");
    when(userB.getUserId()).thenReturn("s");
    when(userB.getFullName()).thenReturn("substitute");

    when(componentInstance.getName("fr")).thenReturn(COMPONENT_NAME + "_fr");
    when(componentInstance.getName("de")).thenReturn(COMPONENT_NAME + "_de");
    when(componentInstance.getName("en")).thenReturn(COMPONENT_NAME + "_en");
    when(organizationController.getComponentInst(INSTANCE_ID)).thenReturn(componentInstance);

    when(userProvider.getCurrentRequester()).thenReturn(mock(User.class));

    when(userManager.getUser(userA.getUserId())).thenReturn(userA);
    when(userManager.getUser(userB.getUserId())).thenReturn(userB);

    mocker.setField(DisplayI18NHelper.class, Locale.getDefault().getLanguage(), "defaultLanguage");

    when(componentAccessControl.isUserAuthorized(anyString(), anyString())).thenReturn(true);
    when(componentAccessControl.isGroupAuthorized(anyString(), anyString())).thenReturn(true);
  }

  @Test
  public void createToIncumbentOnOneDay() {
    final Replacement replacement = initOnDayReplacement();
    final UserNotification notification = new ToIncumbentReplacementNotificationBuilder(replacement,
        NotifAction.CREATE).build();
    final Map<String, String> titles = computeNotificationTitles(notification);
    assertThat(titles.get(DE), is("New replacement on componentNameTest_de"));
    assertThat(titles.get(EN), is("New replacement on componentNameTest_en"));
    assertThat(titles.get(FR), is("Nouveau remplacement sur componentNameTest_fr"));
    final Map<String, String> contents = computeNotificationContents(notification);
    assertThat(contents.get(DE), is("New substitute <b>substitute</b> on <b>24.06.2018</b>."));
    assertThat(contents.get(EN), is("New substitute <b>substitute</b> on <b>06/24/2018</b>."));
    assertThat(contents.get(FR), is("Nouveau remplaçant <b>substitute</b> le <b>24/06/2018</b>."));
  }

  @Test
  public void createToIncumbentOnSeveralDays() {
    final Replacement replacement = initSeveralDayReplacement();
    final UserNotification notification = new ToIncumbentReplacementNotificationBuilder(replacement,
        NotifAction.CREATE).build();
    final Map<String, String> titles = computeNotificationTitles(notification);
    assertThat(titles.get(DE), is("New replacement on componentNameTest_de"));
    assertThat(titles.get(EN), is("New replacement on componentNameTest_en"));
    assertThat(titles.get(FR), is("Nouveau remplacement sur componentNameTest_fr"));
    final Map<String, String> contents = computeNotificationContents(notification);
    assertThat(contents.get(DE), is("New substitute <b>substitute</b> from <b>24.06.2018</b> to <b>25.06.2018</b>."));
    assertThat(contents.get(EN), is("New substitute <b>substitute</b> from <b>06/24/2018</b> to <b>06/25/2018</b>."));
    assertThat(contents.get(FR), is("Nouveau remplaçant <b>substitute</b> du <b>24/06/2018</b> au <b>25/06/2018</b>."));
  }

  @Test
  public void updateToIncumbentOnOneDay() {
    final Replacement replacement = initOnDayReplacement();
    final UserNotification notification = new ToIncumbentReplacementNotificationBuilder(replacement,
        NotifAction.UPDATE).build();
    final Map<String, String> titles = computeNotificationTitles(notification);
    assertThat(titles.get(DE), is("Replacement modification on componentNameTest_de"));
    assertThat(titles.get(EN), is("Replacement modification on componentNameTest_en"));
    assertThat(titles.get(FR), is("Modification d'un remplacement sur componentNameTest_fr"));
    final Map<String, String> contents = computeNotificationContents(notification);
    assertThat(contents.get(DE), is("Period modification of your replacement by <b>substitute</b>: on <b>24.06.2018</b>."));
    assertThat(contents.get(EN), is("Period modification of your replacement by <b>substitute</b>: on <b>06/24/2018</b>."));
    assertThat(contents.get(FR), is("Changement de période de votre remplacement par <b>substitute</b> : le <b>24/06/2018</b>."));
  }

  @Test
  public void deleteToIncumbentOnOneDay() {
    final Replacement replacement = initOnDayReplacement();
    final UserNotification notification = new ToIncumbentReplacementNotificationBuilder(replacement,
        NotifAction.DELETE).build();
    final Map<String, String> titles = computeNotificationTitles(notification);
    assertThat(titles.get(DE), is("Replacement deletion on componentNameTest_de"));
    assertThat(titles.get(EN), is("Replacement deletion on componentNameTest_en"));
    assertThat(titles.get(FR), is("Suppression d'un remplacement sur componentNameTest_fr"));
    final Map<String, String> contents = computeNotificationContents(notification);
    assertThat(contents.get(DE), is("Deletion of your replacement by <b>substitute</b> on <b>24.06.2018</b>."));
    assertThat(contents.get(EN), is("Deletion of your replacement by <b>substitute</b> on <b>06/24/2018</b>."));
    assertThat(contents.get(FR), is("Suppression de votre remplacement par <b>substitute</b> le <b>24/06/2018</b>."));
  }

  @Test
  public void createToSubstituteOnOneDay() {
    final Replacement replacement = initOnDayReplacement();
    final UserNotification notification = new ToSubstituteReplacementNotificationBuilder(replacement,
        NotifAction.CREATE).build();
    final Map<String, String> titles = computeNotificationTitles(notification);
    assertThat(titles.get(DE), is("New replacement on componentNameTest_de"));
    assertThat(titles.get(EN), is("New replacement on componentNameTest_en"));
    assertThat(titles.get(FR), is("Nouveau remplacement sur componentNameTest_fr"));
    final Map<String, String> contents = computeNotificationContents(notification);
    assertThat(contents.get(DE), is("New replacement on <b>24.06.2018</b> as <b>incumbent</b>'s substitute."));
    assertThat(contents.get(EN), is("New replacement on <b>06/24/2018</b> as <b>incumbent</b>'s substitute."));
    assertThat(contents.get(FR), is("Nouveau remplacement le <b>24/06/2018</b> en tant que <b>incumbent</b>."));
  }

  @Test
  public void updateToSubstituteOnOneDay() {
    final Replacement replacement = initOnDayReplacement();
    final UserNotification notification = new ToSubstituteReplacementNotificationBuilder(replacement,
        NotifAction.UPDATE).build();
    final Map<String, String> titles = computeNotificationTitles(notification);
    assertThat(titles.get(DE), is("Replacement modification on componentNameTest_de"));
    assertThat(titles.get(EN), is("Replacement modification on componentNameTest_en"));
    assertThat(titles.get(FR), is("Modification d'un remplacement sur componentNameTest_fr"));
    final Map<String, String> contents = computeNotificationContents(notification);
    assertThat(contents.get(DE), is("Period modification of your replacement as <b>incumbent</b>'s substitute: on <b>24.06.2018</b>."));
    assertThat(contents.get(EN), is("Period modification of your replacement as <b>incumbent</b>'s substitute: on <b>06/24/2018</b>."));
    assertThat(contents.get(FR), is("Changement de période de votre remplacement en tant que <b>incumbent</b> : le <b>24/06/2018</b>."));
  }

  @Test
  public void deleteToSubstituteOnOneDay() {
    final Replacement replacement = initOnDayReplacement();
    final UserNotification notification = new ToSubstituteReplacementNotificationBuilder(replacement,
        NotifAction.DELETE).build();
    final Map<String, String> titles = computeNotificationTitles(notification);
    assertThat(titles.get(DE), is("Replacement deletion on componentNameTest_de"));
    assertThat(titles.get(EN), is("Replacement deletion on componentNameTest_en"));
    assertThat(titles.get(FR), is("Suppression d'un remplacement sur componentNameTest_fr"));
    final Map<String, String> contents = computeNotificationContents(notification);
    assertThat(contents.get(DE), is("Deletion of your replacement as <b>incumbent</b>'s substitute on <b>24.06.2018</b>."));
    assertThat(contents.get(EN), is("Deletion of your replacement as <b>incumbent</b>'s substitute on <b>06/24/2018</b>."));
    assertThat(contents.get(FR), is("Suppression de votre remplacement en tant que <b>incumbent</b> le <b>24/06/2018</b>."));
  }

  private Replacement initOnDayReplacement() {
    return Replacement.between(userA, userB).inWorkflow(INSTANCE_ID)
        .during(Period.between(LocalDate.of(2018, 6, 24), LocalDate.of(2018, 6, 25)));
  }

  private Replacement initSeveralDayReplacement() {
    return Replacement.between(userA, userB).inWorkflow(INSTANCE_ID)
        .during(Period.between(LocalDate.of(2018, 6, 24), LocalDate.of(2018, 6, 26)));
  }

  private Map<String, String> computeNotificationContents(UserNotification userNotification) {
    final Map<String, String> result = new HashMap<>();
    result.put(FR, getContent(userNotification, FR));
    result.put(EN, getContent(userNotification, EN));
    result.put(DE, getContent(userNotification, DE));
    assertThat(result.get(FR), not(is(result.get(EN))));
    assertThat(result.get(EN), not(is(result.get(DE))));
    return result;
  }

  private Map<String, String> computeNotificationTitles(UserNotification userNotification) {
    final Map<String, String> result = new HashMap<>();
    result.put(FR, getTitle(userNotification, FR));
    result.put(EN, getTitle(userNotification, EN));
    result.put(DE, getTitle(userNotification, DE));
    assertThat(result.get(FR), not(is(result.get(EN))));
    assertThat(result.get(EN), not(is(result.get(DE))));
    return result;
  }

  private String getContent(final UserNotification userNotification, final String language) {
    return userNotification.getNotificationMetaData().getContent(language)
        .replaceAll("<!--BEFORE_MESSAGE_FOOTER--><!--AFTER_MESSAGE_FOOTER-->", "");
  }

  private String getTitle(final UserNotification userNotification, final String language) {
    return userNotification.getNotificationMetaData().getTitle(language);
  }
}