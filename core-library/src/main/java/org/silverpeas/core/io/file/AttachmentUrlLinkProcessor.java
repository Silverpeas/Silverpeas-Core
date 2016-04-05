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
package org.silverpeas.core.io.file;

import org.silverpeas.core.util.URLUtil;
import org.apache.commons.io.FileUtils;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.i18n.I18NHelper;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexp;
import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexps;
import static org.silverpeas.core.util.StringDataExtractor.from;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A processor to verify if the given path corresponds to an attachment URL link in order to
 * transform it into the FileSystem attachment path.<br/>
 * This processor must have a high priority.<br/>
 * @author Yohann Chastagnier
 */
public class AttachmentUrlLinkProcessor extends AbstractSilverpeasFileProcessor {

  private static List<Pattern> ATTACHMENT_ID_FROM_LINK_PATTERNS = Arrays
      .asList(Pattern.compile("(?i)/attachmentId/([a-z\\-0-9]+)/"),
          Pattern.compile("(?i)/File/([a-z\\-0-9]+)"));

  private static List<Pattern> ATTACHMENT_LANG_FROM_LINK_PATTERNS = Arrays
      .asList(Pattern.compile("(?i)/lang/([a-z]+)/"),
          Pattern.compile("(?i)ContentLanguage=([a-z]+)"));

  private static Pattern SIZE_DIRECTIVE_FROM_LINK = Pattern.compile("(?i)/size/([0-9 x]+)");

  @Override
  public String processBefore(final String attachmentLink, final ProcessingContext context) {
    if (context == ProcessingContext.GETTING &&
        attachmentLink.contains(URLUtil.getApplicationURL())) {

      // Identify the id of the attachment
      String attachmentId =
          from(attachmentLink).withDirectives(regexps(ATTACHMENT_ID_FROM_LINK_PATTERNS, 1))
              .extractUnique();

      if (!isDefined(attachmentId)) {
        return attachmentLink;
      }

      // Identify the content language requested
      String contentLanguage =
          from(attachmentLink).withDirectives(regexps(ATTACHMENT_LANG_FROM_LINK_PATTERNS, 1))
              .extractUnique();
      contentLanguage = I18NHelper.checkLanguage(contentLanguage);

      // Getting the attachment
      SimpleDocumentPK sdPK = new SimpleDocumentPK(attachmentId);
      SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService().
          searchDocumentById(sdPK, contentLanguage);
      if (attachment == null) {
        return SilverpeasFile.NO_FILE.getPath();
      }

      // Verifying the size directive from url
      String specifiedSize =
          from(attachmentLink).withDirective(regexp(SIZE_DIRECTIVE_FROM_LINK, 1)).extractUnique();

      File attachmentFile = new File(attachment.getAttachmentPath());
      if (isDefined(specifiedSize)) {
        attachmentFile = FileUtils
            .getFile(attachmentFile.getParentFile(), specifiedSize, attachmentFile.getName());
      }
      return attachmentFile.getPath();
    }
    return attachmentLink;
  }

  @Override
  public SilverpeasFile processAfter(final SilverpeasFile file, final ProcessingContext context) {
    return file;
  }

  @Override
  public int getPriority() {
    return 100;
  }
}
