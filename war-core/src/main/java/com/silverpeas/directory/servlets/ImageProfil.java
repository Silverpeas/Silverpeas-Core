package com.silverpeas.directory.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import com.silverpeas.util.FileUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;

public class ImageProfil {

  private String photoFileName;
  private String subDirectory;

  public ImageProfil(String photo, String subDirectory) {
    this.photoFileName = photo;
    this.subDirectory = subDirectory;
  }

  public boolean isImage() {
    try {
      MimeType type = new MimeType(FileUtil.getMimeType(photoFileName));
      return "image".equalsIgnoreCase(type.getPrimaryType());
    } catch (MimeTypeParseException e) {
      return false;
    }

  }

  /**
   * In case of unit upload
   * @param image
   * @throws IOException
   */
  public void saveImage(InputStream data) throws IOException {
    File image = new File(FileRepositoryManager.getAbsolutePath(subDirectory)
        + File.separatorChar + photoFileName);
    image.getParentFile().mkdir();
    FileUtil.writeFile(image, data);
  }

  public InputStream getImage() throws IOException {
    File image = new File(FileRepositoryManager.getAbsolutePath(subDirectory)
        + File.separatorChar + photoFileName);
    return new FileInputStream(image);
  }
}
