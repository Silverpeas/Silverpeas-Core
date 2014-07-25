package org.silverpeas.file;

import com.stratelia.webactiv.util.FileRepositoryManager;

import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.file.SilverpeasFileProcessor.ProcessingContext;

/**
 * A provider of Silverpeas files. This provider aims to provide a single point to simply retrieve
 * files managed in Silverpeas in the form of a {@link org.silverpeas.file.SilverpeasFile}
 * instances. The {@link org.silverpeas.file.SilverpeasFile} class provides useful methods to
 * manage the files in Silverpeas. Both {@¢ode SilverpeasFileProvider} and {@code SilverpeasFile}
 * classes support a mechanism of pre and post operations processing permitting to hook additional
 * treatments on the files handled in Silverpeas.
 * <p/>
 * Unlike the IO Processing API in Silverpeas, it is not dedicated to be used within a
 * transactional file processing; for a such use, please see the
 * {@link org.silverpeas.process.io.file.FileHandler} class that offers a higher level access to the
 * files managed in Silverpeas. This class is dedicated to provide a low-level and a single and
 * unique point to access the files in Silverpeas with a support for an additional computing in
 * order to hook parallel behaviours on the handled file.
 * <p/>
 * The Silverpeas File Provider provides two extensions points to hook additional computations with
 * the file operations. For example, a process can be hooked to resize automatically the images on
 * the demand (see {@link org.silverpeas.file.ImageResizingProcessor} for a such example of
 * processors). To hook such processors, they have to implement the
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
   * @see org.silverpeas.file.SilverpeasFileProvider#getSilverpeasFile(SilverpeasFileDescriptor)
   * @param descriptor a descriptor of a SilverpeasFile.
   * @return the SilverpeasFile with the content of the file identified by the specified descriptor
   * and after filtering by pre and post file processing.
   */
  public static SilverpeasFile getFile(SilverpeasFileDescriptor descriptor) {
    return getInstance().getSilverpeasFile(descriptor);
  }

  /**
   * @see org.silverpeas.file.SilverpeasFileProvider#getSilverpeasFile(String)
   * @param absolutePath the absolute path of a file.
   * @return the SilverpeasFile with the content of the file located at the specified path and
   * after filtering by pre and post file processing.
   */
  public static SilverpeasFile getFile(String absolutePath) {
    return getInstance().getSilverpeasFile(absolutePath);
  }

  /**
   * @see org.silverpeas.file.SilverpeasFileProvider#newSilverpeasFile(String)
   * @param absolutePath the absolute path at which will be located the file.
   * @return a new SilverpeasFile that will be created (if not already) at the specified location.
   */
  public static SilverpeasFile newFile(String absolutePath) {
    return getInstance().newSilverpeasFile(absolutePath);
  }

  /**
   * @see org.silverpeas.file.SilverpeasFileProvider#newSilverpeasFile(org.silverpeas.file.SilverpeasFileDescriptor)
   * @param descriptor a descriptor of a SilverpeasFile.
   * @return a new SilverpeasFile that will be created (if not already) at the location described
   * by the specified descriptor.
   */
  public static SilverpeasFile newFile(SilverpeasFileDescriptor descriptor) {
    return getInstance().newSilverpeasFile(descriptor);
  }

  /**
   * Gets a new {@code SilverpeasFile} instance for the file located at the specified absolute
   * path. If the file already exists, it is this file that will be returned. No chain of pre and
   * post processing are performed against the new Silverpeas file.
   * @param absolutePath the absolute path at which will be located the file.
   * @return a new SilverpeasFile that will be created (if not already) at the specified location.
   */
  public SilverpeasFile newSilverpeasFile(String absolutePath) {
    return new SilverpeasFile("", absolutePath);
  }

  /**
   * Gets a new {@code SilverpeasFile} instance for the file described by the specified file
   * descriptor. If the file already exists, it is this file that will be returned. No chain of pre
   * and post processing are performed against the new Silverpeas file.
   * @param descriptor a descriptor of a SilverpeasFile.
   * @return a new SilverpeasFile that will be created (if not already) at the specified location.
   */
  public SilverpeasFile newSilverpeasFile(SilverpeasFileDescriptor descriptor) {
    return new SilverpeasFile(descriptor.getComponentInstanceId(), getFilePathFrom(descriptor),
        descriptor.getMimeType());
  }

  /**
   * Gets a {@code SilverpeasFile} instance matching the description provided by the specified
   * file descriptor.
   * <p/>
   * A chain of pre and post file processing will be performed against the file
   * to retrieve; in this case, the returned file can be a modified version of the targeted file:
   * <ul>
   *   <li>The chain of pre-processors are ran; the first processor will have as argument the
   *   absolute path of the asked file, generated from the specified descriptor, and then the
   *   returned path of each processor is passed as argument to the next processor. By this way
   *   additional treatments can be performed against the path itself; the path can be altered
   *   according to the treatment and the next processor will work on this modified path.</li>
   *   <li>Once the pre-processing chain is done, the file located by the resulted absoluted path
   *   is fetched from the filesystem.</li>
   *   <li>The chain of post-processors are ran; the first processor will have as argument the
   *   fetched Silverpeas file. Each further processors will have as argument the returned
   *   Silverpeas file by the previous processor. By this way, additional treatments can be
   *   performed against the Silverpeas file itself. One of the pre-defined post processor is a
   *   checker of the fetched file existence.</li>
   *   <li>The Silverpeas file processed by the last chain of processors is then returned by the
   *   method.</li>
   * </ul>
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
   * <p/>
   * A chain of pre and post file processing will be performed against the file
   * to retrieve; in this case, the returned file can be a modified version of the targeted file:
   * <ul>
   *   <li>The chain of pre-processors are ran; the first processor will have as argument the
   *   specified absolute path of the asked file and then the
   *   returned path of each processor is passed as argument to the next processor. By this way
   *   additional treatments can be performed against the path itself; the path can be altered
   *   according to the treatment and the next processor will work on this modified path.</li>
   *   <li>Once the pre-processing chain is done, the file located by the resulted absoluted path
   *   is fetched from the filesystem.</li>
   *   <li>The chain of post-processors are ran; the first processor will have as argument the
   *   fetched Silverpeas file. Each further processors will have as argument the returned
   *   Silverpeas file by the previous processor. By this way, additional treatments can be
   *   performed against the Silverpeas file itself. One of the pre-defined post processor is a
   *   checker of the fetched file existence.</li>
   *   <li>The Silverpeas file processed by the last chain of processors is then returned by the
   *   method.</li>
   * </ul>
   * @param absolutePath the absolute path of a file.
   * @return the SilverpeasFile with the content of the file located at the specified path and
   * after filtering by pre and post file processing.
   */
  public SilverpeasFile getSilverpeasFile(String absolutePath) {
    String filePath = processPath(absolutePath, ProcessingContext.GETTING);
    return processSilverpeasFile(new SilverpeasFile("", filePath), ProcessingContext.GETTING);
  }

  /**
   * Adds a processor of SilverpeasFile. A processor can perform some additional treatments on the
   * path of a SilverpeasFile or on the SilverpeasFile itself. According to the file operation
   * performed, both a pre and a post processing can be triggered or only a post-processing. For
   * instance, getting a Silverpeas file will trigger both a chain of pre and a chain of post
   * processing; the first one on the path of the file and the last one on the fetched Silverpeas
   * file. For others operations (like deletion, update, ...) only the chain of post processing is
   * ran against the Silverpeas file once the operation done, as the Silverpeas file was already
   * get. Each processor are triggered in the order they are added and the output of one processor
   * acts as an input for the second processor.
   * <p/>
   * {@see SilverpeasFileProcessor}
   * @param processor a SilverpeasFile processor to add.
   */
  public void addProcessor(final SilverpeasFileProcessor processor) {
    if (!this.processors.contains(processor)) {
      this.processors.add(processor);
    }
  }

  /**
   * Triggers the chain of post-processors against the specified file and according to the specified
   * processing context. The processing context indicates the file operation on which the processing
   * will behave.
   * @param file the Silverpeas file on which the post-processors will work.
   * @param context the file operation context.
   * @return the Silverpeas file resulting from the post-processors chain execution.
   */
  protected static SilverpeasFile processAfter(SilverpeasFile file, ProcessingContext context) {
    return getInstance().processSilverpeasFile(file, context);
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
