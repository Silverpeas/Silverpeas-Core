/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.core.wopi;

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
import static org.silverpeas.core.wopi.WopiLogger.logger;

/**
 * @author silveryocha
 */
public class WopiCache {
  private final Map<String, Element<WopiUser>> wopiUserCache = new ConcurrentHashMap<>();
  private final Map<String, String> wopiAccessTokenUserMapping = new ConcurrentHashMap<>();
  private final Map<String, Set<String>> wopiUserIdUserMapping = new ConcurrentHashMap<>();
  private final Map<String, Element<WopiFile>> wopiFileCache = new ConcurrentHashMap<>();
  private final Map<String, String> silverpeasResourceWopiFileMapping = new ConcurrentHashMap<>();
  private final Map<String, Set<String>> wopiFileUsersEditionCache = new ConcurrentHashMap<>();
  private final Map<String, Set<String>> wopiUserFilesEditionCache = new ConcurrentHashMap<>();

  /**
   * Gets the list of WOPI users from the context.
   * @return a list of {@link WopiUser} instances.
   */
  List<WopiUser> listAllUsers() {
    return getConsistentWopiUserCache().values().stream().map(Element::get).collect(toList());
  }

  /**
   * Gets the list of WOPI files from the context.
   * @return a list of {@link WopiFile} instances.
   */
  List<WopiFile> listAllFiles() {
    return getConsistentWopiFileCache().values().stream().map(Element::get).collect(toList());
  }

  /**
   * Gets a pair of user session identifier and {@link WopiUser} registered into the cache from
   * its access token.
   * @param accessToken an access token as string.
   * @return an optional pair of spSessionId and {@link WopiUser}.
   */
  Optional<Pair<String, WopiUser>> getFileFromAccessToken(final String accessToken) {
    return ofNullable(wopiAccessTokenUserMapping.get(accessToken))
        .map(j -> Pair.of(j, wopiUserCache.get(j)))
        .filter(p -> p.getSecond() != null)
        .map(p -> Pair.of(p.getFirst(), p.getSecond().get()));
  }

  /**
   * Gets a {@link WopiFile} registered into the cache from its identifier.
   * @param fileId a file identifier as string.
   * @return an optional {@link WopiFile}.
   */
  Optional<WopiFile> getFileFromId(final String fileId) {
    return ofNullable(wopiFileCache.get(fileId)).map(Element::get);
  }

  /**
   * Adds a new user into context if it does not yet exists.
   * @param spSessionId the identifier of session of the user.
   * @param userSupplier the {@link WopiUser} supplier.
   * @return the {@link WopiUser} registered into the cache.
   */
  WopiUser computeUserIfAbsent(final String spSessionId, Supplier<WopiUser> userSupplier) {
    return wopiUserCache.computeIfAbsent(spSessionId, j -> {
      final WopiUser wopiUser = userSupplier.get();
      wopiAccessTokenUserMapping.putIfAbsent(wopiUser.getAccessToken(), spSessionId);
      wopiUserIdUserMapping.computeIfAbsent(wopiUser.getId(), k -> synchronizedSet(new HashSet<>())).add(spSessionId);
      return new Element<>(wopiUser, w -> {
        final String wuId = wopiAccessTokenUserMapping.remove(w.getAccessToken());
        final Set<String> spSessionIds = wopiUserIdUserMapping.get(w.getId());
        spSessionIds.remove(w.getSilverpeasSessionId());
        if (spSessionIds.isEmpty()) {
          wopiUserIdUserMapping.remove(w.getId());
        }
        wopiUserFilesEditionCache.remove(wuId);
        wopiFileUsersEditionCache.forEach((k, v) -> v.remove(wuId));
      });
    }).get();
  }

  /**
   * Adds a new file into context if it does not yet exists.
   * @param file the file to handle.
   * @return the {@link WopiFile} registered into the cache.
   */
  WopiFile computeFileIfAbsent(final WopiFile file) {
    final String silverpeasId = file.silverpeasId();
    final String fileId = file.id();
    final String existingFileId = silverpeasResourceWopiFileMapping.get(silverpeasId);
    if (existingFileId != null && !existingFileId.equals(fileId)) {
      try {
        wopiFileCache.remove(existingFileId).notifyRemove();
      } catch (NullPointerException e) {
        silverpeasResourceWopiFileMapping.remove(silverpeasId);
        logger().warn("file {0} was altered into cache... Cache has been cleaned", existingFileId);
      }
    }
    return wopiFileCache.computeIfAbsent(fileId,
        i -> {
          silverpeasResourceWopiFileMapping.putIfAbsent(silverpeasId, fileId);
          return new Element<>(file, f -> {
            final String wfId = silverpeasResourceWopiFileMapping.remove(silverpeasId);
            wopiFileUsersEditionCache.remove(wfId);
            wopiUserFilesEditionCache.forEach((key, value) -> value.remove(wfId));
          });
        }).get();
  }

  /**
   * Releases the given user from the cache if handled.
   * @param user a Silverpeas's WOPI user.
   */
  void removeUser(final WopiUser user) {
    final String sessionId = user.getSilverpeasSessionId();
    final Element<WopiUser> wopiUser = wopiUserCache.get(sessionId);
    if (wopiUser != null) {
      wopiUser.notifyRemove();
      wopiUserCache.remove(sessionId);
      logger().debug(() ->
          format("releasing user with sessionId {0} (user {1})", sessionId, wopiUser));
    } else {
      logger().warn("releasing user with sessionId {0} which was altered into context (user {1})",
          sessionId, user);
    }
  }

  /**
   * Releases the given wopi file from the cache if handled.
   * @param file a Silverpeas's WOPI file.
   */
  void removeFile(final WopiFile file) {
    final String wopiFileId = silverpeasResourceWopiFileMapping.get(file.silverpeasId());
    if (wopiFileId != null) {
      final Element<WopiFile> wopiFileElement = wopiFileCache.get(wopiFileId);
      if (wopiFileElement != null) {
        wopiFileElement.notifyRemove();
        wopiFileCache.remove(wopiFileId);
        logger().debug(() -> format("releasing silverpeas file with id {0} (wopi id {1})",
            file.silverpeasId(), wopiFileId));
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
    wopiAccessTokenUserMapping.clear();
    wopiUserCache.clear();
    silverpeasResourceWopiFileMapping.clear();
    wopiFileCache.clear();
    wopiFileUsersEditionCache.clear();
    wopiUserFilesEditionCache.clear();
  }

  /**
   * Clears all the date that have not been manipulated since the given date.
   * @param offset the offset date.
   */
  void clearAllBefore(final OffsetDateTime offset) {
    wopiFileCache.entrySet().removeIf(e -> {
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
   * @param file a {@link WopiFile}.
   * @param userIds a set of WOPI user identifiers.
   */
  void registerEdition(final WopiFile file, final Set<String> userIds) {
    final Set<String> spSessionIds = userIds.stream()
        .flatMap(i -> wopiUserIdUserMapping.get(i).stream())
        .filter(StringUtil::isDefined)
        .collect(Collectors.toSet());
    final String fileId = file.id();
    final Set<String> userIdsInCache = wopiFileUsersEditionCache
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
      removed.forEach(i -> wopiUserFilesEditionCache.computeIfPresent(i, (s, l) -> {
        l.remove(fileId);
        return l;
      }));
      added.forEach(i -> {
        final Set<String> fileIdsInCache = wopiUserFilesEditionCache
            .computeIfAbsent(i, s -> synchronizedSet(new HashSet<>()));
        fileIdsInCache.add(fileId);
      });
    }
  }

  /**
   * Gets the list of file edited by the given user.
   * @param user a {@link WopiUser} instance.
   * @return a list of {@link WopiFile}.
   */
  List<WopiFile> getEditedFilesBy(final WopiUser user) {
    return wopiUserIdUserMapping.get(user.getId())
        .stream()
        .flatMap(s -> wopiUserFilesEditionCache.getOrDefault(s, emptySet()).stream())
        .map(wopiFileCache::get)
        .filter(Objects::nonNull)
        .map(Element::get)
        .distinct()
        .collect(toList());
  }

  /**
   * Gets the list of users which are editor of the given file.
   * @param file a {@link WopiFile} instance.
   * @return a list of {@link WopiUser}.
   */
  List<WopiUser> getEditorsOfFile(final WopiFile file) {
    return wopiFileUsersEditionCache.getOrDefault(file.id(), emptySet()).stream()
        .map(wopiUserCache::get)
        .filter(Objects::nonNull)
        .map(Element::get)
        .collect(toList());
  }

  private Map<String, Element<WopiUser>> getConsistentWopiUserCache() {
    return wopiUserCache;
  }

  private Map<String, Element<WopiFile>> getConsistentWopiFileCache() {
    final Iterator<Map.Entry<String, Element<WopiFile>>> it = wopiFileCache.entrySet().iterator();
    while (it.hasNext()) {
      final Map.Entry<String, Element<WopiFile>> entry = it.next();
      try {
        entry.getValue().get().id();
      } catch (final Exception e) {
        it.remove();
        logger().warn("removing WOPI file indexed by id {0}", entry.getKey());
      }
    }
    return wopiFileCache;
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
