/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.node.servlets;

import static com.stratelia.silverpeas.peasCore.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;

import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.RestRequest;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

public class GetNodes extends HttpServlet {

  private static final long serialVersionUID = -6406943829713290811L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException,
      IOException {

    response.setContentType("application/json");
    Writer writer = response.getWriter();

    HttpSession session = request.getSession(true);
    MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(
        MAIN_SESSION_CONTROLLER_ATT);
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
      List<String> availableComponentIds = new ArrayList<String>();
      for (String componentId : componentIds) {
        if (mainSessionCtrl.getOrganizationController().isComponentAvailable(componentId,
            mainSessionCtrl.getUserId())) {
          availableComponentIds.add(componentId);
        }
      }

      if (availableComponentIds.size() == 1) {
        // only one instance is available, root must be expanded by default
        try {
          NodeDetail node = getRoot(availableComponentIds.get(0), mainSessionCtrl);
          JSONObject root = getNodeAsJSTreeObject(node, mainSessionCtrl, "open");
          writer.write(root.toString());
        } catch (CreateException e) {
          writer.write("Silverpeas Node Service is unavailable");
        }
      } else {
        // at least two instances are available, only root of instances have to be displayed
        List<NodeDetail> nodes = new ArrayList<NodeDetail>();
        for (String componentId : availableComponentIds) {
          try {
            nodes.add(getRoot(componentId, mainSessionCtrl));
          } catch (CreateException e) {
            writer.write("Silverpeas Node Service is unavailable");
          }
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
    try {
      NodeDetail node = getNodeBm().getDetail(pk);
      List<NodeDetail> availableChildren = getAvailableChildren(node.getChildrenDetails(), session);
      writer.write(getListAsJSONArray(availableChildren, session));
    } catch (CreateException e) {
      writer.write("Silverpeas Node Service is unavailable");
    }
  }

  private NodeDetail getRoot(String componentId, MainSessionController session)
      throws RemoteException, CreateException {
    NodeDetail node = getNodeBm().getHeader(new NodePK("0", componentId));
    ComponentInstLight component =
        session.getOrganizationController().getComponentInstLight(componentId);
    if (component != null) {
      node.setName(component.getLabel(session.getFavoriteLanguage()));
    }
    return node;
  }

  private List<NodeDetail> getAvailableChildren(Collection<NodeDetail> children,
      MainSessionController session) throws RemoteException, CreateException {
    List<NodeDetail> availableChildren = new ArrayList<NodeDetail>();
    for (NodeDetail child : children) {
      String childId = child.getNodePK().getId();
      if (child.getNodePK().isTrash() || childId.equals("2")) {
        // do not add these nodes
      } else if (!child.haveRights()) {
        availableChildren.add(child);
      } else {
        int rightsDependsOn = child.getRightsDependsOn();
        boolean nodeAvailable =
            session.getOrganizationController().isObjectAvailable(rightsDependsOn, ObjectType.NODE,
            child.getNodePK().getInstanceId(), session.getUserId());
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
              if (session.getOrganizationController().isObjectAvailable(
                  descendant.getRightsDependsOn(), ObjectType.NODE, child.getNodePK().
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
    JSONArray jsonArray = new JSONArray();
    for (NodeDetail node : nodes) {
      JSONObject jsonObject = getNodeAsJSTreeObject(node, session, "closed");
      jsonArray.put(jsonObject);
    }

    return jsonArray.toString();
  }

  private JSONObject getNodeAsJSTreeObject(NodeDetail node, MainSessionController session,
      String state) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("data", node.getName(session.getFavoriteLanguage()));
    jsonObject.put("attr", getAttr(node, session));
    jsonObject.put("state", state);

    return jsonObject;
  }

  private JSONObject getAttr(NodeDetail node, MainSessionController session) {
    JSONObject o = new JSONObject();
    o.put("id", node.getNodePK().getId());
    o.put("instanceId", node.getNodePK().getInstanceId());
    String role = getRole(session, node);
    if (node.getNodePK().isRoot()) {
      role += "-root";
    }
    o.put("rel", role);
    return o;
  }

  private boolean isRightsEnabled(MainSessionController session, String componentId) {
    return StringUtil.getBooleanValue(session.getComponentParameterValue(componentId,
        "rightsOnTopics"));
  }

  private String getRole(MainSessionController session, NodeDetail node) {
    if (node.getNodePK().isRoot() && !isPublicationAllowedOnRoot(node.getNodePK(), session)) {
      return SilverpeasRole.user.toString();
    }
    if (!isRightsEnabled(session, node.getNodePK().getInstanceId())) {
      return getProfile(session.getOrganizationController().getUserProfiles(session.getUserId(),
          node.getNodePK().getInstanceId()));
    }

    // check if we have to take care of topic's rights
    if (node != null && node.haveRights()) {
      int rightsDependsOn = node.getRightsDependsOn();
      return getProfile(session.getOrganizationController().getUserProfiles(session.getUserId(),
          node.getNodePK().getInstanceId(), rightsDependsOn, ObjectType.NODE));
    } else {
      return getProfile(session.getOrganizationController().getUserProfiles(session.getUserId(),
          node.getNodePK().getInstanceId()));
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

  private NodeBm getNodeBm() throws RemoteException, CreateException {
    NodeBmHome nodeBmHome =
        EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
    return nodeBmHome.create();
  }

  private boolean isPublicationAllowedOnRoot(NodePK pk, MainSessionController session) {
    if (pk.getInstanceId().startsWith("toolbox")) {
      return true;
    }
    String param = session.getComponentParameterValue(pk.getInstanceId(), "nbPubliOnRoot");
    return StringUtil.isInteger(param) && Integer.parseInt(param) == 0;
  }

}