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

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Source;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.body;
import org.apache.ecs.xhtml.head;
import org.apache.ecs.xhtml.html;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yohann Chastagnier
 */
public class MailContent {
  public static final MailContent EMPTY = new MailContent();

  private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=\"UTF-8\"";
  private static final String TEXT_CONTENT_TYPE = "text/plain; charset=\"UTF-8\"";
  private static final String ALTERNATIVE_SUBTYPE = "alternative";
  private static final String RELATED_SUBTYPE = "related";
  /**
   * An image directly inlined into the HTML content as a base64 encoded data URI. The user
   * notifications use such URIs to carry the thumbnail of a contribution, as their content is
   * built once, whatever the channel by which they will be then distributed.
   */
  private static final Pattern INLINED_IMAGE =
      Pattern.compile("(?i)src=([\"'])(data:(image/[a-z0-9.+-]+);base64,([a-z0-9+/=\\s]+))\\1");
  private static final String META_CHARSET = "<meta charset=\"utf-8\">";
  private static final String META_VIEWPORT = "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\">";
  private static final String META_HTTP_EQUIV = "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">";
  private static final String START_STYLE_PATTERN = "<style";
  private static final String END_STYLE_PATTERN = "</style>";

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
   * <p>
   *   If HTML TAG container does not exist, then the normalization is performed.
   *   HTML, HEAD and BODY are created and all declared styles in BODY are moved to HEAD part in
   *   order to get as most as possible compatibility email reader.
   * </p>
   * @param htmlContent an HTML content.
   * @return a string representing the normalized HTML content.
   */
  public static String normalizeHtmlContent(final String htmlContent) {
    if (!htmlContent.toLowerCase().contains("<html>")) {
      final String DOCTYPE = "<!DOCTYPE html>";
      final html html = new html(StringUtil.EMPTY);
      html.setLang(DisplayI18NHelper.getDefaultLanguage());
      final head head = new head();
      head.addElement(META_CHARSET);
      head.addElement(META_VIEWPORT);
      head.addElement(META_HTTP_EQUIV);
      final body body = new body();
      String finalHtmlContent = htmlContent;
      int styleStartIndex = htmlContent.indexOf(START_STYLE_PATTERN);
      int styleEndIndex = htmlContent.indexOf(END_STYLE_PATTERN) + END_STYLE_PATTERN.length();
      while (styleStartIndex >= 0 && styleStartIndex < styleEndIndex) {
        String before = finalHtmlContent.substring(0, styleStartIndex);
        String style = finalHtmlContent.substring(styleStartIndex, styleEndIndex);
        String end = finalHtmlContent.substring(styleEndIndex);
        head.addElement(style);
        finalHtmlContent = before + end;
        styleStartIndex = finalHtmlContent.indexOf(START_STYLE_PATTERN);
        styleEndIndex = finalHtmlContent.indexOf(END_STYLE_PATTERN) + END_STYLE_PATTERN.length();
      }
      body.addElement(finalHtmlContent);
      final ElementContainer elements = new ElementContainer();
      elements.addElement(DOCTYPE);
      html.addElement(head);
      html.addElement(body);
      elements.addElement(html);
      return elements.toString();
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
   * Indicates if the content is a html one.
   * @return true if content is an HTML one, false otherwise.
   */
  boolean isHtml() {
    return isHtml;
  }

  /**
   * Gets the content value.
   * @return the content value as string or as {@link jakarta.mail.Multipart}.
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
    if (getValue() instanceof String contentAsString) {
      if (!isHtml() && !contentAsString.toLowerCase().contains("<html>")) {
        // Content as simple text if no <html> TAG is detected.
        message.setText(contentAsString, Charsets.UTF_8.name());
      } else if (getContentType().toLowerCase().contains("html")) {
        // the images inlined as data URIs are turned into referenced body parts as most of the
        // mail readers refuse to render a data URI
        final InlinedImages images =
            InlinedImages.extractFrom(normalizeHtmlContent(contentAsString));
        final String htmlContent = images.getHtmlContent();
        final Multipart alternative = new MimeMultipart(ALTERNATIVE_SUBTYPE);
        alternative.addBodyPart(extractTextBodyPartFromHtmlContent(htmlContent));
        final MimeBodyPart htmlPart = initMimeBodyPartFromContent(htmlContent, getContentType());
        // last body part is the preferred alternative
        alternative.addBodyPart(htmlPart);
        final Multipart multipart = images.isEmpty() ? alternative : images.relate(alternative);
        content = multipart;
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
   * The images that were inlined into an HTML content as base64 encoded data URIs and that have
   * been extracted to be carried by their own body part, each of them being then referred by the
   * HTML content through a {@code cid:} URI.
   * <p>
   * Data URIs are rejected by most of the mail readers (Gmail and Outlook among others), whereas
   * the referenced body parts of a {@code multipart/related} content are the standard and widely
   * supported way to inline an image into a mail.
   * </p>
   */
  private static class InlinedImages {

    private final String htmlContent;
    private final List<MimeBodyPart> bodyParts;

    private InlinedImages(final String htmlContent, final List<MimeBodyPart> bodyParts) {
      this.htmlContent = htmlContent;
      this.bodyParts = bodyParts;
    }

    /**
     * Extracts from the given HTML content all the images inlined as base64 encoded data URIs.
     * An image whose content can not be decoded is left as such into the HTML content.
     * @param htmlContent an HTML content.
     * @return the images that have been extracted, with the HTML content in which each of them
     * is now referred by a {@code cid:} URI.
     */
    static InlinedImages extractFrom(final String htmlContent) {
      final List<MimeBodyPart> parts = new ArrayList<>();
      final Map<String, String> cids = new HashMap<>();
      final Matcher matcher = INLINED_IMAGE.matcher(htmlContent);
      final StringBuilder transformed = new StringBuilder();
      while (matcher.find()) {
        final String dataUri = matcher.group(2);
        final String cid =
            cids.computeIfAbsent(dataUri, u -> newBodyPart(matcher.group(3), matcher.group(4),
                parts));
        final String quote = matcher.group(1);
        final String replacement =
            cid == null ? matcher.group() : "src=" + quote + "cid:" + cid + quote;
        matcher.appendReplacement(transformed, Matcher.quoteReplacement(replacement));
      }
      matcher.appendTail(transformed);
      return new InlinedImages(transformed.toString(), parts);
    }

    /**
     * Creates the body part carrying the given base64 encoded image and appends it to the given
     * parts.
     * @param mimeType the MIME type of the image.
     * @param base64Content the content of the image, base64 encoded.
     * @param parts the body parts to complete.
     * @return the identifier by which the created body part has to be referred, or null if the
     * image can not be decoded.
     */
    private static String newBodyPart(final String mimeType, final String base64Content,
        final List<MimeBodyPart> parts) {
      try {
        final byte[] image = Base64.getMimeDecoder().decode(base64Content);
        final String cid = "inlined-image-" + parts.size();
        final MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(image, mimeType)));
        bodyPart.setHeader("Content-ID", "<" + cid + ">");
        bodyPart.setDisposition(Part.INLINE);
        parts.add(bodyPart);
        return cid;
      } catch (IllegalArgumentException | MessagingException e) {
        SilverLogger.getLogger(MailContent.class)
            .warn("Cannot inline an image of type {0} into a mail: {1}", mimeType, e.getMessage());
        return null;
      }
    }

    boolean isEmpty() {
      return bodyParts.isEmpty();
    }

    String getHtmlContent() {
      return htmlContent;
    }

    /**
     * Relates the images to the given content by wrapping both of them into a
     * {@code multipart/related} content, the given content coming first as being the root one.
     * @param mainContent the content referring the images.
     * @return a {@code multipart/related} content.
     * @throws MessagingException if the content can not be built.
     */
    Multipart relate(final Multipart mainContent) throws MessagingException {
      final Multipart related = new MimeMultipart(RELATED_SUBTYPE);
      final MimeBodyPart mainPart = new MimeBodyPart();
      mainPart.setContent(mainContent);
      related.addBodyPart(mainPart);
      for (final MimeBodyPart bodyPart : bodyParts) {
        related.addBodyPart(bodyPart);
      }
      return related;
    }
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
