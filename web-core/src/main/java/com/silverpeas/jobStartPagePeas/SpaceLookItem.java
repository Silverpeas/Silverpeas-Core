package com.silverpeas.jobStartPagePeas;

import java.io.File;
import java.io.Serializable;

import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;

public class SpaceLookItem implements Serializable {

  private String name = null;
  private String size = null;
  private String url = null;

  public SpaceLookItem(File file, String spaceId) {
    name = file.getName();
    size = FileRepositoryManager.formatFileSize(file.length());

    String mimeType = "text/plain";
    if (FileRepositoryManager.getFileExtension(name).equalsIgnoreCase("gif"))
      mimeType = "image/gif";
    else if (FileRepositoryManager.getFileExtension(name).startsWith("jp"))
      mimeType = "image/jpeg";

    url = FileServerUtils.getOnlineURL(spaceId, name, name, mimeType, "look");
  }

  public String getName() {
    return name;
  }

  public String getSize() {
    return size;
  }

  public String getURL() {
    return url;
  }
}
