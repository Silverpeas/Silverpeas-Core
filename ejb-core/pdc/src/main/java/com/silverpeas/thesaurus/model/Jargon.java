package com.silverpeas.thesaurus.model;

import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.thesaurus.control.ThesaurusBm;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * This class contains a full information about a Jargon a Jargon is linked to a
 * Vocabulary and a User (UserDetail or Group)
 */

public class Jargon extends SilverpeasBean {

  private int type; // 0=User, 1=Group
  private long idVoca;
  private String idUser;

  private ThesaurusBm thesaurus = ThesaurusBm.getInstance();
  private static OrganizationController organizationController = new OrganizationController();

  public Jargon() {
  }

  public String readUserName() {
    String userName = null;
    if (type == 0) // user
    {
      UserDetail userDetail = organizationController.getUserDetail(new Long(
          getIdUser()).toString());
      if (userDetail != null)
        userName = userDetail.getLastName() + " " + userDetail.getFirstName();
    } else // group
    {
      Group group = organizationController.getGroup(new Long(getIdUser())
          .toString());
      if (group != null)
        userName = group.getName();
    }
    return userName;
  }

  public String readVocaName() throws ThesaurusException {
    String name = "";

    try {
      if (this.idVoca != 0) {
        Vocabulary voca = thesaurus.getVocabulary(this.idVoca);
        name = voca.getName();
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException("Jargon.readVocaName",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_VOCABULARY_NAME",
          "", e);
    }

    return name;

  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public long getIdVoca() {
    return idVoca;
  }

  public void setIdVoca(long idVoca) {
    this.idVoca = idVoca;
  }

  public String getIdUser() {
    return idUser;
  }

  public void setIdUser(String idUser) {
    this.idUser = idUser;
  }

  public boolean equals(Object theOther) {
    return (getIdUser().equals(((Jargon) theOther).getIdUser()) && getType() == ((Jargon) theOther)
        .getType());
  }

  public String _getTableName() {
    return "SB_Thesaurus_Jargon";
  }

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

}