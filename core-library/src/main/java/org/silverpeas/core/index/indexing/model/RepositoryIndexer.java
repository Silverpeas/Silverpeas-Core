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
package org.silverpeas.core.index.indexing.model;

import org.silverpeas.core.util.file.FileUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An RepositoryIndexer allow to index files in a whole repository except the directories
 */
public class RepositoryIndexer {

  public static final String ADD_ACTION = "add";
  public static final String REMOVE_ACTION = "remove";
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

  public void pathIndexer(String path, String creationDate, String creatorId, String action) {
    pathIndexer(new File(path), creationDate, creatorId, action);
  }

  public void pathIndexer(File path, String creationDate, String creatorId, String action) {
    if (path.isDirectory()) {
      // index directory
      indexDirectory(action, creationDate, creatorId, path);
      // index directory's content
      processFileList(path, creationDate, creatorId, action);
    }

  }

  /**
   * Recursive function which covers directories. For each file, the file is indexed.
   */
  private void processFileList(File dir, String creationDate, String creatorId, String action) {
    if (count % 10000 == 0) {

    }

    File[] dirs = dir.listFiles(DirectorySPFilter.getInstance());
    File[] files = dir.listFiles(FileSPFilter.getInstance());

    List<File> dirList = Arrays.asList(dirs);
    Collections.sort(dirList, FilenameComparator.comparator);

    List<File> fileList = Arrays.asList(files);
    Collections.sort(fileList, FilenameComparator.comparator);

    for (File currentFile : fileList) {
      indexFile(action, creationDate, creatorId, currentFile);
    }
    for (File currentDir : dirList) {
      indexDirectory(action, creationDate, creatorId, currentDir);
      // recursive call to get the current object
      processFileList(currentDir, creationDate, creatorId, action);
    }
  }

  private void indexDirectory(String action, String creationDate, String creatorId, File directory) {
    String unixDirectoty = FilenameUtils.separatorsToUnix(directory.getPath());
    if (ADD_ACTION.equals(action)) {
      // indexer le répertoire
      FullIndexEntry fullIndexEntry = new FullIndexEntry(getComponentId(), "LinkedDir",
          unixDirectoty);
      fullIndexEntry.setTitle(directory.getName());
      fullIndexEntry.setCreationDate(creationDate);
      fullIndexEntry.setCreationUser(creatorId);
      IndexEngineProxy.addIndexEntry(fullIndexEntry);
      count++;
    } else if (REMOVE_ACTION.equals(action)) {
      IndexEntryKey indexEntry = new IndexEntryKey(getComponentId(), "LinkedDir", unixDirectoty);
      IndexEngineProxy.removeIndexEntry(indexEntry);
    }
  }

  public void indexFile(String action, String creationDate, String creatorId, File file) {
    // String path = currentPath + separator + fileName;

    String unixFilePath = FilenameUtils.separatorsToUnix(file.getPath());

    if (ADD_ACTION.equals(action)) {
      String fileName = file.getName();

      // Add file in index
      FullIndexEntry fullIndexEntry =
          new FullIndexEntry(getComponentId(), "LinkedFile", unixFilePath);
      fullIndexEntry.setTitle(fileName);

      boolean haveGotExtension = (fileName.lastIndexOf('.') != -1);

      if (haveGotExtension) {
        fullIndexEntry.setPreView(fileName.substring(0, fileName.lastIndexOf('.')));
      } else {
        fullIndexEntry.setPreView(fileName);
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
    } else if (REMOVE_ACTION.equals(action)) { // Remove file from index
      IndexEntryKey indexEntry = new IndexEntryKey(getComponentId(), "LinkedFile", unixFilePath);
      IndexEngineProxy.removeIndexEntry(indexEntry);
    }
  }
}