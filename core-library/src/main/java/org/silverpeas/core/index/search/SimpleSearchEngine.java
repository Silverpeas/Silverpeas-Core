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
package org.silverpeas.core.index.search;

import org.silverpeas.core.admin.domain.model.DomainProperties;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.UserIndexation;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.index.search.model.*;
import org.silverpeas.core.security.authorization.ComponentAuthorization;
import org.silverpeas.core.security.authorization.ComponentAuthorization.ComponentResourceReference;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;
import static org.silverpeas.core.security.authorization.AccessControlOperation.SEARCH;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * A SimpleSearchEngine search Silverpeas indexes index and give access to the retrieved index
 * entries.
 */
@Service
@Singleton
public class SimpleSearchEngine implements SearchEngine {

  private static final List<String> EXTERNAL_CONTRIBUTION_TYPE = List.of("Versioning",
      "Publication", "Node");
  private static final Function<FilterMatchingIndexEntryItem, ComponentResourceReference>
      itemAsContributionIdentifier = i -> {
    // Currently, there is no authorization rules on objects that are linked to another object.
    // Their access control is in this case performed by the authorization rules on the linked
    // object in the same component (objects that are linked are handled by the same component).
    final MatchingIndexEntry mie = i.getEntry();
    final String id = mie.isLinkedObject() ? mie.getLinkedObjectId() : mie.getObjectId();
    return new ComponentResourceReference(id, mie.getObjectType(), mie.getComponent());
  };
  @Inject
  private DidYouMeanSearcher didYouMeanSearcher;
  @Inject
  private IndexSearcher indexSearcher;
  @Inject
  private OrganizationController organization;
  private final SettingBundle pdcSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.pdcPeas.settings.pdcPeasSettings");
  private final float minScore = pdcSettings.getFloat("wordSpellingMinScore", 0.5f);
  private final boolean enableWordSpelling = pdcSettings.getBoolean("enableWordSpelling", false);
  private final String localServerName = pdcSettings.getString("server.name");

  /**
   * Hide constructor.
   */
  private SimpleSearchEngine() {
  }

  @Override
  public PlainSearchResult search(QueryDescription query) throws ParseException {
    try {
      List<MatchingIndexEntry> results = Arrays.asList(indexSearcher.search(query));
      if (!query.isAdminScope()) {
        final long startTime = System.currentTimeMillis();
        // filter results to checkout specific rights
        results = filterMatchingIndexEntries(results, query.getSearchingUser());
        final long endTime = System.currentTimeMillis();
        final int nbResultItemsAfterFiltering = results.size();
        SilverLogger.getLogger(this).debug(() -> MessageFormat
            .format(" search index filtering duration in {0} and keeping {1} entries",
                formatDurationHMS(endTime - startTime), nbResultItemsAfterFiltering));
      }
      Set<String> spellingWords = emptySet();
      if (enableWordSpelling && isSpellingNeeded(results)) {
        String[] suggestions = didYouMeanSearcher.suggest(query);
        if (suggestions != null && suggestions.length > 0) {
          spellingWords = new HashSet<>(suggestions.length);
          Collections.addAll(spellingWords, suggestions);
        }
      }
      return new PlainSearchResult(new ArrayList<>(spellingWords), results);
    } catch (IOException ioex) {
      throw new ParseException("SimpleSearchEngine.search", ioex);
    }
  }

  /**
   * check if the results score is low enough to suggest spelling words
   *
   * @return true if the max results score is under the defined threshold
   */
  private boolean isSpellingNeeded(List<MatchingIndexEntry> results) {
    for (MatchingIndexEntry match : results) {
      if (minScore < match.getScore()) {
        return false;
      }
    }
    return true;
  }

  /**
   * gets a list of suggestion from a partial String
   *
   * @param keywordFragment string to execute the search
   * @return a set of result sorted by alphabetic order
   */
  @Override
  public Set<String> suggestKeywords(String keywordFragment) {
    SearchCompletion completion = new SearchCompletion();
    return completion.getSuggestions(keywordFragment);
  }

  private List<MatchingIndexEntry> filterMatchingIndexEntries(
      List<MatchingIndexEntry> matchingIndexEntries, String userId) {
    if (matchingIndexEntries == null || matchingIndexEntries.isEmpty()) {
      return new ArrayList<>();
    }
    // This allows to optimize search (instead of requesting the organization on each result of
    // type 'Component')
    final Set<String> allowedComponentIds = isDefined(userId)
        ? Stream.of(organization.getAvailCompoIds(userId)).collect(Collectors.toSet())
        : emptySet();
    // Convert into items
    final List<FilterMatchingIndexEntryItem> filterItems = matchingIndexEntries.stream()
        .map(FilterMatchingIndexEntryItem::new)
        .collect(Collectors.toList());
    // Filtering external entries: we remove them and get only the non external entries (hence
    // those not yet processed)
    final boolean enableExternalSearch = pdcSettings.getBoolean("external.search.enable", false);
    List<FilterMatchingIndexEntryItem> nonProcessedItems =
        removeExternalEntries(filterItems, enableExternalSearch);
    // Filtering by all existing implementations of ComponentAuthorization interface
    final Iterator<ComponentAuthorization> it = ComponentAuthorization.getAll().iterator();
    while (it.hasNext() && !nonProcessedItems.isEmpty()) {
      final ComponentAuthorization componentAuthorization = it.next();
      nonProcessedItems = checkAccessAuthorization(userId, nonProcessedItems, componentAuthorization);
    }
    // Finalizing the filtering
    return filterItems.stream()
        .filter(r -> isMatchingIndexEntryAvailable(r, userId, allowedComponentIds))
        .map(FilterMatchingIndexEntryItem::getEntry)
        .collect(Collectors.toList());
  }

  @Nonnull
  private List<FilterMatchingIndexEntryItem> checkAccessAuthorization(final String userId,
      final List<FilterMatchingIndexEntryItem> items,
      final ComponentAuthorization componentAuthorization) {
    final List<FilterMatchingIndexEntryItem> processedItems = new ArrayList<>(items.size());
    var nonRelatedItems = items.stream()
        .filter(i -> {
          final boolean relatedTo = componentAuthorization.isRelatedTo(i.getEntry().getComponent());
          if (relatedTo) {
            i.processed();
            processedItems.add(i);
          }
          return !relatedTo;
        })
        .collect(Collectors.toList());
    if (CollectionUtil.isNotEmpty(processedItems)) {
      componentAuthorization
          .filter(processedItems, itemAsContributionIdentifier, userId, SEARCH)
          .forEach(FilterMatchingIndexEntryItem::keep);
    }
    return nonRelatedItems;
  }

  @Nonnull
  private List<FilterMatchingIndexEntryItem> removeExternalEntries(
      final List<FilterMatchingIndexEntryItem> filterItems, final boolean enableExternalSearch) {
    return filterItems.stream()
        .map(i ->
            Optional.of(i.getEntry())
                .filter(e -> enableExternalSearch
                    && isExternalComponent(e.getServerName()))
                .map(e -> {
                  i.processed();
                  e.setExternalResult(true);
                  // Filter only Publication and Node data
                  final String objectType = e.getObjectType();
                  if (EXTERNAL_CONTRIBUTION_TYPE.contains(objectType)) {
                    i.keep();
                  }
                  return i;
                })
                .orElse(i))
        .filter(Predicate.not(FilterMatchingIndexEntryItem::isProcessed))
        .collect(Collectors.toList());
  }

  private boolean isMatchingIndexEntryAvailable(final FilterMatchingIndexEntryItem item,
      final String userId, final Set<String> allowedComponentIds) {
    if (item.isProcessed()) {
      // Item has been already processed
      return item.isToKeep();
    }
    // verify rights onto others items type
    return isOtherItemAvailable(item.getEntry(), userId, allowedComponentIds);
  }

  private boolean isOtherItemAvailable(MatchingIndexEntry mie, String userId,
      Set<String> allowedComponentIds) {
    String objectType = mie.getObjectType();
    if ("Space".equals(objectType)) {
      // check if space is allowed to current user
      return isSpaceVisible(mie.getObjectId(), userId);
    }
    if ("Component".equals(objectType)) {
      // check if component is allowed to current user
      return isComponentVisible(mie.getComponent(), allowedComponentIds);
    }
    if (UserIndexation.OBJECT_TYPE.equals(objectType)) {
      return isUserVisible(mie.getObjectId());
    }
    return true;
  }

  private boolean isSpaceVisible(String spaceId, String userId) {
    // check if space is allowed to current user
    try {
      return Administration.get().isSpaceAvailable(userId, spaceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn("Can't test if space {0} is available for user {1}",
          new String[]{spaceId, userId}, e);
      return false;
    }
  }

  private boolean isComponentVisible(String appId, Set<String> allowedComponentIds) {
    return allowedComponentIds.contains(appId);
  }

  /**
   * Visibility between domains is limited, check found user domain against current user domain
   */
  private boolean isUserVisible(String userId) {
    User userFound = User.getById(userId);
    if (DomainProperties.areDomainsVisibleOnlyToDefaultOne()) {
      String currentUserDomainId = User.getById(userId).getDomainId();
      if ("0".equals(currentUserDomainId)) {
        // current user of default domain can see all users
        return true;
      } else {
        // current user of other domains can see only users of his domain
        return userFound.getDomainId().equals(currentUserDomainId);
      }
    } else if (DomainProperties.areDomainsNonVisibleToOthers()) {
      // user found must be in same domain of current user
      String currentUserDomainId = User.getById(userId).getDomainId();
      return userFound.getDomainId().equals(currentUserDomainId);
    }
    return true;
  }

  private boolean isExternalComponent(String serverName) {
    return StringUtil.isDefined(localServerName) && !localServerName.equalsIgnoreCase(serverName);
  }

  private static class FilterMatchingIndexEntryItem {
    private final MatchingIndexEntry entry;
    private boolean processed = false;
    private boolean toKeep = false;

    private FilterMatchingIndexEntryItem(final MatchingIndexEntry entry) {
      this.entry = entry;
    }

    MatchingIndexEntry getEntry() {
      return entry;
    }

    void processed() {
      this.processed = true;
    }

    boolean isProcessed() {
      return processed;
    }

    public void keep() {
      this.toKeep = true;
    }

    boolean isToKeep() {
      return toKeep;
    }
  }
}
