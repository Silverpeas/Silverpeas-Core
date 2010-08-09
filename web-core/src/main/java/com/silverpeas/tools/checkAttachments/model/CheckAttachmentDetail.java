package com.silverpeas.tools.checkAttachments.model;


public class CheckAttachmentDetail
{
  public long getAttachmentId() {
    return attachmentId;
  }
  public void setAttachmentId(long attachmentId) {
    this.attachmentId = attachmentId;
  }
  public String getLogicalName() {
    return logicalName;
  }
  public void setLogicalName(String logicalName) {
    this.logicalName = logicalName;
  }
  public String getPhysicalName() {
    return physicalName;
  }
  public void setPhysicalName(String physicalName) {
    this.physicalName = physicalName;
  }
  public long getSize() {
    return size;
  }
  public void setSize(long size) {
    this.size = size;
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getSpaceLabel() {
    return spaceLabel;
  }
  public void setSpaceLabel(String spaceLabel) {
    this.spaceLabel = spaceLabel;
  }
  public String getComponentLabel() {
    return componentLabel;
  }
  public void setComponentLabel(String componentLabel) {
    this.componentLabel = componentLabel;
  }
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }
  public String getPublicationPath() {
    return publicationPath;
  }
  public void setPublicationPath(String publicationPath) {
    this.publicationPath = publicationPath;
  }
  public String getActionsDate() {
    return actionsDate;
  }
  public void setActionsDate(String actionsDate) {
    this.actionsDate = actionsDate;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  private long attachmentId;
  private String logicalName = null;
  private String physicalName = null;
  private long size;
  private String title = null;
  private String spaceLabel = null;
  private String componentLabel = null;
  private String context = null;
  private String publicationPath = null;
  private String actionsDate = null;
  private String status = null;
  private String path = null;
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }

}