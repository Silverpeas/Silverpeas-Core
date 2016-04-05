/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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

package org.silverpeas.core.contribution.publication.model;

import java.io.Serializable;
import java.util.List;

import org.silverpeas.core.ForeignPK;

/**
 * This object contains the description of a complete publication (publication parameter, info)
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class CompletePublication implements Serializable {

  private static final long serialVersionUID = 7644813195325660580L;

  private PublicationDetail pubDetail;

  /**
   * The publications linked to the current publication
   */
  private List<ForeignPK> linkList = null;

  /**
   * The publications which are a reference to the current publication
   */
  private List<ForeignPK> reverseLinkList = null;

  private List<ValidationStep> validationSteps = null;

  /**
   * @param pubDetail
   * @param modelDetail
   * @param infoDetail
   * @param linkList The publications linked to the current publication
   * @param reverseLinkList The publications which are a reference to the current publication
   * @see org.silverpeas.core.contribution.publication.model.PulicationDetail
   * @see org.silverpeas.core.contribution.publication.info.model.ModelDetail
   * @see org.silverpeas.core.contribution.publication.info.model.InfoDetail
   */
  public CompletePublication(PublicationDetail pubDetail, List<ForeignPK> linkList,
      List<ForeignPK> reverseLinkList) {
    this.pubDetail = pubDetail;
    this.linkList = linkList;
    this.reverseLinkList = reverseLinkList;
  }

  /**
   * Get the publication parameters
   * @return a PublicationDetail - the publication parameters
   * @see org.silverpeas.core.contribution.publication.model.PulicationDetail
   * @since 1.0
   */
  public PublicationDetail getPublicationDetail() {
    return pubDetail;
  }

  /**
   * @return the linkList
   */
  public List<ForeignPK> getLinkList() {
    return linkList;
  }

  /**
   * @param linkList the linkList to set
   */
  public void setLinkList(List<ForeignPK> linkList) {
    this.linkList = linkList;
  }

  /**
   * @return the reverseLinkList
   */
  public List<ForeignPK> getReverseLinkList() {
    return reverseLinkList;
  }

  /**
   * @param reverseLinkList the reverseLinkList to set
   */
  public void setReverseLinkList(List<ForeignPK> reverseLinkList) {
    this.reverseLinkList = reverseLinkList;
  }

  public void setValidationSteps(List<ValidationStep> validationSteps) {
    this.validationSteps = validationSteps;
  }

  public List<ValidationStep> getValidationSteps() {
    return validationSteps;
  }
}