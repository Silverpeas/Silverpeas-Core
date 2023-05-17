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

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestedBean;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class StreamingProvidersRegistryTest {

  @TestedBean
  private StreamingProvidersRegistry registry;

  @Test
  void testFrom() {
    assertThat(registry.getAll(), hasSize(4));
    assertThat(registry.getByName(null).isPresent(), is(false));
    assertThat(registry.getByName("").isPresent(), is(false));
    assertThat(registry.getByName(" ").isPresent(), is(false));
    assertThat(registry.getByName(" youtube").isPresent(), is(false));
    assertThat(registry.getByName("youtube")
        .map(StreamingProvider::getName).orElse(null), is("youtube"));
    assertThat(registry.getByName("vImeO")
        .map(StreamingProvider::getName).orElse(null), is("vimeo"));
    assertThat(registry.getByName("dAilyMotion")
        .map(StreamingProvider::getName).orElse(null), is("dailymotion"));
  }

  @Test
  void testFromUrl() {
    assertThat(registry.getFromUrl(null).isPresent(), is(false));
    assertThat(registry.getFromUrl("").isPresent(), is(false));
    assertThat(registry.getFromUrl(" ").isPresent(), is(false));
    assertThat(registry.getFromUrl(" youtube")
        .map(StreamingProvider::getName).orElse(null), is("youtube"));
    assertThat(registry.getFromUrl("youtube")
        .map(StreamingProvider::getName).orElse(null), is("youtube"));
    assertThat(registry.getFromUrl("vImeO")
        .map(StreamingProvider::getName).orElse(null), is("vimeo"));
    assertThat(registry.getFromUrl("http://vImeO.be/123456789")
        .map(StreamingProvider::getName).orElse(null), is("vimeo"));
    assertThat(registry.getFromUrl("http://www.dailymotion.com/video/x3fd843_bever")
            .map(StreamingProvider::getName).orElse(null), is("dailymotion"));
  }

  @Test
  void testExtractStreamingId() {
    StreamingProvider provider = registry.getByName("dailymotion").orElse(null);
    assertThat(provider, notNullValue());
    assertThat(provider.extractStreamingId(
        "http://www.dailymotion.com/video/x3fd843_beverly-piegee-par-l-incroyable-strategie-de" +
            "-gilles_tv").orElse(null), is("x3fd843"));
    provider = registry.getByName("soundcloud").orElse(null);
    assertThat(provider, notNullValue());
    assertThat(provider.extractStreamingId(
            "https://soundcloud.com/empreinte-digiale/saison-1-01-la-lazy-company-jean-sebastien" +
                "-vermalle?in=benjamin-roux-10/sets/lazy-compagny").orElse(null),
        is("empreinte-digiale/saison-1-01-la-lazy-company-jean-sebastien-vermalle?in=benjamin" +
            "-roux-10/sets/lazy-compagny"));
  }

  @Test
  void getYoutubeOembedUrl() {
    final Optional<String> oembedUrl = registry.getOembedUrl("https://youtu.be/6xN3hSEj21Q");
    assertThat(oembedUrl.isPresent(), is(true));
    assertThat(oembedUrl.get(), is("http://www.youtube.com/oembed?url=https://youtu.be/6xN3hSEj21Q&format=json"));
  }

  @Test
  void getVimeoOembedUrl() {
    final Optional<String> oembedUrl = registry.getOembedUrl("https://vimeo.com/21040307");
    assertThat(oembedUrl.isPresent(), is(true));
    assertThat(oembedUrl.get(), is("http://vimeo.com/api/oembed.json?url=http://vimeo.com/21040307"));
  }

  @Test
  void getDailymotionOembedUrl() {
    final Optional<String> oembedUrl = registry.getOembedUrl("https://www.dailymotion.com/video/x3fgyln_jeff-bezos-fait-atterrir-en-secret-la-premiere-fusee-reutilisable_tech");
    assertThat(oembedUrl.isPresent(), is(true));
    assertThat(oembedUrl.get(), is("http://www.dailymotion.com/services/oembed?url=http://www.dailymotion.com/video/x3fgyln"));
  }

  @Test
  void getSoundCloudOembedUrl() {
    final Optional<String> oembedUrl = registry.getOembedUrl("https://soundcloud.com/empreinte-digiale/saison-1-01-la-lazy-company-jean-sebastien-vermalle?in=benjamin-roux-10/sets/lazy-compagny");
    assertThat(oembedUrl.isPresent(), is(true));
    assertThat(oembedUrl.get(), is("http://soundcloud.com/oembed?url=http://soundcloud.com/empreinte-digiale/saison-1-01-la-lazy-company-jean-sebastien-vermalle?in=benjamin-roux-10/sets/lazy-compagny&format=json"));
  }
}