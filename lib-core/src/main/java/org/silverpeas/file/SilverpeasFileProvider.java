package org.silverpeas.file;

import com.silverpeas.util.FileUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.file.SilverpeasFileProcessor.ProcessingContext;

/**
 * A provider of Silverpeas files. This provider aims to provide a single point to simply retrieve
 * files managed in Silverpeas in the form of a {@link org.silverpeas.file.SilverpeasFile}
 * instances.
 * <p/>
 * Unlike the IO Processing API in Silverpeas, it is not dedicated to be used within a
 * transactional file processing; for a such use, please see the
 * {@link org.silverpeas.process.io.file.FileHandler} class that offers a higher level access to the
 * files managed in Silverpeas. This class is dedicated to provide a low-level and a single and
 * unique point to access the files in Silverpeas with a support for an additional computing in
 * order to customize the access.
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
    String filePath = processPath(getFilePathFrom(descriptor), ProcessingContext.GETTING);
    return processSilverpeasFile(new SilverpeasFile(descriptor.getComponentInstanceId(), filePath,
        descriptor.getMimeType()), ProcessingContext.GETTING);
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
    String filePath = processPath(absolutePath, ProcessingContext.GETTING);
    return processSilverpeasFile(new SilverpeasFile("", filePath), ProcessingContext.GETTING);
  }

  /**
   * Writes the data into the file matching the description provided by the specified
   * file descriptor. The data is read from the specified input stream and the resulted file is
   * returned as an instance of {@code SilverpeasFile}. If the file already
   * exists, then its content is just replaced by the new one from the stream. If the file doesn't
   * exist, then it is created with all its parent directory.
   * <p/>
   * A chain of pre and post file processing will be performed against the new or updated file.
   * @param descriptor a descriptor of a SilverpeasFile.
   * @param stream an input stream on the data to write.
   * @return the SilverpeasFile with the new content.
   * @throws java.io.IOException if an error occurs while writing the content from the input stream
   * to the file.
   */
  public SilverpeasFile writeSilverpeasFile(SilverpeasFileDescriptor descriptor,
      final InputStream stream) throws IOException {
    String filePath = processPath(getFilePathFrom(descriptor), ProcessingContext.WRITING);
    SilverpeasFile file =
        new SilverpeasFile(descriptor.getComponentInstanceId(), filePath);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
    }
    FileUtil.writeFile(file, stream);
    return processSilverpeasFile(file, ProcessingContext.WRITING);
  }

  /**
   * Writes the data into the file located at the specified absolute path. The data is read from
   * the specified input stream and the resulted file is returned as an instance of
   * {@code SilverpeasFile}. If the file at the specified path already exists, then its content is
   * just replaced by the new one from the stream. If the file doesn't exist, then it is created
   * with all its parent directory.
   * <p/>
   * A chain of pre and post file processing will be performed against the new or updated file.
   * @param absolutePath the absolute path of a file.
   * @param stream an input stream on the data to write.
   * @return the SilverpeasFile with the new content.
   * @throws java.io.IOException if an error occurs while writing the content from the input stream
   * to the file.
   */
  public SilverpeasFile writeSilverpeasFile(String absolutePath, final InputStream stream)
      throws IOException {
    String filePath = processPath(absolutePath, ProcessingContext.WRITING);
    SilverpeasFile file = new SilverpeasFile("", filePath);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
    }
    FileUtil.writeFile(file, stream);
    return processSilverpeasFile(file, ProcessingContext.WRITING);
  }

  /**
   * Deletes the file matching the description provided by the specified file descriptor. The
   * deleted file is then returned as an instance of {@code SilverpeasFile}. If the file doesn't
   * exist, then nothing is done and a {@code SilverpeasFile.NO_FILE} is returned.
   * A chain of pre and post file processing will be performed against the new or file to delete.
   * @param descriptor a descriptor of the SilverpeasFile to delete.
   * @return either the deleted file or NO_FILE whether the file to delete doesn't exist.
   */
  public SilverpeasFile deleteSilverpeasFile(SilverpeasFileDescriptor descriptor) {
    String filePath = processPath(getFilePathFrom(descriptor), ProcessingContext.DELETION);
    SilverpeasFile file =
        new SilverpeasFile(descriptor.getComponentInstanceId(), filePath);
    if (!file.exists()) {
      return SilverpeasFile.NO_FILE;
    }
    file.delete();
    return processSilverpeasFile(file, ProcessingContext.DELETION);
  }

  /**
   * Deletes the file located at the specified absolute path. The deleted file is then returned
   * as an instance of {@code SilverpeasFile}. If the file doesn't exist, then nothing is done and
   * a {@code SilverpeasFile.NO_FILE} is returned.
   * A chain of pre and post file processing will be performed against the new or file to delete.
   * @param absolutePath the absolute path of the file to delete.
   * @return either the deleted file or NO_FILE whether the file to delete doesn't exist.
   */
  public SilverpeasFile deleteSilverpeasFile(String absolutePath) {
    String filePath = processPath(absolutePath, ProcessingContext.DELETION);
    SilverpeasFile file = new SilverpeasFile("", filePath);
    if (!file.exists()) {
      return SilverpeasFile.NO_FILE;
    }
    file.delete();
    return processSilverpeasFile(file, ProcessingContext.DELETION);
  }

  /**
   * Moves the specified SilverpeasFile instance to the specified directory.
   * A chain of pre and post file processing will be performed against the file to move. The
   * pre processors will be invoked before moving the file itself (by its path) and the post
   * processors will be invoked after the move of the file (with the moved file as parameter).
   * @param file the Silverpeas file to move.
   * @param destinationPath the path of the directory into which the file has to be moved.
   * @return the resulted moved Silverpeas file.
   * @throws java.io.IOException if an error occurs while moving the file into its new location.
   */
  public SilverpeasFile moveSilverpeasFile(final SilverpeasFile file, String destinationPath)
      throws IOException {
    String filePath = processPath(file.getAbsolutePath(), ProcessingContext.MOVING);
    FileUtils.moveFileToDirectory(new File(filePath), new File(destinationPath), true);
    SilverpeasFile movedFile = new SilverpeasFile(file.getComponentInstanceId(),
        destinationPath + File.separatorChar + file.getName(),
        file.getMimeType());
    return processSilverpeasFile(movedFile, ProcessingContext.MOVING);
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

  private String processPath(String path, ProcessingContext context) {
    String processedPath = path;
    for (SilverpeasFileProcessor processor : processors) {
      processedPath = processor.processBefore(processedPath, context);
    }
    return processedPath;
  }

  private SilverpeasFile processSilverpeasFile(final SilverpeasFile file,
      ProcessingContext context) {
    SilverpeasFile processedFile = file;
    for (SilverpeasFileProcessor processor : processors) {
      processedFile = processor.processAfter(processedFile, context);
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
