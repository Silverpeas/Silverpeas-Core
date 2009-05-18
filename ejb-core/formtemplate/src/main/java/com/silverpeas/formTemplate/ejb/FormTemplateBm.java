package com.silverpeas.formTemplate.ejb;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.EJBObject;

import com.silverpeas.form.DataRecord;
import com.silverpeas.publicationTemplate.PublicationTemplate;

/**
 * Interface declaration
 *
 * @author neysseri
 */
public interface FormTemplateBm extends EJBObject 
{
	public DataRecord getRecord(String externalId, String id) throws RemoteException;
	
	public PublicationTemplate getPublicationTemplate(String externalId) throws RemoteException;
	
	public List getXMLFieldsForExport(String externalId, String id) throws RemoteException;
	
	public List getXMLFieldsForExport(String externalId, String id, String language) throws RemoteException;

}