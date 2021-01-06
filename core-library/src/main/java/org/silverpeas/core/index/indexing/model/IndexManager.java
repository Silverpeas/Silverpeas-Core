/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.index.indexing.model;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.IndexFileManager;
import org.silverpeas.core.index.indexing.parser.Parser;
import org.silverpeas.core.index.indexing.parser.ParserManager;
import org.silverpeas.core.index.search.SearchEnginePropertiesManager;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.System.currentTimeMillis;
import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;
import static org.silverpeas.core.index.indexing.IndexingLogger.indexingLogger;
import static org.silverpeas.core.index.indexing.model.IndexProcessor.doFlush;
import static org.silverpeas.core.index.indexing.model.IndexProcessor.doRemoveAll;

/**
 * An IndexManager manage all the web'activ's index. An IndexManager is NOT thread safe : to share
 * an IndexManager between several threads use an IndexerThread.
 */
@Technical
@Bean
@Singleton
public class IndexManager {

  /**
   * The fields names used by lucene to store each element of an index entry.
   */
  public static final String ID = "id";
  public static final String KEY = "key";
  public static final String SCOPE = "scope";
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
  public static final String FILENAME = "filename";
  public static final String PATH = "path";
  public static final String ALIAS = "alias";

  /**
   * Exhaustive list of indexation's operations Used by objects which must be indexed
   */
  public static final int NONE = -1;
  public static final int ADD = 0;
  public static final int REMOVE = 1;
  public static final int ADD_AGAIN = 2;
  private static final String ATTACHMENT_PREFIX = "Attachment";
  private static final int DEFAULT_MAX_FIELD_LENGTH = 10000;
  private static final int DEFAULT_MERGE_FACTOR_VALUE = 10;
  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.index.indexing.IndexEngine");
  /*
   * The lucene index engine parameters.
   */
  private static int maxFieldLength = DEFAULT_MAX_FIELD_LENGTH;
  private static int mergeFactor = DEFAULT_MERGE_FACTOR_VALUE;
  private static int maxMergeDocs = Integer.MAX_VALUE;
  private static double defaultRamBufferSizeMb = IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB;
  // enable the "Did you mean " indexing
  private static boolean enableDymIndexing = false;
  private static String serverName = null;
  private final Map<String, IndexWriter> indexWriters = new LinkedHashMap<>();
  @Inject
  private ParserManager parserManager;

  /**
   * The constructor takes no parameters and all the index engine parameters are taken from the
   * properties file "org/silverpeas/util/indexing/indexing.properties".
   */
  private IndexManager() {
  }

  public static IndexManager get() {
    return ServiceProvider.getService(IndexManager.class);
  }

  /**
   * Add an entry index.
   *
   * @param indexEntry
   */
  void addIndexEntry(FullIndexEntry indexEntry) {
    final long start = currentTimeMillis();
    try {
      indexEntry.setServerName(serverName);
      String indexPath = getIndexDirectoryPath(indexEntry);
      IndexWriter writer = getIndexWriter(indexPath, indexEntry.getLang());
      removeIndexEntry(writer, indexEntry.getPK());
      index(writer, indexEntry);
    } finally {
      indexingLogger().debug(() ->
          format("addIndexEntry {0} in {1}", indexEntry.getPK(), formatDurationHMS(currentTimeMillis() - start)));
    }
  }

  /**
   * Optimize all the modified index.
   */
  public void flush() {
    doFlush(() -> {
      final SilverLogger logger = indexingLogger();
      final List<String> pathProcessed = new ArrayList<>(indexWriters.size());
      final Iterator<Map.Entry<String, IndexWriter>> it = indexWriters.entrySet().iterator();
      logger.debug("flushing manager of indexation about {0} writer(s)", indexWriters.size());
      while(it.hasNext()) {
        final Map.Entry<String, IndexWriter> entry = it.next();
        final String path = entry.getKey();
        final IndexWriter writer = entry.getValue();
        pathProcessed.add(path);
        logger.debug("\t- closing writer of path {0}", path);
        try {
          writer.close();
        } catch (IOException e) {
          indexingLogger().error("Cannot close index " + path, e);
        }
        // update the spelling index
        if (enableDymIndexing) {
          DidYouMeanIndexer.createSpellIndexForAllLanguage(CONTENT, path);
        }
        it.remove();
      }
      return pathProcessed;
    });
  }

  private void removeIndexEntry(IndexWriter writer, IndexEntryKey indexEntryKey) {
    Term term = new Term(KEY, indexEntryKey.toString());
    try {
      // removing document according to indexEntryPK
      writer.deleteDocuments(term);
    } catch (IOException e) {
      indexingLogger().error("Index deletion failure: " + indexEntryKey.toString(), e);
    }
  }

  /**
   * Remove an entry index .
   *
   * @param indexEntry
   */
  void removeIndexEntry(IndexEntryKey indexEntry) {
    String indexPath = getIndexDirectoryPath(indexEntry);
    IndexWriter writer = getIndexWriter(indexPath, "");
    if (writer != null) {
      removeIndexEntry(writer, indexEntry);
    }
  }

  private void removeIndexEntries(IndexWriter writer, String scope) {
    Term term = new Term(SCOPE, scope);
    try {
      // removing documents according to SCOPE term
      writer.deleteDocuments(term);
    } catch (IOException e) {
      indexingLogger().error("Index deletion failure for scope : " + scope, e);
    }
  }

  void removeIndexEntries(String scope) {
    String indexPath = getIndexDirectoryPath(scope);
    IndexWriter writer = getIndexWriter(indexPath, "");
    if (writer != null) {
      removeIndexEntries(writer, scope);
    }
  }

  void removeAllIndexEntries() {
    doRemoveAll(() -> {
      flush();
      final File indexRepository = Paths.get(IndexFileManager.getIndexUpLoadPath()).toFile();
      final File savedIndexRepository = Paths.get(indexRepository.getAbsolutePath() + "_" +
          LocalDateTime.now().toString().replaceAll("[:']", "")).toFile();
      if(!indexRepository.renameTo(savedIndexRepository)) {
        throw new SilverpeasRuntimeException("index repository folder can not be renamed, please shutdown the server in order to rename it manually");
      }
    });
  }

  /**
   * Return the path to the directory where are stored the index for the given index entry.
   *
   * @param indexEntry the index entry.
   * @return the path to the directory where are stored the index for the given index entry.
   */
  private String getIndexDirectoryPath(IndexEntry indexEntry) {
    return getIndexDirectoryPath(indexEntry.getPK());
  }

  /**
   * Return the path to the directory where are stored the index for the given index entry.
   *
   * @param indexEntry
   * @return the path to the directory where are stored the index for the given index entry.
   */
  private String getIndexDirectoryPath(IndexEntryKey indexEntry) {
    return getIndexDirectoryPath(indexEntry.getComponent());
  }

  /**
   *
   * Return the path to the directory where are stored the index for the given index entry .
   *
   * @param component
   * @return the path to the directory where are stored the index for the given index entry .
   */
  public String getIndexDirectoryPath(String component) {
    return IndexFileManager.getAbsoluteIndexPath(component);
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
      analyzer = new LimitTokenCountAnalyzer(new StandardAnalyzer(), maxFieldLength);
    }
    return analyzer;
  }

  /**
   * @param file a file description
   * @return the optional {@link Reader} specific of the file described by the file description
   */
  private Optional<Reader> getReader(FileDescription file) {
    final String filePath = file.getPath();
    return ofNullable(file.getFormat())
        .map(f -> hasMimetypeToBeIgnored(filePath, f) ? null : f)
        .flatMap(parserManager::getParser)
        .map(p -> p.getContext(filePath, file.getEncoding()))
        .filter(c -> !c.getMetadata().getValue("Content-Type")
            .map(t -> hasMimetypeToBeIgnored(filePath, t))
            .filter(Boolean.TRUE::equals)
            .isPresent())
        .map(Parser.Context::getReader);
  }

  /**
   * Indicates if the given mime-type has to be ignored.
   * <p>
   * File path is used for logging purpose.
   * </p>
   * @param filePath a file path.
   * @param mimeType the according mime type.
   * @return true if mime type is protected, false otherwise.
   */
  private boolean hasMimetypeToBeIgnored(final String filePath, final String mimeType) {
    final String pattern = settings.getString("index.file.content.mimetype.ignore.pattern", "(?i).*(protected|encrypted).*");
    final boolean hasToBeIgnored = mimeType.matches(pattern);
    if (hasToBeIgnored) {
      indexingLogger().warn("Mimetype {0} must be ignored, removing from indexation the content of file {1}", mimeType, filePath);
    }
    return hasToBeIgnored;
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
    indexWriters.computeIfPresent(path, (s, w) -> w.isOpen() ? w : null);
    return indexWriters.computeIfAbsent(path, p -> {
      try {
        final File file = new File(path);
        if (!file.exists()) {
          file.mkdirs();
        }
        final LogDocMergePolicy policy = new LogDocMergePolicy();
        policy.setMergeFactor(mergeFactor);
        policy.setMaxMergeDocs(maxMergeDocs);
        final IndexWriterConfig configuration =
            new IndexWriterConfig(getAnalyzer(language)).setRAMBufferSizeMB(defaultRamBufferSizeMb)
                .setMergePolicy(policy);
        return new IndexWriter(FSDirectory.open(file.toPath()), configuration);
      } catch (IOException e) {
        indexingLogger().error("Unknown index file " + path, e);
      }
      // The map is not filled
      return null;
    });
  }

  /**
   * Method declaration
   *
   * @param writer
   * @param indexEntry
   */
  private void index(IndexWriter writer, FullIndexEntry indexEntry) {
    try {
      Term key = new Term(KEY, indexEntry.getPK().toString());
      writer.updateDocument(key, makeDocument(indexEntry));
    } catch (Exception e) {
      indexingLogger().error(e.getMessage(), e);
    }
  }

  /**
   * Create a lucene Document object with the given indexEntry.
   */
  private Document makeDocument(FullIndexEntry indexEntry) {
    Document doc = new Document();

    // First enrich indexEntry with files data
    if (StringUtil.isDefined(indexEntry.getObjectId())) {
      ServiceProvider.getAllServices(DocumentIndexing.class)
          .forEach(documentIndexing -> documentIndexing.updateIndexEntryWithDocuments(indexEntry));
    }

    // fields creation
    doc.add(new StringField(KEY, indexEntry.getPK().toString(), Field.Store.YES));
    doc.add(new StringField(SCOPE, indexEntry.getPK().getComponent(), Field.Store.YES));
    setTitleField(indexEntry, doc);
    setPreviewAndKeyWordsField(indexEntry, doc);
    setCreationAndUpdateFields(indexEntry, doc);
    setThumbnailField(indexEntry, doc);
    setContentIdField(indexEntry, doc);
    setHeaderFields(indexEntry, doc);
    setContentFields(indexEntry, doc);
    setContentTextField(indexEntry, doc);
    setAlias(indexEntry, doc);
    setFileRelativeFields(indexEntry, doc);
    setAdditionalFields(indexEntry, doc);

    // Add server name inside Lucene doc
    doc.add(new StringField(SERVER_NAME, indexEntry.getServerName(), Field.Store.YES));

    return doc;
  }

  private void setAlias(final FullIndexEntry indexEntry, final Document doc) {
    if (indexEntry.getPaths() != null) {
      for (String path : indexEntry.getPaths()) {
        doc.add(new StringField(PATH, path, Field.Store.YES));
      }
    }
    doc.add(new Field(ALIAS, Boolean.toString(indexEntry.isAlias()), TextField.TYPE_STORED));
  }

  private void setAdditionalFields(final FullIndexEntry indexEntry, final Document doc) {
    List<FieldDescription> list3 = indexEntry.getFields();
    List<String> fieldsForFacets = new ArrayList<>(list3.size());
    for (FieldDescription field : list3) {
      if (StringUtil.isDefined(field.getContent())) {
        // if a field is used for the sort or to generate a facet, it's stored in the lucene index
        String fieldName = getFieldName(field.getFieldName(), field.getLang());
        Field.Store storeAction;
        if (field.isStored() || SearchEnginePropertiesManager.getFieldsNameList().contains(field.
            getFieldName())) {
          storeAction = Field.Store.YES;
          fieldsForFacets.add(fieldName);
        } else {
          storeAction = Field.Store.NO;
        }
        doc.add(new TextField(fieldName, field.getContent(), storeAction));
      }
    }
    if (!fieldsForFacets.isEmpty()) {
      String stringForFacets = buildStringForFacets(fieldsForFacets);
      // adds all fields which generate facets
      doc.add(new StringField(FIELDS_FOR_FACETS, stringForFacets, Field.Store.YES));
    }
  }

  private void setFileRelativeFields(final FullIndexEntry indexEntry, final Document doc) {
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
      doc.add(new StringField(EMBEDDED_FILE_IDS, linkedFileId, Field.Store.YES));
    }
  }

  private void setContentTextField(final FullIndexEntry indexEntry, final Document doc) {
    List<TextDescription> list1 = indexEntry.getTextContentList();
    for (TextDescription t : list1) {
      if (t != null && t.getContent() != null) {
        doc.add(new Field(getFieldName(CONTENT, t.getLang()), t.getContent(),
            TextField.TYPE_NOT_STORED));
      }
    }
  }

  private void setContentFields(final FullIndexEntry indexEntry, final Document doc) {
    final Iterator<String> languages;
    if (indexEntry.getObjectType() != null &&
        indexEntry.getObjectType().startsWith(ATTACHMENT_PREFIX)) {
      String lang = indexEntry.getLang();
      if (indexEntry.getTitle(lang) != null) {
        addToDocContent(indexEntry.getTitle(lang), doc, lang);
      }
      addToDocContent(indexEntry.getFilename(), doc, lang);
    } else {
      addToDocContent(indexEntry.getTitle(), doc);
    }
    languages = indexEntry.getLanguages();
    while (languages.hasNext()) {
      String language = languages.next();

      if (indexEntry.getTitle(language) != null) {
        addToDocContent(indexEntry.getTitle(language), doc, language);
      }
      if (indexEntry.getPreview(language) != null) {
        addToDocContent(indexEntry.getPreview(language), doc, language);
      }
      if (indexEntry.getKeywords(language) != null) {
        addToDocContent(indexEntry.getKeywords(language), doc, language);
      }
    }
  }

  private void addToDocContent(String value, Document doc) {
    addToDocContent(CONTENT, value, doc);
  }

  private void addToDocContent(String value, Document doc, String lang) {
    String fieldName = getFieldName(CONTENT, lang);
    addToDocContent(fieldName, value, doc);
  }

  private void addToDocContent(String fieldName, String value, Document doc) {
    doc.add(new Field(fieldName, value, TextField.TYPE_NOT_STORED));
    doc.add(new Field(fieldName, value.replaceAll("[_-]", " "), TextField.TYPE_NOT_STORED));
  }

  private void setHeaderFields(final FullIndexEntry indexEntry, final Document doc) {
    if (indexEntry.getObjectType() != null && indexEntry.getObjectType().startsWith(
        ATTACHMENT_PREFIX)) {
      String lang = indexEntry.getLang();
      if (indexEntry.getTitle(lang) != null) {
        doc.add(new Field(getFieldName(HEADER, lang), indexEntry.getTitle(lang),
            TextField.TYPE_NOT_STORED));
      }
      doc.add(new Field(getFieldName(HEADER, lang), indexEntry.getFilename(),
          TextField.TYPE_NOT_STORED));
      doc.add(new Field(getFieldName(HEADER, lang), indexEntry.getFilename(),
          TextField.TYPE_NOT_STORED));
    } else {
      Iterator<String> languages = indexEntry.getLanguages();
      while (languages.hasNext()) {
        String language = languages.next();
        if (indexEntry.getTitle(language) != null) {
          doc.add(new Field(getFieldName(HEADER, language), indexEntry.getTitle(language),
              TextField.TYPE_NOT_STORED));
        }
      }
    }
    Iterator<String> languages = indexEntry.getLanguages();
    while (languages.hasNext()) {
      String language = languages.next();
      if (indexEntry.getPreview(language) != null) {
        doc.add(new Field(getFieldName(HEADER, language), indexEntry.getPreview(language),
            TextField.TYPE_NOT_STORED));
      }
      if (indexEntry.getKeywords(language) != null) {
        doc.add(new Field(getFieldName(HEADER, language), indexEntry.getKeywords(language),
            TextField.TYPE_NOT_STORED));
      }
    }
  }

  private void setContentIdField(final FullIndexEntry indexEntry, final Document doc) {
    if (indexEntry.isIndexId()) {
      doc.add(new Field(CONTENT, indexEntry.getObjectId(), TextField.TYPE_NOT_STORED));
    }
  }

  private void setThumbnailField(final FullIndexEntry indexEntry, final Document doc) {
    if (indexEntry.getThumbnail() != null && indexEntry.getThumbnailMimeType() != null) {
      doc.add(new StringField(THUMBNAIL, indexEntry.getThumbnail(), Field.Store.YES));
      doc.add(
          new StringField(THUMBNAIL_MIMETYPE, indexEntry.getThumbnailMimeType(), Field.Store.YES));
      doc.add(new StringField(THUMBNAIL_DIRECTORY, indexEntry.getThumbnailDirectory(),
          Field.Store.YES));
    }
  }

  private void setCreationAndUpdateFields(final FullIndexEntry indexEntry, final Document doc) {
    doc.add(new StringField(CREATIONDATE, indexEntry.getCreationDate(), Field.Store.YES));
    doc.add(new StringField(CREATIONUSER, indexEntry.getCreationUser(), Field.Store.YES));
    doc.add(new StringField(LASTUPDATEDATE, indexEntry.getLastModificationDate(), Field.Store.YES));
    doc.add(new StringField(LASTUPDATEUSER, indexEntry.getLastModificationUser(), Field.Store.YES));
    doc.add(new StringField(STARTDATE, indexEntry.getStartDate(), Field.Store.YES));
    doc.add(new StringField(ENDDATE, indexEntry.getEndDate(), Field.Store.YES));
  }

  private void setPreviewAndKeyWordsField(final FullIndexEntry indexEntry, final Document doc) {
    Iterator<String> languages = indexEntry.getLanguages();
    while (languages.hasNext()) {
      String language = languages.next();
      if (indexEntry.getPreview(language) != null) {
        doc.add(new Field(getFieldName(PREVIEW, language), indexEntry.getPreview(language),
            TextField.TYPE_STORED));
      }
      if (indexEntry.getKeywords(language) != null) {
        doc.add(new Field(getFieldName(KEYWORDS, language), indexEntry.getKeywords(language),
            TextField.TYPE_NOT_STORED));
      }
    }
  }

  private void setTitleField(final FullIndexEntry indexEntry, final Document doc) {
    Iterator<String> languages = indexEntry.getLanguages();
    if (indexEntry.getObjectType() != null && indexEntry.getObjectType().startsWith(
        ATTACHMENT_PREFIX)) {
      String lang = indexEntry.getLang();
      if (StringUtil.isDefined(indexEntry.getTitle(lang))) {
        doc.add(
            new Field(getFieldName(TITLE, lang), indexEntry.getTitle(lang), TextField.TYPE_STORED));
      }
      doc.add(
          new Field(getFieldName(FILENAME, lang), indexEntry.getFilename(), TextField.TYPE_STORED));
    } else {
      while (languages.hasNext()) {
        String language = languages.next();
        if (indexEntry.getTitle(language) != null) {
          doc.add(new Field(getFieldName(TITLE, language), indexEntry.getTitle(language),
              TextField.TYPE_STORED));
        }
      }
    }
  }

  private String buildStringForFacets(List<String> fieldsForFacets) {
    String fieldsForFacet = "";
    if (fieldsForFacets != null && !fieldsForFacets.isEmpty()) {
      fieldsForFacet = StringUtil.join(fieldsForFacets, ',');
    }
    return fieldsForFacet;
  }

  private String getFieldName(String name, String language) {
    if (!I18NHelper.isI18nContentActivated || I18NHelper.isDefaultLanguage(language)) {
      return name;
    }
    return name + "_" + language;
  }

  /**
   * Add file to Document
   */
  private void addFile(Document doc, FileDescription fileDescription) {
    File file = new File(fileDescription.getPath());
    if (!file.exists() || !file.isFile()) {
      return;
    }
    try {
      getReader(fileDescription).ifPresent(r -> {
        final Field field = new Field(getFieldName(CONTENT, fileDescription.getLang()), r, TextField.TYPE_NOT_STORED);
        doc.add(field);
      });
    } catch (RuntimeException e) {
      indexingLogger().error("Failed to parse file " + fileDescription.getPath(), e);
    }
  }

  static {
    // Reads and set the index engine parameters from the given properties file
    maxFieldLength = settings.getInteger("lucene.maxFieldLength", maxFieldLength);
    mergeFactor = settings.getInteger("lucene.mergeFactor", mergeFactor);
    maxMergeDocs = settings.getInteger("lucene.maxMergeDocs", maxMergeDocs);

    String stringValue = settings.getString("lucene.RAMBufferSizeMB", Double.toString(
        IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB));
    defaultRamBufferSizeMb = Double.parseDouble(stringValue);

    enableDymIndexing = settings.getBoolean("enableDymIndexing", false);
    serverName = settings.getString("server.name", "Silverpeas");
  }
}
