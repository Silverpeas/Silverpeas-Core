package com.silverpeas.personalisation.dao;

/**
 * Created by IntelliJ IDEA. User: ehugonnet Date: 08/03/11 Time: 11:39 To change this template use
 * File | Settings | File Templates.
 */
public interface PersonalizationDao {
  String PERSONALCOLUMNNAMES =
      "id, languages, look, personalWSpace, thesaurusStatus, dragAndDropStatus, onlineEditingStatus, webdavEditingStatus";
  String PERSONALTABLENAME = "Personalization";
}
