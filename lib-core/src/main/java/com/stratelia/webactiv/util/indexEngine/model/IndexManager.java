/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.indexEngine.model;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.indexEngine.parser.Parser;
import com.stratelia.webactiv.util.indexEngine.parser.ParserManager;

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
  public static final String STARTDATE = "startDate";
  public static final String ENDDATE = "endDate";
  public static final String HEADER = "header";
  public static final String CONTENT = "content";
  public static final String THUMBNAIL = "thumbnail";
  public static final String THUMBNAIL_MIMETYPE = "thumbnailMimeType";
  public static final String THUMBNAIL_DIRECTORY = "thumbnailDirectory";

  /**
   * Exhaustive list of indexation's operations Used by objects which must be indexed
   */
  public static final int NONE = -1;
  public static final int ADD = 0;
  public static final int REMOVE = 1;
  public static final int READD = 2;

  private Hashtable indexWriters = new Hashtable();

  /**
   * The constructor takes no parameters and all the index engine parameters are taken from the
   * properties file "com/stratelia/webactiv/util/indexEngine/indexEngine.properties".
   */
  public IndexManager() {
    getProperties("com.stratelia.webactiv.util.indexEngine.IndexEngine");

    SilverTrace.debug("indexEngine", "IndexManager",
        "indexEngine.INFO_INDEX_ENGINE_STARTED", "maxFieldLength="
        + maxFieldLength + ", mergeFactor=" + mergeFactor
        + ", maxMergeDocs=" + maxMergeDocs);
  }

  /**
   * Add an entry index.
   */
  public void addIndexEntry(FullIndexEntry indexEntry) {
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
    SilverTrace.debug("indexEngine", "IndexManager",
        "indexEngine.INFO_STARTS_INDEX_OPTIMIZATION",
        "# of index to optimize = " + indexWriters.size());

    Iterator writerPaths = indexWriters.entrySet().iterator();
    while (writerPaths.hasNext()) {
      Map.Entry writerEntry = (Map.Entry) writerPaths.next();

      String writerPath = (String) writerEntry.getKey();

      SilverTrace.debug("indexEngine", "IndexManager",
          "indexEngine.INFO_STARTS_INDEX_OPTIMIZATION", "writerPath = "
          + writerPath);

      if (writerPath != null) {
        // IndexWriter writer = (IndexWriter) indexWriters.get(writerPath);
        IndexWriter writer = (IndexWriter) writerEntry.getValue();

        if (writer != null) {
          SilverTrace.debug("indexEngine", "IndexManager.optimize()",
              "root_MSG_GEN_PARAM_VALUE", "try to optimize " + writerPath);

          // First, optimize
          try {
            writer.optimize();
          } catch (IOException e) {
            SilverTrace.error("indexEngine", "IndexManager.optimize()",
                "indexEngine.MSG_INDEX_OPTIMIZATION_FAILED",
                "Can't optimize index " + writerPath, e);
          }

          SilverTrace.info("indexEngine", "IndexManager.optimize()",
              "root_MSG_GEN_PARAM_VALUE", "# of documents indexed in "
              + writerPath + " = " + writer.docCount());

          // Then, close the writer
          try {
            writer.close();
          } catch (IOException e) {
            SilverTrace.error("indexEngine", "IndexManager.optimize()",
                "indexEngine.MSG_INDEX_OPTIMIZATION_FAILED",
                "Can't Close index " + writerPath, e);
          }
        }
        writerPaths.remove();
      }
    }
  }

  private void removeIndexEntry(IndexWriter writer, IndexEntryPK indexEntry) {
    Term term = new Term(KEY, indexEntry.toString());
    try {
      writer.deleteDocuments(term);
    } catch (Exception e) {
      SilverTrace.error("indexEngine", "IndexManager",
          "indexEngine.MSG_REMOVE_REQUEST_FAILED", indexEntry.toString(), e);
    }
    SilverTrace.debug("indexEngine", "IndexManager",
        "indexEngine.INFO_REMOVE_REQUEST_SUCCEED", indexEntry.toString());
  }

  /**
   * Remove an entry index.
   */
  public void removeIndexEntry(IndexEntryPK indexEntry) {
    String indexPath = getIndexDirectoryPath(indexEntry);
    IndexWriter writer = getIndexWriter(indexPath, "");
    ;
    if (writer != null) {
      removeIndexEntry(writer, indexEntry);
    } else {
      SilverTrace.debug("indexEngine", "IndexManager",
          "indexEngine.MSG_UNKNOWN_INDEX_FILE", indexPath);
    }
  }

  /**
   * Return the path to the directory where are stored the index for the given index entry.
   */
  public String getIndexDirectoryPath(IndexEntry indexEntry) {
    return getIndexDirectoryPath(indexEntry.getPK());
  }

  /**
   * Return the path to the directory where are stored the index for the given index entry.
   */
  public String getIndexDirectoryPath(IndexEntryPK indexEntry) {
    return getIndexDirectoryPath(null, indexEntry.getComponent());
  }

  /**
   * Return the path to the directory where are stored the index for the given index entry.
   */
  public String getIndexDirectoryPath(String space, String component) {
    String path = FileRepositoryManager.getAbsoluteIndexPath(space, component);

    return path;
  }

  /**
   * Return the analyzer used to parse indexed texts and queries in the locale language.
   */
  public Analyzer getAnalyzer() throws IOException {
    return getAnalyzer(null);
  }

  /**
   * Return the analyzer used to parse indexed texts and queries in the given language.
   * @param language the language used in a document or a query.
   * @return the analyzer for the required language or a default analyzer.
   */
  public Analyzer getAnalyzer(String language) {
    Analyzer analyzer = WAAnalyzer.getAnalyzer(language);
    if (analyzer == null)
      analyzer = new StandardAnalyzer();
    return analyzer;
  }

  /**
   * Get the reader specific of the file described by the file description
   */
  public Reader getReader(FileDescription file) {
    SilverTrace.debug("indexEngine", "IndexManager.getReader",
        "root.MSG_GEN_ENTER_METHOD");
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
   * Reads and set the index engine parameters from the given properties file
   */
  private void getProperties(String propertiesFileName) {
    ResourceLocator resource = null;
    String stringValue = null;

    try {
      resource = new ResourceLocator(propertiesFileName, "");
    } catch (MissingResourceException e) {
    }

    if (resource != null) {
      try {
        stringValue = resource.getString("lucene.maxFieldLength");
        maxFieldLength = Integer.parseInt(stringValue);
      } catch (MissingResourceException e) {
      } catch (NumberFormatException e) {
      }

      try {
        stringValue = resource.getString("lucene.mergeFactor");
        mergeFactor = Integer.parseInt(stringValue);
      } catch (MissingResourceException e) {
      } catch (NumberFormatException e) {
      }

      try {
        stringValue = resource.getString("lucene.maxMergeDocs");
        maxMergeDocs = Integer.parseInt(stringValue);
      } catch (MissingResourceException e) {
      } catch (NumberFormatException e) {
      }

      try {
        stringValue = resource.getString("lucene.RAMBufferSizeMB", Double
            .toString(IndexWriter.DEFAULT_RAM_BUFFER_SIZE_MB));
        RAMBufferSizeMB = Double.parseDouble(stringValue);
      } catch (MissingResourceException e) {
      } catch (NumberFormatException e) {
      }
    }

  }

  /**
   * Returns an IndexWriter to the index stored at the given path. The index directory and files are
   * created if not found.
   * @param path the path to the index root directory
   * @param language the language of the indexed documents.
   * @return an IndexWriter or null if the index can't be found or create or read.
   */
  private IndexWriter getIndexWriter(String path, String language) {
    IndexWriter writer = (IndexWriter) indexWriters.get(path);
    if (writer == null) {
      try {
        boolean createIndex = false;

        File file = new File(path);
        if (!file.exists()) {
          file.mkdirs();
          createIndex = true;
        } else {
          createIndex = !IndexReader.indexExists(file);
        }

        writer = new IndexWriter(path, getAnalyzer(language), createIndex);
        writer.setMaxFieldLength(maxFieldLength);
        writer.setMergeFactor(mergeFactor);
        writer.setMaxMergeDocs(maxMergeDocs);
        writer.setRAMBufferSizeMB(RAMBufferSizeMB);
      } catch (Exception e) {
        if (writer != null) {
          try {
            writer.close();
          } catch (Exception e2) {
            SilverTrace.error("indexEngine", "IndexManager.getIndexWriter",
                "indexEngine.MSG_INDEX_OPTIMIZATION_FAILED",
                "Can't Close Index Writer" + path, e2);
          }
        }
        writer = null;
        SilverTrace.error("indexEngine", "IndexManager.getIndexWriter",
            "indexEngine.MSG_UNKNOWN_INDEX_FILE", path, e);
      }
      if (writer != null)
        indexWriters.put(path, writer);
    }
    return writer;
  }

  /**
   * Method declaration
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
    doc.add(new Field(KEY, indexEntry.getPK().toString(), Field.Store.YES,
        Field.Index.UN_TOKENIZED));
    Iterator languages = indexEntry.getLanguages();
    if (indexEntry.getObjectType() != null
        && indexEntry.getObjectType().startsWith("Attachment")) {
      doc.add(new Field(getFieldName(TITLE, indexEntry.getLang()), indexEntry
          .getTitle(indexEntry.getLang()), Field.Store.YES,
          Field.Index.UN_TOKENIZED));
    } else {
      while (languages.hasNext()) {
        String language = (String) languages.next();
        if (indexEntry.getTitle(language) != null) {
          doc.add(new Field(getFieldName(TITLE, language), indexEntry
              .getTitle(language), Field.Store.YES, Field.Index.TOKENIZED));
          doc.add(new Field(getFieldName(TITLE, language), indexEntry
              .getTitle(language), Field.Store.YES, Field.Index.UN_TOKENIZED));
        }
      }
    }

    // index description
    // doc.add(new Field(PREVIEW, indexEntry.getPreView(), Field.Store.YES,
    // Field.Index.TOKENIZED));
    languages = indexEntry.getLanguages();
    while (languages.hasNext()) {
      String language = (String) languages.next();
      if (indexEntry.getPreview(language) != null)
        doc.add(new Field(getFieldName(PREVIEW, language), indexEntry
            .getPreview(language), Field.Store.YES, Field.Index.TOKENIZED));
      if (indexEntry.getKeywords(language) != null)
        doc.add(new Field(getFieldName(KEYWORDS, language), indexEntry
            .getKeywords(language), Field.Store.YES, Field.Index.NO));
    }

    // doc.add(new Field(KEYWORDS, indexEntry.getKeyWords(), Field.Store.YES,
    // Field.Index.NO));
    doc.add(new Field(CREATIONDATE, indexEntry.getCreationDate(),
        Field.Store.YES, Field.Index.UN_TOKENIZED));
    doc.add(new Field(CREATIONUSER, indexEntry.getCreationUser(),
        Field.Store.YES, Field.Index.UN_TOKENIZED));
    doc.add(new Field(STARTDATE, indexEntry.getStartDate(), Field.Store.YES,
        Field.Index.UN_TOKENIZED));
    doc.add(new Field(ENDDATE, indexEntry.getEndDate(), Field.Store.YES,
        Field.Index.UN_TOKENIZED));
    if (indexEntry.getThumbnail() != null
        && indexEntry.getThumbnailMimeType() != null) {
      doc.add(new Field(THUMBNAIL, indexEntry.getThumbnail(), Field.Store.YES,
          Field.Index.NO));
      doc.add(new Field(THUMBNAIL_MIMETYPE, indexEntry.getThumbnailMimeType(),
          Field.Store.YES, Field.Index.NO));
      doc.add(new Field(THUMBNAIL_DIRECTORY,
          indexEntry.getThumbnailDirectory(), Field.Store.YES, Field.Index.NO));
    }

    String componentId = indexEntry.getComponent();

    if (indexEntry.isIndexId())
      doc.add(new Field(CONTENT, indexEntry.getObjectId(), Field.Store.NO,
          Field.Index.UN_TOKENIZED));

    if ("Wysiwyg".equals(indexEntry.getObjectType())
        && (componentId.startsWith("kmelia") || componentId.startsWith("kmax"))) {
      // Added by NEY - 22/01/2004
      // module Wysiwyg is reuse by several modules like publication,...
      // When you add a wysiwyg content to an object (it's the case in kmelia),
      // we call the wysiwyg's method index to index the content of the wysiwyg.
      // The name, description and keywords of the object are used by the index
      // method
      // to display them when the wysiwyg will be found by the search engine.
      // Here, this data must be unindexed. But it must not be unstored.
      // If it is unstored, this data will be indexed.
      // So, if we search a word present in one of this data, two elements will
      // be returned by the search engine :
      // - the object
      // - the wysiwyg
    } else {
      if (indexEntry.getObjectType() != null
          && indexEntry.getObjectType().startsWith("Attachment")) {
        doc.add(new Field(getFieldName(HEADER, indexEntry.getLang()),
            indexEntry.getTitle(indexEntry.getLang()).toLowerCase(),
            Field.Store.NO, Field.Index.UN_TOKENIZED));
      } else {
        languages = indexEntry.getLanguages();
        while (languages.hasNext()) {
          String language = (String) languages.next();
          if (indexEntry.getTitle(language) != null) {
            doc.add(new Field(getFieldName(HEADER, language), indexEntry
                .getTitle(language).toLowerCase(), Field.Store.NO,
                Field.Index.TOKENIZED));
            doc.add(new Field(getFieldName(HEADER, language), indexEntry
                .getTitle(language).toLowerCase(), Field.Store.NO,
                Field.Index.UN_TOKENIZED));
          }
        }
      }
      languages = indexEntry.getLanguages();
      while (languages.hasNext()) {
        String language = (String) languages.next();
        if (indexEntry.getPreview(language) != null)
          doc.add(new Field(getFieldName(HEADER, language), indexEntry
              .getPreview(language).toLowerCase(), Field.Store.NO,
              Field.Index.TOKENIZED));
        if (indexEntry.getKeywords(language) != null)
          doc.add(new Field(getFieldName(HEADER, language), indexEntry
              .getKeywords(language).toLowerCase(), Field.Store.NO,
              Field.Index.TOKENIZED));
      }
      if (indexEntry.getObjectType() != null
          && indexEntry.getObjectType().startsWith("Attachment"))
        doc.add(new Field(getFieldName(HEADER, indexEntry.getLang()),
            indexEntry.getTitle(indexEntry.getLang()).toLowerCase(),
            Field.Store.NO, Field.Index.UN_TOKENIZED));
      else
        doc.add(new Field(CONTENT, indexEntry.getTitle().toLowerCase(),
            Field.Store.NO, Field.Index.TOKENIZED));
      languages = indexEntry.getLanguages();
      while (languages.hasNext()) {
        String language = (String) languages.next();

        if (indexEntry.getTitle(language) != null)
          doc.add(new Field(getFieldName(CONTENT, language), indexEntry
              .getTitle(language), Field.Store.NO, Field.Index.UN_TOKENIZED));
        if (indexEntry.getPreview(language) != null)
          doc.add(new Field(getFieldName(CONTENT, language), indexEntry
              .getPreview(language).toLowerCase(), Field.Store.NO,
              Field.Index.TOKENIZED));
        if (indexEntry.getKeywords(language) != null)
          doc.add(new Field(getFieldName(CONTENT, language), indexEntry
              .getKeywords(language).toLowerCase(), Field.Store.NO,
              Field.Index.TOKENIZED));
      }
    }

    List list1 = indexEntry.getTextContentList();
    for (int i = 0; i < list1.size(); i++) {
      TextDescription t = (TextDescription) list1.get(i);
      if (t != null) {
        if (t.getContent() != null)
          doc.add(new Field(getFieldName(CONTENT, t.getLang()), t.getContent(),
              Field.Store.NO, Field.Index.TOKENIZED));
      }
    }

    List list2 = indexEntry.getFileContentList();
    for (int i = 0; i < list2.size(); i++) {
      FileDescription f = (FileDescription) list2.get(i);
      addFile(doc, f);
    }

    List list3 = indexEntry.getFields();
    for (int i = 0; i < list3.size(); i++) {
      FieldDescription field = (FieldDescription) list3.get(i);
      if (StringUtil.isDefined(field.getContent()))
        doc.add(new Field(getFieldName(field.getFieldName(), field.getLang()),
            field.getContent(), Field.Store.NO, Field.Index.TOKENIZED));
    }

    if ("Wysiwyg".equals(indexEntry.getObjectType())
        && (componentId.startsWith("kmelia") || componentId.startsWith("kmax"))) {
      // see comments above
    } else {
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
        String language = (String) languages.next();
        if (indexEntry.getTitle(language) != null)
          doc.add(new Field(getFieldName(CONTENT, language), indexEntry
              .getTitle(language).toLowerCase(), Field.Store.NO,
              Field.Index.TOKENIZED));
        if (indexEntry.getPreview(language) != null)
          doc.add(new Field(getFieldName(CONTENT, language), indexEntry
              .getPreview(language).toLowerCase(), Field.Store.NO,
              Field.Index.TOKENIZED));
        if (indexEntry.getKeywords(language) != null)
          doc.add(new Field(getFieldName(CONTENT, language), indexEntry
              .getKeywords(language).toLowerCase(), Field.Store.NO,
              Field.Index.TOKENIZED));
      }
    }
    indexEntry = null;
    return doc;
  }

  private String getFieldName(String name, String language) {
    if (!I18NHelper.isI18N || I18NHelper.isDefaultLanguage(language))
      return name;
    else
      return name + "_" + language;
  }

  /**
   * Add file to Document
   */
  private void addFile(Document doc, FileDescription f) {
    SilverTrace.debug("indexEngine", "IndexManager.addFile",
        "root.MSG_GEN_ENTER_METHOD", "file = " + f.getPath() + ", type = "
        + f.getFormat());
    Reader reader = null;
    try {
      reader = getReader(f);

      SilverTrace.debug("indexEngine", "IndexManager.addFile",
          "root.MSG_GEN_PARAM_VALUE", "reader returned");

      if (reader != null) {
        SilverTrace.debug("indexEngine", "IndexManager.addFile",
            "root.MSG_GEN_PARAM_VALUE", "reader is not null");
        Field fFile = new Field(getFieldName(CONTENT, f.getLang()), reader);
        SilverTrace.debug("indexEngine", "IndexManager.addFile",
            "root.MSG_GEN_PARAM_VALUE", "doc = " + fFile.name() + ", field = "
            + fFile.toString());
        doc.add(fFile);
      }
    } catch (Exception e) {
      SilverTrace.error("indexEngine", "IndexManager",
          "indexEngine.MSG_FILE_PARSING_FAILED", f.getPath(), e);
    }
  }

  /*
   * The lucene index engine parameters.
   */
  private int maxFieldLength = 10000;
  private int mergeFactor = 10;
  private int maxMergeDocs = Integer.MAX_VALUE;
  private double RAMBufferSizeMB = IndexWriter.DEFAULT_RAM_BUFFER_SIZE_MB;

}
