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

package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.core.pdc.pdc.model.PdcAxisValue;
import org.silverpeas.core.pdc.pdc.model.PdcAxisValuePk;
import org.silverpeas.core.persistence.datasource.repository.jpa.JpaBasicEntityManager;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import javax.inject.Singleton;
import java.util.List;

/**
 * DAO that handles the persistence of PdcAxisValue beans.
 */
@Singleton
public class PdcAxisValueRepository extends JpaBasicEntityManager<PdcAxisValue, PdcAxisValuePk> {

  /**
   * Finds all the values of the specified PdC's axis.
   * @param axisId the unique identifier of the axis.
   * @return a list of the values of the specified axis.
   */
  public List<PdcAxisValue> findByAxisId(Long axisId) {
    NamedParameters parameters = newNamedParameters().add("axisId", axisId);
    return findByNamedQuery("findByAxisId", parameters);
  }
}
