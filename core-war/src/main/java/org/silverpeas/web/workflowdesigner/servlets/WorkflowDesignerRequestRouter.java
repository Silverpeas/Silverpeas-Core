/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.workflowdesigner.servlets;

import org.silverpeas.core.workflow.api.Workflow;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.*;
import org.silverpeas.core.workflow.engine.model.ProcessModelImpl;
import org.silverpeas.web.workflowdesigner.control.WorkflowDesignerSessionController;
import org.silverpeas.web.workflowdesigner.model.WorkflowDesignerException;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class WorkflowDesignerRequestRouter extends
    ComponentRequestRouter<WorkflowDesignerSessionController> {

  private static final long serialVersionUID = -6747786008527861783L;
  static private Map<String, FunctionHandler> mapHandler; // mapping of functions to their handlers
  static final private String root = "/workflowDesigner/jsp/"; // the root

  // directory for
  // all the *.jsp

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "WorkflowDesigner";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public WorkflowDesignerSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new WorkflowDesignerSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param workflowDesignerSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      WorkflowDesignerSessionController workflowDesignerSC, HttpRequest request) {
    String destination = null;
    FunctionHandler handler = getHandler(function);

    // Check access rights
    if (!workflowDesignerSC.getUserDetail().isAccessAdmin()) {
      return ResourceLocator.getGeneralSettingBundle().getString("accessForbidden");
    }

    try {
      if (handler != null) {
        destination = handler.getDestination(function, workflowDesignerSC, request);
      }

      if (destination == null) {
        request.setAttribute("redirectTo", "Main");
        destination = root + "redirect.jsp";
      }


    } catch (WorkflowDesignerException e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = root + "errorpageMain.jsp";
    } catch (WorkflowException e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = root + "errorpageMain.jsp";
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }


    return destination;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.GenericServlet#init()
   */
  public void init() throws ServletException {
    super.init();
    initHandlers();
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    initHandlers();
  }

  /**
   *
   */
  static private FunctionHandler getHandler(String function) {
    return mapHandler.get(function);
  }

  /**
   * Initialise the map of the function handlers
   */
  synchronized private void initHandlers() {
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
    mapHandler.put("ModifyForm", hndlEditForm);
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
  private static FunctionHandler hndlListWorkflow = (function, workflowDesignerSC, request) -> {
    Workflow.getProcessModelManager().clearProcessModelCache();
    request.setAttribute("ProcessFileNames", workflowDesignerSC.listProcessModels());
    return root + "welcome.jsp";
  };
  /**
   * Handles the "AddWorkflow", "EditWorkflow" and "ModifyWorkflow" functions
   */
  private static FunctionHandler hndlEditWorkflow = (function, workflowDesignerSC, request) -> {
    String strProcessFileName;
    ProcessModel processModel = null;

    if ("AddWorkflow".equals(function)) {
      processModel = workflowDesignerSC.createProcessModel();
      strProcessFileName = workflowDesignerSC.getProcessFileName();
    } else if ("EditWorkflow".equals(function)) {
      // Loading the process model into the session controller
      //
      strProcessFileName = request.getParameter("ProcessFileName");

      if (StringUtil.isDefined(strProcessFileName)) {
        processModel = workflowDesignerSC.loadProcessModel(strProcessFileName);
      }

      // redirect to change the function name and remove the parameter
      // "ProcessFileName"
      //
      request.setAttribute("redirectTo", "ModifyWorkflow");
      return root + "redirect.jsp";
    } else // ModifyWorkflow
    {
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
    return root + "workflow.jsp";
  };
  /**
   * Handles the "ImportWorkflow, DoImportWorkflow" functions
   */
  private static FunctionHandler hndlImportWorkflow = new FunctionHandler() {

    public String getDestination(String function,
        WorkflowDesignerSessionController workflowDesignerSC,
        HttpServletRequest request) throws WorkflowDesignerException,
        WorkflowException {
      if ("ImportWorkflow".equals(function)) {
        return root + "importWorkflow.jsp";
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
    }
  };
  /**
   * Handles the "UpdateWorkflow" function
   */
  private static FunctionHandler hndlUpdateWorkflow = (function, workflowDesignerSC, request) -> {
    String strProcessFileName = request.getParameter("ProcessFileName"), strProcessName =
        request.getParameter("name");

    // The parameters may not be defined if the action is launched from a
    // screen
    // other than 'workflow header'
    //
    if (!StringUtil.isDefined(strProcessFileName)) {
      strProcessFileName = null;
    }

    if (StringUtil.isDefined(strProcessName)) {
      ProcessModel processModel = new ProcessModelImpl();

      processModel.setName(strProcessName);
      workflowDesignerSC.updateProcessModelHeader(processModel);
    }

    workflowDesignerSC.saveProcessModel(strProcessFileName);

    request.setAttribute("redirectTo", "Main");
    return root + "redirect.jsp";
  };
  /**
   * Handles the "RemoveWorkflow" function
   */
  private static FunctionHandler hndlRemoveWorkflow = (function, workflowDesignerSC, request) -> {
    workflowDesignerSC.removeProcessModel(request.getParameter("ProcessFileName"));
    request.setAttribute("redirectTo", "Main");
    return root + "redirect.jsp";
  };

  /**
   * Handles the "GenerateComponentDescription" function and lists the workflows
   */
  private static FunctionHandler hndlGenerateComponentDescription =
      (function, workflowDesignerSC, request) -> {
        // Generate the component descriptor
        workflowDesignerSC.generateComponentDescriptor();

        request.setAttribute("redirectTo", "ModifyWorkflow");
        return root + "redirect.jsp";
      };
  /**
   * Handles the "ViewRoles" function
   */
  private static FunctionHandler hndlViewRoles = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Roles", workflowDesignerSC.getProcessModel().getRolesEx());

    return root + "roles.jsp";
  };
  /**
   * Handles the "AddRole" and "ModifyRole" function
   */
  private static FunctionHandler hndlEditRole = (function, workflowDesignerSC, request) -> {
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

    return root + "editRole.jsp";

  };
  /**
   * Handles the "UpdateRole" function
   */
  private static FunctionHandler hndlUpdateRole = (function, workflowDesignerSC, request) -> {
    Role role = workflowDesignerSC.createRole();

    role.setName(request.getParameter("name"));
    workflowDesignerSC.updateRole(role, request.getParameter("name_original"));

    request.setAttribute("redirectTo", "ViewRoles");
    return root + "redirect.jsp";
  };
  /**
   * Handles the "RemoveRole" function
   */
  private static FunctionHandler hndlRemoveRole = (function, workflowDesignerSC, request) -> {
    workflowDesignerSC.removeRole(request.getParameter("role"));

    request.setAttribute("redirectTo", "ViewRoles");
    return root + "redirect.jsp";
  };
  /**
   * Handles the "ViewPresentation" function
   */
  private static FunctionHandler hndlViewPresentation = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Presentation", workflowDesignerSC.getProcessModel().getPresentation());
    return root + "presentation.jsp";
  };
  /**
   * Handles the "AddColumns" and "ModifyColumns" functions
   */
  private static FunctionHandler hndlEditColumns = (function, workflowDesignerSC, request) -> {
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

    return root + "editColumns.jsp";
  };
  /**
   * Handles the "UpdateColumns" function
   */
  private static FunctionHandler hndlUpdateColumns = (function, workflowDesignerSC, request) -> {
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

    request.setAttribute("redirectTo", "ViewPresentation");
    return root + "redirect.jsp";
  };
  /**
   * Handles the "RemoveColumns" function
   */
  private static FunctionHandler hndlRemoveColumns = (function, workflowDesignerSC, request) -> {
    workflowDesignerSC.deleteColumns(request.getParameter("columns"));

    request.setAttribute("redirectTo", "ViewPresentation");
    return root + "redirect.jsp";
  };
  /**
   * Handles the "ViewParticipants" function
   */
  private static FunctionHandler hndlViewParticipants = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Participants", workflowDesignerSC.getProcessModel().getParticipantsEx());
    return root + "participants.jsp";
  };
  /**
   * Handles the "AddParticipant" and "ModifyParicipant" function
   */
  private static FunctionHandler hndlEditParticipant = (function, workflowDesignerSC, request) -> {
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

    return root + "editParticipant.jsp";
  };
  /**
   * Handles the "UpdateParticipant" function
   */
  private static FunctionHandler hndlUpdateParticipant =
      (function, workflowDesignerSC, request) -> {
        Participant participant = workflowDesignerSC.createParticipant();
        String strResolvedState = request.getParameter("resolvedState");

        if (!StringUtil.isDefined(strResolvedState)) {
          strResolvedState = null;
        }

        participant.setName(request.getParameter("name"));
        participant.setResolvedState(strResolvedState);
        workflowDesignerSC.updateParticipant(participant, request.getParameter("name_original"));

        request.setAttribute("redirectTo", "ViewParticipants");
        return root + "redirect.jsp";
      };
  /**
   * Handles the "RemoveParticipant" function
   */
  private static FunctionHandler hndlRemoveParticipant =
      (function, workflowDesignerSC, request) -> {
        workflowDesignerSC.removeParticipant(request.getParameter("participant"));

        request.setAttribute("redirectTo", "ViewParticipants");
        return root + "redirect.jsp";
      };
  /**
   * Handles the "ViewStates" function
   */
  private static FunctionHandler hndlViewStates = (function, workflowDesignerSC, request) -> {
    request.setAttribute("States", workflowDesignerSC.getProcessModel().getStatesEx());
    return root + "states.jsp";
  };
  /**
   * Handles the "AddState" and "ModifyState" function
   */
  private static FunctionHandler hndlEditState = (function, workflowDesignerSC, request) -> {
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

    return root + "editState.jsp";
  };
  /**
   * Handles the "UpdateState" function
   */
  private static FunctionHandler hndlUpdateState = (function, workflowDesignerSC, request) -> {
    State state = workflowDesignerSC.createState();
    String strTimeoutAction = request.getParameter("timeoutAction"), strTimeoutInterval =
        request.getParameter("timeoutInterval"), strNotifyAdmin =
        request.getParameter("notifyAdmin");
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

    workflowDesignerSC.updateState(state, request.getParameter("name_original"));

    request.setAttribute("redirectTo", "ViewStates");
    return root + "redirect.jsp";
  };
  /**
   * Handles the "RemoveState" function
   */
  private static FunctionHandler hndlRemoveState = (function, workflowDesignerSC, request) -> {
    workflowDesignerSC.removeState(request.getParameter("state"));

    request.setAttribute("redirectTo", "ViewStates");
    return root + "redirect.jsp";
  };
  /**
   * Handles the "AddQualifiedUsers" and "ModifyQualifiedUsers" function
   */
  private static FunctionHandler hndlEditQualifiedUsers =
      (function, workflowDesignerSC, request) -> {
        String strContext = request.getParameter("context");
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
        request.setAttribute("context", strContext);
        request.setAttribute("parentScreen", calculateParentScreen(workflowDesignerSC, strContext));
        request.setAttribute("EditorName", calculateEditorName(strContext));

        return root + "editQualifiedUsers.jsp";

      };
  /**
   * Handles the "UpdateQualifiedUsers" function
   */
  private static FunctionHandler hndlUpdateQualifiedUsers =
      (function, workflowDesignerSC, request) -> {
        String strRole = request.getParameter("role"), strContext = request.getParameter("context"),
            strMessage = request.getParameter("message");
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
        }

        // call the update in session controller
        //
        workflowDesignerSC.updateQualifiedUsers(qualifiedUsers, strContext);

        request.setAttribute("redirectTo", calculateParentScreen(workflowDesignerSC, strContext));
        return root + "redirect.jsp";
      };
  /**
   * Handles the "RemoveQualifiedUsers" function
   */
  private static FunctionHandler hndlRemoveQualifiedUsers =
      (function, workflowDesignerSC, request) -> {
        String strContext = request.getParameter("context");

        workflowDesignerSC.setQualifiedUsers(null, strContext);

        request.setAttribute("redirectTo", calculateParentScreen(workflowDesignerSC, strContext));
        return root + "redirect.jsp";
      };
  /**
   * Handles the "ViewActions" function
   */
  private static FunctionHandler hndlViewActions = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Actions", workflowDesignerSC.getProcessModel().getActionsEx());

    return root + "actions.jsp";
  };
  /**
   * Handles the "AddAction" and "ModifyAction" function
   */
  private static FunctionHandler hndlEditAction = (function, workflowDesignerSC, request) -> {
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

    return root + "editAction.jsp";
  };
  /**
   * Handles the "UpdateAction" function
   */
  private static FunctionHandler hndlUpdateAction = (function, workflowDesignerSC, request) -> {
    Action action = workflowDesignerSC.createAction();

    action.setName(request.getParameter("name"));

    Forms forms = workflowDesignerSC.getProcessModel().getForms();
    if (forms != null) {
      action.setForm(forms.getForm(request.getParameter("form")));
    }
    action.setKind(request.getParameter("kind"));

    workflowDesignerSC.updateAction(action, request.getParameter("name_original"));

    request.setAttribute("redirectTo", "ViewActions");
    return root + "redirect.jsp";
  };
  /**
   * Handles the "RemoveAction" function
   */
  private static FunctionHandler hndlRemoveAction = (function, workflowDesignerSC, request) -> {
    workflowDesignerSC.removeAction(request.getParameter("action"));

    request.setAttribute("redirectTo", "ViewActions");
    return root + "redirect.jsp";
  };
  /**
   * Handles the "AddConsequence" and "ModifyConsequence" function
   */
  private static FunctionHandler hndlEditConsequence = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter("context");
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
      strContext = strContext + "/consequences/" + Integer.toString(iConsequence);
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
    request.setAttribute("context", strContext);
    request.setAttribute("parentScreen", calculateParentScreen(workflowDesignerSC, strContext));
    request.setAttribute("StateNames", workflowDesignerSC.retrieveStateNames(false));
    request.setAttribute("FolderItemNames",
        workflowDesignerSC.retrieveFolderItemNames(true, false));
    request.setAttribute("Operators", workflowDesignerSC.retrieveOperators(true));

    return root + "editConsequence.jsp";
  };
  /**
   * Handles the "UpdateConsequence" function
   */
  private static FunctionHandler hndlUpdateConsequence =
      (function, workflowDesignerSC, request) -> {
        String strContext = request.getParameter("context"), strItem = request.getParameter("item"),
            strKill = request.getParameter("kill"), strSetUnset;
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

        if (StringUtil.isDefined(strKill)) {
          consequence.setKill(true);
        } else {
          consequence.setKill(false);
        }

        if (StringUtil.isDefined(strItem)) {
          consequence.setItem(strItem);
          consequence.setOperator(request.getParameter("operator"));
          consequence.setValue(request.getParameter("value"));
        }

        // Set / Unset States
        //
        while (iterState.hasNext()) {
          StateSetter stateSetter;

          state = (State) iterState.next();

          strSetUnset = request.getParameter("setUnset_" + state.getName());

          if (StringUtil.isDefined(strSetUnset)) {
            stateSetter = consequence.createStateSetter();
            stateSetter.setState(state);

            if ("set".equals(strSetUnset)) {
              consequence.addTargetState(stateSetter);
            } else if ("unset".equals(strSetUnset)) {
              consequence.addUnsetState(stateSetter);
            }
          }

        }

        workflowDesignerSC.updateConsequence(consequence, strContext);

        request.setAttribute("redirectTo", calculateParentScreen(workflowDesignerSC, strContext));
        return root + "redirect.jsp";
      };
  /**
   * Handles the "MoveConsequence" function
   */
  private static FunctionHandler hndlMoveConsequence = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter("context");
    int nDirection = Integer.parseInt(request.getParameter("direction")), iConsequence =
        Integer.parseInt(request.getParameter("consequenceNo"));

    workflowDesignerSC.moveConsequence(strContext, iConsequence, nDirection);

    request.setAttribute("redirectTo", calculateParentScreen(workflowDesignerSC, strContext));
    return root + "redirect.jsp";
  };
  /**
   * Handles the "RemoveConsequence" function
   */
  private static FunctionHandler hndlRemoveConsequence =
      (function, workflowDesignerSC, request) -> {
        String strContext = request.getParameter("context");

        workflowDesignerSC.removeConsequence(strContext);

        request.setAttribute("redirectTo", calculateParentScreen(workflowDesignerSC, strContext));
        return root + "redirect.jsp";
      };
  /**
   * Handles the "ViewUserInfos" function
   */
  private static FunctionHandler hndlViewUserInfos = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Items", workflowDesignerSC.getProcessModel().getUserInfos());
    request.setAttribute("context", WorkflowDesignerSessionController.USER_INFOS);

    return root + "userInfos.jsp";
  };
  /**
   * Handles the "ViewDataFolder" function
   */
  private static FunctionHandler hndlViewDataFolder = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Items", workflowDesignerSC.getProcessModel().getDataFolder());
    request.setAttribute("context", WorkflowDesignerSessionController.DATA_FOLDER);
    return root + "dataFolder.jsp";
  };
  /**
   * Handles the "ViewForms" function
   */
  private static FunctionHandler hndlViewForms = (function, workflowDesignerSC, request) -> {
    request.setAttribute("Forms", workflowDesignerSC.getProcessModel().getForms());
    return root + "forms.jsp";
  };
  /**
   * Handles the "AddForm" and "ModifyForm" function
   */
  private static FunctionHandler hndlEditForm = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter("context"), strFormType =
        WorkflowDesignerSessionController.FORM_TYPE_ACTION;
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
    request.setAttribute("context", strContext);
    request.setAttribute("type", strFormType);
    request.setAttribute("RoleNames", workflowDesignerSC.retrieveRoleNames(true, false));

    return root + "editForm.jsp";
  };
  /**
   * Handles the "UpdateForm" function
   */
  private static FunctionHandler hndlUpdateForm = (function, workflowDesignerSC, request) -> {
    Form form = workflowDesignerSC.createForm();
    String strHTMLFileName = request.getParameter("HTMLFileName"), strRole =
        request.getParameter("role"), strRoleOriginal = request.getParameter("role_original"),
        strFormType = request.getParameter("type");

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

    workflowDesignerSC.updateForm(form, request.getParameter("context"),
        request.getParameter("name_original"), strRoleOriginal);

    request.setAttribute("redirectTo", "ViewForms");
    return root + "redirect.jsp";
  };
  /**
   * Handles the "RemoveForm" function
   */
  private static FunctionHandler hndlRemoveForm = (function, workflowDesignerSC, request) -> {
    workflowDesignerSC.removeForm(request.getParameter("context"));

    request.setAttribute("redirectTo", "ViewForms");
    return root + "redirect.jsp";
  };
  /**
   * Handles the "AddInput" and "ModifyInput" function
   */
  private static FunctionHandler hndlEditInput = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter("context");
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
      strContext = strContext + "/inputs[" + Integer.toString(iInput) + "]";
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
      request.setAttribute("context", strContext);
      request.setAttribute("parentScreen", calculateParentScreen(
          workflowDesignerSC, strContext));
      request.setAttribute("TypesAndDisplayers", workflowDesignerSC.retrieveTypesAndDisplayers());
      request.setAttribute("FolderItems", workflowDesignerSC.retrieveFolderItems());

      return root + "editInput.jsp";
  };
  /**
   * Handles the "UpdateInput" function
   */
  private static FunctionHandler hndlUpdateInput = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter("context"), strItem = request.getParameter("item"),
        strValue = request.getParameter("value"), strDisplayer =
        request.getParameter("displayerName");
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

    if (StringUtil.isDefined(request.getParameter("mandatory"))) {
      input.setMandatory(true);
    } else {
      input.setMandatory(false);
    }

    if (StringUtil.isDefined(request.getParameter("readonly"))) {
      input.setReadonly(true);
    } else {
      input.setReadonly(false);
    }

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

    request.setAttribute("redirectTo", calculateParentScreen(workflowDesignerSC, strContext));
    return root + "redirect.jsp";
  };
  /**
   * Handles the "RemoveInput" function
   */
  private static FunctionHandler hndlRemoveInput = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter("context");

    workflowDesignerSC.removeInput(strContext);

    request.setAttribute("redirectTo", calculateParentScreen(workflowDesignerSC, strContext));
    return root + "redirect.jsp";
  };
  /**
   * Handles the "AddRelatedUser" and "ModifyRelatedUser" functions
   */
  private static FunctionHandler hndlEditRelatedUser = (function, workflowDesignerSC, request) -> {
    RelatedUser relatedUser;
    String strContext = request.getParameter("context"), strParticipant =
        request.getParameter("participant"), strFolderItem = request.getParameter("folderItem"),
        strRelation = request.getParameter("relation"), strRole = request.getParameter("role");

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
    request.setAttribute("context", strContext);
    request.setAttribute("parentScreen", calculateParentScreen(workflowDesignerSC, strContext));

    return root + "editRelatedUser.jsp";
  };
  /**
   * Handles the "UpdateRelatedUser" function
   */
  private static FunctionHandler hndlUpdateRelatedUser =
      (function, workflowDesignerSC, request) -> {
        String strParticipantOriginal = request.getParameter("participant_original"),
            strFolderItemOriginal = request.getParameter("folderItem_original"),
            strRelationOriginal = request.getParameter("relation_original"), strRoleOriginal =
            request.getParameter("role_original"), strParticipant =
            request.getParameter("participant"), strFolderItem = request.getParameter("folderItem"),
            strRelation = request.getParameter("relation"), strRole = request.getParameter("role"),
            strContext = request.getParameter("context");
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
          strParticipant = null;
          relatedUser.setParticipant(null);
        }

        if (StringUtil.isDefined(strFolderItem)) {
          relatedUser.setFolderItem(
              workflowDesignerSC.getProcessModel().getDataFolder().getItem(strFolderItem));
        } else {
          strFolderItem = null;
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
        request.setAttribute("redirectTo", calculateParentScreen(workflowDesignerSC, strContext));
        return root + "redirect.jsp";
      };
  /**
   * Handles the "RemoveRelatedUser" function
   */
  private static FunctionHandler hndlRemoveRelatedUser =
      (function, workflowDesignerSC, request) -> {
        RelatedUser relatedUser = workflowDesignerSC.getProcessModel().createRelatedUser();
        String strContext = request.getParameter("context"), strParticipant =
            request.getParameter("participant"), strFolderItem = request.getParameter("folderItem"),
            strRelation = request.getParameter("relation"), strRole = request.getParameter("role");

        if (StringUtil.isDefined(strParticipant)) {
          relatedUser.setParticipant(workflowDesignerSC.getProcessModel()
              .getParticipantsEx()
              .getParticipant(strParticipant));
        } else {
          strParticipant = null;
          relatedUser.setParticipant(null);
        }

        if (StringUtil.isDefined(strFolderItem)) {
          relatedUser.setFolderItem(
              workflowDesignerSC.getProcessModel().getDataFolder().getItem(strFolderItem));
        } else {
          strFolderItem = null;
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

        // remove the relatedUser concerned
        //
        workflowDesignerSC.removeRelatedUser(relatedUser, strContext);

        request.setAttribute("redirectTo", calculateParentScreen(workflowDesignerSC, strContext));
        return root + "redirect.jsp";
      };
  /**
   * Handles the "AddContextualDesignation" and "ModifyContextualDesignation" functions
   */
  private static FunctionHandler hndlEditContextualDesignation =
      (function, workflowDesignerSC, request) -> {
        ContextualDesignation designation;
        String strContext = request.getParameter("context");

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
        request.setAttribute("context", strContext);
        request.setAttribute("parentScreen", request.getParameter("parentScreen"));
        request.setAttribute("EditorName", calculateEditorName(strContext));

        return root + "editContextualDesignation.jsp";
      };
  /**
   * Handles the "UpdateContextualDesignation" function
   */
  private static FunctionHandler hndlUpdateContextualDesignation =
      (function, workflowDesignerSC, request) -> {
        String strLanguage = request.getParameter("lang_original"), strRole =
            request.getParameter("role_original"), strContext = request.getParameter("context"),
            strParentScreen = request.getParameter("parentScreen");
        ContextualDesignation designation =
            workflowDesignerSC.getProcessModel().createDesignation();

        designation.setLanguage(request.getParameter("lang"));
        designation.setRole(request.getParameter("role"));
        designation.setContent(request.getParameter("content"));

        workflowDesignerSC.updateContextualDesignations(strContext, designation, strLanguage,
            strRole);

        request.setAttribute("redirectTo", strParentScreen);
        return root + "redirect.jsp";
      };
  /**
   * Handles the "RemoveContextualDesignation" function
   */
  private static FunctionHandler hndlRemoveContextualDesignation =
      (function, workflowDesignerSC, request) -> {
        ContextualDesignation contextualDesignation =
            workflowDesignerSC.getProcessModel().createDesignation();
        String strContext = request.getParameter("context"), strParentScreen =
            request.getParameter("parentScreen");

        contextualDesignation.setRole(request.getParameter("role"));
        contextualDesignation.setLanguage(request.getParameter("lang"));

        // remove the contextual designation concerned
        //
        workflowDesignerSC.removeContextualDesignation(strContext, contextualDesignation);

        request.setAttribute("redirectTo", strParentScreen);
        return root + "redirect.jsp";
      };
  /**
   * Handles the "AddItem" and "ModifyItem" function
   */
  private static FunctionHandler hndlEditItem = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter("context");
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
    request.setAttribute("context", strContext);
    request.setAttribute("parentScreen", calculateParentScreen(workflowDesignerSC, strContext));
    request.setAttribute("TypeValues", workflowDesignerSC.retrieveItemTypeCodes(true));
    request.setAttribute("UserInfosNames",
        workflowDesignerSC.retrieveUserInfoItemNames(true, false));

    return root + "editItem.jsp";
  };
  /**
   * Handles the "UpdateItem" function
   */
  private static FunctionHandler hndlUpdateItem = (function, workflowDesignerSC, request) -> {
    String strNameOriginal = request.getParameter("name_original"), strName =
        request.getParameter("name"), strComupted = request.getParameter("computed"), strMapTo =
        request.getParameter("mapTo"), strType = request.getParameter("type"), strReadonly =
        request.getParameter("readonly"), strFormula = request.getParameter("formula"), strContext =
        request.getParameter("context");
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
    request.setAttribute("redirectTo", calculateParentScreen(workflowDesignerSC, strContext));
    return root + "redirect.jsp";
  };
  /**
   * Handles the "RemoveItem" function
   */
  private static FunctionHandler hndlRemoveItem = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter("context");

    workflowDesignerSC.removeItem(strContext);

    request.setAttribute("redirectTo", calculateParentScreen(workflowDesignerSC, strContext));
    return root + "redirect.jsp";
  };
  /**
   * Handles the "AddParameter" and "ModifyParameter" function
   */
  private static FunctionHandler hndlEditParameter = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter("context"), strName = request.getParameter("name");
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
    request.setAttribute("context", strContext);
    request.setAttribute("parentScreen", calculateParentScreen(workflowDesignerSC, strContext));

    return root + "editParameter.jsp";
  };
  /**
   * Handles the "UpdateParameter" function
   */
  private static FunctionHandler hndlUpdateParameter = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter("context"), strName = request.getParameter("name"),
        strValue = request.getParameter("value"), strNameOriginal =
        request.getParameter("name_original");
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

    request.setAttribute("redirectTo", calculateParentScreen(workflowDesignerSC, strContext));
    return root + "redirect.jsp";
  };
  /**
   * Handles the "RemoveParameter" function
   */
  private static FunctionHandler hndlRemoveParameter = (function, workflowDesignerSC, request) -> {
    String strContext = request.getParameter("context"), strName = request.getParameter("name");

    workflowDesignerSC.removeParameter(strContext, strName);

    request.setAttribute("redirectTo", calculateParentScreen(workflowDesignerSC, strContext));
    return root + "redirect.jsp";
  };

  /**
   * Calculate the name for an editor based on the context
   * @param strContext the context
   * @return resource key to retrieve the name
   */
  static private String calculateEditorName(String strContext) {
    int iSlash = strContext.lastIndexOf('/');
    String strEditor;

    if (iSlash >= 0) {
      strEditor = strContext.substring(iSlash + 1);
    } else {
      strEditor = strContext;
    }

    if (WorkflowDesignerSessionController.LABELS.equals(strEditor)) {
      return "workflowDesigner.editor.label";
    } else if (WorkflowDesignerSessionController.DESCRIPTIONS.equals(strEditor)) {
      return "workflowDesigner.editor.description";
    } else if (WorkflowDesignerSessionController.TITLES.equals(strEditor)) {
      return "workflowDesigner.editor.title";
    } else if (WorkflowDesignerSessionController.ACTIVITIES.equals(strEditor)) {
      return "workflowDesigner.editor.activity";
    } else if (WorkflowDesignerSessionController.WORKING_USERS
        .equals(strEditor)) {
      return "workflowDesigner.editor.workingUsers";
    } else if (WorkflowDesignerSessionController.INTERESTED_USERS
        .equals(strEditor)) {
      return "workflowDesigner.editor.interestedUsers";
    } else if (WorkflowDesignerSessionController.ALLOWED_USERS
        .equals(strEditor)) {
      return "workflowDesigner.editor.allowedUsers";
    } else if (WorkflowDesignerSessionController.NOTIFIED_USERS
        .equals(strEditor)) {
      return "workflowDesigner.editor.notifiedUsers";
    } else {
      return null;
    }
  }

  /**
   * Calculate the URL to the parent screen based on the current context
   * @param strContext the context
   * @param workflowDesignerSC session controller
   * @return the relative URL
   * @throws WorkflowException
   */
  private static String calculateParentScreen(WorkflowDesignerSessionController workflowDesignerSC,
      String strContext) throws WorkflowException {
    StringTokenizer strtok;
    String strElement, strParentScreen = "";
    StringBuffer sb = new StringBuffer();

    if (strContext == null) {
      return strParentScreen;
    }

    strtok = new StringTokenizer(strContext, "/[]"); //$NON-NLS-1$

    try {
      strElement = strtok.nextToken();
      sb.append("?context=");

      if (WorkflowDesignerSessionController.LABELS.equals(strElement)
          || WorkflowDesignerSessionController.DESCRIPTIONS.equals(strElement)) {
        strParentScreen = "ModifyWorkflow";
      } else if (WorkflowDesignerSessionController.ROLES.equals(strElement)) {
        strParentScreen = "ModifyRole?role=" + strtok.nextToken();
      } else if (WorkflowDesignerSessionController.PRESENTATION
          .equals(strElement)) {
        strParentScreen = "ModifyColumns?columns=" + strtok.nextToken();
      } else if (WorkflowDesignerSessionController.STATES.equals(strElement)) {
        State state;

        strElement = strtok.nextToken();
        state = workflowDesignerSC.getProcessModel().getStatesEx().getState(
            strElement);
        strElement = strtok.nextToken();

        sb.append(WorkflowDesignerSessionController.STATES);
        sb.append("/");
        sb.append(state.getName());

        strParentScreen = "ModifyState?state=" + state.getName();

        if (WorkflowDesignerSessionController.WORKING_USERS.equals(strElement)) {
          if (WorkflowDesignerSessionController.RELATED_USER.equals(strtok
              .nextToken())) {
            // is a 'relatedUser'
            //
            sb.append("/");
            sb.append(WorkflowDesignerSessionController.WORKING_USERS);
            strParentScreen = "ModifyQualifiedUsers" + sb.toString();
          }
        } else if (WorkflowDesignerSessionController.INTERESTED_USERS
            .equals(strElement)) {
          if (WorkflowDesignerSessionController.RELATED_USER.equals(strtok
              .nextToken())) {
            // is a 'relatedUser'
            //
            sb.append("/");
            sb.append(WorkflowDesignerSessionController.INTERESTED_USERS);
            strParentScreen = "ModifyQualifiedUsers" + sb.toString();
          }
        }
      } else if (WorkflowDesignerSessionController.ACTIONS.equals(strElement)) {
        Action action;

        strElement = strtok.nextToken();
        action = workflowDesignerSC.getProcessModel().getActionsEx().getAction(
            strElement);
        strElement = strtok.nextToken(); // allowedUsers or consequences

        sb.append(WorkflowDesignerSessionController.ACTIONS);
        sb.append("/");
        sb.append(action.getName());

        strParentScreen = "ModifyAction?action=" + action.getName();

        if (WorkflowDesignerSessionController.ALLOWED_USERS.equals(strElement)) {
          // is a 'relatedUser'
          //
          if (WorkflowDesignerSessionController.RELATED_USER.equals(strtok
              .nextToken())) {
            sb.append("/");
            sb.append(WorkflowDesignerSessionController.ALLOWED_USERS);
            strParentScreen = "ModifyQualifiedUsers" + sb.toString();
          }
        } else if (WorkflowDesignerSessionController.CONSEQUENCES
            .equals(strElement)) {
          sb.append("/");
          sb.append(WorkflowDesignerSessionController.CONSEQUENCES);
          sb.append("/");
          sb.append(strtok.nextToken()); // consequence no.

          // notified users
          //
          if (WorkflowDesignerSessionController.NOTIFIED_USERS.equals(strtok.nextToken())) {
            strParentScreen = "ModifyConsequence" + sb.toString();
            // related users
            if (WorkflowDesignerSessionController.RELATED_USER.equals(strtok.nextToken())) {
              sb.append("/");
              sb.append(WorkflowDesignerSessionController.NOTIFIED_USERS);

              strParentScreen = "ModifyQualifiedUsers" + sb.toString();
            }
          }
        }
      } else if (WorkflowDesignerSessionController.DATA_FOLDER
          .equals(strElement)) {
        strParentScreen = "ViewDataFolder";

        sb.append(WorkflowDesignerSessionController.DATA_FOLDER);
        sb.append("/");
        sb.append(strtok.nextToken()); // item name

        if (WorkflowDesignerSessionController.PARAMETERS.equals(strtok
            .nextToken())) {
          strParentScreen = "ModifyItem" + sb.toString();
        }
      } else if (WorkflowDesignerSessionController.USER_INFOS
          .equals(strElement)) {
        strParentScreen = "ViewUserInfos";

        sb.append(WorkflowDesignerSessionController.USER_INFOS);
        sb.append("/");
        sb.append(strtok.nextToken()); // item name

        if (WorkflowDesignerSessionController.PARAMETERS.equals(strtok
            .nextToken())) {
          strParentScreen = "ModifyItem" + sb.toString();
        }
      } else if (WorkflowDesignerSessionController.FORMS.equals(strElement)) {
        sb.append(WorkflowDesignerSessionController.FORMS);
        sb.append("[");
        sb.append(strtok.nextToken()); // frorm & role name
        sb.append("]");
        strElement = strtok.nextToken();

        if (WorkflowDesignerSessionController.INPUTS.equals(strElement)) {
          strParentScreen = "ModifyForm" + sb.toString();

          sb.append("/");
          sb.append(WorkflowDesignerSessionController.INPUTS);
          sb.append("[");
          sb.append(strtok.nextToken()); // input no
          sb.append("]");
          strElement = strtok.nextToken();

          if (WorkflowDesignerSessionController.LABELS.equals(strElement)) {
            strParentScreen = "ModifyInput" + sb.toString();
          }
        } else if (WorkflowDesignerSessionController.TITLES.equals(strElement)) {
          strParentScreen = "ModifyForm" + sb.toString();
        }
      }
    } catch (NoSuchElementException e) {
      // Thrown when no token was found where expected
      // do nothing, just return null...
    }
    return strParentScreen;
  }
}
