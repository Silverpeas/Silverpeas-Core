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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.peasUtil;

import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.exporters.RSS_2_0_Exporter;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.Item;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;

public abstract class RssServlet<T> extends HttpServlet {

  private static final long serialVersionUID = 1756308502037077021L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    SilverTrace.info("peasUtil", "RssServlet.doPost", "root.MSG_GEN_ENTER_METHOD");
    String instanceId = getObjectId(req);
    String userId = getUserId(req);
    String login = getLogin(req);
    String password = getPassword(req);
    // rechercher si le composant a bien le flux RSS autorisé
    if (isComponentRss(instanceId)) {
      try {
        SilverTrace.info("peasUtil", "RssServlet.doPost", "root.MSG_GEN_PARAM_VALUE",
            "InstanceId = " + instanceId);

        // Vérification que le user a droit d'accès au composant
        AdminController adminController = new AdminController(null);
        UserFull user = adminController.getUserFull(userId);
        if (user != null && login.equals(user.getLogin())
            && password.equals(user.getPassword())
            && isComponentAvailable(adminController, instanceId, userId)) {

          String serverURL = getServerURL(adminController, user.getDomainId());
          ChannelIF channel = new Channel();

          // récupération de la liste des N éléments à remonter dans le flux
          int nbReturnedElements = getNbReturnedElements();
          Collection<T> listElements = getListElements(instanceId, nbReturnedElements);

          // création d'une liste de ItemIF en fonction de la liste des éléments

          for (T element : listElements) {
            String title = getElementTitle(element, userId);
            URL link = new URL(serverURL + getElementLink(element, userId));
            String description = getElementDescription(element, userId);
            Date dateElement = getElementDate(element);
            String creatorId = getElementCreatorId(element);
            ItemIF item = new Item();
            item.setTitle(title);
            item.setLink(link);
            item.setDescription(description);
            item.setDate(dateElement);

            if (StringUtil.isDefined(creatorId)) {
              UserDetail creator = adminController.getUserDetail(creatorId);
              if (creator != null) {
                item.setCreator(creator.getDisplayedName());
              }
            } else if (StringUtil.isDefined(getExternalCreatorId(element))) {
              item.setCreator(getExternalCreatorId(element));
            }
            channel.addItem(item);
          }

          // construction de l'objet Channel
          channel.setTitle(getChannelTitle(instanceId));
          URL componentUrl = new URL(serverURL + URLManager.getApplicationURL()
              + URLManager.getURL("useless", instanceId));
          channel.setLocation(componentUrl);

          // exportation du channel
          res.setContentType(MimeTypes.RSS_MIME_TYPE);
          res.setHeader("Content-Disposition", "inline; filename=feeds.rss");
          Writer writer = res.getWriter();
          RSS_2_0_Exporter rssExporter = new RSS_2_0_Exporter(writer, "UTF-8");
          rssExporter.write(channel);
        } else {
          objectNotFound(req, res);
        }
      } catch (Exception e) {
        objectNotFound(req, res);
      }
    }
  }

  public String getChannelTitle(String instanceId) {
    OrganizationController orga = new OrganizationController();
    ComponentInstLight instance = orga.getComponentInstLight(instanceId);
    if (instance != null) {
      return instance.getLabel();
    }
    return "";
  }

  public String getServerURL(AdminController admin, String domainId) {
    Domain defaultDomain = admin.getDomain(domainId);
    return defaultDomain.getSilverpeasServerURL();
  }

  public boolean isComponentRss(String instanceId) {
    OrganizationController orga = new OrganizationController();
    String paramRssValue = orga.getComponentParameterValue(instanceId, "rss");
    // rechercher si le composant a bien le flux RSS autorisé
    return "yes".equalsIgnoreCase(paramRssValue);
  }

  public boolean isComponentAvailable(AdminController admin, String instanceId,
      String userId) {
    return admin.isComponentAvailable(instanceId, userId);
  }

  public int getNbReturnedElements() {
    return 15;
  }

  public abstract Collection<T> getListElements(String instanceId, int nbReturned)
      throws RemoteException;

  public abstract String getElementTitle(T element, String userId);

  public abstract String getElementLink(T element, String userId);

  public abstract String getElementDescription(T element, String userId);

  public abstract Date getElementDate(T element);

  public abstract String getElementCreatorId(T element);

  public String getExternalCreatorId(T element) {
    return "";
  }

  protected String getObjectId(HttpServletRequest request) {
    String pathInfo = request.getPathInfo();
    if (pathInfo != null) {
      return pathInfo.substring(1);
    }
    return null;
  }

  protected String getUserId(HttpServletRequest request) {
    return request.getParameter("userId");
  }

  protected String getLogin(HttpServletRequest request) {
    return request.getParameter("login");
  }

  protected String getPassword(HttpServletRequest request) {
    return request.getParameter("password");
  }

  protected MainSessionController getMainSessionController(HttpServletRequest req) {
    HttpSession session = req.getSession(true);
    MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    return mainSessionCtrl;
  }

  protected boolean isUserLogin(HttpServletRequest req) {
    return (getMainSessionController(req) != null);
  }

  protected void objectNotFound(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    boolean isLoggedIn = isUserLogin(req);
    if (!isLoggedIn) {
      res.sendRedirect("/weblib/notFound.html");
    } else {
      res.sendRedirect(GeneralPropertiesManager.getGeneralResourceLocator().getString(
          "ApplicationURL") + "/admin/jsp/documentNotFound.jsp");
    }
  }
}