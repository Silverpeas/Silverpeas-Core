/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.formTemplate.ejb;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.EJBObject;

import com.silverpeas.form.DataRecord;
import com.silverpeas.publicationTemplate.PublicationTemplate;

/**
 * Interface declaration
 * @author neysseri
 */
public interface FormTemplateBm extends EJBObject {
  public DataRecord getRecord(String externalId, String id)
      throws RemoteException;

  public PublicationTemplate getPublicationTemplate(String externalId)
      throws RemoteException;

  public List getXMLFieldsForExport(String externalId, String id)
      throws RemoteException;

  public List getXMLFieldsForExport(String externalId, String id,
      String language) throws RemoteException;

}