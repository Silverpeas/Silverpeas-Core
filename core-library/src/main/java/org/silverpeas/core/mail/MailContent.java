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
package org.silverpeas.core.mail;

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Source;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * @author Yohann Chastagnier
 */
public class MailContent {
  public static final MailContent EMPTY = new MailContent();

  private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=\"UTF-8\"";
  private static final String TEXT_CONTENT_TYPE = "text/plain; charset=\"UTF-8\"";
  private static final String ALTERNATIVE_SUBTYPE = "alternative";

  private Object content = "";
  private String contentType = DEFAULT_CONTENT_TYPE;
  private boolean isHtml = true;

  /**
   * Hidden constructor.
   */
  private MailContent() {
  }

  private static MimeBodyPart initMimeBodyPartFromContent(final String content,
      final String contentType) {
    final MimeBodyPart mimeBodyPart = new MimeBodyPart();
    try {
      mimeBodyPart.setDataHandler(
          new DataHandler(new ByteArrayDataSource(content.getBytes(Charsets.UTF_8), contentType)));
    } catch (MessagingException e) {
      throw new SilverpeasRuntimeException(e);
    }
    return mimeBodyPart;
  }

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
   * Normalizes the given HTML content in order to be sent safely by mail infrastructure.
   * @param htmlContent an HTML content.
   * @return a string representing the normalized HTML content.
   */
  public static String normalizeHtmlContent(final String htmlContent) {
    if (!htmlContent.toLowerCase().contains("<html>")) {
      return "<html><body>" + htmlContent + "</body></html>";
    }
    return htmlContent;
  }

  /**
   * Gets a {@link MimeBodyPart} filled with text content extracted from given HTML content.
   * @param htmlContent an HTML content.
   * @return a {@link MimeBodyPart}.
   */
  public static MimeBodyPart extractTextBodyPartFromHtmlContent(final String htmlContent) {
    final String textContent = new Renderer(new Source(htmlContent))
        .setConvertNonBreakingSpaces(true)
        .setIncludeHyperlinkURLs(true)
        .setDecorateFontStyles(true)
        .setIncludeFirstElementTopMargin(true)
        .toString();
    return initMimeBodyPartFromContent(textContent, TEXT_CONTENT_TYPE);
  }

  /**
   * Gets a {@link MimeBodyPart} initialized with given HTML content into UTF8 encoding.
   * @param htmlContent an HTML content as UTF8 encoding.
   * @return a {@link MimeBodyPart}.
   */
  public static MimeBodyPart getHtmlBodyPartFromHtmlContent(final String htmlContent) {
    return initMimeBodyPartFromContent(htmlContent, DEFAULT_CONTENT_TYPE);
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
   * @throws MessagingException if an error occurs with the message
   */
  public void applyOn(MimeMessage message) throws MessagingException {
    if (getValue() instanceof String) {
      final String contentAsString = (String) getValue();
      if (!isHtml() && !contentAsString.toLowerCase().contains("<html>")) {
        // Content as simple text if no <html> TAG is detected.
        message.setText(contentAsString, Charsets.UTF_8.name());
      } else if (getContentType().toLowerCase().contains("html")) {
        final String htmlContent = normalizeHtmlContent(contentAsString);
        final Multipart multipart = new MimeMultipart(ALTERNATIVE_SUBTYPE);
        content = multipart;
        multipart.addBodyPart(extractTextBodyPartFromHtmlContent(htmlContent));
        final MimeBodyPart htmlPart = initMimeBodyPartFromContent(htmlContent, getContentType());
        multipart.addBodyPart(htmlPart);
        // last body part is the preferred alternative
        message.setContent(multipart);
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

  /**
   * Representation of an attached file.
   */
  public interface AttachedFile {
    /**
     * The name of the attached file.
     * @return a string.
     */
    String getName();

    /**
     * The full path to the file content.
     * @return a string.
     */
    String getPath();

    /**
     * Gets the corresponding body part.
     * @return a {@link MimeBodyPart} instance.
     */
    default MimeBodyPart toBodyPart() throws MessagingException {
      // create the second message part
      final MimeBodyPart mbp = new MimeBodyPart();
      // attach the file to the message
      final FileDataSource fds = new FileDataSource(getPath());
      mbp.setDataHandler(new DataHandler(fds));
      // For Displaying images in the mail
      mbp.setFileName(getName());
      mbp.setHeader("Content-ID", "<" + getName() + ">");
      // create the Multipart and its parts to it
      return mbp;
    }
  }
}
