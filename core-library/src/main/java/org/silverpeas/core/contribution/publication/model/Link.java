package org.silverpeas.core.contribution.publication.model;

import org.silverpeas.core.ResourceReference;

import java.io.Serializable;

public class Link implements Serializable {

  private String id;
  private PublicationPK pubPK;
  private ResourceReference target;
  private PublicationDetail pub;
  private boolean reverse = false;

  public Link(String id, PublicationPK pubPK, ResourceReference target) {
    setId(id);
    setPubPK(pubPK);
    setTarget(target);
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public PublicationPK getPubPK() {
    return pubPK;
  }

  public void setPubPK(final PublicationPK pubPK) {
    this.pubPK = pubPK;
  }

  public ResourceReference getTarget() {
    return target;
  }

  public void setTarget(final ResourceReference target) {
    this.target = target;
  }

  public PublicationDetail getPub() {
    return pub;
  }

  public void setPub(final PublicationDetail pub) {
    this.pub = pub;
  }

  public boolean isReverse() {
    return reverse;
  }

  public void setReverse(final boolean reverse) {
    this.reverse = reverse;
  }
}
