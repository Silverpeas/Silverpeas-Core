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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * An RepositoryIndexer allow to index files in a whole repository except the
 * directories
 * 
 */
public class RepositoryIndexer {
  private String spaceId = null;
  private String componentId = null;
  private int count = 0;
  private char separator;
  ResourceLocator resource = null;
  IndexManager indexManager = null;

  public RepositoryIndexer(String spaceId, String componentId) {
    SilverTrace.debug("indexEngine", "RepositoryIndexer.RepositoryIndexer()",
        "root.MSG_GEN_PARAM_VALUE", "spaceId=" + spaceId + " ComponentId="
            + componentId);
    setSpaceId(spaceId);
    setComponentId(componentId);
    resource = new ResourceLocator(
        "com.stratelia.webactiv.util.attachment.mime_types", "fr");
    indexManager = new IndexManager();
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void pathIndexer(String path, String creationDate, String creatorId,
      String action) {
    SilverTrace.debug("indexEngine", "RepositoryIndexer.pathIndexer()",
        "root.MSG_GEN_ENTER_METHOD", "path=" + path);

    separator = path.charAt(0);
    SilverTrace.debug("indexEngine", "RepositoryIndexer.pathIndexer()",
        "root.MSG_GEN_PARAM_VALUE", "separator = " + separator);

    File dir = new File(path);
    if (dir.isDirectory()) {
      // index directory
      indexDirectory(action, creationDate, creatorId, dir);

      // index directory's content
      processFileList(dir, creationDate, creatorId, action);
    }
    indexManager.optimize();
    dir = null;

    SilverTrace.info("indexEngine", "RepositoryIndexer.pathIndexer()",
        "root.MSG_GEN_PARAM_VALUE", "Fichiers à indexer="
            + new Integer(count).toString());
  }

  /**
   * Recursive function which covers directories. For each file, the file is
   * indexed.
   * 
   * @param fileList
   *          an array which contains directories and files
   * @param path
   *          the current path
   * @param currentDirectoryName
   *          the current directory name
   * @param massiveReport
   *          the report to enrich by each upload
   * @return a MassiveReport
   */
  private void processFileList(File dir, String creationDate, String creatorId,
      String action) {
    // SilverTrace.debug("indexEngine", "RepositoryIndexer.processFileList()",
    // "root.MSG_GEN_ENTER_METHOD", "path="+path);

    if (count % 10000 == 0)
      SilverTrace.info("indexEngine", "RepositoryIndexer.processFileList()",
          "root.MSG_GEN_PARAM_VALUE", "# of indexed documents =" + count);

    File[] dirs = dir.listFiles(DirectorySPFilter.getInstance());
    File[] files = dir.listFiles(FileSPFilter.getInstance());

    List dirList = Arrays.asList(dirs);
    Collections.sort(dirList, FilenameComparator.comparator);

    List fileList = Arrays.asList(files);
    Collections.sort(fileList, FilenameComparator.comparator);

    File currentFile = null;
    for (int i = 0; fileList != null && i < fileList.size(); i++) {
      currentFile = (File) fileList.get(i);

      indexFile(action, creationDate, creatorId, currentFile, false);

      currentFile = null;
    }

    File currentDir = null;
    for (int i = 0; dirList != null && i < dirList.size(); i++) {
      currentDir = (File) dirList.get(i);

      indexDirectory(action, creationDate, creatorId, currentDir);

      // recursive call to get the current object
      processFileList(currentDir, creationDate, creatorId, action);

      currentDir = null;
    }

    dirList = null;
    fileList = null;
    dirs = null;
    files = null;

    /*
     * File[] fileList = dir.listFiles();
     * 
     * File currentFile = null; //String fileName = null; for (int i=0; fileList
     * != null && i<fileList.length; i++) { //SilverTrace.debug("indexEngine",
     * "RepositoryIndexer.processFileList()", "root.MSG_GEN_PARAM_VALUE",
     * "fileList["+i+"]="+fileList[i]); //fileName = fileList[i]; //currentFile
     * = new File(path + separator + fileName); currentFile = fileList[i];
     * //SilverTrace.debug("indexEngine", "RepositoryIndexer.processFileList()",
     * "root.MSG_GEN_PARAM_VALUE", "currentFile="+currentFile.getPath());
     * 
     * if (!currentFile.isDirectory()) { //SilverTrace.debug("indexEngine",
     * "RepositoryIndexer.processFileList()", "root.MSG_GEN_PARAM_VALUE",
     * "(IsDirectory false) path+fileName="
     * +path+fileName+", action="+action+", currentFile="
     * +currentFile.getPath()); //It's a document, index or remove it !
     * indexFile(action, creationDate, creatorId, currentFile, false); } else {
     * indexDirectory(action, creationDate, creatorId, currentFile);
     * 
     * // recursive call to get the current object
     * //SilverTrace.debug("indexEngine", "RepositoryIndexer.processFileList()",
     * "root.MSG_GEN_PARAM_VALUE",
     * "RecursiveCall: (IsDirectory true): path+fileName"+path+fileName);
     * processFileList(currentFile, creationDate, creatorId, action); }
     * 
     * currentFile = null; } fileList = null;
     */
  }

  private void indexDirectory(String action, String creationDate,
      String creatorId, File directory) {
    if (action.equals("add")) {
      // indexer le répertoire
      FullIndexEntry fullIndexEntry = new FullIndexEntry(getComponentId(),
          "LinkedDir", directory.getPath());
      fullIndexEntry.setTitle(directory.getName());
      fullIndexEntry.setCreationDate(creationDate);
      fullIndexEntry.setCreationUser(creatorId);

      // IndexEngineProxy.addIndexEntry(fullIndexEntry);
      indexManager.addIndexEntry(fullIndexEntry);

      // SilverTrace.debug("indexEngine", "RepositoryIndexer.indexDirectory()",
      // "root.MSG_GEN_PARAM_VALUE",
      // "Add index Répository : name = "+directory.getName());
      count++;
    } else if (action.equals("remove")) {
      IndexEntryPK indexEntry = new IndexEntryPK(getComponentId(), "LinkedDir",
          directory.getPath());

      // IndexEngineProxy.removeIndexEntry(indexEntry);
      indexManager.removeIndexEntry(indexEntry);
    }
  }

  public void indexFile(String action, String creationDate, String creatorId,
      File file) {
    indexFile(action, creationDate, creatorId, file, true);
  }

  public void indexFile(String action, String creationDate, String creatorId,
      File file, boolean closeIndex) {
    // String path = currentPath + separator + fileName;

    String filePath = file.getPath();

    if (action.equals("add")) {
      String fileName = file.getName();

      // Add file in index
      FullIndexEntry fullIndexEntry = new FullIndexEntry(getComponentId(),
          "LinkedFile", filePath);
      fullIndexEntry.setTitle(fileName);

      boolean haveGotExtension = (fileName.lastIndexOf(".") != -1);

      if (haveGotExtension)
        fullIndexEntry.setPreView(fileName.substring(0, fileName
            .lastIndexOf(".")));
      else
        fullIndexEntry.setPreView(fileName);

      fullIndexEntry.setCreationDate(creationDate);
      fullIndexEntry.setCreationUser(creatorId);

      if (haveGotExtension && !fileName.startsWith("~")) {
        String encoding = null;
        String format = getMimeType(fileName);
        String lang = "fr";

        fullIndexEntry.addFileContent(filePath, encoding, format, lang);
      }

      // IndexEngineProxy.addIndexEntry(fullIndexEntry);
      indexManager.addIndexEntry(fullIndexEntry);
      count++;
    } else if (action.equals("remove"))
    // Remove file from index
    {
      IndexEntryPK indexEntry = new IndexEntryPK(getComponentId(),
          "LinkedFile", filePath);

      // IndexEngineProxy.removeIndexEntry(indexEntry);
      indexManager.removeIndexEntry(indexEntry);
    }

    if (closeIndex)
      indexManager.optimize();
  }

  private String getMimeType(String fileName) {
    String mimeType = null;

    String fileExtension = FileRepositoryManager.getFileExtension(fileName);
    if (resource != null && fileExtension != null)
      mimeType = resource.getString(fileExtension.toLowerCase());

    fileExtension = null;

    return mimeType;
  }
}