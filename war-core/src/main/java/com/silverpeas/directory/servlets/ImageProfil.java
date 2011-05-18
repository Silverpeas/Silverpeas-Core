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

  public ImageProfil(String photo) {
    this.photoFileName = photo;
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
    File image = getImageFile();
    image.getParentFile().mkdir();
    FileUtil.writeFile(image, data);
  }

  public InputStream getImage() throws IOException {
    File image = getImageFile();
    return new FileInputStream(image);
  }
  
  private File getImageFile() {
    return new File(FileRepositoryManager.getAvatarPath() + File.separatorChar + photoFileName);
  }
}
