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

package org.silverpeas.core.wbe;

import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.StringUtil;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptySet;
import static java.util.Collections.synchronizedSet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.silverpeas.core.wbe.WbeLogger.logger;

/**
 * @author silveryocha
 */
public class WbeCache {
  private final Map<String, Element<WbeUser>> wbeUserCache = new ConcurrentHashMap<>();
  private final Map<String, String> wbeAccessTokenUserMapping = new ConcurrentHashMap<>();
  private final Map<String, Set<String>> wbeUserIdUserMapping = new ConcurrentHashMap<>();
  private final Map<String, Element<WbeFile>> wbeFileCache = new ConcurrentHashMap<>();
  private final Map<String, String> silverpeasResourceWbeFileMapping = new ConcurrentHashMap<>();
  private final Map<String, Set<String>> wbeFileUsersEditionCache = new ConcurrentHashMap<>();
  private final Map<String, Set<String>> wbeUserFilesEditionCache = new ConcurrentHashMap<>();

  /**
   * Gets the list of WBE users from the context.
   * @return a list of {@link WbeUser} instances.
   */
  List<WbeUser> listAllUsers() {
    return getConsistentWbeUserCache().values().stream().map(Element::get).collect(toList());
  }

  /**
   * Gets the list of WBE files from the context.
   * @return a list of {@link WbeFile} instances.
   */
  List<WbeFile> listAllFiles() {
    return getConsistentWbeFileCache().values().stream().map(Element::get).collect(toList());
  }

  /**
   * Gets a pair of user session identifier and {@link WbeUser} registered into the cache from
   * its access token.
   * @param accessToken an access token as string.
   * @return an optional pair of spSessionId and {@link WbeUser}.
   */
  Optional<Pair<String, WbeUser>> getFileFromAccessToken(final String accessToken) {
    return ofNullable(wbeAccessTokenUserMapping.get(accessToken))
        .map(j -> Pair.of(j, wbeUserCache.get(j)))
        .filter(p -> p.getSecond() != null)
        .map(p -> Pair.of(p.getFirst(), p.getSecond().get()));
  }

  /**
   * Gets a {@link WbeFile} registered into the cache from its identifier.
   * @param fileId a file identifier as string.
   * @return an optional {@link WbeFile}.
   */
  Optional<WbeFile> getFileFromId(final String fileId) {
    return ofNullable(wbeFileCache.get(fileId)).map(Element::get);
  }

  /**
   * Adds a new user into context if it does not yet exists.
   * @param spSessionId the identifier of session of the user.
   * @param userSupplier the {@link WbeUser} supplier.
   * @return the {@link WbeUser} registered into the cache.
   */
  WbeUser computeUserIfAbsent(final String spSessionId, Supplier<WbeUser> userSupplier) {
    return wbeUserCache.computeIfAbsent(spSessionId, j -> {
      final WbeUser wbeUser = userSupplier.get();
      wbeAccessTokenUserMapping.putIfAbsent(wbeUser.getAccessToken(), spSessionId);
      wbeUserIdUserMapping.computeIfAbsent(wbeUser.getId(), k -> synchronizedSet(new HashSet<>())).add(spSessionId);
      return new Element<>(wbeUser, w -> {
        final String wuId = wbeAccessTokenUserMapping.remove(w.getAccessToken());
        final Set<String> spSessionIds = wbeUserIdUserMapping.get(w.getId());
        spSessionIds.remove(w.getSilverpeasSessionId());
        if (spSessionIds.isEmpty()) {
          wbeUserIdUserMapping.remove(w.getId());
        }
        wbeUserFilesEditionCache.remove(wuId);
        wbeFileUsersEditionCache.forEach((k, v) -> v.remove(wuId));
      });
    }).get();
  }

  /**
   * Adds a new file into context if it does not yet exists.
   * @param file the file to handle.
   * @return the {@link WbeFile} registered into the cache.
   */
  WbeFile computeFileIfAbsent(final WbeFile file) {
    final String silverpeasId = file.silverpeasId();
    final String fileId = file.id();
    final String existingFileId = silverpeasResourceWbeFileMapping.get(silverpeasId);
    if (existingFileId != null && !existingFileId.equals(fileId)) {
      try {
        wbeFileCache.remove(existingFileId).notifyRemove();
      } catch (NullPointerException e) {
        silverpeasResourceWbeFileMapping.remove(silverpeasId);
        logger().warn("file {0} was altered into cache... Cache has been cleaned", existingFileId);
      }
    }
    return wbeFileCache.computeIfAbsent(fileId,
        i -> {
          silverpeasResourceWbeFileMapping.putIfAbsent(silverpeasId, fileId);
          return new Element<>(file, f -> {
            final String wfId = silverpeasResourceWbeFileMapping.remove(silverpeasId);
            wbeFileUsersEditionCache.remove(wfId);
            wbeUserFilesEditionCache.forEach((key, value) -> value.remove(wfId));
          });
        }).get();
  }

  /**
   * Releases the given user from the cache if handled.
   * @param user a Silverpeas's WBE user.
   */
  void removeUser(final WbeUser user) {
    final String sessionId = user.getSilverpeasSessionId();
    final Element<WbeUser> wbeUser = wbeUserCache.get(sessionId);
    if (wbeUser != null) {
      wbeUser.notifyRemove();
      wbeUserCache.remove(sessionId);
      logger().debug(() ->
          format("releasing user with sessionId {0} (user {1})", sessionId, wbeUser));
    } else {
      logger().debug("releasing user with sessionId {0} which was not into context", sessionId);
    }
  }

  /**
   * Releases the given WBE file from the cache if handled.
   * @param file a Silverpeas's WBE file.
   */
  void removeFile(final WbeFile file) {
    final String wbeFileId = silverpeasResourceWbeFileMapping.get(file.silverpeasId());
    if (wbeFileId != null) {
      final Element<WbeFile> wbeFileElement = wbeFileCache.get(wbeFileId);
      if (wbeFileElement != null) {
        wbeFileElement.notifyRemove();
        wbeFileCache.remove(wbeFileId);
        logger().debug(() -> format("releasing silverpeas file with id {0} (WBE id {1})",
            file.silverpeasId(), wbeFileId));
      } else {
        logger().warn("releasing document {0} from cache which was altered for this document",
            file.silverpeasId());
      }
    }
  }

  /**
   * Clears the cache.
   */
  void clear() {
    wbeAccessTokenUserMapping.clear();
    wbeUserCache.clear();
    silverpeasResourceWbeFileMapping.clear();
    wbeFileCache.clear();
    wbeFileUsersEditionCache.clear();
    wbeUserFilesEditionCache.clear();
  }

  /**
   * Clears all the date that have not been manipulated since the given date.
   * @param offset the offset date.
   */
  void clearAllBefore(final OffsetDateTime offset) {
    wbeFileCache.entrySet().removeIf(e -> {
      final boolean toRemove = offset.compareTo(e.getValue().getLastAccess()) > 0;
      if (toRemove) {
        logger().debug(() -> format("removing from cache {0}", e));
        e.getValue().notifyRemove();
      }
      return toRemove;
    });
  }

  /**
   * Registers a set of users on a file.
   * @param file a {@link WbeFile}.
   * @param userIds a set of WBE user identifiers.
   */
  void registerEdition(final WbeFile file, final Set<String> userIds) {
    final Set<String> spSessionIds = userIds.stream()
        .flatMap(i -> wbeUserIdUserMapping.get(i).stream())
        .filter(StringUtil::isDefined)
        .collect(Collectors.toSet());
    final String fileId = file.id();
    final Set<String> userIdsInCache = wbeFileUsersEditionCache
        .computeIfAbsent(fileId, s -> synchronizedSet(new HashSet<>()));
    synchronized (userIdsInCache) {
      final Set<String> removed = new HashSet<>(userIdsInCache.size());
      final Set<String> added = new HashSet<>(userIdsInCache.size());
      spSessionIds.forEach(i -> {
        if (!userIdsInCache.contains(i)) {
          added.add(i);
        }
      });
      userIdsInCache.forEach(i -> {
        if (!spSessionIds.contains(i)) {
          removed.add(i);
        }
      });
      userIdsInCache.removeAll(removed);
      userIdsInCache.addAll(added);
      removed.forEach(i -> wbeUserFilesEditionCache.computeIfPresent(i, (s, l) -> {
        l.remove(fileId);
        return l;
      }));
      added.forEach(i -> {
        final Set<String> fileIdsInCache = wbeUserFilesEditionCache
            .computeIfAbsent(i, s -> synchronizedSet(new HashSet<>()));
        fileIdsInCache.add(fileId);
      });
    }
  }

  /**
   * Gets the list of file edited by the given user.
   * @param user a {@link WbeUser} instance.
   * @return a list of {@link WbeFile}.
   */
  List<WbeFile> getEditedFilesBy(final WbeUser user) {
    return wbeUserIdUserMapping.get(user.getId())
        .stream()
        .flatMap(s -> wbeUserFilesEditionCache.getOrDefault(s, emptySet()).stream())
        .map(wbeFileCache::get)
        .filter(Objects::nonNull)
        .map(Element::get)
        .distinct()
        .collect(toList());
  }

  /**
   * Gets the list of users which are editor of the given file.
   * @param file a {@link WbeFile} instance.
   * @return a list of {@link WbeUser}.
   */
  List<WbeUser> getEditorsOfFile(final WbeFile file) {
    return wbeFileUsersEditionCache.getOrDefault(file.id(), emptySet()).stream()
        .map(wbeUserCache::get)
        .filter(Objects::nonNull)
        .map(Element::get)
        .collect(toList());
  }

  private Map<String, Element<WbeUser>> getConsistentWbeUserCache() {
    return wbeUserCache;
  }

  private Map<String, Element<WbeFile>> getConsistentWbeFileCache() {
    final Iterator<Map.Entry<String, Element<WbeFile>>> it = wbeFileCache.entrySet().iterator();
    while (it.hasNext()) {
      final Map.Entry<String, Element<WbeFile>> entry = it.next();
      try {
        entry.getValue().get().id();
      } catch (final Exception e) {
        it.remove();
        logger().warn("removing WBE file indexed by id {0}", entry.getKey());
      }
    }
    return wbeFileCache;
  }

  private static class Element<T> {
    private final T value;
    private final Consumer<T> onRemove;
    private OffsetDateTime lastAccess = OffsetDateTime.now();

    private Element(final T value, final Consumer<T> onRemove) {
      this.value = value;
      this.onRemove = onRemove;
    }

    T get() {
      lastAccess = OffsetDateTime.now();
      return value;
    }

    OffsetDateTime getLastAccess() {
      return lastAccess;
    }

    void notifyRemove() {
      onRemove.accept(value);
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", Element.class.getSimpleName() + "[", "]")
          .add("element=" + value).add("lastAccess=" + lastAccess).toString();
    }
  }
}
