package com.silverpeas.jobStartPagePeas;

import java.io.File;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class SpaceLookHelper implements Serializable {

  private String spaceId = null;
  private Hashtable items = new Hashtable();

  public SpaceLookHelper(String spaceId) {
    this.spaceId = spaceId;
  }

  public void setFiles(List files) {
    if (files != null) {
      Iterator i = files.iterator();
      while (i.hasNext()) {
        File file = (File) i.next();

        SpaceLookItem item = new SpaceLookItem(file, spaceId);

        if (item != null)
          items.put(item.getName()
              .substring(0, item.getName().lastIndexOf(".")), item);
      }
    }
  }

  public SpaceLookItem getItem(String name) {
    return (SpaceLookItem) items.get(name);
  }
}
