/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.search;

import org.silverpeas.core.admin.domain.model.DomainProperties;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.UserIndexation;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.index.search.model.DidYouMeanSearcher;
import org.silverpeas.core.index.search.model.IndexSearcher;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.index.search.model.SearchCompletion;
import org.silverpeas.core.security.authorization.ComponentAuthorization;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A SimpleSearchEngine search Silverpeas indexes index and give access to the retrieved index
 * entries.
 */
@Singleton
public class SimpleSearchEngine implements SearchEngine {

  @Inject
  private DidYouMeanSearcher didYouMeanSearcher;
  @Inject
  private IndexSearcher indexSearcher;

  SettingBundle pdcSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.pdcPeas.settings.pdcPeasSettings");
  private final float minScore = pdcSettings.getFloat("wordSpellingMinScore", 0.5f);
  private final boolean enableWordSpelling = pdcSettings.getBoolean("enableWordSpelling", false);
  private final boolean enableExternalSearch =
      pdcSettings.getBoolean("external.search.enable", false);
  private final String localServerName = pdcSettings.getString("server.name");

  /**
   * Search the index for the required documents.
   * @param query the search query.
   * @return the results.
   */
  @Override
  public PlainSearchResult search(QueryDescription query) throws ParseException {
    try {
      List<MatchingIndexEntry> results = Arrays.asList(indexSearcher.search(query));
      // filter results to checkout specific rights
      results = filterMatchingIndexEntries(results, query.getSearchingUser());
      @SuppressWarnings("unchecked") Set<String> spellingWords = Collections.EMPTY_SET;
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
   * Hide constructor.
   */
  private SimpleSearchEngine() {
  }

  /**
   * check if the results score is low enough to suggest spelling words
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
    List<MatchingIndexEntry> results = new ArrayList<>(matchingIndexEntries.size());
    ComponentAuthorization authorization = null;
    try {
      authorization = getSecurityIntf();
      authorization.enableCache();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }

    for (MatchingIndexEntry result : matchingIndexEntries) {
      if (!isMatchingIndexEntryAvailable(result, userId, authorization)) {
        continue;
      }
      results.add(result);
    }

    authorization.disableCache();

    return results;
  }

  private boolean isMatchingIndexEntryAvailable(MatchingIndexEntry mie, String userId,
      ComponentAuthorization authorization) {
    // Do not filter and check external components
    if (enableExternalSearch && isExternalComponent(mie.getServerName())) {
      mie.setExternalResult(true);
      // Filter only Publication and Node data
      String objectType = mie.getObjectType();
      return "Versioning".equals(objectType) || "Publication".equals(objectType)
          || "Node".equals(objectType);
    }

    // verify rights according to 'kmelia' rights
    String componentId = mie.getComponent();
    if (componentId.startsWith("kmelia")) {
      return authorization
          .isObjectAvailable(componentId, userId, mie.getObjectId(), mie.getObjectType());
    }

    // verify rights onto others items type
    return isOtherItemAvailable(mie, userId);
  }

  private boolean isOtherItemAvailable(MatchingIndexEntry mie, String userId) {
    String objectType = mie.getObjectType();
    if ("Space".equals(objectType)) {
      // check if space is allowed to current user
      return isSpaceVisible(mie.getObjectId(), userId);
    }
    if ("Component".equals(objectType)) {
      // check if component is allowed to current user
      return isComponentVisible(mie.getObjectId(), userId);
    }
    if (UserIndexation.OBJECT_TYPE.equals(objectType)) {
      return isUserVisible(mie.getObjectId());
    }
    return true;
  }

  private boolean isSpaceVisible(String spaceId, String userId) {
    // check if space is allowed to current user
    try {
      return Administration.get().isSpaceAvailable(spaceId, userId);
    } catch (Exception ignored) {
      SilverLogger.getLogger(this).warn("Can't test if space {0} is available for user {1}",
          new String[] {spaceId, userId}, ignored);
      return false;
    }
  }

  private boolean isComponentVisible(String appId, String userId) {
    try {
      return Administration.get().isComponentAvailable(appId, userId);
    } catch (Exception ignored) {
      SilverLogger.getLogger(this).warn("Can't test if component {0} is available for user {1}",
          new String[] {appId, userId}, ignored);
      return false;
    }
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
    if (StringUtil.isDefined(localServerName) && !localServerName.equalsIgnoreCase(serverName)) {
      return true;
    }
    return false;
  }

  private ComponentAuthorization getSecurityIntf()
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    return (ComponentAuthorization) Class.forName(
          "org.silverpeas.components.kmelia.KmeliaAuthorization").newInstance();
  }
}
