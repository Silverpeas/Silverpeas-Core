package org.silverpeas.file;

import com.stratelia.webactiv.util.FileRepositoryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A provider of Silverpeas files. This provider aims to provide a single point to simply retrieve
 * files managed in Silverpeas in the form of a {@link org.silverpeas.file.SilverpeasFile}
 * instances.
 * <p/>
 * Unlike the IO Processing API in Silverpeas, it is not dedicated to be used within a
 * transactional file processing; for a such use, please see the
 * {@link org.silverpeas.process.io.file.FileHandler} class. This class is dedicated to provide a
 * single and unique point to access the files in Silverpeas with a support for an additional
 * computing in order to customize the access.
 * <p/>
 * The Silverpeas File Provider provides two extensions points to hook additional computations in
 * order to process an incoming file path and to process an outgoing file (a {@code SilverpeasFile}
 * instance). For example, a process can be hooked to resize automatically the images on the
 * demand. To hook such processors, they have to implement the
 * {@link org.silverpeas.file.SilverpeasFileProcessor} interface.
 * @author mmoquillon
 */
public class SilverpeasFileProvider {

  private static final SilverpeasFileProvider instance = new SilverpeasFileProvider();

  public static final SilverpeasFileProvider getInstance() {
    return instance;
  }

  private List<SilverpeasFileProcessor> processors = new ArrayList<SilverpeasFileProcessor>();

  private SilverpeasFileProvider() {

  }

  /**
   * Gets a {@code SilverpeasFile} instance matching the description provided by the specified
   * file descriptor. A chain of pre and post file processing will be performed against the file
   * to retrieve; in this case, the returned file can be a modified version of the targeted file.
   * This method is for delivering a file to the end-user.
   * @param descriptor a descriptor of a SilverpeasFile.
   * @return the SilverpeasFile with the content of the file identified by the specified descriptor
   * and after filtering by pre and post file processing.
   */
  public SilverpeasFile getSilverpeasFile(SilverpeasFileDescriptor descriptor) {
    String filePath = processPath(getFilePathFrom(descriptor));
    return processSilverpeasFile(new SilverpeasFile(descriptor.getComponentInstanceId(), filePath,
        descriptor.getMimeType()));
  }

  /**
   * Gets a {@code SilverpeasFile} instance for the file located at the specified absolute path.
   * A chain of pre and post file processing will be performed against the file
   * to retrieve; in this case, the returned file can be a modified version of the targeted file.
   * This method is for delivering a file to the end-user.
   * @param absolutePath the absolute path of a file.
   * @return the SilverpeasFile with the content of the file located at the specified path and
   * after filtering by pre and post file processing.
   */
  public SilverpeasFile getSilverpeasFile(String absolutePath) {
    String filePath = processPath(absolutePath);
    return processSilverpeasFile(new SilverpeasFile("", filePath));
  }

  /**
   * Adds a processor of SilverpeasFile. A processor can perform some additional treatments on the
   * path of a SilverpeasFile or on the SilverpeasFile itself.
   * <p/>
   * Each processor are triggered in the order they are added and the output of one processor acts
   * as an input for the second processor. First, the processors are invoked on the path of a
   * SilverpeasFile before retrieving really the asked SilverpeasFile. It is then a chance to
   * rework the path or to perform some additional treatment based on the path passed as argument.
   * Then, once the SilverpeasFile referred by the path is fetched, the processors is again invoked
   * with this time the SilverpeasFile itself as argument.
   * <p/>
   * {@see SilverpeasFileProcessor}
   * @param processor a SilverpeasFile processor to add.
   */
  public void addProcessor(final SilverpeasFileProcessor processor) {
    if (!this.processors.contains(processor)) {
      this.processors.add(processor);
    }
  }

  private String processPath(String path) {
    String processedPath = path;
    for (SilverpeasFileProcessor processor : processors) {
      processedPath = processor.processBefore(processedPath);
    }
    return processedPath;
  }

  private SilverpeasFile processSilverpeasFile(final SilverpeasFile file) {
    SilverpeasFile processedFile = file;
    for (SilverpeasFileProcessor processor : processors) {
      processedFile = processor.processAfter(processedFile);
    }
    return processedFile;
  }

  private String getFilePathFrom(SilverpeasFileDescriptor descriptor) {
    String filePath;
    if (descriptor.isTemporaryFile()) {
      filePath =
          FileRepositoryManager.getTemporaryPath("useless", descriptor.getComponentInstanceId()) +
              descriptor.getFilePath();
    } else {
      if (descriptor.isAbsolutePath()) {
        filePath = descriptor.getFilePath();
      } else {
        filePath = FileRepositoryManager.getAbsolutePath(descriptor.getComponentInstanceId()) +
            descriptor.getFilePath();
      }
    }
    return filePath;
  }

}
