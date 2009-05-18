package com.stratelia.silverpeas.contentManager;

import java.util.Iterator;

/**
 * The interface for all the SilverContent (filebox+, ..)
 */
public interface SilverContentInterface
{
	public String getName();
	public String getName(String language);
	public String getDescription();
	public String getDescription(String language);
	public String getURL();
	public String getId();
	public String getInstanceId();
    /*public String getTitle();
    public String getTitle(String language);*/
    public String getDate();
    public String getSilverCreationDate(); //added by ney. 16/05/2004.
    public String getIconUrl();
	public String getCreatorId();
	
	public Iterator getLanguages();
}