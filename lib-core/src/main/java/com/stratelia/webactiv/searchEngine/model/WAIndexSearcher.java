package com.stratelia.webactiv.searchEngine.model;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.indexEngine.model.FieldDescription;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;
import com.stratelia.webactiv.util.indexEngine.model.IndexManager;
import com.stratelia.webactiv.util.indexEngine.model.SpaceComponentPair;

/**
 * The WAIndexSearcher class implements search over all the WebActiv's index.
 * 
 * A WAIndexSearcher manages a set of cached lucene IndexSearcher.
 */
public class WAIndexSearcher {
  /**
   * The primary and secondary factor are used to give a better score to entries
   * whose title or abstract match the query.
   * 
   * @see #merge
   */
  private int primaryFactor = 3;
  private int secondaryFactor = 1;
  private QueryParser.Operator defaultOperand = QueryParser.OR_OPERATOR;

  /**
   * The no parameters constructor retrieves all the needed data from the
   * IndexEngine.properties file.
   */

  public WAIndexSearcher() {
    indexManager = new IndexManager();
    primaryFactor = getFactorFromProperties("PrimaryFactor", primaryFactor);
    secondaryFactor = getFactorFromProperties("SecondaryFactor",
        secondaryFactor);
    try {
      ResourceLocator resource = new ResourceLocator(
          "com.silverpeas.searchEngine.searchEngineSettings", "");
      int paramOperand = Integer.parseInt(resource.getString("defaultOperand",
          "0"));
      if (paramOperand == 0)
        defaultOperand = QueryParser.OR_OPERATOR;
      else
        defaultOperand = QueryParser.AND_OPERATOR;
    } catch (MissingResourceException e) {
    } catch (NumberFormatException e) {
    }
  }

  /**
   * Get the primary factor from the IndexEngine.properties file.
   */
  public static int getFactorFromProperties(String propertyName,
      int defaultValue) {
    int factor = defaultValue;

    try {
      ResourceLocator resource = new ResourceLocator(
          "com.stratelia.webactiv.util.indexEngine.IndexEngine", "");

      String factorString = resource.getString(propertyName);

      factor = Integer.parseInt(factorString);
    } catch (MissingResourceException e) {
    } catch (NumberFormatException e) {
    }

    return factor;
  }

  /**
   * Search the documents of the given component's set.
   * 
   * All entries found whose startDate is not reached or whose endDate is passed
   * are pruned from the results set.
   */
  public MatchingIndexEntry[] search(QueryDescription query)
      throws com.stratelia.webactiv.searchEngine.model.ParseException {
    if (query.isEmpty())
      return new MatchingIndexEntry[0];

    List results = null;

    Searcher searcher = getSearcher(query.getSpaceComponentPairSet());

    try {
      if (query.getXmlQuery() != null) {
        results = makeList(getXMLHits(query, searcher), query);
      } else {
        if (query.getMultiFieldQuery() != null) {
          results = makeList(getMultiFieldHits(query, searcher), query);
        } else {
          List contentMatchingResults = makeList(getHits(query,
              IndexManager.CONTENT, searcher), query);

          if (contentMatchingResults.size() > 0) {
            List headerMatchingResults = makeList(getHits(query,
                IndexManager.HEADER, searcher), query);

            if (headerMatchingResults.size() > 0) {
              results = merge(headerMatchingResults, primaryFactor,
                  contentMatchingResults, secondaryFactor);
            } else {
              results = contentMatchingResults;
            }
          } else {
            results = new ArrayList(); // empty results list.
          }
        }
      }
    } catch (IOException ioe) {
      SilverTrace.fatal("searchEngine", "WAIndexSearcher.search()",
          "searchEngine.MSG_CORRUPTED_INDEX_FILE", ioe);
      results = new ArrayList();
    } finally {
      try {
        if (searcher != null)
          searcher.close();
      } catch (IOException ioe) {
        SilverTrace.fatal("searchEngine", "WAIndexSearcher.search()",
            "searchEngine.MSG_CANNOT_CLOSE_SEARCHER", ioe);
      }
    }

    return (MatchingIndexEntry[]) results
        .toArray(new MatchingIndexEntry[results.size()]);
  }

  /**
   * Returns the lucene hits of the query
   * 
   * @param searchField
   *          the search field within the index.
   */
  private Hits getHits(QueryDescription query, String searchField,
      Searcher searcher)
      throws com.stratelia.webactiv.searchEngine.model.ParseException {
    Hits hits = null;

    try {
      Analyzer analyzer = indexManager.getAnalyzer(Locale.getDefault()
          .getLanguage());

      String language = query.getRequestedLanguage();

      Query parsedQuery = null;
      if (I18NHelper.isI18N && "*".equals(language)) {
        // search over all languages
        String[] fields = new String[I18NHelper.getNumberOfLanguages()];
        String[] queries = new String[I18NHelper.getNumberOfLanguages()];

        int l = 0;
        Iterator languages = I18NHelper.getLanguages();
        while (languages.hasNext()) {
          language = (String) languages.next();

          if (I18NHelper.isDefaultLanguage(language))
            fields[l] = searchField;
          else
            fields[l] = searchField + "_" + language;
          queries[l] = query.getQuery();
          l++;
        }

        parsedQuery = MultiFieldQueryParser.parse(queries, fields, analyzer);
      } else {
        // search only specified language
        if (I18NHelper.isI18N && !"*".equals(language)
            && !I18NHelper.isDefaultLanguage(language))
          searchField = searchField + "_" + language;

        QueryParser queryParser = new QueryParser(searchField, analyzer);
        queryParser.setDefaultOperator(defaultOperand);
        SilverTrace.info("searchEngine", "WAIndexSearcher.getHits",
            "root.MSG_GEN_PARAM_VALUE", "defaultOperand = " + defaultOperand);
        parsedQuery = queryParser.parse(query.getQuery());
        SilverTrace.info("searchEngine", "WAIndexSearcher.getHits",
            "root.MSG_GEN_PARAM_VALUE", "getOperator() = "
                + queryParser.getDefaultOperator());
      }

      SilverTrace
          .info("searchEngine", "WAIndexSearcher.getHits",
              "root.MSG_GEN_PARAM_VALUE", "parsedQuery = "
                  + parsedQuery.toString());

      hits = searcher.search(parsedQuery);
    } catch (org.apache.lucene.queryParser.ParseException e) {
      throw new com.stratelia.webactiv.searchEngine.model.ParseException(
          "WAIndexSearcher", e);
    } catch (IOException e) {
      SilverTrace.fatal("searchEngine", "WAIndexSearcher",
          "searchEngine.MSG_CORRUPTED_INDEX_FILE", e);
      hits = null;
    } catch (ArrayIndexOutOfBoundsException e) {
      SilverTrace.fatal("searchEngine", "WAIndexSearcher",
          "searchEngine.MSG_CORRUPTED_INDEX_FILE", e);
      hits = null;
    }

    return hits;
  }

  private Hits getXMLHits(QueryDescription query, Searcher searcher)
      throws com.stratelia.webactiv.searchEngine.model.ParseException,
      IOException {
    Hits hits = null;

    try {
      Hashtable xmlQuery = query.getXmlQuery();
      String xmlTitle = query.getXmlTitle();

      int nbFields = xmlQuery.size();
      if (xmlTitle != null)
        nbFields++;

      String[] fields = (String[]) xmlQuery.keySet().toArray(
          new String[nbFields]);
      String[] queries = (String[]) xmlQuery.values().toArray(
          new String[nbFields]);

      if (xmlTitle != null) {
        fields[nbFields - 1] = IndexManager.TITLE;
        queries[nbFields - 1] = xmlTitle;
      }

      Analyzer analyzer = indexManager
          .getAnalyzer(query.getRequestedLanguage());

      BooleanClause.Occur[] flags = new BooleanClause.Occur[fields.length];
      for (int f = 0; f < fields.length; f++) {
        flags[f] = BooleanClause.Occur.MUST;
      }

      Query parsedQuery = MultiFieldQueryParser.parse(queries, fields, flags,
          analyzer);
      SilverTrace
          .info("searchEngine", "WAIndexSearcher.getXMLHits",
              "root.MSG_GEN_PARAM_VALUE", "parsedQuery = "
                  + parsedQuery.toString());

      hits = searcher.search(parsedQuery);
    } catch (org.apache.lucene.queryParser.ParseException e) {
      throw new com.stratelia.webactiv.searchEngine.model.ParseException(
          "WAIndexSearcher", e);
    } catch (IOException e) {
      SilverTrace.fatal("searchEngine", "WAIndexSearcher.getXMLHits",
          "searchEngine.MSG_CORRUPTED_INDEX_FILE", e);
      hits = null;
    }

    return hits;
  }

  private Hits getMultiFieldHits(QueryDescription query, Searcher searcher)
      throws com.stratelia.webactiv.searchEngine.model.ParseException,
      IOException {
    Hits hits = null;

    try {
      List fieldQueries = query.getMultiFieldQuery();
      String keyword = query.getQuery();

      int nbFields = fieldQueries.size();
      if (StringUtil.isDefined(keyword))
        nbFields++;

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
        fieldQuery = (FieldDescription) fieldQueries.get(f);

        flags[f] = BooleanClause.Occur.MUST;
        fields[f] = fieldQuery.getFieldName();
        queries[f] = fieldQuery.getContent();
      }

      Analyzer analyzer = indexManager
          .getAnalyzer(query.getRequestedLanguage());
      Query parsedQuery = MultiFieldQueryParser.parse(queries, fields, flags,
          analyzer);
      SilverTrace
          .info("searchEngine", "WAIndexSearcher.getMultiFieldHits",
              "root.MSG_GEN_PARAM_VALUE", "parsedQuery = "
                  + parsedQuery.toString());

      hits = searcher.search(parsedQuery);
    } catch (org.apache.lucene.queryParser.ParseException e) {
      throw new com.stratelia.webactiv.searchEngine.model.ParseException(
          "WAIndexSearcher", e);
    } catch (IOException e) {
      SilverTrace.fatal("searchEngine", "WAIndexSearcher.getMultiFieldHits",
          "searchEngine.MSG_CORRUPTED_INDEX_FILE", e);
      hits = null;
    }

    return hits;
  }

  /**
   * Makes a List of MatchingIndexEntry from a lucene hits.
   * 
   * All entries found whose startDate is not reached or whose endDate is passed
   * are pruned from the results list.
   */
  private List makeList(Hits hits, QueryDescription query) throws IOException {
    List results = new ArrayList();
    String today = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
    String user = query.getSearchingUser();
    String beforeDate = query.getRequestedCreatedBefore();
    String afterDate = query.getRequestedCreatedAfter();
    String requestedAuthor = query.getRequestedAuthor();

    SilverTrace.info("searchEngine", "WAIndexSearcher.makeList",
        "root.MSG_GEN_PARAM_VALUE", "beforeDate = " + beforeDate);
    SilverTrace.info("searchEngine", "WAIndexSearcher.makeList",
        "root.MSG_GEN_PARAM_VALUE", "afterDate = " + afterDate);

    if (hits != null) {
      for (int i = 0; i < hits.length(); i++) {
        // try
        // {
        MatchingIndexEntry indexEntry;
        Document doc = hits.doc(i);

        String startDate = doc.get(IndexManager.STARTDATE);
        String endDate = doc.get(IndexManager.ENDDATE);
        String creationDate = doc.get(IndexManager.CREATIONDATE);

        SilverTrace.info("searchEngine", "WAIndexSearcher.makeList",
            "root.MSG_GEN_PARAM_VALUE", "startDate = " + startDate);
        SilverTrace.info("searchEngine", "WAIndexSearcher.makeList",
            "root.MSG_GEN_PARAM_VALUE", "endDate = " + endDate);
        SilverTrace.info("searchEngine", "WAIndexSearcher.makeList",
            "root.MSG_GEN_PARAM_VALUE", "creationDate = " + creationDate);

        boolean testDate = true;

        if ((beforeDate != null) && (afterDate != null)) {
          if (creationDate.compareTo(afterDate) >= 0) {
            if (beforeDate.compareTo(creationDate) >= 0)
              testDate = true;
            else
              testDate = false;
          } else {
            testDate = false;
          }
          SilverTrace.info("searchEngine", "WAIndexSearcher.makeList",
              "root.MSG_GEN_PARAM_VALUE", "testDate = " + testDate);

        }
        if ((beforeDate == null) && (afterDate != null)) {
          if (creationDate.compareTo(afterDate) >= 0) {
            testDate = true;
          } else {
            testDate = false;
          }
          SilverTrace.info("searchEngine", "WAIndexSearcher.makeList",
              "root.MSG_GEN_PARAM_VALUE", "testDate = " + testDate);
        }
        if ((beforeDate != null) && (afterDate == null)) {

          if (beforeDate.compareTo(creationDate) >= 0)
            testDate = true;
          else
            testDate = false;
          SilverTrace.info("searchEngine", "WAIndexSearcher.makeList",
              "root.MSG_GEN_PARAM_VALUE", "testDate = " + testDate);
        }
        String author = doc.get(IndexManager.CREATIONUSER);

        if (startDate == null || startDate.equals("")) {
          startDate = IndexEntry.STARTDATE_DEFAULT;
        }
        if (endDate == null || endDate.equals("")) {
          endDate = IndexEntry.ENDDATE_DEFAULT;
        }

        // the document must be published onless the searcher is the author
        if ((startDate.compareTo(today) <= 0 && today.compareTo(endDate) <= 0)
            || user.equals(author)) {
          if (testDate
              && (requestedAuthor == null || requestedAuthor.equals(author))) {
            indexEntry = new MatchingIndexEntry(IndexEntryPK.create(doc
                .get(IndexManager.KEY)));

            Iterator languages = I18NHelper.getLanguages();
            while (languages.hasNext()) {
              String language = (String) languages.next();

              if (I18NHelper.isDefaultLanguage(language)) {
                indexEntry.setTitle(doc.get(IndexManager.TITLE), language);
                indexEntry.setPreview(doc.get(IndexManager.PREVIEW), language);
              } else {
                indexEntry.setTitle(doc
                    .get(IndexManager.TITLE + "_" + language), language);
                indexEntry.setPreview(doc.get(IndexManager.PREVIEW + "_"
                    + language), language);
              }
            }

            indexEntry.setKeyWords(doc.get(IndexManager.KEYWORDS));
            indexEntry.setCreationUser(doc.get(IndexManager.CREATIONUSER));
            indexEntry.setCreationDate(doc.get(IndexManager.CREATIONDATE));
            indexEntry.setThumbnail(doc.get(IndexManager.THUMBNAIL));
            indexEntry.setThumbnailMimeType(doc
                .get(IndexManager.THUMBNAIL_MIMETYPE));
            indexEntry.setThumbnailDirectory(doc
                .get(IndexManager.THUMBNAIL_DIRECTORY));
            indexEntry.setStartDate(startDate);
            indexEntry.setEndDate(endDate);
            indexEntry.setScore(hits.score(i));
            results.add(indexEntry);
          }
        }
        /*
         * } catch (IOException e) { SilverTrace.fatal("searchEngine",
         * "WAIndexSearcher", "searchEngine.MSG_CORRUPTED_INDEX_FILE", e); }
         */
      }
    }
    return results;
  }

  /**
   * Merges two MatchingIndexEntry List and re-computes the scores.
   * 
   * The new score is :
   * 
   * <PRE>
   * primaryScore * primaryFactor + secondaryScore * secondaryFactor
   * ---------------------------------------------------------------
   * primaryFactor + primaryScore
   * </PRE>
   * 
   * If an entry is in the secondary list but not in the primary, his score is
   * left unchanged.
   * 
   * If any, all entries in the primary list but not the secondary are ignored.
   * In practice this case should not occurs as the secondary is extracted from
   * the CONTENT index which contains all the HEADER contents from which is
   * extracted the primary list.
   */
  private List merge(List primaryList, int primaryFactor, List secondaryList,
      int secondaryFactor) {
    List result = new ArrayList();

    float newScore = 0;
    MatchingIndexEntry primaryEntry = null;
    MatchingIndexEntry secondaryEntry = null;

    // Create a map key -> entry for the primaryList.
    Map primaryMap = new HashMap();
    Iterator i = primaryList.iterator();

    while (i.hasNext()) {
      primaryEntry = (MatchingIndexEntry) i.next();
      primaryMap.put(primaryEntry.getPK(), primaryEntry);
    }

    Iterator j = secondaryList.iterator();

    while (j.hasNext()) {
      secondaryEntry = (MatchingIndexEntry) j.next();

      primaryEntry = (MatchingIndexEntry) primaryMap
          .get(secondaryEntry.getPK());
      if (primaryEntry != null) {
        newScore = secondaryFactor * secondaryEntry.getScore();
        newScore += primaryFactor * primaryEntry.getScore();
        newScore /= (primaryFactor + secondaryFactor);
        secondaryEntry.setScore(newScore);
      }

      result.add(secondaryEntry);
    }

    Collections.sort(result, ScoreComparator.comparator);
    return result;
  }

  /**
   * The manager of all the Web'Activ index.
   */
  private final IndexManager indexManager;

  /**
   * Return a multi-searcher built on the searchers list matching the (space,
   * component) pair set.
   */
  private Searcher getSearcher(Set spaceComponentPairSet) {
    List searcherList = new ArrayList();
    Set indexPathSet = getIndexPathSet(spaceComponentPairSet);

    Iterator i = indexPathSet.iterator();

    while (i.hasNext()) {
      String path = (String) i.next();
      Searcher searcher = getSearcher(path);

      if (searcher != null) {
        searcherList.add(searcher);
      }
    }

    try {
      return new MultiSearcher((Searcher[]) searcherList
          .toArray(new Searcher[searcherList.size()]));
    } catch (IOException e) {
      SilverTrace.fatal("searchEngine", "WAIndexSearcher",
          "searchEngine.MSG_CORRUPTED_INDEX_FILE", e);
      return null;
    }
  }

  /**
   * Build the set of all the path to the directories index corresponding the
   * given (space, component) pairs.
   */
  private Set getIndexPathSet(Set spaceComponentPairSet) {
    Set pathSet = new HashSet();

    Iterator i = spaceComponentPairSet.iterator();

    SpaceComponentPair pair = null;
    String space = null;
    String component = null;
    while (i.hasNext()) {
      pair = (SpaceComponentPair) i.next();
      space = pair.getSpace();
      component = pair.getComponent();

      // Both cases.
      // 1 - space == null && component != null : search in an component's
      // instance
      // 2 - space != null && component != null : search in pdc or user's
      // components (todo, agenda...)

      if (component != null) {
        pathSet.add(indexManager.getIndexDirectoryPath(space, component));
      }
    }
    return pathSet;
  }

  /**
   * Retrieve the index searcher over the specified index directory.
   * 
   * The index readers are cached in a Map (path -> (timestamp, reader)). If a
   * reader is found in the cache but appear to be too old (according to the
   * timestamp) then it is re-open.
   * 
   * If the index files are not found, null is returned without any error (as
   * this case comes each time a request is made on a space/component without
   * any indexed documents).
   */
  private Searcher getSearcher(String path) {
    /*
     * if (!(new File(path).exists())) { return null;
     * 
     * } CachedIndex cached = (CachedIndex) cache.get(path);
     * 
     * try { if (cached == null || cached.timestamp !=
     * IndexReader.lastModified(path)) { if (cached != null) {
     * cached.reader.close(); SilverTrace.info("searchEngine",
     * "WAIndexSearcher", "searchEngine.INFO_REOPEN_INDEX_FILE", path); } else {
     * SilverTrace.info("searchEngine", "WAIndexSearcher",
     * "searchEngine.INFO_OPEN_INDEX_FILE", path); }
     * 
     * cached = new CachedIndex(path); cache.put(path, cached); } } catch
     * (IOException e) {
     * 
     * SilverTrace.error("searchEngine", "WAIndexSearcher",
     * "searchEngine.MSG_CANT_READ_INDEX_FILE", e); return null; }
     * 
     * return new IndexSearcher(cached.reader);
     */

    Searcher searcher = null;

    try {
      searcher = new IndexSearcher(path);
    } catch (IOException ioe) {
      SilverTrace.debug("searchEngine", "WAIndexSearcher.getSearcher()",
          "searchEngine.MSG_CANT_READ_INDEX_FILE", ioe);
    }

    return searcher;
  }
}