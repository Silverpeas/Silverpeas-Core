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
package org.silverpeas.core.index.search.model;

import org.silverpeas.core.index.indexing.model.ExternalComponent;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.util.StringUtil;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

/**
 * A QueryDescription packs a query with the different spaces and components to be searched.
 */
public final class QueryDescription implements Serializable {

  private static final long serialVersionUID = 1L;
  /**
   * The searched components' instance is built empty. To be searched any space or component must
   * be explicitly added with the addSpaceComponent() method.
   **/
  private HashSet<String> whereToSearch = new HashSet<>();
  /**
   * The query defaults to the empty query. This is an error to set the query to null : a query is
   * needed to perform the search.
   */
  private String query = "";
  /**
   * The searchingUser defaults to the empty query. This is an error to set the user to null : a
   * search is done for a given user.
   */
  private String searchingUser = "";
  /**
   * The others criteria default to null. When the requestedAuthor is null, documents of any author
   * are returned (even if the author is unknown). When the requested Date range is null, all
   * documents are returned even if the creation dates are unknown.
   */
  private String requestedLang = null;
  private String requestedAuthor = null;
  private LocalDate requestedCreatedBefore = null;
  private LocalDate requestedCreatedAfter = null;
  private LocalDate requestedUpdatedBefore = null;
  private LocalDate requestedUpdatedAfter = null;
  private List<FieldDescription> multiFieldQuery = null;
  private boolean searchBySpace = false;
  private boolean searchByComponentType = false;
  private String requestedFolder = null;
  private String taxonomyPosition = null;

  /**
   * The external searched components are build empty. This is a set of ExternalComponent
   */
  private Set<ExternalComponent> extComponents = new HashSet<>();

  private boolean adminScope = false;

  /**
   * The no parameters constructor builds an empty query. The setQuery and addComponent()
   * methods should be called to initialize the query. Other criterion (language, creation date
   * ...)
   * can be set before the request is sent to the searchEngine.
   */
  public QueryDescription() {
    // nothing to do
  }

  /**
   * The constructor set only the query string. The addComponent() method should be called
   * to set the components instances whose documents will be searched. Other criterium (language,
   * creation date ...) can be set before the request is sent to the searchEngine.
   */
  public QueryDescription(String query) {
    setQuery(query);
  }

  /**
   * Set the query string.
   */
  public void setQuery(String query) {
    this.query = (query == null) ? "" : query.toLowerCase();
    // string already in lower case, means "and" "And" "AND" will works.
    this.query = findAndReplace(this.query, " and ", " AND ");
    this.query = findAndReplace(this.query, " or ", " OR ");
    this.query = findAndReplace(this.query, " not ", " NOT ");
    if (this.query.indexOf("not ") == 0) {
      final int notLength = 4;
      this.query = "NOT " + this.query.substring(notLength, this.query.length());
    }

  }

  /**
   * Return the query string.
   * @return the query string
   */
  public String getQuery() {
    return query;
  }

  /**
   * Set the user
   * @param searchingUser : the user.
   */
  public void setSearchingUser(String searchingUser) {
    this.searchingUser = (searchingUser == null) ? "" : searchingUser.toLowerCase();
  }

  /**
   * return the user.
   * @return the user.
   */
  public String getSearchingUser() {
    return searchingUser;
  }

  public void addComponent(String component) {
    whereToSearch.add(component);
  }

  /**
   * Return the set of all the component's instances whose the documents must be searched. The
   * return Set is a set of SpaceComponentPair.
   * @return
   */
  public Set<String> getWhereToSearch() {
    return whereToSearch;
  }

  /**
   * Set the requested language.
   * @param requestedLang
   */
  public void setRequestedLanguage(String requestedLang) {
    this.requestedLang = requestedLang;
  }

  /**
   * Returns the requested language.
   * <p>
   * if no language or wildcard '*' has been set, all languages are checked.
   * </p>
   * @return optional requested language. Empty optional means all languages.
   */
  public Optional<String> getRequestedLanguage() {
    return ofNullable(requestedLang).filter(not("*"::equals));
  }

  /**
   * Set the requested author.
   * @param author
   */
  public void setRequestedAuthor(String author) {
    this.requestedAuthor = (author == null) ? null : author.toLowerCase();
  }

  /**
   * Return the requested author.
   * @return
   */
  public String getRequestedAuthor() {
    return requestedAuthor;
  }

  /**
   * Set the before date
   * @param beforedate
   */
  public void setRequestedCreatedBefore(LocalDate beforedate) {
    this.requestedCreatedBefore = beforedate;
  }

  /**
   * get the before date
   */
  public LocalDate getRequestedCreatedBefore() {
    return requestedCreatedBefore;
  }

  /**
   * Set the after date
   */
  public void setRequestedCreatedAfter(LocalDate afterdate) {
    this.requestedCreatedAfter = afterdate;
  }

  /**
   * get the after date
   */
  public LocalDate getRequestedCreatedAfter() {
    return requestedCreatedAfter;
  }

  public List<FieldDescription> getMultiFieldQuery() {
    return multiFieldQuery;
  }

  public void clearMultiFieldQuery() {
    if (multiFieldQuery != null) {
      multiFieldQuery.clear();
    }
  }

  public void addFieldQuery(FieldDescription fieldQuery) {
    if (fieldQuery == null) {
      return;
    }

    if (multiFieldQuery == null) {
      multiFieldQuery = new ArrayList<>();
    }

    multiFieldQuery.add(fieldQuery);
  }

  public void addFieldQueries(List<FieldDescription> fieldQueries) {
    if (multiFieldQuery == null) {
      multiFieldQuery = new ArrayList<>();
    }

    multiFieldQuery.addAll(fieldQueries);
  }

  public void setFieldQueries(List<FieldDescription> fieldQueries) {
    clearMultiFieldQuery();
    addFieldQueries(fieldQueries);
  }

  public boolean isEmpty() {
    boolean queryDefined = StringUtil.isDefined(query) || getMultiFieldQuery() != null;
    boolean filtersDefined = StringUtil.isDefined(getRequestedAuthor()) || isPeriodDefined();
    return !queryDefined && !filtersDefined;
  }

  /**
   * Find and replace used for the AND OR NOT lucene's keyword in the search engine query.
   * 26/01/2004
   */
  private String findAndReplace(String source, String find, String replace) {
    int index = source.indexOf(find);
    String replacedSource = source;
    while (index > -1) {
      replacedSource = replacedSource.substring(0, index) + replace +
          replacedSource.substring(index + find.length(), replacedSource.length());
      index = replacedSource.indexOf(find);
    }

    return replacedSource;
  }

  public boolean isPeriodDefined() {
    return !Objects.isNull(requestedCreatedAfter) || !Objects.isNull(requestedCreatedBefore) ||
        !Objects.isNull(requestedUpdatedAfter) || !Objects.isNull(requestedUpdatedBefore);
  }

  public LocalDate getRequestedUpdatedBefore() {
    return requestedUpdatedBefore;
  }

  public void setRequestedUpdatedBefore(LocalDate requestedUpdatedBefore) {
    this.requestedUpdatedBefore = requestedUpdatedBefore;
  }

  public LocalDate getRequestedUpdatedAfter() {
    return requestedUpdatedAfter;
  }

  public void setRequestedUpdatedAfter(LocalDate requestedUpdatedAfter) {
    this.requestedUpdatedAfter = requestedUpdatedAfter;
  }

  /**
   * @return the external components
   */
  public Set<ExternalComponent> getExtComponents() {
    return extComponents;
  }

  /**
   * add new external component to the list
   *
   * @param server the external server name
   * @param component the external component
   * @param path the external path
   * @param url the external url
   */
  public void addExternalComponents(String server, String component, String path, String url) {

    // add all needed information
    extComponents.add(new ExternalComponent(server, component, path, url));
  }

  public void setRequestedFolder(String requestedFolder) {
    this.requestedFolder = requestedFolder;
  }

  public String getRequestedFolder() {
    return requestedFolder;
  }

  public String getTaxonomyPosition() {
    return taxonomyPosition;
  }

  public void setTaxonomyPosition(final String taxonomyPosition) {
    this.taxonomyPosition = taxonomyPosition;
  }

  public boolean isTaxonomyUsed() {
    return StringUtil.isDefined(taxonomyPosition);
  }

  public boolean isAdminScope() {
    return adminScope;
  }

  public void setAdminScope(final boolean adminScope) {
    this.adminScope = adminScope;
  }
}