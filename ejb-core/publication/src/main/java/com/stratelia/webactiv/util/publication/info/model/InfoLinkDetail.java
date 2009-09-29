package com.stratelia.webactiv.util.publication.info.model;

import java.io.Serializable;

public class InfoLinkDetail extends InfoItemDetail implements Serializable {

  private String targetId = null;

  public InfoLinkDetail(InfoPK infoPK, String order, String id, String targetId) {
    super(infoPK, order, id);
    this.targetId = targetId;
  }

  public String getTargetId() {
    return targetId;
  }

  public void setTargetId(String targetId) {
    this.targetId = targetId;
  }
}