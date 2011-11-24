/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.dao;

import com.silverpeas.pdc.model.PdcAxisValue;
import com.silverpeas.pdc.model.PdcAxisValuePk;
import java.util.List;
import org.synyx.hades.dao.GenericDao;

/**
 * DAO that handles the persistence of PdcAxisValue beans.
 */
public interface PdcAxisValueDAO extends GenericDao<PdcAxisValue, PdcAxisValuePk> {
  
  /**
   * Finds all the values of the specified PdC's axis.
   * @param axisId the unique identifier of the axis.
   * @return a list of the values of the specified axis.
   */
  List<PdcAxisValue> findByAxisId(Long axisId);
}
