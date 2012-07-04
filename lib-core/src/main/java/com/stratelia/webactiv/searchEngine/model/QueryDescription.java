/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.searchEngine.model;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.indexEngine.model.CharReplacer;
import com.stratelia.webactiv.util.indexEngine.model.ExternalComponent;
import com.stratelia.webactiv.util.indexEngine.model.FieldDescription;
import com.stratelia.webactiv.util.indexEngine.model.SpaceComponentPair;
import java.io.Serializable;
import java.util.*;

/**
 * A QueryDescription packs a query with the different spaces and components to be searched.
 */
public final class QueryDescription implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The searched components' instance is built empty. To be searched any space or component must be
   * explicitly added with the addSpaceComponent() method. This Set is a set of SpaceComponentPair.
   */
  private Set<SpaceComponentPair> spaceComponentPairSet = new HashSet<SpaceComponentPair>();

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
  private String requestedCreatedBefore = null;
  private String requestedCreatedAfter = null;
  private String requestedUpdatedBefore = null;
  private String requestedUpdatedAfter = null;
  private Map<String, String> xmlQuery = null;
  private String xmlTitle = null;
  private List<FieldDescription> multiFieldQuery = null;
  private boolean searchBySpace = false;
  private boolean searchByComponentType = false;

  /**
   * The external searched components are build empty. This is a set of ExternalComponent
   */
  private Set<ExternalComponent> extComponents = new HashSet<ExternalComponent>();

  /**
   * The no parameters constructor builds an empty query. The setQuery and addSpaceComponentPair()
   * methods should be called to initialize the query. Other criterium (language, creation date ...)
   * can be set before the request is sent to the searchEngine.
   */
  public QueryDescription() {
  }

  /**
   * The constructor set only the query string. The addSpaceComponentPair() method should be called
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

    CharReplacer charReplacer;

    this.query = (query == null) ? "" : query.toLowerCase();

    // string already in lower case, means "and" "And" "AND" will works.
    this.query = findAndReplace(this.query, " and ", " AND ");
    this.query = findAndReplace(this.query, " or ", " OR ");
    this.query = findAndReplace(this.query, " not ", " NOT ");
    if (this.query.indexOf("not ") == 0) {
      this.query = "NOT " + this.query.substring(4, this.query.length());
    }

    // here substitute the special char sur as: é, è or ê to e
    charReplacer = getCharReplacer();
    this.query = charReplacer.replace(this.query);

    SilverTrace.info("searchEngine", "QueryDescription.setQuery()",
        "root.MSG_GEN_PARAM_VALUE", "Query String to Lucence= " + this.query);

  }

  /**
   * Return the query string.
   */
  public String getQuery() {
    return query;
  }

  /**
   * Set the user
   */
  public void setSearchingUser(String searchingUser) {
    this.searchingUser = (searchingUser == null) ? "" : searchingUser
        .toLowerCase();
  }

  /**
   * return the user
   */
  public String getSearchingUser() {
    return searchingUser;
  }

  /**
   * Add the given instance of the component to the searched instances set.
   */
  public void addSpaceComponentPair(String space, String component) {
    SilverTrace.info("searchEngine",
        "QueryDescription.addSpaceComponentPair()", "root.MSG_GEN_PARAM_VALUE",
        "space = " + space + ", component=" + component);
    spaceComponentPairSet.add(new SpaceComponentPair(space, component));
  }

  public void addComponent(String component) {
    SilverTrace.info("searchEngine", "QueryDescription.addComponent()",
        "root.MSG_GEN_PARAM_VALUE", "component=" + component);
    spaceComponentPairSet.add(new SpaceComponentPair(null, component));
  }

  /**
   * Return the set of all the component's instances whose the documents must be searched. The
   * return Set is a set of SpaceComponentPair.
   */
  public Set<SpaceComponentPair> getSpaceComponentPairSet() {
    return spaceComponentPairSet;
  }

  /**
   * Set the requested language.
   */
  public void setRequestedLanguage(String requestedLang) {
    this.requestedLang = requestedLang;
  }

  /**
   * Returns the requested language.
   */
  public String getRequestedLanguage() {
    if (requestedLang == null) {
      return Locale.getDefault().getLanguage();
    } else
      return requestedLang;
  }

  /**
   * Set the requested author.
   */
  public void setRequestedAuthor(String author) {
    this.requestedAuthor = (author == null) ? null : author.toLowerCase();
  }

  /**
   * Return the requested author.
   */
  public String getRequestedAuthor() {
    return requestedAuthor;
  }

  /**
   * Set the before date
   */
  public void setRequestedCreatedBefore(String beforedate) {
    this.requestedCreatedBefore = beforedate;
  }

  /**
   * get the before date
   */
  public String getRequestedCreatedBefore() {
    return requestedCreatedBefore;
  }

  /**
   * Set the after date
   */
  public void setRequestedCreatedAfter(String afterdate) {
    this.requestedCreatedAfter = afterdate;
  }

  /**
   * get the after date
   */
  public String getRequestedCreatedAfter() {
    return requestedCreatedAfter;
  }

  public void setXmlQuery(Map<String, String> xmlQuery) {
    this.xmlQuery = xmlQuery;
  }

  public Map<String, String> getXmlQuery() {
    return xmlQuery;
  }

  public String getXmlTitle() {
    return xmlTitle;
  }

  public void setXmlTitle(String xmlTitle) {
    this.xmlTitle = xmlTitle;
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
      multiFieldQuery = new ArrayList<FieldDescription>();
    }

    multiFieldQuery.add(fieldQuery);
  }

  public void addFieldQueries(List<FieldDescription> fieldQueries) {
    if (multiFieldQuery == null) {
      multiFieldQuery = new ArrayList<FieldDescription>();
    }

    multiFieldQuery.addAll(fieldQueries);
  }

  public void setFieldQueries(List<FieldDescription> fieldQueries) {
    clearMultiFieldQuery();
    addFieldQueries(fieldQueries);
  }

  public boolean isEmpty() {
    return !StringUtil.isDefined(query) && getMultiFieldQuery() == null
        && getXmlQuery() == null && !StringUtil.isDefined(xmlTitle);
  }

  /**
   * Find and replace used for the AND OR NOT lucene's keyword in the search engine query.
   * 26/01/2004
   */
  private String findAndReplace(String source, String find, String replace) {
    int index = source.indexOf(find);

    while (index > -1) {
      source = source.substring(0, index) + replace
          + source.substring(index + find.length(), source.length());
      index = source.indexOf(find);
    }

    return source;
  }

  /**
   * Function to get the substitute char for the current language. (Same function used for the
   * Indexer) PHiL 29/01/2004
   */

  private CharReplacer getCharReplacer() {
    CharReplacer replacer = new CharReplacer();
    int replacementCount = 0;
    // String language = new String(getRequestedLanguage());
    try {
      ResourceLocator resource = new ResourceLocator(
          "com.stratelia.webactiv.util.indexEngine.SpecialChars", "");

      Enumeration<String> replacements = resource.getKeys();

      while (replacements.hasMoreElements()) {
        String oldChars = replacements.nextElement();
        String newChars = resource.getString(oldChars);

        replacer.setReplacement(oldChars, newChars);
        replacementCount++;
      }
    } catch (MissingResourceException e) {
      SilverTrace.warn("indexEngine", "getCharReplacer",
          "indexEngine.MSG_MISSING_SPECIALCHARS_DEFINITION");
    }

    if (replacementCount == 0) {
      return null;
    } else {
      return replacer;
    }
  }

  public boolean isPeriodDefined() {
    return StringUtil.isDefined(requestedCreatedAfter) ||
        StringUtil.isDefined(requestedCreatedBefore) ||
        StringUtil.isDefined(requestedUpdatedAfter) ||
        StringUtil.isDefined(requestedUpdatedBefore);
  }

  /**
   * @return the searchBySpace
   */
  public boolean isSearchBySpace() {
    return searchBySpace;
  }

  /**
   * @param searchBySpace the searchBySpace to set
   */
  public void setSearchBySpace(boolean isSearchBySpace) {
    this.searchBySpace = isSearchBySpace;
  }

  public String getRequestedUpdatedBefore() {
    return requestedUpdatedBefore;
  }

  public void setRequestedUpdatedBefore(String requestedUpdatedBefore) {
    this.requestedUpdatedBefore = requestedUpdatedBefore;
  }

  public String getRequestedUpdatedAfter() {
    return requestedUpdatedAfter;
  }

  public void setRequestedUpdatedAfter(String requestedUpdatedAfter) {
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
   * @param server
   * @param component
   */
  public void addExternalComponents(String server, String component, String path, String url) {
    SilverTrace.info("searchEngine",
        "QueryDescription.addExternalComponents()", "root.MSG_GEN_PARAM_VALUE",
        "server = " + server + ", component=" + component);
    // add all needed information
    extComponents.add(new ExternalComponent(server, component, path, url));
  }

  public boolean isSearchByComponentType() {
    return searchByComponentType;
  }

  public void setSearchByComponentType(boolean searchByComponentType) {
    this.searchByComponentType = searchByComponentType;
  }

}