/**
 * 
 */
package com.stratelia.webactiv.applicationIndexer.control;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Allows filtering the responses when reading an directory content. Detects the directory of todo
 * component.
 */
public class FileFilterTodo implements FilenameFilter {

  /**
   * 
   */
  public FileFilterTodo() {
  }

  /* (non-Javadoc)
   * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
   */
  @Override
  public boolean accept(File dir, String name) {
    if (name.startsWith("user@") && name.endsWith("todo")) {
      return true;
    }
    return false;
  }

}
