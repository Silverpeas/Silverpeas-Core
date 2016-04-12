/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.comment.service.notification;

import com.silverpeas.comment.mock.OrganizationControllerMocking;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.DefaultCommentService;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.util.ForeignPK;
import org.silverpeas.core.util.ResourceLocator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.silverpeas.comment.service.notification.NotificationMatchers.isSetIn;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests notification of the users at comment adding on a given Silverpeas content.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-comment-user-notification.xml")
public class CommentUserNotificationServiceTest {

  /**
   * Id of the commented classified.
   */
  private static final String CLASSIFIED_ID = "230";
  /**
   * Id of the author that wrote the comment concerned by the tests.
   */
  private static final String COMMENT_AUTHORID = "3";
  /**
   * Id of the user that don't have enough rights to access the commented resource.
   */
  private static final String UNAUTHORIZED_USERID = "20";
  /**
   * Type of the commented resource concerned by the tests.
   */
  private static final String COMMENT_RESOURCETYPE = aClassified().getContributionType();
  /*
   * Id of the classified component instance to use in the tests.
   */
  private static final String CLASSIFIED_INSTANCEID = "classifieds3";
  /**
   * The settings of the comment core component.
   */
  private static final String SETTINGS_PATH = "org.silverpeas.util.comment.Comment";
  /**
   * The user notification to test. It is partially mocked.
   */
  private DefaultCommentUserNotificationService notificationService = null;
  /**
   * The classified service to use in tests.
   */
  private final ClassifiedService classifiedService = new ClassifiedService();
  /**
   * The comment to use in the test when invoking the callback.
   */
  private Comment concernedComment = null;
  /**
   * All of the comments on the classified used in the tests.
   */
  private final List<Comment> classifiedComments = new ArrayList<Comment>();
  /**
   * The notification sender to mock and that will be used by the callback.
   */
  private NotificationSender notificationSender = null;
  /**
   * The captor of notification information passed to a mocked notification sender.
   */
  private final ArgumentCaptor<NotificationMetaData> notifInfoCaptor =
      ArgumentCaptor.forClass(NotificationMetaData.class);
  @Inject
  private OrganizationControllerMocking organizationController;

  public CommentUserNotificationServiceTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    setUpClassifieds();
    setUpClassifiedComments();
    notificationService = spy(new DefaultCommentUserNotificationService());
    notificationService.register(ClassifiedService.COMPONENT_NAME, classifiedService);
    doReturn(mockCommentService()).when(notificationService).getCommentService();
    doReturn(mockNotificationSender()).when(notificationService)
        .getNotificationSender(CLASSIFIED_INSTANCEID);
    mockGetUserDetailReturnedValue();
  }

  @After
  public void tearDown() {
    notificationService.unregister(ClassifiedService.COMPONENT_NAME);
  }

  /**
   * The commentAdded() method should notify both the author of the commented ad and the authors of
   * all of the ad's comments.
   * @throws java.lang.Exception if an error occurs during the test.
   */
  @Test
  public void commentAddedShouldNotifyClassifiedAndCommentAuthors() throws Exception {
    notificationService.commentAdded(concernedComment);
    verify(notificationService).getNotificationSender(CLASSIFIED_INSTANCEID);
    verify(notificationSender).notifyUser(notifInfoCaptor.capture());
    NotificationMetaData notif = getCapturedInfoInNotification();
    assertNotNull(notif);
    assertThat("The comment should be in the notification", concernedComment, isSetIn(notif));
    assertEquals(
        "The sender should be the author of the comment from which the callback is invoked",
        COMMENT_AUTHORID, notif.getSender());
    for (Comment aComment : classifiedComments) {
      UserRecipient authorId = new UserRecipient(String.valueOf(aComment.getOwnerId()));
      if (!authorId.getUserId().equals(String.valueOf(concernedComment.getOwnerId()))) {
        assertThat("The author '" + authorId + "' should be in the notification recipients",
            authorId, isIn(notif.getUserRecipients()));
      } else {
        assertThat("The author '" + authorId + "' shouldn't be in the notification recipients",
            authorId, not(isIn(notif.getUserRecipients())));
      }
    }
  }

  /**
   * Tests the notification about a comment isn't sent to the users that don't have enough rights
   * to
   * access the commented resource. To access a commented resource, a user first must have right to
   * access the component instance and then to the commented resource.
   * @throws java.lang.Exception if an error occurs during the test.
   */
  @Test
  public void notificationsAreNotSentToUsersWithoutEnoughAccessRights() throws Exception {
    addAUserWithoutEnoughAccessRights();
    notificationService.commentAdded(concernedComment);
    verify(notificationSender).notifyUser(notifInfoCaptor.capture());
    NotificationMetaData notification = getCapturedInfoInNotification();
    UserRecipient unauthorizedUser = new UserRecipient(UNAUTHORIZED_USERID);
    UserRecipient author = new UserRecipient(concernedComment.getOwner());
    assertThat("The unauthorized user shouldn't be notified", unauthorizedUser,
        not(isIn(notification.getUserRecipients())));
    assertThat("The author of the comment shouldn't be notified", author,
        not(isIn(notification.getUserRecipients())));
    assertThat("Others users should be notified", notification.getUserRecipients().size(),
        is(classifiedComments.size() - 2));
  }

  /**
   * Sets up all of the comments about the classified used in the current test. The comment to use
   * in the invocation of the callback is also set.
   */
  protected void setUpClassifiedComments() {
    ForeignPK classifiedPk = new ForeignPK(String.valueOf(CLASSIFIED_ID), CLASSIFIED_INSTANCEID);
    for (int i = 0; i < 5; i++) {
      Date date = new Date();
      UserDetail commentAuthor = new UserDetail();
      commentAuthor.setId(String.valueOf(i));
      Comment aComment =
          new Comment(new CommentPK(String.valueOf(i), CLASSIFIED_INSTANCEID), COMMENT_RESOURCETYPE,
              classifiedPk, i, "Toto" + i, "comment " + i, date, date);
      aComment.setOwnerDetail(commentAuthor);
      classifiedComments.add(aComment);
    }
    Date date = new Date();
    UserDetail commentAuthor = new UserDetail();
    commentAuthor.setId(String.valueOf(COMMENT_AUTHORID));
    concernedComment =
        new Comment(new CommentPK("10", CLASSIFIED_INSTANCEID), COMMENT_RESOURCETYPE, classifiedPk,
            Integer.parseInt(COMMENT_AUTHORID), "Toto" + COMMENT_AUTHORID, "concerned comment",
            date, date);
    concernedComment.setOwnerDetail(commentAuthor);
    classifiedComments.add(concernedComment);
  }

  protected void addAUserWithoutEnoughAccessRights() {
    int rank = classifiedComments.size() + 1;
    Date date = new Date();
    UserDetail author = new UserDetail();
    author.setId(UNAUTHORIZED_USERID);
    Comment aComment = new Comment(new CommentPK(String.valueOf(rank), CLASSIFIED_INSTANCEID),
        COMMENT_RESOURCETYPE, new ForeignPK(String.valueOf(CLASSIFIED_ID), CLASSIFIED_INSTANCEID),
        rank, "Toto" + rank, "comment " + rank, date, date);
    aComment.setOwnerDetail(author);
    classifiedComments.add(aComment);
    Classified classified = classifiedService.getContentById(CLASSIFIED_ID);
    classified.unauthorize(author);
  }

  protected void setUpClassifieds() {
    Classified classified = aClassified();
    classifiedService.putContent(classified);
  }

  protected static Classified aClassified() {
    UserDetail author = new UserDetail();
    author.setId("0");
    Classified classified = new Classified(CLASSIFIED_ID, CLASSIFIED_INSTANCEID).
        createdBy(author).
        entitled("a classified");
    return classified;
  }

  /**
   * Mocks the DefaultCommentService to use by the callback. It is expected all of other comments
   * are asked by the callback to get their authors. So that it can notify them about the new
   * comment.
   * @return the mocked comment controller.
   * @throws Exception - it is just for satisfying the contract of some called methods of
   * DefaultCommentService.
   */
  protected CommentService mockCommentService() throws Exception {
    CommentService commentService = mock(DefaultCommentService.class);
    when(commentService.getAllCommentsOnPublication(COMMENT_RESOURCETYPE,
        new ForeignPK(String.valueOf(CLASSIFIED_ID), CLASSIFIED_INSTANCEID)))
        .thenReturn(classifiedComments);
    when(commentService.getComponentSettings()).thenReturn(
        ResourceLocator.getSettingBundle(SETTINGS_PATH));
    return commentService;
  }

  /**
   * Mocks the NotificationSender instance to use by the callback. It is expected it is used by the
   * callback for sending notification to users. The notification information passed to the sender
   * is captured.
   * @return the mocked notification sender.
   * @throws Exception - it is just for satisfying the contract of some called methods of
   * NotifySender.
   */
  protected NotificationSender mockNotificationSender() throws Exception {
    notificationSender = mock(NotificationSender.class);
    return notificationSender;
  }

  protected void mockGetUserDetailReturnedValue() {
    doAnswer(new Answer<UserDetail>() {
      @Override
      public UserDetail answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        UserDetail userDetail = new UserDetail() {
          @Override
          public boolean isAnonymous() {
            return false;
          }
        };
        userDetail.setId((String) args[0]);
        return userDetail;
      }
    }).when(organizationController.getMock()).getUserDetail(anyString());
  }

  /**
   * Gets the captured information from the notification sender at notifiyUser() call by the
   * callback.
   * @return the notification information passed by the callback to the notification sender.
   */
  protected NotificationMetaData getCapturedInfoInNotification() {
    return notifInfoCaptor.getValue();
  }
}
