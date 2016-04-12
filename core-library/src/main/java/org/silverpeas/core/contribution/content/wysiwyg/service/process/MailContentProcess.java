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
package org.silverpeas.core.contribution.content.wysiwyg.service.process;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformerProcess;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileProvider;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexps;
import static org.silverpeas.core.util.StringDataExtractor.from;

/**
 * Transforms all referenced content links in order to be handled in mail sending. A content can be
 * for example an attachment.
 * @author Yohann Chastagnier
 */
public class MailContentProcess
    implements WysiwygContentTransformerProcess<MailContentProcess.MailResult> {

  private static List<Pattern> CKEDITOR_LINK_PATTERNS =
      Arrays.asList(Pattern.compile("(?i)=\"([^\"]*/wysiwyg/jsp/ckeditor/[^\"]+)"));

  private static List<Pattern> ATTACHMENT_LINK_PATTERNS = Arrays
      .asList(Pattern.compile("(?i)=\"([^\"]*/attachmentId/[a-z\\-0-9]+/[^\"]+)"),
          Pattern.compile("(?i)=\"([^\"]*/File/[a-z\\-0-9]+[^\"]*)"));

  private static List<Pattern> GALLERY_CONTENT_LINK_PATTERNS =
      Arrays.asList(Pattern.compile("(?i)=\"([^\"]*/GalleryInWysiwyg/[^\"]+)"));

  @Override
  public MailResult execute(final String wysiwygContent) throws Exception {
    String transformedWysiwygContent = wysiwygContent;
    List<MimeBodyPart> bodyParts = new ArrayList<>();
    int idCount = 0;

    // Handling CKEditor and Gallery links
    List<Pattern> linkPatterns = new ArrayList<>(CKEDITOR_LINK_PATTERNS);
    linkPatterns.addAll(GALLERY_CONTENT_LINK_PATTERNS);
    for (String link : from(transformedWysiwygContent).withDirectives(regexps(linkPatterns, 1))
        .extract()) {

      String cid = "link-content-" + (idCount++);

      // CID links
      transformedWysiwygContent = replaceLinkByCid(transformedWysiwygContent, link, cid);

      // CID references
      MimeBodyPart mbp = new MimeBodyPart();
      String linkForDataSource = link;
      if (!linkForDataSource.toLowerCase()
          .startsWith(URLUtil.getCurrentServerURL().toLowerCase())) {
        linkForDataSource = (URLUtil.getCurrentServerURL() + link);
      }
      linkForDataSource = linkForDataSource.replace("&amp;", "&");

      URLDataSource uds = new URLDataSource(new URL(linkForDataSource));
      mbp.setDataHandler(new DataHandler(uds));
      mbp.setHeader("Content-ID", "<" + cid + ">");
      bodyParts.add(mbp);
    }

    // Handling attachments
    Map<String, String> attachmentCidCache = new HashMap<>();
    for (String attachmentUrlLink : extractAllLinksOfReferencedAttachments(
        transformedWysiwygContent)) {

      SilverpeasFile attachmentFile = SilverpeasFileProvider.getFile(attachmentUrlLink);
      if (attachmentFile.exists() && attachmentFile.isImage()) {
        String cid = attachmentCidCache.get(attachmentUrlLink);
        if (cid == null) {
          cid = "attachment-content-" + (idCount++);
          attachmentCidCache.put(attachmentUrlLink, cid);
        }

        // CID links
        transformedWysiwygContent =
            replaceLinkByCid(transformedWysiwygContent, attachmentUrlLink, cid);

        // CID references
        MimeBodyPart mbp = new MimeBodyPart();
        FileDataSource fds = new FileDataSource(attachmentFile);
        mbp.setDataHandler(new DataHandler(fds));
        mbp.setHeader("Content-ID", "<" + cid + ">");
        bodyParts.add(mbp);
      }
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
  private String replaceLinkByCid(String wysiwygContent, String link, String cid) {
    return wysiwygContent.replace("=\"" + link + "\"", "=\"cid:" + cid + "\"");
  }

  /**
   * Extracts from a WYSIWYG content all the links of referenced attachments.
   * @param wysiwygContent a WYSIWYG content.
   * @return a link list of referenced attachments, empty if no attachment detected.
   */
  List<String> extractAllLinksOfReferencedAttachments(final String wysiwygContent) {
    return from(wysiwygContent).withDirectives(regexps(ATTACHMENT_LINK_PATTERNS, 1)).extract();
  }
}
