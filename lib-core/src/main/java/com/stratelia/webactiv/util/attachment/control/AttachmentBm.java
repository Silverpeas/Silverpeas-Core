package com.stratelia.webactiv.util.attachment.control;

import java.util.Collection;
import java.util.Date;
import java.util.Vector;
import java.sql.Connection;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;

public interface AttachmentBm
{
    public AttachmentDetail createAttachment(AttachmentDetail attachDetail) throws AttachmentException;

	public void updateAttachment(AttachmentDetail attachDetail) throws AttachmentException;

    public Vector getAttachmentsByForeignKey(AttachmentPK foreignKey) throws AttachmentException;

    public AttachmentDetail getAttachmentByPrimaryKey(AttachmentPK primaryKey) throws AttachmentException;
    
    public Vector getAttachmentsByWorkerId(String workerId) throws AttachmentException;

    public AttachmentDetail findPrevious(AttachmentDetail ad) throws AttachmentException;

    public AttachmentDetail findNext(AttachmentDetail ad) throws AttachmentException;

    public Vector getAttachmentsByPKAndParam(AttachmentPK foreignKey, String nameAttribut, String valueAttribut) throws AttachmentException;

	public Vector getAttachmentsByPKAndContext(AttachmentPK foreignKey, String context, Connection con) throws AttachmentException;

    public void deleteAttachment(AttachmentPK primaryKey) throws AttachmentException;
    
    public void updateForeignKey(AttachmentPK pk, String foreignKey) throws AttachmentException;
    
    // pour la gestion des retards sur les réservations de fichiers
    public Collection getAllAttachmentByDate(Date date, boolean alert) throws AttachmentException;
    public Collection getAllAttachmentToLib(Date date) throws AttachmentException;
    
    public void notifyUser(NotificationMetaData notifMetaData, String senderId, String componentId) throws AttachmentException;
    
    public void updateXmlForm(AttachmentPK pk, String language, String xmlFormName) throws AttachmentException;
}