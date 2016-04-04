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

package org.silverpeas.core.contribution.contentcontainer.container;

import java.util.List;

/**
 * This is the interfcae on data structure that the content JSP is going to use (built by the
 * container router)
 */
public interface ContainerContext {
  /* Get the URL to get Back on the container */
  public String getReturnURL();

  /* Get the URLIcone corresponding to classify */
  public URLIcone getClassifyURLIcone();

  /* Get the classify URL with parameters to put as link on the Classify Icone */
  public String getClassifyURLWithParameters(String sComponentId,
      String sSilverContentId);

  /** Find the SearchContext for the given SilverContentId */
  public ContainerPositionInterface getSilverContentIdSearchContext(
      int nSilverContentId, String sComponentId);

  /*
   * Get All the SilverContentIds corresponding to the given position in the given Components
   */
  public List<Integer> getSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List<String> alComponentIds);
}
