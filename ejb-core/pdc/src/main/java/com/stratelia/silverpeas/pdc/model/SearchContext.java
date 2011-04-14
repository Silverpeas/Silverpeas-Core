/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.pdc.model;

import java.util.ArrayList;

import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * @author Nicolas EYSSERIC
 */
public class SearchContext implements ContainerPositionInterface, java.io.Serializable {

  private static final long serialVersionUID = 3377353406396353627L;
  private ArrayList<SearchCriteria> criterias = new ArrayList<SearchCriteria>();
  private String userId = null; // user who search

  public SearchContext() {
  }

  public SearchContext(ArrayList<SearchCriteria> criterias) {
    this.criterias = criterias;
  }

  public ArrayList<SearchCriteria> getCriterias() {
    return criterias;
  }

  public void addCriteria(SearchCriteria criteria) {
    SilverTrace.info("Pdc", "SearchContext.addCriteria()",
        "root.MSG_GEN_PARAM_VALUE", "criteria = " + criteria.toString());
    if (criterias == null) {
      criterias = new ArrayList<SearchCriteria>();
    }

    // recherche de l'existance d'un critère sur l'axe
    SearchCriteria existingCriteriaOnAxis = getCriteriaOnAxis(criteria
        .getAxisId());
    if (existingCriteriaOnAxis != null) {
      // un critère sur l'axe existe déjà
      // on le supprime du contexte
      removeCriteria(existingCriteriaOnAxis);
    }

    criterias.add(criteria);
  }

  public void clearCriterias() {
    SilverTrace.info("Pdc", "SearchContext.clearCriterias()",
        "root.MSG_GEN_PARAM_VALUE");
    criterias = new ArrayList<SearchCriteria>();
  }

  public void removeCriteria(SearchCriteria criteria) {
    if (criterias != null) {
      criterias.remove(criteria);
    }
  }

  public void removeCriteria(int axisId) {
    SearchCriteria criteria = getCriteriaOnAxis(axisId);
    if (criteria != null) {
      removeCriteria(criteria);
    }
  }

  /** Return true if the position is empty */
  public boolean isEmpty() {
    return (criterias.size() == 0);
  }

  public SearchCriteria getCriteriaOnAxis(int axisId) {
    SearchCriteria criteria = null;
    for (int c = 0; criterias != null && c < criterias.size(); c++) {
      criteria = (SearchCriteria) criterias.get(c);
      if (criteria.getAxisId() == axisId)
        return criteria;
    }
    return null;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

}