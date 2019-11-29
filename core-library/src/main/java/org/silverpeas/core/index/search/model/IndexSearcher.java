/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.IndexFileManager;
import org.silverpeas.core.index.indexing.model.ExternalComponent;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.indexing.model.IndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.index.search.SearchEnginePropertiesManager;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;
import static org.silverpeas.core.index.indexing.model.IndexProcessor.doSearch;
import static org.silverpeas.core.index.indexing.model.IndexReadersCache.getIndexReader;
import static org.silverpeas.core.util.StringUtil.getBooleanValue;

/**
 * The IndexSearcher class implements search over all the indexes. A IndexSearcher
 * manages a set of cached lucene IndexSearcher.
 */
@Singleton
public class IndexSearcher {

  private static final String INDEX_SEARCH_ERROR = "Index search failure";
  private static final int DEFAULT_MAX_RESULT = 100;
  private static final int DEFAULT_FIELDHEADER_BOOST = 3;

  private QueryParser.Operator defaultOperator;

  /**
   * The primary factor used with the secondary one to give a better score to entries whose title or
   * abstract match the query.
   */
  private int fieldHeaderBoost;

  @Inject
  private IndexManager indexManager;

  /**
   * indicates the number maximum of results returned by the search
   */
  private int maxNumberResult;

  /**
   * The no parameters constructor retrieves all the needed data from the IndexEngine.properties
   * file.
   */
  private IndexSearcher() {
    indexManager = IndexManager.get();
  }

  public static IndexSearcher get() {
    return ServiceProvider.getSingleton(IndexSearcher.class);
  }

  public QueryParser.Operator getDefaultOperator() {
    return defaultOperator;
  }

  @PostConstruct
  private void init() {
    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.index.search.searchEngineSettings");
    int paramOperand = settings.getInteger("defaultOperand", 0);
    if (paramOperand == 0) {
      defaultOperator = QueryParser.OR_OPERATOR;
    } else {
      defaultOperator = QueryParser.AND_OPERATOR;
    }

    maxNumberResult = settings.getInteger("maxResults", DEFAULT_MAX_RESULT);

    fieldHeaderBoost = settings.getInteger("boost.field.header", DEFAULT_FIELDHEADER_BOOST);
  }

  /**
   * Searches the Lucene index for a specific object, by giving the PK of the index entry
   *
   * @param component
   * @param objectId
   * @param objectType
   * @return MatchingIndexEntry wrapping the result, else null
   */
  public MatchingIndexEntry search(String component, String objectId, String objectType)
      throws ParseException {
    return doSearch(() -> {
      final Set<String> set = Collections.singleton(component);
      IndexEntryKey indexEntryKey = new IndexEntryKey(component, objectType, objectId);
      MatchingIndexEntry matchingIndexEntry = null;
      org.apache.lucene.search.IndexSearcher searcher = getSearcher(set);
      try {
        Term term = new Term(IndexManager.KEY, indexEntryKey.toString());
        TermQuery query = new TermQuery(term);
        TopDocs topDocs = searcher.search(query, maxNumberResult);
        ScoreDoc scoreDoc = topDocs.scoreDocs[0];
        matchingIndexEntry = createMatchingIndexEntry(scoreDoc, "*", searcher);
      } catch (IOException ioe) {
        SilverLogger.getLogger(this).error("Index file corrupted", ioe);
      }
      return matchingIndexEntry;
    }, () -> null);
  }

  /**
   * Search the documents of the given component's set. All entries found whose startDate is not
   * reached or whose endDate is passed are pruned from the results set.
   * @param query the query.
   * @return an array of index entries.
   * @throws org.silverpeas.core.index.search.model.ParseException on parse error
   */
  public MatchingIndexEntry[] search(QueryDescription query)
      throws org.silverpeas.core.index.search.model.ParseException {
    return doSearch(() -> {
      final long startTime = System.currentTimeMillis();
      List<MatchingIndexEntry> results;
      org.apache.lucene.search.IndexSearcher searcher = getSearcher(query);
      try {
        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        BooleanQuery.Builder rangeClausesBuilder = new BooleanQuery.Builder();
        rangeClausesBuilder.add(getVisibilityStartQuery(), BooleanClause.Occur.MUST);
        rangeClausesBuilder.add(getVisibilityEndQuery(), BooleanClause.Occur.MUST);
        // filtering on searched scopes
        booleanQueryBuilder.add(getScopeQuery(query), BooleanClause.Occur.FILTER);
        parseQuery(query, booleanQueryBuilder, rangeClausesBuilder);
        // date range clauses are passed in the filter to optimize search performances
        // but the query cannot be empty : if so, then pass date range in the query
        BooleanQuery booleanQuery = booleanQueryBuilder.build();
        BooleanQuery rangeClauses = rangeClausesBuilder.build();
        TopDocs topDocs;
        if (booleanQuery.clauses().isEmpty()) {
          topDocs = searcher.search(rangeClauses, maxNumberResult);
          SilverLogger.getLogger(this).info(rangeClauses.toString());
        } else {
          booleanQueryBuilder.add(rangeClauses, BooleanClause.Occur.FILTER);
          booleanQuery = booleanQueryBuilder.build();
          SilverLogger.getLogger(this).info(booleanQuery.toString());
          topDocs = searcher.search(booleanQuery, maxNumberResult);
        }
        results = makeList(topDocs, query, searcher);
      } catch (IOException ioe) {
        SilverLogger.getLogger(this).error("Index file corrupted", ioe);
        results = new ArrayList<>();
      }
      final long endTime = System.currentTimeMillis();
      final int nbResults = results.size();
      SilverLogger.getLogger(this).debug(() -> MessageFormat
          .format(" search index duration in {0} with {1} matching entries",
              formatDurationHMS(endTime - startTime), nbResults));
      return results.toArray(new MatchingIndexEntry[0]);
    }, () -> new MatchingIndexEntry[0]);
  }

  private void parseQuery(final QueryDescription query,
      final BooleanQuery.Builder booleanQueryBuilder,
      final BooleanQuery.Builder rangeClausesBuilder) throws ParseException {
    if (query.getMultiFieldQuery() != null) {
      booleanQueryBuilder.add(getMultiFieldQuery(query), BooleanClause.Occur.MUST);
    } else {
      parseRangeQuery(query, booleanQueryBuilder, rangeClausesBuilder);
    }
  }

  private void parseRangeQuery(final QueryDescription query,
      final BooleanQuery.Builder booleanQueryBuilder,
      final BooleanQuery.Builder rangeClausesBuilder) throws ParseException {
    TermRangeQuery rangeQuery = getRangeQueryOnCreationDate(query);
    if (!StringUtil.isDefined(query.getQuery()) && (query.isSearchBySpace() || query.
        isSearchByComponentType()) && !query.isPeriodDefined()) {
      rangeQuery = TermRangeQuery
            .newStringRange(IndexManager.CREATIONDATE, "1900/01/01", "2200/01/01", true, true);
    }
    if (rangeQuery != null) {
      rangeClausesBuilder.add(rangeQuery, BooleanClause.Occur.MUST);
    }
    TermRangeQuery rangeQueryOnLastUpdateDate = getRangeQueryOnLastUpdateDate(query);
    if (rangeQueryOnLastUpdateDate != null) {
      rangeClausesBuilder.add(rangeQueryOnLastUpdateDate, BooleanClause.Occur.MUST);
    }
    TermQuery termQueryOnAuthor = getTermQueryOnAuthor(query);
    if (termQueryOnAuthor != null) {
      booleanQueryBuilder.add(termQueryOnAuthor, BooleanClause.Occur.MUST);
    }
    PrefixQuery termQueryOnFolder = getTermQueryOnFolder(query);
    if (termQueryOnFolder != null) {
      booleanQueryBuilder.add(termQueryOnFolder, BooleanClause.Occur.MUST);
    }

    try {
      Query plainTextQuery = getPlainTextQuery(query);
      if (plainTextQuery != null) {
        booleanQueryBuilder.add(plainTextQuery, BooleanClause.Occur.MUST);
      }
    } catch (ParseException e) {
      throw new ParseException(INDEX_SEARCH_ERROR, e);
    }
  }

  private TermRangeQuery getVisibilityStartQuery() {
    return TermRangeQuery.newStringRange(IndexManager.STARTDATE, IndexEntry.STARTDATE_DEFAULT,
        formatDate(LocalDate.now()), true, true);
  }

  private TermRangeQuery getVisibilityEndQuery() {
    return TermRangeQuery.newStringRange(IndexManager.ENDDATE, formatDate(LocalDate.now()),
        IndexEntry.ENDDATE_DEFAULT, true, true);
  }

  private Query getPlainTextQuery(QueryDescription query) throws ParseException {
    if (!StringUtil.isDefined(query.getQuery())) {
      return null;
    }

    String language = query.getRequestedLanguage();
    Analyzer analyzer = indexManager.getAnalyzer(language);

    Set<String> languages = Stream.of(language).collect(Collectors.toSet());
    if (I18NHelper.isI18nContentActivated && "*".equals(language)) {
      // search over all languages
      languages = I18NHelper.getAllSupportedLanguages();
    }

    Query queryOnContent = getQuery(IndexManager.CONTENT, query.getQuery(), languages, analyzer);
    Query queryOnHeader = getQuery(IndexManager.HEADER, query.getQuery(), languages, analyzer);

    BoostQuery boostQuery = new BoostQuery(queryOnHeader, fieldHeaderBoost);
    BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
    booleanQuery.add(boostQuery, BooleanClause.Occur.SHOULD);
    booleanQuery.add(queryOnContent, BooleanClause.Occur.SHOULD);
    booleanQuery.setMinimumNumberShouldMatch(1);
    return booleanQuery.build();
  }

  private Query getMultiFieldQuery(QueryDescription query)
      throws org.silverpeas.core.index.search.model.ParseException {
    try {
      Set<String> languages = I18NHelper.getAllSupportedLanguages();
      Analyzer analyzer = indexManager.getAnalyzer(query.getRequestedLanguage());
      BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

      Query plainTextQuery = getPlainTextQuery(query);
      if (plainTextQuery != null) {
        booleanQuery.add(plainTextQuery, BooleanClause.Occur.MUST);
      }

      List<FieldDescription> fieldQueries = query.getMultiFieldQuery();
      for (FieldDescription fieldQuery : fieldQueries) {
        if (fieldQuery.isBasedOnDate()) {
          TermRangeQuery rangeQuery = getTermRangeQuery(fieldQuery.getFieldName(),
              fieldQuery.getStartDate(), fieldQuery.getEndDate());
          if (rangeQuery != null) {
            booleanQuery.add(rangeQuery, BooleanClause.Occur.MUST);
          }
        } else {
          Query fieldI18NQuery =
              getQuery(fieldQuery.getFieldName(), fieldQuery.getContent(), languages, analyzer);
          booleanQuery.add(fieldI18NQuery, BooleanClause.Occur.MUST);
        }
      }

      return booleanQuery.build();
    } catch (ParseException e) {
      throw new org.silverpeas.core.index.search.model.ParseException(INDEX_SEARCH_ERROR, e);
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
    BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
    for (String language : languages) {
      QueryParser parser = new QueryParser(getFieldName(fieldName, language), analyzer);
      parser.setDefaultOperator(defaultOperator);
      Query query = parse(parser, queryStr);
      if (query != null) {
        booleanQueryBuilder.add(query, BooleanClause.Occur.SHOULD);
      }
    }
    return booleanQueryBuilder.build();
  }

  private Query parse(QueryParser parser, String toParse) throws ParseException {
    Query query;
    try {
      query = parser.parse(toParse);
    } catch (org.apache.lucene.queryparser.classic.ParseException e) {
      try {
        query = parser.parse(QueryParser.escape(toParse));
      } catch (org.apache.lucene.queryparser.classic.ParseException pe) {
        throw new org.silverpeas.core.index.search.model.ParseException(INDEX_SEARCH_ERROR, e);
      }
    }
    return query;
  }

  private String getFieldName(String name, String language) {
    if (!I18NHelper.isI18nContentActivated || I18NHelper.isDefaultLanguage(language)) {
      return name;
    }
    return name + "_" + language;
  }

  /**
   * @param scoreDoc occurrence of Lucene search result
   * @param requestedLanguage a language as short string
   * @param searcher the index search result instance
   * @return MatchingIndexEntry wraps the Lucene search result, null if the lucene result is not
   * expected according to possible components.
   * @throws IOException if there is a problem when searching Lucene index
   */
  private MatchingIndexEntry createMatchingIndexEntry(ScoreDoc scoreDoc, String requestedLanguage,
      org.apache.lucene.search.IndexSearcher searcher) throws IOException {
    final Document doc = searcher.doc(scoreDoc.doc);
    final MatchingIndexEntry indexEntry = new MatchingIndexEntry(
        IndexEntryKey.create(doc.get(IndexManager.KEY)));

    setIndexEntryLanguageData(indexEntry, doc);
    setIndexEntryCommonData(indexEntry, doc, scoreDoc);
    setIndexEntryPublicationData(indexEntry, doc, requestedLanguage);
    setIndexEntryFacetData(indexEntry, doc);

    // Set server name
    indexEntry.setServerName(doc.get(IndexManager.SERVER_NAME));
    return indexEntry;
  }

  private void setIndexEntryFacetData(final MatchingIndexEntry indexEntry, final Document doc) {
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
  }

  private void setIndexEntryPublicationData(final MatchingIndexEntry indexEntry, final Document doc,
      final String requestedLanguage) {
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
  }

  private void setIndexEntryCommonData(final MatchingIndexEntry indexEntry, final Document doc,
      final ScoreDoc scoreDoc) {
    indexEntry.setCreationUser(doc.get(IndexManager.CREATIONUSER));
    indexEntry.setCreationDate(parseDate(doc.get(IndexManager.CREATIONDATE)));
    indexEntry.setLastModificationUser(doc.get(IndexManager.LASTUPDATEUSER));
    indexEntry.setLastModificationDate(parseDate(doc.get(IndexManager.LASTUPDATEDATE)));
    indexEntry.setThumbnail(doc.get(IndexManager.THUMBNAIL));
    indexEntry.setThumbnailMimeType(doc.get(IndexManager.THUMBNAIL_MIMETYPE));
    indexEntry.setThumbnailDirectory(doc.get(IndexManager.THUMBNAIL_DIRECTORY));
    indexEntry.setStartDate(parseDate(doc.get(IndexManager.STARTDATE)));
    indexEntry.setEndDate(parseDate(doc.get(IndexManager.ENDDATE)));
    indexEntry.setEmbeddedFileIds(doc.getValues(IndexManager.EMBEDDED_FILE_IDS));
    indexEntry.setAlias(getBooleanValue(doc.get(IndexManager.ALIAS)));
    indexEntry.setScore(scoreDoc.score);
    final String[] paths = doc.getValues(IndexManager.PATH);
    if (paths != null && paths.length > 0) {
      indexEntry.setPaths(Stream.of(paths).collect(Collectors.toList()));
    }
  }

  private void setIndexEntryLanguageData(final MatchingIndexEntry indexEntry, final Document doc) {
    final Collection<String> languages = I18NHelper.getLanguages();
    for (final String language: languages) {
      indexEntry.setTitle(doc.get(getFieldName(IndexManager.TITLE, language)), language);
      indexEntry.setPreview(doc.get(getFieldName(IndexManager.PREVIEW, language)), language);
      indexEntry.setKeywords(doc.get(getFieldName(IndexManager.KEYWORDS, language)), language);
      String filename = doc.get(getFieldName(IndexManager.FILENAME, language));
      if (StringUtil.isDefined(filename)) {
        indexEntry.setFilename(filename);
      }
    }
  }

  /**
   * Makes a List of MatchingIndexEntry from a lucene hits. All entries found whose startDate is not
   * reached or whose endDate is passed are pruned from the results list.
   */
  private List<MatchingIndexEntry> makeList(TopDocs topDocs, QueryDescription query,
      org.apache.lucene.search.IndexSearcher searcher) throws IOException {
    List<MatchingIndexEntry> results = new ArrayList<>();

    if (topDocs != null) {
      for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
        results.add(createMatchingIndexEntry(scoreDoc, query.getRequestedLanguage(), searcher));
      }
    }
    return results;
  }

  /**
   * Return a multi-searcher built on the searchers list matching the (space, component) pair set.
   */
  private org.apache.lucene.search.IndexSearcher getSearcher(Set<String> componentIds)
      throws ParseException {
    Set<String> indexPathSet = getIndexPathSet(componentIds);

    List<IndexReader> readers = new ArrayList<>();
    for (String path : indexPathSet) {
      final IndexReader indexReader = getIndexReader(path);
      if (indexReader != null) {
        readers.add(indexReader);
      }
    }
    try {
      return new org.apache.lucene.search.IndexSearcher(
          new MultiReader(readers.toArray(new IndexReader[0])));
    } catch (IOException e) {
      throw new org.silverpeas.core.index.search.model.ParseException(INDEX_SEARCH_ERROR, e);
    }
  }

  /**
   * Return a multi-searcher built on the searchers list matching the (space, component) pair set.
   */
  private org.apache.lucene.search.IndexSearcher getSearcher(QueryDescription query)
      throws ParseException {
    Set<String> indexPathSet = getIndexPathSet(query.getWhereToSearch());
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
    try {
      return new org.apache.lucene.search.IndexSearcher(
          new MultiReader(readers.toArray(new IndexReader[0])));
    } catch (IOException ioe) {
      throw new org.silverpeas.core.index.search.model.ParseException(INDEX_SEARCH_ERROR, ioe);
    }
  }

  private String getExternalComponentPath(ExternalComponent extComp) {
    final String externalComponentPathPart = IndexFileManager
        .extractComponentPath(extComp.getComponent());
    return extComp.getDataPath() + File.separator + "index" + File.separator +
        externalComponentPathPart + File.separator + "index";
  }

  /**
   * Build the set of all the path to the directories index corresponding the given (space,
   * component) pairs.
   */
  Set<String> getIndexPathSet(Set<String> componentIds) {
    Set<String> pathSet = new HashSet<>();
    for (String componentId : componentIds) {
      pathSet.add(indexManager.getIndexDirectoryPath(componentId));
    }
    return pathSet;
  }

  private TermRangeQuery getRangeQueryOnCreationDate(QueryDescription query) {
    LocalDate beginDate = query.getRequestedCreatedAfter();
    LocalDate endDate = query.getRequestedCreatedBefore();
    return getTermRangeQuery(IndexManager.CREATIONDATE, beginDate, endDate);
  }

  private TermRangeQuery getRangeQueryOnLastUpdateDate(QueryDescription query) {
    LocalDate beginDate = query.getRequestedUpdatedAfter();
    LocalDate endDate = query.getRequestedUpdatedBefore();
    return getTermRangeQuery(IndexManager.LASTUPDATEDATE, beginDate, endDate);
  }

  private TermQuery getTermQueryOnAuthor(QueryDescription query) {
    if (!StringUtil.isDefined(query.getRequestedAuthor())) {
      return null;
    }
    Term authorTerm = new Term(IndexManager.CREATIONUSER, query.getRequestedAuthor());
    return new TermQuery(authorTerm);
  }

  private PrefixQuery getTermQueryOnFolder(QueryDescription query) {
    if (!StringUtil.isDefined(query.getRequestedFolder())) {
      return null;
    }
    Term term = new Term(IndexManager.PATH, query.getRequestedFolder());
    return new PrefixQuery(term);
  }

  private TermRangeQuery getTermRangeQuery(String fieldName, LocalDate beginDate, LocalDate endDate) {
    if (Objects.isNull(beginDate) && Objects.isNull(endDate)) {
      return null;
    }

    String start = IndexEntry.STARTDATE_DEFAULT;
    if (Objects.nonNull(beginDate)) {
      start = DateUtil.formatAsLuceneDate(beginDate);
    }

    String end = IndexEntry.ENDDATE_DEFAULT;
    if (Objects.nonNull(endDate)) {
      end = DateUtil.formatAsLuceneDate(endDate);
    }
    return TermRangeQuery.newStringRange(fieldName, start, end, true, true);
  }

  private String formatDate(LocalDate date) {
    return DateUtil.formatAsLuceneDate(date);
  }

  private LocalDate parseDate(String date) {
    try {
      return DateUtil.parseFromLucene(date);
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
    }
    return null;
  }

  private TermInSetQuery getScopeQuery(QueryDescription query) {
    List<BytesRef> terms = new ArrayList<>();
    for (String scope : query.getWhereToSearch()) {
      terms.add(new BytesRef(scope));
    }
    return new TermInSetQuery(IndexManager.SCOPE, terms);
  }
}