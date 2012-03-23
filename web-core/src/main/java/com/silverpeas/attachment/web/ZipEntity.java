package com.silverpeas.attachment.web;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;

import com.silverpeas.rest.Exposable;

public class ZipEntity implements Exposable {

  private static final long serialVersionUID = -1614659571095493071L;
  
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(required = true)
  private String url;
  @XmlElement(required = true)
  private long size;
  
  public ZipEntity(URI uri, String url, long size) {
    this.uri = uri;
    this.url = url;
    this.size = size;
  }
  
  @Override
  public URI getURI() {
    return uri;
  }

}
