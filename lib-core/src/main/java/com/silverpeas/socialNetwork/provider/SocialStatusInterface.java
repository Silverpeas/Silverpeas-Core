package com.silverpeas.socialNetwork.provider;

import java.util.List;


import com.stratelia.webactiv.util.exception.SilverpeasException;

public interface SocialStatusInterface {

  /*
   * get list of socialInformation according to the type of information and number of Item and the first Index
   * @return: List <SocialInformation>
   * @param: String userId, int numberOfElement, int firstIndex
   */

  public List getSocialInformationsList(String userId, int numberOfElement, int firstIndex) throws
      SilverpeasException;
}
