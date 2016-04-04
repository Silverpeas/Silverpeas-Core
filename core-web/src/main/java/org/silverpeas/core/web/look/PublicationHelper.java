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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.web.look;

import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;

import java.util.List;

/**
 * @author ehugonnet
 */
public interface PublicationHelper {

  public List<PublicationDetail> getPublications(String spaceId, int nbPublis);

  /**
   * Get the last publications of a space updated since a specified date.
   * @param spaceId the id of the space.
   * @param since the number of days to be taken into account. If 0 or negativ, no limit is taken.
   * @param nbPublis the max number of publications returned.
   * @return the last publications of a space updated since a specified date.
   */
  public List<PublicationDetail> getUpdatedPublications(String spaceId, int since, int nbPublis);

  public void setMainSessionController(MainSessionController mainSC);
}
