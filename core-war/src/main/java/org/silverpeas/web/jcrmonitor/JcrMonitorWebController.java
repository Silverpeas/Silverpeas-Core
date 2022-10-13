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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.jcrmonitor;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.persistence.jcr.JcrDatastoreManager;
import org.silverpeas.core.wbe.WbeHostManager;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * @author silveryocha
 */
@WebComponentController(JcrMonitorWebController.JCR_MONITOR_COMPONENT_NAME)
public class JcrMonitorWebController extends
    org.silverpeas.core.web.mvc.webcomponent.WebComponentController<JcrMonitorWebRequestContext> {

  public static final String JCR_MONITOR_COMPONENT_NAME = "jcrmonitor";

  public JcrMonitorWebController(final MainSessionController controller,
      final ComponentContext context) {
    super(controller, context, "org.silverpeas.jcrmonitor.multilang.jcrMonitor");
  }

  @Override
  protected void onInstantiation(final JcrMonitorWebRequestContext context) {
    // nothing to do
  }

  @Override
  public String getComponentName() {
    return JCR_MONITOR_COMPONENT_NAME;
  }

  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternalJsp("jcrmonitor.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void home(JcrMonitorWebRequestContext context) {
    setCommons(context);
  }

  @POST
  @Path("ForceSizeComputing")
  @RedirectToInternalJsp("jcrmonitor.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void forceSizeComputing(JcrMonitorWebRequestContext context) {
    final JcrDatastoreManager manager = getJcrDatastoreManager();
    if (!manager.isRunningTask()) {
      manager.getDatastorePathView().notifyChanges();
    }
    home(context);
  }

  @POST
  @Path("forceGC")
  @RedirectToInternalJsp("jcrmonitor.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void forceGC(JcrMonitorWebRequestContext context) {
    getJcrDatastoreManager().forceGC();
    home(context);
  }

  private void setCommons(final JcrMonitorWebRequestContext context) {
    final HttpRequest request = context.getRequest();
    final JcrDatastoreManager manager = getJcrDatastoreManager();
    request.setAttribute("wbeEnabled", WbeHostManager.get().isEnabled());
    request.setAttribute("datastorePathView", manager.getDatastorePathView());
    manager.getPreviousTask().ifPresent(p -> request.setAttribute("previousTask", p));
    manager.getCurrentTask().ifPresent(c -> request.setAttribute("currentTask", c));
  }

  private JcrDatastoreManager getJcrDatastoreManager() {
    return JcrDatastoreManager.get();
  }
}
