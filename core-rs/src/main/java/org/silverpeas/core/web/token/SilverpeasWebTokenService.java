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

package org.silverpeas.core.web.token;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static java.util.Collections.synchronizedMap;
import static java.util.Collections.synchronizedSet;
import static java.util.Optional.ofNullable;

/**
 * This service allows generating several WEB tokens per identifier.
 * <p>
 *   An identifier represents any kind of resource (Service, entity, etc.).
 * </p>
 * <p>
 *   This allows a service to identify use token to identify resources without using the real
 *   identifiers.
 * </p>
 * <p>
 *   Generated tokens MUST be cleared by the service which creating them.
 * </p>
 * @author silveryocha
 */
@Technical
@Bean
@Singleton
public class SilverpeasWebTokenService {

  protected static final int MAX_TOKENS_PER_ID = 10;
  private final Repository repository = new Repository();

  public static SilverpeasWebTokenService get() {
    return ServiceProvider.getSingleton(SilverpeasWebTokenService.class);
  }

  /**
   * Generates a new token linked to an identifier.
   * <p>
   *   The token is put into cache.
   * </p>
   * <p>
   *   To retrieve the identifier, please use {@link #consumeIdentifierBy(String)}.
   * </p>
   * <p> At most {@link #MAX_TOKENS_PER_ID} tokens CAN be generated per identifier. When this
   * limit is reached, the next takes previous token space.
   * </p>
   * @param identifier any identifier as string managed by any service.
   * @return a new instance of {@link WebToken}.
   */
  public WebToken generateFor(final String identifier) {
    final WebToken token = new WebToken(identifier);
    repository.register(token);
    return token;
  }

  /**
   * Gets the identifier behind the given token value.
   * @param tokenValue a token value.
   * @return an optional string representing the identifier behind the token.
   */
  public Optional<String> consumeIdentifierBy(final String tokenValue) {
    return repository.consume(tokenValue).map(WebToken::getId);
  }

  /**
   * Removes all generated token for an identifier.
   * @param id any identifier.
   */
  public void revokeById(final String id) {
    repository.removeAllById(id);
  }

  public static class WebToken implements Serializable, Comparable<WebToken> {
    private static final long serialVersionUID = -9174043216375231076L;

    private final String timestamp;
    private final String value;
    private final String id;

    private WebToken(final String id) {
      this.timestamp = LocalDateTime.now().toString();
      this.value = UUID.randomUUID().toString();
      this.id = id;
    }

    public String getValue() {
      return value;
    }

    protected String getId() {
      return id;
    }

    @Override
    public int compareTo(@Nonnull final SilverpeasWebTokenService.WebToken o) {
      return timestamp.compareTo(o.timestamp);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final WebToken that = (WebToken) o;
      return value.equals(that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }
  }

  private static class Repository {
    private final Map<String, WebToken> byTokens = synchronizedMap(new HashMap<>(2000));
    private final Map<String, Set<WebToken>> byIds = synchronizedMap(new HashMap<>(2000));

    protected void register(final WebToken token) {
      byTokens.put(token.value, token);
      byIds.compute(token.id, (s, l) -> {
        final Set<WebToken> tokenList = l == null ? synchronizedSet(new TreeSet<>()) : l;
        if (tokenList.size() >= MAX_TOKENS_PER_ID) {
          removeByToken(tokenList.iterator().next().getValue());
        }
        tokenList.add(token);
        return tokenList;
      });
    }

    protected Optional<WebToken> consume(final String tokenValue) {
      return removeByToken(tokenValue);
    }

    private Optional<WebToken> removeByToken(final String tokenValue) {
      return ofNullable(byTokens.remove(tokenValue)).map(t -> {
        ofNullable(byIds.get(t.getId())).ifPresent(s -> {
          s.removeIf(l -> l.getValue().equals(tokenValue));
          if (s.isEmpty()) {
            byIds.remove(t.getId());
          }
        });
        return t;
      });
    }

    protected void removeAllById(final String id) {
      ofNullable(byIds.remove(id))
          .stream()
          .flatMap(Set::stream)
          .map(WebToken::getValue)
          .forEach(byTokens::remove);
    }
  }
}
