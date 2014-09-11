/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.publication.web;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.CharEncoding;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.node.web.NodeEntity;

/**
 * Web entity representing a publication that can be serialized into a given media type (JSON, XML)
 * and accessed on sharing mode.
 */
public class SharedPublicationEntity extends PublicationEntity {

  private static final long serialVersionUID = -710565351635262776L;

  protected SharedPublicationEntity(PublicationDetail publication, URI uri) {
    super(publication, uri);
  }

  /**
   * Creates a new publication entity from the specified publication.
   *
   * @param publication the publication to identify.
   * @return the entity representing the specified node.
   */
  public static SharedPublicationEntity fromPublicationDetail(
      final PublicationDetail publication, URI uri) {
    return new SharedPublicationEntity(publication, uri);
  }

  @Override
  protected URI getAttachmentURI(SimpleDocument attachment, String baseURI, String token) {
    URI sharedUri;
    try {
      sharedUri = new URI(baseURI + "sharing/attachments/" + attachment.getInstanceId() + "/"
          + token + "/" + attachment.getId() + "/"
          + URLEncoder.encode(attachment.getFilename(), CharEncoding.UTF_8));
    } catch (UnsupportedEncodingException ex) {
      Logger.getLogger(NodeEntity.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    } catch (URISyntaxException ex) {
      Logger.getLogger(NodeEntity.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }

    return sharedUri;
  }
  
}
