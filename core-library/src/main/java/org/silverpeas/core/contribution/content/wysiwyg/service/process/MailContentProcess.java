/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.content.wysiwyg.service.process;

import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.contribution.content.LinkUrlDataSource;
import org.silverpeas.core.contribution.content.LinkUrlDataSourceScanner;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformerProcess;
import org.silverpeas.core.mail.MailContent;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexp;
import static org.silverpeas.core.util.StringDataExtractor.from;
import static org.silverpeas.core.util.file.FileUtil.isImage;

/**
 * Transforms all referenced content links in order to be handled in mail sending. A content can be
 * for example an attachment.
 * @author Yohann Chastagnier
 */
public class MailContentProcess
    implements WysiwygContentTransformerProcess<MailContentProcess.MailResult> {

  @Override
  public MailResult execute(final String wysiwygContent) throws SilverpeasException {
    try {
      final List<MimeBodyPart> bodyParts = new ArrayList<>();
      final Map<String, Optional<String>> mailAttachmentCidCache = new HashMap<>();
      final Mutable<String> transformedWysiwygContent = Mutable.of(wysiwygContent)
          .filter(StringUtil::isDefined)
          .map(MailContent::normalizeHtmlContent);
      final IdentifierGenerator idCount = new IdentifierGenerator();
      LinkUrlDataSourceScanner.getAll().forEach(u ->
        u.scanHtml(transformedWysiwygContent.get())
          .forEach(d -> mailAttachmentCidCache.computeIfAbsent(d.getLinkUrl(), l -> {
            final Mutable<String> cid = generateCid(idCount, d);
            cid.ifPresent(c -> {
              try {
                // CID references
                final MimeBodyPart mbp = new MimeBodyPart();
                mbp.setDataHandler(new DataHandler(d.getDataSource()));
                mbp.setHeader("Content-ID", "<" + c + ">");
                bodyParts.add(mbp);
                // CID links
                final String previousContent = transformedWysiwygContent.get();
                transformedWysiwygContent.set(replaceLinkByCid(previousContent, l, c));
              } catch (MessagingException me) {
                throw new SilverpeasRuntimeException(me);
              }
            });
            return Optional.ofNullable(cid.orElse(null));
          })));
      return new MailResult(transformedWysiwygContent.get(), bodyParts);
    } catch (Exception e) {
      throw new SilverpeasException(e);
    }
  }

  private Mutable<String> generateCid(final IdentifierGenerator idCount, final LinkUrlDataSource d) {
    final Mutable<String> cid = Mutable.empty();
    if (d.getDataSource() instanceof FileDataSource) {
      final FileDataSource fileDataSource = (FileDataSource) d.getDataSource();
      if (fileDataSource.getFile().exists() && isImage(fileDataSource.getFile().getPath())) {
        cid.set(idCount.newOne());
      }
    } else {
      cid.set(idCount.newOne());
    }
    return cid;
  }

  @Singleton
  public static class WysiwygCkeditorMediaLinkUrlToDataSourceScanner
      implements LinkUrlDataSourceScanner {

    private static final Pattern CKEDITOR_LINK_PATTERNS =
        Pattern.compile("(?i)=\"([^\"]*/wysiwyg/jsp/ckeditor/[^\"]+)");

    @Override
    public List<LinkUrlDataSource> scanHtml(final String htmlContent) {
      final List<LinkUrlDataSource> result = new ArrayList<>();
      from(htmlContent).withDirectives(singletonList(regexp(CKEDITOR_LINK_PATTERNS, 1))).extract().forEach(l -> {
        String linkForDataSource = l;
        if (linkForDataSource.trim().toLowerCase().startsWith("/")) {
          linkForDataSource = URLUtil.getCurrentLocalServerURL() + l;
        }
        linkForDataSource = linkForDataSource.replace("&amp;", "&");
        try {
          final URL url = new URL(linkForDataSource);
          result.add(new LinkUrlDataSource(l, () -> new URLDataSource(url)));
        } catch (MalformedURLException e) {
          throw new SilverpeasRuntimeException(e);
        }
      });
      return result;
    }
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

    public void applyOn(Multipart multipart) throws SilverpeasException {
      try {
        for (MimeBodyPart mimeBodyPart : getBodyParts()) {
          multipart.addBodyPart(mimeBodyPart);
        }
      } catch (MessagingException e) {
        throw new SilverpeasException(e);
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
   * Generates identifiers for mail content attachments.
   */
  private static class IdentifierGenerator {
    private int count = 0;

    /**
     * Generates a new identifier.
     * @return a string.
     */
    String newOne() {
      return "mail-content-attachment-" + (count++);
    }
  }
}
