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
package org.silverpeas.core.mail;

import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

/**
 * @author Yohann Chastagnier
 */
public class MailContent {
  public static final MailContent EMPTY = new MailContent();

  private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=\"UTF-8\"";

  private Object content = "";
  private String contentType = DEFAULT_CONTENT_TYPE;
  private boolean isHtml = true;

  /**
   * Gets a new instance of {@link MimeMessage} by specifying a content as a string.
   * @param content the string content
   * @return a new instance of {@link MimeMessage}.
   */
  public static MailContent of(String content) {
    MailContent mailContent = new MailContent();
    if (StringUtil.isDefined(content)) {
      mailContent.content = content;
    }
    return mailContent;
  }

  /**
   * Gets a new instance of {@link MimeMessage} by specifying a content as a {@link Multipart}.
   * @param multipart the {@link Multipart} content
   * @return a new instance of {@link MimeMessage}.
   */
  public static MailContent of(Multipart multipart) {
    MailContent mailContent = new MailContent();
    if (multipart != null) {
      mailContent.content = multipart;
    }
    return mailContent;
  }

  /**
   * Hidden constructor.
   */
  private MailContent() {
  }

  /**
   * Forces the information that the content is not an HTML one.
   * By default, the content is considered as an HTML one.
   * @return the instance of {@link MailContent}.
   */
  public MailContent notHtml() {
    this.isHtml = false;
    return this;
  }

  /**
   * Indicates if the content is an html one.
   * @return true if content is an HTML one, false otherwise.
   */
  boolean isHtml() {
    return isHtml;
  }

  /**
   * Gets the content value.
   * @return the content value as string or as {@link javax.mail.Multipart}.
   */
  Object getValue() {
    return content;
  }

  /**
   * Get the content type.
   * @return the content type.
   */
  String getContentType() {
    return contentType;
  }

  /**
   * Sets the content type.
   * @param contentType the content type to set.
   * @return the instance of {@link MailContent}.
   */
  public MailContent withContentType(final String contentType) {
    this.contentType = StringUtil.defaultStringIfNotDefined(contentType, DEFAULT_CONTENT_TYPE);
    return this;
  }

  /**
   * Applies the content information on a {@link MimeMessage}.
   * If the content is a string and contains {@code <html>} TAG, then the content is considered as
   * an HTML one, even if {@link #isHtml()} returns false.
   * @param message the {@link MimeMessage}.
   */
  public void applyOn(MimeMessage message) throws MessagingException {
    if (getValue() instanceof String) {
      String contentAsString = (String) getValue();
      if (!contentAsString.toLowerCase().contains("<html>") && !isHtml()) {
        // Content as simple text if no <html> TAG is detected.
        message.setText(contentAsString, Charsets.UTF_8.name());
      } else {
        message.setContent(contentAsString, getContentType());
      }
    } else {
      message.setContent((Multipart) getValue());
    }
  }

  @Override
  public String toString() {
    return getValue().toString();
  }
}
