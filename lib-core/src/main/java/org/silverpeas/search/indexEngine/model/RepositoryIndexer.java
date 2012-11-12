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

import com.silverpeas.util.FileUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An RepositoryIndexer allow to index files in a whole repository except the directories
 */
public class RepositoryIndexer {

  public enum IndexerAction {

    add, remove;
  }
  private String spaceId = null;
  private String componentId = null;
  private int count = 0;
  private char separator;
  private IndexManager indexManager = new IndexManager();

  public RepositoryIndexer(String spaceId, String componentId) {
    SilverTrace.debug("indexEngine", "RepositoryIndexer.RepositoryIndexer()",
        "root.MSG_GEN_PARAM_VALUE", "spaceId=" + spaceId + " ComponentId=" + componentId);
    this.spaceId = spaceId;
    this.componentId = componentId;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void pathIndexer(String path, String creationDate, String creatorId, IndexerAction action) {
    SilverTrace.debug("indexEngine", "RepositoryIndexer.pathIndexer()", "root.MSG_GEN_ENTER_METHOD",
        "path=" + path);
    separator = path.charAt(0);
    SilverTrace.debug("indexEngine", "RepositoryIndexer.pathIndexer()", "root.MSG_GEN_PARAM_VALUE",
        "separator = " + separator);
    File dir = new File(path);
    if (dir.isDirectory()) {
      // index directory
      indexDirectory(action, creationDate, creatorId, dir);
      // index directory's content
      processFileList(dir, creationDate, creatorId, action);
    }
    indexManager.optimize();
    SilverTrace.info("indexEngine", "RepositoryIndexer.pathIndexer()",
        "root.MSG_GEN_PARAM_VALUE", "Fichiers à indexer=" + Integer.toString(count));
  }

  /**
   * Recursive function which covers directories. For each file, the file is indexed.
   *
   * @return a MassiveReport
   */
  private void processFileList(File dir, String creationDate, String creatorId, IndexerAction action) {
    if (count % 10000 == 0) {
      SilverTrace.info("indexEngine", "RepositoryIndexer.processFileList()",
          "root.MSG_GEN_PARAM_VALUE", "# of indexed documents =" + count);
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

  private void indexDirectory(IndexerAction action, String creationDate, String creatorId,
      File directory) {
    switch ((action)) {
      case add:
        // indexer le répertoire
        FullIndexEntry fullIndexEntry = new FullIndexEntry(getComponentId(), "LinkedDir",
            directory.getPath());
        fullIndexEntry.setTitle(directory.getName());
        fullIndexEntry.setCreationDate(creationDate);
        fullIndexEntry.setCreationUser(creatorId);
        indexManager.addIndexEntry(fullIndexEntry);
        count++;
        break;
      case remove:
        IndexEntryPK indexEntry = new IndexEntryPK(getComponentId(), "LinkedDir", directory.
            getPath());
        indexManager.removeIndexEntry(indexEntry);
        break;
    }
  }

  public void indexFile(IndexerAction action, String creationDate, String creatorId, File file) {
    String filePath = file.getPath();

    switch (action) {
      case add:
        String fileName = file.getName();
        // Add file in index
        FullIndexEntry fullIndexEntry = new FullIndexEntry(getComponentId(), "LinkedFile", filePath);
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
          fullIndexEntry.addFileContent(filePath, null, format, lang);
        }
        indexManager.addIndexEntry(fullIndexEntry);
        count++;
        break;
      case remove:
        IndexEntryPK indexEntry = new IndexEntryPK(getComponentId(), "LinkedFile", filePath);
        indexManager.removeIndexEntry(indexEntry);
        break;
    }
  }
}