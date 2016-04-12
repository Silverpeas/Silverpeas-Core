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
package org.silverpeas.core.comment.service.notification;

import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.service.CommentUserNotification;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import java.util.Map;
import java.util.Set;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;

/**
 * This class is an utilitary one that gather all matchers on NotificationMetaData objects and that
 * is dedicated to be use in JUnit assertion.
 */
public final class NotificationMatchers {

  /**
   * A matcher on the presence of a comment in a notification message for all supported languages in
   * Silverpeas.
   *
   * @param notification the notification message.
   * @return the matcher with the specified notification message.
   */
  public static Matcher<Comment> isSetIn(final NotificationMetaData notification) {
    return new CommentSettingMatcher(notification);
  }

  /**
   * A matcher to assert a comment is correctly set within a notification data.
   */
  private static class CommentSettingMatcher extends CustomMatcher<Comment> {

    private NotificationMetaData notif;

    public CommentSettingMatcher(NotificationMetaData notificationMetaData) {
      super("The comment should be set in the notification message for all supported languages");
      this.notif = notificationMetaData;
    }

    @Override
    public boolean matches(Object item) {
      Comment expectedComment = (Comment) item;
      Map<String, SilverpeasTemplate> templates = notif.getTemplates();
      Set<String> languages = I18NHelper.getAllSupportedLanguages();
      for (String language : languages) {
        SilverpeasTemplate template = templates.get(language);
        if (template == null) {
          return false;
        }
        Comment theComment = (Comment) template.getAttributes().get(
            CommentUserNotification.NOTIFICATION_COMMENT_ATTRIBUTE);
        if (theComment == null || !theComment.equals(expectedComment)) {
          return false;
        }
      }
      return true;
    }
  }
}
