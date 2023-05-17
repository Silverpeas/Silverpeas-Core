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

package org.silverpeas.core.web.token;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedBean;
import org.silverpeas.core.web.token.SilverpeasWebTokenService.WebToken;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.shuffle;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.test.util.TestRuntime.awaitUntil;
import static org.silverpeas.core.web.token.SilverpeasWebTokenService.MAX_TOKENS_PER_ID;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class SilverpeasWebTokenServiceTest {

  private static final String DEFAULT_ID = "anIdentifier";
  private static final int NB_IDENTIFIERS = MAX_TOKENS_PER_ID + 10;

  @TestManagedBean
  private SilverpeasWebTokenService service;

  private RepositoryView repositoryView;

  @BeforeEach
  void setup() {
    repositoryView = new RepositoryView(service);
    empty();
  }

  @Test
  void empty() {
    assertThat(repositoryView.getByTokensMap(), anEmptyMap());
    assertThat(repositoryView.getByIdsMap(), anEmptyMap());
  }

  @Test
  void generateOneToken() {
    generateOneTokenWith(DEFAULT_ID);
  }

  @Test
  void consumeIdentifierBy() {
    final WebToken webToken = generateOneTokenWith(DEFAULT_ID);
    assertThat(service.consumeIdentifierBy(webToken.getId()).orElse(null), nullValue());
    assertMapAreNotEmpty();
    final String id = service.consumeIdentifierBy(webToken.getValue()).orElse(null);
    assertThat(id, is(webToken.getId()));
    empty();
  }

  @Test
  void revokeById() {
    final WebToken webToken = generateOneTokenWith(DEFAULT_ID);
    service.revokeById(webToken.getValue());
    assertMapAreNotEmpty();
    service.revokeById(webToken.getId());
    empty();
  }

  @Test
  void verifyMaxPerId() {
    final int nbCalls = MAX_TOKENS_PER_ID + 10;
    final List<WebToken> generatedTokens = IntStream.range(0, nbCalls)
        .mapToObj(i -> service.generateFor(DEFAULT_ID))
        .peek(s -> awaitUntil(20, TimeUnit.MILLISECONDS))
        .collect(Collectors.toList());
    final WebToken[] expectedTokens = generatedTokens.stream()
        .skip(generatedTokens.size() - MAX_TOKENS_PER_ID)
        .toArray(WebToken[]::new);
    assertThat(expectedTokens.length, is(MAX_TOKENS_PER_ID));
    final Map<String, WebToken> byTokensMap = repositoryView.getByTokensMap();
    final Map<String, Set<WebToken>> byIdsMap = repositoryView.getByIdsMap();
    assertThat(byTokensMap.size(), is(MAX_TOKENS_PER_ID));
    Stream.of(expectedTokens).forEach(t -> {
      final WebToken webToken = byTokensMap.get(t.getValue());
      assertThat(t.getId(), is(DEFAULT_ID));
      assertThat(t, is(webToken));
    });
    assertThat(byIdsMap.size(), is(1));
    final Set<WebToken> actualTokens = byIdsMap.get(DEFAULT_ID);
    assertThat(actualTokens, notNullValue());
    assertThat(actualTokens, contains(expectedTokens));
  }

  @Test
  void revokingAfterMaxPerIdReached() {
    verifyMaxPerId();
    service.revokeById(DEFAULT_ID);
    empty();
  }

  @Test
  void handleSeveralIds() {
    final List<String> identifiers = IntStream.range(0, NB_IDENTIFIERS)
        .mapToObj(String::valueOf)
        .collect(Collectors.toList());
    shuffle(identifiers);
    final List<WebToken> generatedTokens = identifiers.stream()
        .map(service::generateFor)
        .peek(s -> awaitUntil(20, TimeUnit.MILLISECONDS))
        .collect(Collectors.toList());
    final Map<String, WebToken> byTokensMap = repositoryView.getByTokensMap();
    assertThat(byTokensMap.size(), is(NB_IDENTIFIERS));
    generatedTokens.forEach(t -> {
      final WebToken webToken = byTokensMap.get(t.getValue());
      assertThat(t, is(webToken));
    });
    final Map<String, Set<WebToken>> byIdsMap = repositoryView.getByIdsMap();
    assertThat(byIdsMap.size(), is(NB_IDENTIFIERS));
    byIdsMap.forEach((key, value) -> assertThat(value, hasSize(1)));
  }

  @Test
  void revokingAfterSeveralIds() {
    handleSeveralIds();
    service.revokeById(String.valueOf(MAX_TOKENS_PER_ID - 1));
    final Map<String, WebToken> byTokensMap = repositoryView.getByTokensMap();
    assertThat(byTokensMap.size(), is(NB_IDENTIFIERS - 1));
    final Map<String, Set<WebToken>> byIdsMap = repositoryView.getByIdsMap();
    assertThat(byIdsMap.size(), is(NB_IDENTIFIERS - 1));
    byIdsMap.forEach((key, value) -> assertThat(value, hasSize(1)));
  }

  private void assertMapAreNotEmpty() {
    assertThat(repositoryView.getByTokensMap(), not(anEmptyMap()));
    assertThat(repositoryView.getByIdsMap(), not(anEmptyMap()));
  }

  @SuppressWarnings("SameParameterValue")
  private WebToken generateOneTokenWith(final String id) {
    final WebToken webToken = service.generateFor(id);
    assertThat(webToken, notNullValue());
    assertThat(webToken.getId(), is(DEFAULT_ID));
    assertThat(webToken.getValue().length(), is(randomUUID().toString().length()));
    assertThat(repositoryView.getByTokensMap().size(), is(1));
    final WebToken actual = repositoryView.getByTokensMap().get(webToken.getValue());
    assertThat(actual, is(webToken));
    assertThat(repositoryView.getByIdsMap().size(), is(1));
    final Set<WebToken> webTokens = repositoryView.getByIdsMap().get(webToken.getId());
    assertThat(webTokens, notNullValue());
    assertThat(webTokens, contains(webToken));
    return webToken;
  }

  private static class RepositoryView {
    private final Object repository;

    private RepositoryView(final SilverpeasWebTokenService service) {
      this.repository = get(service, "repository");
    }

    private Map<String, WebToken> getByTokensMap() {
      return get(repository, "byTokens");
    }

    private Map<String, Set<WebToken>> getByIdsMap() {
      return get(repository, "byIds");
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(final Object object, final String attributeName) {
      try {
        return (T) FieldUtils.readDeclaredField(object, attributeName, true);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}