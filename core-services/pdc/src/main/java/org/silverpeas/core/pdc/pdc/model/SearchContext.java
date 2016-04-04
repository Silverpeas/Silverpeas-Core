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

package org.silverpeas.core.pdc.pdc.model;

import org.silverpeas.core.contribution.contentcontainer.container.ContainerPositionInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas EYSSERIC
 */
public class SearchContext implements ContainerPositionInterface, java.io.Serializable {

  private static final long serialVersionUID = 3377353406396353627L;
  private ArrayList<SearchCriteria> criterias = new ArrayList<SearchCriteria>();
  private String userId = null; // user who search

  public SearchContext(String userId) {
    this.userId = userId;
  }

  public List<SearchCriteria> getCriterias() {
    return criterias;
  }

  public void addCriteria(SearchCriteria criteria) {

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
      criteria = criterias.get(c);
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