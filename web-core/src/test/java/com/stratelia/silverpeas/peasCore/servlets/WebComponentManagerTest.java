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
package com.stratelia.silverpeas.peasCore.servlets;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.control.*;
import com.stratelia.webactiv.SilverpeasRole;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.silverpeas.servlet.HttpRequest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import java.net.URI;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author: Yohann Chastagnier
 */
public class WebComponentManagerTest extends WebComponentRequestRouterTest {

  @Test(expected = IllegalArgumentException.class)
  public void webComponentControllerIsNotAnnoted() throws Exception {
    try {
      onController(BadTestWebComponentController.class).defaultRequest().perform();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), Matchers.endsWith(
          "BadTestWebComponentController must specify one, and only one, " +
              "@WebComponentController annotation"
      ));
      throw e;
    }
  }

  @Test
  public void veryfingWebComponentControllerInitialize() throws Exception {
    TestResult testResult = onDefaultController().defaultRequest().perform();
    assertThat(testResult.router.getSessionControlBeanName(),
        is("TestWebComponentControllerIdentifier"));
    assertThat(testResult.router.createComponentSessionController(mock(MainSessionController.class),
        mock(ComponentContext.class)), instanceOf(TestWebComponentController.class));
  }

  @Test
  public void doGetOnRequestRouterWithRedirectToInternalJsp() throws Exception {
    TestResult testResult = onDefaultController().defaultRequest().perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(1))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/jsp/homepage.jsp");
    NavigationContext navigationContext = testResult.requestContext.getNavigationContext();
    assertThat(navigationContext.getBaseNavigationStep(), notNullValue());
    assertThat(navigationContext.getBaseNavigationStep(),
        is(navigationContext.getCurrentNavigationStep()));
    assertThat(navigationContext.getBaseNavigationStep().getUri(),
        is(URI.create("/componentName/Main")));
  }

  @Test
  public void doPostOnRequestRouterWithRedirectToInternal() throws Exception {
    TestResult testResult =
        onDefaultController().defaultRequest().changeHttpMethodWith(HttpMethod.POST)
            .changeSuffixPathWith("create").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/created");
  }

  @Test
  public void doPutOnRequestRouterWithRedirectToInternalWithRedirectTo() throws Exception {
    TestResult testResult =
        onDefaultController().defaultRequest().changeHttpMethodWith(HttpMethod.PUT)
            .changeSuffixPathWith("update").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/updated");
  }

  @Test
  public void doDeleteOnRequestRouter() throws Exception {
    TestResult testResult =
        onDefaultController().defaultRequest().changeHttpMethodWith(HttpMethod.DELETE)
            .changeSuffixPathWith("delete").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/common/deleted.jsp");
  }

  @Test(expected = IllegalArgumentException.class)
  public void homepageIsNotSpecified() throws Exception {
    try {
      onController(HomePageIsNotSpecifiedController.class).defaultRequest().perform();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), is("The homepage method must be specified with @Homepage"));
      throw e;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void twoHomepageIsSpecified() throws Exception {
    try {
      onController(TwoHomepagesController.class).defaultRequest().perform();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(),
          is("@Homepage is specified on otherHome method, but @Homepage has already been defined " +
              "one another one")
      );
      throw e;
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void lowerAccessRoleSuccess() throws Exception {
    TestResult<TestWebComponentRequestContext> testResult =
        onDefaultController().setGreaterUserRole(SilverpeasRole.publisher).defaultRequest()
            .changeHttpMethodWith(HttpMethod.POST).changeSuffixPathWith("lowerRoleAccess")
            .perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/lowerRoleAccessOk");
    assertThat(testResult.requestContext.getNbBeforeRequestInitializeCalls(), is(1));
    assertThat(testResult.requestContext.getNbInvokationsBeforeCall(), is(0));
    assertThat(testResult.requestContext.getNbInvokationsAfterCall(), is(0));
  }

  @Test
  public void lowerAccessRoleWithUserThatHasNotEnoughRights() throws Exception {
    TestResult testResult =
        onDefaultController().setGreaterUserRole(SilverpeasRole.writer).defaultRequest()
            .changeHttpMethodWith(HttpMethod.POST).changeSuffixPathWith("lowerRoleAccess")
            .perform();
    verify(testResult.requestContext.getResponse(), times(1))
        .sendError(HttpServletResponse.SC_FORBIDDEN);
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/admin/jsp/errorpageMain.jsp");
  }

  @Test
  public void lowerAccessRoleButWrongHttpMethod() throws Exception {
    TestResult testResult =
        onDefaultController().defaultRequest().changeHttpMethodWith(HttpMethod.GET)
            .changeSuffixPathWith("lowerRoleAccess").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(1))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/jsp/homepage.jsp");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void lowerRoleAccessRedirectToInternalJspOnError() throws Exception {
    TestResult<TestWebComponentRequestContext> testResult =
        onDefaultController().setGreaterUserRole(SilverpeasRole.writer).defaultRequest()
            .changeHttpMethodWith(HttpMethod.POST)
            .changeSuffixPathWith("lowerRoleAccessRedirectToInternalJspOnError").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/jsp/error.jsp");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void lowerRoleAccessRedirectToInternalOnError() throws Exception {
    TestResult<TestWebComponentRequestContext> testResult =
        onDefaultController().setGreaterUserRole(SilverpeasRole.writer).defaultRequest()
            .changeHttpMethodWith(HttpMethod.POST)
            .changeSuffixPathWith("lowerRoleAccessRedirectToInternalOnError").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/error");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void lowerRoleAccessRedirectToOnError() throws Exception {
    TestResult<TestWebComponentRequestContext> testResult =
        onDefaultController().setGreaterUserRole(SilverpeasRole.writer).defaultRequest()
            .changeHttpMethodWith(HttpMethod.POST)
            .changeSuffixPathWith("lowerRoleAccessRedirectToOnError").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/error");
  }

  @Test(expected = IllegalArgumentException.class)
  public void httpMethodWithInvokableAnnotation() throws Exception {
    try {
      onController(HttpMethodWithInvokableAnnotationController.class).defaultRequest().perform();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), is("Http Method homeMethod can not be annoted with @Invokable"));
      throw e;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void referenceInvokableBeforeThatDoesNotExist() throws Exception {
    try {
      onController(InvokeBeforeNoReferenceController.class).defaultRequest().perform();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(),
          is("method behind 'invokable_2' invokable identifier must be performed before the " +
              "execution of HTTP method homeMethod, but it is not registred")
      );
      throw e;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void referenceInvokableAfterThatDoesNotExist() throws Exception {
    try {
      onController(InvokeAfterNoReferenceController.class).defaultRequest().perform();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(),
          is("method behind 'invokable_1' invokable identifier must be performed after the " +
              "execution of HTTP method homeMethod, but it is not registred")
      );
      throw e;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void invokableIdentifierAlreadyExists() throws Exception {
    try {
      onController(InvokableIdentifierAlreadyExistsController.class).defaultRequest().perform();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), is("invokable_1 invokable identifier has already been set"));
      throw e;
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void oneIvokationBefore() throws Exception {
    TestResult<TestWebComponentRequestContext> testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("invokation/oneBefore")
            .perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/invokation/oneBefore/ok");
    assertThat(testResult.requestContext.getNbBeforeRequestInitializeCalls(), is(1));
    assertThat(testResult.requestContext.getNbInvokationsBeforeCall(), is(1));
    assertThat(testResult.requestContext.getNbInvokationsAfterCall(), is(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void twoIvokationsBefore() throws Exception {
    TestResult<TestWebComponentRequestContext> testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("/invokation/2Before")
            .perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/invokation/2Before/ok");
    assertThat(testResult.requestContext.getNbBeforeRequestInitializeCalls(), is(1));
    assertThat(testResult.requestContext.getNbInvokationsBeforeCall(), is(2));
    assertThat(testResult.requestContext.getNbInvokationsAfterCall(), is(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void oneIvokationAfter() throws Exception {
    TestResult<TestWebComponentRequestContext> testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("/invokation/oneAfter")
            .perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/invokation/oneAfter/ok");
    assertThat(testResult.requestContext.getNbBeforeRequestInitializeCalls(), is(1));
    assertThat(testResult.requestContext.getNbInvokationsBeforeCall(), is(0));
    assertThat(testResult.requestContext.getNbInvokationsAfterCall(), is(1));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void threeIvokationsAfter() throws Exception {
    TestResult<TestWebComponentRequestContext> testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("/invokation/3After").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/invokation/3After/ok");
    assertThat(testResult.requestContext.getNbBeforeRequestInitializeCalls(), is(1));
    assertThat(testResult.requestContext.getNbInvokationsBeforeCall(), is(0));
    assertThat(testResult.requestContext.getNbInvokationsAfterCall(), is(3));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void threeIvokationsBeforeAndFourAfter() throws Exception {
    TestResult<TestWebComponentRequestContext> testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("/invokation/3Before4After")
            .perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/invokation/3Before4After/ok");
    assertThat(testResult.requestContext.getNbBeforeRequestInitializeCalls(), is(1));
    assertThat(testResult.requestContext.getNbInvokationsBeforeCall(), is(3));
    assertThat(testResult.requestContext.getNbInvokationsAfterCall(), is(4));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void navigateToHtmlEditor() throws Exception {
    TestResult<TestWebComponentRequestContext> testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("wysiwyg/modify").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/wysiwyg/jsp/htmlEditor.jsp");
  }


  @Test(expected = IllegalArgumentException.class)
  public void missingNavigationOnHttpMethod() throws Exception {
    try {
      onController(MissingNavigationController.class).defaultRequest().perform();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(),
          is("home method must return a Navigation instance or be annoted by one of @RedirectTo.." +
              ". annotations")
      );
      throw e;
    }
  }


  @Test(expected = IllegalArgumentException.class)
  public void twoNavigationsSpecifiedOnHttpMethod() throws Exception {
    try {
      onController(TwoNavigationsSpecifiedController.class).defaultRequest().perform();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(),
          is("home method must, either return a Navigation instance, either be annoted by one of " +
              "@RedirectTo... annotation")
      );
      throw e;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void samePathsWithoutVariable() throws Exception {
    try {
      onController(SamePathsWithoutVariableController.class).defaultRequest().perform();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(),
          is("specified path for method method2 already exists for method method1 -> /a/b/c/d/"));
      throw e;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void samePathsWithVariables() throws Exception {
    try {
      onController(SamePathsWithVariablesController.class).defaultRequest().perform();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(),
          is("specified path for method method2 already exists for method method1 -> " +
              "a/b/c/d/resourceId-{anResourceId  :  [0-9]+  }-test")
      );
      throw e;
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void oneVariableSimple() throws Exception {
    TestResult testResult = onDefaultController().defaultRequest()
        .changeSuffixPathWith("/wysiwyg/myVariableValue_123/view").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/view/resource/1");
    Map<String, String> variables = testResult.requestContext.getPathVariables();
    assertThat(variables, notNullValue());
    assertThat(variables.size(), is(1));
    assertThat(variables, hasEntry("anResourceId", "myVariableValue_123"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void oneVariableComplex() throws Exception {
    TestResult testResult = onDefaultController().defaultRequest()
        .changeSuffixPathWith("/wysiwyg/resourceId-myVariableValue_123-test/").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/view/resource/2");
    Map<String, String> variables = testResult.requestContext.getPathVariables();
    assertThat(variables, notNullValue());
    assertThat(variables.size(), is(1));
    assertThat(variables, hasEntry("anResourceId", "myVariableValue_123"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void oneVariableComplex2() throws Exception {
    TestResult testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("/wysiwyg/resourceId-_123-test")
            .perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/view/resource/2");
    Map<String, String> variables = testResult.requestContext.getPathVariables();
    assertThat(variables, notNullValue());
    assertThat(variables.size(), is(1));
    assertThat(variables, hasEntry("anResourceId", "_123"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void oneVariableRegexpCheckOk() throws Exception {
    TestResult testResult = onDefaultController().defaultRequest()
        .changeSuffixPathWith("/wysiwyg/resourceId-123-otherTest").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/view/resource/3");
    Map<String, String> variables = testResult.requestContext.getPathVariables();
    assertThat(variables, notNullValue());
    assertThat(variables.size(), is(1));
    assertThat(variables, hasEntry("anResourceId", "123"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void oneVariableRegexpCheckOk2() throws Exception {
    TestResult testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("/wysiwyg/resourceId-_123-test")
            .perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/view/resource/2");
    Map<String, String> variables = testResult.requestContext.getPathVariables();
    assertThat(variables, notNullValue());
    assertThat(variables.size(), is(1));
    assertThat(variables, hasEntry("anResourceId", "_123"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void twoVariablesRegexpCheckOk() throws Exception {
    TestResult testResult = onDefaultController().defaultRequest()
        .changeSuffixPathWith("/wysiwyg/resourceId-123-test/id26/view").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/view/resource/4");
    Map<String, String> variables = testResult.requestContext.getPathVariables();
    assertThat(variables, notNullValue());
    assertThat(variables.size(), is(2));
    assertThat(variables, hasEntry("anResourceId", "123"));
    assertThat(variables, hasEntry("otherId", "id26"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void sameVariableName() throws Exception {
    TestResult testResult = onDefaultController().defaultRequest()
        .changeSuffixPathWith("/wysiwyg/myVariableValue_123/myVariableValue_123/review").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/view/resource/5");
    Map<String, String> variables = testResult.requestContext.getPathVariables();
    assertThat(variables, notNullValue());
    assertThat(variables.size(), is(1));
    assertThat(variables, hasEntry("anResourceId", "myVariableValue_123"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void sameVariableNameButNotSameValues() throws Exception {
    TestResult testResult = onDefaultController().defaultRequest()
        .changeSuffixPathWith("/wysiwyg/myVariableValue_123/myVariableValue_124/review").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/admin/jsp/errorpageMain.jsp");
    Map<String, String> variables = testResult.requestContext.getPathVariables();
    assertThat(variables, notNullValue());
    assertThat(variables.size(), is(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void redirectToInternalJspWithVariable() throws Exception {
    TestResult testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("/redirect/report").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router,
        "/componentName/jsp/pushed.jsp?action=anAction&otherId=id26");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void redirectToInternalWithVariable() throws Exception {
    TestResult testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("/redirect/123/push/26/report")
            .perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/123/pushed?action=anAction&otherId=26");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void redirectToWithVariable() throws Exception {
    TestResult testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("/redirect/report/123/push/26")
            .perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/123/pushed?action=anAction&otherId=26");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void redirectToWithVariableButSeveralValuesForSameVariable() throws Exception {
    TestResult testResult = onDefaultController().defaultRequest()
        .changeSuffixPathWith("/redirect/report/123/push/26/SameVariableSevralValues").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/admin/jsp/errorpageMain.jsp");
    Map<String, String> variables = testResult.requestContext.getPathVariables();
    assertThat(variables, notNullValue());
    assertThat(variables.size(), is(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void webApplicationException412() throws Exception {
    TestResult testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("webApplicationException412")
            .perform();
    verify(testResult.requestContext.getResponse(), times(1))
        .sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
    verify(testResult.requestContext.getMultilang(), times(0))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/admin/jsp/errorpageMain.jsp");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void specialControllerInheritance() throws Exception {
    TestResult testResult =
        onController(TestWebComponentSpecialInheritanceController.class).defaultRequest().perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(1))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/jsp/homepage.jsp");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void variableConcurrencyVariableMethodNotFound() throws Exception {
    TestResult testResult = onDefaultController().defaultRequest()
        .changeSuffixPathWith("/variables/concurrencyWithStatics/").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verify(testResult.requestContext.getMultilang(), times(1))
        .getString("GML.action.user.forbidden");
    verifyDestination(testResult.router, "/componentName/jsp/homepage.jsp");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void variableConcurrencyVariable() throws Exception {
    TestResult testResult = onDefaultController().defaultRequest()
        .changeSuffixPathWith("/variables/concurrencyWithStatics/dynamicPath/").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/variableWay");
    Map<String, String> variables = testResult.requestContext.getPathVariables();
    assertThat(variables, notNullValue());
    assertThat(variables.size(), is(1));
    assertThat(variables, hasEntry("variablePath", "dynamicPath"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void variableConcurrencyStatic() throws Exception {
    TestResult testResult = onDefaultController().defaultRequest()
        .changeSuffixPathWith("/variables/concurrencyWithStatics/staticPath").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/staticWay");
    Map<String, String> variables = testResult.requestContext.getPathVariables();
    assertThat(variables, notNullValue());
    assertThat(variables.size(), is(0));
  }

  @Test
  public void navigationContextManagement() throws Exception {
    TestResult testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("Main").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/componentName/jsp/homepage.jsp");

    HttpRequest request = testResult.requestContext.getRequest();
    verify(request, times(1)).setAttribute("navigationContextCleared", true);
    verify(request, times(0)).setAttribute("navigationStepCreated", true);
    verify(request, times(0)).setAttribute("navigationStepReset", true);
    verify(request, times(1)).setAttribute("noNavigationStepPerformed", true);
    verify(request, times(0)).setAttribute("navigationStepTrashed", true);
    verify(request, times(0)).setAttribute("navigationStepLabelSet", true);
    verify(request, times(0)).setAttribute("navigationStepContextIdentifierSet", true);
    verify(request, times(0)).setAttribute("effectiveNavigationStepContextIdentifierSet", true);

    NavigationContext navigationContext = testResult.requestContext.getNavigationContext();
    NavigationContext.NavigationStep currentNavigationStep =
        navigationContext.getBaseNavigationStep();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getCurrentNavigationStep()));
    assertThat(currentNavigationStep, is(navigationContext.getPreviousNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), nullValue());
    assertThat(currentNavigationStep.getContextIdentifier(), nullValue());
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/Main")));
    assertThat(currentNavigationStep.getPrevious(), nullValue());
    assertThat(currentNavigationStep.getNext(), nullValue());

    /*
    NAVIGATION TO ONE
     */
    testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("navigationStep/one").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/navigationStepOne");

    request = testResult.requestContext.getRequest();
    verify(request, times(0)).setAttribute("navigationContextCleared", true);
    verify(request, times(1)).setAttribute("navigationStepCreated", true);
    verify(request, times(0)).setAttribute("navigationStepReset", true);
    verify(request, times(0)).setAttribute("noNavigationStepPerformed", true);
    verify(request, times(0)).setAttribute("navigationStepTrashed", true);
    verify(request, times(1)).setAttribute("navigationStepLabelSet", true);
    verify(request, times(1)).setAttribute("navigationStepContextIdentifierSet", true);
    verify(request, times(1)).setAttribute("effectiveNavigationStepContextIdentifierSet", true);

    navigationContext = testResult.requestContext.getNavigationContext();
    currentNavigationStep = navigationContext.getBaseNavigationStep();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getBaseNavigationStep()));
    assertThat(currentNavigationStep, is(navigationContext.getPreviousNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), nullValue());
    assertThat(currentNavigationStep.getContextIdentifier(), nullValue());
    assertThat(currentNavigationStep.getLabel(), nullValue());
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/Main")));
    assertThat(currentNavigationStep.getPrevious(), nullValue());
    assertThat(currentNavigationStep.getNext(), notNullValue());

    NavigationContext.NavigationStep previousNavigationStep = currentNavigationStep;
    currentNavigationStep = currentNavigationStep.getNext();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getCurrentNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), is("list"));
    assertThat(currentNavigationStep.getContextIdentifier(), is("navigationStepA"));
    assertThat(currentNavigationStep.getLabel(), is("navigationStepALabel"));
    // The URI is the request.getRequestUri() and not the result of Redirect mechanism
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/navigationStep/one")));
    assertThat(currentNavigationStep.getPrevious(), is(previousNavigationStep));
    assertThat(currentNavigationStep.getNext(), nullValue());

    /*
    NAVIGATION TO TWO (no NavigationStep defined)
     */
    testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("navigationStep/two").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/componentName/navigationStep/one");

    request = testResult.requestContext.getRequest();
    verify(request, times(0)).setAttribute("navigationContextCleared", true);
    verify(request, times(0)).setAttribute("navigationStepCreated", true);
    verify(request, times(0)).setAttribute("navigationStepReset", true);
    verify(request, times(1)).setAttribute("noNavigationStepPerformed", true);
    verify(request, times(0)).setAttribute("navigationStepTrashed", true);
    verify(request, times(0)).setAttribute("navigationStepLabelSet", true);
    verify(request, times(0)).setAttribute("navigationStepContextIdentifierSet", true);
    verify(request, times(0)).setAttribute("effectiveNavigationStepContextIdentifierSet", true);

    navigationContext = testResult.requestContext.getNavigationContext();
    currentNavigationStep = navigationContext.getBaseNavigationStep();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getBaseNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), nullValue());
    assertThat(currentNavigationStep.getContextIdentifier(), nullValue());
    assertThat(currentNavigationStep.getLabel(), nullValue());
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/Main")));
    assertThat(currentNavigationStep.getPrevious(), nullValue());
    assertThat(currentNavigationStep.getNext(), notNullValue());

    previousNavigationStep = currentNavigationStep;
    currentNavigationStep = currentNavigationStep.getNext();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getCurrentNavigationStep()));
    assertThat(currentNavigationStep, is(navigationContext.getPreviousNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), is("list"));
    assertThat(currentNavigationStep.getContextIdentifier(), is("navigationStepA"));
    assertThat(currentNavigationStep.getLabel(), is("navigationStepALabel"));
    // The URI is the request.getRequestUri() and not the result of Redirect mechanism
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/navigationStep/one")));
    assertThat(currentNavigationStep.getPrevious(), is(previousNavigationStep));
    assertThat(currentNavigationStep.getNext(), nullValue());

    /*
    NAVIGATION TO TWO (no NavigationStep defined)
     */
    testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("navigationStep/two").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/componentName/navigationStep/one");

    request = testResult.requestContext.getRequest();
    verify(request, times(0)).setAttribute("navigationContextCleared", true);
    verify(request, times(0)).setAttribute("navigationStepCreated", true);
    verify(request, times(0)).setAttribute("navigationStepReset", true);
    verify(request, times(1)).setAttribute("noNavigationStepPerformed", true);
    verify(request, times(0)).setAttribute("navigationStepTrashed", true);
    verify(request, times(0)).setAttribute("navigationStepLabelSet", true);
    verify(request, times(0)).setAttribute("navigationStepContextIdentifierSet", true);
    verify(request, times(0)).setAttribute("effectiveNavigationStepContextIdentifierSet", true);

    navigationContext = testResult.requestContext.getNavigationContext();
    currentNavigationStep = navigationContext.getBaseNavigationStep();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getBaseNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), nullValue());
    assertThat(currentNavigationStep.getContextIdentifier(), nullValue());
    assertThat(currentNavigationStep.getLabel(), nullValue());
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/Main")));
    assertThat(currentNavigationStep.getPrevious(), nullValue());
    assertThat(currentNavigationStep.getNext(), notNullValue());

    previousNavigationStep = currentNavigationStep;
    currentNavigationStep = currentNavigationStep.getNext();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getCurrentNavigationStep()));
    assertThat(currentNavigationStep, is(navigationContext.getPreviousNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), is("list"));
    assertThat(currentNavigationStep.getContextIdentifier(), is("navigationStepA"));
    assertThat(currentNavigationStep.getLabel(), is("navigationStepALabel"));
    // The URI is the request.getRequestUri() and not the result of Redirect mechanism
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/navigationStep/one")));
    assertThat(currentNavigationStep.getPrevious(), is(previousNavigationStep));
    assertThat(currentNavigationStep.getNext(), nullValue());

    /*
    NAVIGATION TO ONE
     */
    testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("navigationStep/one").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/navigationStepOne");

    request = testResult.requestContext.getRequest();
    verify(request, times(0)).setAttribute("navigationContextCleared", true);
    verify(request, times(0)).setAttribute("navigationStepCreated", true);
    verify(request, times(1)).setAttribute("navigationStepReset", true);
    verify(request, times(0)).setAttribute("noNavigationStepPerformed", true);
    verify(request, times(0)).setAttribute("navigationStepTrashed", true);
    verify(request, times(0)).setAttribute("navigationStepLabelSet", true);
    verify(request, times(1)).setAttribute("navigationStepContextIdentifierSet", true);
    verify(request, times(0)).setAttribute("effectiveNavigationStepContextIdentifierSet", true);

    navigationContext = testResult.requestContext.getNavigationContext();
    currentNavigationStep = navigationContext.getBaseNavigationStep();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getBaseNavigationStep()));
    assertThat(currentNavigationStep, is(navigationContext.getPreviousNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), nullValue());
    assertThat(currentNavigationStep.getContextIdentifier(), nullValue());
    assertThat(currentNavigationStep.getLabel(), nullValue());
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/Main")));
    assertThat(currentNavigationStep.getPrevious(), nullValue());
    assertThat(currentNavigationStep.getNext(), notNullValue());

    previousNavigationStep = currentNavigationStep;
    currentNavigationStep = currentNavigationStep.getNext();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getCurrentNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), is("list"));
    assertThat(currentNavigationStep.getContextIdentifier(), is("navigationStepA"));
    assertThat(currentNavigationStep.getLabel(), is("navigationStepALabel"));
    // The URI is the request.getRequestUri() and not the result of Redirect mechanism
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/navigationStep/one")));
    assertThat(currentNavigationStep.getPrevious(), is(previousNavigationStep));
    assertThat(currentNavigationStep.getNext(), nullValue());

    /*
    NAVIGATION to THREE (set view manually)
     */
    testResult = onDefaultController().defaultRequest().changeSuffixPathWith("navigationStep/three")
        .perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/componentName/navigationStep/one");

    request = testResult.requestContext.getRequest();
    verify(request, times(0)).setAttribute("navigationContextCleared", true);
    verify(request, times(1)).setAttribute("navigationStepCreated", true);
    verify(request, times(0)).setAttribute("navigationStepReset", true);
    verify(request, times(0)).setAttribute("noNavigationStepPerformed", true);
    verify(request, times(0)).setAttribute("navigationStepTrashed", true);
    verify(request, times(2)).setAttribute("navigationStepLabelSet", true);
    verify(request, times(1)).setAttribute("navigationStepContextIdentifierSet", true);
    verify(request, times(1)).setAttribute("effectiveNavigationStepContextIdentifierSet", true);

    navigationContext = testResult.requestContext.getNavigationContext();
    currentNavigationStep = navigationContext.getBaseNavigationStep();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getBaseNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), nullValue());
    assertThat(currentNavigationStep.getContextIdentifier(), nullValue());
    assertThat(currentNavigationStep.getLabel(), nullValue());
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/Main")));
    assertThat(currentNavigationStep.getPrevious(), nullValue());
    assertThat(currentNavigationStep.getNext(), notNullValue());

    previousNavigationStep = currentNavigationStep;
    currentNavigationStep = currentNavigationStep.getNext();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, not(is(navigationContext.getCurrentNavigationStep())));
    assertThat(currentNavigationStep, is(navigationContext.getPreviousNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), is("list"));
    assertThat(currentNavigationStep.getContextIdentifier(), is("navigationStepA"));
    assertThat(currentNavigationStep.getLabel(), is("navigationStepALabel"));
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/navigationStep/one")));
    assertThat(currentNavigationStep.getPrevious(), is(previousNavigationStep));
    assertThat(currentNavigationStep.getNext(), notNullValue());

    previousNavigationStep = currentNavigationStep;
    currentNavigationStep = currentNavigationStep.getNext();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getCurrentNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), is("otherList"));
    assertThat(currentNavigationStep.getContextIdentifier(), is("navigationStepC"));
    assertThat(currentNavigationStep.getLabel(), is("navigationStepThreeLabel"));
    assertThat(currentNavigationStep.getUri(),
        is(URI.create("/componentName/navigationStep/three")));
    assertThat(currentNavigationStep.getPrevious(), is(previousNavigationStep));
    assertThat(currentNavigationStep.getNext(), nullValue());

    /*
    NAVIGATION TO TWO (no NavigationStep defined)
     */
    testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("navigationStep/two").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/componentName/navigationStep/one");

    request = testResult.requestContext.getRequest();
    verify(request, times(0)).setAttribute("navigationContextCleared", true);
    verify(request, times(0)).setAttribute("navigationStepCreated", true);
    verify(request, times(0)).setAttribute("navigationStepReset", true);
    verify(request, times(1)).setAttribute("noNavigationStepPerformed", true);
    verify(request, times(0)).setAttribute("navigationStepTrashed", true);
    verify(request, times(0)).setAttribute("navigationStepLabelSet", true);
    verify(request, times(0)).setAttribute("navigationStepContextIdentifierSet", true);
    verify(request, times(0)).setAttribute("effectiveNavigationStepContextIdentifierSet", true);

    navigationContext = testResult.requestContext.getNavigationContext();
    currentNavigationStep = navigationContext.getBaseNavigationStep();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getBaseNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), nullValue());
    assertThat(currentNavigationStep.getContextIdentifier(), nullValue());
    assertThat(currentNavigationStep.getLabel(), nullValue());
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/Main")));
    assertThat(currentNavigationStep.getPrevious(), nullValue());
    assertThat(currentNavigationStep.getNext(), notNullValue());

    previousNavigationStep = currentNavigationStep;
    currentNavigationStep = currentNavigationStep.getNext();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, not(is(navigationContext.getCurrentNavigationStep())));
    assertThat(currentNavigationStep.getIdentifier(), is("list"));
    assertThat(currentNavigationStep.getContextIdentifier(), is("navigationStepA"));
    assertThat(currentNavigationStep.getLabel(), is("navigationStepALabel"));
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/navigationStep/one")));
    assertThat(currentNavigationStep.getPrevious(), is(previousNavigationStep));
    assertThat(currentNavigationStep.getNext(), notNullValue());

    previousNavigationStep = currentNavigationStep;
    currentNavigationStep = currentNavigationStep.getNext();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getCurrentNavigationStep()));
    assertThat(currentNavigationStep, is(navigationContext.getPreviousNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), is("otherList"));
    assertThat(currentNavigationStep.getContextIdentifier(), is("navigationStepC"));
    assertThat(currentNavigationStep.getLabel(), is("navigationStepThreeLabel"));
    assertThat(currentNavigationStep.getUri(),
        is(URI.create("/componentName/navigationStep/three")));
    assertThat(currentNavigationStep.getPrevious(), is(previousNavigationStep));
    assertThat(currentNavigationStep.getNext(), nullValue());

    /*
    NAVIGATION TO ONE
     */
    testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("navigationStep/one").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/navigationStepOne");

    request = testResult.requestContext.getRequest();
    verify(request, times(0)).setAttribute("navigationContextCleared", true);
    verify(request, times(0)).setAttribute("navigationStepCreated", true);
    verify(request, times(1)).setAttribute("navigationStepReset", true);
    verify(request, times(0)).setAttribute("noNavigationStepPerformed", true);
    verify(request, times(1)).setAttribute("navigationStepTrashed", true);
    verify(request, times(0)).setAttribute("navigationStepLabelSet", true);
    verify(request, times(1)).setAttribute("navigationStepContextIdentifierSet", true);
    verify(request, times(0)).setAttribute("effectiveNavigationStepContextIdentifierSet", true);

    navigationContext = testResult.requestContext.getNavigationContext();
    currentNavigationStep = navigationContext.getBaseNavigationStep();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getBaseNavigationStep()));
    assertThat(currentNavigationStep, is(navigationContext.getPreviousNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), nullValue());
    assertThat(currentNavigationStep.getContextIdentifier(), nullValue());
    assertThat(currentNavigationStep.getLabel(), nullValue());
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/Main")));
    assertThat(currentNavigationStep.getPrevious(), nullValue());
    assertThat(currentNavigationStep.getNext(), notNullValue());

    previousNavigationStep = currentNavigationStep;
    currentNavigationStep = currentNavigationStep.getNext();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getCurrentNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), is("list"));
    assertThat(currentNavigationStep.getContextIdentifier(), is("navigationStepA"));
    assertThat(currentNavigationStep.getLabel(), is("navigationStepALabel"));
    // The URI is the request.getRequestUri() and not the result of Redirect mechanism
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/navigationStep/one")));
    assertThat(currentNavigationStep.getPrevious(), is(previousNavigationStep));
    assertThat(currentNavigationStep.getNext(), nullValue());

    /*
    NAVIGATION TO HOMEPAGE
     */
    testResult = onDefaultController().defaultRequest().changeSuffixPathWith("Main").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/componentName/jsp/homepage.jsp");

    request = testResult.requestContext.getRequest();
    verify(request, times(1)).setAttribute("navigationContextCleared", true);
    verify(request, times(0)).setAttribute("navigationStepCreated", true);
    verify(request, times(0)).setAttribute("navigationStepReset", true);
    verify(request, times(1)).setAttribute("noNavigationStepPerformed", true);
    verify(request, times(1)).setAttribute("navigationStepTrashed", true);
    verify(request, times(0)).setAttribute("navigationStepLabelSet", true);
    verify(request, times(0)).setAttribute("navigationStepContextIdentifierSet", true);
    verify(request, times(0)).setAttribute("effectiveNavigationStepContextIdentifierSet", true);

    navigationContext = testResult.requestContext.getNavigationContext();
    currentNavigationStep = navigationContext.getBaseNavigationStep();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getCurrentNavigationStep()));
    assertThat(currentNavigationStep, is(navigationContext.getPreviousNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), nullValue());
    assertThat(currentNavigationStep.getContextIdentifier(), nullValue());
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/Main")));
    assertThat(currentNavigationStep.getPrevious(), nullValue());
    assertThat(currentNavigationStep.getNext(), nullValue());

    /*
    NAVIGATION TO ONE
     */
    testResult =
        onDefaultController().defaultRequest().changeSuffixPathWith("navigationStep/one").perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/navigationStepOne");

    request = testResult.requestContext.getRequest();
    verify(request, times(0)).setAttribute("navigationContextCleared", true);
    verify(request, times(1)).setAttribute("navigationStepCreated", true);
    verify(request, times(0)).setAttribute("navigationStepReset", true);
    verify(request, times(0)).setAttribute("noNavigationStepPerformed", true);
    verify(request, times(0)).setAttribute("navigationStepTrashed", true);
    verify(request, times(1)).setAttribute("navigationStepLabelSet", true);
    verify(request, times(1)).setAttribute("navigationStepContextIdentifierSet", true);
    verify(request, times(1)).setAttribute("effectiveNavigationStepContextIdentifierSet", true);

    navigationContext = testResult.requestContext.getNavigationContext();
    currentNavigationStep = navigationContext.getBaseNavigationStep();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getBaseNavigationStep()));
    assertThat(currentNavigationStep, is(navigationContext.getPreviousNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), nullValue());
    assertThat(currentNavigationStep.getContextIdentifier(), nullValue());
    assertThat(currentNavigationStep.getLabel(), nullValue());
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/Main")));
    assertThat(currentNavigationStep.getPrevious(), nullValue());
    assertThat(currentNavigationStep.getNext(), notNullValue());

    previousNavigationStep = currentNavigationStep;
    currentNavigationStep = currentNavigationStep.getNext();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getCurrentNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), is("list"));
    assertThat(currentNavigationStep.getContextIdentifier(), is("navigationStepA"));
    assertThat(currentNavigationStep.getLabel(), is("navigationStepALabel"));
    // The URI is the request.getRequestUri() and not the result of Redirect mechanism
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/navigationStep/one")));
    assertThat(currentNavigationStep.getPrevious(), is(previousNavigationStep));
    assertThat(currentNavigationStep.getNext(), nullValue());

    /*
    NAVIGATION to THREE (set view manually)
     */
    testResult = onDefaultController().defaultRequest().changeSuffixPathWith("navigationStep/three")
        .perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/componentName/navigationStep/one");

    request = testResult.requestContext.getRequest();
    verify(request, times(0)).setAttribute("navigationContextCleared", true);
    verify(request, times(1)).setAttribute("navigationStepCreated", true);
    verify(request, times(0)).setAttribute("navigationStepReset", true);
    verify(request, times(0)).setAttribute("noNavigationStepPerformed", true);
    verify(request, times(0)).setAttribute("navigationStepTrashed", true);
    verify(request, times(2)).setAttribute("navigationStepLabelSet", true);
    verify(request, times(1)).setAttribute("navigationStepContextIdentifierSet", true);
    verify(request, times(1)).setAttribute("effectiveNavigationStepContextIdentifierSet", true);

    navigationContext = testResult.requestContext.getNavigationContext();
    currentNavigationStep = navigationContext.getBaseNavigationStep();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getBaseNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), nullValue());
    assertThat(currentNavigationStep.getContextIdentifier(), nullValue());
    assertThat(currentNavigationStep.getLabel(), nullValue());
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/Main")));
    assertThat(currentNavigationStep.getPrevious(), nullValue());
    assertThat(currentNavigationStep.getNext(), notNullValue());

    previousNavigationStep = currentNavigationStep;
    currentNavigationStep = currentNavigationStep.getNext();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, not(is(navigationContext.getCurrentNavigationStep())));
    assertThat(currentNavigationStep, is(navigationContext.getPreviousNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), is("list"));
    assertThat(currentNavigationStep.getContextIdentifier(), is("navigationStepA"));
    assertThat(currentNavigationStep.getLabel(), is("navigationStepALabel"));
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/navigationStep/one")));
    assertThat(currentNavigationStep.getPrevious(), is(previousNavigationStep));
    assertThat(currentNavigationStep.getNext(), notNullValue());

    previousNavigationStep = currentNavigationStep;
    currentNavigationStep = currentNavigationStep.getNext();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getCurrentNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), is("otherList"));
    assertThat(currentNavigationStep.getContextIdentifier(), is("navigationStepC"));
    assertThat(currentNavigationStep.getLabel(), is("navigationStepThreeLabel"));
    assertThat(currentNavigationStep.getUri(),
        is(URI.create("/componentName/navigationStep/three")));
    assertThat(currentNavigationStep.getPrevious(), is(previousNavigationStep));
    assertThat(currentNavigationStep.getNext(), nullValue());

    /*
    NAVIGATION TO A PAGE THAT CLEAR THE NAVIGATION CONTEXT
     */
    testResult = onDefaultController().defaultRequest().changeSuffixPathWith("navigationStep/clear")
        .perform();
    verify(testResult.requestContext.getResponse(), times(0)).sendError(anyInt());
    verifyDestination(testResult.router, "/navigationStepClear");

    request = testResult.requestContext.getRequest();
    verify(request, times(1)).setAttribute("navigationContextCleared", true);
    verify(request, times(0)).setAttribute("navigationStepCreated", true);
    verify(request, times(0)).setAttribute("navigationStepReset", true);
    verify(request, times(1)).setAttribute("noNavigationStepPerformed", true);
    verify(request, times(2)).setAttribute("navigationStepTrashed", true);
    verify(request, times(0)).setAttribute("navigationStepLabelSet", true);
    verify(request, times(0)).setAttribute("navigationStepContextIdentifierSet", true);
    verify(request, times(0)).setAttribute("effectiveNavigationStepContextIdentifierSet", true);

    navigationContext = testResult.requestContext.getNavigationContext();
    currentNavigationStep = navigationContext.getBaseNavigationStep();
    assertThat(currentNavigationStep, notNullValue());
    assertThat(currentNavigationStep, is(navigationContext.getCurrentNavigationStep()));
    assertThat(currentNavigationStep, is(navigationContext.getPreviousNavigationStep()));
    assertThat(currentNavigationStep.getIdentifier(), nullValue());
    assertThat(currentNavigationStep.getContextIdentifier(), nullValue());
    assertThat(currentNavigationStep.getUri(), is(URI.create("/componentName/Main")));
    assertThat(currentNavigationStep.getPrevious(), nullValue());
    assertThat(currentNavigationStep.getNext(), nullValue());
  }

  private ControllerTest<TestWebComponentController, TestWebComponentRequestContext>
  onDefaultController() {
    return onController(TestWebComponentController.class);
  }
}
