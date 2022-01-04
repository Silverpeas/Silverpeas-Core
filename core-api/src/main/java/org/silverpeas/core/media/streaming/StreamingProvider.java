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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.media.streaming;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data defining a stream provider.
 * @author silveryocha
 */
public class StreamingProvider implements Serializable {
  private static final long serialVersionUID = 3725979193939989226L;

  /**
   * The name of the streaming provider.
   * <p>
   *   It can be considered as the identifier of the provider from point of view of Silverpeas's
   *   server.
   * </p>
   */
  private final String name;
  /**
   * For each streaming provider, some parameters MUST be defined and some CAN be optionally
   * defined. Following parameter MUST be a regexp which permits extracting streaming identifier
   * from a full URL of a streaming. Second regexp matching group is taken into account for the
   * identifier extraction. The first group is just permitting to hit several URL cases.
   */
  private final Pattern urlIdExtractorRegexpPattern;
  /**
   * The <a href="https://oembed.com">oembed</a> URL pattern that permits to build the HTTP
   * request to get standard oembed data from the concerned streaming provider.
   */
  private final String oembedUrlStringPattern;
  /**
   * Optional parameter in order to indicate if the data to inject into pattern defined by
   * attribute {@link #oembedUrlStringPattern} MUST be the identifier of the streaming (by
   * default) or the full URL of a streaming (false).
   */
  private final boolean justIdInOembedUrl;
  /**
   * List of all regexp patterns that permits to identify a provider from a full URL of streaming.
   */
  private final List<Pattern> regexpDetectionParts;

  /**
   * Constructor to initialize a streaming provider instance.
   * @param name see {@link #name} attribute documentation.
   * @param urlIdExtractorRegexpPattern see {@link #urlIdExtractorRegexpPattern} attribute documentation.
   * @param oembedUrlStringPattern see {@link #oembedUrlStringPattern} attribute documentation.
   * @param justIdInOembedUrl see {@link #justIdInOembedUrl} attribute documentation.
   * @param regexpDetectionParts see {@link #regexpDetectionParts} attribute documentation.
   */
  public StreamingProvider(final String name, final Pattern urlIdExtractorRegexpPattern,
      final String oembedUrlStringPattern, final boolean justIdInOembedUrl,
      final List<Pattern> regexpDetectionParts) {
    this.name = name;
    this.urlIdExtractorRegexpPattern = urlIdExtractorRegexpPattern;
    this.oembedUrlStringPattern = oembedUrlStringPattern;
    this.justIdInOembedUrl = justIdInOembedUrl;
    this.regexpDetectionParts = regexpDetectionParts;
  }

  /**
   * @see #name
   * @return a string.
   */
  public String getName() {
    return name;
  }

  /**
   * @see #oembedUrlStringPattern
   * @return a regexp pattern as string.
   */
  String getOembedUrlStringPattern() {
    return oembedUrlStringPattern;
  }

  /**
   * Gets all regexp patterns that permits about a full URL of a streaming to indicate if it is
   * one handled by the streaming provider.
   * @return list of regexp pattern containing at least one element.
   */
  public List<Pattern> getRegexpDetectionParts() {
    return regexpDetectionParts;
  }

  /**
   * Gets the identifier of the streaming from its full url by using a REGEXP pattern defined by
   * {@link #urlIdExtractorRegexpPattern}.
   * @param url a full streaming URL
   * @return an optional streaming identifier which permits handling the not found case.
   */
  Optional<String> extractStreamingId(String url) {
    if (!justIdInOembedUrl) {
      return Optional.of(url);
    }
    final int matchedGroup = 2;
    Matcher matcher = urlIdExtractorRegexpPattern.matcher(url);
    if (matcher.find()) {
      return Optional.of(matcher.group(matchedGroup));
    }
    return Optional.empty();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final StreamingProvider that = (StreamingProvider) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
