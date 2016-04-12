/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.core.node.notification.NodeEvent;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.system.CDIResourceEventListener;

import javax.inject.Inject;

/**
 * Listener of notifications about some events that can have an impact on the PdC or on the
 * classification on the PdC.
 * @author mmoquillon
 */
public class PdcNodeEventListener extends CDIResourceEventListener<NodeEvent> {

  @Inject
  private PdcClassificationService service;

  @Override
  public void onDeletion(final NodeEvent event) throws Exception {
    NodePK nodePK = event.getTransition().getBefore().getNodePK();
    service.deletePreDefinedClassification(nodePK.getId(), nodePK.getInstanceId());
  }
}
