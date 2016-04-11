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
package org.silverpeas.core.index.search.model;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.ExternalComponent;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.indexing.model.IndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.index.indexing.model.IndexReadersCache;
import org.silverpeas.core.index.indexing.model.SpaceComponentPair;
import org.silverpeas.core.index.search.SearchEnginePropertiesManager;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * The IndexSearcher class implements search over all the indexes. A IndexSearcher
 * manages a set of cached lucene IndexSearcher.
 */
public class IndexSearcher {

  public static QueryParser.Operator defaultOperand = QueryParser.AND_OPERATOR;

  /**
   * The primary factor used with the secondary one to give a better score to entries whose title or
   * abstract match the query.
   */
  private int primaryFactor = 3;
  /**
   * The secondary factor used with the first one to give a better score to entries whose title or
   * abstract match the query.
   */
  private int secondaryFactor = 1;
  @Inject
  private IndexManager indexManager;

  /**
   * indicates the number maximum of results returned by the search
   */
  public static int maxNumberResult = 0;

  @PostConstruct
  private void init() {
    try {
      SettingBundle settings =
          ResourceLocator.getSettingBundle("org.silverpeas.index.search.searchEngineSettings");
      int paramOperand = settings.getInteger("defaultOperand", 0);
      if (paramOperand == 0) {
        defaultOperand = QueryParser.OR_OPERATOR;
      } else {
        defaultOperand = QueryParser.AND_OPERATOR;
      }

      maxNumberResult = settings.getInteger("maxResults", 100);
    } catch (MissingResourceException e) {
      SilverLogger.getLogger(this)
          .error("Error while loading the settings from searchEngineSettings.properties", e);
    }
  }

  /**
   * The no parameters constructor retrieves all the needed data from the IndexEngine.properties
   * file.
   */
  private IndexSearcher() {
    indexManager = IndexManager.get();
    primaryFactor = getFactorFromProperties("PrimaryFactor", primaryFactor);
    secondaryFactor = getFactorFromProperties("SecondaryFactor", secondaryFactor);
  }


  /**
   * Get the primary factor from the IndexEngine.properties file.
   *
   * @param propertyName
   * @param defaultValue
   * @return
   */
  public static int getFactorFromProperties(String propertyName, int defaultValue) {
    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.index.indexing.IndexEngine");
    return settings.getInteger(propertyName, defaultValue);
  }

  /**
   * Searches the Lucene index for a specific object, by giving the PK of the index entry
   *
   * @param component
   * @param objectId
   * @param objectType
   * @return MatchingIndexEntry wrapping the result, else null
   */
  public MatchingIndexEntry search(String component, String objectId, String objectType) {
    SpaceComponentPair pair = new SpaceComponentPair(null, component);
    Set<SpaceComponentPair> set = new HashSet<>(1);
    set.add(pair);

    IndexEntryKey indexEntryKey = new IndexEntryKey(component, objectType, objectId);
    MatchingIndexEntry matchingIndexEntry = null;
    org.apache.lucene.search.IndexSearcher searcher = getSearcher(set);
    try {
      TopDocs topDocs;
      Term term = new Term(IndexManager.KEY, indexEntryKey.toString());

      TermQuery query = new TermQuery(term);
      topDocs = searcher.search(query, maxNumberResult);
      ScoreDoc scoreDoc = topDocs.scoreDocs[0];

      matchingIndexEntry = createMatchingIndexEntry(scoreDoc, "*", searcher);
    } catch (IOException ioe) {
      SilverLogger.getLogger(this).error("Index file corrupted", ioe);
    } finally {
      IOUtils.closeQuietly(searcher);
    }
    return matchingIndexEntry;
  }

  /**
   * Search the documents of the given component's set. All entries found whose startDate is not
   * reached or whose endDate is passed are pruned from the results set.
   *
   * @param query the
   * @return
   * @throws org.silverpeas.core.index.search.model.ParseException
   */
  public MatchingIndexEntry[] search(QueryDescription query)
      throws org.silverpeas.core.index.search.model.ParseException {
    List<MatchingIndexEntry> results;

    org.apache.lucene.search.IndexSearcher searcher = getSearcher(query);

    try {
      TopDocs topDocs;
      BooleanQuery booleanQuery = new BooleanQuery();
      BooleanQuery rangeClauses = new BooleanQuery();
      rangeClauses.add(getVisibilityStartQuery(), BooleanClause.Occur.MUST);
      rangeClauses.add(getVisibilityEndQuery(), BooleanClause.Occur.MUST);

      if (query.getXmlQuery() != null) {
        booleanQuery.add(getXMLQuery(query), BooleanClause.Occur.MUST);
      } else {
        if (query.getMultiFieldQuery() != null) {
          booleanQuery.add(getMultiFieldQuery(query), BooleanClause.Occur.MUST);
        } else {
          TermRangeQuery rangeQuery = getRangeQueryOnCreationDate(query);
          if (!StringUtil.isDefined(query.getQuery()) && (query.isSearchBySpace() || query.
              isSearchByComponentType()) && !query.isPeriodDefined()) {
            rangeQuery = new TermRangeQuery(IndexManager.CREATIONDATE, "1900/01/01", "2200/01/01",
                true, true);
          }
          if (rangeQuery != null) {
            rangeClauses.add(rangeQuery, BooleanClause.Occur.MUST);
          }
          TermRangeQuery rangeQueryOnLastUpdateDate = getRangeQueryOnLastUpdateDate(query);
          if (rangeQueryOnLastUpdateDate != null) {
            rangeClauses.add(rangeQueryOnLastUpdateDate, BooleanClause.Occur.MUST);
          }
          TermQuery termQueryOnAuthor = getTermQueryOnAuthor(query);
          if (termQueryOnAuthor != null) {
            booleanQuery.add(termQueryOnAuthor, BooleanClause.Occur.MUST);
          }
          PrefixQuery termQueryOnFolder = getTermQueryOnFolder(query);
          if (termQueryOnFolder != null) {
            booleanQuery.add(termQueryOnFolder, BooleanClause.Occur.MUST);
          }

          try {
            Query plainTextQuery = getPlainTextQuery(query, IndexManager.CONTENT);
            if (plainTextQuery != null) {
              booleanQuery.add(plainTextQuery, BooleanClause.Occur.MUST);
            }
          } catch (ParseException e) {
            throw new org.silverpeas.core.index.search.model.ParseException("IndexSearcher", e);
          }
        }
      }


      // date range clauses are passed in the filter to optimize search performances
      // but the query cannot be empty : if so, then pass date range in the query
      if (booleanQuery.getClauses().length == 0) {
        topDocs = searcher.search(rangeClauses, null, maxNumberResult);
      } else {
        QueryWrapperFilter wrappedFilter = new QueryWrapperFilter(rangeClauses);
        topDocs = searcher.search(booleanQuery, wrappedFilter, maxNumberResult);
      }

      results = makeList(topDocs, query, searcher);
    } catch (IOException ioe) {
      SilverLogger.getLogger(this).error("Index file corrupted", ioe);
      results = new ArrayList<>();
    }
    return results.toArray(new MatchingIndexEntry[results.size()]);
  }

  private TermRangeQuery getVisibilityStartQuery() {
    return new TermRangeQuery(IndexManager.STARTDATE, IndexEntry.STARTDATE_DEFAULT, DateUtil.
        today2SQLDate(), true, true);
  }

  private TermRangeQuery getVisibilityEndQuery() {
    return new TermRangeQuery(IndexManager.ENDDATE, DateUtil.today2SQLDate(),
        IndexEntry.ENDDATE_DEFAULT, true, true);
  }

  private Query getPlainTextQuery(QueryDescription query, String searchField) throws ParseException {
    if (!StringUtil.isDefined(query.getQuery())) {
      return null;
    }

    String language = query.getRequestedLanguage();
    Analyzer analyzer = indexManager.getAnalyzer(language);

    Query parsedQuery;
    if (I18NHelper.isI18nContentActivated && "*".equals(language)) {
      // search over all languages
      String[] fields = new String[I18NHelper.getNumberOfLanguages()];

      int l = 0;
      Set<String> languages = I18NHelper.getAllSupportedLanguages();
      for (String lang : languages) {
        fields[l] = getFieldName(searchField, lang);
        l++;
      }

      MultiFieldQueryParser mfqp = new MultiFieldQueryParser(Version.LUCENE_36, fields, analyzer);
      mfqp.setDefaultOperator(defaultOperand);
      parsedQuery = mfqp.parse(query.getQuery());
    } else {
      // search only specified language
      if (I18NHelper.isI18nContentActivated && !"*".equals(language) && !I18NHelper.isDefaultLanguage(language)) {
        searchField = getFieldName(searchField, language);
      }

      QueryParser queryParser = new QueryParser(Version.LUCENE_36, searchField, analyzer);
      queryParser.setDefaultOperator(defaultOperand);
      parsedQuery = queryParser.parse(query.getQuery());
    }

    return parsedQuery;
  }

  private Query getXMLQuery(QueryDescription query)
      throws org.silverpeas.core.index.search.model.ParseException {
    try {
      Set<String> languages = I18NHelper.getAllSupportedLanguages();
      Analyzer analyzer = indexManager.getAnalyzer(query.getRequestedLanguage());
      BooleanQuery booleanQuery = new BooleanQuery();

      String xmlTitle = query.getXmlTitle();
      if (StringUtil.isDefined(xmlTitle)) {
        Query headerQuery = getQuery(IndexManager.HEADER, xmlTitle, languages, analyzer);
        booleanQuery.add(headerQuery, BooleanClause.Occur.MUST);
      }

      Map<String, String> xmlQuery = query.getXmlQuery();
      for (String fieldName : xmlQuery.keySet()) {
        Query fieldI18NQuery =
            getQuery(fieldName, xmlQuery.get(fieldName), languages, analyzer);
        booleanQuery.add(fieldI18NQuery, BooleanClause.Occur.MUST);
      }


      return booleanQuery;
    } catch (org.apache.lucene.queryParser.ParseException e) {
      throw new org.silverpeas.core.index.search.model.ParseException("IndexSearcher", e);
    }
  }

  private Query getMultiFieldQuery(QueryDescription query)
      throws org.silverpeas.core.index.search.model.ParseException {
    try {
      Set<String> languages = I18NHelper.getAllSupportedLanguages();
      Analyzer analyzer = indexManager.getAnalyzer(query.getRequestedLanguage());
      BooleanQuery booleanQuery = new BooleanQuery();

      String keyword = query.getQuery();
      if (StringUtil.isDefined(keyword)) {
        Query headerQuery = getQuery(IndexManager.HEADER, keyword, languages, analyzer);
        booleanQuery.add(headerQuery, BooleanClause.Occur.MUST);
      }

      List<FieldDescription> fieldQueries = query.getMultiFieldQuery();
      for (FieldDescription fieldQuery : fieldQueries) {
        Query fieldI18NQuery =
            getQuery(fieldQuery.getFieldName(), fieldQuery.getContent(), languages, analyzer);
        booleanQuery.add(fieldI18NQuery, BooleanClause.Occur.MUST);
      }


      return booleanQuery;
    } catch (ParseException e) {
      throw new org.silverpeas.core.index.search.model.ParseException("IndexSearcher", e);
    }
  }

  /**
   * Generates MultiFieldQuery about one field and all languages.
   * This generated the following query : (fieldName:queryStr fieldName_en:queryStr fieldName_de:queryStr)
   * @return a Query limited to given fieldName
   * @throws ParseException
   */
  private Query getQuery(String fieldName, String queryStr, Set<String> languages, Analyzer analyzer)
      throws ParseException {
    Map<String, BooleanClause.Occur> fieldNames = new HashMap<>();
    for (String language : languages) {
      fieldNames.put(getFieldName(fieldName, language), BooleanClause.Occur.SHOULD);
    }
    Query query =
        MultiFieldQueryParser.parse(Version.LUCENE_36, queryStr,
            fieldNames.keySet().toArray(new String[fieldNames.size()]), fieldNames.values()
                .toArray(new BooleanClause.Occur[fieldNames.size()]), analyzer);
    return query;
  }

  private String getFieldName(String name, String language) {
    if (!I18NHelper.isI18nContentActivated || I18NHelper.isDefaultLanguage(language)) {
      return name;
    }
    return name + "_" + language;
  }

  /**
   * @param scoreDoc occurence of Lucene search result
   * @param requestedLanguage
   * @param searcher
   * @return MatchingIndexEntry wraps the Lucene search result
   * @throws IOException if there is a problem when searching Lucene index
   */
  private MatchingIndexEntry createMatchingIndexEntry(ScoreDoc scoreDoc, String requestedLanguage,
      org.apache.lucene.search.IndexSearcher searcher) throws IOException {
    Document doc = searcher.doc(scoreDoc.doc);
    MatchingIndexEntry indexEntry =
        new MatchingIndexEntry(IndexEntryKey.create(doc.get(IndexManager.KEY)));

    Iterator<String> languages = I18NHelper.getLanguages();
    while (languages.hasNext()) {
      String language = languages.next();
      indexEntry.setTitle(doc.get(getFieldName(IndexManager.TITLE, language)), language);
      indexEntry.setPreview(doc.get(getFieldName(IndexManager.PREVIEW, language)), language);
    }

    indexEntry.setKeyWords(doc.get(IndexManager.KEYWORDS));
    indexEntry.setCreationUser(doc.get(IndexManager.CREATIONUSER));
    indexEntry.setCreationDate(doc.get(IndexManager.CREATIONDATE));
    indexEntry.setLastModificationUser(doc.get(IndexManager.LASTUPDATEUSER));
    indexEntry.setLastModificationDate(doc.get(IndexManager.LASTUPDATEDATE));
    indexEntry.setThumbnail(doc.get(IndexManager.THUMBNAIL));
    indexEntry.setThumbnailMimeType(doc.get(IndexManager.THUMBNAIL_MIMETYPE));
    indexEntry.setThumbnailDirectory(doc.get(IndexManager.THUMBNAIL_DIRECTORY));
    indexEntry.setStartDate(doc.get(IndexManager.STARTDATE));
    indexEntry.setEndDate(doc.get(IndexManager.ENDDATE));
    indexEntry.setEmbeddedFileIds(doc.getValues(IndexManager.EMBEDDED_FILE_IDS));
    indexEntry.setFilename(doc.get(IndexManager.FILENAME));
    indexEntry.setAlias(StringUtil.getBooleanValue(doc.get(IndexManager.ALIAS)));
    indexEntry.setScore(scoreDoc.score); // TODO check the score.
    // Checks the content to see if it contains sortable field
    // and puts them in MatchingIndexEntry object
    if ("Publication".equals(indexEntry.getObjectType())) {
      HashMap<String, String> sortableField = new HashMap<>();
      String fieldValue;

      for (String formXMLFieldName : SearchEnginePropertiesManager.getFieldsNameList()) {
        if ("*".equals(requestedLanguage) || I18NHelper.isDefaultLanguage(requestedLanguage)) {
          fieldValue = doc.get(formXMLFieldName);
        } else {
          fieldValue = doc.get(formXMLFieldName + "_" + requestedLanguage);
        }
        if (fieldValue != null) {
          sortableField.put(formXMLFieldName, fieldValue);
        }
      }
      indexEntry.setSortableXMLFormFields(sortableField);
    }
    // adds fields and values used to generate facets
    String fieldsForFacets = doc.get(IndexManager.FIELDS_FOR_FACETS);
    if (StringUtil.isDefined(fieldsForFacets)) {
      Map<String, String> fieldsValueForFacets = new HashMap<>();
      StringTokenizer tokenizer = new StringTokenizer(fieldsForFacets, ",");
      while (tokenizer.hasMoreTokens()) {
        String fieldName = tokenizer.nextToken();
        fieldsValueForFacets.put(fieldName, doc.get(fieldName));
      }
      indexEntry.setXMLFormFieldsForFacets(fieldsValueForFacets);
    }

    // Set server name
    indexEntry.setServerName(doc.get(IndexManager.SERVER_NAME));
    return indexEntry;
  }

  /**
   * Makes a List of MatchingIndexEntry from a lucene hits. All entries found whose startDate is not
   * reached or whose endDate is passed are pruned from the results list.
   */
  private List<MatchingIndexEntry> makeList(TopDocs topDocs, QueryDescription query,
      org.apache.lucene.search.IndexSearcher searcher) throws IOException {
    List<MatchingIndexEntry> results = new ArrayList<>();

    if (topDocs != null) {
      ScoreDoc scoreDoc;

      for (int i = 0; i < topDocs.scoreDocs.length; i++) {
        scoreDoc = topDocs.scoreDocs[i];
        MatchingIndexEntry indexEntry = createMatchingIndexEntry(scoreDoc, query
            .getRequestedLanguage(), searcher);
        results.add(indexEntry);
      }
    }
    return results;
  }

  /**
   * Return a multi-searcher built on the searchers list matching the (space, component) pair set.
   */
  private org.apache.lucene.search.IndexSearcher getSearcher(
      Set<SpaceComponentPair> spaceComponentPairSet) {
    Set<String> indexPathSet = getIndexPathSet(spaceComponentPairSet);

    List<IndexReader> readers = new ArrayList<>();
    for (String path : indexPathSet) {
      IndexReader indexReader = getIndexReader(path);
      if (indexReader != null) {
        readers.add(indexReader);
      }
    }
    return new org.apache.lucene.search.IndexSearcher(
        new MultiReader(readers.toArray(new IndexReader[readers.size()])));
  }

  /**
   * Return a multi-searcher built on the searchers list matching the (space, component) pair set.
   */
  private org.apache.lucene.search.IndexSearcher getSearcher(QueryDescription query) {
    Set<String> indexPathSet = getIndexPathSet(query.getSpaceComponentPairSet());
    List<IndexReader> readers = new ArrayList<>();
    for (String path : indexPathSet) {
      IndexReader indexReader = getIndexReader(path);
      if (indexReader != null) {
        readers.add(indexReader);
      }
    }

    // Add searcher from external silverpeas server
    Set<ExternalComponent> extSearchers = query.getExtComponents();
    for (ExternalComponent externalComponent : extSearchers) {
      String externalComponentPath = getExternalComponentPath(externalComponent);
      IndexReader searcher = getIndexReader(externalComponentPath);
      if (searcher != null) {
        readers.add(searcher);
      }
    }
    return new org.apache.lucene.search.IndexSearcher(
        new MultiReader(readers.toArray(new IndexReader[readers.size()])));
  }

  private String getExternalComponentPath(ExternalComponent extComp) {
    return extComp.getDataPath() + File.separator + "index" + File.separator +
        extComp.getComponent() + File.separator + "index";
  }

  /**
   * Build the set of all the path to the directories index corresponding the given (space,
   * component) pairs.
   */
  public Set<String> getIndexPathSet(Set<SpaceComponentPair> spaceComponentPairSet) {
    Set<String> pathSet = new HashSet<>();

    for (SpaceComponentPair pair : spaceComponentPairSet) {

      // Both cases.
      // 1 - space == null && component != null : search in an component's
      // instance
      // 2 - space != null && component != null : search in pdc or user's
      // components (todo, agenda...)

      if (pair != null && pair.getComponent() != null) {
        pathSet.add(indexManager.getIndexDirectoryPath(pair.getSpace(), pair.getComponent()));
      }
    }
    return pathSet;
  }

  /**
   * Retrieve the index searcher over the specified index directory. The index readers are cached in
   * a Map (path -> (timestamp, reader)). If a reader is found in the cache but appear to be too old
   * (according to the timestamp) then it is re-open. If the index files are not found, null is
   * returned without any error (as this case comes each time a request is made on a space/component
   * without any indexed documents).
   */
  private IndexReader getIndexReader(String path) {
    return IndexReadersCache.getIndexReader(path);
  }

  protected TermRangeQuery getRangeQueryOnCreationDate(QueryDescription query) {
    String beginDate = query.getRequestedCreatedAfter();
    String endDate = query.getRequestedCreatedBefore();
    if (!StringUtil.isDefined(beginDate) && !StringUtil.isDefined(endDate)) {
      return null;
    }
    if (!StringUtil.isDefined(beginDate)) {
      beginDate = IndexEntry.STARTDATE_DEFAULT;
    }
    if (!StringUtil.isDefined(endDate)) {
      endDate = IndexEntry.ENDDATE_DEFAULT;
    }
    return new TermRangeQuery(IndexManager.CREATIONDATE, beginDate, endDate, true, true);
  }

  protected TermRangeQuery getRangeQueryOnLastUpdateDate(QueryDescription query) {
    String beginDate = query.getRequestedUpdatedAfter();
    String endDate = query.getRequestedUpdatedBefore();
    if (!StringUtil.isDefined(beginDate) && !StringUtil.isDefined(endDate)) {
      return null;
    }

    if (!StringUtil.isDefined(beginDate)) {
      beginDate = IndexEntry.STARTDATE_DEFAULT;
    }
    if (!StringUtil.isDefined(endDate)) {
      endDate = IndexEntry.ENDDATE_DEFAULT;
    }
    return new TermRangeQuery(IndexManager.LASTUPDATEDATE, beginDate, endDate, true, true);
  }

  protected TermQuery getTermQueryOnAuthor(QueryDescription query) {
    if (!StringUtil.isDefined(query.getRequestedAuthor())) {
      return null;
    }
    Term authorTerm = new Term(IndexManager.CREATIONUSER, query.getRequestedAuthor());
    return new TermQuery(authorTerm);
  }

  protected PrefixQuery getTermQueryOnFolder(QueryDescription query) {
    if (!StringUtil.isDefined(query.getRequestedFolder())) {
      return null;
    }
    Term term = new Term(IndexManager.PATH, query.getRequestedFolder());
    return new PrefixQuery(term);
  }
}