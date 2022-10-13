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

package org.silverpeas.core.media.streaming;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.silverpeas.core.util.StringUtil.EMPTY;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * Register of all streaming providers handled into Silverpeas.
 * <p>
 *   This permits also to control which streaming providers are allowed into Silverpeas.
 * </p>
 * @author silveryocha
 */
@Singleton
public class StreamingProvidersRegistry {

  private final Map<String, StreamingProvider> registry = new HashMap<>();

  public static StreamingProvidersRegistry get() {
    return ServiceProvider.getService(StreamingProvidersRegistry.class);
  }

  /**
   * The registry is filled by default from 'org.silverpeas.media.streaming.properties' file definitions.
   * <p>
   *   In order to handle other streaming providers, the property file MUST be completed.
   *   Or it is also possible to an additional library to add programmatically an other streaming provider.
   * </p>
   */
  @PostConstruct
  protected void setupDefaults() {
    registry.clear();
    final SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.media.streaming");
    of(settings.getList("streaming.provider.handledIds", new String[0]))
        .map(i -> {
          final String urlIdExtractorRegexpPattern = settings.getString(
              format("streaming.provider.%s.urlIdExtractorRegexpPattern", i), EMPTY);
          final String oembedUrlStringPattern = settings.getString(
              format("streaming.provider.%s.oembedUrlStringPattern", i), EMPTY);
          final Stream<String> additionalRegexpDetectionParts = of(
              settings.getList(format("streaming.provider.%s.additionalRegexpDetectionParts", i),
                  new String[0]));
          if (isDefined(urlIdExtractorRegexpPattern) && isDefined(oembedUrlStringPattern)) {
            final List<Pattern> regexpDetectionParts = concat(of(i), additionalRegexpDetectionParts)
                .map(Pattern::compile)
                .collect(toList());
            final boolean justIdInOembedUrl = settings.getBoolean(
                format("streaming.provider.%s.justIdInOembedUrl", i), true);
            return new StreamingProvider(i, Pattern.compile(urlIdExtractorRegexpPattern),
                oembedUrlStringPattern, justIdInOembedUrl, regexpDetectionParts);
          }
          return null;
        })
        .filter(Objects::nonNull)
        .forEach(this::add);
  }

  public Set<StreamingProvider> getAll() {
    return registry.values().stream().collect(toUnmodifiableSet());
  }

  /**
   * Gets by name (which is considered as the identifier) the registered
   * {@link StreamingProvider} instance if any.
   * @param name a Silverpeas's identifier of streaming provider which is represented the name of
   * the streaming provider.
   * @return the aimed {@link StreamingProvider} is any.
   */
  public Optional<StreamingProvider> getByName(final String name) {
    return ofNullable(name).map(String::toLowerCase).map(registry::get);

  }

  /**
   * Adds a {@link StreamingProvider} instance into registry.
   * @param streaming the streaming provider definition.
   */
  public void add(final StreamingProvider streaming) {
    registry.put(streaming.getName().toLowerCase(), streaming);
  }

  /**
   * Gets the registered {@link StreamingProvider} instance matching with the given full URL of a
   * streaming.
   * @param streamingUrl the full URL of a streaming.
   * @return a {@link StreamingProvider} instance if any guessed from the given URL.
   */
  public Optional<StreamingProvider> getFromUrl(String streamingUrl) {
    return ofNullable(streamingUrl)
        .filter(StringUtil::isDefined)
        .map(String::toLowerCase)
        .flatMap(u -> registry.values().stream()
            .filter(s -> s.getRegexpDetectionParts()
                .stream()
                .map(p -> p.matcher(u))
                .anyMatch(Matcher::find))
            .findFirst());
  }

  /**
   * Gets the <a href="https://oembed.com/">oembed</a> url from the full URL of a streaming.
   * <p>
   *   oEmbed is a format for allowing an embedded representation of a URL on third party sites.
   *   The simple API allows a website to display embedded content (such as photos or videos)
   *   when a user posts a link to that resource, without having to parse the resource directly.
   * </p>
   * <p>
   *   All streaming providers implementing oembed services are listed here:
   *   <a href="https://oembed.com/providers.json">https://oembed.com/providers.json</a>
   * </p>
   * @param streamingUrl the full url of a streaming.
   * @return the oembed url as string.
   */
  public Optional<String> getOembedUrl(String streamingUrl) {
    return getFromUrl(streamingUrl)
        .flatMap(p -> p.extractStreamingId(streamingUrl)
            .map(i -> format(p.getOembedUrlStringPattern(), i)));
  }
}
