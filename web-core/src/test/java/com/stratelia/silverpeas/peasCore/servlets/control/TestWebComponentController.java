/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package com.stratelia.silverpeas.peasCore.servlets.control;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.Navigation;
import com.stratelia.silverpeas.peasCore.servlets.annotation.Homepage;
import com.stratelia.silverpeas.peasCore.servlets.annotation.Invokable;
import com.stratelia.silverpeas.peasCore.servlets.annotation.InvokeAfter;
import com.stratelia.silverpeas.peasCore.servlets.annotation.InvokeBefore;
import com.stratelia.silverpeas.peasCore.servlets.annotation.LowestRoleAccess;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectTo;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectToInternal;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectToInternalJsp;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectToType;
import com.stratelia.webactiv.SilverpeasRole;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author: Yohann Chastagnier
 */
@com.stratelia.silverpeas.peasCore.servlets.annotation.WebComponentController(
    "TestWebComponentControllerIdentifier")
public class TestWebComponentController extends ParentTestWebComponentController {

  /**
   * Standard Session Controller Constructor
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public TestWebComponentController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext);
  }

  @GET
  @Homepage
  @RedirectToInternalJsp("/homepage.jsp")
  public void homeMethodNotWired(String aParam) {
  }

  @GET
  @Homepage
  @RedirectToInternalJsp("homepage.jsp")
  public void homeMethod(TestWebComponentRequestContext context) {
  }

  @POST
  @Path("create")
  @RedirectToInternal("/created")
  public void createMethod(TestWebComponentRequestContext context) {
  }

  @PUT
  @Path("update")
  @RedirectToInternal("updated")
  public void updateMethod(TestWebComponentRequestContext context) {
  }

  @DELETE
  @Path("delete")
  @RedirectTo("/common/deleted.jsp")
  public void deleteMethod(TestWebComponentRequestContext context) {
  }

  @POST
  @Path("lowerRoleAccess")
  @RedirectToInternal("/lowerRoleAccessOk")
  @LowestRoleAccess(SilverpeasRole.publisher)
  public void lowerRoleAccessMethod(TestWebComponentRequestContext context) {
  }

  @POST
  @Path("lowerRoleAccessRedirectToInternalJspOnError")
  @RedirectToInternal("/lowerRoleAccessOk")
  @LowestRoleAccess(value = SilverpeasRole.publisher,
      onError = @RedirectTo(value = "error.jsp", type = RedirectToType.INTERNAL_JSP))
  public void lowerRoleAccessRedirectToInternalJspOnErrorMethod(
      TestWebComponentRequestContext context) {
  }

  @POST
  @Path("lowerRoleAccessRedirectToInternalOnError")
  @RedirectToInternal("/lowerRoleAccessOk")
  @LowestRoleAccess(value = SilverpeasRole.publisher,
      onError = @RedirectTo(value = "error", type = RedirectToType.INTERNAL))
  public void lowerRoleAccessRedirectToInternalOnErrorMethod(
      TestWebComponentRequestContext context) {
  }

  @POST
  @Path("lowerRoleAccessRedirectToOnError")
  @RedirectToInternal("/lowerRoleAccessOk")
  @LowestRoleAccess(value = SilverpeasRole.publisher, onError = @RedirectTo("error"))
  public void lowerRoleAccessRedirectToOnErrorMethod(TestWebComponentRequestContext context) {
  }

  @GET
  @Path("/invokation/oneBefore")
  @RedirectTo("invokation/oneBefore/ok")
  @InvokeBefore("invoke_before_1")
  public void oneInvokationBefore(TestWebComponentRequestContext context) {
  }

  @GET
  @Path("/invokation/2Before")
  @RedirectToInternal("invokation/2Before/ok")
  @InvokeBefore({"invoke_before_1", "invoke_before_3"})
  public void twoInvokationsBefore(TestWebComponentRequestContext context) {
  }

  @GET
  @Path("/invokation/oneAfter")
  @RedirectToInternal("invokation/oneAfter/ok")
  @InvokeAfter("invoke_after_4")
  public void oneInvokationAfter(TestWebComponentRequestContext context) {
  }

  @GET
  @Path("/invokation/3After")
  @RedirectToInternal("invokation/3After/ok")
  @InvokeAfter({"invoke_after_3", "invoke_after_2", "invoke_after_1"})
  public void threeInvokationsAfter(TestWebComponentRequestContext context) {
  }

  @GET
  @Path("/invokation/3Before4After")
  @RedirectToInternal("invokation/3Before4After/ok")
  @InvokeBefore({"invoke_before_3", "invoke_before_1", "invoke_before_2"})
  @InvokeAfter({"invoke_after_3", "invoke_after_2", "invoke_after_4", "invoke_after_1"})
  public void threeInvokationsBeforeAndFourAfter(TestWebComponentRequestContext context) {
  }

  @Invokable("invoke_before_1")
  public void invokable_before_1(TestWebComponentRequestContext context) {
    context.addInvokationBeforeCall();
  }

  @Invokable("invoke_before_2")
  public void invokable_before_2(TestWebComponentRequestContext context) {
    context.addInvokationBeforeCall();
  }

  @Invokable("invoke_before_3")
  public void invokable_before_3(TestWebComponentRequestContext context) {
    context.addInvokationBeforeCall();
  }

  @Invokable("invoke_before_4")
  public void invokable_before_4(TestWebComponentRequestContext context) {
    context.addInvokationBeforeCall();
  }

  @Invokable("invoke_after_1")
  public void invokable_after_1(TestWebComponentRequestContext context) {
    context.addInvokationAfterCall();
  }

  @Invokable("invoke_after_2")
  public void invokable_after_2(TestWebComponentRequestContext context) {
    context.addInvokationAfterCall();
  }

  @Invokable("invoke_after_3")
  public void invokable_after_3(TestWebComponentRequestContext context) {
    context.addInvokationAfterCall();
  }

  @Invokable("invoke_after_4")
  public void invokable_after_4(TestWebComponentRequestContext context) {
    context.addInvokationAfterCall();
  }

  @GET
  @Path("/wysiwyg/modify")
  public Navigation modifyWysiwyg(TestWebComponentRequestContext context) {
    return context.redirectToHtmlEditor("objectId", "resturnPath", false);
  }

  @GET
  @Path("/wysiwyg/{anResourceId}/view")
  @RedirectToInternal("view/resource/1")
  public void viewResource(TestWebComponentRequestContext context) {
  }

  @GET
  @Path("/wysiwyg/resourceId-{anResourceId}-test")
  @RedirectToInternal("view/resource/2")
  public void viewResource2(TestWebComponentRequestContext context) {
  }

  @GET
  @Path("/wysiwyg/resourceId-{anResourceId:[0-9]+}-otherTest")
  @RedirectToInternal("view/resource/3")
  public void viewOtherResource(TestWebComponentRequestContext context) {
  }

  @GET
  @Path("/wysiwyg/resourceId-{anResourceId  :  [0-9]+ }-test/{otherId}/view")
  @RedirectToInternal("view/resource/4")
  public void viewOtherResource2(TestWebComponentRequestContext context) {
  }

  @GET
  @Path("/wysiwyg/{anResourceId}/{anResourceId}/review")
  @RedirectToInternal("view/resource/5")
  public void sameVariableName(TestWebComponentRequestContext context) {
  }

  @GET
  @Path("/redirect/report")
  @RedirectToInternalJsp("pushed.jsp?action={action}&otherId={otherId}")
  public void redirectToInternalJspWithVariable(TestWebComponentRequestContext context) {
    context.addRedirectVariable("action", "anAction");
    context.addRedirectVariable("otherId", "id26");
  }

  @GET
  @Path("/redirect/{anResourceId}/push/{otherId}/report")
  @RedirectToInternal("{anResourceId}/pushed/?action={action}&otherId={otherId}")
  public void redirectToInternalWithVariable(TestWebComponentRequestContext context) {
    context.addRedirectVariable("action", "anAction");
  }

  @GET
  @Path("/redirect/report/{anResourceId}/push/{otherId}/")
  @RedirectTo("{anResourceId}/pushed?action={action}&otherId={otherId}")
  public void redirectToWithVariable(TestWebComponentRequestContext context) {
    context.addRedirectVariable("action", "anAction");
  }

  @GET
  @Path("/redirect/report/{anResourceId}/push/{otherId}/SameVariableSevralValues")
  @RedirectTo("{anResourceId}/pushed?action={action}&otherId={otherId}")
  public void redirectToWithVariableButSeveralValuesForSameVariable(
      TestWebComponentRequestContext context) {
    context.addRedirectVariable("anResourceId", "anAction");
  }

  @GET
  @Path("/webApplicationException412")
  @RedirectTo("webApplicationException412")
  public void webApplicationException412(TestWebComponentRequestContext context) {
    throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED).build());
  }

  @GET
  @Path("/variables/concurrencyWithStatics/{variablePath}")
  @RedirectTo("variableWay")
  public void variableConcurrencyVariable(TestWebComponentRequestContext context) {
  }

  @GET
  @Path("/variables/concurrencyWithStatics/staticPath")
  @RedirectTo("staticWay")
  public void variableConcurrencyStatic(TestWebComponentRequestContext context) {
  }
}
