package com.silverpeas.external.filesharing.model;

import java.io.Serializable;
import java.util.Date;

public class DownloadDetail implements Serializable {
  private int id;
  private String keyFile;
  private Date downloadDate;
  private String userIP;

  public DownloadDetail() {

  }

  public DownloadDetail(int id, String keyFile, Date downloadDate, String userIP) {
    setId(id);
    setKeyFile(keyFile);
    setDownloadDate(downloadDate);
    setUserIP(userIP);
  }

  public DownloadDetail(String keyFile, Date downloadDate, String userIP) {
    setKeyFile(keyFile);
    setDownloadDate(downloadDate);
    setUserIP(userIP);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getKeyFile() {
    return keyFile;
  }

  public void setKeyFile(String keyFile) {
    this.keyFile = keyFile;
  }

  public Date getDownloadDate() {
    return downloadDate;
  }

  public void setDownloadDate(Date downloadDate) {
    this.downloadDate = downloadDate;
  }

  public String getUserIP() {
    return userIP;
  }

  public void setUserIP(String userIP) {
    this.userIP = userIP;
  }

}
