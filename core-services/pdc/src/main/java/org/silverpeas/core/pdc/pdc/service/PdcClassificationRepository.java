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
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.repository.jpa.JpaBasicEntityManager;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import javax.inject.Singleton;
import java.util.List;

/**
 * DAO that handles the persistence of PdcClassification beans.
 */
@Singleton
public class PdcClassificationRepository
    extends JpaBasicEntityManager<PdcClassification, UniqueLongIdentifier> {

  /**
   * Finds the predefined classification on the PdC that is set for the whole specified component
   * instance.
   * @param instanceId the unique identifier of the component instance.
   * @return the predefined classification that is set to the component instance, or null if no
   * predefined classification was set for the component instance.
   */
  public PdcClassification findPredefinedClassificationByComponentInstanceId(String instanceId) {
    NamedParameters parameters = newNamedParameters();
    parameters.add("instanceId", instanceId);
    return findOneByNamedQuery("findByComponentInstanceId", parameters);
  }

  /**
   * Finds the predefined classification on the PdC that is set for the contents in the specified
   * node of the specified component instance.
   * @param nodeId the unique identifier of the node.
   * @param instanceId the unique identifier of the component instance to which the node belongs.
   * @return either the predefined classification associated with the node or null if no predefined
   * classification exists for that node.
   */
  public PdcClassification findPredefinedClassificationByNodeId(String nodeId, String instanceId) {
    NamedParameters parameters = newNamedParameters();
    parameters.add("nodeId", nodeId).add("instanceId", instanceId);
    return findOneByNamedQuery("findByNodeId", parameters);
  }

  /**
   * Finds all classifications on the PdC that have at least one position with the one or more of
   * the specified axis values. If no such values exist, then an empty list is returned.
   * @param values a list of PdC's axis values.
   * @return a list of classifications having at least one of the specified values or an empty list.
   */
  public List<PdcClassification> findClassificationsByPdcAxisValues(
      final List<PdcAxisValue> values) {
    NamedParameters parameters = newNamedParameters();
    parameters.add("values", values);
    return findByNamedQuery("findByPdcAxisValues", parameters);
  }

  /**
   * Deletes all the classifications (both the predefined ones and the content's ones) that were
   * set in the specified component instance. This method is generally used when a component
   * instance is being deleted.
   * @param instanceId the unique identifier of the component instance.
   */
  public void deleteAllClassificationsByComponentInstanceId(String instanceId) {
    NamedParameters parameters = newNamedParameters();
    parameters.add("instanceId", instanceId);
    deleteFromJpqlQuery("delete from PdcClassification c where c.instanceId = :instanceId",
        parameters);
  }
}
