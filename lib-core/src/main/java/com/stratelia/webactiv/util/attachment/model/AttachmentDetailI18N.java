/**
 * Titre : Silverpeas<p>
 * Description : This object provides the function of files attached<p>
 * Copyright : Copyright (c) Jean-Claude Groccia<p>
 * Société : Stratelia<p>
 * @author Jean-Claude Groccia
 * @version 1.0
 */
package com.stratelia.webactiv.util.attachment.model;

import java.io.Serializable;
import java.util.Date;

import com.silverpeas.util.i18n.Translation;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;

/**
 * Class declaration
 *
 *
 * @author
 */
public class AttachmentDetailI18N extends Translation implements Serializable
{    
    private String			physicalName	= null;
    private String			logicalName		= null;
    private String			type			= null;
	private Date			creationDate;
    private long			size;
    private String			author			= null;
	private String			title			= null;
	private String			info			= null;
	private String			instanceId		= null;
	
	public AttachmentDetailI18N()
	{
		
	}
	
	public AttachmentDetailI18N(AttachmentDetail attachmentDetail)
    {
		super.setLanguage(attachmentDetail.getLanguage());
		super.setObjectId(attachmentDetail.getPK().getId());
		if (attachmentDetail.getTranslationId() != null)
			super.setId(Integer.parseInt(attachmentDetail.getTranslationId()));
		
		this.physicalName = attachmentDetail.getPhysicalName();
    	this.logicalName = attachmentDetail.getLogicalName();
    	this.type = attachmentDetail.getType();
    	this.creationDate = attachmentDetail.getCreationDate();
    	this.size = attachmentDetail.getSize();
    	this.author = attachmentDetail.getAuthor();
    	this.title = attachmentDetail.getTitle();
    	this.info = attachmentDetail.getInfo();
    	this.instanceId = attachmentDetail.getPK().getInstanceId();
    }
	
    /**
     * Constructor
     * @author Nicolas EYSSERIC
     * @version
     */
    public AttachmentDetailI18N(String lang, String physicalName, String logicalName, String type, Date creationDate, long size, String author, String title, String info, String instanceId)
    {
    	if (lang != null)
			super.setLanguage(lang);
    	
    	this.physicalName = physicalName;
    	this.logicalName = logicalName;
    	this.type = type;
    	this.creationDate = creationDate;
    	this.size = size;
    	this.author = author;
    	this.title = title;
    	this.info = info;
    	this.instanceId = instanceId;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getPhysicalName()
    {
        return physicalName;
    }

    /**
     * Method declaration
     *
     *
     * @param physicalName
     *
     * @see
     */
    public void setPhysicalName(String physicalName)
    {
        this.physicalName = physicalName;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getLogicalName()
    {
        return logicalName;
    }

    /**
     * Method declaration
     *
     *
     * @param logicalName
     *
     * @see
     */
    public void setLogicalName(String logicalName)
    {
		SilverTrace.info("attachment", "AttachmentDetail.setLogicalName()", "root.MSG_GEN_PARAM_VALUE", "logicalName = "+logicalName);
        this.logicalName = logicalName;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getType()
    {
        return type;
    }

    /**
     * Method declaration
     *
     *
     * @param type
     *
     * @see
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public long getSize()
    {
        return size;
    }

    /**
     * Method declaration
     *
     *
     * @param size
     *
     * @see
     */
    public void setSize(long size)
    {
        this.size = size;
    }

	/**
	 * Methode declaration
	 *
	 *
	 * @see
	 */
	public Date getCreationDate()
	{
		return creationDate;
	}

	/**
	 * Methode declaration
	 *
	 *
	 * @param fileDate
	 *
	 * @see
	 */
	public void setCreationDate(Date creationDate)
	{
		this.creationDate = creationDate;
	}

    public void setAuthor( String author )
    {
        this.author = author;
    }

    public String getAuthor()
    {
        return this.author;
    }

	public void setTitle(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return this.title;
    }

	public void setInfo(String info)
    {
        this.info = info;
    }

    public String getInfo()
    {
        return this.info;
    }

    public void setInstanceId(String instanceId)
    {
    	this.instanceId = instanceId;
    }
    
    public String getInstanceId()
    {
    	return instanceId;
    }

	public String getExtension()
	{
		return FileRepositoryManager.getFileExtension(logicalName);
	}
	

	public boolean equals(Object arg0) {
		if (arg0 == null)
			return false;
		if (arg0 instanceof AttachmentDetailI18N)
		{
			AttachmentDetailI18N a = (AttachmentDetailI18N) arg0;
			return a.getId() == getId();
		}
		else
			return false;
	}
}