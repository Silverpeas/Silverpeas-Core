/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.wysiwyg.control;

import com.stratelia.silverpeas.peasCore.URLManager;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides method to transform wysiwyg content source into other wysiwyg formats.
 * @author Yohann Chastagnier
 */
public class WysiwygContentTransformer {

  private static List<Pattern> CKEDITOR_LINK_PATTERNS =
      Arrays.asList(Pattern.compile("(?i)=\"([^\"]*/wysiwyg/jsp/ckeditor/[^\"]+)"));

  private static List<Pattern> ATTACHMENT_LINK_PATTERNS = Arrays
      .asList(Pattern.compile("(?i)=\"([^\"]*/attachmentId/[a-z\\-0-9]+/[^\"]+)"),
          Pattern.compile("(?i)=\"([^\"]*/File/[a-z\\-0-9]+[^\"]*)"));

  private static List<Pattern> ATTACHMENT_ID_FROM_LINK_PATTERNS = Arrays
      .asList(Pattern.compile("(?i)/attachmentId/([a-z\\-0-9]+)/"),
          Pattern.compile("(?i)/File/([a-z\\-0-9]+)"));

  private static List<Pattern> GALLERY_CONTENT_LINK_PATTERNS =
      Arrays.asList(Pattern.compile("(?i)=\"([^\"]*/GalleryInWysiwyg/[^\"]+)"));

  private final String wysiwygContent;

  /**
   * An instance of WYSIWYG transformer on the given content.
   * @param wysiwygContent the wysiwyg content.
   * @return an instance of the transformer.
   */
  public static WysiwygContentTransformer on(String wysiwygContent) {
    return new WysiwygContentTransformer(wysiwygContent);
  }

  /**
   * Hidden constructor.
   * @param wysiwygContent the wysiwyg content.
   */
  private WysiwygContentTransformer(final String wysiwygContent) {
    this.wysiwygContent = wysiwygContent;
  }

  /**
   * Transforms all referenced content links in order to be handled in mail sending.
   * A content can be for example an attachment.
   * @return the wysiwyg content transformed to be sent by mail.
   */
  public MailResult toMailContent() throws Exception {
    String transformedWysiwygContent = wysiwygContent;
    List<MimeBodyPart> bodyParts = new ArrayList<MimeBodyPart>();
    int idCount = 0;

    // Handling CKEditor and Gallery links
    List<Pattern> linkPatterns = new ArrayList<Pattern>(CKEDITOR_LINK_PATTERNS);
    linkPatterns.addAll(GALLERY_CONTENT_LINK_PATTERNS);
    for (String link : extractUniqueData(transformedWysiwygContent, linkPatterns)) {

      String cid = "link-content-" + (idCount++);

      // CID links
      transformedWysiwygContent = replaceLinkByCid(transformedWysiwygContent, link, cid);

      // CID references
      MimeBodyPart mbp = new MimeBodyPart();
      String linkForDataSource = link;
      if (!linkForDataSource.toLowerCase()
          .startsWith(URLManager.getCurrentServerURL().toLowerCase())) {
        linkForDataSource = (URLManager.getCurrentServerURL() + link);
      }
      linkForDataSource = linkForDataSource.replace("&amp;", "&");

      URLDataSource uds = new URLDataSource(new URL(linkForDataSource));
      mbp.setDataHandler(new DataHandler(uds));
      mbp.setHeader("Content-ID", "<" + cid + ">");
      bodyParts.add(mbp);
    }

    // Handling attachments
    for (Map.Entry<String, SimpleDocument> attachmentByLink : extractAttachmentsByLinks()
        .entrySet()) {
      String attachmentLink = attachmentByLink.getKey();
      SimpleDocument attachment = attachmentByLink.getValue();

      String cid = "attachment-content-" + (idCount++);

      // CID links
      transformedWysiwygContent = replaceLinkByCid(transformedWysiwygContent, attachmentLink, cid);

      // CID references
      MimeBodyPart mbp = new MimeBodyPart();
      FileDataSource fds = new FileDataSource(attachment.getAttachmentPath());
      mbp.setDataHandler(new DataHandler(fds));
      mbp.setHeader("Content-ID", "<" + cid + ">");
      bodyParts.add(mbp);
    }

    // Returning the result of complete parsing
    return new MailResult(transformedWysiwygContent, bodyParts);
  }

  /**
   * Container of Mail transformation resulting.
   */
  public static class MailResult {
    private final String wysiwygContent;
    private final List<MimeBodyPart> bodyParts;

    public MailResult(final String wysiwygContent, final List<MimeBodyPart> bodyParts) {
      this.wysiwygContent = wysiwygContent;
      this.bodyParts = bodyParts;
    }

    public String getWysiwygContent() {
      return wysiwygContent;
    }

    protected List<MimeBodyPart> getBodyParts() {
      return bodyParts;
    }

    public void applyOn(Multipart multipart) throws Exception {
      for (MimeBodyPart mimeBodyPart : getBodyParts()) {
        multipart.addBodyPart(mimeBodyPart);
      }
    }
  }

  /**
   * Centralization of link replacement.
   * @param wysiwygContent the content in which some parts will be replaced
   * @param link the link to replace by cid.
   * @param cid the cid to put instead of the link.
   * @return
   */
  private static String replaceLinkByCid(String wysiwygContent, String link, String cid) {
    return wysiwygContent.replace("=\"" + link + "\"", "=\"cid:" + cid + "\"");
  }

  /**
   * Extracts from a WYSIWYG content all links and data of referenced attachments.
   * @return referenced {@link SimpleDocument} links and instance, empty if no attachment detected.
   */
  protected Map<String, SimpleDocument> extractAttachmentsByLinks() {
    Map<String, SimpleDocument> attachmentsByLinks = new LinkedHashMap<String, SimpleDocument>();
    for (String attachmentLink : extractAllLinksOfReferencedAttachments()) {
      String attachmentId =
          extractUniqueData(attachmentLink, ATTACHMENT_ID_FROM_LINK_PATTERNS).iterator().next();
      SimpleDocumentPK sdPK = new SimpleDocumentPK(attachmentId);
      attachmentsByLinks.put(attachmentLink, AttachmentServiceFactory.getAttachmentService().
          searchDocumentById(sdPK, null));
    }
    return attachmentsByLinks;
  }

  /**
   * Extracts from a WYSIWYG content all the links of referenced attachments.
   * @return a link list of referenced attachments, empty if no attachment detected.
   */
  private List<String> extractAllLinksOfReferencedAttachments() {
    Set<String> attachmentLinks = extractUniqueData(wysiwygContent, ATTACHMENT_LINK_PATTERNS);
    return new ArrayList<String>(attachmentLinks);
  }

  /**
   * Extracts data from a pattern list definition.
   * @param fromWysiwygContent the content to parse.
   * @param withPatterns the patterns to apply for data extraction.
   * @return the list of data.
   */
  private static Set<String> extractUniqueData(final String fromWysiwygContent,
      List<Pattern> withPatterns) {
    Set<String> data = new LinkedHashSet<String>();
    for (Pattern pattern : withPatterns) {
      data.addAll(extractUniqueData(fromWysiwygContent, pattern));
    }
    return data;
  }

  /**
   * Extracts data from a pattern definition.
   * @param fromWysiwygContent the content to parse.
   * @param withPattern the pattern to apply for data extraction.
   * @return the list of data.
   */
  private static Set<String> extractUniqueData(final String fromWysiwygContent,
      Pattern withPattern) {
    Set<String> data = new LinkedHashSet<String>();
    Matcher matcher = withPattern.matcher(fromWysiwygContent);
    while (matcher.find()) {
      data.add(matcher.group(1));
    }
    return data;
  }
}
