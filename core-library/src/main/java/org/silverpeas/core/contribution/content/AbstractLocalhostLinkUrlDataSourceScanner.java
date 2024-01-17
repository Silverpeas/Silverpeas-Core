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

package org.silverpeas.core.contribution.content;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.StringDataExtractor.ExtractorDirective;
import org.silverpeas.core.util.URLUtil;

import javax.activation.URLDataSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.util.StringDataExtractor.from;

/**
 * @author silveryocha
 */
public abstract class AbstractLocalhostLinkUrlDataSourceScanner
    implements LinkUrlDataSourceScanner {

  protected abstract List<ExtractorDirective> directives();

  @Override
  public List<LinkUrlDataSource> scanHtml(final String htmlContent) {
    final List<LinkUrlDataSource> result = new ArrayList<>();
    from(htmlContent).withDirectives(directives())
        .extract()
        .forEach(l -> {
          String linkForDataSource = l;
          if (linkForDataSource.trim().toLowerCase().startsWith("/")) {
            linkForDataSource = URLUtil.getCurrentServerURL() + l;
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
