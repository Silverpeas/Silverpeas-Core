/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.attachment.repository;

import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.persistence.jcr.AbstractJcrConverter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static org.silverpeas.core.persistence.jcr.util.JcrConstants.*;

/**
 *
 * @author ehugonnet
 */
class SimpleAttachmentConverter extends AbstractJcrConverter {

  public SimpleAttachment convertNode(Node node) throws RepositoryException {
    SimpleAttachment attachment = new SimpleAttachment();
    attachment.setContentType(getStringProperty(node, JCR_MIMETYPE));
    attachment.setCreated(getDateProperty(node, SLV_PROPERTY_CREATION_DATE));
    attachment.setCreatedBy(getStringProperty(node, SLV_PROPERTY_CREATOR));
    attachment.setDescription(getStringProperty(node, JCR_DESCRIPTION));
    attachment.setFilename(getStringProperty(node, SLV_PROPERTY_NAME));
    attachment.setLanguage(getStringProperty(node, JCR_LANGUAGE));
    attachment.setSize(getLongProperty(node, SLV_PROPERTY_SIZE));
    attachment.setTitle(getStringProperty(node, JCR_TITLE));
    attachment.setUpdated(getDateProperty(node, JCR_LAST_MODIFIED));
    attachment.setUpdatedBy(getStringProperty(node, JCR_LAST_MODIFIED_BY));
    attachment.setXmlFormId(getStringProperty(node, SLV_PROPERTY_XMLFORM_ID));
    return attachment;
  }

  public void fillNode(SimpleAttachment attachment, Node node) throws RepositoryException {
    addDateProperty(node, SLV_PROPERTY_CREATION_DATE, attachment.getCreated());
    addStringProperty(node, SLV_PROPERTY_CREATOR, attachment.getCreatedBy());
    addStringProperty(node, JCR_DESCRIPTION, attachment.getDescription());
    addStringProperty(node, SLV_PROPERTY_NAME, attachment.getFilename());
    addStringProperty(node, JCR_LANGUAGE, attachment.getLanguage());
    addStringProperty(node, JCR_TITLE, attachment.getTitle());
    addDateProperty(node, JCR_LAST_MODIFIED, attachment.getUpdated());
    addStringProperty(node, JCR_LAST_MODIFIED_BY, attachment.getUpdatedBy());
    addStringProperty(node, SLV_PROPERTY_XMLFORM_ID, attachment.getXmlFormId());
    addStringProperty(node, JCR_MIMETYPE, attachment.getContentType());
    node.setProperty(SLV_PROPERTY_SIZE, attachment.getSize());
  }
}
