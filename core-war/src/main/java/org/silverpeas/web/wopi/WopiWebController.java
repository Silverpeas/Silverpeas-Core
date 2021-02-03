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
package org.silverpeas.web.wopi;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;
import org.silverpeas.core.wopi.WopiFile;
import org.silverpeas.core.wopi.WopiFileEditionManager;
import org.silverpeas.core.wopi.WopiUser;

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
@WebComponentController(WopiWebController.WOPI_COMPONENT_NAME)
public class WopiWebController extends
    org.silverpeas.core.web.mvc.webcomponent.WebComponentController<WopiWebRequestContext> {
  private static final long serialVersionUID = -800453654549621458L;

  public static final String WOPI_COMPONENT_NAME = "wopi";
  private final Set<String> selectedUserIds = synchronizedSet(new HashSet<>());
  private final Set<String> selectedFileIds = synchronizedSet(new HashSet<>());

  public WopiWebController(final MainSessionController controller, final ComponentContext context) {
    super(controller, context, "org.silverpeas.wopi.multilang.wopi");
  }

  @Override
  protected void onInstantiation(final WopiWebRequestContext context) {
    // nothing to do
  }

  @Override
  public String getComponentName() {
    return WOPI_COMPONENT_NAME;
  }

  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternalJsp("wopi.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void home(WopiWebRequestContext context) {
    mergeSelectedItems(context);
    setCommons(context);
  }

  @POST
  @Path("enable")
  @RedirectToInternalJsp("wopi.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void enable(WopiWebRequestContext context) {
    getManager().enable(true);
    setCommons(context);
  }

  @POST
  @Path("disable")
  @RedirectToInternalJsp("wopi.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void disable(WopiWebRequestContext context) {
    getManager().enable(false);
    setCommons(context);
  }

  @POST
  @Path("revokeSelected")
  @RedirectToInternalJsp("wopi.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void revokeSelected(WopiWebRequestContext context) {
    mergeSelectedItems(context);
    getAllUsers().stream()
        .filter(u -> selectedUserIds.contains(u.getId()))
        .forEach(getManager()::revokeUser);
    getAllFiles().stream()
        .filter(u -> selectedFileIds.contains(u.id()))
        .forEach(getManager()::revokeFile);
    selectedUserIds.clear();
    selectedFileIds.clear();
    setCommons(context);
  }

  @POST
  @Path("revokeAll")
  @RedirectToInternalJsp("wopi.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void revokeAll(WopiWebRequestContext context) {
    getAllUsers().forEach(getManager()::revokeUser);
    getAllFiles().forEach(getManager()::revokeFile);
    selectedUserIds.clear();
    selectedFileIds.clear();
    setCommons(context);
  }

  private void setCommons(final WopiWebRequestContext context) {
    final HttpRequest request = context.getRequest();
    request.setAttribute("isEnabled", getManager().isEnabled());
    request.setAttribute("AllUsers", SilverpeasList.wrap(getAllUsers()));
    request.setAttribute("AllFiles", SilverpeasList.wrap(getAllFiles()));
    request.setAttribute("SelectedUserIds", selectedUserIds);
    request.setAttribute("SelectedFileIds", selectedFileIds);
  }

  private void mergeSelectedItems(final WopiWebRequestContext context) {
    final HttpRequest request = context.getRequest();
    request.mergeSelectedItemsInto(selectedUserIds, "selectedUserIds", "unselectedUserIds");
    request.mergeSelectedItemsInto(selectedFileIds, "selectedFileIds", "unselectedFileIds");
  }

  private List<WopiUser> getAllUsers() {
    return getManager().listCurrentUsers();
  }

  private List<WopiFile> getAllFiles() {
    return getManager().listCurrentFiles();
  }

  private WopiFileEditionManager getManager() {
    return WopiFileEditionManager.get();
  }
}