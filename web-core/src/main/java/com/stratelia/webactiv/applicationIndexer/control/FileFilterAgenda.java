/**
 * 
 */
package com.stratelia.webactiv.applicationIndexer.control;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Allows filtering the responses when reading an directory content. Detects the directory of agenda
 * component.
 */
public class FileFilterAgenda implements FilenameFilter {

  /**
   * 
   */
  public FileFilterAgenda() {
  }

  /* (non-Javadoc)
   * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
   */
  @Override
  public boolean accept(File dir, String name) {
    if (name.startsWith("user@") && name.endsWith("agenda")) {
      return true;
    }
    return false;
  }
}
