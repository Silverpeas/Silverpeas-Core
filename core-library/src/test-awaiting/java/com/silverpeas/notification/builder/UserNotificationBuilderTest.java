/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.usernotification.builder;

import com.silverpeas.usernotification.builder.helper.UserNotificationHelper;
import com.silverpeas.usernotification.builder.mock.OrganizationControllerMock;
import com.silverpeas.usernotification.model.NotificationResourceData;
import com.silverpeas.ui.DisplayI18NHelper;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.util.template.SilverpeasTemplate;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.client.constant.NotifMessageType;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.core.test.rule.MockByReflectionRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Yohann Chastagnier
 */
public class UserNotificationBuilderTest {

  @Rule
  public MockByReflectionRule mockByReflectionRule = new MockByReflectionRule();

  private static final String TECHNICAL_CONTENT =
      "<!--BEFORE_MESSAGE_FOOTER--><!--AFTER_MESSAGE_FOOTER-->";

  OrganisationController organizationController;

  @Before
  public void setup() {
    organizationController = mockByReflectionRule
        .mockField(OrganisationControllerFactory.class, OrganisationController.class,
            "instance.organisationController");
    when(organizationController.getUserDetail(anyString())).thenReturn(new UserDetail());
  }

  @Test
  public void testBuild_AUNB_1() {

    // Build
    NotificationMetaData notifTest = UserNotificationHelper.build(new AUNBTest() {

      @Override
      protected Collection<String> getUserIdsToNotify() {
        return null;
      }
    });
    assertThat(notifTest, nullValue());

    // Asserts
    notifTest = UserNotificationHelper.build(new AUNBTest());
    assertThat(notifTest, notNullValue());
    assertThat(notifTest.getMessageType(), is(NotifMessageType.NORMAL.getId()));
    assertThat(notifTest.getAction(), is(NotifAction.CREATE));
    assertThat(notifTest.getComponentId(), is("aComponentInstanceId"));
    assertThat(notifTest.getSender(), is("aSenderId"));
    assertThat(notifTest.getContent(), is(TECHNICAL_CONTENT));
    assertThat(notifTest.getDate(), notNullValue());
    assertThat(notifTest.getFileName(), nullValue());
    assertThat(notifTest.getLanguages().size(), is(0));
    assertThat(notifTest.getLink(), is(""));
    assertThat(notifTest.getSource(), is(""));
    assertThat(notifTest.getTitle(), nullValue());
    assertThat(notifTest.getTemplates().size(), is(0));
    assertThat(notifTest.isSendImmediately(), is(false));
    assertThat(notifTest.isAnswerAllowed(), is(false));
    assertThat(notifTest.getUserRecipients().size(), is(1));
  }

  @Test
  public void testBuild_AUNB_1Bis() {

    // Build
    final NotificationMetaData notifTest =
        UserNotificationHelper.build(new AUNBTest() {

          @Override
          protected NotifMessageType getMessageType() {
            return NotifMessageType.URGENT;
          }

          @Override
          protected boolean isSendImmediatly() {
            return true;
          }

          @Override
          protected NotifAction getAction() {
            return NotifAction.REPORT;
          }

          @Override
          protected void performBuild() {
            super.performBuild();
            getNotificationMetaData().setTitle("Title_ANB_1Bis");
            getNotificationMetaData().setContent("Content_ANB_1Bis");
          }
        });

    // Asserts
    assertThat(notifTest, notNullValue());
    assertThat(notifTest.getMessageType(), is(NotifMessageType.URGENT.getId()));
    assertThat(notifTest.getAction(), is(NotifAction.REPORT));
    assertThat(notifTest.getComponentId(), is("aComponentInstanceId"));
    assertThat(notifTest.getSender(), is("aSenderId"));
    assertThat(notifTest.getContent(), is("Content_ANB_1Bis" + TECHNICAL_CONTENT));
    assertThat(notifTest.getDate(), notNullValue());
    assertThat(notifTest.getFileName(), nullValue());
    assertThat(notifTest.getLanguages().size(), is(1));
    assertThat(notifTest.getLink(), is(""));
    assertThat(notifTest.getSource(), is(""));
    assertThat(notifTest.getTitle(), is("Title_ANB_1Bis"));
    assertThat(notifTest.getTemplates().size(), is(0));
    assertThat(notifTest.isSendImmediately(), is(true));
    assertThat(notifTest.isAnswerAllowed(), is(false));
    assertThat(notifTest.getNotificationResourceData(), nullValue());
    assertThat(notifTest.getUserRecipients().size(), is(1));
  }

  @Test
  public void testBuild_AUNB_2() {

    // Build
    final NotificationMetaData notifTest =
        UserNotificationHelper.build(new AUNBTest(null, null) {

          @Override
          protected String getSender() {
            return "aSenderId";
          }

          @Override
          protected String getComponentInstanceId() {
            return "aComponentInstanceId";
          }

          @Override
          protected NotifAction getAction() {
            return NotifAction.CREATE;
          }
        });

    // Asserts
    assertThat(notifTest, notNullValue());
    assertThat(notifTest.getMessageType(), is(NotifMessageType.NORMAL.getId()));
    assertThat(notifTest.getAction(), is(NotifAction.CREATE));
    assertThat(notifTest.getComponentId(), is("aComponentInstanceId"));
    assertThat(notifTest.getSender(), is("aSenderId"));
    assertThat(notifTest.getContent(), is(TECHNICAL_CONTENT));
    assertThat(notifTest.getDate(), notNullValue());
    assertThat(notifTest.getFileName(), nullValue());
    assertThat(notifTest.getLanguages().size(), is(0));
    assertThat(notifTest.getLink(), is(""));
    assertThat(notifTest.getSource(), is(""));
    assertThat(notifTest.getTitle(), nullValue());
    assertThat(notifTest.getTemplates().size(), is(0));
    assertThat(notifTest.isSendImmediately(), is(false));
    assertThat(notifTest.isAnswerAllowed(), is(false));
    assertThat(notifTest.getNotificationResourceData(), nullValue());
    assertThat(notifTest.getUserRecipients().size(), is(1));
  }

  @Test
  public void testBuild_AUNB_2Bis() {

    // Build
    final NotificationMetaData notifTest =
        UserNotificationHelper.build(new AUNBTest("aTitle", "aContent"));

    // Asserts
    assertThat(notifTest, notNullValue());
    assertThat(notifTest.getMessageType(), is(NotifMessageType.NORMAL.getId()));
    assertThat(notifTest.getAction(), is(NotifAction.CREATE));
    assertThat(notifTest.getComponentId(), is("aComponentInstanceId"));
    assertThat(notifTest.getSender(), is("aSenderId"));
    assertThat(notifTest.getContent(), is("aContent" + TECHNICAL_CONTENT));
    assertThat(notifTest.getDate(), notNullValue());
    assertThat(notifTest.getFileName(), nullValue());
    assertThat(notifTest.getLanguages().size(), is(1));
    assertThat(notifTest.getLink(), is(""));
    assertThat(notifTest.getSource(), is(""));
    assertThat(notifTest.getTitle(), is("aTitle"));
    assertThat(notifTest.getTemplates().size(), is(0));
    assertThat(notifTest.isSendImmediately(), is(false));
    assertThat(notifTest.isAnswerAllowed(), is(false));
    assertThat(notifTest.getNotificationResourceData(), nullValue());
    assertThat(notifTest.getUserRecipients().size(), is(1));
  }

  @Test
  public void testBuild_ARUNB_1() {

    // Build
    NotificationMetaData notifTest = UserNotificationHelper.build(new ARUNBTest(null));
    assertBuild_ARUNB_1(notifTest, "", false);
    notifTest = UserNotificationHelper.build(new ARUNBTest(new String("testBuild_ARUNB_1")) {

      @Override
      protected void performBuild(final Object resource) {
        getNotificationMetaData().setSource(resource.toString());
      }
    });
    assertBuild_ARUNB_1(notifTest, "testBuild_ARUNB_1", false);

    notifTest = UserNotificationHelper.build(new ARUNBTest(new ResourceDataTest()));
    assertBuild_ARUNB_1(notifTest, "", true);

    notifTest = UserNotificationHelper.build(new ARUNBTest(new ResourceDataTest()) {

      @Override
      protected void performBuild(final Object resource) {
        super.performBuild(resource);
        stop();
      }
    });
    assertThat(notifTest, nullValue());
  }

  private void assertBuild_ARUNB_1(final NotificationMetaData notifTest, final String aSource,
      final boolean isSilverpeasContent) {
    assertThat(notifTest, notNullValue());
    assertThat(notifTest.getMessageType(), is(NotifMessageType.NORMAL.getId()));
    assertThat(notifTest.getAction(), is(NotifAction.UPDATE));
    assertThat(notifTest.getComponentId(), is("aComponentInstanceId"));
    assertThat(notifTest.getSender(), is("aSenderId"));
    assertThat(notifTest.getContent(), is(TECHNICAL_CONTENT));
    assertThat(notifTest.getDate(), notNullValue());
    assertThat(notifTest.getFileName(), nullValue());
    assertThat(notifTest.getLanguages().size(), is(0));
    assertThat(StringUtil.isDefined(notifTest.getLink()), is(isSilverpeasContent));
    assertThat(notifTest.getSource(), is(aSource));
    assertThat(notifTest.getTitle(), nullValue());
    assertThat(notifTest.getTemplates().size(), is(0));
    assertThat(notifTest.isSendImmediately(), is(false));
    assertThat(notifTest.isAnswerAllowed(), is(false));
    final NotificationResourceData nrdTest = notifTest.getNotificationResourceData();
    assertThat(nrdTest, notNullValue());
    assertThat(nrdTest.getComponentInstanceId(), is("aComponentInstanceId"));
    assertThat(nrdTest.getResourceDescription(), nullValue());
    if (isSilverpeasContent) {
      assertThat(nrdTest.getResourceId(), is("aIdFromResource"));
    } else {
      assertThat(nrdTest.getResourceId(), nullValue());
    }
    assertThat(nrdTest.getResourceLocation(), nullValue());
    assertThat(nrdTest.getResourceName(), nullValue());
    if (isSilverpeasContent) {
      assertThat(nrdTest.getResourceType(), is("aContributionTypeFromResource"));
    } else {
      assertThat(nrdTest.getResourceType(), nullValue());
    }
    assertThat(StringUtil.isDefined(nrdTest.getResourceUrl()), is(isSilverpeasContent));
    assertThat(notifTest.getUserRecipients().size(), is(2));
  }

  @Test
  public void testBuild_ARUNB_2() {

    // Build
    final NotificationMetaData notifTest =
        UserNotificationHelper.build(new ARUNBTest(new ResourceDataTest()) {

          @Override
          protected void performNotificationResource(final Object resource,
              final NotificationResourceData notificationResourceData) {
            final ResourceDataTest resourceTest = (ResourceDataTest) resource;
            notificationResourceData.setResourceDescription("aResourceDescription");
            notificationResourceData.setResourceName(resourceTest.getTitle());
            notificationResourceData.setResourceLocation(resourceTest.getComponentInstanceId());
          }
        });

    // Asserts
    assertThat(notifTest, notNullValue());
    assertThat(notifTest.getMessageType(), is(NotifMessageType.NORMAL.getId()));
    assertThat(notifTest.getAction(), is(NotifAction.UPDATE));
    assertThat(notifTest.getComponentId(), is("aComponentInstanceId"));
    assertThat(notifTest.getSender(), is("aSenderId"));
    assertThat(notifTest.getContent(), is(TECHNICAL_CONTENT));
    assertThat(notifTest.getDate(), notNullValue());
    assertThat(notifTest.getFileName(), nullValue());
    assertThat(notifTest.getLanguages().size(), is(0));
    assertThat(StringUtil.isDefined(notifTest.getLink()), is(true));
    assertThat(notifTest.getSource(), is(""));
    assertThat(notifTest.getTitle(), nullValue());
    assertThat(notifTest.getTemplates().size(), is(0));
    assertThat(notifTest.isSendImmediately(), is(false));
    assertThat(notifTest.isAnswerAllowed(), is(false));
    final NotificationResourceData nrdTest = notifTest.getNotificationResourceData();
    assertThat(nrdTest, notNullValue());
    assertThat(nrdTest.getComponentInstanceId(), is("aComponentInstanceId"));
    assertThat(nrdTest.getResourceDescription(), is("aResourceDescription"));
    assertThat(nrdTest.getResourceId(), is("aIdFromResource"));
    assertThat(nrdTest.getResourceLocation(), is("aComponentInstanceIdFromResource"));
    assertThat(nrdTest.getResourceName(), is("aTitleFromResource"));
    assertThat(nrdTest.getResourceType(), is("aContributionTypeFromResource"));
    assertThat(StringUtil.isDefined(nrdTest.getResourceUrl()), is(true));
    assertThat(notifTest.getUserRecipients().size(), is(2));
  }

  @Test
  public void testBuild_ARUNB_2Bis() {

    // Build
    final NotificationMetaData notifTest =
        UserNotificationHelper.build(new ARUNBTest(new ResourceDataTest(), "aTitle", "aContent"));

    // Asserts
    assertThat(notifTest, notNullValue());
    assertThat(notifTest.getMessageType(), is(NotifMessageType.NORMAL.getId()));
    assertThat(notifTest.getAction(), is(NotifAction.UPDATE));
    assertThat(notifTest.getComponentId(), is("aComponentInstanceId"));
    assertThat(notifTest.getSender(), is("aSenderId"));
    assertThat(notifTest.getContent(), is("aContent" + TECHNICAL_CONTENT));
    assertThat(notifTest.getDate(), notNullValue());
    assertThat(notifTest.getFileName(), nullValue());
    assertThat(notifTest.getLanguages().size(), is(1));
    assertThat(StringUtil.isDefined(notifTest.getLink()), is(true));
    assertThat(notifTest.getSource(), is(""));
    assertThat(notifTest.getTitle(), is("aTitle"));
    assertThat(notifTest.getTemplates().size(), is(0));
    assertThat(notifTest.isSendImmediately(), is(false));
    assertThat(notifTest.isAnswerAllowed(), is(false));
    final NotificationResourceData nrdTest = notifTest.getNotificationResourceData();
    assertThat(nrdTest, notNullValue());
    assertThat(nrdTest.getComponentInstanceId(), is("aComponentInstanceId"));
    assertThat(nrdTest.getResourceDescription(), nullValue());
    assertThat(nrdTest.getResourceId(), is("aIdFromResource"));
    assertThat(nrdTest.getResourceLocation(), nullValue());
    assertThat(nrdTest.getResourceName(), nullValue());
    assertThat(nrdTest.getResourceType(), is("aContributionTypeFromResource"));
    assertThat(StringUtil.isDefined(nrdTest.getResourceUrl()), is(true));
    assertThat(notifTest.getUserRecipients().size(), is(2));
  }

  @Test
  public void testBuild_ATUNB_1() {
    mockOrganizationController_isComponentExist();

    NotificationMetaData notifTest =
        UserNotificationHelper.build(new ATUNBTest(new ResourceDataTest(), "aTitle", "notificationHelperTemplateFile"));
    assertBuild_ATUNB_1(notifTest, "");

    notifTest =
        UserNotificationHelper.build(new ATUNBTest(new ResourceDataTest(), "aTitle", "notificationHelperTemplateFile") {

          @Override
          protected String getMultilangPropertyFile() {
            return "com.silverpeas.notification.helper.multilang.notificationHelperBundle";
          }

        });
    assertBuild_ATUNB_1(notifTest, "bundleValue");
  }

  private void assertBuild_ATUNB_1(final NotificationMetaData notifTest, final String contentValue) {
    assertThat(notifTest, notNullValue());
    assertThat(notifTest.getMessageType(), is(NotifMessageType.ERROR.getId()));
    assertThat(notifTest.getAction(), is(NotifAction.DELETE));
    assertThat(notifTest.getComponentId(), is("aComponentInstanceId"));
    assertThat(notifTest.getSender(), is("aSenderId"));
    assertThat(notifTest.getDate(), notNullValue());
    assertThat(notifTest.getFileName(), is("notificationHelperTemplateFile"));
    assertThat(notifTest.getLanguages().size(), is(3));
    assertThat(StringUtil.isDefined(notifTest.getLink()), is(true));
    assertThat(notifTest.getSource(), is("aSource"));
    assertThat(notifTest.getTitle(), is("aTitle"));
    assertThat(notifTest.getTemplates().size(), is(3));
    assertThat(notifTest.isSendImmediately(), is(false));
    assertThat(notifTest.isAnswerAllowed(), is(false));
    final NotificationResourceData nrdTest = notifTest.getNotificationResourceData();
    assertThat(nrdTest, notNullValue());
    assertThat(nrdTest.getComponentInstanceId(), is("aComponentInstanceId"));
    assertThat(nrdTest.getResourceDescription(), nullValue());
    assertThat(nrdTest.getResourceId(), is("aIdFromResource"));
    assertThat(nrdTest.getResourceLocation(), nullValue());
    assertThat(nrdTest.getResourceName(), is("aTitleFromResource"));
    assertThat(nrdTest.getResourceType(), is("aContributionTypeFromResource"));
    assertThat(StringUtil.isDefined(nrdTest.getResourceUrl()), is(true));
    assertThat(notifTest.getUserRecipients().size(), is(3));

    for (final String curLanguage : DisplayI18NHelper.getLanguages()) {
      assertThat(notifTest.getContent(curLanguage),
          is(curLanguage + "-" + contentValue + " :-)" + TECHNICAL_CONTENT));
    }
  }

  @Test
  public void testBuild_ATUNB_2() {
    mockOrganizationController_isComponentExist();

    // Builds
    final NotificationMetaData notifTest =
        UserNotificationHelper.build(new ATUNBTest(new ResourceDataTest(), "aTitle", "notificationHelperTemplateFile") {

          @Override
          protected String getMultilangPropertyFile() {
            return "com.silverpeas.notification.helper.multilang.notificationHelperBundle";
          }

          @Override
          protected String getComponentInstanceId() {
            return "<componentInstanceId>";
          }
        });

    // Asserts
    assertThat(notifTest, notNullValue());
    assertThat(notifTest.getMessageType(), is(NotifMessageType.ERROR.getId()));
    assertThat(notifTest.getAction(), is(NotifAction.DELETE));
    assertThat(notifTest.getComponentId(), is("<componentInstanceId>"));
    assertThat(notifTest.getSender(), is("aSenderId"));
    assertThat(notifTest.getDate(), notNullValue());
    assertThat(notifTest.getFileName(), is("notificationHelperTemplateFile"));
    assertThat(notifTest.getLanguages().size(), is(3));
    assertThat(StringUtil.isDefined(notifTest.getLink()), is(true));
    assertThat(notifTest.getSource(), is("aSource"));
    assertThat(notifTest.getTitle(), is("aTitle"));
    assertThat(notifTest.getTemplates().size(), is(3));
    assertThat(notifTest.isSendImmediately(), is(false));
    assertThat(notifTest.isAnswerAllowed(), is(false));
    final NotificationResourceData nrdTest = notifTest.getNotificationResourceData();
    assertThat(nrdTest, notNullValue());
    assertThat(nrdTest.getComponentInstanceId(), is("<componentInstanceId>"));
    assertThat(nrdTest.getResourceDescription(), nullValue());
    assertThat(nrdTest.getResourceId(), is("aIdFromResource"));
    assertThat(nrdTest.getResourceLocation(), nullValue());
    assertThat(nrdTest.getResourceName(), is("aTitleFromResource"));
    assertThat(nrdTest.getResourceType(), is("aContributionTypeFromResource"));
    assertThat(StringUtil.isDefined(nrdTest.getResourceUrl()), is(true));
    assertThat(notifTest.getUserRecipients().size(), is(3));

    for (final String curLanguage : DisplayI18NHelper.getLanguages()) {
      assertThat(notifTest.getContent(curLanguage),
          is(curLanguage + "-bundleValue :-) - components" + TECHNICAL_CONTENT));
    }
  }

  protected void mockOrganizationController_isComponentExist() {
    doAnswer(new Answer<Boolean>() {
      @Override
      public Boolean answer(final InvocationOnMock invocation) throws Throwable {
        final String componentInstanceId = (String) invocation.getArguments()[0];
        if ("<componentInstanceId>".equals(componentInstanceId)) {
          return true;
        }
        return false;
      }
    }).when(organizationController).isComponentExist(anyString());
  }

  protected class ATUNBTest extends AbstractTemplateUserNotificationBuilder<ResourceDataTest> {

    public ATUNBTest(final ResourceDataTest resource, final String title, final String fileName) {
      super(resource, title, fileName);
    }

    @Override
    protected String getBundleSubjectKey() {
      return null;
    }

    @Override
    protected Collection<String> getUserIdsToNotify() {
      return Arrays.asList("123", "124", "125");
    }

    @Override
    protected void perform(final ResourceDataTest resource) {
      getNotificationMetaData().setSource("aSource");
    }

    @Override
    protected String getTemplatePath() {
      return "//notification////helper//////";
    }

    @Override
    protected void performTemplateData(final String language, final ResourceDataTest resource,
        final SilverpeasTemplate template) {
      if (getBundle(language) != null) {
        template.setAttribute("testAttribute", getBundle(language).getString("testAttributeKey", ""));
      }
    }

    @Override
    protected void performNotificationResource(final String language,
        final ResourceDataTest resource,
        final NotificationResourceData notificationResourceData) {
      notificationResourceData.setResourceName(resource.getTitle());
    }

    @Override
    protected String getSender() {
      return "aSenderId";
    }

    @Override
    protected String getComponentInstanceId() {
      return "aComponentInstanceId";
    }

    @Override
    protected NotifAction getAction() {
      return NotifAction.DELETE;
    }

    @Override
    protected NotifMessageType getMessageType() {
      return NotifMessageType.ERROR;
    }
  }

  protected class ARUNBTest extends AbstractResourceUserNotificationBuilder<Object> {

    public ARUNBTest(final Object resource, final String title, final String content) {
      super(resource, title, content);
    }

    public ARUNBTest(final Object resource) {
      super(resource);
    }

    @Override
    protected Collection<String> getUserIdsToNotify() {
      return Arrays.asList("123", "124");
    }

    @Override
    protected void performBuild(final Object resource) {
      // Nothing to do
    }

    @Override
    protected void performNotificationResource(final Object resource,
        final NotificationResourceData notificationResourceData) {
      // Nothing to do
    }

    @Override
    protected String getSender() {
      return "aSenderId";
    }

    @Override
    protected String getComponentInstanceId() {
      return "aComponentInstanceId";
    }

    @Override
    protected NotifAction getAction() {
      return NotifAction.UPDATE;
    }
  }

  /**
   * @author Yohann Chastagnier
   */
  protected class AUNBTest extends AbstractUserNotificationBuilder {

    public AUNBTest() {
      super();
    }

    public AUNBTest(final String title, final String content) {
      super(title, content);
    }

    @Override
    protected Collection<String> getUserIdsToNotify() {
      return Collections.singletonList("123");
    }

    @Override
    protected void performBuild() {
      // Nothing to do
    }

    @Override
    protected String getSender() {
      return "aSenderId";
    }

    @Override
    protected String getComponentInstanceId() {
      return "aComponentInstanceId";
    }

    @Override
    protected NotifAction getAction() {
      return NotifAction.CREATE;
    }
  }
}
