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

package org.silverpeas.web.node.servlets;

import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.ObjectType;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.web.servlets.RestRequest;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static org.silverpeas.core.web.mvc.controller.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;

public class GetNodes extends HttpServlet {

  private static final long serialVersionUID = -6406943829713290811L;

  @Inject
  private OrganizationController organizationController;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");
    Writer writer = response.getWriter();
    HttpSession session = request.getSession(true);
    MainSessionController mainSessionCtrl =
        (MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT);
    if (mainSessionCtrl == null) {
      writer.write("This service required to be logged in !");
      return;
    }

    RestRequest req = new RestRequest(request, null);

    String scope = req.getElementValue("scope");
    if (StringUtil.isDefined(scope)) {
      // One or more application instances are requested
      String[] ids = scope.split(",");
      List<String> componentIds = Arrays.asList(ids);

      // retain only available instances for current user
      List<String> availableComponentIds = new ArrayList<>();
      for (String componentId : componentIds) {
        if (organizationController.isComponentAvailable(componentId, mainSessionCtrl.getUserId())) {
          availableComponentIds.add(componentId);
        }
      }

      if (availableComponentIds.size() == 1) {
        // only one instance is available, root must be expanded by default
        NodeDetail node = getRoot(availableComponentIds.get(0), mainSessionCtrl);
        writer.write(getNodeAsJSTreeObject(node, mainSessionCtrl, "open"));
      } else {
        // at least two instances are available, only root of instances have to be displayed
        List<NodeDetail> nodes = new ArrayList<>();
        for (String componentId : availableComponentIds) {
          nodes.add(getRoot(componentId, mainSessionCtrl));
        }
        writer.write(getListAsJSONArray(nodes, mainSessionCtrl));
      }

    } else {
      String componentId = req.getElementValue("componentid");
      String id = req.getElementValue("id");
      if (!StringUtil.isDefined(id)) {
        id = "0";
      }
      NodePK nodePK = new NodePK(id, componentId);
      getChildren(nodePK, mainSessionCtrl, writer);
    }

  }

  private void getChildren(NodePK pk, MainSessionController session, Writer writer)
      throws IOException {
    NodeDetail node = getNodeBm().getDetail(pk);
    List<NodeDetail> availableChildren = getAvailableChildren(node.getChildrenDetails(), session);
    writer.write(getListAsJSONArray(availableChildren, session));
  }

  private NodeDetail getRoot(String componentId, MainSessionController session) {
    NodeDetail node = getNodeBm().getHeader(new NodePK("0", componentId));
    ComponentInstLight component = organizationController.getComponentInstLight(componentId);
    if (component != null) {
      node.setName(component.getLabel(session.getFavoriteLanguage()));
    }
    return node;
  }

  private List<NodeDetail> getAvailableChildren(Collection<NodeDetail> children,
      MainSessionController session) {
    List<NodeDetail> availableChildren = new ArrayList<>();
    for (NodeDetail child : children) {
      String childId = child.getNodePK().getId();
      if (child.getNodePK().isTrash() || "2".equals(childId)) {
        // do not add these nodes
      } else if (!child.haveRights()) {
        availableChildren.add(child);
      } else {
        int rightsDependsOn = child.getRightsDependsOn();
        boolean nodeAvailable = organizationController
            .isObjectAvailable(rightsDependsOn, ObjectType.NODE, child.getNodePK().getInstanceId(),
                session.getUserId());
        if (nodeAvailable) {
          availableChildren.add(child);
        } else { // check if at least one descendant is available
          Iterator<NodeDetail> descendants = getNodeBm().getDescendantDetails(child).iterator();
          NodeDetail descendant = null;
          boolean childAllowed = false;
          while (!childAllowed && descendants.hasNext()) {
            descendant = descendants.next();
            if (descendant.getRightsDependsOn() == rightsDependsOn) {
              // same rights of father (which is not available) so it is not available too
            } else {
              // different rights of father check if it is available
              if (organizationController
                  .isObjectAvailable(descendant.getRightsDependsOn(), ObjectType.NODE,
                      child.getNodePK().
                          getInstanceId(), session.getUserId())) {
                childAllowed = true;
                if (!availableChildren.contains(child)) {
                  availableChildren.add(child);
                }
              }
            }
          }
        }
      }
    }
    return availableChildren;
  }

  private String getListAsJSONArray(Collection<NodeDetail> nodes, MainSessionController session) {
    return JSONCodec.encodeArray(jsonNodeDetails -> {
      for (NodeDetail node : nodes) {
        jsonNodeDetails.addJSONObject(
            root -> root.put("data", node.getName(session.getFavoriteLanguage()))
                .put("attr", getJsonAttr(node, session)).put("state", "closed"));
      }
      return jsonNodeDetails;
    });
  }

  private String getNodeAsJSTreeObject(NodeDetail node, MainSessionController session,
      String state) {
    return JSONCodec.encodeObject(
        jsonNode -> jsonNode.put("data", node.getName(session.getFavoriteLanguage()))
            .put("attr", getJsonAttr(node, session)).put("state", state));
  }

  private Function<JSONCodec.JSONArray, JSONCodec.JSONArray> getJsonAttr(NodeDetail node,
      MainSessionController session) {
    return nodeAttr -> {
      nodeAttr.addJSONObject(o -> {
        o.put("id", node.getNodePK().getId()).put("instanceId", node.getNodePK().getInstanceId());
        String role = getRole(session, node);
        if (node.getNodePK().isRoot()) {
          role += "-root";
        }
        o.put("rel", role);
        o.put("path", node.getFullPath());
        return o;
      });
      return nodeAttr;
    };
  }

  private boolean isRightsEnabled(MainSessionController session, String componentId) {
    return StringUtil
        .getBooleanValue(session.getComponentParameterValue(componentId, "rightsOnTopics"));
  }

  private String getRole(MainSessionController session, NodeDetail node) {
    if (node.getNodePK().isRoot() && !isPublicationAllowedOnRoot(node.getNodePK(), session)) {
      return SilverpeasRole.user.toString();
    }
    if (!isRightsEnabled(session, node.getNodePK().getInstanceId())) {
      return getProfile(organizationController
          .getUserProfiles(session.getUserId(), node.getNodePK().getInstanceId()));
    }

    // check if we have to take care of topic's rights
    if (node.haveRights()) {
      int rightsDependsOn = node.getRightsDependsOn();
      return getProfile(organizationController
          .getUserProfiles(session.getUserId(), node.getNodePK().getInstanceId(), rightsDependsOn,
              ObjectType.NODE));
    } else {
      return getProfile(organizationController
          .getUserProfiles(session.getUserId(), node.getNodePK().getInstanceId()));
    }
  }

  private String getProfile(String[] profiles) {
    SilverpeasRole flag = SilverpeasRole.user;
    for (String profile : profiles) {
      SilverpeasRole role = SilverpeasRole.valueOf(profile);
      switch (role) {
        case admin:
          return SilverpeasRole.admin.toString();
        case publisher:
          flag = SilverpeasRole.publisher;
          break;
        case writer:
          if (flag != SilverpeasRole.publisher) {
            flag = SilverpeasRole.writer;
          }
          break;
        case supervisor:
          flag = SilverpeasRole.supervisor;
          break;
      }
    }
    return flag.toString();
  }

  private NodeService getNodeBm() {
    return NodeService.get();
  }

  private boolean isPublicationAllowedOnRoot(NodePK pk, MainSessionController session) {
    if (pk.getInstanceId().startsWith("toolbox")) {
      return true;
    }
    String param = session.getComponentParameterValue(pk.getInstanceId(), "nbPubliOnRoot");
    return StringUtil.isInteger(param) && Integer.parseInt(param) == 0;
  }

}