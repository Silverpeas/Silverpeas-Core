/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.lookV5;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import com.silverpeas.look.LookHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.instance.control.WAComponent;

public class PersonalSpaceJSONServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    SilverTrace.info("lookSilverpeasV5", "JSONServlet.doPost", "root.MSG_GEN_ENTER_METHOD");

    HttpSession session = req.getSession(true);
    MainSessionController m_MainSessionCtrl =
        (MainSessionController) session.getAttribute("SilverSessionController");
    LookHelper helper = (LookHelper) session.getAttribute("Silverpeas_LookHelper");
    OrganizationController orgaController = m_MainSessionCtrl.getOrganizationController();
    String userId = m_MainSessionCtrl.getUserId();

    res.setContentType("application/json");

    String action = req.getParameter("Action");

    Writer writer = res.getWriter();

    if ("GetAvailableComponents".equals(action)) {
      PersonalSpaceController psc = new PersonalSpaceController();
      Collection<WAComponent> components = psc.getVisibleComponents(orgaController);
      SpaceInst space = psc.getPersonalSpace(userId);
      if (space != null) {
        writer.write(getWAComponentsAsJSONArray(getNotUsedComponents(components, space), helper));
      } else {
        writer.write(getWAComponentsAsJSONArray(components, helper));
      }
    } else if ("GetComponents".equals(action)) {
      PersonalSpaceController psc = new PersonalSpaceController();
      SpaceInst space = psc.getPersonalSpace(userId);
      if (space != null) {
        writer.write(getComponentsAsJSONArray(space.getAllComponentsInst(), helper));
      }
    } else if ("AddComponent".equals(action)) {
      String componentName = req.getParameter("ComponentName");
      PersonalSpaceController psc = new PersonalSpaceController();
      try {
        String componentId =
            psc.addComponent(helper.getUserId(), componentName, getComponentLabel(componentName,
            helper));
        writer.write(getResult(componentName, componentId, null, helper).toString());
      } catch (AdminException e) {
        writer.write(getResult(componentName, null, e, helper).toString());
        e.printStackTrace();
      }
    } else if ("RemoveComponent".equals(action)) {
      String componentId = req.getParameter("ComponentId");
      PersonalSpaceController psc = new PersonalSpaceController();
      try {
        String componentName = psc.removeComponent(userId, componentId);
        writer.write(getResult(componentName, componentId, null, helper).toString());
      } catch (AdminException e) {
        writer.write(getResult(null, componentId, e, helper).toString());
        e.printStackTrace();
      }
    }
  }

  private Collection<WAComponent> getNotUsedComponents(Collection<WAComponent> components,
      SpaceInst space) {
    Collection<WAComponent> availables = new ArrayList<WAComponent>();
    Collection<ComponentInst> used = space.getAllComponentsInst();
    for (WAComponent component : components) {
      if (!isComponentUsed(component, used)) {
        availables.add(component);
      }
    }
    return availables;
  }

  private boolean isComponentUsed(WAComponent component, Collection<ComponentInst> componentsUsed) {
    for (ComponentInst componentUsed : componentsUsed) {
      if (componentUsed.getName().equalsIgnoreCase(component.getName())) {
        return true;
      }
    }
    return false;
  }

  private String getWAComponentsAsJSONArray(Collection<WAComponent> components, LookHelper helper) {
    JSONArray jsonArray = new JSONArray();
    for (WAComponent component : components) {
      JSONObject jsonObject = getWAComponentAsJSONObject(component, helper);
      jsonArray.put(jsonObject);
    }
    return jsonArray.toString();
  }

  private String getComponentsAsJSONArray(Collection<ComponentInst> components, LookHelper helper) {
    JSONArray jsonArray = new JSONArray();
    for (ComponentInst component : components) {
      JSONObject jsonObject = getComponentAsJSONObject(component, helper);
      jsonArray.put(jsonObject);
    }
    return jsonArray.toString();
  }

  private JSONObject getWAComponentAsJSONObject(WAComponent component, LookHelper helper) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("name", component.getName());
    jsonObject.put("description", component.getDescription());
    jsonObject.put("label", getComponentLabel(component.getName(), helper));

    return jsonObject;
  }

  private JSONObject getComponentAsJSONObject(ComponentInst component, LookHelper helper) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("name", component.getName());
    jsonObject.put("description", component.getDescription());
    jsonObject.put("label", getComponentLabel(component.getName(), helper));
    jsonObject.put("id", component.getId());

    return jsonObject;
  }

  private JSONObject getResult(String componentName, String componentId, Exception e,
      LookHelper helper) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("name", componentName);
    jsonObject.put("label", getComponentLabel(componentName, helper));
    jsonObject.put("successfull", e == null);
    if (componentId != null) {
      jsonObject.put("id", componentId);
      jsonObject.put("url", URLManager.getURL(componentName, "useless", componentId) + "Main");
    }
    if (e != null) {
      jsonObject.put("exception", e.toString());
    }
    return jsonObject;
  }

  private String getComponentLabel(String componentName, LookHelper helper) {
    String label = helper.getString("lookSilverpeasV5.personalSpace." + componentName);
    if (!StringUtil.isDefined(label)) {
      label = componentName;
    }
    return label;
  }

}
