/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.util.file.FileUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.silverpeas.core.index.indexing.IndexingLogger.indexingLogger;

/**
 * An RepositoryIndexer allow to index files in a whole repository except the directories
 */
public class RepositoryIndexer {

  private static final String ADD_ACTION = "add";
  private static final String REMOVE_ACTION = "remove";
  private String spaceId = null;
  private String componentId = null;
  private int count = 0;

  public RepositoryIndexer(String spaceId, String componentId) {
    this.spaceId = spaceId;
    this.componentId = componentId;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void addPath(Path path, String creatorId) {
    performPath(path, LocalDate.now(), creatorId, ADD_ACTION);
  }

  public void removePath(Path path, String creatorId) {
    performPath(path, LocalDate.now(), creatorId, REMOVE_ACTION);
  }

  private void performPath(Path path, LocalDate creationDate, String creatorId, String action) {
    if (Files.isDirectory(path)) {
      // index directory
      indexDirectory(path, creationDate, creatorId, action);
      // index directory's content
      processFileList(path, creationDate, creatorId, action);
    } else if (Files.exists(path)) {
      // index file
      indexFile(path.toFile(), creationDate, creatorId, action);
    }
  }

  /**
   * Recursive function which covers directories. For each file, the file is indexed.
   */
  private void processFileList(Path dir, LocalDate creationDate, String creatorId, String action) {
    if (count % 10000 == 0) {
      indexingLogger().debug("# of indexed documents = {0}", count);
    }

    File[] dirs = dir.toFile().listFiles(DirectorySPFilter.getInstance());
    File[] files = dir.toFile().listFiles(FileSPFilter.getInstance());

    List<File> dirList = Arrays.asList(dirs != null ? dirs : new File[0]);
    dirList.sort(FilenameComparator.comparator);

    List<File> fileList = Arrays.asList(files != null ? files : new File[0]);
    fileList.sort(FilenameComparator.comparator);

    for (File currentFile : fileList) {
      indexFile(currentFile, creationDate, creatorId, action);
    }
    for (File currentDir : dirList) {
      final Path currentDirectoryPath = currentDir.toPath();
      indexDirectory(currentDirectoryPath, creationDate, creatorId, action);
      // recursive call to get the current object
      processFileList(currentDirectoryPath, creationDate, creatorId, action);
    }
  }

  private void indexDirectory(Path directory, LocalDate creationDate, String creatorId,
      String action) {
    String unixDirectory = FilenameUtils.separatorsToUnix(directory.toString());
    if (ADD_ACTION.equals(action)) {
      // indexer le répertoire
      FullIndexEntry fullIndexEntry =
          new FullIndexEntry(getComponentId(), "LinkedDir", unixDirectory);
      fullIndexEntry.setTitle(directory.toFile().getName());
      fullIndexEntry.setCreationDate(creationDate);
      fullIndexEntry.setCreationUser(creatorId);
      IndexEngineProxy.addIndexEntry(fullIndexEntry);
      count++;
    } else if (REMOVE_ACTION.equals(action)) {
      IndexEntryKey indexEntry = new IndexEntryKey(getComponentId(), "LinkedDir", unixDirectory);
      IndexEngineProxy.removeIndexEntry(indexEntry);
    }
  }

  private void indexFile(File file, LocalDate creationDate, String creatorId, String action) {

    String unixFilePath = FilenameUtils.separatorsToUnix(file.getPath());

    if (ADD_ACTION.equals(action)) {
      String fileName = file.getName();

      // Add file in index
      FullIndexEntry fullIndexEntry =
          new FullIndexEntry(getComponentId(), "LinkedFile", unixFilePath);
      fullIndexEntry.setTitle(fileName);

      boolean haveGotExtension = (fileName.lastIndexOf('.') != -1);

      if (haveGotExtension) {
        fullIndexEntry.setPreview(fileName.substring(0, fileName.lastIndexOf('.')));
      } else {
        fullIndexEntry.setPreview(fileName);
      }

      fullIndexEntry.setCreationDate(creationDate);
      fullIndexEntry.setCreationUser(creatorId);

      if (haveGotExtension && !fileName.startsWith("~")) {
        String format = FileUtil.getMimeType(fileName);
        String lang = "fr";
        fullIndexEntry.addFileContent(unixFilePath, null, format, lang);
      }
      IndexEngineProxy.addIndexEntry(fullIndexEntry);
      count++;
    } else if (REMOVE_ACTION.equals(action)) {
      // Remove file from index
      IndexEntryKey indexEntry = new IndexEntryKey(getComponentId(), "LinkedFile", unixFilePath);
      IndexEngineProxy.removeIndexEntry(indexEntry);
    }
  }
}
