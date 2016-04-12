/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.pdc.control;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.silverpeas.core.pdc.PdcServiceProvider;
import org.silverpeas.core.pdc.thesaurus.service.ThesaurusManager;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.pdc.model.Axis;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.exception.SilverpeasException;

public class PdcSessionController extends AbstractComponentSessionController {

  private String currentView = "P";
  private Axis currentAxis = null;
  private Value currentValue = null;
  private String values = "";
  private String currentLanguage = null;

  /**
   * Constructor declaration
   *
   * @param mainSessionCtrl
   * @param componentContext
   * @see
   */
  public PdcSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBundle,
      String iconBundle) {
    super(mainSessionCtrl, componentContext, multilangBundle, iconBundle);
    currentLanguage = getLanguage();
  }

  private PdcManager getPdcBm() {
    return PdcServiceProvider.getPdcManager();
  }

  private ThesaurusManager getThBm() {
    return PdcServiceProvider.getThesaurusManager();
  }

  public ArrayList<String> getComponentList() {
    ArrayList<String> componentList = new ArrayList<String>();
    String[] allowedComponentIds = getUserAvailComponentIds();
    for (int i = 0; i < allowedComponentIds.length; i++) {
      componentList.add(allowedComponentIds[i]);
    }
    return componentList;
  }

  public boolean isPDCAdmin() {
    return getUserDetail().isAccessAdmin()
        || getUserDetail().isAccessPdcManager();
  }

  public void setCurrentLanguage(String language) {
    currentLanguage = language;
  }

  public String getCurrentLanguage() {
    return currentLanguage;
  }

  public void setCurrentView(String view) {
    currentView = view;
  }

  public String getCurrentView() {
    return currentView;
  }

  private void setCurrentAxis(Axis axis) {
    currentAxis = axis;
  }

  public Axis getCurrentAxis() {
    return currentAxis;
  }

  private void setCurrentValue(Value value) {
    currentValue = value;
  }

  public Value getCurrentValue() {
    return currentValue;
  }

  public void resetCurrentValue() {
    setCurrentValue(null);
  }

  public int getNbMaxAxis() throws PdcException {
    return getPdcBm().getNbMaxAxis();
  }

  public List<AxisHeader> getPrimaryAxis() throws PdcException {
    return getPdcBm().getAxisByType("P");
  }

  public List<AxisHeader> getSecondaryAxis() throws PdcException {
    return getPdcBm().getAxisByType("S");
  }

  public List<AxisHeader> getAxis() throws PdcException {
    return getPdcBm().getAxisByType(getCurrentView());
  }

  public boolean isCreationAllowed() throws PdcException {
    int nbAxis = getPdcBm().getNbAxis();
    if (nbAxis < getNbMaxAxis()) {
      return true;
    }
    return false;
  }

  public int createAxis(AxisHeader axisHeader) throws PdcException {
    axisHeader.setCreatorId(getUserId());
    axisHeader.setCreationDate(DateUtil.formatDate(new Date()));
    return getPdcBm().createAxis(axisHeader);
  }

  public int updateAxis(AxisHeader axisHeader) throws PdcException {
    return getPdcBm().updateAxis(axisHeader);
  }

  public void deleteAxis(String axisId) throws PdcException {
    Connection con = getConnection();

    try {
      // on recherche le treeId de l'axe à supprimer
      Axis axis = getPdcBm().getAxisDetail(axisId);
      long treeId = axis.getAxisHeader().getRootId();

      // supprime l'axe
      getPdcBm().deleteAxis(con, axisId);
      setCurrentAxis(null);

      // supprime les synonymes
      getThBm().deleteSynonymsAxis(con, treeId);

      commitConnection(con);

    } catch (Exception e) {
      rollbackConnection(con);
      throw new PdcException("PdcSessionController.deleteAxis",
          SilverpeasException.ERROR, "pdcPeas.CANNOT_DELETE_AXIS", "", e);
    } finally {
      closeConnection(con);
    }
  }

  public Axis getAxisDetail(String axisId) throws PdcException {
    Axis axis = getPdcBm().getAxisDetail(axisId);
    setCurrentAxis(axis);
    return axis;
  }

  public Value getAxisValue(String valueId) throws PdcException {
    return getAxisValue(valueId, true);
  }

  public Value getAxisValue(String valueId, boolean setAsCurrentValue)
      throws PdcException {
    Value value = getPdcBm().getAxisValue(valueId,
        new Integer(getCurrentAxis().getAxisHeader().getRootId()).toString());
    value.setAxisId(Integer.parseInt(getCurrentAxis().getAxisHeader().getPK()
        .getId()));
    if (setAsCurrentValue) {
      setCurrentValue(value);
    }
    return value;
  }

  public void setValuesToPdcAddAPosteriori(String values) {
    this.values = values;
  }

  public String getValuesToPdcAddAPosteriori() {
    return this.values;
  }

  public int insertMotherValue(Value value) throws PdcException {
    String currentValueId = getCurrentValue().getPK().getId();
    String currentAxisId = getCurrentAxis().getAxisHeader().getPK().getId();
    value.setCreatorId(getUserId());
    value.setCreationDate(DateUtil.formatDate(new Date()));
    int motherId = getPdcBm().insertMotherValue(value, currentValueId, currentAxisId);
    refreshCurrentAxis(getAxisDetail(getCurrentAxis().getAxisHeader().getPK().getId()));
    return motherId;
  }

  public int moveCurrentValueToNewFatherId(String newFatherId, int orderNumber)
      throws PdcException {
    int status = getPdcBm().moveValueToNewFatherId(getCurrentAxis(),
        getCurrentValue(), newFatherId, orderNumber);
    return status;
  }

  public int createDaughterValue(Value value) throws PdcException {
    String currentValueId = getCurrentValue().getPK().getId();
    value.setCreatorId(getUserId());
    value.setCreationDate(DateUtil.formatDate(new Date()));
    int status = getPdcBm().createDaughterValue(value, currentValueId,
        String.valueOf(getCurrentAxis().getAxisHeader().getRootId()));
    refreshCurrentAxis(getAxisDetail(getCurrentAxis().getAxisHeader().getPK().getId()));

    return status;
  }

  public int updateValue(Value value) throws PdcException {
    int status = getPdcBm().updateValue(value,
        new Integer(getCurrentAxis().getAxisHeader().getRootId()).toString());
    refreshCurrentValueAndCurrentAxis(value, getAxisDetail(getCurrentAxis()
        .getAxisHeader().getPK().getId()));
    return status;
  }

  public void deleteValueAndSubtree(String valueId) throws PdcException {
    Connection con = null;

    try {
      long treeId = getCurrentAxis().getAxisHeader().getRootId();
      // recupere les valueId des fils de valueId
      List<String> values = getPdcBm().getDaughterValues(Long.toString(treeId), valueId);
      // ajoute le valueId
      values.add(valueId);

      con = getConnection();

      getPdcBm().deleteValueAndSubtree(con, valueId,
          getCurrentAxis().getAxisHeader().getPK().getId(),
          new Integer(getCurrentAxis().getAxisHeader().getRootId()).toString());

      getThBm().deleteSynonymsTerms(con, treeId, values);
      // dans le PdcBmImpl : on efface les droits liées aux valeurs du subtree
      // effacé
      commitConnection(con);

      refreshCurrentValueAndCurrentAxis(null, getAxisDetail(getCurrentAxis()
          .getAxisHeader().getPK().getId()));

    } catch (Exception e) {
      rollbackConnection(con);
      throw new PdcException("PdcSessionController.deleteValueAndSubtree",
          SilverpeasException.ERROR, "pdcPeas.CANNOT_DELETE_VALUES", "", e);
    } finally {
      closeConnection(con);
    }
  }

  public String deleteValue(String valueId) throws PdcException {
    Connection con = null;
    String daughterValueName = null;
    try {
      con = getConnection();

      daughterValueName = getPdcBm().deleteValue(con, valueId,
          getCurrentAxis().getAxisHeader().getPK().getId(), String.valueOf(
          getCurrentAxis().getAxisHeader().getRootId()));
      if (daughterValueName == null) {
        long treeId = getCurrentAxis().getAxisHeader().getRootId();

        List<String> theValues = new ArrayList<String>();
        theValues.add(valueId);
        getThBm().deleteSynonymsTerms(con, treeId, theValues);

        commitConnection(con);

        refreshCurrentValueAndCurrentAxis(null, getAxisDetail(getCurrentAxis()
            .getAxisHeader().getPK().getId()));
      }
    } catch (Exception e) {
      rollbackConnection(con);
      throw new PdcException("PdcSessionController.deleteValue",
          SilverpeasException.ERROR, "pdcPeas.CANNOT_DELETE_VALUE", "", e);
    } finally {
      closeConnection(con);
    }
    return daughterValueName;
  }

  /**
   * Returns the full path of the value
   *
   * @param valueId - the id of the selected value (valueId is not empty)
   * @return the complet path
   */
  public List getFullPath(String valueId) throws PdcException {
    return getPdcBm().getFullPath(valueId,
        Integer.toString(getCurrentAxis().getAxisHeader().getRootId()));
  }

  private void refreshCurrentValueAndCurrentAxis(Value value, Axis axis) {
    // Supprime la valeur courante dans la session
    setCurrentValue(value);
    // Recharge le détail de l'axe courant
    // afin de prendre en compte la suppression
    setCurrentAxis(axis);
  }

  private void refreshCurrentAxis(Axis axis) {
    // Recharge le détail de l'axe courant
    // afin de prendre en compte la suppression
    setCurrentAxis(axis);
  }

  private Connection getConnection() throws PdcException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();
      con.setAutoCommit(false);
    } catch (Exception e) {
      throw new PdcException("PdcSessionController.getConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  private void closeConnection(Connection con) throws PdcException {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        throw new PdcException("pdcPeas.closeConnection",
            SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  private void rollbackConnection(Connection con) {
    if (con != null) {
      try {
        con.rollback();
      } catch (Exception e) {
        SilverTrace.error("pdcPeas", "rollbackConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  private void commitConnection(Connection con) {
    if (con != null) {
      try {
        con.commit();
      } catch (Exception e) {
        SilverTrace.error("pdcPeas", "commitConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /**
   * Initialise le UserPanel avec les permissions déjà existantes pour la valeur courante
   *
   * @return l'URL du panel
   * @throws RemoteException
   * @throws PdcException
   * @throws SQLException
   */
  public String initUserPanelForPdcManager() throws RemoteException,
      PdcException, SQLException {
    String m_context = URLUtil.getApplicationURL();
    Pair<String, String>[] hostPath = new Pair[1];

    String name = getCurrentAxis().getAxisHeader()
        .getName(getCurrentLanguage());
    if (getCurrentValue() != null) {
      name = getCurrentValue().getName(getCurrentLanguage());
    }

    hostPath[0] = new Pair<>(name, "/Rpdc/jsp/ViewManager");

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(getString("pdcPeas.pdc"));
    sel.setHostComponentName(new Pair<>(getString("pdcPeas.managers"), ""));
    sel.setHostPath(hostPath);

    String hostUrl = m_context + "/Rpdc/jsp/UpdateManager";
    String cancelUrl = m_context + "/Rpdc/jsp/ViewManager";

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    // Contraintes
    // sel.setMultiSelect(false);
    // sel.setPopupMode(true);
    // sel.setSetSelectable(false);

    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());

    // On récupère la liste des utilisateurs et groupes ayant droits
    List managers = getManagers();
    List<UserDetail> users = (List<UserDetail>) managers.get(0);
    List<Group> groups = (List<Group>) managers.get(1);
    String[] selectedUsers = new String[users.size()];
    String[] selectedGroups = new String[groups.size()];

    int i = 0;
    while (i < users.size()) {
      selectedUsers[i] = users.get(i).getId();
      i++;
    }
    i = 0;
    while (i < groups.size()) {
      selectedGroups[i] = groups.get(i).getId();
      i++;
    }
    sel.setSelectedElements(selectedUsers);
    sel.setSelectedSets(selectedGroups);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /**
   * récupère le résultat du UserPanel
   *
   * @throws PdcException
   * @throws SQLException
   */
  public void updateManager() throws PdcException, SQLException {
    List<String> usersId = new ArrayList<String>();
    ArrayList<String> groupsId = new ArrayList<String>();

    for (int i = 0; i < getSelection().getSelectedElements().length; i++) {
      usersId.add(getSelection().getSelectedElements()[i]);
    }
    for (int i = 0; i < getSelection().getSelectedSets().length; i++) {
      groupsId.add(getSelection().getSelectedSets()[i]);
    }
    setManagers(usersId, groupsId);
  }

  public Value getValue(String axisId, String valueId) throws PdcException {

    SearchContext searchContext = new SearchContext(this.getUserId());

    List<Value> values = getPdcBm().getPertinentDaughterValuesByInstanceIds(
        searchContext, axisId, valueId, getComponentList());

    Value value = null;
    if (values != null) {
      Iterator<Value> i = values.iterator();
      Value theValue = null;
      while (i.hasNext()) {
        theValue = i.next();
        if (theValue.getPK().getId().equals(valueId)) {
          value = theValue;
          value.setAxisId(Integer.parseInt(axisId));
          setCurrentValue(value);
          break;
        }
      }
    }
    return value;
  }

  /**
   * get the managers for the current value
   *
   * @return ArrayList ( ArrayList UserDetail, ArrayList Group )
   * @throws PdcException
   */
  public List getManagers() throws PdcException, SQLException {
    List usersAndGroups = new ArrayList();

    String valueId = "-1";
    if (getCurrentValue() != null) {
      valueId = getCurrentValue().getPK().getId();
    }

    List<List<String>> managers = getPdcBm().getManagers(getCurrentAxis().getAxisHeader().getPK()
        .getId(), valueId);

    List<String> usersId = managers.get(0);
    List<String> groupsId = managers.get(1);
    usersAndGroups.add(userIds2Users(usersId));
    usersAndGroups.add(groupIds2Groups(groupsId));
    return usersAndGroups;
  }

  /**
   * get the managers of the specified value
   *
   * @return ArrayList ( ArrayList UserDetail, ArrayList Group )
   * @throws PdcException
   */
  public List getManagers(String axisId, String valueId) throws PdcException,
      SQLException {
    List usersAndGroups = new ArrayList();

    List<List<String>> managers = getPdcBm().getManagers(axisId, valueId);

    List<String> usersId = managers.get(0);
    List<String> groupsId = managers.get(1);
    usersAndGroups.add(userIds2Users(usersId));
    usersAndGroups.add(groupIds2Groups(groupsId));
    return usersAndGroups;
  }

  /**
   * get the inherited managers of the specified value
   *
   * @return ArrayList ( ArrayList UserDetail, ArrayList Group )
   * @throws PdcException , SQLException
   */
  public List getInheritedManagers(Value value) throws PdcException,
      SQLException {
    List usersAndGroups = new ArrayList();

    List managers = getPdcBm().getInheritedManagers(value);

    List<String> usersId = (List) managers.get(0);
    List<String> groupsId = (List) managers.get(1);
    usersAndGroups.add(userIds2Users(usersId));
    usersAndGroups.add(groupIds2Groups(groupsId));
    return usersAndGroups;
  }

  /**
   * retourne un tableau des valeurs où l'utilisateur courant possède des droits
   *
   * @return ArrayList ( valueid )
   * @throws PdcException , SQLException
   */
  public List<String> getRights() throws PdcException, SQLException {
    String valueId = "";
    String currentUserId = getUserId();
    List<String> rights = new ArrayList<String>();
    Value value = new Value();
    List<Value> axisValues = getCurrentAxis().getValues();
    List usersAndGroups = new ArrayList();
    for (int i = 0; i < axisValues.size(); i++) {
      value = axisValues.get(i);
      valueId = value.getPK().getId();
      usersAndGroups = getManagers(getCurrentAxis().getAxisHeader().getPK()
          .getId(), valueId);
      List<UserDetail> users = (List<UserDetail>) usersAndGroups.get(0);
      List<Group> groups = (List<Group>) usersAndGroups.get(1);
      for (int j = 0; j < groups.size(); j++) {
        Group groupe = groups.get(j);
        for (int k = 0; k < groupe.getUserIds().length; k++) {
          if (groupe.getUserIds()[k].equals(currentUserId)) {
            rights.add(valueId);
          }
        }
      }
      for (int j = 0; j < users.size(); j++) {
        UserDetail user = users.get(j);
        if (user.getId().equals(currentUserId)) {
          rights.add(valueId);
        }
      }
    }
    return rights;
  }

  public boolean isValueManager() throws PdcException, SQLException {
    return isValueManager(getCurrentAxis().getAxisHeader().getPK().getId(),
        getCurrentValue().getPK().getId());
  }

  public boolean isAxisManager() throws PdcException, SQLException {
    return isManager(getCurrentAxis().getAxisHeader().getPK().getId(), "-1");
  }

  public boolean isAxisManager(String axisId) throws PdcException, SQLException {
    return isManager(axisId, "-1");
  }

  public boolean isValueManager(String axisId, String valueId)
      throws PdcException, SQLException {
    return isManager(axisId, valueId);
  }

  public boolean isManager(String axisId, String valueId) throws PdcException,
      SQLException {
    if (getUserDetail().isAccessPdcManager() || getUserDetail().isAccessAdmin()) {
      return true;
    }

    boolean userAllowed = false;

    List usersAndGroups = getManagers(axisId, valueId);

    List<UserDetail> users = (List<UserDetail>) usersAndGroups.get(0);
    for (int j = 0; !userAllowed && j < users.size(); j++) {
      UserDetail user = users.get(j);
      if (user.getId().equals(getUserId())) {
        return true;
      }
    }

    if (!userAllowed) {
      List<Group> groups = (List<Group>) usersAndGroups.get(1);
      for (int j = 0; !userAllowed && j < groups.size(); j++) {
        Group groupe = groups.get(j);
        for (int k = 0; !userAllowed && k < groupe.getUserIds().length; k++) {
          if (groupe.getUserIds()[k].equals(getUserId())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean isInheritedManager() throws PdcException, SQLException {
    boolean userAllowed = false;

    List usersAndGroups = getInheritedManagers(getCurrentValue());

    List<UserDetail> users = (List<UserDetail>) usersAndGroups.get(0);
    for (int j = 0; !userAllowed && j < users.size(); j++) {
      UserDetail user = users.get(j);
      if (user.getId().equals(getUserId())) {
        return true;
      }
    }

    if (!userAllowed) {
      List<Group> groups = (List<Group>) usersAndGroups.get(1);
      for (int j = 0; !userAllowed && j < groups.size(); j++) {
        Group groupe = groups.get(j);
        for (int k = 0; !userAllowed && k < groupe.getUserIds().length; k++) {
          if (groupe.getUserIds()[k].equals(getUserId())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public List<String> getAxisManageables() throws PdcException, SQLException {
    List<String> axisManageables = new ArrayList<String>();

    List<AxisHeader> axisList = getAxis();

    AxisHeader axis = null;
    for (int a = 0; a < axisList.size(); a++) {
      axis = axisList.get(a);

      if (isAxisManager(axis.getPK().getId())) {
        axisManageables.add(axis.getPK().getId());
      }
    }

    return axisManageables;
  }

  /**
   * update permissions on current value
   *
   * @param userIds
   * @param groupIds
   * @throws PdcException
   * @throws SQLException
   */
  public void setManagers(List userIds, List groupIds) throws PdcException,
      SQLException {
    String valueId = "-1";
    if (getCurrentValue() != null) {
      valueId = getCurrentValue().getPK().getId();
    }

    getPdcBm().setManagers(userIds, groupIds, getCurrentAxis().getAxisHeader()
        .getPK().getId(), valueId);
  }

  /**
   * delete permissions on current value
   *
   * @throws PdcException
   * @throws SQLException
   */
  public void eraseManagers() throws PdcException, SQLException {
    String valueId = "-1";
    if (getCurrentValue() != null) {
      valueId = getCurrentValue().getPK().getId();
    }

    getPdcBm().razManagers(getCurrentAxis().getAxisHeader().getPK().getId(), valueId);
  }

  private AdminController getAdmin() {
    return ServiceProvider.getService(AdminController.class);
  }

  public List<Group> groupIds2Groups(List<String> groupIds) {
    List<Group> res = new ArrayList<Group>();
    Group theGroup = null;

    for (int nI = 0; groupIds != null && nI < groupIds.size(); nI++) {
      theGroup = getAdmin().getGroupById(groupIds.get(nI));
      if (theGroup != null) {
        res.add(theGroup);
      }
    }

    return res;
  }

  public List<UserDetail> userIds2Users(List<String> userIds) {
    List<UserDetail> res = new ArrayList<UserDetail>();
    UserDetail user = null;

    for (int nI = 0; userIds != null && nI < userIds.size(); nI++) {
      user = getUserDetail(userIds.get(nI));
      if (user != null) {
        res.add(user);
      }
    }

    return res;
  }
}