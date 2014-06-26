package org.silverpeas.file;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import org.apache.commons.io.FileUtils;
import org.silverpeas.attachment.model.SimpleDocument;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A representation of a File in Silverpeas. This class abstracts the way the files are managed
 * in Silverpeas by extending the original JDK file with additional features. A file in Silverpeas
 * belongs always to an component instance and is qualified by its MIME type.
 * <p/>
 * Such file can be either a document referred by a publication's attachment or an image from a
 * form.
 * @author mmoquillon
 */
public class SilverpeasFile extends File {

  /**
   * A SilverpeasFile representing a non existent file. It is better to work with it than taking
   * into account a FileNotFoundException exception raising.
   */
  public static final SilverpeasFile NO_FILE = new SilverpeasFile("", "", "");

  private final String instanceId;
  private String mimeType;

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
    if (StringUtil.isDefined((mimeType))) {
      this.mimeType = mimeType;
    } else {
      this.mimeType = FileUtil.getMimeType(path);
    }
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getComponentInstanceId() {
    return instanceId;
  }

  /**
   * Opens and returns and input stream to this file.
   * @return a buffered input stream to this file.
   * @throws IOException if an error occurs while opening the input stream.
   */
  public InputStream inputStream() throws IOException {
    return new BufferedInputStream(FileUtils.openInputStream(this));
  }

}
