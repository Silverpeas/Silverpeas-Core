/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package com.stratelia.silverpeas.notificationManager;

import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.test.rule.MockByReflectionRule;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class NotificationMetaDataTest {

  private static final String USER_SENDER = "2406";

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  private NotificationMetaData current;
  private ResourceLocator mockedSettings;

  @Before
  public void setup() throws Exception {
    current = new NotificationMetaData();

    // Settings
    mockedSettings = reflectionRule
        .mockField(NotificationManagerSettings.class, ResourceLocator.class, "settings");

    // Organization controller
    final OrganisationController mockedOrganisationController = reflectionRule
        .mockField(OrganisationControllerFactory.class, OrganisationController.class,
            "instance.organisationController");
    when(mockedOrganisationController.getUserDetail(anyString())).thenReturn(new UserDetail());
    when(mockedOrganisationController.getGroup(anyString())).thenAnswer(new Answer<Group>() {
      @Override
      public Group answer(final InvocationOnMock invocation) throws Throwable {
        Group group = new Group();
        group.setId((String) invocation.getArguments()[0]);
        group.setNbUsers(5);
        return group;
      }
    });

    // Notification Manager
    final NotificationManager mockedNotificationManager =
        reflectionRule.mockField(current, NotificationManager.class, "notificationManager");
    when(mockedNotificationManager.getUsersFromGroup(anyString()))
        .thenAnswer(new Answer<Collection<UserRecipient>>() {
          @Override
          public Collection<UserRecipient> answer(final InvocationOnMock invocation)
              throws Throwable {
            String groupId = (String) invocation.getArguments()[0];
            return Arrays
                .asList(new UserRecipient(groupId + "_1"), new UserRecipient(groupId + "_2"),
                    new UserRecipient(groupId + "_3"));
          }
        });
  }

  /*
  TESTS around
  {@link NotificationMetaData#displayGroup(String)}.
   */

  @Test
  public void displayGroupWithDisplayGroupNotEnabledAndThresholdNotSet() {
    assertThat(current.displayGroup("1"), is(false));
  }

  @Test
  public void displayGroupWithDisplayGroupEnabledAndThresholdNotSet() {
    enableDisplayGroup();
    assertThat(current.displayGroup("1"), is(true));
  }

  @Test
  public void displayGroupWithDisplayGroupNotEnabledAndHighThresholdSet() {
    setThresholdTo(50);
    assertThat(current.displayGroup("1"), is(false));
  }

  @Test
  public void displayGroupWithDisplayGroupNotEnabledAndNbUserInGroupEqualsThresholdSet() {
    setThresholdTo(5);
    assertThat(current.displayGroup("1"), is(false));
  }

  @Test
  public void displayGroupWithDisplayGroupNotEnabledAndNbUserInGroupGreaterThanThresholdSet() {
    setThresholdTo(4);
    assertThat(current.displayGroup("1"), is(true));
  }

  /*
  TESTS around
  {@link NotificationMetaData#getAllUserRecipients()}.
   */

  @Test
  public void getAllUserRecipientsButNoSenderAndNoRecipient() throws Exception {
    assertThat(current.getAllUserRecipients(), empty());
    assertThat(current.getUserRecipientsToExclude(), empty());
  }

  @Test
  public void getAllUserRecipientsWithUserSenderAndNoRecipient() throws Exception {
    current.setSender(USER_SENDER);

    assertThat(current.getAllUserRecipients(), empty());
    assertThat(current.getUserRecipientsToExclude(), empty());
  }

  @Test
  public void getAllUserRecipientsButNoSenderAndTwoUserRecipients() throws Exception {
    current.addUserRecipient(new UserRecipient("26"));
    current.addUserRecipient(new UserRecipient("38"));

    assertThat(current.getAllUserRecipients(),
        containsInAnyOrder(current.getUserRecipients().toArray()));
    assertThat(current.getUserRecipientsToExclude(), empty());
  }

  @Test
  public void getAllUserRecipientsWithUserSenderAndTwoUserRecipients() throws Exception {
    current.setSender(USER_SENDER);
    current.addUserRecipient(new UserRecipient("26"));
    current.addUserRecipient(new UserRecipient("38"));

    assertThat(current.getAllUserRecipients(),
        containsInAnyOrder(current.getUserRecipients().toArray()));
    assertThat(current.getUserRecipientsToExclude(), empty());
  }

  @Test
  public void getAllUserRecipientsButNoSenderAndTwoGroupRecipients() throws Exception {
    current.addGroupRecipient(new GroupRecipient("26"));
    current.addGroupRecipient(new GroupRecipient("38"));

    assertThat(current.getAllUserRecipients(),
        containsInAnyOrder(new UserRecipient("26_1"), new UserRecipient("26_2"),
            new UserRecipient("26_3"), new UserRecipient("38_1"), new UserRecipient("38_2"),
            new UserRecipient("38_3")));
    assertThat(current.getUserRecipientsToExclude(), empty());
  }

  @Test
  public void getAllUserRecipientsWithUserSenderAndTwoGroupRecipients() throws Exception {
    current.setSender(USER_SENDER);
    current.addGroupRecipient(new GroupRecipient("26"));
    current.addGroupRecipient(new GroupRecipient("38"));

    assertThat(current.getAllUserRecipients(),
        containsInAnyOrder(new UserRecipient("26_1"), new UserRecipient("26_2"),
            new UserRecipient("26_3"), new UserRecipient("38_1"), new UserRecipient("38_2"),
            new UserRecipient("38_3")));
    assertThat(current.getUserRecipientsToExclude(), empty());
  }

  @Test
  public void getAllUserRecipientsButNoSenderAndThreeUserRecipientsAndOneGroupRecipients()
      throws Exception {
    current.addUserRecipient(new UserRecipient(USER_SENDER));
    current.addUserRecipient(new UserRecipient("26"));
    current.addUserRecipient(new UserRecipient("38"));
    current.addGroupRecipient(new GroupRecipient("GU"));

    assertThat(current.getAllUserRecipients(),
        containsInAnyOrder(new UserRecipient(USER_SENDER), new UserRecipient("26"),
            new UserRecipient("38"), new UserRecipient("GU_1"), new UserRecipient("GU_2"),
            new UserRecipient("GU_3")));
    assertThat(current.getUserRecipientsToExclude(), empty());
  }

  @Test
  public void getAllUserRecipientsWithUserSenderAndThreeUserRecipientsAndOneGroupRecipient()
      throws Exception {
    current.setSender(USER_SENDER);
    current.addUserRecipient(new UserRecipient(USER_SENDER));
    current.addUserRecipient(new UserRecipient("26"));
    current.addUserRecipient(new UserRecipient("38"));
    current.addGroupRecipient(new GroupRecipient("GU"));

    assertThat(current.getAllUserRecipients(),
        containsInAnyOrder(new UserRecipient("26"), new UserRecipient("38"),
            new UserRecipient("GU_1"), new UserRecipient("GU_2"), new UserRecipient("GU_3")));
    assertThat(current.getUserRecipientsToExclude(), empty());
  }

  @Test
  public void
  getAllUserRecipientsWithUserSenderAndThreeUserRecipientsAndOneGroupRecipientAndModifyingInternalContainer()
      throws Exception {
    current.setSender(USER_SENDER);
    current.addUserRecipient(new UserRecipient(USER_SENDER));
    current.addUserRecipient(new UserRecipient("26"));
    current.addUserRecipient(new UserRecipient("38"));
    current.addGroupRecipient(new GroupRecipient("GU"));

    assertThat(current.getAllUserRecipients(true),
        containsInAnyOrder(new UserRecipient("26"), new UserRecipient("38"),
            new UserRecipient("GU_1"), new UserRecipient("GU_2"), new UserRecipient("GU_3")));
    assertThat(current.getUserRecipientsToExclude(), contains(new UserRecipient(USER_SENDER)));
  }

  @Test
  public void getAllUserRecipientsWithUserSenderAndThreeUserRecipientsAndOneUserRecipientExcluded()
      throws Exception {
    current.setSender(USER_SENDER);
    current.addUserRecipientToExclude(new UserRecipient("Excluded"));
    current.addUserRecipient(new UserRecipient(USER_SENDER));
    current.addUserRecipient(new UserRecipient("26"));
    current.addUserRecipient(new UserRecipient("38"));
    current.addGroupRecipient(new GroupRecipient("GU"));

    assertThat(current.getAllUserRecipients(),
        containsInAnyOrder(new UserRecipient("26"), new UserRecipient("38"),
            new UserRecipient("GU_1"), new UserRecipient("GU_2"), new UserRecipient("GU_3")));
    assertThat(current.getUserRecipientsToExclude(), contains(new UserRecipient("Excluded")));
  }

  @Test
  public void
  getAllUserRecipientsWithUserSenderAndThreeUserRecipientsAndOneUserRecipientExcludedAndModifyingInternalContainer()
      throws Exception {
    current.setSender(USER_SENDER);
    current.addUserRecipientToExclude(new UserRecipient("Excluded"));
    current.addUserRecipient(new UserRecipient(USER_SENDER));
    current.addUserRecipient(new UserRecipient("26"));
    current.addUserRecipient(new UserRecipient("38"));
    current.addGroupRecipient(new GroupRecipient("GU"));

    assertThat(current.getAllUserRecipients(true),
        containsInAnyOrder(new UserRecipient("26"), new UserRecipient("38"),
            new UserRecipient("GU_1"), new UserRecipient("GU_2"), new UserRecipient("GU_3")));
    assertThat(current.getUserRecipientsToExclude(),
        containsInAnyOrder(new UserRecipient("Excluded"), new UserRecipient(USER_SENDER)));
  }

  /*
  TESTS around
  {@link NotificationMetaData#isManualUserOne()}.
   */

  @Test
  public void isManualUserOneWithNoSenderAndNotManualOne() throws Exception {
    assertThat(current.isManualUserOne(), is(false));
  }

  @Test
  public void isManualUserOneWithSenderAndNotManualOne() throws Exception {
    current.setSender(USER_SENDER);

    assertThat(current.isManualUserOne(), is(false));
  }

  @Test
  public void isManualUserOneWithNoSender() throws Exception {
    current.manualUserNotification();

    assertThat(current.isManualUserOne(), is(true));
  }

  @Test
  public void isManualUserOneWithSender() throws Exception {
    current.manualUserNotification();
    current.setSender(USER_SENDER);

    assertThat(current.isManualUserOne(), is(true));
  }

  /*
  CURRENT TEST TOOLS
   */

  private void enableDisplayGroup() {
    when(mockedSettings.getBoolean("notif.receiver.displayGroup", false)).thenReturn(true);
  }

  private void setThresholdTo(int value) {
    when(mockedSettings.getInteger("notif.receiver.displayUser.threshold", 0)).thenReturn(value);
  }
}