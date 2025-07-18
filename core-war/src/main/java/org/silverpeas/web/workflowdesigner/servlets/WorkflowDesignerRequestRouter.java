/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.web.workflowdesigner.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.AdminComponentRequestRouter;
import org.silverpeas.core.web.token.SynchronizerTokenService;
import org.silverpeas.core.workflow.api.Workflow;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.*;
import org.silverpeas.core.workflow.engine.model.ProcessModelImpl;
import org.silverpeas.core.workflow.engine.model.SpecificLabel;
import org.silverpeas.core.workflow.engine.model.StateRef;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.web.workflowdesigner.control.WorkflowDesignerSessionController;
import org.silverpeas.web.workflowdesigner.model.WorkflowDesignerException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.*;

public class WorkflowDesignerRequestRouter extends
    AdminComponentRequestRouter<WorkflowDesignerSessionController> {

  private static final long serialVersionUID = -6747786008527861783L;
  private static final String REDIRECT_TO = "redirectTo";
  private static Map<String, FunctionHandler> mapHandler; // mapping of functions to their handlers
  private static final String ROOT_URL = "/workflowDesigner/jsp/"; // the root
  private static final String REDIRECTION_URL = ROOT_URL + "redirect.jsp";

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "WorkflowDesigner";
  }

  @Override
  public WorkflowDesignerSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new WorkflowDesignerSessionController(mainSessionCtrl, componentContext);
  }

  @Override
  public String getAdminDestination(String function,
      WorkflowDesignerSessionController workflowDesignerSC, HttpRequest request) {
    String destination = null;
    FunctionHandler handler = getHandler(function);

    try {
      if (handler != null) {
        destination = handler.getDestination(function, workflowDesignerSC, request);
      }

      if (destination == null) {
        request.setAttribute(REDIRECT_TO, "Main");
        destination = REDIRECTION_URL;
      }


    } catch (WorkflowDesignerException | WorkflowException e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = ROOT_URL + "errorpageMain.jsp";
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }


    return destination;
  }

  @Override
  public void init() throws ServletException {
    super.init();
    initHandlers();
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    initHandlers();
  }

  /**
   *
   */
  private static FunctionHandler getHandler(String function) {
    return mapHandler.get(function);
  }

  /**
   * Initialise the map of the function handlers
   */
  private synchronized void initHandlers() {
    if (mapHandler != null) {
      return;
    }

    mapHandler = new HashMap<>();

    mapHandler.put("Main", hndlListWorkflow);
    mapHandler.put("AddWorkflow", hndlEditWorkflow);
    mapHandler.put("ImportWorkflow", hndlImportWorkflow);
    mapHandler.put("DoImportWorkflow", hndlImportWorkflow);
    mapHandler.put("EditWorkflow", hndlEditWorkflow);
    mapHandler.put("ModifyWorkflow", hndlEditWorkflow);
    mapHandler.put("UpdateWorkflow", hndlUpdateWorkflow);
    mapHandler.put("RemoveWorkflow", hndlRemoveWorkflow);
    mapHandler.put("GenerateComponentDescription", hndlGenerateComponentDescription);
    mapHandler.put("ViewRoles", hndlViewRoles);
    mapHandler.put("AddRole", hndlEditRole);
    mapHandler.put("ModifyRole", hndlEditRole);
    mapHandler.put("UpdateRole", hndlUpdateRole);
    mapHandler.put("RemoveRole", hndlRemoveRole);
    mapHandler.put("ViewPresentation", hndlViewPresentation);
    mapHandler.put("AddColumns", hndlEditColumns);
    mapHandler.put("ModifyColumns", hndlEditColumns);
    mapHandler.put("UpdateColumns", hndlUpdateColumns);
    mapHandler.put("RemoveColumns", hndlRemoveColumns);
    mapHandler.put("ViewParticipants", hndlViewParticipants);
    mapHandler.put("AddParticipant", hndlEditParticipant);
    mapHandler.put("ModifyParticipant", hndlEditParticipant);
    mapHandler.put("UpdateParticipant", hndlUpdateParticipant);
    mapHandler.put("RemoveParticipant", hndlRemoveParticipant);
    mapHandler.put("ViewStates", hndlViewStates);
    mapHandler.put("AddState", hndlEditState);
    mapHandler.put("ModifyState", hndlEditState);
    mapHandler.put("UpdateState", hndlUpdateState);
    mapHandler.put("RemoveState", hndlRemoveState);
    mapHandler.put("AddQualifiedUsers", hndlEditQualifiedUsers);
    mapHandler.put("ModifyQualifiedUsers", hndlEditQualifiedUsers);
    mapHandler.put("UpdateQualifiedUsers", hndlUpdateQualifiedUsers);
    mapHandler.put("RemoveQualifiedUsers", hndlRemoveQualifiedUsers);
    mapHandler.put("AddRelatedUser", hndlEditRelatedUser);
    mapHandler.put("ModifyRelatedUser", hndlEditRelatedUser);
    mapHandler.put("UpdateRelatedUser", hndlUpdateRelatedUser);
    mapHandler.put("RemoveRelatedUser", hndlRemoveRelatedUser);
    mapHandler.put("ViewActions", hndlViewActions);
    mapHandler.put("AddAction", hndlEditAction);
    mapHandler.put("ModifyAction", hndlEditAction);
    mapHandler.put("UpdateAction", hndlUpdateAction);
    mapHandler.put("RemoveAction", hndlRemoveAction);
    mapHandler.put("AddConsequence", hndlEditConsequence);
    mapHandler.put("ModifyConsequence", hndlEditConsequence);
    mapHandler.put("UpdateConsequence", hndlUpdateConsequence);
    mapHandler.put("MoveConsequence", hndlMoveConsequence);
    mapHandler.put("RemoveConsequence", hndlRemoveConsequence);
    mapHandler.put("ViewUserInfos", hndlViewUserInfos);
    mapHandler.put("ViewDataFolder", hndlViewDataFolder);
    mapHandler.put("ViewForms", hndlViewForms);
    mapHandler.put("AddForm", hndlEditForm);
    mapHandler.put("EditForm", hndlEditForm);
    mapHandler.put("UpdateForm", hndlUpdateForm);
    mapHandler.put("RemoveForm", hndlRemoveForm);
    mapHandler.put("AddInput", hndlEditInput);
    mapHandler.put("ModifyInput", hndlEditInput);
    mapHandler.put("UpdateInput", hndlUpdateInput);
    mapHandler.put("RemoveInput", hndlRemoveInput);
    mapHandler.put("AddContextualDesignation", hndlEditContextualDesignation);
    mapHandler
        .put("ModifyContextualDesignation", hndlEditContextualDesignation);
    mapHandler.put("UpdateContextualDesignation",
        hndlUpdateContextualDesignation);
    mapHandler.put("RemoveContextualDesignation",
        hndlRemoveContextualDesignation);
    mapHandler.put("AddItem", hndlEditItem);
    mapHandler.put("ModifyItem", hndlEditItem);
    mapHandler.put("UpdateItem", hndlUpdateItem);
    mapHandler.put("RemoveItem", hndlRemoveItem);
    mapHandler.put("AddParameter", hndlEditParameter);
    mapHandler.put("ModifyParameter", hndlEditParameter);
    mapHandler.put("UpdateParameter", hndlUpdateParameter);
    mapHandler.put("RemoveParameter", hndlRemoveParameter);

  }

  /**
   * Handles the "Main" function and lists the workflows
   */
  private static final FunctionHandler hndlListWorkflow = (function, workflowDesignerSC, request) -> {
    Workflow.getProcessModelManager().clearProcessModelCache();
    request.setAttribute("ProcessFileNames", workflowDesignerSC.listProcessModels());
    return ROOT_URL + "welcome.jsp";
  };

  /**
   * Handles the "AddWorkflow", "EditWorkflow" and "ModifyWorkflow" functions
   */
  private static final FunctionHandler hndlEditWorkflow = (function, workflowDesignerSC, request) -> {
    String strProcessFileName;
    ProcessModel processModel;

    if ("AddWorkflow".equals(function)) {
      processModel = workflowDesignerSC.createProcessModel();
      strProcessFileName = workflowDesignerSC.getProcessFileName();
    } else if ("EditWorkflow".equals(function)) {
      // Loading the process model into the session controller
      //
      strProcessFileName = request.getParameter("ProcessFileName");

      if (StringUtil.isDefined(strProcessFileName)) {
        workflowDesignerSC.loadProcessModel(strProcessFileName);
      }

      // redirect to change the function name and remove the parameter
      // "ProcessFileName"
      //
      request.setAttribute(REDIRECT_TO, "ModifyWorkflow");
      return REDIRECTION_URL;
    } else {
      // ModifyWorkflow

      // Get the Process Model and the file name from the SC
      //
      processModel = workflowDesignerSC.getProcessModel();
      strProcessFileName = workflowDesignerSC.getProcessFileName();

    }
    Workflow.getProcessModelManager().clearProcessModelCache();

    request.setAttribute("ProcessModel", processModel);
    request.setAttribute("ProcessFileName", strProcessFileName);
    request.setAttribute("IsANewProcess", workflowDesignerSC.isNewProcessModel());

    // also pass an info whether the component descriptor has already been
    // defined.
    request.setAttribute("componentDescriptor", workflowDesignerSC.getComponentDescriptorName());
    return ROOT_URL + "workflow.jsp";
  };

  /**
   * Handles the "ImportWorkflow, DoImportWorkflow" functions
   */
  private static final FunctionHandler hndlImportWorkflow = (function, workflowDesignerSC, request) -> {
    if ("ImportWorkflow".equals(function)) {
      return ROOT_URL + "importWorkflow.jsp";
    } else if ("DoImportWorkflow".equals(function)) {
      List<FileItem> items = HttpRequest.decorate(request).getFileItems();

      /*
       * Une seule donnée le fichier à uploader.
       */
      if (items.size() == 1) {
        FileItem item = items.get(0);

        workflowDesignerSC.uploadProcessModel(item);

      }

      return hndlListWorkflow.getDestination("Main", workflowDesignerSC,
          request);
    } else {
      return hndlListWorkflow.getDestination("Main", workflowDesignerSC,
          request);
    }
  };

  /**
   * Handles the "UpdateWorkflow" function
   */
  private static final FunctionHandler hndlUpdateWorkflow = (function, workflowDesignerSC, request) -> {
    String strProcessName = request.getParameter("name");

    if (StringUtil.isDefined(strProcessName)) {
      ProcessModel processModel = new ProcessModelImpl();

      processModel.setName(strProcessName);
      workflowDesignerSC.updateProcessModelHeader(processModel);
    }

    workflowDesignerSC.saveProcessModel();

    request.setAttribute(REDIRECT_TO, "Main");
    return REDIRECTION_URL;
  };

  /**
   * Handles the "RemoveWorkflow" function
   */
  private static final FunctionHandler hndlRemoveWorkflow = (function, workflowDesignerSC, request) -> {
    String processName = URLDecoder.decode(request.getParameter("ProcessFileName"), Charsets.UTF_8);
    workflowDesignerSC.removeProcessModel(processName);
    request.setAttribute(REDIRECT_TO, "Main");
    return REDIRECTION_URL;
  };

  /**
   * Handles the "GenerateComponentDescription" function and lists the workflows
   */
  private static final FunctionHandler hndlGenerateComponentDescription =
      (function, workflowDesignerSC, request) -> {
        // Generate the component descriptor
        workflowDesignerSC.generateComponentDescriptor();

        request.setAttribute(REDIRECT_TO, "ModifyWorkflow");
        return REDIRECTION_URL;
      };

  /**
   * Handles the "ViewRoles" function
   */
  private static final FunctionHandler hndlViewRoles = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Roles", workflowDesignerSC.getProcessModel().getRolesEx());

    return ROOT_URL + "roles.jsp";
  };

  /**
   * Handles the "AddRole" and "ModifyRole" function
   */
  private static final FunctionHandler hndlEditRole = (function, workflowDesignerSC, request) -> {
    String strRoleName = request.getParameter("role");
    Role role;

    if ("AddRole".equals(function)) {
      role = workflowDesignerSC.createRole();
      request.setAttribute("IsExisitingRole", Boolean.FALSE);
    } else // ModifyRole
    {
      role = workflowDesignerSC.getProcessModel().getRolesEx().getRole(strRoleName);
      request.setAttribute("IsExisitingRole", Boolean.TRUE);
    }

    if (role == null) {
      throw new WorkflowException("WorkflowDesignerRequestRouter.hndlEditRole", //$NON-NLS-1$
          SilverpeasException.ERROR, "workflowEngine.EX_ROLE_NOT_FOUND"); //$NON-NLS-1$
    }
    request.setAttribute("Role", role);

    return ROOT_URL + "editRole.jsp";

  };

  private static final String NAME_ORIGINAL = "name_original";
  /**
   * Handles the "UpdateRole" function
   */
  private static final FunctionHandler hndlUpdateRole = (function, workflowDesignerSC, request) -> {
    Role role = workflowDesignerSC.createRole();

    role.setName(request.getParameter("name"));
    workflowDesignerSC.updateRole(role, request.getParameter(NAME_ORIGINAL));

    request.setAttribute(REDIRECT_TO, "ViewRoles");
    return REDIRECTION_URL;
  };

  /**
   * Handles the "RemoveRole" function
   */
  private static final FunctionHandler hndlRemoveRole = (function, workflowDesignerSC, request) -> {
    String roleName = URLDecoder.decode(request.getParameter("role"), Charsets.UTF_8);
    workflowDesignerSC.removeRole(roleName);

    request.setAttribute(REDIRECT_TO, "ViewRoles");
    return REDIRECTION_URL;
  };

  /**
   * Handles the "ViewPresentation" function
   */
  private static final FunctionHandler hndlViewPresentation = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Presentation", workflowDesignerSC.getProcessModel().getPresentation());
    return ROOT_URL + "presentation.jsp";
  };

  /**
   * Handles the "AddColumns" and "ModifyColumns" functions
   */
  private static final FunctionHandler hndlEditColumns = (function, workflowDesignerSC, request) -> {
    Columns columns;

    if ("AddColumns".equals(function)) {
      columns = workflowDesignerSC.addColumns();
    } else // ModifyColumns
    {
      String strColumnsName = request.getParameter("columns");
        //Get columns checked in the page
      columns =
          workflowDesignerSC.getProcessModel().getPresentation().getColumnsByRole(strColumnsName);
    }

    if (columns == null) {
      throw new WorkflowException("WorkflowDesignerRequestRouter.hndlEditColumns", //$NON-NLS-1$
          SilverpeasException.ERROR, "workflowEngine.EX_COLUMNS_NOT_FOUND"); //$NON-NLS-1$
    }
    request.setAttribute("Columns", columns);
    request.setAttribute("RoleNames", workflowDesignerSC.retrieveRoleNames(false, true));
    request.setAttribute("FolderItemNames",
        workflowDesignerSC.retrieveFolderItemNames(false, false));

    return ROOT_URL + "editColumns.jsp";
  };

  /**
   * Handles the "UpdateColumns" function
   */
  private static final FunctionHandler hndlUpdateColumns = (function, workflowDesignerSC, request) -> {
      String role = null;
      if (StringUtil.isDefined(request.getParameter("role")))
          role = request.getParameter("role");

      //Get columns checked in the page
      String[] astrColumnNames = request.getParameterValues("column");
      Column column;
      Columns columns = workflowDesignerSC.getProcessModel().getPresentation().createColumns();
      columns.setRoleName(role);

      for (int i = 0; astrColumnNames != null && i < astrColumnNames.length; i++) {
          column = columns.createColumn();
          column.setItem(workflowDesignerSC.getProcessModel().getDataFolder()
              .getItem(astrColumnNames[i]));
          columns.addColumn(column);
      }
    // The 'Columns' original name has been stored in a hidden field,
    // to be able to identify the object later in the case the name changed...
    //
    workflowDesignerSC.updateColumns(columns, request.getParameter("role_original"));

    request.setAttribute(REDIRECT_TO, "ViewPresentation");
    return REDIRECTION_URL;
  };

  /**
   * Handles the "RemoveColumns" function
   */
  private static final FunctionHandler hndlRemoveColumns = (function, workflowDesignerSC, request) -> {
    String column = URLDecoder.decode(request.getParameter("column"), Charsets.UTF_8);
    workflowDesignerSC.deleteColumns(column);

    request.setAttribute(REDIRECT_TO, "ViewPresentation");
    return REDIRECTION_URL;
  };

  /**
   * Handles the "ViewParticipants" function
   */
  private static final FunctionHandler hndlViewParticipants = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Participants", workflowDesignerSC.getProcessModel().getParticipantsEx());
    return ROOT_URL + "participants.jsp";
  };

  /**
   * Handles the "AddParticipant" and "ModifyParicipant" function
   */
  private static final FunctionHandler hndlEditParticipant = (function, workflowDesignerSC, request) -> {
    String strParticipantName = request.getParameter("participant");
    Participant participant;

    if ("AddParticipant".equals(function)) {
      participant = workflowDesignerSC.createParticipant();
      request.setAttribute("IsExisitingParticipant", Boolean.FALSE);
    } else // ModifyParticipant
    {
      participant = workflowDesignerSC.getProcessModel()
          .getParticipantsEx()
          .getParticipant(strParticipantName);
      request.setAttribute("IsExisitingParticipant", Boolean.TRUE);
    }

    if (participant == null) {
      throw new WorkflowException("WorkflowDesignerRequestRouter.hndlEditParticipant",
          //$NON-NLS-1$
          SilverpeasException.ERROR, "workflowEngine.EX_PARTICIPANT_NOT_FOUND"); //$NON-NLS-1$
    }
    request.setAttribute("Participant", participant);
    request.setAttribute("StateNames", workflowDesignerSC.retrieveStateNames(true));

    return ROOT_URL + "editParticipant.jsp";
  };

  /**
   * Handles the "UpdateParticipant" function
   */
  private static final FunctionHandler hndlUpdateParticipant =
      (function, workflowDesignerSC, request) -> {
        Participant participant = workflowDesignerSC.createParticipant();
        String strResolvedState = request.getParameter("resolvedState");

        if (!StringUtil.isDefined(strResolvedState)) {
          strResolvedState = null;
        }

        participant.setName(request.getParameter("name"));
        participant.setResolvedState(strResolvedState);
        workflowDesignerSC.updateParticipant(participant, request.getParameter(NAME_ORIGINAL));

        request.setAttribute(REDIRECT_TO, "ViewParticipants");
        return REDIRECTION_URL;
      };

  /**
   * Handles the "RemoveParticipant" function
   */
  private static final FunctionHandler hndlRemoveParticipant =
      (function, workflowDesignerSC, request) -> {
        String participant = URLDecoder.decode(request.getParameter("participant"), Charsets.UTF_8);
        workflowDesignerSC.removeParticipant(participant);

        request.setAttribute(REDIRECT_TO, "ViewParticipants");
        return REDIRECTION_URL;
      };

  /**
   * Handles the "ViewStates" function
   */
  private static final FunctionHandler hndlViewStates = (function, workflowDesignerSC, request) -> {
    request.setAttribute("States", workflowDesignerSC.getProcessModel().getStatesEx());
    return ROOT_URL + "states.jsp";
  };

  /**
   * Handles the "AddState" and "ModifyState" function
   */
  private static final FunctionHandler hndlEditState = (function, workflowDesignerSC, request) -> {
    String strStateName = request.getParameter("state");
    State state;

    if ("AddState".equals(function)) {
      state = workflowDesignerSC.createState();
      request.setAttribute("IsExisitingState", Boolean.FALSE);
    } else // ModifyState
    {
      state = workflowDesignerSC.getProcessModel().getStatesEx().getState(strStateName);
      request.setAttribute("IsExisitingState", Boolean.TRUE);
    }

    if (state == null) {
      throw new WorkflowException("WorkflowDesignerRequestRouter.hndlEditState", //$NON-NLS-1$
          SilverpeasException.ERROR, "workflowEngine.EX_STATE_NOT_FOUND"); //$NON-NLS-1$
    }
    request.setAttribute("State", state);
    request.setAttribute("ActionNames", workflowDesignerSC.retrieveActionNames(true));

    return ROOT_URL + "editState.jsp";
  };

  /**
   * Handles the "UpdateState" function
   */
  private static final FunctionHandler hndlUpdateState = (function, workflowDesignerSC, request) -> {
    State state = workflowDesignerSC.createState();
    String strTimeoutAction = request.getParameter("timeoutAction");
    String strTimeoutInterval = request.getParameter("timeoutInterval");
    String strNotifyAdmin = request.getParameter("notifyAdmin");
    String[] astrAllowedActions = request.getParameterValues("allow");

    state.setName(request.getParameter("name"));

    if (StringUtil.isDefined(strTimeoutAction)) {
      state.setTimeoutAction(
          workflowDesignerSC.getProcessModel().getActionsEx().getAction(strTimeoutAction));

      if (StringUtil.isDefined(strTimeoutInterval)) {
        state.setTimeoutInterval(Integer.parseInt(strTimeoutInterval));
      }

      state.setTimeoutNotifyAdmin(StringUtil.isDefined(strNotifyAdmin));
    }

    // Allowed Actions
    //
    if (astrAllowedActions != null) {
      AllowedActions allowedActions = state.createAllowedActions();
      AllowedAction allowedAction;

      for (String astrAllowedAction : astrAllowedActions) {
        allowedAction = allowedActions.createAllowedAction();
        allowedAction.setAction(
            workflowDesignerSC.getProcessModel().getActionsEx().getAction(astrAllowedAction));
        allowedActions.addAllowedAction(allowedAction);
      }
      state.setAllowedActions(allowedActions);
    }

    workflowDesignerSC.updateState(state, request.getParameter(NAME_ORIGINAL));

    request.setAttribute(REDIRECT_TO, "ViewStates");
    return REDIRECTION_URL;
  };

  /**
   * Handles the "RemoveState" function
   */
  private static final FunctionHandler hndlRemoveState = (function, workflowDesignerSC, request) -> {
    String state = URLDecoder.decode(request.getParameter("state"), Charsets.UTF_8);
    workflowDesignerSC.removeState(state);

    request.setAttribute(REDIRECT_TO, "ViewStates");
    return REDIRECTION_URL;
  };

  private static final String CONTEXT = "context";
  private static final String PARENT_SCREEN = "parentScreen";
  /**
   * Handles the "AddQualifiedUsers" and "ModifyQualifiedUsers" function
   */
  private static final FunctionHandler hndlEditQualifiedUsers =
      (function, workflowDesignerSC, request) -> {
        String strContext = request.getParameter(CONTEXT);
        QualifiedUsers qualifiedUsers;
        StringTokenizer strtok;
        String strElement;

        if ("AddQualifiedUsers".equals(function)) {
          qualifiedUsers = workflowDesignerSC.getProcessModel().createQualifiedUsers();
          qualifiedUsers.setRole(WorkflowDesignerSessionController.NEW_ELEMENT_NAME);
          request.setAttribute("IsExisitingQualifiedUser", Boolean.FALSE);
        } else // ModifyQualifiedUsers
        {
          qualifiedUsers = workflowDesignerSC.findQualifiedUsers(strContext);
          request.setAttribute("IsExisitingQualifiedUser", Boolean.TRUE);
        }

        if (qualifiedUsers == null) {
          throw new WorkflowDesignerException("WorkflowDesignerRequestRouter.hndlEditQualifiedUser",
              //$NON-NLS-1$
              SilverpeasException.ERROR,
              "workflowDesigner.EX_QUALIFIED_USERS_NOT_FOUND"); //$NON-NLS-1$
        }
        // Configure the QualifiedUsrs editor to match the content of
        // worikingUsers,
        // notifiedUsers, allowedUsers or interestedUsers
        //
        if (strContext != null) {
          strtok = new StringTokenizer(strContext, "/"); //$NON-NLS-1$

          try {
            strElement = strtok.nextToken();

            if (WorkflowDesignerSessionController.STATES.equals(strElement)) {
              request.setAttribute("RoleSelector", Boolean.TRUE);
              request.setAttribute("NotifiedUser", Boolean.FALSE);
            } else if (WorkflowDesignerSessionController.ACTIONS
                .equals(strElement)) {

              // noinspection UnusedAssignment
              strElement = strtok.nextToken(); // action name
              strElement = strtok.nextToken(); // allowed users or consequences

              if (WorkflowDesignerSessionController.ALLOWED_USERS.equals(strElement)) {
                request.setAttribute("RoleSelector", Boolean.FALSE);
                request.setAttribute("NotifiedUser", Boolean.FALSE);
              } else if (WorkflowDesignerSessionController.CONSEQUENCES.equals(strElement)) {
                request.setAttribute("RoleSelector", Boolean.FALSE);
                request.setAttribute("NotifiedUser", Boolean.TRUE);
              }
            }
          } catch (NoSuchElementException e) {
            // Thrown when no token was found where expected
            // do nothing
          }
        }

        request.setAttribute("RoleNames", workflowDesignerSC.retrieveRoleNames(true, false));
        request.setAttribute("QualifiedUsers", qualifiedUsers);
        request.setAttribute(CONTEXT, strContext);
        request.setAttribute(PARENT_SCREEN, calculateParentScreen(request, workflowDesignerSC,
            strContext));
        request.setAttribute("EditorName",
            calculateEditorName(Objects.requireNonNull(strContext)));

        return ROOT_URL + "editQualifiedUsers.jsp";

      };

  /**
   * Handles the "UpdateQualifiedUsers" function
   */
  private static final FunctionHandler hndlUpdateQualifiedUsers =
      (function, workflowDesignerSC, request) -> {
        final String strRole = request.getParameter("role");
        final String strContext = request.getParameter(CONTEXT);
        final String strMessage = request.getParameter("message");
        final String linkDisabled = request.getParameter("linkDisabled");
        String[] astrUserInRole = request.getParameterValues("userInRole");
        QualifiedUsers qualifiedUsers = workflowDesignerSC.getProcessModel().createQualifiedUsers();
        UserInRole userInRole;

        // Check if there is anything entered in the form
        //
        if (StringUtil.isDefined(strRole)) {
          qualifiedUsers.setRole(strRole);
        }

        // Read the user in role
        //
        if (astrUserInRole != null && StringUtil.isDefined(astrUserInRole[0])) {
          for (String anAstrUserInRole : astrUserInRole) {
            userInRole = qualifiedUsers.createUserInRole();
            userInRole.setRoleName(anAstrUserInRole);
            qualifiedUsers.addUserInRole(userInRole);
          }
        }

        // read the message.
        if (StringUtil.isDefined(strMessage)) {
          qualifiedUsers.setMessage(strMessage);
          qualifiedUsers.setLinkDisabled(Boolean.valueOf(linkDisabled));
        }

        // call the update in session controller
        //
        workflowDesignerSC.updateQualifiedUsers(qualifiedUsers, strContext);

        request.setAttribute(REDIRECT_TO, calculateParentScreen(request, workflowDesignerSC,
            strContext));
        return REDIRECTION_URL;
      };

  /**
   * Handles the "RemoveQualifiedUsers" function
   */
  private static final FunctionHandler hndlRemoveQualifiedUsers =
      (function, workflowDesignerSC, request) -> {
        String strContext = URLDecoder.decode(request.getParameter(CONTEXT), Charsets.UTF_8);

        workflowDesignerSC.setQualifiedUsers(null, strContext);

        request.setAttribute(REDIRECT_TO, calculateParentScreen(request, workflowDesignerSC,
            strContext));
        return REDIRECTION_URL;
      };

  /**
   * Handles the "ViewActions" function
   */
  private static final FunctionHandler hndlViewActions = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Actions", workflowDesignerSC.getProcessModel().getActionsEx());

    return ROOT_URL + "actions.jsp";
  };

  /**
   * Handles the "AddAction" and "ModifyAction" function
   */
  private static final FunctionHandler hndlEditAction = (function, workflowDesignerSC, request) -> {
    String strActionName = request.getParameter("action");
    Action action;

    if ("AddAction".equals(function)) {
      action = workflowDesignerSC.createAction();
      request.setAttribute("IsExisitingAction", Boolean.FALSE);
    } else // ModifyAction
    {
      action = workflowDesignerSC.getProcessModel().getActionsEx().getAction(strActionName);
      request.setAttribute("IsExisitingAction", Boolean.TRUE);
    }

    if (action == null) {
      throw new WorkflowException("WorkflowDesignerRequestRouter.hndlEditAction", //$NON-NLS-1$
          SilverpeasException.ERROR,
          "WorkflowEngine.EX_ERR_ACTION_NOT_FOUND_IN_MODEL"); //$NON-NLS-1$
    }
    request.setAttribute("Action", action);
    request.setAttribute("FormNames", workflowDesignerSC.retrieveFormNames(true));
    request.setAttribute("KindValues", workflowDesignerSC.retrieveActionKindCodes());

    return ROOT_URL + "editAction.jsp";
  };

  /**
   * Handles the "UpdateAction" function
   */
  private static final FunctionHandler hndlUpdateAction = (function, workflowDesignerSC, request) -> {
    Action action = workflowDesignerSC.createAction();

    action.setName(request.getParameter("name"));

    Forms forms = workflowDesignerSC.getProcessModel().getForms();
    if (forms != null) {
      action.setForm(forms.getForm(request.getParameter("form")));
    }
    action.setKind(request.getParameter("kind"));

    workflowDesignerSC.updateAction(action, request.getParameter(NAME_ORIGINAL));

    request.setAttribute(REDIRECT_TO, "ViewActions");
    return REDIRECTION_URL;
  };

  /**
   * Handles the "RemoveAction" function
   */
  private static final FunctionHandler hndlRemoveAction = (function, workflowDesignerSC, request) -> {
    String action = URLDecoder.decode(request.getParameter("action"), Charsets.UTF_8);
    workflowDesignerSC.removeAction(action);

    request.setAttribute(REDIRECT_TO, "ViewActions");
    return REDIRECTION_URL;
  };

  /**
   * Handles the "AddConsequence" and "ModifyConsequence" function
   */
  private static final FunctionHandler hndlEditConsequence = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter(CONTEXT);
    int iConsequence;
    Action action = workflowDesignerSC.findAction(strContext);
    Consequences consequences = action.getConsequences();
    Consequence consequence;

    // We do not have to check if the action != null,
    // unsuccessful action lookup result in an exception anyway
    //

    if ("AddConsequence".equals(function)) {
      if (consequences == null) {
        consequences = action.createConsequences();
      }

      consequence = consequences.createConsequence();
      consequence.setItem(WorkflowDesignerSessionController.NEW_ELEMENT_NAME);
      iConsequence = consequences.getConsequenceList().size();
      strContext = strContext + "/consequences/" + iConsequence;
      request.setAttribute("IsExisitingConsequence", Boolean.FALSE);
    } else // ModifyConsequence
    {
      consequence = workflowDesignerSC.findConsequence(strContext);
      request.setAttribute("IsExisitingConsequence", Boolean.TRUE);
    }

    if (consequence == null) {
      throw new WorkflowDesignerException("WorkflowDesignerRequestRouter.hndlEditConsequence",
          //$NON-NLS-1$
          SilverpeasException.ERROR, "workflowDesigner.EX_CONSEQUENCE_NOT_FOUND"); //$NON-NLS-1$
    }
    request.setAttribute("Consequence", consequence);
    request.setAttribute(CONTEXT, strContext);
    request.setAttribute(PARENT_SCREEN, calculateParentScreen(request, workflowDesignerSC,
        strContext));
    request.setAttribute("StateNames", workflowDesignerSC.retrieveStateNames(false));
    request.setAttribute("FolderItemNames",
        workflowDesignerSC.retrieveFolderItemNames(true, false));
    request.setAttribute("Operators", workflowDesignerSC.retrieveOperators(true));

    return ROOT_URL + "editConsequence.jsp";
  };

  private static final String VALUE = "value";
  /**
   * Handles the "UpdateConsequence" function
   */
  private static final FunctionHandler hndlUpdateConsequence =
      (function, workflowDesignerSC, request) -> {
        String strContext = request.getParameter(CONTEXT);
        String strItem = request.getParameter("item");
        String strKill = request.getParameter("kill");
        String strSetUnset;
        Action action = workflowDesignerSC.findAction(strContext);
        Consequences consequences = action.getConsequences();
        Consequence consequence;
        Iterator<State> iterState =
            workflowDesignerSC.getProcessModel().getStatesEx().iterateState();
        State state;

        if (consequences == null) {
          consequence = action.createConsequences().createConsequence();
        } else {
          consequence = action.getConsequences().createConsequence();
        }

        consequence.setKill(StringUtil.isDefined(strKill));

        if (StringUtil.isDefined(strItem)) {
          consequence.setItem(strItem);
          consequence.setOperator(request.getParameter("operator"));
          consequence.setValue(request.getParameter(VALUE));
        }

        // Set / Unset States
        //
        while (iterState.hasNext()) {
          state = iterState.next();

          strSetUnset = request.getParameter("setUnset_" + state.getName());

          if (StringUtil.isDefined(strSetUnset)) {
            StateSetter stateSetter = new StateRef();
            stateSetter.setState(state);

            if ("set".equals(strSetUnset)) {
              consequence.addTargetState(stateSetter);
            } else if ("unset".equals(strSetUnset)) {
              consequence.addUnsetState(stateSetter);
            }
          }

        }

        workflowDesignerSC.updateConsequence(consequence, strContext);

        request.setAttribute(REDIRECT_TO, calculateParentScreen(request, workflowDesignerSC,
            strContext));
        return REDIRECTION_URL;
      };

  /**
   * Handles the "MoveConsequence" function
   */
  private static final FunctionHandler hndlMoveConsequence = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter(CONTEXT);
    int nDirection = Integer.parseInt(request.getParameter("direction"));
    int iConsequence = Integer.parseInt(request.getParameter("consequenceNo"));

    workflowDesignerSC.moveConsequence(strContext, iConsequence, nDirection);

    request.setAttribute(REDIRECT_TO, calculateParentScreen(request, workflowDesignerSC,
        strContext));
    return REDIRECTION_URL;
  };

  /**
   * Handles the "RemoveConsequence" function
   */
  private static final FunctionHandler hndlRemoveConsequence =
      (function, workflowDesignerSC, request) -> {
        String strContext =URLDecoder.decode(request.getParameter(CONTEXT), Charsets.UTF_8);

        workflowDesignerSC.removeConsequence(strContext);

        request.setAttribute(REDIRECT_TO, calculateParentScreen(request, workflowDesignerSC,
            strContext));
        return REDIRECTION_URL;
      };

  /**
   * Handles the "ViewUserInfos" function
   */
  private static final FunctionHandler hndlViewUserInfos = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Items", workflowDesignerSC.getProcessModel().getUserInfos());
    request.setAttribute(CONTEXT, WorkflowDesignerSessionController.USER_INFOS);

    return ROOT_URL + "userInfos.jsp";
  };

  /**
   * Handles the "ViewDataFolder" function
   */
  private static final FunctionHandler hndlViewDataFolder = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Items", workflowDesignerSC.getProcessModel().getDataFolder());
    request.setAttribute(CONTEXT, WorkflowDesignerSessionController.DATA_FOLDER);
    return ROOT_URL + "dataFolder.jsp";
  };

  /**
   * Handles the "ViewForms" function
   */
  private static final FunctionHandler hndlViewForms = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Forms", workflowDesignerSC.getProcessModel().getForms());
    return ROOT_URL + "forms.jsp";
  };

  /**
   * Handles the "AddForm" and "ModifyForm" function
   */
  private static final FunctionHandler hndlEditForm = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter(CONTEXT);
    String strFormType = WorkflowDesignerSessionController.FORM_TYPE_ACTION;
    Form form;

    if ("AddForm".equals(function)) {
      form = workflowDesignerSC.createForm();
      strContext = WorkflowDesignerSessionController.FORMS + "[" + form.getName() + "," +
          (form.getRole() == null ? "" : form.getRole()) + "]";
      request.setAttribute("IsExisitingForm", Boolean.FALSE);
    } else // ModifyForm
    {
      form = workflowDesignerSC.findForm(strContext);
      request.setAttribute("IsExisitingForm", Boolean.TRUE);

      // Check the type of the form
      //
      if (WorkflowDesignerSessionController.FORM_TYPE_PRESENTATION.equals(form.getName()) ||
          WorkflowDesignerSessionController.FORM_TYPE_PRINT.equals(form.getName())) {
        strFormType = form.getName();
      }
    }

    request.setAttribute("Form", form);
    request.setAttribute(CONTEXT, strContext);
    request.setAttribute("type", strFormType);
    request.setAttribute("RoleNames", workflowDesignerSC.retrieveRoleNames(true, false));

    return ROOT_URL + "editForm.jsp";
  };

  /**
   * Handles the "UpdateForm" function
   */
  private static final FunctionHandler hndlUpdateForm = (function, workflowDesignerSC, request) -> {
    Form form = workflowDesignerSC.createForm();
    String strHTMLFileName = request.getParameter("HTMLFileName");
    String strRole = request.getParameter("role");
    String strRoleOriginal = request.getParameter("role_original");
    String strFormType = request.getParameter("type");

    form.setName(request.getParameter("name"));

    if (!StringUtil.isDefined(strHTMLFileName)) {
      strHTMLFileName = null;
    }

    if (!StringUtil.isDefined(strRole)) {
      strRole = null;
    }

    if (!StringUtil.isDefined(strRoleOriginal)) {
      strRoleOriginal = null;
    }

    form.setHTMLFileName(strHTMLFileName);
    if (WorkflowDesignerSessionController.FORM_TYPE_ACTION.equals(strFormType)) {
      // ignore role
      form.setRole(null);
    } else if (WorkflowDesignerSessionController.FORM_TYPE_PRESENTATION.equals(strFormType)) {
      form.setRole(strRole);
    } else if (WorkflowDesignerSessionController.FORM_TYPE_PRINT.equals(strFormType)) {
      // ignore role
      form.setRole(null);
    }

    workflowDesignerSC.updateForm(form, request.getParameter(CONTEXT),
        request.getParameter(NAME_ORIGINAL), strRoleOriginal);

    request.setAttribute(REDIRECT_TO, "ViewForms");
    return REDIRECTION_URL;
  };

  /**
   * Handles the "RemoveForm" function
   */
  private static final FunctionHandler hndlRemoveForm = (function, workflowDesignerSC, request) -> {
    String context = URLDecoder.decode(request.getParameter(CONTEXT));
    workflowDesignerSC.removeForm(context);

    request.setAttribute(REDIRECT_TO, "ViewForms");
    return REDIRECTION_URL;
  };

  /**
   * Handles the "AddInput" and "ModifyInput" function
   */
  private static final FunctionHandler hndlEditInput = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter(CONTEXT);
    int iInput;
    Form form = workflowDesignerSC.findForm(strContext);
    Input input;

    // Check if the parent object can be found
    //
    if (form == null) {
      throw new WorkflowDesignerException("WorkflowDesignerRequestRouter.hndlEditInput",
          //$NON-NLS-1$
          SilverpeasException.ERROR, "workflowDesigner.EX_PARENT_NOT_FOUND"); //$NON-NLS-1$
    }
    if ("AddInput".equals(function)) {
      input = form.createInput();
      input.setValue(WorkflowDesignerSessionController.NEW_ELEMENT_NAME);
      iInput = form.getInputs().length;
      strContext = strContext + "/inputs[" + iInput + "]";
      request.setAttribute("IsExisitingInput", Boolean.FALSE);
    } else // ModifyInput
    {
      input = workflowDesignerSC.findInput(strContext);
      request.setAttribute("IsExisitingInput", Boolean.TRUE);
    }

    if (input == null) {
      throw new WorkflowDesignerException("WorkflowDesignerRequestRouter.hndlEditInput",
          //$NON-NLS-1$
          SilverpeasException.ERROR, "workflowDesigner.EX_INPUT_NOT_FOUND"); //$NON-NLS-1$
      }
      request.setAttribute("Input", input);
      request.setAttribute(CONTEXT, strContext);
      request.setAttribute(PARENT_SCREEN, calculateParentScreen(request,
          workflowDesignerSC, strContext));
      request.setAttribute("TypesAndDisplayers", workflowDesignerSC.retrieveTypesAndDisplayers());
      request.setAttribute("FolderItems", workflowDesignerSC.retrieveFolderItems());

      return ROOT_URL + "editInput.jsp";
  };

  /**
   * Handles the "UpdateInput" function
   */
  private static final FunctionHandler hndlUpdateInput = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter(CONTEXT);
    String strItem = request.getParameter("item");
    String strValue = request.getParameter(VALUE);
    String strDisplayer = request.getParameter("displayerName");
    Form form = workflowDesignerSC.findForm(strContext);
    Input input;

    // Check if the parent object can be found
    //
    if (form == null) {
      throw new WorkflowDesignerException("WorkflowDesignerRequestRouter.hndlUpdateInput",
          //$NON-NLS-1$
          SilverpeasException.ERROR, "workflowDesigner.EX_PARENT_NOT_FOUND"); //$NON-NLS-1$
    }
    input = form.createInput();

    input.setMandatory(StringUtil.isDefined(request.getParameter("mandatory")));

    input.setReadonly(StringUtil.isDefined(request.getParameter("readonly")));

    if (StringUtil.isDefined(strItem)) {
      input.setItem(workflowDesignerSC.getProcessModel().getDataFolder().getItem(strItem));
    }

    if (StringUtil.isDefined(strValue)) {
      input.setValue(strValue);
    }

    if (StringUtil.isDefined(strDisplayer)) {
      input.setDisplayerName(strDisplayer);
    }

    workflowDesignerSC.updateInput(input, strContext);

    request.setAttribute(REDIRECT_TO, calculateParentScreen(request, workflowDesignerSC,
        strContext));
    return REDIRECTION_URL;
  };

  /**
   * Handles the "RemoveInput" function
   */
  private static final FunctionHandler hndlRemoveInput = (function, workflowDesignerSC, request) -> {
    String strContext = URLDecoder.decode(request.getParameter(CONTEXT), Charsets.UTF_8);
    workflowDesignerSC.removeInput(strContext);

    request.setAttribute(REDIRECT_TO, calculateParentScreen(request, workflowDesignerSC,
        strContext));
    return REDIRECTION_URL;
  };

  /**
   * Handles the "AddRelatedUser" and "ModifyRelatedUser" functions
   */
  private static final FunctionHandler hndlEditRelatedUser = (function, workflowDesignerSC, request) -> {
    RelatedUser relatedUser;
    String strContext = request.getParameter(CONTEXT);
    String strParticipant = request.getParameter("participant");
    String strFolderItem = request.getParameter("folderItem");
    String strRelation = request.getParameter("relation");
    String strRole = request.getParameter("role");

    if ("AddRelatedUser".equals(function)) {
      relatedUser = workflowDesignerSC.getProcessModel().createRelatedUser();
      relatedUser.setRole(WorkflowDesignerSessionController.NEW_ELEMENT_NAME);
    } else // ModifyRelatedUser
    {

      if (!StringUtil.isDefined(strParticipant)) {
        strParticipant = null;
      }
      if (!StringUtil.isDefined(strFolderItem)) {
        strFolderItem = null;
      }
      if (!StringUtil.isDefined(strRelation)) {
        strRelation = null;
      }
      if (!StringUtil.isDefined(strRole)) {
        strRole = null;
      }

      relatedUser =
          workflowDesignerSC.findRelatedUser(strContext, strParticipant, strFolderItem, strRelation,
              strRole);
    }

    if (relatedUser == null) {
      throw new WorkflowException("WorkflowDesignerRequestRouter.hndlEditRelatedUser",
          //$NON-NLS-1$
          SilverpeasException.ERROR, "workflowEngine.EX_RELATED_USER_NOT_FOUND"); //$NON-NLS-1$
    }
    request.setAttribute("RoleNames", workflowDesignerSC.retrieveRoleNames(true, false));
    request.setAttribute("ParticipantNames", workflowDesignerSC.retrieveParticipantNames(true));
    request.setAttribute("FolderItemNames", workflowDesignerSC.retrieveFolderItemNames(true, true));
    request.setAttribute("UserInfoNames", workflowDesignerSC.retrieveUserInfoItemNames(true, true));
    request.setAttribute("RelatedUser", relatedUser);
    request.setAttribute(CONTEXT, strContext);
    request.setAttribute(PARENT_SCREEN, calculateParentScreen(request, workflowDesignerSC,
        strContext));

    return ROOT_URL + "editRelatedUser.jsp";
  };

  /**
   * Handles the "UpdateRelatedUser" function
   */
  private static final FunctionHandler hndlUpdateRelatedUser =
      (function, workflowDesignerSC, request) -> {
        String strParticipantOriginal = request.getParameter("participant_original");
        String strFolderItemOriginal = request.getParameter("folderItem_original");
        String strRelationOriginal = request.getParameter("relation_original");
        String strRoleOriginal = request.getParameter("role_original");
        String strParticipant = request.getParameter("participant");
        String strFolderItem = request.getParameter("folderItem");
        String strRelation = request.getParameter("relation");
        String strRole = request.getParameter("role");
        String strContext = request.getParameter(CONTEXT);
        RelatedUser relatedUser = workflowDesignerSC.getProcessModel().createRelatedUser();

        if (!StringUtil.isDefined(strParticipantOriginal)) {
          strParticipantOriginal = null;
        }
        if (!StringUtil.isDefined(strFolderItemOriginal)) {
          strFolderItemOriginal = null;
        }
        if (!StringUtil.isDefined(strRelationOriginal)) {
          strRelationOriginal = null;
        }
        if (!StringUtil.isDefined(strRoleOriginal)) {
          strRoleOriginal = null;
        }

        if (StringUtil.isDefined(strParticipant)) {
          relatedUser.setParticipant(workflowDesignerSC.getProcessModel()
              .getParticipantsEx()
              .getParticipant(strParticipant));
        } else {
          relatedUser.setParticipant(null);
        }

        if (StringUtil.isDefined(strFolderItem)) {
          relatedUser.setFolderItem(
              workflowDesignerSC.getProcessModel().getDataFolder().getItem(strFolderItem));
        } else {
          relatedUser.setFolderItem(null);
        }

        if (!StringUtil.isDefined(strRelation)) {
          strRelation = null;
        }
        if (!StringUtil.isDefined(strRole)) {
          strRole = null;
        }

        relatedUser.setRelation(strRelation);
        relatedUser.setRole(strRole);

        workflowDesignerSC.updateRelatedUser(relatedUser, strContext, strParticipantOriginal,
            strFolderItemOriginal, strRelationOriginal, strRoleOriginal);
        request.setAttribute(REDIRECT_TO, calculateParentScreen(request, workflowDesignerSC,
            strContext));
        return REDIRECTION_URL;
      };

  /**
   * Handles the "RemoveRelatedUser" function
   */
  private static final FunctionHandler hndlRemoveRelatedUser =
      (function, workflowDesignerSC, request) -> {
        RelatedUser relatedUser = workflowDesignerSC.getProcessModel().createRelatedUser();
        String strContext = URLDecoder.decode(request.getParameter(CONTEXT), Charsets.UTF_8);
        String strParticipant = request.getParameter("participant");
        String strFolderItem = request.getParameter("folderItem");
        String strRelation = request.getParameter("relation");
        String strRole = request.getParameter("role");

        if (StringUtil.isDefined(strParticipant)) {
          relatedUser.setParticipant(workflowDesignerSC.getProcessModel()
              .getParticipantsEx()
              .getParticipant(URLDecoder.decode(strParticipant, Charsets.UTF_8)));
        } else {
          relatedUser.setParticipant(null);
        }

        if (StringUtil.isDefined(strFolderItem)) {
          relatedUser.setFolderItem(
              workflowDesignerSC.getProcessModel()
                  .getDataFolder().getItem(URLDecoder.decode(strFolderItem, Charsets.UTF_8)));
        } else {
          relatedUser.setFolderItem(null);
        }

        if (!StringUtil.isDefined(strRelation)) {
          strRelation = null;
        } else  {
          strRelation = URLDecoder.decode(strRelation, Charsets.UTF_8);
        }
        if (!StringUtil.isDefined(strRole)) {
          strRole = null;
        } else {
          strRole = URLDecoder.decode(strRole, Charsets.UTF_8);
        }

        relatedUser.setRelation(strRelation);
        relatedUser.setRole(strRole);

        // remove the relatedUser concerned
        //
        workflowDesignerSC.removeRelatedUser(relatedUser, strContext);

        request.setAttribute(REDIRECT_TO, calculateParentScreen(request, workflowDesignerSC,
            strContext));
        return REDIRECTION_URL;
      };

  /**
   * Handles the "AddContextualDesignation" and "ModifyContextualDesignation" functions
   */
  private static final FunctionHandler hndlEditContextualDesignation =
      (function, workflowDesignerSC, request) -> {
        ContextualDesignation designation;
        String strContext = request.getParameter(CONTEXT);

        if ("AddContextualDesignation".equals(function)) {
          designation = workflowDesignerSC.createDesignation();
        } else // ModifyContextualDesignation
        {
          designation =
              workflowDesignerSC.findContextualDesignation(strContext, request.getParameter("role"),
                  request.getParameter("lang"));
        }

        if (designation == null) {
          throw new WorkflowDesignerException(
              "WorkflowDesignerRequestRouter.hndlEditContextualDesignation", //$NON-NLS-1$
              SilverpeasException.ERROR,
              "workflowDesigner.EX_CONTEXTUAL_DESIGNATION_NOT_FOUND"); //$NON-NLS-1$
        }
        request.setAttribute("RoleNames", workflowDesignerSC.retrieveRoleNames(false, true));
        request.setAttribute("LanguageNames", workflowDesignerSC.retrieveLanguageNames(true));
        request.setAttribute("LanguageCodes", workflowDesignerSC.retrieveLanguageCodes(true));
        request.setAttribute("ContextualDesignation", designation);
        request.setAttribute(CONTEXT, strContext);
        request.setAttribute(PARENT_SCREEN, request.getParameter(PARENT_SCREEN));
        request.setAttribute("EditorName", calculateEditorName(strContext));

        return ROOT_URL + "editContextualDesignation.jsp";
      };

  /**
   * Handles the "UpdateContextualDesignation" function
   */
  private static final FunctionHandler hndlUpdateContextualDesignation =
      (function, workflowDesignerSC, request) -> {
        String strLanguage = request.getParameter("lang_original");
        String strRole = request.getParameter("role_original");
        String strContext = request.getParameter(CONTEXT);
        String strParentScreen = request.getParameter(PARENT_SCREEN);
        ContextualDesignation designation = new SpecificLabel();

        designation.setLanguage(request.getParameter("lang"));
        designation.setRole(request.getParameter("role"));
        designation.setContent(request.getParameter("content"));

        workflowDesignerSC.updateContextualDesignations(strContext, designation, strLanguage,
            strRole);

        request.setAttribute(REDIRECT_TO, applySessionToken(request, strParentScreen));
        return REDIRECTION_URL;
      };

  /**
   * Handles the "RemoveContextualDesignation" function
   */
  private static final FunctionHandler hndlRemoveContextualDesignation =
      (function, workflowDesignerSC, request) -> {
        ContextualDesignation contextualDesignation = new SpecificLabel();
        String strContext = URLDecoder.decode(request.getParameter(CONTEXT), Charsets.UTF_8);
        String strParentScreen = URLDecoder.decode(request.getParameter(PARENT_SCREEN), Charsets.UTF_8);

        String role = request.getParameter("role");
        if (StringUtil.isDefined(role)) {
          role = URLDecoder.decode(role, Charsets.UTF_8);
        }

        String language = request.getParameter("lang");
        if (StringUtil.isDefined(language)) {
          language = URLDecoder.decode(language, Charsets.UTF_8);
        }
        contextualDesignation.setRole(role);
        contextualDesignation.setLanguage(language);

        // remove the contextual designation concerned
        //
        workflowDesignerSC.removeContextualDesignation(strContext, contextualDesignation);

        request.setAttribute(REDIRECT_TO, applySessionToken(request, strParentScreen));
        return REDIRECTION_URL;
      };

  /**
   * Handles the "AddItem" and "ModifyItem" function
   */
  private static final FunctionHandler hndlEditItem = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter(CONTEXT);
    Item item;

    if ("AddItem".equals(function)) {
      item = workflowDesignerSC.createItem(strContext);
      strContext += "/" + item.getName();
      request.setAttribute("IsExisitingItem", Boolean.FALSE);
    } else // ModifyItem
    {
      item = workflowDesignerSC.findItem(strContext);
      request.setAttribute("IsExisitingItem", Boolean.TRUE);
    }

    if (item == null) {
      throw new WorkflowException("WorkflowDesignerRequestRouter.hndlEditItem", //$NON-NLS-1$
          SilverpeasException.ERROR, "workflowEngine.EX_ITEM_NOT_FOUND"); //$NON-NLS-1$
    }
    request.setAttribute("Item", item);
    request.setAttribute(CONTEXT, strContext);
    request.setAttribute(PARENT_SCREEN, calculateParentScreen(request, workflowDesignerSC,
        strContext));
    request.setAttribute("TypeValues", workflowDesignerSC.retrieveItemTypeCodes(true));
    request.setAttribute("UserInfosNames",
        workflowDesignerSC.retrieveUserInfoItemNames(true, false));

    return ROOT_URL + "editItem.jsp";
  };

  /**
   * Handles the "UpdateItem" function
   */
  private static final FunctionHandler hndlUpdateItem = (function, workflowDesignerSC, request) -> {
    String strNameOriginal = request.getParameter(NAME_ORIGINAL);
    String strName = request.getParameter("name");
    String strComupted = request.getParameter("computed");
    String strMapTo = request.getParameter("mapTo");
    String strType = request.getParameter("type");
    String strReadonly = request.getParameter("readonly");
    String strFormula = request.getParameter("formula");
    String strContext = request.getParameter(CONTEXT);
    Item item = workflowDesignerSC.createItem(strContext);

    if (!StringUtil.isDefined(strMapTo)) {
      strMapTo = null;
    }
    if (!StringUtil.isDefined(strType)) {
      strType = null;
    }
    if (!StringUtil.isDefined(strName)) {
      strName = null;
    }
    if (!StringUtil.isDefined(strNameOriginal)) {
      strNameOriginal = null;
    }
    if (!StringUtil.isDefined(strFormula)) {
      strFormula = null;
    }

    item.setComputed(StringUtil.isDefined(strComupted));
    item.setReadonly(StringUtil.isDefined(strReadonly));

    item.setName(strName);
    item.setFormula(strFormula);
    item.setMapTo(strMapTo);
    item.setType(strType);

    workflowDesignerSC.updateItem(item, strContext, strNameOriginal);
    request.setAttribute(REDIRECT_TO, calculateParentScreen(request, workflowDesignerSC,
        strContext));
    return REDIRECTION_URL;
  };

  /**
   * Handles the "RemoveItem" function
   */
  private static final FunctionHandler hndlRemoveItem = (function, workflowDesignerSC, request) -> {
    String strContext = URLDecoder.decode(request.getParameter(CONTEXT), Charsets.UTF_8);
    workflowDesignerSC.removeItem(strContext);

    request.setAttribute(REDIRECT_TO, calculateParentScreen(request, workflowDesignerSC,
        strContext));
    return REDIRECTION_URL;
  };

  /**
   * Handles the "AddParameter" and "ModifyParameter" function
   */
  private static final FunctionHandler hndlEditParameter = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter(CONTEXT);
    String strName = request.getParameter("name");
    Item item = workflowDesignerSC.findItem(strContext);
    Parameter parameter;

    // Check if the parent object can be found
    //
    if (item == null) {
      throw new WorkflowDesignerException("WorkflowDesignerRequestRouter.hndlEditParameter",
          //$NON-NLS-1$
          SilverpeasException.ERROR, "workflowDesigner.EX_PARENT_NOT_FOUND"); //$NON-NLS-1$
    }
    if ("AddParameter".equals(function)) {
      parameter = item.createParameter();
      parameter.setName(WorkflowDesignerSessionController.NEW_ELEMENT_NAME);
    } else // ModifyParameter
    {
      parameter = item.getParameter(strName);
    }

    if (parameter == null) {
      throw new WorkflowException("WorkflowDesignerRequestRouter.hndlEditParameter", //$NON-NLS-1$
          SilverpeasException.ERROR, "workflowEngine.EX_PARAMETER_NOT_FOUND"); //$NON-NLS-1$
    }
    request.setAttribute("Parameter", parameter);
    request.setAttribute(CONTEXT, strContext);
    request.setAttribute(PARENT_SCREEN, calculateParentScreen(request, workflowDesignerSC,
        strContext));

    return ROOT_URL + "editParameter.jsp";
  };

  /**
   * Handles the "UpdateParameter" function
   */
  private static final FunctionHandler hndlUpdateParameter = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter(CONTEXT);
    String strName = request.getParameter("name");
    String strValue = request.getParameter(VALUE);
    String strNameOriginal = request.getParameter(NAME_ORIGINAL);
    Item item = workflowDesignerSC.findItem(strContext);
    Parameter parameter;

    parameter = item.createParameter();

    if (StringUtil.isDefined(strName)) {
      parameter.setName(strName);
    } else {
      parameter.setName("");
    }

    if (StringUtil.isDefined(strValue)) {
      parameter.setValue(strValue);
    } else {
      parameter.setValue("");
    }

    workflowDesignerSC.updateParameter(parameter, strContext, strNameOriginal);

    request.setAttribute(REDIRECT_TO, calculateParentScreen(request, workflowDesignerSC,
        strContext));
    return REDIRECTION_URL;
  };

  /**
   * Handles the "RemoveParameter" function
   */
  private static final FunctionHandler hndlRemoveParameter = (function, workflowDesignerSC, request) -> {
    String strContext = URLDecoder.decode(request.getParameter(CONTEXT), Charsets.UTF_8);
    String strName = URLDecoder.decode(request.getParameter("name"), Charsets.UTF_8);

    workflowDesignerSC.removeParameter(strContext, strName);

    request.setAttribute(REDIRECT_TO, calculateParentScreen(request, workflowDesignerSC,
        strContext));
    return REDIRECTION_URL;
  };

  /**
   * Calculate the name for an editor based on the context
   * @param strContext the context
   * @return resource key to retrieve the name
   */
  private static String calculateEditorName(String strContext) {
    int iSlash = strContext.lastIndexOf('/');
    String strEditor;

    if (iSlash >= 0) {
      strEditor = strContext.substring(iSlash + 1);
    } else {
      strEditor = strContext;
    }

    switch (strEditor) {
      case WorkflowDesignerSessionController.LABELS:
        return "workflowDesigner.editor.label";
      case WorkflowDesignerSessionController.DESCRIPTIONS:
        return "workflowDesigner.editor.description";
      case WorkflowDesignerSessionController.TITLES:
        return "workflowDesigner.editor.title";
      case WorkflowDesignerSessionController.ACTIVITIES:
        return "workflowDesigner.editor.activity";
      case WorkflowDesignerSessionController.WORKING_USERS:
        return "workflowDesigner.editor.workingUsers";
      case WorkflowDesignerSessionController.INTERESTED_USERS:
        return "workflowDesigner.editor.interestedUsers";
      case WorkflowDesignerSessionController.ALLOWED_USERS:
        return "workflowDesigner.editor.allowedUsers";
      case WorkflowDesignerSessionController.NOTIFIED_USERS:
        return "workflowDesigner.editor.notifiedUsers";
      default:
        return null;
    }
  }

  /**
   * Calculate the URL to the parent screen based on the current context
   * @param strContext the context
   * @param workflowDesignerSC session controller
   * @return the relative URL
   * @throws WorkflowException if the computation fails
   */
  private static String calculateParentScreen(HttpServletRequest request,
      WorkflowDesignerSessionController workflowDesignerSC,
      String strContext) throws WorkflowException {
    if (strContext == null) {
      return "";
    }

    var strtok = new StringTokenizer(strContext, "/[]");
    String strParentScreen = "";
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("?context=");

      String strElement = strtok.nextToken();
      if (WorkflowDesignerSessionController.LABELS.equals(strElement)
          || WorkflowDesignerSessionController.DESCRIPTIONS.equals(strElement)) {
        strParentScreen = "ModifyWorkflow";
      } else if (WorkflowDesignerSessionController.ROLES.equals(strElement)) {
        strParentScreen = "ModifyRole?role=" + strtok.nextToken();
      } else if (WorkflowDesignerSessionController.PRESENTATION.equals(strElement)) {
        strParentScreen = "ModifyColumns?columns=" + strtok.nextToken();
      } else if (WorkflowDesignerSessionController.STATES.equals(strElement)) {
        strParentScreen = parentScreenForStates(workflowDesignerSC, strtok, sb);
      } else if (WorkflowDesignerSessionController.ACTIONS.equals(strElement)) {
        strParentScreen = parentScreenForActions(workflowDesignerSC, strtok, sb);
      } else if (WorkflowDesignerSessionController.DATA_FOLDER.equals(strElement)) {
        strParentScreen = parentScreenForDataFolder(strtok, sb);
      } else if (WorkflowDesignerSessionController.USER_INFOS.equals(strElement)) {
        strParentScreen = parentScreenForUserInfo(strtok, sb);
      } else if (WorkflowDesignerSessionController.FORMS.equals(strElement)) {
        strParentScreen = parentScreenForForms(strtok, sb);
      }
    } catch (NoSuchElementException e) {
      // Thrown when no token was found where expected
      // do nothing, just return null...
      SilverLogger.getLogger(WorkflowDesignerRequestRouter.class).error(e.getMessage());
    }
    return applySessionToken(request, strParentScreen);
  }

  private static String parentScreenForUserInfo(StringTokenizer strtok, StringBuilder sb) {
    String strParentScreen = "ViewUserInfos";

    sb.append(WorkflowDesignerSessionController.USER_INFOS);
    sb.append("/");
    sb.append(strtok.nextToken()); // item name

    if (strtok.hasMoreTokens() &&
        WorkflowDesignerSessionController.PARAMETERS.equals(strtok.nextToken())) {
      strParentScreen = "ModifyItem" + sb;
    }
    return strParentScreen;
  }

  private static String parentScreenForDataFolder(StringTokenizer strtok, StringBuilder sb) {
    String strParentScreen = "ViewDataFolder";

    sb.append(WorkflowDesignerSessionController.DATA_FOLDER);
    sb.append("/");
    sb.append(strtok.nextToken()); // item name

    if (strtok.hasMoreTokens() &&
        WorkflowDesignerSessionController.PARAMETERS.equals(strtok.nextToken())) {
      strParentScreen = "ModifyItem" + sb;
    }
    return strParentScreen;
  }

  private static String parentScreenForStates(WorkflowDesignerSessionController workflowDesignerSC,
      StringTokenizer strtok, StringBuilder sb) {
    String nextStrElement = strtok.nextToken();
    State state = workflowDesignerSC.getProcessModel().getStatesEx().getState(nextStrElement);
    nextStrElement = strtok.nextToken();

    sb.append(WorkflowDesignerSessionController.STATES);
    sb.append("/");
    sb.append(state.getName());

    String strParentScreen = "ModifyState?state=" + state.getName();

    if (WorkflowDesignerSessionController.WORKING_USERS.equals(nextStrElement)) {
      if (WorkflowDesignerSessionController.RELATED_USER.equals(strtok
          .nextToken())) {
        // is a 'relatedUser'
        //
        sb.append("/");
        sb.append(WorkflowDesignerSessionController.WORKING_USERS);
        strParentScreen = "ModifyQualifiedUsers" + sb;
      }
    } else if (WorkflowDesignerSessionController.INTERESTED_USERS
        .equals(nextStrElement) && WorkflowDesignerSessionController.RELATED_USER.equals(strtok
        .nextToken())) {
      // is a 'relatedUser'
      //
      sb.append("/");
      sb.append(WorkflowDesignerSessionController.INTERESTED_USERS);
      strParentScreen = "ModifyQualifiedUsers" + sb;
    }
    return strParentScreen;
  }

  private static String parentScreenForActions(WorkflowDesignerSessionController workflowDesignerSC,
      StringTokenizer strtok, StringBuilder sb) throws WorkflowException {
    Action action;
    String strElement = strtok.nextToken();
    action = workflowDesignerSC.getProcessModel().getActionsEx().getAction(strElement);
    strElement = strtok.nextToken(); // allowedUsers or consequences

    sb.append(WorkflowDesignerSessionController.ACTIONS);
    sb.append("/");
    sb.append(action.getName());

    String strParentScreen = "ModifyAction?action=" + action.getName();

    if (WorkflowDesignerSessionController.ALLOWED_USERS.equals(strElement)) {
      // is a 'relatedUser'
      //
      if (strtok.hasMoreTokens() &&
          WorkflowDesignerSessionController.RELATED_USER.equals(strtok.nextToken())) {
        sb.append("/");
        sb.append(WorkflowDesignerSessionController.ALLOWED_USERS);
        strParentScreen = "ModifyQualifiedUsers" + sb;
      }
    } else if (WorkflowDesignerSessionController.CONSEQUENCES.equals(strElement)) {
      sb.append("/");
      sb.append(WorkflowDesignerSessionController.CONSEQUENCES);
      sb.append("/");
      sb.append(strtok.nextToken()); // consequence no.

      // notified users
      //
      if (strtok.hasMoreTokens() &&
          WorkflowDesignerSessionController.NOTIFIED_USERS.equals(strtok.nextToken())) {
        strParentScreen = "ModifyConsequence" + sb;
        // related users
        if (strtok.hasMoreTokens() &&
            WorkflowDesignerSessionController.RELATED_USER.equals(strtok.nextToken())) {
          sb.append("/");
          sb.append(WorkflowDesignerSessionController.NOTIFIED_USERS);

          strParentScreen = "ModifyQualifiedUsers" + sb;
        }
      }
    }
    return strParentScreen;
  }

  private static String parentScreenForForms(StringTokenizer strtok, StringBuilder sb) {
    sb.append(WorkflowDesignerSessionController.FORMS);
    sb.append("[");
    sb.append(strtok.nextToken()); // form & role name
    sb.append("]");
    String nextStrElement = strtok.nextToken();

    String strParentScreen = "";
    if (WorkflowDesignerSessionController.INPUTS.equals(nextStrElement)) {
      strParentScreen = "EditForm" + sb;

      sb.append("/");
      sb.append(WorkflowDesignerSessionController.INPUTS);
      sb.append("[");
      sb.append(strtok.nextToken()); // input no
      sb.append("]");

      if (strtok.hasMoreTokens()) {
        nextStrElement = strtok.nextToken();

        if (WorkflowDesignerSessionController.LABELS.equals(nextStrElement)) {
          strParentScreen = "ModifyInput" + sb;
        }
      }
    } else if (WorkflowDesignerSessionController.TITLES.equals(nextStrElement)) {
      strParentScreen = "EditForm" + sb;
    }
    return strParentScreen;
  }

  private static String applySessionToken(HttpServletRequest request, String strParentScreen) {
    var tokenService = SynchronizerTokenService.getInstance();
    var token = tokenService.getSessionToken(request);
    if (token.isDefined()) {
      return strParentScreen + (strParentScreen.contains("?") ? "&" : "?") +
          SynchronizerTokenService.SESSION_TOKEN_KEY + "=" + token.getValue();
    } else {
      return strParentScreen;
    }
  }
}
