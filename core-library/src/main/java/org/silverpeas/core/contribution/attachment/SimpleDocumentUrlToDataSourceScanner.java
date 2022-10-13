/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.contribution.attachment;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.content.LinkUrlDataSource;
import org.silverpeas.core.contribution.content.LinkUrlDataSourceScanner;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileProvider;

import javax.activation.FileDataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexps;
import static org.silverpeas.core.util.StringDataExtractor.from;

/**
 * @author silveryocha
 */
@Service
public class SimpleDocumentUrlToDataSourceScanner implements LinkUrlDataSourceScanner {

  private static final List<Pattern> ATTACHMENT_LINK_PATTERNS = Arrays
      .asList(Pattern.compile("(?i)=\"([^\"]*/attachmentId/[a-z\\-0-9]+/[^\"]+)"),
          Pattern.compile("(?i)=\"([^\"]*/File/[a-z\\-0-9]+[^\"]*)"));

  @Override
  public List<LinkUrlDataSource> scanHtml(final String htmlContent) {
    final List<LinkUrlDataSource> result = new ArrayList<>();
    from(htmlContent).withDirectives(regexps(ATTACHMENT_LINK_PATTERNS, 1)).extract().forEach(l -> {
      final SilverpeasFile attachmentFile = SilverpeasFileProvider.getFile(l);
      if (attachmentFile.exists()) {
        result.add(new LinkUrlDataSource(l, () -> new FileDataSource(attachmentFile)));
      }
    });
    return result;
  }
}
