package com.stratelia.webactiv.util.readingControl.model;

import java.io.Serializable;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

public class PublicationActorLinkDetail implements Serializable {

  private String userId;
  private PublicationPK pubPK;

  public PublicationActorLinkDetail(String userId, PublicationPK pubPK) {
    this.userId = userId;
    this.pubPK = pubPK;
  }

  public String getUserId() {
    return userId;
  }

  public PublicationPK getPublicationPK() {
    return pubPK;
  }

}