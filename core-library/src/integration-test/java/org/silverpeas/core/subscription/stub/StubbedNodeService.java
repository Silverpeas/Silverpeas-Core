/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.subscription.stub;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.DefaultNodeService;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * Stubbed node service implementation in order to simulate treatment behavior without setting all
 * the data...<br>
 * @author Yohann Chastagnier
 */
@Service
@Singleton
@Alternative
@Priority(APPLICATION + 10)
public class StubbedNodeService extends DefaultNodeService {

  @Override
  public NodePath getPath(final NodePK pk) {
    NodePath path = new NodePath();
    NodeDetail currentNodeDetail = new NodeDetail();
    currentNodeDetail.setNodePK(pk);
    path.add(currentNodeDetail);
    if (!pk.getId().equals("0")) {
      currentNodeDetail = new NodeDetail();
      currentNodeDetail.setNodePK(new NodePK("0", pk.getInstanceId()));
      path.add(currentNodeDetail);
    }
    return path;
  }
}
