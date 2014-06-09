package org.silverpeas.file;

import com.stratelia.webactiv.util.FileRepositoryManager;
import org.silverpeas.attachment.model.SimpleDocument;

import java.io.File;

/**
 * A factory of Silverpeas files. This factory creates a {@link org.silverpeas.file.SilverpeasFile}
 * instances from the origin and the kind of the file to retrieve in Silverpeas. It wraps also
 * additional path processing before getting them, providing them an extension point to hook
 * additional features.
 * @author mmoquillon
 */
public class SilverpeasFileFactory {

  private static final SilverpeasFileFactory instance = new SilverpeasFileFactory();

  public static final SilverpeasFileFactory getFactory() {
    return instance;
  }

  private SilverpeasFileFactory() {

  }

  /**
   * Gets a {@code SilverpeasFile} instance with the content of the specified {@link
   * org.silverpeas.attachment.model.SimpleDocument}
   * <p/>
   * A SimpleDocument represents a document that was published in Silverpeas by a user
   * in the context of a publication, a form or any other type of contribution.
   * @param document a SimpleDocument.
   * @return the SilverpeasFile with the content of the SimpleDocument.
   */
  public SilverpeasFile getSilverpeasFile(SimpleDocument document) {
    SilverpeasFile file = new SilverpeasFile(document.getInstanceId(), document.getAttachmentPath(),
        document.getContentType());
    if (file == null) {
      file = SilverpeasFile.NO_FILE;
    }
    return file;
  }

  /**
   * Gets a {@code SilverpeasFile} instance matching the description provided by the specified
   * file descriptor.
   * @param descriptor a descriptor of a SilverpeasFile.
   * @return the SilverpeasFile with the content of the file identified by the specified descriptor.
   */
  public SilverpeasFile getSilverpeasFile(SilverpeasFileDescriptor descriptor) {
    String filePath;

    if (descriptor.isTemporary()) {
      filePath =
          FileRepositoryManager.getTemporaryPath("useless", descriptor.getComponentInstanceId()) +
              descriptor.getFileName();
    } else {
      filePath = FileRepositoryManager.getAbsolutePath(descriptor.getComponentInstanceId()) +
          descriptor.getParentDirectory() + File.separator + descriptor.getFileName();
    }
    SilverpeasFile file =
        new SilverpeasFile(descriptor.getComponentInstanceId(), filePath, descriptor.getMimeType());
    if (!file.exists() && !file.isFile()) {
      file = SilverpeasFile.NO_FILE;
    }
    return file;
  }

  /**
   * Gets a {@code SilverpeasFile} instance belonging to the specified component instance and
   * located at the specified absolute path.
   * @param componentId the unique identifier of a component instance.
   * @param path the absolute path of the file to get.
   * @return the SilverpeasFile with the content of the file located at the specified path.
   */
  public SilverpeasFile getSilverpeasFile(String componentId, String path) {
    SilverpeasFile file = new SilverpeasFile(componentId, path);
    if (!file.exists() && !file.isFile()) {
      file = SilverpeasFile.NO_FILE;
    }
    return file;
  }

}
