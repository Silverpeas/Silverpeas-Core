package org.silverpeas.core.util.file;

import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.io.FileCleaningTracker;

import java.nio.file.Path;

/**
 * A provider of a {@link DiskFileItemFactory} factory. Its goal is to initialize this factory with
 * some default parameters for the particular use of Silverpeas:
 * <ul>
 * <li>the path of the temporary directory in use in Silverpeas,</li>
 * <li>the threshold size above which the uploaded files are temporarily stored in disk.</li>
 * </ul>
 *
 * @author mmoquillon
 */
class DiskFileItemFactoryProvider {

  private static final int THRESHOLD_SIZE = 2097152;

  /**
   * Provides a {@link DiskFileItemFactory} instance initialized for the Silverpeas use.
   *
   * @return a {@link DiskFileItemFactory} object.
   */
  public DiskFileItemFactory provide() {
    return DiskFileItemFactory.builder()
        .setPath(Path.of(FileRepositoryManager.getTemporaryPath()))
        .setFileCleaningTracker(new FileCleaningTracker())
        .setBufferSize(THRESHOLD_SIZE)
        .get();
  }
}
