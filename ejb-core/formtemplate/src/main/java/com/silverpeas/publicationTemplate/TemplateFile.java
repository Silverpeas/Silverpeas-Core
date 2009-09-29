package com.silverpeas.publicationTemplate;

public class TemplateFile {
  private String fileName = "";
  private String typeName = "";
  private String name = "";

  public TemplateFile() {
  }

  public TemplateFile(String fileName, String typeName, String name) {
    this.fileName = fileName;
    this.typeName = typeName;
    this.name = name;
  }

  public String getTypeName() {
    return this.typeName;
  }

  public String getFileName() {
    return this.fileName;
  }

  public String getName() {
    return this.name;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setName(String name) {
    this.name = name;
  }

}
