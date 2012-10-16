/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.util.publication.model;

import java.io.Serializable;
import java.util.List;

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;

/**
 * This object contains the description of a complete publication (publication parameter, model
 * detail, info)
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class CompletePublication implements Serializable {

  private static final long serialVersionUID = 7644813195325660580L;

  private PublicationDetail pubDetail;
  private ModelDetail modelDetail;
  private InfoDetail infoDetail;

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
   * @see com.stratelia.webactiv.util.publication.model.PulicationDetail
   * @see com.stratelia.webactiv.util.publication.info.model.ModelDetail
   * @see com.stratelia.webactiv.util.publication.info.model.InfoDetail
   */
  public CompletePublication(PublicationDetail pubDetail, ModelDetail modelDetail,
      InfoDetail infoDetail, List<ForeignPK> linkList, List<ForeignPK> reverseLinkList) {
    this.pubDetail = pubDetail;
    this.modelDetail = modelDetail;
    this.infoDetail = infoDetail;
    this.linkList = linkList;
    this.reverseLinkList = reverseLinkList;
  }

  /**
   * Create a new CompletePublication
   * @param pubDetail
   * @param modelDetail
   * @param infoDetail
   * @see com.stratelia.webactiv.util.publication.model.PulicationDetail
   * @see com.stratelia.webactiv.util.publication.info.model.ModelDetail
   * @see com.stratelia.webactiv.util.publication.info.model.InfoDetail
   * @since 1.0
   */
  public CompletePublication(PublicationDetail pubDetail,
      ModelDetail modelDetail, InfoDetail infoDetail) {
    this.pubDetail = pubDetail;
    this.modelDetail = modelDetail;
    this.infoDetail = infoDetail;
  }

  /**
   * Get the publication parameters
   * @return a PublicationDetail - the publication parameters
   * @see com.stratelia.webactiv.util.publication.model.PulicationDetail
   * @since 1.0
   */
  public PublicationDetail getPublicationDetail() {
    return pubDetail;
  }

  /**
   * Get the model detail associated to the publication
   * @return a ModelDetail
   * @see com.stratelia.webactiv.util.publication.info.model.ModelDetail
   * @since 1.0
   */
  public ModelDetail getModelDetail() {
    return modelDetail;
  }

  /**
   * Get the info detail associated to the publication
   * @return a InfoDetail
   * @see com.stratelia.webactiv.util.publication.info.model.InfoDetail
   * @since 1.0
   */
  public InfoDetail getInfoDetail() {
    return infoDetail;
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