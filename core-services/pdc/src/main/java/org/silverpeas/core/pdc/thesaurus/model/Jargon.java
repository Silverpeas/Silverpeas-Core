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

package org.silverpeas.core.pdc.thesaurus.model;

import org.silverpeas.core.pdc.thesaurus.service.ThesaurusService;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.exception.SilverpeasException;

/**
 * This class contains a full information about a Jargon a Jargon is linked to a Vocabulary and a
 * User (UserDetail or Group)
 */
public class Jargon extends SilverpeasBean {

  private static final long serialVersionUID = 2926231339303196258L;
  private int type; // 0=User, 1=Group
  private long idVoca;
  private String idUser;
  private ThesaurusService thesaurus = ThesaurusService.getInstance();

  public Jargon() {
  }

  public String readUserName() {
    String userName = null;
    if (type == 0) {// user
      UserDetail userDetail = OrganizationControllerProvider
          .getOrganisationController().getUserDetail(getIdUser());
      if (userDetail != null) {
        userName = userDetail.getLastName() + " " + userDetail.getFirstName();
      }
    } else { // group
      Group group = OrganizationControllerProvider.getOrganisationController().getGroup(getIdUser());
      if (group != null) {
        userName = group.getName();
      }
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
      throw new ThesaurusException("Jargon.readVocaName", SilverpeasException.ERROR,
          "Thesaurus.EX_CANT_GET_VOCABULARY_NAME", "", e);
    }
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Jargon other = (Jargon) obj;
    if (this.type != other.type) {
      return false;
    }
    if ((this.idUser == null) ? (other.idUser != null) : !this.idUser.equals(other.idUser)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 67 * hash + this.type;
    hash = 67 * hash + (this.idUser != null ? this.idUser.hashCode() : 0);
    return hash;
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

  public String _getTableName() {
    return "SB_Thesaurus_Jargon";
  }

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }
}