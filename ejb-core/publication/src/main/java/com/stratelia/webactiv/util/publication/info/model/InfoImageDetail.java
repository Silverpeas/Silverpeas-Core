package com.stratelia.webactiv.util.publication.info.model;

import java.io.Serializable;
import java.util.Map;

import com.stratelia.webactiv.util.FileServerUtils;

public class InfoImageDetail extends InfoAttachmentDetail implements Serializable{
    
  public InfoImageDetail(InfoPK infoPK, String order, String id, String physicalName, 
    String logicalName, String description, String type, long size) 
  {
    super(infoPK, order, id, physicalName, logicalName, description, type, size);
  }

  public Map getMappedUrl()
  {
	return FileServerUtils.getMappedUrl(getPK().getSpace(), getPK().getComponentName(), getLogicalName(), getPhysicalName(), getType(), "images");
  }

  public String getWebURL()
  {
	return FileServerUtils.getWebUrl(getPK().getSpace(), getPK().getComponentName(), getLogicalName(), getPhysicalName(), getType(), "images");
  }

  public String getUrl(String serverNameAndPort)
  {
	return serverNameAndPort+FileServerUtils.getUrl(getPK().getSpace(), getPK().getComponentName(), getLogicalName(), getPhysicalName(), getType(), "images");
  }

}