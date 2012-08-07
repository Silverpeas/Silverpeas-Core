/**
* Copyright (C) 2000 - 2011 Silverpeas
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of
* the GPL, you may redistribute this Program in connection with Free/Libre
* Open Source Software ("FLOSS") applications as described in Silverpeas's
* FLOSS exception. You should have received a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://repository.silverpeas.com/legal/licensing"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.stratelia.webactiv.searchEngine.model;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.SearchEnginePropertiesManager;
import com.stratelia.webactiv.util.indexEngine.model.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;

/**
* The WAIndexSearcher class implements search over all the WebActiv's index. A WAIndexSearcher
* manages a set of cached lucene IndexSearcher.
*/
public class WAIndexSearcher {

  /**
* The primary and secondary factor are used to give a better score to entries whose title or
* abstract match the query.
* @see #merge
*/
  private int primaryFactor = 3;
  private int secondaryFactor = 1;
  private QueryParser.Operator defaultOperand = QueryParser.AND_OPERATOR;
  /**
* indicates the number maximum of results returned by the search
*/
  private int maxNumberResult = 0;

  /**
* The no parameters constructor retrieves all the needed data from the IndexEngine.properties
* file.
*/
  public WAIndexSearcher() {
    indexManager = new IndexManager();
    primaryFactor = getFactorFromProperties("PrimaryFactor", primaryFactor);
    secondaryFactor = getFactorFromProperties("SecondaryFactor", secondaryFactor);
    try {
      ResourceLocator resource = new ResourceLocator(
              "com.silverpeas.searchEngine.searchEngineSettings", "");
      int paramOperand = resource.getInteger("defaultOperand", 0);
      if (paramOperand == 0) {
        defaultOperand = QueryParser.OR_OPERATOR;
      } else {
        defaultOperand = QueryParser.AND_OPERATOR;
      }

      maxNumberResult = resource.getInteger("maxResults", 100);
    } catch (MissingResourceException e) {
    } catch (NumberFormatException e) {
    }
  }

  /**
* Get the primary factor from the IndexEngine.properties file.
*/
  /**
   *
   * @param propertyName
   * @param defaultValue
   * @return
   */
  public static int getFactorFromProperties(String propertyName, int defaultValue) {
    ResourceLocator resource = new ResourceLocator(
            "com.stratelia.webactiv.util.indexEngine.IndexEngine", "");
    return resource.getInteger(propertyName, defaultValue);
  }

  /**
* Searches the Lucene index for a specific object, by giving the PK of the index entry
* @param component
* @param objectId
* @param objectType
* @return MatchingIndexEntry wrapping the result, else null
*/
  public MatchingIndexEntry search(String component, String objectId, String objectType) {
    SpaceComponentPair pair = new SpaceComponentPair(null, component);
    HashSet<SpaceComponentPair> set = new HashSet<SpaceComponentPair>();
    set.add(pair);

    IndexEntryPK indexEntryPK = new IndexEntryPK(component, objectType, objectId);
    MatchingIndexEntry matchingIndexEntry = null;
    Searcher searcher = getSearcher(set);
    try {
      TopDocs topDocs;
      Term term = new Term(IndexManager.KEY, indexEntryPK.toString());

      TermQuery query = new TermQuery(term);
      topDocs = searcher.search(query, maxNumberResult);
      ScoreDoc scoreDoc = topDocs.scoreDocs[0];

      matchingIndexEntry = createMatchingIndexEntry(scoreDoc, "*", searcher);
    } catch (IOException ioe) {
      SilverTrace.fatal("searchEngine", "WAIndexSearcher.search()",
              "searchEngine.MSG_CORRUPTED_INDEX_FILE", ioe);
    } finally {
      try {
        if (searcher != null) {
          searcher.close();
        }
      } catch (IOException ioe) {
        SilverTrace.fatal("searchEngine", "WAIndexSearcher.search()",
                "searchEngine.MSG_CANNOT_CLOSE_SEARCHER", ioe);
      }
    }
    return matchingIndexEntry;
  }

  /**
* Search the documents of the given component's set. All entries found whose startDate is not
* reached or whose endDate is passed are pruned from the results set.
*/
  public MatchingIndexEntry[] search(QueryDescription query)
          throws com.stratelia.webactiv.searchEngine.model.ParseException {
    long startTime = System.nanoTime();
    List<MatchingIndexEntry> results;

    Searcher searcher = getSearcher(query);

    try {
      TopDocs topDocs;
      BooleanQuery booleanQuery = new BooleanQuery();
      BooleanQuery rangeClauses = new BooleanQuery();
      rangeClauses.add(getVisibilityStartQuery(), BooleanClause.Occur.MUST);
      rangeClauses.add(getVisibilityEndQuery(), BooleanClause.Occur.MUST);

      if (query.getXmlQuery() != null) {
        booleanQuery.add(getXMLQuery(query, searcher), BooleanClause.Occur.MUST);
      } else {
        if (query.getMultiFieldQuery() != null) {
          booleanQuery.add(getMultiFieldQuery(query, searcher), BooleanClause.Occur.MUST);
        } else {
          RangeQuery rangeQuery = getRangeQueryOnCreationDate(query);
          if (!StringUtil.isDefined(query.getQuery()) &&
              (query.isSearchBySpace() || query.isSearchByComponentType()) &&
              !query.isPeriodDefined()) {
            // realizes the search on space without keywords indicated by the user
            String beginDate = "1900/01/01";
            String endDate = "2200/01/01";
            Term lowerTerm = new Term(IndexManager.CREATIONDATE, beginDate);
            Term upperTerm = new Term(IndexManager.CREATIONDATE, endDate);

            rangeQuery = new RangeQuery(lowerTerm, upperTerm, true);
          }
          if (rangeQuery != null) {
            rangeClauses.add(rangeQuery, BooleanClause.Occur.MUST);
          }
          RangeQuery rangeQueryOnLastUpdateDate = getRangeQueryOnLastUpdateDate(query);
          if (rangeQueryOnLastUpdateDate != null) {
            rangeClauses.add(rangeQueryOnLastUpdateDate, BooleanClause.Occur.MUST);
          }
          TermQuery termQueryOnAuthor = getTermQueryOnAuthor(query);
          if (termQueryOnAuthor != null) {
            booleanQuery.add(termQueryOnAuthor, BooleanClause.Occur.MUST);
          }

          try {
            Query plainTextQuery = getPlainTextQuery(query, IndexManager.CONTENT);
            if (plainTextQuery != null) {
              booleanQuery.add(plainTextQuery, BooleanClause.Occur.MUST);
            }
          } catch (ParseException e) {
            throw new com.stratelia.webactiv.searchEngine.model.ParseException("WAIndexSearcher", e);
          }
        }
      }
      SilverTrace.info("searchEngine", "WAIndexSearcher.search()", "root.MSG_GEN_PARAM_VALUE",
              "Query = " + booleanQuery.toString());

      // date range clauses are passed in the filter to optimize search performances
      // but the query cannot be empty : if so, then pass date range in the query
      if (booleanQuery.getClauses().length == 0) {
        topDocs = searcher.search(rangeClauses, null, maxNumberResult);
      }
      else {
        QueryWrapperFilter wrappedFilter = new QueryWrapperFilter(rangeClauses);
        topDocs = searcher.search(booleanQuery, wrappedFilter, maxNumberResult);
      }

      results = makeList(topDocs, query, searcher);
    } catch (IOException ioe) {
      SilverTrace.fatal("searchEngine", "WAIndexSearcher.search()",
              "searchEngine.MSG_CORRUPTED_INDEX_FILE", ioe);
      results = new ArrayList<MatchingIndexEntry>();
    }
    long endTime = System.nanoTime();

    SilverTrace.debug("searchEngine", WAIndexSearcher.class.toString(), " search duration in ms "
            + (endTime - startTime) / 1000000);
    return results.toArray(new MatchingIndexEntry[results.size()]);
  }

  private RangeQuery getVisibilityStartQuery() {
    Term lowerTerm = new Term(IndexManager.STARTDATE, IndexEntry.STARTDATE_DEFAULT);
    Term upperTerm = new Term(IndexManager.STARTDATE, DateUtil.today2SQLDate());

    return new RangeQuery(lowerTerm, upperTerm, true);
  }

  private RangeQuery getVisibilityEndQuery() {
    Term lowerTerm = new Term(IndexManager.ENDDATE, DateUtil.today2SQLDate());
    Term upperTerm = new Term(IndexManager.ENDDATE, IndexEntry.ENDDATE_DEFAULT);

    return new RangeQuery(lowerTerm, upperTerm, true);
  }

  private Query getPlainTextQuery(QueryDescription query, String searchField) throws ParseException {
    if (!StringUtil.isDefined(query.getQuery())) {
      return null;
    }

    String language = query.getRequestedLanguage();
    Analyzer analyzer = indexManager.getAnalyzer(language);

    Query parsedQuery;
    if (I18NHelper.isI18N && "*".equals(language)) {
      // search over all languages
      String[] fields = new String[I18NHelper.getNumberOfLanguages()];

      int l = 0;
      Iterator<String> languages = I18NHelper.getLanguages();
      while (languages.hasNext()) {
        language = languages.next();

        if (I18NHelper.isDefaultLanguage(language)) {
          fields[l] = searchField;
        } else {
          fields[l] = searchField + "_" + language;
        }
        l++;
      }

      MultiFieldQueryParser mfqp = new MultiFieldQueryParser(fields, analyzer);
      mfqp.setDefaultOperator(defaultOperand);
      parsedQuery = mfqp.parse(query.getQuery());
    } else {
      // search only specified language
      if (I18NHelper.isI18N && !"*".equals(language) && !I18NHelper.isDefaultLanguage(language)) {
        searchField = searchField + "_" + language;
      }

      QueryParser queryParser = new QueryParser(searchField, analyzer);
      queryParser.setDefaultOperator(defaultOperand);
      SilverTrace.info("searchEngine", "WAIndexSearcher.getPlainTextQuery", "root.MSG_GEN_PARAM_VALUE",
              "defaultOperand = " + defaultOperand);
      parsedQuery = queryParser.parse(query.getQuery());
      SilverTrace.info("searchEngine", "WAIndexSearcher.getPlainTextQuery", "root.MSG_GEN_PARAM_VALUE",
              "getOperator() = " + queryParser.getDefaultOperator());
    }

    SilverTrace.info("searchEngine", "WAIndexSearcher.getPlainTextQuery", "root.MSG_GEN_PARAM_VALUE",
            "parsedQuery = " + parsedQuery.toString());
    return parsedQuery;
  }

  private Query getXMLQuery(QueryDescription query, Searcher searcher)
          throws com.stratelia.webactiv.searchEngine.model.ParseException,
          IOException {

    try {
      Map<String, String> xmlQuery = query.getXmlQuery();
      String xmlTitle = query.getXmlTitle();

      int nbFields = xmlQuery.size();
      if (StringUtil.isDefined(xmlTitle)) {
        nbFields++;
      }

      String[] fields = xmlQuery.keySet().toArray(new String[nbFields]);
      String[] queries = xmlQuery.values().toArray(new String[nbFields]);

      if (StringUtil.isDefined(xmlTitle)) {
        fields[nbFields - 1] = IndexManager.TITLE;
        queries[nbFields - 1] = xmlTitle;
      }

      Analyzer analyzer = indexManager.getAnalyzer(query.getRequestedLanguage());

      BooleanClause.Occur[] flags = new BooleanClause.Occur[fields.length];
      for (int f = 0; f < fields.length; f++) {
        flags[f] = BooleanClause.Occur.MUST;
      }

      Query parsedQuery = MultiFieldQueryParser.parse(queries, fields, flags, analyzer);
      SilverTrace.info("searchEngine", "WAIndexSearcher.getXMLHits", "root.MSG_GEN_PARAM_VALUE",
              "parsedQuery = " + parsedQuery.toString());

      return parsedQuery;
    } catch (org.apache.lucene.queryParser.ParseException e) {
      throw new com.stratelia.webactiv.searchEngine.model.ParseException("WAIndexSearcher", e);
    }
  }

  private Query getMultiFieldQuery(QueryDescription query, Searcher searcher)
          throws com.stratelia.webactiv.searchEngine.model.ParseException, IOException {
    try {
      List<FieldDescription> fieldQueries = query.getMultiFieldQuery();
      String keyword = query.getQuery();

      int nbFields = fieldQueries.size();
      if (StringUtil.isDefined(keyword)) {
        nbFields++;
      }

      String[] fields = new String[nbFields];
      String[] queries = new String[nbFields];
      BooleanClause.Occur[] flags = new BooleanClause.Occur[nbFields];

      if (StringUtil.isDefined(keyword)) {
        flags[nbFields - 1] = BooleanClause.Occur.MUST;
        fields[nbFields - 1] = IndexManager.HEADER;
        queries[nbFields - 1] = keyword;
      }

      FieldDescription fieldQuery;
      for (int f = 0; f < fieldQueries.size(); f++) {
        fieldQuery = fieldQueries.get(f);

        flags[f] = BooleanClause.Occur.MUST;
        fields[f] = fieldQuery.getFieldName();
        queries[f] = fieldQuery.getContent();
      }

      Analyzer analyzer = indexManager.getAnalyzer(query.getRequestedLanguage());
      Query parsedQuery = MultiFieldQueryParser.parse(queries, fields, flags, analyzer);
      SilverTrace.info("searchEngine", "WAIndexSearcher.getMultiFieldHits",
              "root.MSG_GEN_PARAM_VALUE", "parsedQuery = " + parsedQuery.toString());
      return parsedQuery;
    } catch (org.apache.lucene.queryParser.ParseException e) {
      throw new com.stratelia.webactiv.searchEngine.model.ParseException("WAIndexSearcher", e);
    }

  }

  /**
* @param scoreDoc occurence of Lucene search result
* @param requestedLanguage
* @param searcher
* @return MatchingIndexEntry wraps the Lucene search result
* @throws IOException if there is a problem when searching Lucene index
*/
  private MatchingIndexEntry createMatchingIndexEntry(ScoreDoc scoreDoc, String requestedLanguage,
          Searcher searcher) throws IOException {
    Document doc = searcher.doc(scoreDoc.doc);
    MatchingIndexEntry indexEntry =
            new MatchingIndexEntry(IndexEntryPK.create(doc.get(IndexManager.KEY)));

    Iterator<String> languages = I18NHelper.getLanguages();
    while (languages.hasNext()) {
      String language = languages.next();

      if (I18NHelper.isDefaultLanguage(language)) {
        indexEntry.setTitle(doc.get(IndexManager.TITLE), language);
        indexEntry.setPreview(doc.get(IndexManager.PREVIEW), language);
      } else {
        indexEntry.setTitle(doc.get(IndexManager.TITLE + "_" + language), language);
        indexEntry.setPreview(doc.get(IndexManager.PREVIEW + "_" + language), language);
      }
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
    indexEntry.setScore(scoreDoc.score); // TODO check the score.
    // Checks the content to see if it contains sortable field
    // and puts them in MatchingIndexEntry object
    if ("Publication".equals(indexEntry.getObjectType())) {
      HashMap<String, String> sortableField = new HashMap<String, String>();
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
      Hashtable<String, String> fieldsValueForFacets = new Hashtable<String, String>();
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
          Searcher searcher) throws IOException {
    List<MatchingIndexEntry> results = new ArrayList<MatchingIndexEntry>();

    if (topDocs != null) {
      ScoreDoc scoreDoc;

      for (int i = 0; i < topDocs.scoreDocs.length; i++) {
        scoreDoc = topDocs.scoreDocs[i];
        MatchingIndexEntry indexEntry =
                createMatchingIndexEntry(scoreDoc, query.getRequestedLanguage(), searcher);
        results.add(indexEntry);
      }
    }
    return results;
  }
  /**
* The manager of all the Web'Activ index.
*/
  private final IndexManager indexManager;

  /**
* Return a multi-searcher built on the searchers list matching the (space, component) pair set.
*/
  private Searcher getSearcher(Set<SpaceComponentPair> spaceComponentPairSet) {
    List<Searcher> searcherList = new ArrayList<Searcher>();
    Set<String> indexPathSet = getIndexPathSet(spaceComponentPairSet);

    for (String path : indexPathSet) {
      Searcher searcher = getSearcher(path);
      if (searcher != null) {
        searcherList.add(searcher);
      }
    }

    try {
      return new MultiSearcher(searcherList.toArray(new Searcher[searcherList.size()]));
    } catch (IOException e) {
      SilverTrace.fatal("searchEngine", "WAIndexSearcher",
              "searchEngine.MSG_CORRUPTED_INDEX_FILE", e);
      return null;
    }
  }

  /**
* Return a multi-searcher built on the searchers list matching the (space, component) pair set.
*/
  private Searcher getSearcher(QueryDescription query) {
    List<Searcher> searcherList = new ArrayList<Searcher>();
    Set<String> indexPathSet = getIndexPathSet(query.getSpaceComponentPairSet());

    for (String path : indexPathSet) {
      Searcher searcher = getSearcher(path);
      if (searcher != null) {
        searcherList.add(searcher);
      }
    }

    // Add searcher from external silverpeas server
    Set<ExternalComponent> extSearchers = query.getExtComponents();
    for (ExternalComponent externalComponent : extSearchers) {
      String externalComponentPath = getExternalComponentPath(externalComponent);
      Searcher searcher = getSearcher(externalComponentPath);
      if (searcher != null) {
        searcherList.add(searcher);
      }
    }

    try {
      return new MultiSearcher(searcherList.toArray(new Searcher[searcherList.size()]));
    } catch (IOException e) {
      SilverTrace.fatal("searchEngine", "WAIndexSearcher",
              "searchEngine.MSG_CORRUPTED_INDEX_FILE", e);
      return null;
    }
  }

  private String getExternalComponentPath(ExternalComponent extComp) {
    StringBuilder extStrBuilder = new StringBuilder();
    extStrBuilder.append(extComp.getDataPath()).append(File.separator);
    extStrBuilder.append("index").append(File.separator);
    extStrBuilder.append(extComp.getComponent()).append(File.separator).append("index");
    return extStrBuilder.toString();
  }

  /**
* Build the set of all the path to the directories index corresponding the given (space,
* component) pairs.
*/
  public Set<String> getIndexPathSet(Set<SpaceComponentPair> spaceComponentPairSet) {
    Set<String> pathSet = new HashSet<String>();

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
  private Searcher getSearcher(String path) {
    return IndexSearchersCache.getIndexSearcher(path);
  }

  protected RangeQuery getRangeQueryOnCreationDate(QueryDescription query) {
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

    Term lowerTerm = new Term(IndexManager.CREATIONDATE, beginDate);
    Term upperTerm = new Term(IndexManager.CREATIONDATE, endDate);

    return new RangeQuery(lowerTerm, upperTerm, true);
  }

  protected RangeQuery getRangeQueryOnLastUpdateDate(QueryDescription query) {
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

    Term lowerTerm = new Term(IndexManager.LASTUPDATEDATE, beginDate);
    Term upperTerm = new Term(IndexManager.LASTUPDATEDATE, endDate);

    return new RangeQuery(lowerTerm, upperTerm, true);
  }

  protected TermQuery getTermQueryOnAuthor(QueryDescription query) {
    if (!StringUtil.isDefined(query.getRequestedAuthor())) {
      return null;
    }
    Term authorTerm = new Term(IndexManager.CREATIONUSER, query.getRequestedAuthor());
    return new TermQuery(authorTerm);
  }
}