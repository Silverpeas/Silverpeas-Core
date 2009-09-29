package com.stratelia.webactiv.servlets;

public class OnlineFile {
  private String mimeType;
  private String sourceFile;
  private String directory;
  private String componentId;

  public OnlineFile(String mimeType, String sourceFile, String directory) {
    this.mimeType = mimeType;
    this.sourceFile = sourceFile;
    this.directory = directory;
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getSourceFile() {
    return sourceFile;
  }

  public String getDirectory() {
    return directory;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

}
