package com.stratelia.webactiv.util.publication.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;

/**
 * This object contains the description of a complete publication (publication
 * parameter, model detail, info)
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class CompletePublication implements Serializable {

  private PublicationDetail pubDetail;
  private ModelDetail modelDetail;
  private InfoDetail infoDetail;

  /**
   * Create a new CompletePublication
   * 
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
   * 
   * @return a PublicationDetail - the publication parameters
   * @see com.stratelia.webactiv.util.publication.model.PulicationDetail
   * @since 1.0
   */
  public PublicationDetail getPublicationDetail() {
    return pubDetail;
  }

  /**
   * Get the model detail associated to the publication
   * 
   * @return a ModelDetail
   * @see com.stratelia.webactiv.util.publication.info.model.ModelDetail
   * @since 1.0
   */
  public ModelDetail getModelDetail() {
    return modelDetail;
  }

  /**
   * Get the info detail associated to the publication
   * 
   * @return a InfoDetail
   * @see com.stratelia.webactiv.util.publication.info.model.InfoDetail
   * @since 1.0
   */
  public InfoDetail getInfoDetail() {
    return infoDetail;
  }
}