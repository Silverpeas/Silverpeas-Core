package com.stratelia.webactiv.util.attachment.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDAO;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetailI18N;
import com.stratelia.webactiv.util.attachment.model.AttachmentI18NDAO;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;

public class AttachmentBmImpl implements AttachmentBm
{
	public AttachmentBmImpl()
	{
	}

    public AttachmentDetail createAttachment(AttachmentDetail attachDetail) throws AttachmentException
	{
		Connection con = getConnection();
		try
		{
			return AttachmentDAO.insertRow(con, attachDetail);
		}
		catch (SQLException se)
		{
			throw new AttachmentException("AttachmentBmImpl.createAttachment()", SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_CREATE_ATTACHMENT", se);
		}
		catch (UtilException ue)
		{
			throw new AttachmentException("AttachmentBmImpl.createAttachment()", SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_CREATE_ATTACHMENT", ue);
		}
		finally
		{
			closeConnection(con);
		}
	}

	public void updateAttachment(AttachmentDetail attachDetail) throws AttachmentException
	{
		Connection con = getConnection();
		try
		{						
			boolean updateDefault = false;
			
			AttachmentDetail oldAttachment = AttachmentDAO.findByPrimaryKey(con, attachDetail.getPK());
			String oldLang = oldAttachment.getLanguage();
			
			if (attachDetail.isRemoveTranslation())
            {
        		//Remove of a translation is required 
				//if (oldLang.equalsIgnoreCase(attachDetail.getLanguage()))
				if ("-1".equals(attachDetail.getTranslationId()))
				{
					//Default language = translation 
					List translations = AttachmentI18NDAO.getTranslations(con, attachDetail.getPK());
					
					if (translations != null && translations.size() > 0)
					{
						AttachmentDetailI18N translation = (AttachmentDetailI18N) translations.get(0);
						
						attachDetail.setPhysicalName(translation.getPhysicalName());
						attachDetail.setLogicalName(translation.getLogicalName());
					    attachDetail.setType(translation.getType());
						attachDetail.setCreationDate(translation.getCreationDate());
						attachDetail.setSize(translation.getSize());
					    attachDetail.setAuthor(translation.getAuthor());
					    attachDetail.setTitle(translation.getTitle());
					    attachDetail.setInfo(translation.getInfo());
						attachDetail.setLanguage(translation.getLanguage());
						            			
						AttachmentI18NDAO.removeTranslation(con, translation.getId());
						
						updateDefault = true;
					}
				}
				else
				{
					AttachmentI18NDAO.removeTranslation(con, attachDetail.getTranslationId());
				}
            }
            else
            {
            	//Add or update a translation
	            if (attachDetail.getLanguage() != null)
	            {
	            	if (oldLang == null)
	            	{
	            		//translation for the first time
	            		oldLang = I18NHelper.defaultLanguage;
	            	}
	            	
	            	if (!oldLang.equalsIgnoreCase(attachDetail.getLanguage()))
	                {
	            		AttachmentDetailI18N translation = new AttachmentDetailI18N(attachDetail);	            		
	            		String translationId = attachDetail.getTranslationId();

            			if (translationId != null && !translationId.equals("-1"))
						{
							AttachmentI18NDAO.updateTranslation(con, translation);
						}
						else
						{
							AttachmentI18NDAO.addTranslation(con, translation);
						}
						
						attachDetail.setPhysicalName(oldAttachment.getPhysicalName());
						attachDetail.setLogicalName(oldAttachment.getLogicalName());
					    attachDetail.setType(oldAttachment.getType());
						attachDetail.setCreationDate(oldAttachment.getCreationDate());
						attachDetail.setSize(oldAttachment.getSize());
					    attachDetail.setAuthor(oldAttachment.getAuthor());
					    attachDetail.setTitle(oldAttachment.getTitle());
					    attachDetail.setInfo(oldAttachment.getInfo());
						attachDetail.setLanguage(oldLang);
	                }
	            	else
	            	{
	            		updateDefault = true;
	            	}
	            }
	            else
	            {
	            	updateDefault = true;
	            }
            }
			
			if (updateDefault)
				AttachmentDAO.updateRow(con, attachDetail);
		}
		catch (SQLException se)
		{
			throw new AttachmentException("AttachmentBmImpl.updateAttachment()", SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_UPDATE_ATTACHMENT", se);
		}
		finally
		{
			closeConnection(con);
		}
	}

    public Vector getAttachmentsByForeignKey(AttachmentPK foreignKey) throws AttachmentException
	{
		Connection con = getConnection();
		try
		{
			return AttachmentDAO.findByForeignKey(con, foreignKey);
		}
		catch (SQLException se)
		{
			throw new AttachmentException("AttachmentBmImpl.getAttachmentsByForeignKey()", SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
		}
		finally
		{
			closeConnection(con);
		}
	}
    
    public Vector getAttachmentsByWorkerId(String workerId) throws AttachmentException
	{
		Connection con = getConnection();
		try
		{
			return AttachmentDAO.findByWorkerId(con, workerId);
		}
		catch (SQLException se)
		{
			throw new AttachmentException("AttachmentBmImpl.getAttachmentsByWorkerId()", SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
		}
		finally
		{
			closeConnection(con);
		}
	}

    public AttachmentDetail findPrevious(AttachmentDetail ad) throws AttachmentException
	{
		Connection con = getConnection();
		try
		{
			return AttachmentDAO.findPrevious(con, ad);
		}
		catch (SQLException se)
		{
			throw new AttachmentException("AttachmentBmImpl.findPrevious()", SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
		}
		finally
		{
			closeConnection(con);
		}
	}

    public AttachmentDetail findNext(AttachmentDetail ad) throws AttachmentException
	{
		Connection con = getConnection();
		try
		{
			return AttachmentDAO.findNext(con, ad);
		}
		catch (SQLException se)
		{
			throw new AttachmentException("AttachmentBmImpl.findNext()", SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
		}
		finally
		{
			closeConnection(con);
		}
	}

    public AttachmentDetail getAttachmentByPrimaryKey(AttachmentPK primaryKey) throws AttachmentException
	{
		Connection con = getConnection();
		try
		{
			return AttachmentDAO.findByPrimaryKey(con, primaryKey);
		}
		catch (SQLException se)
		{
			throw new AttachmentException("AttachmentBmImpl.createAttachment()", SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
		}
		finally
		{
			closeConnection(con);
		}
	}

	public Vector getAttachmentsByPKAndParam(AttachmentPK foreignKey, String nameAttribut, String valueAttribut) throws AttachmentException
	{
		Connection con = getConnection();
		try
		{
			return AttachmentDAO.findByPKAndParam(con, foreignKey, nameAttribut, valueAttribut);
		}
		catch (SQLException se)
		{
			throw new AttachmentException("AttachmentBmImpl.createAttachment()", SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
		}
		finally
		{
			closeConnection(con);
		}
	}

	public Vector getAttachmentsByPKAndContext(AttachmentPK foreignKey, String context, Connection con) throws AttachmentException
	{
		if (con == null) {
			SilverTrace.info("attachment", "AttachmentBmImpl.getAttachmentsByPKAndContext()", "root.MSG_GEN_PARAM_VALUE", "parameter con is null, new connection is created !");
			con = getConnection();
		} else {
			SilverTrace.info("attachment", "AttachmentBmImpl.getAttachmentsByPKAndContext()", "root.MSG_GEN_PARAM_VALUE", "parameter con is not null, this connection is used !");
		}
		try
		{
			return AttachmentDAO.findByPKAndContext(con, foreignKey, context);
		}
		catch (SQLException se)
		{
			throw new AttachmentException("AttachmentBmImpl.createAttachment()", SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
		}
		finally
		{
			closeConnection(con);
		}
	}

    public void deleteAttachment(AttachmentPK primaryKey) throws AttachmentException
	{
		Connection con = getConnection();
		try
		{
			AttachmentI18NDAO.removeTranslations(con, primaryKey);
			
			AttachmentDAO.deleteAttachment(con, primaryKey);
		}
		catch (SQLException se)
		{
			throw new AttachmentException("AttachmentBmImpl.createAttachment()", SilverpeasException.ERROR, "attachment_MSG_NOT_DELETE_FILE", se);
		}
		finally
		{
			closeConnection(con);
		}
	}
	
	public void updateForeignKey(AttachmentPK pk, String foreignKey) throws AttachmentException
	{
		Connection con = getConnection();
		try
		{
			AttachmentDAO.updateForeignKey(con, pk, foreignKey);
		}
		catch (SQLException se)
		{
			throw new AttachmentException("AttachmentBmImpl.updateForeignKey()", SilverpeasException.ERROR, "EX_RECORD_NOT_UPDATE_ATTACHMENT", se);
		}
		finally
		{
			closeConnection(con);
		}
	}
	
	public Collection getAllAttachmentByDate(Date date, boolean alert) throws AttachmentException
	{
		Connection con = getConnection();
		try
		{
			return AttachmentDAO.getAllAttachmentByDate(con, date, alert);
		}
		catch (SQLException se)
		{
			throw new AttachmentException("AttachmentBmImpl.createAttachment()", SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
		}
		finally
		{
			closeConnection(con);
		}
	}
	
	public Collection getAllAttachmentToLib(Date date) throws AttachmentException
	{
		Connection con = getConnection();
		try
		{
			return AttachmentDAO.getAllAttachmentToLib(con, date);
		}
		catch (SQLException se)
		{
			throw new AttachmentException("AttachmentBmImpl.createAttachment()", SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
		}
		finally
		{
			closeConnection(con);
		}
	}
	
	public void notifyUser(NotificationMetaData notifMetaData, String senderId, String componentId) throws AttachmentException 
	{
		Connection con = getConnection();
		SilverTrace.info("attachment", "AttachmentBmImpl.notifyUser()", "root.MSG_GEN_EXIT_METHOD");
		try 
		{
			SilverTrace.info("attachment", "AttachmentBmImpl.notifyUser()", "root.MSG_GEN_EXIT_METHOD", " senderId = " + senderId + " componentId = " + componentId);
			notifMetaData.setConnection(con);
			if (notifMetaData.getSender() == null || notifMetaData.getSender().length() == 0)
				notifMetaData.setSender(senderId);
			NotificationSender notifSender = new NotificationSender(componentId);
			notifSender.notifyUser(notifMetaData);
		} 
		catch (NotificationManagerException e) 
		{
			throw new AttachmentException("AttachmentBmImpl.notifyUser()", SilverpeasRuntimeException.ERROR, "attachment.MSG_ATTACHMENT_NOT_EXIST", e);
		} 
		finally 
		{
			closeConnection(con);
		}
    }
		
	private Connection getConnection() throws AttachmentException
    {
		SilverTrace.info("attachment", "AttachmentBmImpl.getConnection()", "root.MSG_GEN_ENTER_METHOD");
        try
        {
            Connection con = DBUtil.makeConnection(JNDINames.ATTACHMENT_DATASOURCE);
            return con;
        }
        catch (Exception e)
        {
            throw new AttachmentException("AttachmentBmImpl.getConnection()", SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
        }
    }

	private void closeConnection(Connection con)
    {
		try
		{
			if (con != null)
				con.close();
		}
		catch (Exception e)
		{
			SilverTrace.error("attachment", "AttachmentBmImpl.closeConnection()", "root.EX_CONNECTION_CLOSE_FAILED", "", e);
		}
    }
}