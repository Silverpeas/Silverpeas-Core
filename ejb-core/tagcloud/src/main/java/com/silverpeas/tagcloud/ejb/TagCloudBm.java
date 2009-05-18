package com.silverpeas.tagcloud.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBObject;

import com.silverpeas.tagcloud.model.TagCloud;
import com.silverpeas.tagcloud.model.TagCloudPK;


public interface TagCloudBm extends EJBObject {

    public void createTagCloud(TagCloud tagCloud) throws RemoteException;
    
    public void deleteTagCloud(TagCloudPK pk, int type) throws RemoteException;
	
    public Collection getInstanceTagClouds(String instanceId) throws RemoteException;
    
    public Collection getInstanceTagClouds(String instanceId, int maxCount) throws RemoteException;
    
    public Collection getElementTagClouds(TagCloudPK pk) throws RemoteException;
    
    public Collection getTagCloudsByTags(String tags, String instanceId, int type)
    	throws RemoteException;
    
    public Collection getTagCloudsByElement(String instanceId, String externalId, int type)
    	throws RemoteException;
    
    public String getTagsByElement(TagCloudPK pk) throws RemoteException;

}