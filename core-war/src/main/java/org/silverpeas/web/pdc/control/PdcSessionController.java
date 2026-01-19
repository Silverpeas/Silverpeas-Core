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
package org.silverpeas.web.pdc.control;

import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.pdc.PdcServiceProvider;
import org.silverpeas.core.pdc.pdc.model.*;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.thesaurus.service.ThesaurusManager;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.controller.AbstractAdminComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.Pair;

import java.sql.Connection;
import java.util.*;

public class PdcSessionController extends AbstractAdminComponentSessionController {
  private static final long serialVersionUID = -7993993070048344281L;

  private String currentView = "P";
  private Axis currentAxis = null;
  private Value currentValue = null;
  private String values = "";
  private String currentLanguage;

  public PdcSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBundle,
      String iconBundle) {
    super(mainSessionCtrl, componentContext, multilangBundle, iconBundle);
    currentLanguage = getLanguage();
  }

  @Override
  public boolean isAccessGranted() {
    try {
      return isPDCAdmin() || getPdcManager().isUserManager(getUserId());
    } catch (PdcException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  private PdcManager getPdcManager() {
    return PdcServiceProvider.getPdcManager();
  }

  private ThesaurusManager getThBm() {
    return PdcServiceProvider.getThesaurusManager();
  }

  public ArrayList<String> getComponentList() {
    ArrayList<String> componentList = new ArrayList<>();
    String[] allowedComponentIds = getUserAvailComponentIds();
    Collections.addAll(componentList, allowedComponentIds);
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
    return getPdcManager().getNbMaxAxis();
  }

  public List<AxisHeader> getPrimaryAxis() throws PdcException {
    return getPdcManager().getAxisByType("P");
  }

  public List<AxisHeader> getSecondaryAxis() throws PdcException {
    return getPdcManager().getAxisByType("S");
  }

  public List<AxisHeader> getAxis() throws PdcException {
    return getPdcManager().getAxisByType(getCurrentView());
  }

  public boolean isCreationAllowed() throws PdcException {
    int nbAxis = getPdcManager().getNbAxis();
    return nbAxis < getNbMaxAxis();
  }

  public int createAxis(AxisHeader axisHeader) throws PdcException {
    axisHeader.setCreatorId(getUserId());
    axisHeader.setCreationDate(DateUtil.formatDate(new Date()));
    return getPdcManager().createAxis(axisHeader);
  }

  public int updateAxis(AxisHeader axisHeader) throws PdcException {
    return getPdcManager().updateAxis(axisHeader);
  }

  public void deleteAxis(String axisId) throws PdcException {
    Connection con = getConnection();

    try {
      // on recherche le treeId de l'axe à supprimer
      Axis axis = getPdcManager().getAxisDetail(axisId);
      long treeId = axis.getAxisHeader().getRootId();

      // supprime l'axe
      getPdcManager().deleteAxis(con, axisId);
      setCurrentAxis(null);

      // supprime les synonymes
      getThBm().deleteSynonymsAxis(con, treeId);

      commitConnection(con);

    } catch (Exception e) {
      rollbackConnection(con);
      throw new PdcException(e);
    } finally {
      closeConnection(con);
    }
  }

  public Axis getAxisDetail(String axisId) throws PdcException {
    Axis axis = getPdcManager().getAxisDetail(axisId);
    setCurrentAxis(axis);
    return axis;
  }

  public Value getAxisValue(String valueId) throws PdcException {
    return getAxisValue(valueId, true);
  }

  public Value getAxisValue(String valueId, boolean setAsCurrentValue)
      throws PdcException {
    Value value = getPdcManager().getAxisValue(valueId,
        Integer.toString(getCurrentAxis().getAxisHeader().getRootId()));
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
    int motherId = getPdcManager().insertMotherValue(value, currentValueId, currentAxisId);
    refreshCurrentAxis(getAxisDetail(getCurrentAxis().getAxisHeader().getPK().getId()));
    return motherId;
  }

  public int moveCurrentValueToNewFatherId(String newFatherId, int orderNumber)
      throws PdcException {
    return getPdcManager().moveValueToNewFatherId(getCurrentAxis(),
        getCurrentValue(), newFatherId, orderNumber);
  }

  public int createDaughterValue(Value value) throws PdcException {
    String currentValueId = getCurrentValue().getPK().getId();
    value.setCreatorId(getUserId());
    value.setCreationDate(DateUtil.formatDate(new Date()));
    int status = getPdcManager().createDaughterValue(value, currentValueId,
        String.valueOf(getCurrentAxis().getAxisHeader().getRootId()));
    refreshCurrentAxis(getAxisDetail(getCurrentAxis().getAxisHeader().getPK().getId()));

    return status;
  }

  public int updateValue(Value value) throws PdcException {
    int status = getPdcManager().updateValue(value,
        Integer.toString(getCurrentAxis().getAxisHeader().getRootId()));
    refreshCurrentValueAndCurrentAxis(value, getAxisDetail(getCurrentAxis()
        .getAxisHeader().getPK().getId()));
    return status;
  }

  public void deleteValueAndSubtree(String valueId) throws PdcException {
    Connection con = null;

    try {
      long treeId = getCurrentAxis().getAxisHeader().getRootId();
      // recupere les valueId des fils de valueId
      List<String> values = getPdcManager().getDaughterValues(Long.toString(treeId), valueId);
      // ajoute le valueId
      values.add(valueId);

      con = getConnection();

      getPdcManager().deleteValueAndSubtree(con, valueId,
          getCurrentAxis().getAxisHeader().getPK().getId(),
          Integer.toString(getCurrentAxis().getAxisHeader().getRootId()));

      getThBm().deleteSynonymsTerms(con, treeId, values);
      // dans le PdcBmImpl : on efface les droits liées aux valeurs du subtree
      // effacé
      commitConnection(con);

      refreshCurrentValueAndCurrentAxis(null, getAxisDetail(getCurrentAxis()
          .getAxisHeader().getPK().getId()));

    } catch (Exception e) {
      rollbackConnection(con);
      throw new PdcException(e);
    } finally {
      closeConnection(con);
    }
  }

  public String deleteValue(String valueId) throws PdcException {
    Connection con = null;
    String daughterValueName;
    try {
      con = getConnection();

      daughterValueName = getPdcManager().deleteValue(con, valueId,
          getCurrentAxis().getAxisHeader().getPK().getId(), String.valueOf(
          getCurrentAxis().getAxisHeader().getRootId()));
      if (daughterValueName == null) {
        long treeId = getCurrentAxis().getAxisHeader().getRootId();

        List<String> theValues = new ArrayList<>();
        theValues.add(valueId);
        getThBm().deleteSynonymsTerms(con, treeId, theValues);

        commitConnection(con);

        refreshCurrentValueAndCurrentAxis(null, getAxisDetail(getCurrentAxis()
            .getAxisHeader().getPK().getId()));
      }
    } catch (Exception e) {
      rollbackConnection(con);
      throw new PdcException(e);
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
  public List<Value> getFullPath(String valueId) throws PdcException {
    return getPdcManager().getFullPath(valueId,
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
      DBUtil.close(con);
      throw new PdcException(e);
    }
    return con;
  }

  private void closeConnection(Connection con) throws PdcException {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        throw new PdcException(e);
      }
    }
  }

  private void rollbackConnection(Connection con) {
    if (con != null) {
      try {
        con.rollback();
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  private void commitConnection(Connection con) {
    if (con != null) {
      try {
        con.commit();
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  public String initUserPanelForPdcManager() throws PdcException {
    String m_context = URLUtil.getApplicationURL();
    //noinspection unchecked
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

    // On récupère la liste des utilisateurs et groupes ayant droits
    Pair<List<User>, List<Group>> managers = getManagers();
    List<User> users = managers.getFirst();
    List<Group> groups = managers.getSecond();
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
    return Selection.getSelectionURL();
  }

  public void updateManager() throws PdcException {
    List<String> usersId = Arrays.asList(getSelection().getSelectedElements());
    List<String> groupsId = Arrays.asList(getSelection().getSelectedSets());
    setManagers(usersId, groupsId);
  }

  public Value getValue(String axisId, String valueId) throws PdcException {

    SearchContext searchContext = new SearchContext(this.getUserId());

    List<Value> values = getPdcManager().getPertinentDaughterValuesByInstanceIds(
        searchContext, axisId, valueId, getComponentList());

    Value value = null;
    if (values != null) {
      Iterator<Value> i = values.iterator();
      Value theValue;
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
   * Get the managers for the current value
   *
   * @return pair of list: the first one with the users and the second one with the groups.
   * @throws PdcException if an error occurs while getting the managers.
   */
  public Pair<List<User>, List<Group>> getManagers() throws PdcException {
    String valueId = "-1";
    if (getCurrentValue() != null) {
      valueId = getCurrentValue().getPK().getId();
    }

    List<List<String>> managers = getPdcManager().getManagers(getCurrentAxis().getAxisHeader().getPK()
        .getId(), valueId);

    List<String> usersId = managers.get(0);
    List<String> groupsId = managers.get(1);
    List<User> users = userIds2Users(usersId);
    List<Group> groups = groupIds2Groups(groupsId);
    return Pair.of(users, groups);
  }

  /**
   * get the managers of the specified value
   *
   * @return a pair of lists: the first one with the users and the last one with the groups
   * @throws PdcException if the managers fails to be fetched
   */
  public Pair<List<User>, List<Group>> getManagers(String axisId, String valueId) throws PdcException {
    List<List<String>> managers = getPdcManager().getManagers(axisId, valueId);

    List<String> usersId = managers.get(0);
    List<String> groupsId = managers.get(1);
    return Pair.of(userIds2Users(usersId), groupIds2Groups(groupsId));
  }

  /**
   * Get the inherited managers of the specified value
   *
   * @return a pair of lists: the first one with the users and the last one with the groups
   * @throws PdcException if the managers cannot be fetched.
   */
  public Pair<List<User>, List<Group>> getInheritedManagers(Value value) throws PdcException {
    List<List<String>> managers = getPdcManager().getInheritedManagers(value);

    List<String> usersId = managers.get(0);
    List<String> groupsId = managers.get(1);
    return Pair.of(userIds2Users(usersId), groupIds2Groups(groupsId));
  }

  /**
   * Retourne un tableau des valeurs où l'utilisateur courant possède des droits
   *
   * @return ArrayList ( valueid )
   * @throws PdcException , SQLException
   */
  public List<String> getRights() throws PdcException {
    String currentUserId = getUserId();
    List<String> rights = new ArrayList<>();
    List<Value> axisValues = getCurrentAxis().getValues();
    for (Value value : axisValues) {
      String valueId = value.getPK().getId();
      var usersAndGroups = getManagers(
          getCurrentAxis().getAxisHeader().getPK().getId(), valueId);
      List<User> users = usersAndGroups.getFirst();
      List<Group> groups = usersAndGroups.getSecond();
      for (Group groupe : groups) {
        for (int k = 0; k < groupe.getUserIds().length; k++) {
          if (groupe.getUserIds()[k].equals(currentUserId)) {
            rights.add(valueId);
          }
        }
      }
      for (User user : users) {
        if (user.getId().equals(currentUserId)) {
          rights.add(valueId);
        }
      }
    }
    return rights;
  }

  public boolean isValueManager() throws PdcException {
    return isValueManager(getCurrentAxis().getAxisHeader().getPK().getId(),
        getCurrentValue().getPK().getId());
  }

  public boolean isAxisManager() throws PdcException {
    return isManager(getCurrentAxis().getAxisHeader().getPK().getId(), "-1");
  }

  public boolean isAxisManager(String axisId) throws PdcException {
    return isManager(axisId, "-1");
  }

  public boolean isValueManager(String axisId, String valueId)
      throws PdcException {
    return isManager(axisId, valueId);
  }

  public boolean isManager(String axisId, String valueId) throws PdcException {
    if (getUserDetail().isAccessPdcManager() || getUserDetail().isAccessAdmin()) {
      return true;
    }
    var usersAndGroups = getManagers(axisId, valueId);
    return isThereAManager(usersAndGroups);
  }

  public boolean isInheritedManager() throws PdcException {
    var usersAndGroups = getInheritedManagers(getCurrentValue());
    return isThereAManager(usersAndGroups);
  }

  private boolean isThereAManager(Pair<List<User>, List<Group>> usersAndGroups) {
    List<User> users = usersAndGroups.getFirst();
    for (User user : users) {
      if (user.getId().equals(getUserId())) {
        return true;
      }
    }

    List<Group> groups = usersAndGroups.getSecond();
    for (Group groupe : groups) {
      for (int k = 0; k < groupe.getUserIds().length; k++) {
        if (groupe.getUserIds()[k].equals(getUserId())) {
          return true;
        }
      }
    }
    return false;
  }

  public List<String> getAxisManageables() throws PdcException {
    List<String> axisManageables = new ArrayList<>();

    List<AxisHeader> axisList = getAxis();

    for (AxisHeader axisHeader : axisList) {
      if (isAxisManager(axisHeader.getPK().getId())) {
        axisManageables.add(axisHeader.getPK().getId());
      }
    }

    return axisManageables;
  }

  public void setManagers(List<String> userIds, List<String> groupIds) throws PdcException {
    String valueId = "-1";
    if (getCurrentValue() != null) {
      valueId = getCurrentValue().getPK().getId();
    }

    getPdcManager().setManagers(userIds, groupIds, getCurrentAxis().getAxisHeader()
        .getPK().getId(), valueId);
  }

  public void eraseManagers() throws PdcException {
    String valueId = "-1";
    if (getCurrentValue() != null) {
      valueId = getCurrentValue().getPK().getId();
    }
    getPdcManager().razManagers(getCurrentAxis().getAxisHeader().getPK().getId(), valueId);
  }

  private AdminController getAdmin() {
    return ServiceProvider.getService(AdminController.class);
  }

  public List<Group> groupIds2Groups(List<String> groupIds) {
    List<Group> res = new ArrayList<>();
    Group theGroup;

    for (int nI = 0; groupIds != null && nI < groupIds.size(); nI++) {
      theGroup = getAdmin().getGroupById(groupIds.get(nI));
      if (theGroup != null) {
        res.add(theGroup);
      }
    }

    return res;
  }

  public List<User> userIds2Users(List<String> userIds) {
    List<User> res = new ArrayList<>();
    for (int nI = 0; userIds != null && nI < userIds.size(); nI++) {
      User user = getUserDetail(userIds.get(nI));
      if (user != null) {
        res.add(user);
      }
    }

    return res;
  }
}