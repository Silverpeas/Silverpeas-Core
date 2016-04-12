package org.silverpeas.core.io.file;

import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.StringUtil;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A representation of a File in Silverpeas. This class abstracts the way the files are managed
 * in Silverpeas by extending the original JDK file with additional features. A file in Silverpeas
 * belongs always to a component instance and is qualified by its MIME type.
 * <p/>
 * Such file can be either a document referred by a publication's attachment or an image from a
 * form.
 * @author mmoquillon
 */
public class SilverpeasFile extends File {
  private static final long serialVersionUID = -7236431331553364723L;

  /**
   * A SilverpeasFile representing a non existent file. It is better to work with it than taking
   * into account a FileNotFoundException exception raising.
   */
  public static final SilverpeasFile NO_FILE = new SilverpeasFile("", "", "");

  private final String instanceId;
  private String mimeType;
  /**
   * Path to instance identified by instanceId used for security purpose
   */
  private String instancePath;

  /**
   * Creates a new Silverpeas file beloging to the specified component instance and located at the
   * specified path in the Silverpeas filesystem. The MIME type of the file is detected.
   * @param componentId the unique identifier of the component instance.
   * @param path the absolute path of the file.
   */
  protected SilverpeasFile(String componentId, String path) {
    this(componentId, path, FileUtil.getMimeType(path));
  }

  /**
   * Creates a new Silverpeas file beloging to the specified component instance and located at the
   * specified path in the Silverpeas filesystem. The exact MIME type of the file is also
   * specified.
   * @param componentId the unique identifier of the component instance.
   * @param path the absolute path of the file.
   * @param mimeType the MIME type of the file.
   */
  protected SilverpeasFile(String componentId, String path, String mimeType) {
    super(path);
    this.instanceId = componentId;
    this.instancePath = path.substring(0, path.indexOf(componentId) + componentId.length());
    if (StringUtil.isDefined((mimeType))) {
      this.mimeType = mimeType;
    } else {
      this.mimeType = (path.isEmpty() ? "":FileUtil.getMimeType(path));
    }
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getComponentInstanceId() {
    return instanceId;
  }

  @Override
  public boolean exists() {
    return this != NO_FILE && super.exists();
  }

  /**
   * @see java.io.File#delete()
   * <p/>
   * A chain of post-processors will be ran once this file deleted to perform possible additional
   * treatments.
   * @return true if the deletion succeed, false otherwise.
   */
  @Override
  public boolean delete() {
    boolean deleted = false;
    if (exists()) {
      deleted = super.delete();
      SilverpeasFileProvider.processAfter(this, SilverpeasFileProcessor.ProcessingContext.DELETION);
    }
    return deleted;
  }

  /**
   * Opens and returns an input stream to this file.
   * @return a buffered input stream to this file.
   * @throws IOException if an error occurs while opening the input stream.
   */
  public InputStream inputStream() throws IOException {
    return new BufferedInputStream(FileUtils.openInputStream(this));
  }

  /**
   * Writes the content of the specified input stream into this file. If this file doesn't aleady
   * exists, then it is created.
   * <p/>
   * A chain of post-processors will be ran once the content written in this file to perform
   * possible additional treatments.
   * @param stream the input stream from which the content to write is fetched.
   * @throws java.io.IOException if an error occurs while writing the content from the specified
   * input stream into this file.
   */
  public void writeFrom(final InputStream stream) throws IOException {
    File parentFile = getParentFile();
    if (parentFile != null) {
      if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
        throw new IOException("The '" + parentFile + "' directory cannot be created!");
      }
    }
    if (!exists()) {
      this.createNewFile();
    }
    if (!canWrite()) {
      throw new IOException("The file'" + getPath() + "' is read-only!");
    }
    if (!exists()) {
      this.createNewFile();
    }
    FileUtils.copyInputStreamToFile(stream, this);
    SilverpeasFileProvider.processAfter(this, SilverpeasFileProcessor.ProcessingContext.WRITING);
  }

  /**
   * Moves this file into the specified directory. If the directory doesn't exist, it is then
   * created before. Once the file is moved, it is not more existing. If the file doesn't exist,
   * then nothing is done and {@code NO_FILE} is returned.
   * <p/>
   * A chain of post-processors will be ran once this file is moved to the directory to perform
   * possible additional treatments on the the moved file.
   * <p/>
   * The moving operation will create a new file in the specified directory with the content of this
   * file and then delete this file. Consequently, a chain of post-processors will be ran against
   * the deleted file to perform additional treatments at file deletion.
   * @param directoryPath the absolute path of the directory into which this file has to be moved.
   * @return the SilverpeasFile located at the specified directory.
   * @throws java.io.IOException if an error occurs while moving this file into the specified
   * directory
   */
  public SilverpeasFile moveInto(String directoryPath) throws IOException {
    SilverpeasFile movedFile = NO_FILE;
    if (exists()) {
      FileUtils.moveFileToDirectory(this, new File(directoryPath), true);
      movedFile = new SilverpeasFile(getComponentInstanceId(),
          directoryPath + File.separatorChar + getName(), getMimeType());
      SilverpeasFileProvider
          .processAfter(movedFile, SilverpeasFileProcessor.ProcessingContext.MOVING);
    }
    return movedFile;
  }

  /**
   * Copies this file into the specified directory. If the directory doesn't exist, it is then
   * created before.
   * <p/>
   * A chain of post-processors will be ran once this file is copied to the directory to perform
   * possible additional treatments on the copied file.
   * @param directoryPath the absolute path of the directory into which this file has to be moved.
   * @throws java.io.IOException if an error occurs while copying this file into the specified
   * directory
   */
  public SilverpeasFile copyInto(String directoryPath) throws IOException {
    SilverpeasFile copiedFile = NO_FILE;
    if (exists()) {
      FileUtils.copyFileToDirectory(this, new File(directoryPath), true);
      copiedFile = new SilverpeasFile(getComponentInstanceId(),
          directoryPath + File.separatorChar + getName(), getMimeType());
      SilverpeasFileProvider
          .processAfter(copiedFile, SilverpeasFileProcessor.ProcessingContext.COPY);
    }
    return copiedFile;
  }

  /**
   * Indicates if the current silverpeas file is of type archive.
   * @return true if it is an archive file, false otherwise.
   */
  public boolean isArchive() {
    return FileUtil.isArchive(getPath());
  }

  /**
   * Indicates if the current file is of type image.
   * @return true if it is an image file, false otherwise.
   */
  public boolean isImage() {
    return FileUtil.isImage(getPath());
  }

  /**
   * Indicates if the current file is of type mail.
   * @return true if it is a mail file, false otherwise.
   */
  public boolean isMail() {
    return FileUtil.isMail(getPath());
  }

  /**
   * Indicates if the current file is of type PDF.
   * @return true if it is a PDF file, false otherwise.
   */
  public boolean isPdf() {
    return FileUtil.isPdf(getPath());
  }

  /**
   * Indicates if the current file is of type OpenOffice compatible.
   * @return true if it is a OpenOffice compatible file, false otherwise.
   */
  public boolean isOpenOfficeCompatible() {
    return FileUtil.isOpenOfficeCompatible(getPath());
  }

  public boolean isFileSecure() {
    return FileUtil.isFileSecure(getPath(), this.instancePath);
  }
}
