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

package org.silverpeas.core.webapi.media.streaming;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.media.streaming.StreamingProvider;
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * This factory permits to instantiate the right {@link StreamingProviderDataEntity} instance
 * from a {@link OembedDataEntity} decoded from the response of a streaming provider about a full
 * URL of a streaming.
 * <p>
 *   If the factory is not able to construct a {@link StreamingProviderDataEntity} from oembed
 *   data, then a default one is instantiated.
 * </p>
 * <p>
 *   When some specific behaviors MUST be taken into account for streamings of a provider, then a
 *   specific constructor override of {@link StreamingProviderDataEntity} MUST be implemented and
 *   indicated to factory by using {@link #registerConstructor(String, BiFunction)} method. As
 *   {@link YoutubeDataEntity} for example.
 * </p>
 * @author silveryocha
 */
@Service
@Singleton
public class StreamingProviderDataEntityFactory {

  private static final BiFunction<StreamingProvider, OembedDataEntity,
      StreamingProviderDataEntity> DEFAULT = StreamingProviderDataEntity::new;

  private final Map<String, BiFunction<StreamingProvider, OembedDataEntity, StreamingProviderDataEntity>>
      constructorRegistry = Collections.synchronizedMap(new HashMap<>());

  public static StreamingProviderDataEntityFactory get() {
    return ServiceProvider.getService(StreamingProviderDataEntityFactory.class);
  }

  /**
   * Registers default constructors of {@link StreamingProviderDataEntity} of Silverpeas's server.
   */
  @PostConstruct
  protected void setupDefaults() {
    registerConstructor(YoutubeDataEntity.NAME, YoutubeDataEntity::new);
    registerConstructor(VimeoDataEntity.NAME, VimeoDataEntity::new);
  }

  /**
   * Registers a new {@link StreamingProviderDataEntity} constructor.
   * <p>
   *   It permits to external projects to defined theirs own constructors if needed.
   * </p>
   * @param providerName name of streaming provider (lowercase).
   * @param constructor the constructor of {@link StreamingProviderDataEntity}.
   */
  public void registerConstructor(final String providerName,
      final BiFunction<StreamingProvider, OembedDataEntity, StreamingProviderDataEntity> constructor) {
    constructorRegistry.put(providerName, constructor);
  }

  /**
   * Initializes the {@link StreamingProviderDataEntity} from given elements.
   * <p>
   *   If no specific constructors is known for a streaming provider, then a default one is used.
   * </p>
   * @param streamingProvider a {@link StreamingProvider} instance.
   * @param oembedData the oembed data.
   * @return a {@link StreamingProviderDataEntity} instance.
   */
  StreamingProviderDataEntity createWith(final StreamingProvider streamingProvider,
      final OembedDataEntity oembedData) {
    return constructorRegistry
        .getOrDefault(streamingProvider.getName(), DEFAULT)
        .apply(streamingProvider, oembedData);
  }
}
