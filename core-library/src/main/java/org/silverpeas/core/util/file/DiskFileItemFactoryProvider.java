package org.silverpeas.core.util.file;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FileCleaningTracker;

import java.io.File;

/**
 * A provider of a {@link org.apache.commons.fileupload.disk.DiskFileItemFactory} factory. Its goal
 * is to initialize this factory with some default parameters for the particular use
 * of Silverpeas:
 * <ul>
 * <li>the path of the temporary directory in use in Silverpeas,</li>
 * <li>the threshold size above which the uploaded files are temporarily stored in disk.</li>
 * </ul>
 * @author mmoquillon
 */
public class DiskFileItemFactoryProvider {

  private static final int THRESHOLD_SIZE = 2097152;

  /**
   * Provides a {@link DiskFileItemFactory} instance initialized for the Silverpeas use.
   * @return a {@link DiskFileItemFactory} object.
   */
  public DiskFileItemFactory provide() {
    DiskFileItemFactory factory = new DiskFileItemFactory();
    factory.setSizeThreshold(THRESHOLD_SIZE);
    factory.setRepository(new File(FileRepositoryManager.getTemporaryPath()));
    factory.setFileCleaningTracker(new FileCleaningTracker());
    return factory;
  }
}
