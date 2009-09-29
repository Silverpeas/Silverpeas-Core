package com.stratelia.webactiv.util.indexEngine;

import java.io.File;

import com.stratelia.silverpeas.silverpeasinitialize.IInitialize;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class IndexEngineInitialize implements IInitialize {

  public IndexEngineInitialize() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.stratelia.silverpeas.silverpeasinitialize.IInitialize#Initialize()
   */
  /**
   * Since version 1.3 of Lucene, lock files are stored in the java.io.tmpdir
   * system's property By default on Windows, it's C:\Documents and
   * Settings\neysseri\Local Settings\TEMP and /tmp on Unix
   */
  public boolean Initialize() {
    // Remove all remaining *.lock files in index path
    ResourceLocator resource = new ResourceLocator(
        "com.stratelia.webactiv.general", "");

    String indexPath = resource.getString("uploadsIndexPath");
    String removeLocks = resource.getString("removeLocksOnInit", "");
    if ("yes".equalsIgnoreCase(removeLocks)) {
      String property = System.getProperty("java.io.tmpdir");

      SilverTrace.debug("indexEngine", "IndexEngineInitialize.Initialize()",
          "Removing Locks...(" + property + ")");
      removeLockFiles(new File(property));
      removeLockFiles(new File(indexPath));
      SilverTrace.debug("indexEngine", "IndexEngineInitialize.Initialize()",
          "Locks removed !");
    }
    return true;
  }

  protected void removeLockFiles(File theFile) {
    if (theFile.isDirectory()) {
      File[] list = theFile.listFiles();
      int i = 0;

      while (list != null && i < list.length) {
        removeLockFiles(list[i++]);
      }
    } else {
      if (theFile.isFile() && isLockFile(theFile.getName())) {
        if (!theFile.delete()) {
          SilverTrace.error("indexEngine",
              "IndexEngineInitialize.removeLockFiles",
              "util.EX_DELETE_FILE_ERROR", theFile.getPath());
        } else {
          SilverTrace.debug("indexEngine",
              "IndexEngineInitialize.removeLockFiles", "Lock "
                  + theFile.getPath() + " removed.");
        }
      }
    }
  }

  /**
   * Since version 1.3 of Lucene, lock files have names that start with
   * "lucene-" followed by an MD5 hash of the index directory path. Since
   * version 2.3 of Lucene, lock files are in index dirs and named "write.lock"
   * 
   * @param fileName
   *          - the file to test
   * @return true if the file is a lucene's lock file, false otherwise.
   */
  protected boolean isLockFile(String fileName) {
    // return (("commit.lock".equalsIgnoreCase(fileName)) ||
    // ("write.lock".equalsIgnoreCase(fileName)));
    return fileName.startsWith("lucene-")
        || ("write.lock".equalsIgnoreCase(fileName));
  }
}