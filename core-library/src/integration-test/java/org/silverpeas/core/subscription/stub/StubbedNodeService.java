package org.silverpeas.core.subscription.stub;

import org.silverpeas.core.node.service.DefaultNodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * Stubbed node service implementation in order to simulate treatment behavior without setting all
 * the data...<br/>
 * @author Yohann Chastagnier
 */
@Singleton
@Alternative
@Priority(APPLICATION + 10)
public class StubbedNodeService extends DefaultNodeService {

  @Override
  public Collection<NodeDetail> getPath(final NodePK pk) {
    Collection<NodeDetail> path = new ArrayList<NodeDetail>();
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
