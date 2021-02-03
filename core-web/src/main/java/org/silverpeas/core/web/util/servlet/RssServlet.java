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
package org.silverpeas.core.web.util.servlet;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.SyndFeedOutput;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public abstract class RssServlet<T> extends HttpServlet {

  private static final long serialVersionUID = 1756308502037077021L;
  private static final int DEFAULT_MAX_TEMS_COUNT = 15;
  private static final String NOTHING = "";

  @Inject
  private AdminController adminController;
  @Inject
  private OrganizationController organizationController;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)  {

    String instanceId = getObjectId(req);
    String userId = getUserId(req);
    String login = getLogin(req);
    String password = getPassword(req);
    // check the component instance have the authorized RSS feed?
    if (isComponentRss(instanceId)) {
      try {


        // check the user right to access the component instance
        UserFull user = adminController.getUserFull(userId);
        if (user != null && login.equals(user.getLogin()) && password.equals(user.getPassword()) &&
            isComponentAvailable(instanceId, userId)) {

          String serverURL = getServerURL(adminController, user.getDomainId());
          SyndFeed feed = new SyndFeedImpl();
          feed.setFeedType("rss_2.0");
          feed.setTitle(getChannelTitle(instanceId));
          feed.setDescription(getChannelTitle(instanceId));
          feed.setLink(
              serverURL + URLUtil.getApplicationURL() + URLUtil.getURL("useless", instanceId));

          // fetch N items to syndicate within the feed
          int nbReturnedElements = getNbReturnedElements();
          Collection<T> listElements = getListElements(instanceId, nbReturnedElements);

          // write each of them as a feed entry into the syndication feed
          List<SyndEntry> entries = new ArrayList<>(listElements.size());
          for (T element : listElements) {
            String title = getElementTitle(element, userId);
            String link = serverURL + getElementLink(element, userId);
            String description = getElementDescription(element, userId);
            Date dateElement = getElementDate(element);
            String creatorId = getElementCreatorId(element);
            SyndEntry entry = new SyndEntryImpl();
            SyndContent descriptionContent = new SyndContentImpl();
            descriptionContent.setType("text/plain");
            descriptionContent.setValue(description);
            entry.setTitle(title);
            entry.setLink(link);
            entry.setDescription(descriptionContent);
            entry.setPublishedDate(dateElement);
            setCreator(element, creatorId, entry);
            entries.add(entry);
          }

          feed.setEntries(entries);

          // exportation du channel
          res.setContentType(MimeTypes.RSS_MIME_TYPE);
          Writer writer = res.getWriter();
          SyndFeedOutput feedOutput = new SyndFeedOutput();
          feedOutput.output(feed, writer);
        } else {
          objectNotFound(req, res);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
        objectNotFound(req, res);
      }
    }
  }

  protected void setCreator(final T element, final String creatorId, final SyndEntry entry) {
    if (StringUtil.isDefined(creatorId)) {
      UserDetail creator = adminController.getUserDetail(creatorId);
      if (creator != null) {
        entry.setAuthor(creator.getDisplayedName());
      }
    } else if (StringUtil.isDefined(getExternalCreatorId(element))) {
      entry.setAuthor(getExternalCreatorId(element));
    }
  }

  public String getChannelTitle(String instanceId) {
    ComponentInstLight instance = organizationController.getComponentInstLight(instanceId);
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
    String paramRssValue = organizationController.getComponentParameterValue(instanceId, "rss");
    // rechercher si le composant a bien le flux RSS autorisé
    return "yes".equalsIgnoreCase(paramRssValue);
  }

  public boolean isComponentAvailable(String instanceId, String userId) {
    return adminController.isComponentAvailable(instanceId, userId);
  }

  public int getNbReturnedElements() {
    return DEFAULT_MAX_TEMS_COUNT;
  }

  public abstract Collection<T> getListElements(String instanceId, int nbReturned)
      throws RemoteException;

  public abstract String getElementTitle(T element, String userId);

  public abstract String getElementLink(T element, String userId);

  public abstract String getElementDescription(T element, String userId);

  public abstract Date getElementDate(T element);

  public abstract String getElementCreatorId(T element);

  public String getExternalCreatorId(T element) {
    // designed to be extended but return nothing in standard case
    return NOTHING;
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
    return  (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
  }

  protected boolean isUserLogin(HttpServletRequest req) {
    return getMainSessionController(req) != null;
  }

  protected void objectNotFound(HttpServletRequest req, HttpServletResponse res) {
    try {
      boolean isLoggedIn = isUserLogin(req);
      if (!isLoggedIn) {
        res.sendRedirect("/weblib/notFound.html");
      } else {
        res.sendRedirect(URLUtil.getApplicationURL() + "/admin/jsp/documentNotFound.jsp");
      }
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}