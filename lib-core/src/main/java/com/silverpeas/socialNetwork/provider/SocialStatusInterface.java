package com.silverpeas.socialNetwork.provider;

import com.silverpeas.socialNetwork.model.SocialInformation;
import java.util.List;


import com.stratelia.webactiv.util.exception.SilverpeasException;

public interface SocialStatusInterface {

  /**
   * get list of socialInformation
   * according number of Item and the first Index
   * @param userId
   * @param numberOfElement
   * @param firstIndex
   * @return
   * @throws SilverpeasException
   */
  public List getSocialInformationsList(String userId, int numberOfElement, int firstIndex) throws
      SilverpeasException;
/**
   * get list of socialInformation of my contacts
   * according to ids of my contacts , number of Item and the first Index
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return
   * @throws SilverpeasException
   */
  public List getSocialInformationsListOfMyContacts(List<String> myContactsIds,
      int numberOfElement, int firstIndex);
}
