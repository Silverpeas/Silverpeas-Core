/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.comment.service.notification;

import static com.silverpeas.comment.service.notification.NotificationMatchers.isSetIn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.silverpeas.comment.mock.OrganizationControllerMocking;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.DefaultCommentService;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

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
  private static final String SETTINGS_PATH = "com.stratelia.webactiv.util.comment.Comment";
  /**
   * The user notification to test. It is partially mocked.
   */
  private CommentUserNotificationService notificationService = null;
  /**
   * The classified service to use in tests.
   */
  private ClassifiedService classifiedService = new ClassifiedService();
  /**
   * The comment to use in the test when invoking the callback.
   */
  private Comment concernedComment = null;
  /**
   * All of the comments on the classified used in the tests.
   */
  private List<Comment> classifiedComments = new ArrayList<Comment>();
  /**
   * The notification sender to mock and that will be used by the callback.
   */
  private NotificationSender notificationSender = null;
  /**
   * The captor of notification information passed to a mocked notification sender.
   */
  private ArgumentCaptor<NotificationMetaData> notifInfoCaptor =
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
    notificationService = spy(new CommentUserNotificationService());
    notificationService.register(ClassifiedService.COMPONENT_NAME, classifiedService);
    doReturn(mockCommentService()).when(notificationService).getCommentService();
    doReturn(mockNotificationSender()).when(notificationService).getNotificationSender(
        CLASSIFIED_INSTANCEID);
  }

  @After
  public void tearDown() {
    notificationService.unregister(ClassifiedService.COMPONENT_NAME);
  }

  /**
   * The commentAdded() method should notify both the author of the commented ad and the authors of
   * all of the ad's comments.
   */
  @Test
  public void commentAddedShouldNotifyClassifiedAndCommentAuthors() throws Exception {
    mockGetUserDetailReturnedValue();
    notificationService.commentAdded(concernedComment);
    verify(notificationService).getNotificationSender(CLASSIFIED_INSTANCEID);
    verify(notificationSender).notifyUser(notifInfoCaptor.capture());
    NotificationMetaData notif = getCapturedInfoInNotificiation();
    assertNotNull(notif);
    assertThat("The comment should be in the notification", concernedComment, isSetIn(notif));
    assertEquals(
        "The sender should be the author of the comment from which the callback is invoked",
        COMMENT_AUTHORID, notif.getSender());
    for (Comment aComment : classifiedComments) {
      UserRecipient authorId = new UserRecipient(String.valueOf(aComment.getOwnerId()));
      if (!authorId.getUserId().equals(String.valueOf(concernedComment.getOwnerId()))) {
        assertThat("The author '" + authorId + "' should be in the notification recipients",
            notif.getUserRecipients(), hasItem(authorId));
      } else {
        assertFalse("The author '" + authorId + "' shouldn't be in the notification recipients",
            notif.getUserRecipients().contains(authorId));
      }
    }
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
          new Comment(new CommentPK(String.valueOf(i), CLASSIFIED_INSTANCEID),
              COMMENT_RESOURCETYPE,
              classifiedPk, i, "Toto" + i, "comment " + i, date, date);
      aComment.setOwnerDetail(commentAuthor);
      classifiedComments.add(aComment);
    }
    Date date = new Date();
    UserDetail commentAuthor = new UserDetail();
    commentAuthor.setId(String.valueOf(COMMENT_AUTHORID));
    concernedComment = new Comment(
        new CommentPK("10", CLASSIFIED_INSTANCEID),
        COMMENT_RESOURCETYPE,
        classifiedPk,
        Integer.parseInt(COMMENT_AUTHORID),
        "Toto" + COMMENT_AUTHORID,
        "concerned comment",
        date,
        date);
    concernedComment.setOwnerDetail(commentAuthor);
    classifiedComments.add(concernedComment);
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
        new ForeignPK(String.valueOf(CLASSIFIED_ID), CLASSIFIED_INSTANCEID))).thenReturn(
        classifiedComments);
    when(commentService.getComponentSettings()).thenReturn(new ResourceLocator(SETTINGS_PATH, ""));
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
      public UserDetail answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        UserDetail userDetail = new UserDetail();
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
  protected NotificationMetaData getCapturedInfoInNotificiation() {
    return notifInfoCaptor.getValue();
  }
}