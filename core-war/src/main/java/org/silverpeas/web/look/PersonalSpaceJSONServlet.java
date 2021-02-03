/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.web.look;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.PersonalSpaceManager;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.user.UserNotificationServerEvent;
import org.silverpeas.core.sharing.services.SharingServiceProvider;
import org.silverpeas.core.sharing.services.SharingTicketService;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.JSONCodec.JSONArray;
import org.silverpeas.core.util.JSONCodec.JSONObject;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.external.webconnections.model.WebConnectionsInterface;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class PersonalSpaceJSONServlet extends SilverpeasAuthenticatedHttpServlet {

  private static final long serialVersionUID = 8565616592829678418L;
  private static final String DESCRIPTION = "description";
  private static final String LABEL = "label";

  @Inject
  private OrganizationController organizationController;
  @Inject
  private PersonalSpaceManager personalSpaceManager;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) {
    HttpSession session = req.getSession(true);
    LookHelper helper = LookHelper.getLookHelper(session);
    User user = UserDetail.getCurrentRequester();
    String userId = user.getId();

    res.setContentType("application/json");

    String action = req.getParameter("Action");

    try {
      Writer writer = res.getWriter();
      if ("GetAvailableComponents".equals(action)) {
        Collection<WAComponent> components = personalSpaceManager.getVisibleComponents();
        SpaceInst space = personalSpaceManager.getPersonalSpace(userId);
        if (space != null) {
          writer.write(getWAComponentsAsJSONArray(getNotUsedComponents(components, space), helper));
        } else {
          writer.write(getWAComponentsAsJSONArray(components, helper));
        }
      } else if ("GetComponents".equals(action)) {
        SpaceInst space = personalSpaceManager.getPersonalSpace(userId);
        if (space == null) {
          // Creating a dummy personal space instance which does not exist in database
          space = new SpaceInst();
          space.setPersonalSpace(true);
          space.setCreatorUserId(userId);
        }
        final List<SilverpeasComponentInstance> allComponentInstances = space.getAllComponentInstances()
            .stream()
            .filter(i -> !i.isPersonal() || PersonalComponent.getByName(i.getName())
                .filter(PersonalComponent::isVisible)
                .isPresent())
            .sorted(comparing((SilverpeasComponentInstance i) -> !i.isPersonal()).thenComparing(
                SilverpeasComponentInstance::getId))
            .collect(Collectors.toList());
        writer.write(getComponentsAsJSONArray(allComponentInstances, helper));
      } else if ("AddComponent".equals(action)) {
        addComponent(req, writer, helper, user);
      } else if ("RemoveComponent".equals(action)) {
        removeComponent(req, writer, helper, userId);
      } else if ("GetTools".equals(action)) {
        writer.write(getToolsAsJSONArray(helper));
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void removeComponent(final HttpServletRequest req, final Writer writer,
      final LookHelper helper, final String userId) throws IOException {
    String componentId = req.getParameter("ComponentId");
    try {
      String componentName = personalSpaceManager.removeComponent(userId, componentId);
      writer.write(getResult(componentName, componentId, null, helper));
    } catch (AdminException e) {
      writer.write(getResult(null, componentId, e, helper));
      SilverLogger.getLogger(this).error(e);
    }
  }

  private void addComponent(final HttpServletRequest req, final Writer writer,
      final LookHelper helper, final User user) throws IOException {
    String componentName = req.getParameter("ComponentName");
    try {
      String componentId = personalSpaceManager.addComponent(user, componentName);
      writer.write(getResult(componentName, componentId, null, helper));
    } catch (Exception e) {
      writer.write(getResult(componentName, null, e, helper));
      SilverLogger.getLogger(this).error(e);
    }
  }

  private Collection<WAComponent> getNotUsedComponents(Collection<WAComponent> components,
      SpaceInst space) {
    Collection<WAComponent> availables = new ArrayList<>();
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
    return JSONCodec.encodeArray(jsonArray -> {
      for (WAComponent component : components) {
        jsonArray.addJSONObject(getWAComponentAsJSONObject(component, helper));
      }
      return jsonArray;
    });
  }

  private String getComponentsAsJSONArray(Collection<SilverpeasComponentInstance> components,
      LookHelper helper) {
    return JSONCodec.encodeArray(jsonArray -> {
      for (SilverpeasComponentInstance component : components) {
        jsonArray.addJSONObject(getComponentAsJSONObject(component, helper));
      }
      return jsonArray;
    });
  }

  private UnaryOperator<JSONObject> getWAComponentAsJSONObject(
      WAComponent component, LookHelper helper) {
    return (jsonObject -> jsonObject.put("name", component.getName())
        .put(DESCRIPTION, component.getDescription().get(helper.getLanguage()))
        .put(LABEL, getComponentLabel(component.getName(), helper)));
  }

  private UnaryOperator<JSONObject> getComponentAsJSONObject(
      SilverpeasComponentInstance component, LookHelper helper) {
    return (jsonObject -> jsonObject.put("name", component.getName())
        .put(DESCRIPTION, component.getDescription())
        .put(LABEL, getComponentLabel(component.getName(), helper))
        .put("id", component.getId())
        .put("url", URLUtil.getURL(component.getName(), "useless",
            component.getId()) + "Main"));
  }

  private String getResult(String componentName,
      String componentId, Exception e, LookHelper helper) {
    return JSONCodec.encodeObject(jsonObject -> {
      jsonObject.put("name", componentName).put(LABEL, getComponentLabel(componentName, helper))
          .put("successfull", e == null);
      if (componentId != null) {
        jsonObject.put("id", componentId)
            .put("url", URLUtil.getURL(componentName, "useless", componentId) + "Main");
      }
      if (e != null) {
        jsonObject.put("exception", e.getMessage());
      }
      return jsonObject;
    });
  }

  private String getComponentLabel(String componentName, LookHelper helper) {
    String label;
    try {
      label = helper.getString("lookSilverpeasV5.personalSpace." + componentName);
    } catch (MissingResourceException e) {
      SilverLogger.getLogger(this).silent(e);
      label = PersonalComponentInstance.from(User.getCurrentRequester(),
          PersonalComponent.getByName(componentName)
              .orElseThrow(() -> new SilverpeasRuntimeException(
                  "No personnal component with such name " + componentName)))
          .getLabel(helper.getLanguage());
    }
    if (!StringUtil.isDefined(label)) {
      label = componentName;
    }
    return label;
  }

  private UnaryOperator<JSONObject> getToolAsJSONObject(String id,
      String label, String url, int nb) {
    return (jsonObject -> jsonObject.put("name", "").put(DESCRIPTION, "").put(LABEL, label)
        .put("id", id)
        .put("url", url)
        .put("nb", nb));
  }

  private String getToolsAsJSONArray(LookHelper helper) {
    LocalizationBundle message = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.homePage.multilang.homePageBundle", helper.getLanguage());
    String json = "";
    if (!helper.isAnonymousAccess() && helper.getSettings("personnalSpaceVisible", true)) {
      json = JSONCodec.encodeArray(jsonArray -> {
        addTool(jsonArray, helper, "agendaVisible", "agenda", message.getString("Diary"),
            URLUtil.getURL(URLUtil.CMP_AGENDA, null, null) + "Main");
        addTool(jsonArray, helper, "todoVisible", "todo", message.getString("ToDo"),
            URLUtil.getURL(URLUtil.CMP_TODO, null, null) + "todo.jsp");
        addNotificationsAsTool(jsonArray, helper, message);
        addTool(jsonArray, helper, "interestVisible", "subscriptions",
            message.getString("MyInterestCenters"),
            URLUtil.getURL(URLUtil.CMP_PDCSUBSCRIPTION, null, null) + "subscriptionList.jsp");
        addTool(jsonArray, helper, "favRequestVisible", "requests",
            message.getString("FavRequests"),
            URLUtil.getURL(URLUtil.CMP_INTERESTCENTERPEAS, null, null) + "iCenterList.jsp");
        addTool(jsonArray, helper, "linksVisible", "links", message.getString("FavLinks"),
            URLUtil.getURL(URLUtil.CMP_MYLINKSPEAS, null, null) + "Main");
        addFileSharingAsTool(jsonArray, helper, message);
        addWebConnectionsAsTool(jsonArray, helper, message);
        addTool(jsonArray, helper, "scheduleEventVisible", "scheduleevent",
            message.getString("ScheduleEvent"),
            URLUtil.getURL(URLUtil.CMP_SCHEDULE_EVENT, null, null) + "Main");
        addTool(jsonArray, helper, "customVisible", "personalize",
            message.getString("Personalization"),
            URLUtil.getURL(URLUtil.CMP_MYPROFILE, null, null) + "MyInfos");
        addTool(jsonArray, helper, "mailVisible", "notifAdmins", message.getString("Feedback"),
            "javascript:notifyAdministrators()");
        addTool(jsonArray, helper, "clipboardVisible", "clipboard", message.getString("Clipboard"),
            "javascript:openClipboard()");
        return jsonArray;
      });
    }
    return json;
  }

  private void addTool(JSONArray jsonArray, LookHelper helper, String property, String id,
      String label, String url) {
    addTool(jsonArray, helper, property, id, label, url, 0);
  }

  private void addTool(JSONArray jsonArray, LookHelper helper, String property, String id,
      String label, String url, int nbElements) {
    if (helper.getSettings(property, true)) {
      jsonArray.addJSONObject(getToolAsJSONObject(id, label, url, nbElements));
    }
  }

  private void addNotificationsAsTool(JSONArray jsonArray, LookHelper helper,
      LocalizationBundle message) {
    if (helper.getSettings("notificationVisible", true)) {

      // get number of notifications
      int nbNotifications = UserNotificationServerEvent.getNbUnreadFor(helper.getUserId());

      addTool(jsonArray, helper, "notificationVisible", "notification", message.getString("Mail"),
          URLUtil.getURL(URLUtil.CMP_SILVERMAIL) + "Main", nbNotifications);
    }
  }

  private void addFileSharingAsTool(JSONArray jsonArray, LookHelper helper,
      LocalizationBundle message) {
    // mes tickets
    if (helper.getSettings("fileSharingVisible", true)) {
      SharingTicketService sharingTicket =
          SharingServiceProvider.getSharingTicketService();
      try {
        if (sharingTicket.countTicketsByUser(helper.getUserId()) > 0) {
          addTool(jsonArray, helper, "fileSharingVisible", "sharingTicket",
              message.getString("FileSharing"), URLUtil.getURL(URLUtil.CMP_FILESHARING)
              + "Main");
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  private void addWebConnectionsAsTool(JSONArray jsonArray, LookHelper helper,
      LocalizationBundle message) {
    // mes connexions
    if (helper.getSettings("webconnectionsVisible", true)) {
      WebConnectionsInterface webConnections = WebConnectionsInterface.get();
      try {
        if (!webConnections.listWebConnectionsOfUser(helper.getUserId()).isEmpty()) {
          addTool(jsonArray, helper, "webconnectionsVisible", "webConnections",
              message.getString("WebConnections"),
              URLUtil.getURL(URLUtil.CMP_WEBCONNECTIONS) + "Main");
        }
      } catch (RemoteException e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }
}
