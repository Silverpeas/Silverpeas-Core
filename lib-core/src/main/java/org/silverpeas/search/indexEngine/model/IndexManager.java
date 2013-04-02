/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.search.indexEngine.model;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LimitTokenCountAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.search.indexEngine.parser.Parser;
import org.silverpeas.search.indexEngine.parser.ParserManager;
import org.silverpeas.search.util.SearchEnginePropertiesManager;

/**
 * An IndexManager manage all the web'activ's index. An IndexManager is NOT thread safe : to share
 * an IndexManager between several threads use an IndexerThread.
 */
public class IndexManager {

  /**
   * The fields names used by lucene to store each element of an index entry.
   */
  public static final String ID = "id";
  public static final String KEY = "key";
  public static final String TITLE = "title";
  public static final String PREVIEW = "preview";
  public static final String KEYWORDS = "keywords";
  public static final String LANG = "lang";
  public static final String CREATIONDATE = "creationDate";
  public static final String CREATIONUSER = "creationUser";
  public static final String LASTUPDATEDATE = "updateDate";
  public static final String LASTUPDATEUSER = "updateUser";
  public static final String STARTDATE = "startDate";
  public static final String ENDDATE = "endDate";
  public static final String HEADER = "header";
  public static final String CONTENT = "content";
  public static final String THUMBNAIL = "thumbnail";
  public static final String THUMBNAIL_MIMETYPE = "thumbnailMimeType";
  public static final String THUMBNAIL_DIRECTORY = "thumbnailDirectory";
  public static final String SERVER_NAME = "serverName";
  public static final String EMBEDDED_FILE_IDS = "embeddedFileIds";
  public static final String FIELDS_FOR_FACETS = "fieldsForFacet";
  /**
   * Exhaustive list of indexation's operations Used by objects which must be indexed
   */
  public static final int NONE = -1;
  public static final int ADD = 0;
  public static final int REMOVE = 1;
  public static final int READD = 2;
  private Map<String, IndexWriter> indexWriters = new HashMap<String, IndexWriter>();

  /**
   * The constructor takes no parameters and all the index engine parameters are taken from the
   * properties file "com/stratelia/webactiv/util/indexEngine/indexEngine.properties".
   */
  public IndexManager() {
    SilverTrace.debug("indexEngine", "IndexManager", "indexEngine.INFO_INDEX_ENGINE_STARTED",
        "maxFieldLength=" + maxFieldLength + ", mergeFactor=" + mergeFactor + ", maxMergeDocs="
        + maxMergeDocs);
  }

  /**
   * Add an entry index.
   *
   * @param indexEntry
   */
  public void addIndexEntry(FullIndexEntry indexEntry) {
    indexEntry.setServerName(serverName);
    String indexPath = getIndexDirectoryPath(indexEntry);
    IndexWriter writer = getIndexWriter(indexPath, indexEntry.getLang());
    removeIndexEntry(writer, indexEntry.getPK());
    indexDocs(writer, indexEntry);
    SilverTrace.debug("applicationIndexer", "IndexManager().addIndexEntry()",
        "applicationIndexer.MSG_INDEXING_COMPONENT_ITEM", "componentId = "
        + indexEntry.getComponent());
  }

  /**
   * Optimize all the modified index.
   */
  public void optimize() {
    SilverTrace.debug("indexEngine", "IndexManager", "indexEngine.INFO_STARTS_INDEX_OPTIMIZATION",
        "# of index to optimize = " + indexWriters.size());

    Iterator<Entry<String, IndexWriter>> writerPaths = indexWriters.entrySet().iterator();
    while (writerPaths.hasNext()) {
      Entry<String, IndexWriter> writerEntry = writerPaths.next();
      String writerPath = writerEntry.getKey();
      SilverTrace.debug("indexEngine", "IndexManager", "indexEngine.INFO_STARTS_INDEX_OPTIMIZATION",
          "writerPath = " + writerPath);

      if (writerPath != null) {
        IndexWriter writer = writerEntry.getValue();
        if (writer != null) {
          SilverTrace.debug("indexEngine", "IndexManager.optimize()", "root_MSG_GEN_PARAM_VALUE",
              "try to optimize " + writerPath);
          // First, optimize
          try {
            writer.optimize();
          } catch (IOException e) {
            SilverTrace.error("indexEngine", "IndexManager.optimize()",
                "indexEngine.MSG_INDEX_OPTIMIZATION_FAILED", "Can't optimize index " + writerPath, e);
          }

          SilverTrace.info("indexEngine", "IndexManager.optimize()", "root_MSG_GEN_PARAM_VALUE",
              "# of documents indexed in " + writerPath + " = " + writer.maxDoc());
          // Then, close the writer
          try {
            writer.close();
          } catch (IOException e) {
            SilverTrace.error("indexEngine", "IndexManager.optimize()",
                "indexEngine.MSG_INDEX_OPTIMIZATION_FAILED", "Can't Close index " + writerPath, e);
          }
        }
        // update the spelling index
        if (enableDymIndexing) {
          DidYouMeanIndexer.createSpellIndexForAllLanguage("content", writerPath);
        }

        writerPaths.remove();
      }
    }
  }

  private void removeIndexEntry(IndexWriter writer, IndexEntryPK indexEntry) {
    Term term = new Term(KEY, indexEntry.toString());
    try {
      // removing document according to indexEntryPK
      writer.deleteDocuments(term);
      // closing associated index searcher and removing it from cache
      IndexReadersCache.removeIndexReader(getIndexDirectoryPath(indexEntry));
    } catch (IOException e) {
      SilverTrace.error("indexEngine", "IndexManager", "indexEngine.MSG_REMOVE_REQUEST_FAILED",
          indexEntry.toString(), e);
    }
    SilverTrace.debug("indexEngine", "IndexManager", "indexEngine.INFO_REMOVE_REQUEST_SUCCEED",
        indexEntry.toString());
  }

  /**
   * Remove an entry index .
   *
   * @param indexEntry
   */
  public void removeIndexEntry(IndexEntryPK indexEntry) {
    String indexPath = getIndexDirectoryPath(indexEntry);
    IndexWriter writer = getIndexWriter(indexPath, "");
    if (writer != null) {
      removeIndexEntry(writer, indexEntry);
    } else {
      SilverTrace.debug("indexEngine", "IndexManager", "indexEngine.MSG_UNKNOWN_INDEX_FILE",
          indexPath);
    }
  }

  /**
   * Return the path to the directory where are stored the index for the given index entry.
   *
   * @param indexEntry the index entry.
   * @return the path to the directory where are stored the index for the given index entry.
   */
  public String getIndexDirectoryPath(IndexEntry indexEntry) {
    return getIndexDirectoryPath(indexEntry.getPK());
  }

  /**
   * Return the path to the directory where are stored the index for the given index entry.
   *
   * @param indexEntry
   * @return the path to the directory where are stored the index for the given index entry.
   */
  public String getIndexDirectoryPath(IndexEntryPK indexEntry) {
    return getIndexDirectoryPath(null, indexEntry.getComponent());
  }

  /**
   *
   * Return the path to the directory where are stored the index for the given index entry .
   *
   * @param space
   * @param component
   * @return the path to the directory where are stored the index for the given index entry .
   */
  public String getIndexDirectoryPath(String space, String component) {
    return org.silverpeas.search.indexEngine.IndexFileManager.getAbsoluteIndexPath(space, component);
  }

  /**
   * Return the analyzer used to parse indexed texts and queries in the given language.
   *
   * @param language the language used in a document or a query.
   * @return the analyzer for the required language or a default analyzer.
   */
  public Analyzer getAnalyzer(String language) {
    Analyzer analyzer = WAAnalyzer.getAnalyzer(language);
    if (analyzer == null) {
      analyzer =
          new LimitTokenCountAnalyzer(new StandardAnalyzer(Version.LUCENE_36), maxFieldLength);
    }
    return analyzer;
  }

  /**
   * Get the reader specific of the file described by the file description
   *
   * @param file
   * @return the reader specific of the file described by the file description
   */
  public Reader getReader(FileDescription file) {
    SilverTrace.debug("indexEngine", "IndexManager.getReader", "root.MSG_GEN_ENTER_METHOD");
    Reader reader = null;
    Parser parser = ParserManager.getParser(file.getFormat());

    if (parser != null) {
      reader = parser.getReader(file.getPath(), file.getEncoding());
    }
    SilverTrace.debug("indexEngine", "IndexManager.getReader",
        "root.MSG_GEN_EXIT_METHOD");
    return reader;
  }

  /**
   *
   * Returns an IndexWriter to the index stored at the given path.The index directory and files are
   * created if not found .
   *
   * @param path the path to the index root directory
   * @param language the language of the indexed documents.
   * @return an IndexWriter or null if the index can't be found or create or read.
   */
  private IndexWriter getIndexWriter(String path, String language) {
    IndexWriter writer = indexWriters.get(path);
    if (writer == null) {
      try {
        File file = new File(path);
        if (!file.exists()) {
          file.mkdirs();
        }
        LogDocMergePolicy policy = new LogDocMergePolicy();
        policy.setMergeFactor(mergeFactor);
        policy.setMaxMergeDocs(maxMergeDocs);
        IndexWriterConfig configuration = new IndexWriterConfig(Version.LUCENE_36, getAnalyzer(
            language)).setRAMBufferSizeMB(RAMBufferSizeMB).setMergePolicy(policy);
        writer = new IndexWriter(FSDirectory.open(file), configuration);
      } catch (IOException e) {
        IOUtils.closeQuietly(writer);
        writer = null;
        SilverTrace.error("indexEngine", "IndexManager.getIndexWriter",
            "indexEngine.MSG_UNKNOWN_INDEX_FILE", path, e);
      }
      if (writer != null) {
        indexWriters.put(path, writer);
      }
    }
    return writer;
  }

  /**
   * Method declaration
   *
   * @param writer
   * @param indexEntry
   */
  private void indexDocs(IndexWriter writer, FullIndexEntry indexEntry) {
    try {
      writer.addDocument(makeDocument(indexEntry));
      SilverTrace.debug("indexEngine", "IndexManager.indexDocs",
          "indexEngine.INFO_ADD_REQUEST_SUCCEED", indexEntry.toString());
    } catch (Exception e) {
      SilverTrace.error("indexEngine", "IndexManager.indexDocs",
          "indexEngine.MSG_ADD_REQUEST_FAILED", indexEntry.getTitle(), e);
    }
  }

  /**
   * Create a lucene Document object with the given indexEntry.
   */
  private Document makeDocument(FullIndexEntry indexEntry) {
    Document doc = new Document();
    // fields creation
    doc.add(new Field(KEY, indexEntry.getPK().toString(), Store.YES, Index.NOT_ANALYZED));
    Iterator<String> languages = indexEntry.getLanguages();
    if (indexEntry.getObjectType() != null && indexEntry.getObjectType().startsWith("Attachment")) {
      doc.add(new Field(getFieldName(TITLE, indexEntry.getLang()), indexEntry.getTitle(indexEntry.
          getLang()), Store.YES, Index.NOT_ANALYZED));
    } else {
      while (languages.hasNext()) {
        String language = languages.next();
        if (indexEntry.getTitle(language) != null) {
          doc.add(new Field(getFieldName(TITLE, language), indexEntry.getTitle(language),
              Store.YES, Index.ANALYZED));
        }
      }
    }

    // index description
    // doc.add(new Field(PREVIEW, indexEntry.getPreView(), Field.Store.YES,
    // Field.Index.ANALYZED));
    languages = indexEntry.getLanguages();
    while (languages.hasNext()) {
      String language = languages.next();
      if (indexEntry.getPreview(language) != null) {
        doc.add(new Field(getFieldName(PREVIEW, language), indexEntry.getPreview(language),
            Store.YES, Index.ANALYZED));
      }
      if (indexEntry.getKeywords(language) != null) {
        doc.add(new Field(getFieldName(KEYWORDS, language), indexEntry.getKeywords(language),
            Store.YES, Index.NO));
      }
    }

    // doc.add(new Field(KEYWORDS, indexEntry.getKeyWords(), Field.Store.YES,
    // Field.Index.NO));
    doc.add(new Field(CREATIONDATE, indexEntry.getCreationDate(),
        Store.YES, Index.NOT_ANALYZED));
    doc.add(new Field(CREATIONUSER, indexEntry.getCreationUser(),
        Store.YES, Index.NOT_ANALYZED));
    doc.add(new Field(LASTUPDATEDATE, indexEntry.getLastModificationDate(),
        Store.YES, Index.NOT_ANALYZED));
    doc.add(new Field(LASTUPDATEUSER, indexEntry.getLastModificationUser(),
        Store.YES, Index.NOT_ANALYZED));
    doc.add(new Field(STARTDATE, indexEntry.getStartDate(), Store.YES,
        Index.NOT_ANALYZED));
    doc.add(new Field(ENDDATE, indexEntry.getEndDate(), Store.YES,
        Index.NOT_ANALYZED));
    if (indexEntry.getThumbnail() != null
        && indexEntry.getThumbnailMimeType() != null) {
      doc.add(new Field(THUMBNAIL, indexEntry.getThumbnail(), Store.YES,
          Index.NO));
      doc.add(new Field(THUMBNAIL_MIMETYPE, indexEntry.getThumbnailMimeType(),
          Store.YES, Index.NO));
      doc.add(new Field(THUMBNAIL_DIRECTORY,
          indexEntry.getThumbnailDirectory(), Store.YES, Index.NO));
    }
    if (indexEntry.isIndexId()) {
      doc.add(new Field(CONTENT, indexEntry.getObjectId(), Store.NO, Index.NOT_ANALYZED));
    }
    if (!isWysiwyg(indexEntry)) {
      if (indexEntry.getObjectType() != null
          && indexEntry.getObjectType().startsWith("Attachment")) {
        doc.add(new Field(getFieldName(HEADER, indexEntry.getLang()), indexEntry.getTitle(
            indexEntry.getLang()).toLowerCase(), Store.NO, Index.NOT_ANALYZED));
      } else {
        languages = indexEntry.getLanguages();
        while (languages.hasNext()) {
          String language = languages.next();
          if (indexEntry.getTitle(language) != null) {
            doc.add(new Field(getFieldName(HEADER, language), indexEntry.getTitle(language).
                toLowerCase(), Store.NO, Index.ANALYZED));
            doc.add(new Field(getFieldName(HEADER, language), indexEntry.getTitle(language).
                toLowerCase(), Store.NO, Index.NOT_ANALYZED));
          }
        }
      }
      languages = indexEntry.getLanguages();
      while (languages.hasNext()) {
        String language = languages.next();
        if (indexEntry.getPreview(language) != null) {
          doc.add(new Field(getFieldName(HEADER, language), indexEntry.getPreview(language).
              toLowerCase(), Store.NO, Index.ANALYZED));
        }
        if (indexEntry.getKeywords(language) != null) {
          doc.add(new Field(getFieldName(HEADER, language), indexEntry.getKeywords(language).
              toLowerCase(), Store.NO, Index.ANALYZED));
        }
      }
      if (indexEntry.getObjectType() != null
          && indexEntry.getObjectType().startsWith("Attachment")) {
        doc.add(new Field(getFieldName(HEADER, indexEntry.getLang()), indexEntry.getTitle(
            indexEntry.getLang()).toLowerCase(), Store.NO, Index.NOT_ANALYZED));
      } else {
        doc.add(new Field(CONTENT, indexEntry.getTitle().toLowerCase(), Store.NO, Index.ANALYZED));
      }
      languages = indexEntry.getLanguages();
      while (languages.hasNext()) {
        String language = languages.next();

        if (indexEntry.getTitle(language) != null) {
          doc.add(new Field(getFieldName(CONTENT, language), indexEntry.getTitle(language),
              Store.NO, Index.NOT_ANALYZED));
        }
        if (indexEntry.getPreview(language) != null) {
          doc.add(new Field(getFieldName(CONTENT, language), indexEntry.getPreview(language).
              toLowerCase(), Store.NO, Index.ANALYZED));
        }
        if (indexEntry.getKeywords(language) != null) {
          doc.add(new Field(getFieldName(CONTENT, language), indexEntry.getKeywords(language).
              toLowerCase(), Store.NO, Index.ANALYZED));
        }
      }
    }
    List<TextDescription> list1 = indexEntry.getTextContentList();
    for (TextDescription t : list1) {
      if (t != null) {
        if (t.getContent() != null) {
          doc.add(new Field(getFieldName(CONTENT, t.getLang()), t.getContent(),
              Store.NO, Index.ANALYZED));
        }
      }
    }
    if (StringUtil.isDefined(indexEntry.getObjectId())) {
      AttachmentServiceFactory.getAttachmentService().updateIndexEntryWithDocuments(indexEntry);
    }

    List<FileDescription> list2 = indexEntry.getFileContentList();
    for (FileDescription f : list2) {
      addFile(doc, f);
    }

    List<FileDescription> linkedFiles = indexEntry.getLinkedFileContentList();
    for (FileDescription linkedFile : linkedFiles) {
      addFile(doc, linkedFile);
    }

    Set<String> linkedFileIds = indexEntry.getLinkedFileIdsSet();
    for (String linkedFileId : linkedFileIds) {
      doc.add(new Field(EMBEDDED_FILE_IDS, linkedFileId, Store.YES, Index.NOT_ANALYZED));
    }

    List<FieldDescription> list3 = indexEntry.getFields();
    List<String> fieldsForFacets = new ArrayList<String>(list3.size());
    for (FieldDescription field : list3) {
      if (StringUtil.isDefined(field.getContent())) {
        // if a field is used for the sort or to generate a facet, it's stored in the lucene index
        String fieldName = getFieldName(field.getFieldName(), field.getLang());
        Store storeAction;
        if (field.isStored() || SearchEnginePropertiesManager.getFieldsNameList().contains(field.
            getFieldName())) {
          storeAction = Store.YES;
          fieldsForFacets.add(fieldName);
        } else {
          storeAction = Store.NO;
        }
        doc.add(new Field(fieldName, field.getContent(), storeAction, Index.ANALYZED));
      }
    }
    if (!fieldsForFacets.isEmpty()) {
      String stringForFacets = buildStringForFacets(fieldsForFacets);
      // adds all fields which generate facets
      doc.add(new Field(FIELDS_FOR_FACETS, stringForFacets, Store.YES, Index.NO));
    }

    if (!isWysiwyg(indexEntry)) {
      // Lucene doesn't index all the words in a field
      // (the max is given by the maxFieldLength property)
      // The problem is that we don't no which words are skipped
      // and which ones are taken. So the trick used here :
      // the words which MUST been indexed are given twice to lucene
      // at the beginning of the field CONTENT and at the end of this field.
      // (In the current implementation of lucene and without this trick;
      // some key words are not indexed !!!)
      languages = indexEntry.getLanguages();
      while (languages.hasNext()) {
        String language = languages.next();
        if (indexEntry.getTitle(language) != null) {
          doc.add(new Field(getFieldName(CONTENT, language), indexEntry.getTitle(language).
              toLowerCase(), Store.NO, Index.ANALYZED));
        }
        if (indexEntry.getPreview(language) != null) {
          doc.add(new Field(getFieldName(CONTENT, language), indexEntry.getPreview(language).
              toLowerCase(), Store.NO, Index.ANALYZED));
        }
        if (indexEntry.getKeywords(language) != null) {
          doc.add(new Field(getFieldName(CONTENT, language), indexEntry.getKeywords(language).
              toLowerCase(), Store.NO, Index.ANALYZED));
        }
      }
    }
    // Add server name inside Lucene doc
    doc.add(new Field(SERVER_NAME, indexEntry.getServerName(), Store.YES, Index.NOT_ANALYZED));
    return doc;
  }

  private String buildStringForFacets(List<String> fieldsForFacets) {
    String fieldsForFacet = "";
    if (fieldsForFacets != null && !fieldsForFacets.isEmpty()) {
      fieldsForFacet = StringUtil.join(fieldsForFacets, ',');
    }
    return fieldsForFacet;
  }

  private String getFieldName(String name, String language) {
    if (!I18NHelper.isI18N || I18NHelper.isDefaultLanguage(language)) {
      return name;
    }
    return name + "_" + language;
  }

  /**
   * Add file to Document
   */
  private void addFile(Document doc, FileDescription fileDescription) {
    SilverTrace.debug("indexEngine", "IndexManager.addFile", "root.MSG_GEN_ENTER_METHOD",
        "file = " + fileDescription.getPath() + ", type = " + fileDescription.getFormat());
    File file = new File(fileDescription.getPath());
    if (!file.exists() || !file.isFile()) {
      return;
    }
    try {
      Reader reader = getReader(fileDescription);
      SilverTrace.debug("indexEngine", "IndexManager.addFile", "root.MSG_GEN_PARAM_VALUE",
          "reader returned");
      if (reader != null) {
        SilverTrace.debug("indexEngine", "IndexManager.addFile", "root.MSG_GEN_PARAM_VALUE",
            "reader is not null");
        Field field = new Field(getFieldName(CONTENT, fileDescription.getLang()), reader);
        SilverTrace.debug("indexEngine", "IndexManager.addFile", "root.MSG_GEN_PARAM_VALUE",
            "doc = " + field.name() + ", field = " + field.toString());
        doc.add(field);
      }
    } catch (Exception e) {
      SilverTrace.error("indexEngine", "IndexManager",
          "indexEngine.MSG_FILE_PARSING_FAILED", fileDescription.getPath(), e);
    }
  }

  /**
   *
   * Added by NEY - 22/01/2004 Module Wysiwyg is reused by several modules like publication , ...
   * When you add a wysiwyg content to an object(it 's the case in kmelia), we call the wysiwyg's
   * method index to index the content of the wysiwyg. The name, description and keywords of the
   * object are used by the index method to display them when the wysiwyg will be found by the
   * search engine. Here, this data must be unindexed. But it must not be unstored. If it is
   * unstored, this data will be indexed. So, if we search a word present in one of this data, two
   * elements will be returned by the search engine : - the object - the wysiwyg
   *
   * @param indexEntry
   * @return
   */
  private boolean isWysiwyg(FullIndexEntry indexEntry) {
    return "Wysiwyg".equals(indexEntry.getObjectType())
        && (indexEntry.getComponent().startsWith("kmelia")
        || indexEntry.getComponent().startsWith("kmax"));
  }

  /*
   * The lucene index engine parameters.
   */
  private static int maxFieldLength = 10000;
  private static int mergeFactor = 10;
  private static int maxMergeDocs = Integer.MAX_VALUE;
  private static double RAMBufferSizeMB = IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB;
  // enable the "Did you mean " indexing
  private static boolean enableDymIndexing = false;
  private static String serverName = null;

  static {
    // Reads and set the index engine parameters from the given properties file
    ResourceLocator resource =
        new ResourceLocator("org.silverpeas.search.indexEngine.IndexEngine", "");
    maxFieldLength = resource.getInteger("lucene.maxFieldLength", maxFieldLength);
    mergeFactor = resource.getInteger("lucene.mergeFactor", mergeFactor);
    maxMergeDocs = resource.getInteger("lucene.maxMergeDocs", maxMergeDocs);

    String stringValue = resource.getString("lucene.RAMBufferSizeMB", Double.toString(
        IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB));
    RAMBufferSizeMB = Double.parseDouble(stringValue);

    enableDymIndexing = resource.getBoolean("enableDymIndexing", false);
    serverName = resource.getString("server.name", "Silverpeas");
  }
}
