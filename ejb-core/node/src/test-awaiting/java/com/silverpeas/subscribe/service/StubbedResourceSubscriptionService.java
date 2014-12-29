/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package com.silverpeas.subscribe.service;

import com.stratelia.webactiv.node.control.NodeService;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
* @author Yohann Chastagnier
*/
public class StubbedResourceSubscriptionService
    extends AbstractResourceSubscriptionService {

  @Override
  protected String getHandledComponentName() {
    return DefaultResourceSubscriptionService.DEFAULT_IMPLEMENTATION_ID;
  }

  private NodeService mock = Mockito.mock(NodeBm.class);

  public StubbedResourceSubscriptionService() {
    when(mock.getPath(any(NodePK.class))).thenAnswer(new Answer<Collection<NodeDetail>>() {
      @Override
      public Collection<NodeDetail> answer(final InvocationOnMock invocation) throws Throwable {
        NodePK parameter = (NodePK) invocation.getArguments()[0];
        Collection<NodeDetail> path = new ArrayList<NodeDetail>();
        NodeDetail currentNodeDetail = new NodeDetail();
        currentNodeDetail.setNodePK(parameter);
        path.add(currentNodeDetail);
        if (!parameter.getId().equals("0")) {
          currentNodeDetail = new NodeDetail();
          currentNodeDetail.setNodePK(new NodePK("0", parameter.getInstanceId()));
          path.add(currentNodeDetail);
        }
        return path;
      }
    });
  }

  @Override
  protected NodeService getNodeService() {
    return mock;
  }
}
