package org.silverpeas.core.io.file;

import org.silverpeas.core.util.StringUtil;

import java.io.File;

/**
 * A reference to a SilverpeasFile. It defines some properties about a SilverpeasFile from which
 * it can be located.
 * @author mmoquillon
 */
public class SilverpeasFileDescriptor {

  private final String instanceId;
  private String directory;
  private String fileName;
  private String mimeType;
  private short flag;

  /**
   * Constructs a new reference to a Silverpeas file that belonging to the specified component
   * instance.
   * @param componentId the unique identifier of a component instance.
   */
  public SilverpeasFileDescriptor(String componentId) {
    this.instanceId = componentId;
  }

  /**
   * Constructs a new reference to a Silverpeas file that belonging to no component instance.
   */
  public SilverpeasFileDescriptor() {
    this.instanceId = "";
  }

  /**
   * The Silverpeas file referred by this instance is located into the specified directory.
   * @param directory the parent directory of the Silverpeas file.
   * @return itself.
   */
  public SilverpeasFileDescriptor parentDirectory(String directory) {
    this.directory = directory;
    return this;
  }

  /**
   * The Silverpeas file referred by this instance has the specified name.
   * @param fileName the name of the Silverpeas file.
   * @return itself.
   */
  public SilverpeasFileDescriptor fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  /**
   * The content of the Silverpeas file referred by this instance is of the given MIME type.
   * @param mimeType the MIME type of the content of the Silverpeas file.
   * @return itself.
   */
  public SilverpeasFileDescriptor mimeType(String mimeType) {
    this.mimeType = mimeType;
    return this;
  }

  /**
   * The Silverpeas file is in fact a temporary one and then isn't located among the data of
   * Silverpeas. In this case, the location of the file is computed from the temporary directory
   * of Silverpeas.
   * @return itself.
   */
  public SilverpeasFileDescriptor temporaryFile() {
    this.flag = 2;
    return this;
  }

  /**
   * The path of the Silverpeas file referred by this descriptor is given as absolute. In this
   * case,
   * either the parent directory is given as an absolute path or, in the case it is not defined,
   * the
   * file name is given as an absolute path.
   * @return itself.
   */
  public SilverpeasFileDescriptor absolutePath() {
    this.flag = 1;
    return this;
  }

  /**
   * Gets the unique identifier of the component instance that manages the file referred by this
   * descriptor.
   * @return the component instance identifier or null if the file doesn't belong to any Silverpeas
   * component instance.
   */
  public String getComponentInstanceId() {
    return instanceId;
  }

  /**
   * Gets the parent directory to the file as specified in the descriptor.
   * @return the parent directory path or null if no such information is defined in the descriptor.
   */
  public String getParentDirectory() {
    return directory;
  }

  /**
   * Gets the name of the file referred by this descriptor.
   * @return the filename of the descriptor. The filename can an absolute path of the file (see
   * {@code isAbsolutePath()} to know if the descriptor referes an absolute or a relative path).
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Gets the path of the file by taking into account both the parent directory and the filename.
   * @return the relative or absolute path of the described file (see the method
   * {@code isAbsolutePath()} to known if the returned path is absolute or relative).
   */
  public String getFilePath() {
    String path = getFileName();
    if (StringUtil.isDefined(getParentDirectory())) {
      path = getParentDirectory() + File.separatorChar + path;
    }
    return path;
  }

  /**
   * Gets the MIME type of the file content.
   * @return the MIME type of the file content.
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Is the file referred by this descriptor is a temporary one. In a such case, the location of
   * the file is computed from the temporary directory of Silverpeas; the path of the file is then
   * considered as relative to the temporary directory of Silverpeas, whatever the returned value
   * of the {@code isAbsolutePath()} method.
   * @return true of the file referred by this descriptor is a temporary one.
   */
  public boolean isTemporaryFile() {
    return this.flag == 2;
  }

  /**
   * Is the parent directory or the file name is provided as an absolute path?
   * @return true of the path of the file referred by this descriptor is provided as absolute,
   * false otherwise.
   */
  public boolean isAbsolutePath() {
    return this.flag == 1;
  }

}
