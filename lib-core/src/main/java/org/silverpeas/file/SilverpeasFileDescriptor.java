package org.silverpeas.file;

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
  private boolean temp;

  /**
   * Constructs a new reference to a Silverpeas file that belonging to the specified component
   * instance.
   * @param componentId the unique identifier of a component instance.
   */
  public SilverpeasFileDescriptor(String componentId) {
    this.instanceId = componentId;
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
   * Silverpeas.
   * @return itself.
   */
  public SilverpeasFileDescriptor temporary() {
    this.temp = true;
    return this;
  }

  public String getComponentInstanceId() {
    return instanceId;
  }

  public String getParentDirectory() {
    return directory;
  }

  public String getFileName() {
    return fileName;
  }

  public String getMimeType() {
    return mimeType;
  }

  public boolean isTemporary() {
    return temp;
  }

}
