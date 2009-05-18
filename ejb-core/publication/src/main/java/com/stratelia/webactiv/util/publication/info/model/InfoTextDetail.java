package com.stratelia.webactiv.util.publication.info.model;

import java.io.Serializable;

public class InfoTextDetail extends InfoItemDetail implements Serializable{
  
  private String content = null;
  
  public InfoTextDetail(InfoPK infoPK, String order, String id, String content) {
    super(infoPK, order, id);
    this.content = content;
  }

  public String getContent() {
	if ((content != null) && (!content.toLowerCase().equals("null")))
	    return content;
	else return "";
  }
  
  public void setContent(String content) {
    this.content = content;
  }
}