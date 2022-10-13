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

package org.silverpeas.core.contribution.content;

import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective;

import javax.activation.DataSource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An implementation of this interface provides a list of {@link RegexpPatternDirective} to extract
 * link urls from an html content represented as a {@link String} and it provides also the
 * {@link DataSource} initialization according to an extracted link.
 * @author silveryocha
 */
public interface LinkUrlDataSourceScanner {

  static List<LinkUrlDataSourceScanner> getAll() {
    return ServiceProvider.getAllServices(LinkUrlDataSourceScanner.class)
        .stream()
        .sorted(Comparator.comparing(o -> o.getClass().getSimpleName()))
        .collect(Collectors.toUnmodifiableList());
  }

  static Map<String, String> extractUrlParameters(final String url) {
    return Optional.ofNullable(url)
        .stream()
        .flatMap(u -> Stream.of(u.substring(u.indexOf('?') + 1).replace("&amp;", "&").split("[&]")))
        .map(p -> p.split("[=]"))
        .collect(Collectors.toMap(p -> p[0], p -> p[1]));
  }

  /**
   * Scans the given html content to extract the link url and provide related {@link DataSource}.
   * @param htmlContent the HTML content to scan.
   * @return list of {@link LinkUrlDataSource}.
   */
  List<LinkUrlDataSource> scanHtml(final String htmlContent);
}
