package com.sun.portal.portletcontainer.driver.admin;

import java.io.File;
import java.util.EventListener;

public interface DirectoryChangedListener extends EventListener {
  public void fileAdded(File file);
}
