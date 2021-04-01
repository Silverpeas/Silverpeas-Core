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
package org.silverpeas.web.wbe;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.wbe.WbeHostManager;
import org.silverpeas.core.wbe.WbeUser;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.synchronizedSet;

/**
 * @author silveryocha
 */
@WebComponentController(WbeWebController.WBE_COMPONENT_NAME)
public class WbeWebController extends
    org.silverpeas.core.web.mvc.webcomponent.WebComponentController<WbeWebRequestContext> {
  private static final long serialVersionUID = -800453654549621458L;

  public static final String WBE_COMPONENT_NAME = "wbe";
  private final Set<String> selectedUserIds = synchronizedSet(new HashSet<>());
  private final Set<String> selectedFileIds = synchronizedSet(new HashSet<>());

  public WbeWebController(final MainSessionController controller, final ComponentContext context) {
    super(controller, context, "org.silverpeas.wbe.multilang.wbe");
  }

  @Override
  protected void onInstantiation(final WbeWebRequestContext context) {
    // nothing to do
  }

  @Override
  public String getComponentName() {
    return WBE_COMPONENT_NAME;
  }

  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternalJsp("wbe.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void home(WbeWebRequestContext context) {
    mergeSelectedItems(context);
    setCommons(context);
  }

  @POST
  @Path("enable")
  @RedirectToInternalJsp("wbe.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void enable(WbeWebRequestContext context) {
    getHostManager().enable(true);
    setCommons(context);
  }

  @POST
  @Path("disable")
  @RedirectToInternalJsp("wbe.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void disable(WbeWebRequestContext context) {
    getHostManager().enable(false);
    setCommons(context);
  }

  @POST
  @Path("revokeSelected")
  @RedirectToInternalJsp("wbe.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void revokeSelected(WbeWebRequestContext context) {
    mergeSelectedItems(context);
    getAllUsers().stream()
        .filter(u -> selectedUserIds.contains(u.getId()))
        .forEach(getHostManager()::revokeUser);
    getAllFiles().stream()
        .filter(u -> selectedFileIds.contains(u.id()))
        .forEach(getHostManager()::revokeFile);
    selectedUserIds.clear();
    selectedFileIds.clear();
    setCommons(context);
  }

  @POST
  @Path("revokeAll")
  @RedirectToInternalJsp("wbe.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void revokeAll(WbeWebRequestContext context) {
    getAllUsers().forEach(getHostManager()::revokeUser);
    getAllFiles().forEach(getHostManager()::revokeFile);
    selectedUserIds.clear();
    selectedFileIds.clear();
    setCommons(context);
  }

  private void setCommons(final WbeWebRequestContext context) {
    final HttpRequest request = context.getRequest();
    request.setAttribute("isEnabled", getHostManager().isEnabled());
    getHostManager().getClientAdministrationUrl().ifPresent(u -> request.setAttribute("clientAdministrationUrl", u));
    request.setAttribute("AllUsers", SilverpeasList.wrap(getAllUsers()));
    request.setAttribute("AllFiles", SilverpeasList.wrap(getAllFiles()));
    request.setAttribute("SelectedUserIds", selectedUserIds);
    request.setAttribute("SelectedFileIds", selectedFileIds);
  }

  private void mergeSelectedItems(final WbeWebRequestContext context) {
    final HttpRequest request = context.getRequest();
    request.mergeSelectedItemsInto(selectedUserIds, "selectedUserIds", "unselectedUserIds");
    request.mergeSelectedItemsInto(selectedFileIds, "selectedFileIds", "unselectedFileIds");
  }

  private List<WbeUser> getAllUsers() {
    return getHostManager().listCurrentUsers();
  }

  private List<WbeFile> getAllFiles() {
    return getHostManager().listCurrentFiles();
  }

  private WbeHostManager getHostManager() {
    return WbeHostManager.get();
  }
}
