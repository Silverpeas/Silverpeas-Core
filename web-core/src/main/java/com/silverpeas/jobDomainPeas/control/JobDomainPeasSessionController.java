/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.jobDomainPeas.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.jobDomainPeas.DomainNavigationStock;
import com.silverpeas.jobDomainPeas.GroupNavigationStock;
import com.silverpeas.jobDomainPeas.JobDomainPeasDAO;
import com.silverpeas.jobDomainPeas.JobDomainPeasException;
import com.silverpeas.jobDomainPeas.JobDomainPeasTrappedException;
import com.silverpeas.jobDomainPeas.JobDomainSettings;
import com.silverpeas.jobDomainPeas.SynchroUserWebServiceItf;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.csv.CSVReader;
import com.silverpeas.util.csv.Variant;
import com.silverpeas.util.security.X509Factory;
import com.stratelia.silverpeas.authentication.Authentication;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionException;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.beans.admin.AbstractDomainDriver;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.GroupProfileInst;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.exception.UtilTrappedException;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class JobDomainPeasSessionController extends
    AbstractComponentSessionController {
  String m_TargetUserId = null;

  String m_TargetDomainId = "";
  DomainNavigationStock m_TargetDomain = null;

  Vector m_GroupsPath = new Vector();

  SynchroThread m_theThread = null;
  Exception m_ErrorOccured = null;
  String m_SynchroReport = "";

  Selection sel = null;

  List usersToImport = null;
  Hashtable queryToImport = null;

  AdminController m_AdminCtrl = null;

  private ArrayList listSelectedUsers = new ArrayList();

  // pagination de la liste des résultats
  private int indexOfFirstItemToDisplay = 0;

  /**
   * Standard Session Controller Constructeur
   * 
   * 
   * @param mainSessionCtrl
   *          The user's profile
   * @param componentContext
   *          The component's profile
   * 
   * @see
   */
  public JobDomainPeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.jobDomainPeas.multilang.jobDomainPeasBundle",
        "com.silverpeas.jobDomainPeas.settings.jobDomainPeasIcons");
    setComponentRootName(URLManager.CMP_JOBDOMAINPEAS);
    m_AdminCtrl = new AdminController(getUserId());
    sel = getSelection();
  }

  public int getMinLengthLogin() {
    return JobDomainSettings.m_MinLengthLogin;
  }

  public int getMinLengthPwd() {
    return JobDomainSettings.m_MinLengthPwd;
  }

  public boolean isBlanksAllowedInPwd() {
    return JobDomainSettings.m_BlanksAllowedInPwd;
  }

  public boolean isUserAddingAllowedForGroupManager() {
    return JobDomainSettings.m_UserAddingAllowedForGroupManagers;
  }

  public boolean isAccessGranted() {
    boolean accessGranted = false;
    if (getUserManageableGroupIds().size() > 0)
      return true;
    if (getUserDetail().isAccessAdmin()
        || getUserDetail().isAccessDomainManager())
      return true;

    return accessGranted;
  }

  /*
   * 
   * USER functions
   */
  public void setTargetUser(String userId) {
    m_TargetUserId = userId;
  }

  public UserDetail getTargetUserDetail() throws JobDomainPeasException {
    UserDetail valret = null;

    if ((m_TargetUserId != null) && (m_TargetUserId.length() > 0)) {
      valret = getOrganizationController().getUserDetail(m_TargetUserId);
      if (valret == null) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.getTargetUserDetail()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_USER_NOT_AVAILABLE",
            "UserId=" + m_TargetUserId);
      }
    }
    return valret;
  }

  public UserFull getTargetUserFull() throws JobDomainPeasException {
    UserFull valret = null;

    if ((m_TargetUserId != null) && (m_TargetUserId.length() > 0)) {
      valret = getOrganizationController().getUserFull(m_TargetUserId);
      if (valret == null) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.getTargetUserFull()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_USER_NOT_AVAILABLE",
            "UserId=" + m_TargetUserId);
      }
    }
    return valret;
  }

  public void createUser(String userLogin, String userLastName,
      String userFirstName, String userEMail, String userAccessLevel,
      boolean userPasswordValid, String userPassword, HashMap properties)
      throws JobDomainPeasException, JobDomainPeasTrappedException {
    UserDetail theNewUser = new UserDetail();
    String idRet = null;
    String existingUser;

    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.createUser()",
        "root.MSG_GEN_ENTER_METHOD", "userLogin=" + userLogin
            + " userLastName=" + userLastName + " userFirstName="
            + userFirstName + " userEMail=" + userEMail + " userAccessLevel="
            + userAccessLevel);

    existingUser = m_AdminCtrl.getUserIdByLoginAndDomain(userLogin,
        m_TargetDomainId);
    if ((existingUser != null) && (existingUser.length() > 0)) {
      JobDomainPeasTrappedException te = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.createUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_LOGIN_ALREADY_USED");
      te.setGoBackPage("displayUserCreate");
      throw te;
    }

    theNewUser.setId("-1");
    if ((m_TargetDomainId != null) && (m_TargetDomainId.equals("-1") == false)
        && (m_TargetDomainId.length() > 0)) {
      theNewUser.setDomainId(m_TargetDomainId);
    }
    theNewUser.setLogin(userLogin);
    theNewUser.setLastName(userLastName);
    theNewUser.setFirstName(userFirstName);
    theNewUser.seteMail(userEMail);
    theNewUser.setAccessLevel(userAccessLevel);
    idRet = m_AdminCtrl.addUser(theNewUser);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.createUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_USER");
    }
    refresh();
    setTargetUser(idRet);

    // Update UserFull informations
    UserFull uf = getTargetUserFull();
    if (uf != null) {
      if (uf.isPasswordAvailable()) {
        uf.setPasswordValid(userPasswordValid);
        uf.setPassword(userPassword);
      }

      // process extra properties
      Set keys = properties.keySet();
      Iterator iKeys = keys.iterator();
      String key = null;
      String value = null;
      while (iKeys.hasNext()) {
        key = (String) iKeys.next();
        value = (String) properties.get(key);

        uf.setValue(key, value);
      }

      idRet = m_AdminCtrl.updateUserFull(uf);
      if ((idRet == null) || (idRet.length() <= 0)) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.createUser()",
            SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER");
      }
    }

    // regroupement de l'utilisateur dans un groupe
    regroupInGroup(properties, null);
  }

  /**
   * Regroupement éventuel de l'utilisateur dans un groupe (pour les domaines
   * SQL)
   * 
   * @throws JobDomainPeasException
   * 
   */
  private void regroupInGroup(HashMap properties, String lastGroupId)
      throws JobDomainPeasException {

    // Traitement du domaine SQL
    if (!"-1".equals(getTargetDomain().getId())
        && !"0".equals(getTargetDomain().getId())
        && getTargetDomain().getDriverClassName().equals(
            "com.stratelia.silverpeas.domains.sqldriver.SQLDriver")) {

      ResourceLocator specificRs = new ResourceLocator(getTargetDomain()
          .getPropFileName(), "");
      int numPropertyRegroup = SilverpeasSettings.readInt(specificRs,
          "property.Grouping", -1);
      String nomRegroup = null;
      String theUserIdToRegroup = m_TargetUserId;
      String[] newUserIds;
      List lUserIds;
      List lNewUserIds;
      if (numPropertyRegroup > -1) {
        String nomPropertyRegroupement = SilverpeasSettings.readString(
            specificRs, "property_" + numPropertyRegroup + ".Name", null);

        if (nomPropertyRegroupement != null) {
          // Suppression de l'appartenance de l'utilisateur au groupe auquel il
          // appartenait
          if (lastGroupId != null) {
            Group lastGroup = m_AdminCtrl.getGroupById(lastGroupId);
            lUserIds = Arrays.asList((String[]) lastGroup.getUserIds());
            lNewUserIds = new ArrayList(lUserIds);
            lNewUserIds.remove(theUserIdToRegroup);
            newUserIds = (String[]) lNewUserIds.toArray(new String[lNewUserIds
                .size()]);

            updateGroupSubUsers(lastGroupId, newUserIds);
          }

          // Recherche du nom du regroupement (nom du groupe)
          Set keys = properties.keySet();
          Iterator iKeys = keys.iterator();
          String key = null;
          String value = null;
          boolean trouve = false;
          while (iKeys.hasNext()) {
            key = (String) iKeys.next();
            value = (String) properties.get(key);

            if (key.equals(nomPropertyRegroupement)) {
              trouve = true;
              break;
            }
          }

          if (trouve) {
            nomRegroup = value;
          }
        }
      }

      if (nomRegroup != null && nomRegroup.length() > 0) {
        // Recherche le groupe dans le domaine
        Group group = m_AdminCtrl.getGroupByNameInDomain(nomRegroup,
            m_TargetDomainId);

        if (group == null) {
          // le groupe n'existe pas, on le crée
          group = new Group();
          group.setId("-1");
          group.setDomainId(m_TargetDomainId);
          group.setSuperGroupId(null); // groupe à la racine
          group.setName(nomRegroup);
          group.setDescription("");
          String groupId = m_AdminCtrl.addGroup(group);
          group = m_AdminCtrl.getGroupById(groupId);
        }

        lUserIds = Arrays.asList((String[]) group.getUserIds());
        lNewUserIds = new ArrayList(lUserIds);
        lNewUserIds.add(theUserIdToRegroup);
        newUserIds = (String[]) lNewUserIds.toArray(new String[lNewUserIds
            .size()]);

        // Ajout de l'appartenance de l'utilisateur au groupe
        updateGroupSubUsers(group.getId(), newUserIds);
      }
    }
  }

  /**
   * parse le fichier CSV
   * 
   * @param InputStream
   */
  public void importCsvUsers(FileItem filePart) throws UtilTrappedException,
      JobDomainPeasTrappedException, JobDomainPeasException {
    InputStream is;
    try {
      is = filePart.getInputStream();
    } catch (IOException e) {
      JobDomainPeasTrappedException jdpe = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.importCsvUsers",
          SilverpeasException.ERROR, "jobDomainPeas.EX_CSV_FILE", e);
      jdpe.setGoBackPage("displayUsersCsvImport");
      throw jdpe;
    }
    CSVReader csvReader = new CSVReader(getLanguage());
    csvReader.initCSVFormat(
        "com.silverpeas.jobDomainPeas.settings.usersCSVFormat", "User", ";",
        getTargetDomain().getPropFileName(), "property_");

    // spécifique domaine Silverpeas (2 colonnes en moins (password et
    // passwordValid)
    if ("-1".equals(getTargetDomain().getId())
        || "0".equals(getTargetDomain().getId())) {// domaine Silverpeas
      csvReader.setM_specificNbCols(csvReader.getM_specificNbCols() - 2);
    }

    Variant[][] csvValues;
    try {
      csvValues = csvReader.parseStream(is);
    } catch (UtilTrappedException ute) {
      ute.setGoBackPage("displayUsersCsvImport");
      throw ute;
    }

    StringBuffer listErrors = new StringBuffer("");
    String nom;
    String prenom;
    String login;
    String existingLogin;
    String email;
    String droits;
    String userAccessLevel;
    String motDePasse;

    String title;
    String company;
    String position;
    String boss;
    String phone;
    String homePhone;
    String fax;
    String cellularPhone;
    String address;

    String informationSpecifiqueString;
    boolean informationSpecifiqueBoolean;

    for (int i = 0; i < csvValues.length; i++) {
      // Nom
      nom = csvValues[i][0].getValueString();
      if (nom.length() == 0) {// champ obligatoire
        listErrors.append(getString("JDP.ligne") + " = "
            + Integer.toString(i + 1) + ", ");
        listErrors.append(getString("JDP.colonne") + " = 1, ");
        listErrors.append(getString("JDP.valeur") + " = " + nom + ", ");
        listErrors.append(getString("JDP.obligatoire") + "<BR>");
      } else if (nom.length() > 100) {// verifier 100 char max
        listErrors.append(getString("JDP.ligne") + " = "
            + Integer.toString(i + 1) + ", ");
        listErrors.append(getString("JDP.colonne") + " = 1, ");
        listErrors.append(getString("JDP.valeur") + " = " + nom + ", ");
        listErrors.append(getString("JDP.nbCarMax") + " 100 "
            + getString("JDP.caracteres") + "<BR>");
      }

      // Prenom
      prenom = csvValues[i][1].getValueString(); // verifier 100 char max
      if (prenom.length() > 100) {
        listErrors.append(getString("JDP.ligne") + " = "
            + Integer.toString(i + 1) + ", ");
        listErrors.append(getString("JDP.colonne") + " = 2, ");
        listErrors.append(getString("JDP.valeur") + " = " + prenom + ", ");
        listErrors.append(getString("JDP.nbCarMax") + " 100 "
            + getString("JDP.caracteres") + "<BR>");
      }

      // Login
      login = csvValues[i][2].getValueString();
      if (login.length() == 0) {// champ obligatoire
        listErrors.append(getString("JDP.ligne") + " = "
            + Integer.toString(i + 1) + ", ");
        listErrors.append(getString("JDP.colonne") + " = 3, ");
        listErrors.append(getString("JDP.valeur") + " = " + login + ", ");
        listErrors.append(getString("JDP.obligatoire") + "<BR>");
      } else if (login.length() < JobDomainSettings.m_MinLengthLogin) {// verifier
        // jobDomainPeasSettings.getString("minLengthLogin")
        // char
        // min
        listErrors.append(getString("JDP.ligne") + " = "
            + Integer.toString(i + 1) + ", ");
        listErrors.append(getString("JDP.colonne") + " = 3, ");
        listErrors.append(getString("JDP.valeur") + " = " + login + ", ");
        listErrors.append(getString("JDP.nbCarMin") + " "
            + JobDomainSettings.m_MinLengthLogin + " "
            + getString("JDP.caracteres") + "<BR>");
      } else if (login.length() > 20) {// verifier 20 char max
        listErrors.append(getString("JDP.ligne") + " = "
            + Integer.toString(i + 1) + ", ");
        listErrors.append(getString("JDP.colonne") + " = 3, ");
        listErrors.append(getString("JDP.valeur") + " = " + login + ", ");
        listErrors.append(getString("JDP.nbCarMax") + " 20 "
            + getString("JDP.caracteres") + "<BR>");
      } else {// verif login unique
        existingLogin = m_AdminCtrl.getUserIdByLoginAndDomain(login,
            m_TargetDomainId);
        if (existingLogin != null) {
          listErrors.append(getString("JDP.ligne") + " = "
              + Integer.toString(i + 1) + ", ");
          listErrors.append(getString("JDP.colonne") + " = 3, ");
          listErrors.append(getString("JDP.valeur") + " = " + login + ", ");
          listErrors.append(getString("JDP.existingLogin") + "<BR>");
        }
      }

      // Email
      email = csvValues[i][3].getValueString(); // verifier 100 char max
      if (email.length() > 100) {
        listErrors.append(getString("JDP.ligne") + " = "
            + Integer.toString(i + 1) + ", ");
        listErrors.append(getString("JDP.colonne") + " = 4, ");
        listErrors.append(getString("JDP.valeur") + " = " + email + ", ");
        listErrors.append(getString("JDP.nbCarMax") + " 100 "
            + getString("JDP.caracteres") + "<BR>");
      }

      // Droits
      droits = csvValues[i][4].getValueString();
      if (!"".equals(droits) && !"Admin".equals(droits)
          && !"AdminPdc".equals(droits) && !"AdminDomain".equals(droits)
          && !"User".equals(droits) && !"Guest".equals(droits)) {
        listErrors.append(getString("JDP.ligne") + " = "
            + Integer.toString(i + 1) + ", ");
        listErrors.append(getString("JDP.colonne") + " = 5, ");
        listErrors.append(getString("JDP.valeur") + " = " + droits + ", ");
        listErrors.append(getString("JDP.valeursPossibles") + "<BR>");
      }

      // MotDePasse
      motDePasse = csvValues[i][5].getValueString();
      if (!JobDomainSettings.m_BlanksAllowedInPwd
          && motDePasse.indexOf(" ") != -1) {
        listErrors.append(getString("JDP.ligne") + " = "
            + Integer.toString(i + 1) + ", ");
        listErrors.append(getString("JDP.colonne") + " = 6, ");
        listErrors.append(getString("JDP.valeur") + " = " + motDePasse + ", ");
        listErrors.append(getString("JDP.espaces") + "<BR>");
      } else if (motDePasse.length() < JobDomainSettings.m_MinLengthPwd) {// verifier
        // jobDomainPeasSettings.getString("minLengthPwd")
        // char
        // min
        listErrors.append(getString("JDP.ligne") + " = "
            + Integer.toString(i + 1) + ", ");
        listErrors.append(getString("JDP.colonne") + " = 6, ");
        listErrors.append(getString("JDP.valeur") + " = " + motDePasse + ", ");
        listErrors.append(getString("JDP.nbCarMin") + " "
            + JobDomainSettings.m_MinLengthPwd + " "
            + getString("JDP.caracteres") + "<BR>");
      } else if (motDePasse.length() > 32) {// verifier 32 char max
        listErrors.append(getString("JDP.ligne") + " = "
            + Integer.toString(i + 1) + ", ");
        listErrors.append(getString("JDP.colonne") + " = 6, ");
        listErrors.append(getString("JDP.valeur") + " = " + motDePasse + ", ");
        listErrors.append(getString("JDP.nbCarMax") + " 32 "
            + getString("JDP.caracteres") + "<BR>");
      }

      if (csvReader.getM_specificNbCols() > 0) {
        if ("-1".equals(getTargetDomain().getId())
            || "0".equals(getTargetDomain().getId())) {// domaine Silverpeas

          // title
          title = csvValues[i][6].getValueString(); // verifier 100 char max
          if (title.length() > 100) {
            listErrors.append(getString("JDP.ligne") + " = "
                + Integer.toString(i + 1) + ", ");
            listErrors.append(getString("JDP.colonne") + " = 7, ");
            listErrors.append(getString("JDP.valeur") + " = " + title + ", ");
            listErrors.append(getString("JDP.nbCarMax") + " 100 "
                + getString("JDP.caracteres") + "<BR>");
          }

          // company
          company = csvValues[i][7].getValueString(); // verifier 100 char max
          if (company.length() > 100) {
            listErrors.append(getString("JDP.ligne") + " = "
                + Integer.toString(i + 1) + ", ");
            listErrors.append(getString("JDP.colonne") + " = 8, ");
            listErrors.append(getString("JDP.valeur") + " = " + company + ", ");
            listErrors.append(getString("JDP.nbCarMax") + " 100 "
                + getString("JDP.caracteres") + "<BR>");
          }

          // position
          position = csvValues[i][8].getValueString(); // verifier 100 char max
          if (position.length() > 100) {
            listErrors.append(getString("JDP.ligne") + " = "
                + Integer.toString(i + 1) + ", ");
            listErrors.append(getString("JDP.colonne") + " = 9, ");
            listErrors
                .append(getString("JDP.valeur") + " = " + position + ", ");
            listErrors.append(getString("JDP.nbCarMax") + " 100 "
                + getString("JDP.caracteres") + "<BR>");
          }

          // boss
          boss = csvValues[i][9].getValueString(); // verifier 100 char max
          if (boss.length() > 100) {
            listErrors.append(getString("JDP.ligne") + " = "
                + Integer.toString(i + 1) + ", ");
            listErrors.append(getString("JDP.colonne") + " = 10, ");
            listErrors.append(getString("JDP.valeur") + " = " + boss + ", ");
            listErrors.append(getString("JDP.nbCarMax") + " 100 "
                + getString("JDP.caracteres") + "<BR>");
          }

          // phone
          phone = csvValues[i][10].getValueString(); // verifier 20 char max
          if (phone.length() > 20) {
            listErrors.append(getString("JDP.ligne") + " = "
                + Integer.toString(i + 1) + ", ");
            listErrors.append(getString("JDP.colonne") + " = 11, ");
            listErrors.append(getString("JDP.valeur") + " = " + phone + ", ");
            listErrors.append(getString("JDP.nbCarMax") + " 20 "
                + getString("JDP.caracteres") + "<BR>");
          }

          // homePhone
          homePhone = csvValues[i][11].getValueString(); // verifier 20 char max
          if (homePhone.length() > 20) {
            listErrors.append(getString("JDP.ligne") + " = "
                + Integer.toString(i + 1) + ", ");
            listErrors.append(getString("JDP.colonne") + " = 12, ");
            listErrors.append(getString("JDP.valeur") + " = " + homePhone
                + ", ");
            listErrors.append(getString("JDP.nbCarMax") + " 20 "
                + getString("JDP.caracteres") + "<BR>");
          }

          // fax
          fax = csvValues[i][12].getValueString(); // verifier 20 char max
          if (fax.length() > 20) {
            listErrors.append(getString("JDP.ligne") + " = "
                + Integer.toString(i + 1) + ", ");
            listErrors.append(getString("JDP.colonne") + " = 13, ");
            listErrors.append(getString("JDP.valeur") + " = " + fax + ", ");
            listErrors.append(getString("JDP.nbCarMax") + " 20 "
                + getString("JDP.caracteres") + "<BR>");
          }

          // cellularPhone
          cellularPhone = csvValues[i][13].getValueString(); // verifier 20 char
          // max
          if (cellularPhone.length() > 20) {
            listErrors.append(getString("JDP.ligne") + " = "
                + Integer.toString(i + 1) + ", ");
            listErrors.append(getString("JDP.colonne") + " = 14, ");
            listErrors.append(getString("JDP.valeur") + " = " + cellularPhone
                + ", ");
            listErrors.append(getString("JDP.nbCarMax") + " 20 "
                + getString("JDP.caracteres") + "<BR>");
          }

          // address
          address = csvValues[i][14].getValueString(); // verifier 500 char max
          if (address.length() > 500) {
            listErrors.append(getString("JDP.ligne") + " = "
                + Integer.toString(i + 1) + ", ");
            listErrors.append(getString("JDP.colonne") + " = 15, ");
            listErrors.append(getString("JDP.valeur") + " = " + address + ", ");
            listErrors.append(getString("JDP.nbCarMax") + " 500 "
                + getString("JDP.caracteres") + "<BR>");
          }
        } else {// domaine SQL

          // informations spécifiques
          for (int j = 0; j < csvReader.getM_specificNbCols(); j++) {
            if (Variant.TYPE_STRING.equals(csvReader.getM_specificColType(j))) {
              informationSpecifiqueString = csvValues[i][j + 6]
                  .getValueString(); // verifier 50 char max
              if (informationSpecifiqueString.length() > 50) {
                listErrors.append(getString("JDP.ligne") + " = "
                    + Integer.toString(i + 1) + ", ");
                listErrors.append(getString("JDP.colonne") + " = " + j + 6
                    + ", ");
                listErrors.append(getString("JDP.valeur") + " = "
                    + informationSpecifiqueString + ", ");
                listErrors.append(getString("JDP.nbCarMax") + " 50 "
                    + getString("JDP.caracteres") + "<BR>");
              }
            }
          }
        }
      }
    }

    if (listErrors.length() > 0) {
      JobDomainPeasTrappedException jdpe = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.importCsvUsers",
          SilverpeasException.ERROR, "jobDomainPeas.EX_CSV_FILE", listErrors
              .toString());
      jdpe.setGoBackPage("displayUsersCsvImport");
      throw jdpe;
    }

    // pas d'erreur, on importe les utilisateurs
    HashMap properties;
    for (int i = 0; i < csvValues.length; i++) {
      // Nom
      nom = csvValues[i][0].getValueString();

      // Prenom
      prenom = csvValues[i][1].getValueString();

      // Login
      login = csvValues[i][2].getValueString();

      // Email
      email = csvValues[i][3].getValueString();

      // Droits
      droits = csvValues[i][4].getValueString();
      if ("Admin".equals(droits)) {
        userAccessLevel = "A";
      } else if ("AdminPdc".equals(droits)) {
        userAccessLevel = "K";
      } else if ("AdminDomain".equals(droits)) {
        userAccessLevel = "D";
      } else if ("User".equals(droits)) {
        userAccessLevel = "U";
      } else if ("Guest".equals(droits)) {
        userAccessLevel = "G";
      } else {
        userAccessLevel = "U";
      }

      // MotDePasse
      motDePasse = csvValues[i][5].getValueString();

      // données spécifiques
      properties = new HashMap();
      if (csvReader.getM_specificNbCols() > 0) {
        if ("-1".equals(getTargetDomain().getId())
            || "0".equals(getTargetDomain().getId())) {// domaine Silverpeas

          // title
          title = csvValues[i][6].getValueString();
          properties.put(csvReader.getM_specificParameterName(0), title);

          // company
          company = csvValues[i][7].getValueString();
          properties.put(csvReader.getM_specificParameterName(1), company);

          // position
          position = csvValues[i][8].getValueString();
          properties.put(csvReader.getM_specificParameterName(2), position);

          // boss
          boss = csvValues[i][9].getValueString();
          properties.put(csvReader.getM_specificParameterName(3), boss);

          // phone
          phone = csvValues[i][10].getValueString();
          properties.put(csvReader.getM_specificParameterName(4), phone);

          // homePhone
          homePhone = csvValues[i][11].getValueString();
          properties.put(csvReader.getM_specificParameterName(5), homePhone);

          // fax
          fax = csvValues[i][12].getValueString();
          properties.put(csvReader.getM_specificParameterName(6), fax);

          // cellularPhone
          cellularPhone = csvValues[i][13].getValueString();
          properties
              .put(csvReader.getM_specificParameterName(7), cellularPhone);

          // address
          address = csvValues[i][14].getValueString();
          properties.put(csvReader.getM_specificParameterName(8), address);

        } else {// domaine SQL

          // informations spécifiques
          for (int j = 0; j < csvReader.getM_specificNbCols(); j++) {
            if (Variant.TYPE_STRING.equals(csvReader.getM_specificColType(j))) {
              informationSpecifiqueString = csvValues[i][j + 6]
                  .getValueString();
              properties.put(csvReader.getM_specificParameterName(j),
                  informationSpecifiqueString);
            } else if (Variant.TYPE_BOOLEAN.equals(csvReader
                .getM_specificColType(j))) {
              informationSpecifiqueBoolean = csvValues[i][j + 6]
                  .getValueBoolean();
              if (informationSpecifiqueBoolean)
                properties.put(csvReader.getM_specificParameterName(j), "1");
              else
                properties.put(csvReader.getM_specificParameterName(j), "0");
            }
          }
        }
      }

      createUser(login, nom, prenom, email, userAccessLevel, true, motDePasse,
          properties); // l'id User créé est dans m_TargetUserId

    }
  }

  private String getLastGroupId(UserFull theUser) {
    // Traitement du domaine SQL
    if (!"-1".equals(getTargetDomain().getId())
        && !"0".equals(getTargetDomain().getId())
        && getTargetDomain().getDriverClassName().equals(
            "com.stratelia.silverpeas.domains.sqldriver.SQLDriver")) {
      ResourceLocator specificRs = new ResourceLocator(getTargetDomain()
          .getPropFileName(), "");
      int numPropertyRegroup = SilverpeasSettings.readInt(specificRs,
          "property.Grouping", -1);
      String nomLastGroup = null;
      if (numPropertyRegroup > -1) {
        String nomPropertyRegroupement = SilverpeasSettings.readString(
            specificRs, "property_" + numPropertyRegroup + ".Name", null);
        if (nomPropertyRegroupement != null) {
          // Recherche du nom du regroupement (nom du groupe)
          String[] keys = theUser.getPropertiesNames();
          String key = null;
          String value = null;
          boolean trouve = false;
          for (int i = 0; i < keys.length; i++) {
            key = keys[i];
            value = theUser.getValue(key);

            if (key.equals(nomPropertyRegroupement)) {
              trouve = true;
              break;
            }
          }

          if (trouve) {
            nomLastGroup = value;
          }
        }
      }

      if (nomLastGroup != null && nomLastGroup.length() > 0) {
        // Recherche le groupe dans le domaine
        Group group = m_AdminCtrl.getGroupByNameInDomain(nomLastGroup,
            m_TargetDomainId);
        if (group != null) {
          return group.getId();
        }
      }
    }

    return null;
  }

  public void modifyUser(String idUser, String userLastName,
      String userFirstName, String userEMail, String userAccessLevel,
      boolean userPasswordValid, String userPassword, HashMap properties)
      throws JobDomainPeasException {
    UserFull theModifiedUser = null;
    String idRet = null;

    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.modifyUser()",
        "root.MSG_GEN_ENTER_METHOD", "UserId=" + idUser + " userLastName="
            + userLastName + " userFirstName=" + userFirstName + " userEMail="
            + userEMail + " userAccessLevel=" + userAccessLevel);

    theModifiedUser = m_AdminCtrl.getUserFull(idUser);
    if (theModifiedUser == null) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifyUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_USER");
    }

    // nom du groupe auquel était rattaché l'utilisateur
    String lastGroupId = getLastGroupId(theModifiedUser);

    theModifiedUser.setLastName(userLastName);
    theModifiedUser.setFirstName(userFirstName);
    theModifiedUser.seteMail(userEMail);
    theModifiedUser.setAccessLevel(userAccessLevel);
    if (theModifiedUser.isPasswordAvailable()) {
      theModifiedUser.setPasswordValid(userPasswordValid);
      theModifiedUser.setPassword(userPassword);
    }

    // process extra properties
    Set keys = properties.keySet();
    Iterator iKeys = keys.iterator();
    String key = null;
    String value = null;
    while (iKeys.hasNext()) {
      key = (String) iKeys.next();
      value = (String) properties.get(key);

      theModifiedUser.setValue(key, value);
    }

    idRet = m_AdminCtrl.updateUserFull(theModifiedUser);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifyUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", "UserId="
              + idUser);
    }
    refresh();
    setTargetUser(idRet);

    // regroupement de l'utilisateur dans un groupe
    regroupInGroup(properties, lastGroupId);
  }

  public void modifySynchronizedUser(String idUser, String userAccessLevel)
      throws JobDomainPeasException {
    UserDetail theModifiedUser = null;
    String idRet = null;

    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.modifySynchronizedUser()",
        "root.MSG_GEN_ENTER_METHOD", "UserId=" + idUser);

    theModifiedUser = m_AdminCtrl.getUserDetail(idUser);
    if (theModifiedUser == null) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifySynchronizedUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_USER");
    }
    theModifiedUser.setAccessLevel(userAccessLevel);
    idRet = m_AdminCtrl.updateSynchronizedUser(theModifiedUser);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifySynchronizedUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", "UserId="
              + idUser);
    }
    refresh();
    setTargetUser(idRet);
  }

  public void deleteUser(String idUser) throws JobDomainPeasException {
    String idRet = null;

    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.deleteUser()",
        "root.MSG_GEN_ENTER_METHOD", "UserId=" + idUser);

    UserDetail user = getUserDetail(idUser);

    idRet = m_AdminCtrl.deleteUser(idUser);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.deleteUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_USER", "UserId="
              + idUser);
    }
    if (m_TargetUserId.equals(idUser)) {
      m_TargetUserId = null;
    }

    if ((getDomainActions() & AbstractDomainDriver.ACTION_X509_USER) != 0) {
      // revocate user's certificate
      revocateCertificate(user);
    }

    refresh();
  }

  public Iterator getPropertiesToImport() throws JobDomainPeasException {
    return m_AdminCtrl.getSpecificPropertiesToImportUsers(m_TargetDomainId,
        getLanguage()).iterator();
  }

  public void importUser(String userLogin) throws JobDomainPeasException {
    String idRet = null;

    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.importUser()",
        "root.MSG_GEN_ENTER_METHOD", "userLogin=" + userLogin);

    idRet = m_AdminCtrl.synchronizeImportUser(m_TargetDomainId, userLogin);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.importUser()",
          SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_USER",
          "userLogin=" + userLogin);
    }
    refresh();
    setTargetUser(idRet);
  }

  public void importUsers(String[] specificIds) throws JobDomainPeasException {
    for (int i = 0; specificIds != null && i < specificIds.length; i++) {
      SilverTrace.info("jobDomainPeas",
          "JobDomainPeasSessionController.importUsers()",
          "root.MSG_GEN_ENTER_METHOD", "specificId=" + specificIds[i]);

      String idRet = m_AdminCtrl.synchronizeImportUser(m_TargetDomainId,
          specificIds[i]);
      if (!StringUtil.isDefined(idRet)) {
        // throw new
        // JobDomainPeasException("JobDomainPeasSessionController.importUser()",SilverpeasException.ERROR,"admin.MSG_ERR_SYNCHRONIZE_USER",
        // "specificId="+specificIds[i]);
      }
    }
    refresh();
  }

  public List searchUsers(Hashtable query) throws JobDomainPeasException {
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.searchUsers()",
        "root.MSG_GEN_ENTER_METHOD", "query=" + query.toString());

    // UserDetail[] existingUsers =
    // m_AdminCtrl.getUsersOfDomain(m_TargetDomainId);

    queryToImport = query;
    usersToImport = m_AdminCtrl.searchUsers(m_TargetDomainId, query);

    return usersToImport;
  }

  public List getUsersToImport() {
    return usersToImport;
  }

  public Hashtable getQueryToImport() {
    return queryToImport;
  }

  public UserFull getUser(String specificId) {
    return m_AdminCtrl.getUserFull(m_TargetDomainId, specificId);
  }

  public void synchroUser(String idUser) throws JobDomainPeasException {
    String idRet = null;

    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.synchroUser()",
        "root.MSG_GEN_ENTER_METHOD", "UserId=" + idUser);

    idRet = m_AdminCtrl.synchronizeUser(idUser);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.synchroUser()",
          SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_USER");
    }
    refresh();
    setTargetUser(idRet);
  }

  public void unsynchroUser(String idUser) throws JobDomainPeasException {
    String idRet = null;

    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.unsynchroUser()",
        "root.MSG_GEN_ENTER_METHOD", "UserId=" + idUser);

    idRet = m_AdminCtrl.synchronizeRemoveUser(idUser);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.unsynchroUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_USER");
    }
    if (m_TargetUserId.equals(idUser)) {
      m_TargetUserId = null;
    }
    refresh();
  }

  /*
   * 
   * GROUP functions
   */
  public void returnIntoGroup(String groupId) throws JobDomainPeasException {
    int i = 0;

    if (!StringUtil.isDefined(groupId)) {
      m_GroupsPath.clear();
    } else {
      i = m_GroupsPath.size() - 1;
      while ((i >= 0)
          && (((GroupNavigationStock) m_GroupsPath.get(i)).isThisGroup(groupId) == false)) {
        m_GroupsPath.removeElementAt(i);
        i--;
      }
    }
    setTargetUser(null);
  }

  public void removeGroupFromPath(String groupId) throws JobDomainPeasException {
    int i = 0;

    if (StringUtil.isDefined(groupId)) {
      i = 0;
      while ((i < m_GroupsPath.size())
          && (((GroupNavigationStock) m_GroupsPath.get(i)).isThisGroup(groupId) == false)) {
        i++;
      }
      if (i < m_GroupsPath.size()) {
        m_GroupsPath.setSize(i); // Tunc the vector
      }
    }
  }

  public void goIntoGroup(String groupId) throws JobDomainPeasException {
    if (StringUtil.isDefined(groupId)) {
      if (getTargetGroup() == null
          || (getTargetGroup() != null && !getTargetGroup().getId().equals(
              groupId))) {
        Group targetGroup = m_AdminCtrl.getGroupById(groupId);
        if (GroupNavigationStock.isGroupValid(targetGroup)) {
          List manageableGroupIds = null;
          if (isOnlyGroupManager() && !isGroupManagerOnGroup(groupId))
            manageableGroupIds = getUserManageableGroupIds();
          GroupNavigationStock newSubGroup = new GroupNavigationStock(groupId,
              m_AdminCtrl, manageableGroupIds);
          m_GroupsPath.add(newSubGroup);
        }
      }
    } else {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.setTargetGroup()",
          SilverpeasException.ERROR, "jobDomainPeas.EX_GROUP_NOT_AVAILABLE",
          "GroupId=" + groupId);
    }
    setTargetUser(null);
  }

  public Group getTargetGroup() throws JobDomainPeasException {
    if (m_GroupsPath.size() <= 0) {
      return null;
    }
    return (((GroupNavigationStock) m_GroupsPath.lastElement()).getThisGroup());
  }

  /**
   * @return a List with 2 elements. First one, a List of UserDetail. Last one,
   *         a List of Group.
   * @throws JobDomainPeasException
   */
  public List getGroupManagers() throws JobDomainPeasException {
    List usersAndGroups = new ArrayList();
    List users = new ArrayList();
    List groups = new ArrayList();

    GroupProfileInst profile = m_AdminCtrl.getGroupProfile(getTargetGroup()
        .getId());

    if (profile != null) {
      List groupIds = profile.getAllGroups();
      Group group = null;
      for (int nI = 0; nI < groupIds.size(); nI++) {
        group = m_AdminCtrl.getGroupById((String) groupIds.get(nI));
        groups.add(group);
      }

      List userIds = profile.getAllUsers();
      UserDetail user = null;
      for (int nI = 0; nI < userIds.size(); nI++) {
        user = getUserDetail((String) userIds.get(nI));
        users.add(user);
      }
    }

    usersAndGroups.add(users);
    usersAndGroups.add(groups);

    return usersAndGroups;
  }

  // user panel de selection de n groupes et n users
  public void initUserPanelForGroupManagers(String compoURL)
      throws SelectionException, JobDomainPeasException {
    sel.resetAll();

    sel.setHostSpaceName(getMultilang().getString("JDP.jobDomain"));
    sel.setHostComponentName(new PairObject(getTargetGroup().getName(), null));

    ResourceLocator generalMessage = GeneralPropertiesManager
        .getGeneralMultilang(getLanguage());
    PairObject[] hostPath = { new PairObject(getMultilang().getString(
        "JDP.roleManager")
        + " > " + generalMessage.getString("GML.selection"), null) };
    sel.setHostPath(hostPath);

    sel.setGoBackURL(compoURL + "groupManagersUpdate");
    sel.setCancelURL(compoURL + "groupManagersCancel");

    GroupProfileInst profile = m_AdminCtrl.getGroupProfile(getTargetGroup()
        .getId());

    sel.setSelectedElements((String[]) profile.getAllUsers().toArray(
        new String[0]));
    sel.setSelectedSets((String[]) profile.getAllGroups()
        .toArray(new String[0]));
  }

  public void updateGroupProfile() throws JobDomainPeasException {
    GroupProfileInst profile = m_AdminCtrl.getGroupProfile(getTargetGroup()
        .getId());

    profile.removeAllGroups();
    profile.removeAllUsers();

    setGroupsAndUsers(profile, sel.getSelectedSets(), sel.getSelectedElements());

    m_AdminCtrl.updateGroupProfile(profile);
  }

  public void deleteGroupProfile() throws JobDomainPeasException {
    m_AdminCtrl.deleteGroupProfile(getTargetGroup().getId());
  }

  private void setGroupsAndUsers(GroupProfileInst profile, String[] groupIds,
      String[] userIds) {
    // groups
    for (int i = 0; groupIds != null && i < groupIds.length; i++) {
      if (groupIds[i] != null && groupIds[i].length() > 0) {
        profile.addGroup(groupIds[i]);
      }
    }

    // users
    for (int i = 0; userIds != null && i < userIds.length; i++) {
      if (userIds[i] != null && userIds[i].length() > 0) {
        profile.addUser(userIds[i]);
      }
    }
  }

  public boolean isGroupRoot(String groupId) throws JobDomainPeasException {
    Group gr = m_AdminCtrl.getGroupById(groupId);

    if (GroupNavigationStock.isGroupValid(gr)) {
      if ((gr.getSuperGroupId() == null)
          || (gr.getSuperGroupId().length() <= 0)
          || (gr.getSuperGroupId().equals("-1"))) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public Group[] getSubGroups(boolean isParentGroup)
      throws JobDomainPeasException {
    Group[] groups = null;

    if (isParentGroup) {
      if (m_GroupsPath.size() <= 0) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.getTargetGroup()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_GROUP_NOT_AVAILABLE");
      }
      groups = ((GroupNavigationStock) m_GroupsPath.lastElement())
          .getGroupPage();
    } else { // Domain case
      groups = m_TargetDomain.getGroupPage();
    }

    // if (isOnlyGroupManager())
    if (isOnlyGroupManager() && !isGroupManagerOnCurrentGroup()) {
      groups = filterGroupsToGroupManager(groups);
    }

    for (int i = 0; i < groups.length; i++) {
      if (groups[i] != null) {
        groups[i].setNbUsers(getOrganizationController().getAllSubUsersNumber(
            groups[i].getId()));
      }
    }
    return groups;
  }

  public String[][] getSubUsers(boolean isParentGroup)
      throws JobDomainPeasException {
    UserDetail[] usDetails = null;
    String[][] valret = null;
    int i = 0;

    if (isParentGroup) {
      if (m_GroupsPath.size() <= 0) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.getTargetGroup()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_GROUP_NOT_AVAILABLE");
      }
      usDetails = ((GroupNavigationStock) m_GroupsPath.lastElement())
          .getUserPage();
    } else { // Domain case
      usDetails = m_TargetDomain.getUserPage();
    }
    valret = new String[usDetails.length][4];
    for (i = 0; i < usDetails.length; i++) {
      if (usDetails[i] != null) {
        valret[i][0] = getSureString(usDetails[i].getId());
        valret[i][1] = EncodeHelper
            .javaStringToHtmlString(getSureString(usDetails[i].getLastName()));
        valret[i][2] = EncodeHelper
            .javaStringToHtmlString(getSureString(usDetails[i].getFirstName()));
        valret[i][3] = EncodeHelper
            .javaStringToHtmlString(getSureString(usDetails[i].getLogin()));
      } else {
        valret[i][0] = "";
        valret[i][1] = "";
        valret[i][2] = "";
        valret[i][3] = "";
      }
    }
    return valret;
  }

  public boolean[] getPageNavigation(boolean isParentGroup) {
    boolean[] valret = new boolean[4];
    if (isParentGroup) {
      if (m_GroupsPath.size() > 0) {
        GroupNavigationStock theGroupStock = ((GroupNavigationStock) m_GroupsPath
            .lastElement());

        valret[0] = (!theGroupStock.isFirstGroupPage());
        valret[1] = (!theGroupStock.isLastGroupPage());
        valret[2] = (!theGroupStock.isFirstUserPage());
        valret[3] = (!theGroupStock.isLastUserPage());
      } else {
        valret[0] = false;
        valret[1] = false;
        valret[2] = false;
        valret[3] = false;
      }
    } else {
      if (m_TargetDomain != null) {
        valret[0] = (!m_TargetDomain.isFirstGroupPage());
        valret[1] = (!m_TargetDomain.isLastGroupPage());
        valret[2] = (!m_TargetDomain.isFirstUserPage());
        valret[3] = (!m_TargetDomain.isLastUserPage());
      } else {
        valret[0] = false;
        valret[1] = false;
        valret[2] = false;
        valret[3] = false;
      }
    }
    return valret;
  }

  public void previousGroupPage(boolean isParentGroup)
      throws JobDomainPeasException {
    if (isParentGroup) {
      if (m_GroupsPath.size() <= 0) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.previousGroupPage()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_GROUP_NOT_AVAILABLE");
      }
      ((GroupNavigationStock) m_GroupsPath.lastElement()).previousGroupPage();
    } else {
      if (m_TargetDomain == null) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.previousGroupPage()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_DOMAIN_NOT_AVAILABLE");
      }
      m_TargetDomain.previousGroupPage();
    }
  }

  public void nextGroupPage(boolean isParentGroup)
      throws JobDomainPeasException {
    if (isParentGroup) {
      if (m_GroupsPath.size() <= 0) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.nextGroupPage()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_GROUP_NOT_AVAILABLE");
      }
      ((GroupNavigationStock) m_GroupsPath.lastElement()).nextGroupPage();
    } else {
      if (m_TargetDomain == null) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.nextGroupPage()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_DOMAIN_NOT_AVAILABLE");
      }
      m_TargetDomain.nextGroupPage();
    }
  }

  public void previousUserPage(boolean isParentGroup)
      throws JobDomainPeasException {
    if (isParentGroup) {
      if (m_GroupsPath.size() <= 0) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.previousUserPage()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_GROUP_NOT_AVAILABLE");
      }
      ((GroupNavigationStock) m_GroupsPath.lastElement()).previousUserPage();
    } else {
      if (m_TargetDomain == null) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.previousUserPage()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_DOMAIN_NOT_AVAILABLE");
      }
      m_TargetDomain.previousUserPage();
    }
  }

  public void nextUserPage(boolean isParentGroup) throws JobDomainPeasException {
    if (isParentGroup) {
      if (m_GroupsPath.size() <= 0) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.nextUserPage()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_GROUP_NOT_AVAILABLE");
      }
      ((GroupNavigationStock) m_GroupsPath.lastElement()).nextUserPage();
    } else {
      if (m_TargetDomain == null) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.nextUserPage()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_DOMAIN_NOT_AVAILABLE");
      }
      m_TargetDomain.nextUserPage();
    }
  }

  public String getPath(String baseURL, String toAppendAtEnd)
      throws JobDomainPeasException {
    StringBuffer strPath = new StringBuffer("");
    Group theGroup = null;
    int i;

    for (i = 0; i < m_GroupsPath.size(); i++) {
      theGroup = ((GroupNavigationStock) m_GroupsPath.get(i)).getThisGroup();
      if (strPath.length() > 0) {
        strPath.append(" &gt ");
      }
      if (((i + 1) < m_GroupsPath.size()) || (m_TargetUserId != null)
          || (toAppendAtEnd != null)) {
        strPath.append("<a href=\"" + baseURL + "groupReturn?Idgroup="
            + theGroup.getId() + "\">"
            + EncodeHelper.javaStringToHtmlString(theGroup.getName()) + "</a>");
      } else {
        strPath.append(EncodeHelper.javaStringToHtmlString(theGroup.getName()));
      }
    }
    if (m_TargetUserId != null) {
      if (strPath.length() > 0) {
        strPath.append(" &gt ");
      }
      if (toAppendAtEnd != null) {
        strPath.append("<a href=\""
            + baseURL
            + "userContent?Iduser="
            + m_TargetUserId
            + "\">"
            + EncodeHelper.javaStringToHtmlString(getTargetUserDetail()
                .getDisplayedName()) + "</a>");
      } else {
        strPath.append(EncodeHelper
            .javaStringToHtmlString(getTargetUserDetail().getDisplayedName()));
      }
    }
    if (toAppendAtEnd != null) {
      if (strPath.length() > 0) {
        strPath.append(" &gt ");
      }
      strPath.append(EncodeHelper.javaStringToHtmlString(toAppendAtEnd));
    }
    return strPath.toString();
  }

  public boolean createGroup(String idParent, String groupName,
      String groupDescription, String groupRule) throws JobDomainPeasException {
    Group theNewGroup = new Group();

    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.createGroup()",
        "root.MSG_GEN_ENTER_METHOD", "ParentId=" + idParent + " Name="
            + groupName + " Desc=" + groupDescription);
    theNewGroup.setId("-1");
    if (StringUtil.isDefined(m_TargetDomainId)
        && !m_TargetDomainId.equals("-1")) {
      theNewGroup.setDomainId(m_TargetDomainId);
    }
    theNewGroup.setSuperGroupId(idParent);
    theNewGroup.setName(groupName);
    theNewGroup.setDescription(groupDescription);
    theNewGroup.setRule(groupRule);
    String idRet = m_AdminCtrl.addGroup(theNewGroup);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.createGroup()",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_GROUP");
    }
    refresh();
    return isGroupRoot(idRet);
  }

  public boolean modifyGroup(String idGroup, String groupName,
      String groupDescription, String groupRule) throws JobDomainPeasException {
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.modifyGroup()",
        "root.MSG_GEN_ENTER_METHOD", "GroupId=" + idGroup + " Desc="
            + groupDescription);

    Group theModifiedGroup = m_AdminCtrl.getGroupById(idGroup);
    if (theModifiedGroup == null) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifyGroup()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_GROUP");
    }
    theModifiedGroup.setName(groupName);
    theModifiedGroup.setDescription(groupDescription);
    theModifiedGroup.setRule(groupRule);

    String idRet = m_AdminCtrl.updateGroup(theModifiedGroup);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifyGroup()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_GROUP");
    }
    refresh();
    return isGroupRoot(idRet);
  }

  public boolean updateGroupSubUsers(String idGroup, String[] userIds)
      throws JobDomainPeasException {
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.updateGroupSubUsers()",
        "root.MSG_GEN_ENTER_METHOD", "GroupId=" + idGroup);

    Group theModifiedGroup = m_AdminCtrl.getGroupById(idGroup);
    if (theModifiedGroup == null) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.updateGroupSubUsers()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_GROUP");
    }
    theModifiedGroup.setUserIds(userIds);
    String idRet = m_AdminCtrl.updateGroup(theModifiedGroup);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.updateGroupSubUsers()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_GROUP");
    }
    refresh();
    return false;
  }

  public boolean deleteGroup(String idGroup) throws JobDomainPeasException {
    boolean haveToRefreshDomain = isGroupRoot(idGroup);

    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.deleteGroup()",
        "root.MSG_GEN_ENTER_METHOD", "GroupId=" + idGroup);

    String idRet = m_AdminCtrl.deleteGroupById(idGroup);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.deleteGroup()",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_GROUP");
    }
    removeGroupFromPath(idGroup);
    refresh();
    return haveToRefreshDomain;
  }

  public boolean synchroGroup(String idGroup) throws JobDomainPeasException {
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.synchroGroup()",
        "root.MSG_GEN_ENTER_METHOD", "GroupId=" + idGroup);

    String idRet = m_AdminCtrl.synchronizeGroup(idGroup);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.synchroGroup()",
          SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_GROUP");
    }
    refresh();
    return isGroupRoot(idRet);
  }

  public boolean unsynchroGroup(String idGroup) throws JobDomainPeasException {
    boolean haveToRefreshDomain = isGroupRoot(idGroup);

    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.unsynchroGroup()",
        "root.MSG_GEN_ENTER_METHOD", "GroupId=" + idGroup);

    String idRet = m_AdminCtrl.synchronizeRemoveGroup(idGroup);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.unsynchroGroup()",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_GROUP");
    }
    removeGroupFromPath(idGroup);
    refresh();
    return haveToRefreshDomain;
  }

  public boolean importGroup(String groupName) throws JobDomainPeasException {
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.importGroup()",
        "root.MSG_GEN_ENTER_METHOD", "groupName=" + groupName);

    String idRet = m_AdminCtrl.synchronizeImportGroup(m_TargetDomainId,
        groupName);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.importGroup()",
          SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_GROUP");
    }
    refresh();
    return isGroupRoot(idRet);
  }

  /*
   * 
   * DOMAIN functions
   */

  public void setDefaultTargetDomain() {
    UserDetail ud = getUserDetail();

    if (ud.isDomainAdminRestricted()) {
      setTargetDomain(ud.getDomainId());
    }
  }

  public void setTargetDomain(String domainId) {
    if (!StringUtil.isDefined(domainId)) {
      m_TargetDomain = null;
      m_TargetDomainId = "";
    } else {
      List manageableGroupIds = null;
      if (isOnlyGroupManager())
        manageableGroupIds = getUserManageableGroupIds();

      m_TargetDomain = new DomainNavigationStock(domainId, m_AdminCtrl,
          manageableGroupIds);
      m_TargetDomainId = domainId;
    }
  }

  public Domain getTargetDomain() {
    if (m_TargetDomain == null) {
      return null;
    } else
      return m_TargetDomain.getThisDomain();
  }

  public long getDomainActions() {
    if (m_TargetDomainId.length() > 0) {
      return m_AdminCtrl.getDomainActions(m_TargetDomainId);
    } else {
      return 0;
    }
  }

  public String[][] getAllDomains() {
    String[][] valret = null;
    UserDetail ud = getUserDetail();

    if (ud.isAccessDomainManager()) {
      Domain userDomain = m_AdminCtrl.getDomain(ud.getDomainId());

      valret = new String[1][3];
      valret[0][0] = ud.getDomainId();
      valret[0][1] = EncodeHelper.javaStringToHtmlString(userDomain.getName());
      if (m_TargetDomainId.equals(userDomain.getId())) {
        valret[0][2] = "selected";
      } else {
        valret[0][2] = "";
      }
    } else if (ud.isAccessAdmin()) {
      Domain[] allDomains = m_AdminCtrl.getAllDomains();

      valret = new String[allDomains.length + 1][3];

      // Domaine mixte
      valret[0][0] = "-1";
      valret[0][1] = getString("JDP.domainMixt");
      if (m_TargetDomainId.equals("-1")) {
        valret[0][2] = "selected";
      } else {
        valret[0][2] = "";
      }

      // Tous les domaines
      for (int i = 1; i < valret.length; i++) {
        valret[i][0] = allDomains[i - 1].getId();
        valret[i][1] = EncodeHelper.javaStringToHtmlString(allDomains[i - 1]
            .getName());
        if (m_TargetDomainId.equals(allDomains[i - 1].getId())) {
          valret[i][2] = "selected";
        } else {
          valret[i][2] = "";
        }
      }
    } else if (isOnlyGroupManager()) {
      // Domaine mixte
      valret = new String[2][3];
      valret[0][0] = "-1";
      valret[0][1] = getString("JDP.domainMixt");
      if (m_TargetDomainId.equals("-1"))
        valret[0][2] = "selected";
      else
        valret[0][2] = "";

      // Domaine de l'utilisateur
      Domain userDomain = m_AdminCtrl.getDomain(ud.getDomainId());

      valret[1][0] = userDomain.getId();
      valret[1][1] = EncodeHelper.javaStringToHtmlString(userDomain.getName());
      if (m_TargetDomainId.equals(userDomain.getId()))
        valret[1][2] = "selected";
      else
        valret[1][2] = "";
    }
    return valret;
  }

  public boolean isOnlyGroupManager() {
    return isGroupManager() && !getUserDetail().isAccessAdmin()
        && !getUserDetail().isDomainAdminRestricted();
  }

  public boolean isGroupManagerOnCurrentGroup() throws JobDomainPeasException {
    if (getTargetGroup() != null) {
      return isGroupManagerOnGroup(getTargetGroup().getId());
    }
    return false;
  }

  public boolean isGroupManagerOnGroup(String groupId)
      throws JobDomainPeasException {
    List manageableGroupIds = getUserManageableGroupIds();

    if (manageableGroupIds.contains(groupId)) {
      // Current user is directly manager of group
      return true;
    } else {
      List groupPath = m_AdminCtrl.getPathToGroup(groupId);

      groupPath.retainAll(manageableGroupIds);

      if (groupPath.size() > 0) {
        // Current user is at least manager of one super group of group
        return true;
      }
    }
    return false;
  }

  public boolean isGroupManagerDirectlyOnCurrentGroup()
      throws JobDomainPeasException {
    List manageableGroupIds = getUserManageableGroupIds();

    return manageableGroupIds.contains(getTargetGroup().getId());
  }

  public Group[] getAllRootGroups() {
    if (m_TargetDomainId.length() <= 0) {
      return new Group[0];
    }

    Group[] selGroupsArray = m_TargetDomain.getAllGroupPage();

    if (isOnlyGroupManager()) {
      selGroupsArray = filterGroupsToGroupManager(selGroupsArray);
    }

    JobDomainSettings.sortGroups(selGroupsArray);
    return selGroupsArray;
  }

  private Group[] filterGroupsToGroupManager(Group[] groups) {
    // get all manageable groups by current user
    List manageableGroupIds = getUserManageableGroupIds();
    Iterator itManageableGroupsIds = null;

    List temp = new ArrayList();

    // filter groups
    Group group = null;
    for (int g = 0; g < groups.length; g++) {
      group = groups[g];

      if (manageableGroupIds.contains(group.getId()))
        temp.add(group);
      else {
        // get all subGroups of group
        List subGroupIds = Arrays.asList(m_AdminCtrl
            .getAllSubGroupIdsRecursively(group.getId()));

        // check if at least one manageable group is part of subGroupIds
        itManageableGroupsIds = manageableGroupIds.iterator();

        String manageableGroupId = null;
        boolean find = false;
        while (!find && itManageableGroupsIds.hasNext()) {
          manageableGroupId = (String) itManageableGroupsIds.next();
          if (subGroupIds.contains(manageableGroupId))
            find = true;
        }

        if (find)
          temp.add(group);
      }
    }

    return (Group[]) temp.toArray(new Group[0]);
  }

  public String createDomain(String domainName, String domainDescription,
      String domainDriver, String domainProperties,
      String domainAuthentication, String silverpeasServerURL,
      String domainTimeStamp) throws JobDomainPeasException,
      JobDomainPeasTrappedException {
    // Vérif domainName
    verifCreateDomain(domainName, false);

    Domain theNewDomain = new Domain();

    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.createDomain()",
        "root.MSG_GEN_ENTER_METHOD", "domainName=" + domainName);
    theNewDomain.setId("-1");
    theNewDomain.setName(domainName);
    theNewDomain.setDescription(domainDescription);
    theNewDomain.setDriverClassName(domainDriver);
    theNewDomain.setPropFileName(domainProperties);
    theNewDomain.setAuthenticationServer(domainAuthentication);
    theNewDomain.setSilverpeasServerURL(silverpeasServerURL);
    theNewDomain.setTheTimeStamp(domainTimeStamp);

    String idRet = m_AdminCtrl.addDomain(theNewDomain);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.createDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN");
    }
    refresh();
    return idRet;
  }

  private void verifCreateDomain(String domainName, boolean domainSql)
      throws JobDomainPeasTrappedException {
    JobDomainPeasTrappedException trappedException = new JobDomainPeasTrappedException(
        "JobDomainPeasSessionController", SilverpeasException.WARNING,
        "jobDomainPeas.WARN_DOMAIN_SQL_NAME");
    if (domainSql)
      trappedException.setGoBackPage("displayDomainSQLCreate");
    else
      trappedException.setGoBackPage("displayDomainCreate");

    // 1-Vérif non présence d'espaces
    int indexOfSpace = domainName.indexOf(" ");
    if (indexOfSpace > -1) {
      throw trappedException;
    }

    // 2-Vérif caractères alphanumériques
    int i = 0;
    char car;
    while (i < domainName.length()) {
      car = domainName.charAt(i);
      if (!Character.isLetterOrDigit(car))
        throw trappedException;
      i++;
    }

    // 3-Vérif domainName unique dans la table ST_Domain
    Domain[] tabDomain = m_AdminCtrl.getAllDomains();
    Domain domain;
    for (int j = 0; j < tabDomain.length; j++) {
      domain = tabDomain[j];
      if (domain.getName().toLowerCase().equals(domainName.toLowerCase()))
        throw trappedException;
    }

    if (domainSql) {
      // 4-Vérif domainName unique dans le fileSystem
      // pour les properties
      // com.stratelia.silverpeas.domains.domain<domainName>.properties
      // et
      // com.stratelia.silverpeas.authentication.autDomain<domainName>.properties
      ResourceLocator propInitialize = new ResourceLocator(
          "com.stratelia.silverpeas._silverpeasinitialize.settings._silverpeasinitializeSettings",
          "");
      String pathInitialize = propInitialize.getString("pathInitialize");
      int indexOfInitialize = pathInitialize.indexOf("initialize");
      String cheminFichierDomain = pathInitialize.substring(0,
          indexOfInitialize)
          + "properties"
          + File.separator
          + "com"
          + File.separator
          + "stratelia"
          + File.separator
          + "silverpeas"
          + File.separator
          + "domains";
      String nomFichierDomain = "domain" + domainName + ".properties";

      File directoryDomain = new File(cheminFichierDomain);
      File fileDomain = new File(directoryDomain, nomFichierDomain);
      if (fileDomain.exists()) {
        throw trappedException;
      }

      String cheminFichierAutDomain = pathInitialize.substring(0,
          indexOfInitialize)
          + "properties"
          + File.separator
          + "com"
          + File.separator
          + "stratelia"
          + File.separator
          + "silverpeas"
          + File.separator
          + "authentication";
      String nomFichierAutDomain = "autDomain" + domainName + ".properties";

      File directoryAutDomain = new File(cheminFichierAutDomain);
      File fileAutDomain = new File(directoryAutDomain, nomFichierAutDomain);
      if (fileAutDomain.exists()) {
        throw trappedException;
      }
    }
  }

  public String createSQLDomain(String domainName, String domainDescription,
      String silverpeasServerURL) throws JobDomainPeasException,
      JobDomainPeasTrappedException {

    // 0- Vérif domainName
    verifCreateDomain(domainName, true);

    // 1-Création sur le fileSystem du properties
    // com.stratelia.silverpeas.domains.domain<domainName>
    SilverTrace
        .info(
            "jobDomainPeas",
            "JobDomainPeasSessionController.createSQLDomain()",
            "root.MSG_GEN_ENTER_METHOD",
            "Création sur le fileSystem du properties com.stratelia.silverpeas.domains.domain<domainName>");

    ResourceLocator propInitialize = new ResourceLocator(
        "com.stratelia.silverpeas._silverpeasinitialize.settings._silverpeasinitializeSettings",
        "");
    String pathInitialize = propInitialize.getString("pathInitialize");
    int indexOfInitialize = pathInitialize.indexOf("initialize");

    String cheminFichierDomain = pathInitialize.substring(0, indexOfInitialize)
        + "properties" + File.separator + "com" + File.separator + "stratelia"
        + File.separator + "silverpeas" + File.separator + "domains";
    String nomFichierDomain = "domain" + domainName + ".properties";

    File directoryDomain = new File(cheminFichierDomain);
    File fileDomain = new File(directoryDomain, nomFichierDomain);
    PrintWriter sortie = null;
    try {
      sortie = new PrintWriter(new FileWriter(fileDomain));
    } catch (IOException e) {
      // suppression du fichier domain<domainName>.properties
      fileDomain.delete();

      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.createSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e);
    }

    // properties templateDomainSQL
    ResourceLocator templateDomainSql = new ResourceLocator(
        "com.stratelia.silverpeas.domains.templateDomainSQL", "");
    String cryptMethod = SilverpeasSettings.readString(templateDomainSql,
        "database.SQLPasswordEncryption", Authentication.ENC_TYPE_MD5);
    boolean allowPasswordChange = SilverpeasSettings.readBoolean(
        templateDomainSql, "allowPasswordChange", false);

    BufferedReader readerTemplateDomainSQL = null;
    try {
      String cheminFichierTemplateDomainSQL = pathInitialize.substring(0,
          indexOfInitialize)
          + "properties"
          + File.separator
          + "com"
          + File.separator
          + "stratelia"
          + File.separator
          + "silverpeas"
          + File.separator
          + "domains" + File.separator + "templateDomainSQL.properties";
      File fileTemplateDomainSQL = new File(cheminFichierTemplateDomainSQL);
      readerTemplateDomainSQL = new BufferedReader(new FileReader(
          fileTemplateDomainSQL));
    } catch (FileNotFoundException e) {
      // suppression du fichier domain<domainName>.properties
      fileDomain.delete();

      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.createSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e);
    }
    String currentLine;

    sortie.println("# SQL driver");
    sortie.println("");
    sortie.println("# DataBase Access");
    sortie.println("# ----------------");
    sortie.println("");
    ResourceLocator propAdmin = new ResourceLocator(
        "com.stratelia.webactiv.beans.admin.admin", "");
    sortie.println("database.SQLClassName					= "
        + propAdmin.getString("AdminDBDriver"));
    sortie.println("database.SQLJDBCUrl						= "
        + propAdmin.getString("WaProductionDb"));
    sortie.println("database.SQLAccessLogin					= "
        + propAdmin.getString("WaProductionUser"));
    sortie.println("database.SQLAccessPasswd				= "
        + propAdmin.getString("WaProductionPswd"));
    sortie.println("");
    sortie.println("database.SQLUserTableName				= Domain" + domainName
        + "_User");
    sortie.println("database.SQLGroupTableName				= Domain" + domainName
        + "_Group");
    sortie.println("database.SQLUserGroupTableName			= Domain" + domainName
        + "_Group_User_Rel");
    sortie.println("");
    sortie.println("# Generic Properties");
    sortie.println("# ----------------");
    sortie.println("");
    sortie.println("# For Users");
    sortie.println("database.SQLUserSpecificIdColumnName	= id");
    sortie.println("database.SQLUserLoginColumnName			= login");
    sortie.println("database.SQLUserFirstNameColumnName		= firstName");
    sortie.println("database.SQLUserLastNameColumnName		= lastName");
    sortie.println("database.SQLUserEMailColumnName			= email");
    sortie.println("database.SQLUserPasswordColumnName		= password");
    sortie.println("database.SQLUserPasswordValidColumnName	= passwordValid");
    sortie.println("");
    sortie.println("# For Groups");
    sortie.println("database.SQLGroupSpecificIdColumnName	= id");
    sortie.println("database.SQLGroupNameColumnName			= name");
    sortie.println("database.SQLGroupDescriptionColumnName	= description");
    sortie.println("database.SQLGroupParentIdColumnName		= superGroupId");
    sortie.println("");
    sortie.println("# For Users-Groups relations");
    sortie.println("database.SQLUserGroupUIDColumnName		= userId");
    sortie.println("database.SQLUserGroupGIDColumnName		= groupId");
    sortie.println("");

    try {
      while ((currentLine = readerTemplateDomainSQL.readLine()) != null) {
        if (!currentLine.startsWith("allowPasswordChange"))
          sortie.println(currentLine);
      }
    } catch (IOException e) {
      // suppression du fichier domain<domainName>.properties
      fileDomain.delete();

      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.createSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e);
    }
    sortie.close();

    // 2-Création sur le fileSystem du properties
    // com.stratelia.silverpeas.authentication.autDomain<domainName>
    SilverTrace
        .info(
            "jobDomainPeas",
            "JobDomainPeasSessionController.createSQLDomain()",
            "root.MSG_GEN_ENTER_METHOD",
            "Création sur le fileSystem du properties com.stratelia.silverpeas.authentication.autDomain<domainName>");
    String cheminFichierAutDomain = pathInitialize.substring(0,
        indexOfInitialize)
        + "properties"
        + File.separator
        + "com"
        + File.separator
        + "stratelia"
        + File.separator + "silverpeas" + File.separator + "authentication";
    String nomFichierAutDomain = "autDomain" + domainName + ".properties";

    File directoryAutDomain = new File(cheminFichierAutDomain);
    File fileAutDomain = new File(directoryAutDomain, nomFichierAutDomain);
    try {
      sortie = new PrintWriter(new FileWriter(fileAutDomain));
    } catch (IOException e) {
      // suppression du fichier domain<domainName>.properties
      fileDomain.delete();

      // suppression du fichier domain<domainName>.properties
      fileAutDomain.delete();

      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.createSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e);
    }

    sortie.println("# Silverpeas default driver authentication");
    sortie.println("# ----------------------------------------");
    sortie.println("");
    sortie
        .println("# Fallback type : could be one of the following values : none, ifNotRejected, always");
    sortie.println("fallbackType							= none");
    sortie.println("");
    sortie.println("allowPasswordChange						= " + allowPasswordChange);
    sortie.println("");
    sortie.println("# Authentication servers");
    sortie
        .println("# Available types are : com.stratelia.silverpeas.authentication.AuthenticationNT, com.stratelia.silverpeas.authentication.AuthenticationSQL and com.stratelia.silverpeas.authentication.AuthenticationLDAP");
    sortie.println("");
    sortie.println("autServersCount							= 1");
    sortie.println("");
    sortie
        .println("autServer0.type							= com.stratelia.silverpeas.authentication.AuthenticationSQL");
    sortie.println("autServer0.enabled						= true");
    sortie.println("autServer0.SQLJDBCUrl 					= "
        + propAdmin.getString("WaProductionDb"));
    sortie.println("autServer0.SQLAccessLogin  				= "
        + propAdmin.getString("WaProductionUser"));
    sortie.println("autServer0.SQLAccessPasswd 				= "
        + propAdmin.getString("WaProductionPswd"));
    sortie.println("autServer0.SQLDriverClass  				= "
        + propAdmin.getString("AdminDBDriver"));
    sortie.println("autServer0.SQLUserTableName 			= Domain" + domainName
        + "_User");
    sortie.println("autServer0.SQLUserLoginColumnName 		= login");
    sortie.println("autServer0.SQLUserPasswordColumnName 	= password");
    sortie
        .println("autServer0.SQLUserPasswordAvailableColumnName = passwordValid");
    sortie.println("autServer0.SQLPasswordEncryption 		= " + cryptMethod);
    sortie.close();

    // 3-Création en base de données des 3 nouvelles tables du domaine :
    // Domain<domainName>_Group, Domain<domainName>_Group_User_Rel et
    // Domain<domainName>_User
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.createSQLDomain()",
        "root.MSG_GEN_ENTER_METHOD",
        "Création en base de données des 3 tablse du Domain");
    try {
      JobDomainPeasDAO.createTableDomain_Group(domainName);
    } catch (SQLException e) {
      // suppression du fichier domain<domainName>.properties
      fileDomain.delete();

      // suppression du fichier domain<domainName>.properties
      fileAutDomain.delete();

      // suppresion de la table Domain<domainName>_Group
      try {
        JobDomainPeasDAO.dropTableDomain_Group(domainName);
      } catch (SQLException e1) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.createSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e1);
      }

      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.createSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e);
    }

    try {
      JobDomainPeasDAO.createTableDomain_User(domainName);
    } catch (SQLException e) {
      // suppression du fichier domain<domainName>.properties
      fileDomain.delete();

      // suppression du fichier domain<domainName>.properties
      fileAutDomain.delete();

      // suppresion de la table Domain<domainName>_Group
      try {
        JobDomainPeasDAO.dropTableDomain_Group(domainName);
      } catch (SQLException e1) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.createSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e1);
      }

      // suppresion de la table Domain<domainName>_User
      try {
        JobDomainPeasDAO.dropTableDomain_User(domainName);
      } catch (SQLException e1) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.createSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e1);
      }

      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.createSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e);
    }

    try {
      JobDomainPeasDAO.createTableDomain_Group_User_Rel(domainName);
    } catch (SQLException e) {
      // suppression du fichier domain<domainName>.properties
      fileDomain.delete();

      // suppression du fichier domain<domainName>.properties
      fileAutDomain.delete();

      // suppresion de la table Domain<domainName>_Group
      try {
        JobDomainPeasDAO.dropTableDomain_Group(domainName);
      } catch (SQLException e1) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.createSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e1);
      }

      // suppresion de la table Domain<domainName>_User
      try {
        JobDomainPeasDAO.dropTableDomain_User(domainName);
      } catch (SQLException e1) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.createSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e1);
      }

      // suppresion de la table Domain<domainName>_Group_User_Rel
      try {
        JobDomainPeasDAO.dropTableDomain_Group_User_Rel(domainName);
      } catch (SQLException e1) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.createSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e1);
      }

      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.createSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e);
    }

    // 4-Enregistrement en base de données du nouveau Domaine dans ST_Domain
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.createSQLDomain()",
        "root.MSG_GEN_ENTER_METHOD",
        "Insertion d'une entrée dans la table ST_Domain");
    Domain theNewDomain = new Domain();
    String idRet = null;
    theNewDomain.setId("-1");
    theNewDomain.setName(domainName);
    theNewDomain.setDescription(domainDescription);
    theNewDomain
        .setDriverClassName("com.stratelia.silverpeas.domains.sqldriver.SQLDriver");
    theNewDomain.setPropFileName("com.stratelia.silverpeas.domains.domain"
        + domainName);
    theNewDomain.setAuthenticationServer("autDomain" + domainName);
    theNewDomain.setSilverpeasServerURL(silverpeasServerURL);
    theNewDomain.setTheTimeStamp("0");
    idRet = m_AdminCtrl.addDomain(theNewDomain);
    if ((idRet == null) || (idRet.length() <= 0)) {
      // suppression du fichier domain<domainName>.properties
      fileDomain.delete();

      // suppression du fichier domain<domainName>.properties
      fileAutDomain.delete();

      // suppresion de la table Domain<domainName>_Group
      try {
        JobDomainPeasDAO.dropTableDomain_Group(domainName);
      } catch (SQLException e1) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.createSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e1);
      }

      // suppresion de la table Domain<domainName>_User
      try {
        JobDomainPeasDAO.dropTableDomain_User(domainName);
      } catch (SQLException e1) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.createSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e1);
      }

      // suppresion de la table Domain<domainName>_Group_User_Rel
      try {
        JobDomainPeasDAO.dropTableDomain_Group_User_Rel(domainName);
      } catch (SQLException e1) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.createSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e1);
      }

      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.createSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN");
    }
    return idRet;
  }

  public String modifyDomain(String domainName, String domainDescription,
      String domainDriver, String domainProperties,
      String domainAuthentication, String silverpeasServerURL,
      String domainTimeStamp) throws JobDomainPeasException,
      JobDomainPeasTrappedException {
    Domain theNewDomain = getTargetDomain();

    // Vérif domainName unique dans la table ST_Domain
    JobDomainPeasTrappedException trappedException = new JobDomainPeasTrappedException(
        "JobDomainPeasSessionController", SilverpeasException.WARNING,
        "jobDomainPeas.WARN_DOMAIN_SQL_NAME");
    trappedException.setGoBackPage("domainContent");
    Domain[] tabDomain = m_AdminCtrl.getAllDomains();
    Domain domain;
    for (int j = 0; j < tabDomain.length; j++) {
      domain = tabDomain[j];
      if (!domain.getId().equals(theNewDomain.getId())
          && domain.getName().toLowerCase().equals(domainName.toLowerCase()))
        throw trappedException;
    }

    String idRet = null;

    if (!StringUtil.isDefined(m_TargetDomainId)
        || m_TargetDomainId.equals("-1")) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifyDomain()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_DOMAIN");
    }
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.modifyDomain()",
        "root.MSG_GEN_ENTER_METHOD", "domainName=" + domainName);
    theNewDomain.setName(domainName);
    theNewDomain.setDescription(domainDescription);
    theNewDomain.setDriverClassName(domainDriver);
    theNewDomain.setPropFileName(domainProperties);
    theNewDomain.setAuthenticationServer(domainAuthentication);
    theNewDomain.setSilverpeasServerURL(silverpeasServerURL);
    theNewDomain.setTheTimeStamp(domainTimeStamp);
    idRet = m_AdminCtrl.updateDomain(theNewDomain);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifyDomain()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_DOMAIN");
    }
    refresh();
    return idRet;
  }

  public String modifySQLDomain(String domainName, String domainDescription,
      String silverpeasServerURL) throws JobDomainPeasException,
      JobDomainPeasTrappedException {
    Domain theNewDomain = getTargetDomain();

    // Vérif domainName unique dans la table ST_Domain
    JobDomainPeasTrappedException trappedException = new JobDomainPeasTrappedException(
        "JobDomainPeasSessionController", SilverpeasException.WARNING,
        "jobDomainPeas.WARN_DOMAIN_SQL_NAME");
    trappedException.setGoBackPage("domainContent");
    Domain[] tabDomain = m_AdminCtrl.getAllDomains();
    Domain domain;
    for (int j = 0; j < tabDomain.length; j++) {
      domain = tabDomain[j];
      if (!domain.getId().equals(theNewDomain.getId())
          && domain.getName().toLowerCase().equals(domainName.toLowerCase()))
        throw trappedException;
    }

    String idRet = null;

    if ((m_TargetDomainId == null) || (m_TargetDomainId.equals("-1"))
        || (m_TargetDomainId.equals("0")) || (m_TargetDomainId.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifySQLDomain()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_DOMAIN");
    }
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.modifySQLDomain()",
        "root.MSG_GEN_ENTER_METHOD", "domainName=" + domainName);
    theNewDomain.setName(domainName);
    theNewDomain.setDescription(domainDescription);
    theNewDomain.setSilverpeasServerURL(silverpeasServerURL);
    idRet = m_AdminCtrl.updateDomain(theNewDomain);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifySQLDomain()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_DOMAIN");
    }
    refresh();
    return idRet;
  }

  public void deleteDomain() throws JobDomainPeasException {
    String idRet = null;

    if ((m_TargetDomainId == null) || (m_TargetDomainId.equals("-1"))
        || (m_TargetDomainId.equals("0")) || (m_TargetDomainId.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.deleteDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_DELETE_DOMAIN");
    }
    idRet = m_AdminCtrl.removeDomain(getTargetDomain().getId());
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.deleteDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_DELETE_DOMAIN");
    }
    returnIntoGroup(null);
    m_TargetDomain = null;
  }

  public void deleteSQLDomain() throws JobDomainPeasException {
    String cheminProperties = getTargetDomain().getPropFileName();
    String domainName = cheminProperties.substring(39);

    // 1-Suppression en base de données du Domaine dans ST_Domain
    if ((m_TargetDomainId == null) || (m_TargetDomainId.equals("-1"))
        || (m_TargetDomainId.equals("0")) || (m_TargetDomainId.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.deleteSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_DELETE_DOMAIN");
    }
    String idRet = m_AdminCtrl.removeDomain(getTargetDomain().getId());
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.deleteSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_DELETE_DOMAIN");
    }
    returnIntoGroup(null);
    m_TargetDomain = null;

    // 2-Suppresion de la table Domain<domainName>_Group
    try {
      JobDomainPeasDAO.dropTableDomain_Group(domainName);
    } catch (SQLException e1) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.deleteSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_DELETE_DOMAIN", e1);
    }

    // 3-Suppresion de la table Domain<domainName>_User
    try {
      JobDomainPeasDAO.dropTableDomain_User(domainName);
    } catch (SQLException e1) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.deleteSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_DELETE_DOMAIN", e1);
    }

    // 4-Suppresion de la table Domain<domainName>_Group_User_Rel
    try {
      JobDomainPeasDAO.dropTableDomain_Group_User_Rel(domainName);
    } catch (SQLException e1) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.deleteSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_DELETE_DOMAIN", e1);
    }

    // 5-Suppression du fichier domain<domainName>.properties
    ResourceLocator propInitialize = new ResourceLocator(
        "com.stratelia.silverpeas._silverpeasinitialize.settings._silverpeasinitializeSettings",
        "");
    String pathInitialize = propInitialize.getString("pathInitialize");
    int indexOfInitialize = pathInitialize.indexOf("initialize");
    String cheminFichierDomain = pathInitialize.substring(0, indexOfInitialize)
        + "properties" + File.separator + "com" + File.separator + "stratelia"
        + File.separator + "silverpeas" + File.separator + "domains";
    String nomFichierDomain = "domain" + domainName + ".properties";

    File directoryDomain = new File(cheminFichierDomain);
    File fileDomain = new File(directoryDomain, nomFichierDomain);
    fileDomain.delete();

    // 6-Suppression du fichier domain<domainName>.properties
    String cheminFichierAutDomain = pathInitialize.substring(0,
        indexOfInitialize)
        + "properties"
        + File.separator
        + "com"
        + File.separator
        + "stratelia"
        + File.separator + "silverpeas" + File.separator + "authentication";
    String nomFichierAutDomain = "autDomain" + domainName + ".properties";

    File directoryAutDomain = new File(cheminFichierAutDomain);
    File fileAutDomain = new File(directoryAutDomain, nomFichierAutDomain);
    fileAutDomain.delete();
  }

  protected String getSureString(String s) {
    if (s == null) {
      return "";
    } else {
      return s;
    }
  }

  public void refresh() {
    if (m_TargetDomain != null)
      m_TargetDomain.refresh();
    for (int i = 0; i < m_GroupsPath.size(); i++) {
      ((GroupNavigationStock) m_GroupsPath.get(i)).refresh();
    }
    setTargetUser(null);
  }

  /*
   * 
   * Selection Peas functions
   */

  public String initSelectionPeasForGroups(String compoURL)
      throws JobDomainPeasException {
    String hostSpaceName = getString("JDP.userPanelGroup");
    PairObject hostComponentName = new PairObject(getTargetGroup().getName(),
        compoURL + "groupContent");
    PairObject[] hostPath = new PairObject[0];
    String hostUrl = compoURL + "groupAddRemoveUsers";
    String cancelUrl = compoURL + "groupContent";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostPath(hostPath);
    sel.setHostComponentName(hostComponentName);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    if ((m_TargetDomainId != null) && (!m_TargetDomainId.equals("-1"))
        && (m_TargetDomainId.length() > 0)) {
      // Add extra params
      SelectionUsersGroups sug = new SelectionUsersGroups();
      sug.setDomainId(m_TargetDomainId);
      sel.setExtraParams(sug);
    }

    sel.setSelectedElements(SelectionUsersGroups
        .getUserIds(((GroupNavigationStock) m_GroupsPath.lastElement())
            .getAllUserPage()));

    // Contraintes
    sel.setSetSelectable(false);
    sel.setPopupMode(false);
    sel.setFirstPage(Selection.FIRST_PAGE_CART);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*
   * Appel UserPannel pour récup du user sélectionné :
   */
  public String[] getSelectedUsersIds() {
    return getSelection().getSelectedElements();
  }

  // Throws Specific Exception
  public String initSelectionPeasForOneGroupOrUser(String compoURL)
      throws JobDomainPeasException {
    String hostSpaceName = getString("JDP.userPanelDomain");
    PairObject hostComponentName = new PairObject(getTargetDomain().getName(),
        compoURL + "domainContent");
    PairObject[] hostPath = new PairObject[0];
    String hostUrl = compoURL + "selectUserOrGroup";
    String cancelUrl = compoURL + "domainContent";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostPath(hostPath);
    sel.setHostComponentName(hostComponentName);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    if ((m_TargetDomainId == null) || (m_TargetDomainId.equals("-1"))
        || (m_TargetDomainId.length() <= 0)) {
      sel.setElementSelectable(false);
    }

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setDomainId(m_TargetDomainId);
    sel.setExtraParams(sug);

    // Contraintes
    sel.setMultiSelect(false);
    sel.setPopupMode(false);
    sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public String getSelectedUserId() {
    return getSelection().getFirstSelectedElement();
  }

  public String getSelectedGroupId() {
    return getSelection().getFirstSelectedSet();
  }

  // Synchro Management
  // ------------------

  public void synchroSQLDomain() {
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.synchroSQLDomain()",
        "root.MSG_GEN_PARAM_VALUE",
        "------------SYNCHRO SQL DOMAIN APPELE----------- domainId="
            + m_TargetDomainId);
    if (m_theThread == null) {
      SynchroReport.setTraceLevel(SynchroReport.TRACE_LEVEL_INFO);
      SynchroReport.setState(SynchroReport.STATE_WAITSTART);
      m_theThread = new SynchroWebServiceThread(this);
      m_ErrorOccured = null;
      m_SynchroReport = "";
      m_theThread.startTheThread();
      SilverTrace.info("jobDomainPeas",
          "JobDomainPeasSessionController.synchroSQLDomain()",
          "root.MSG_GEN_PARAM_VALUE",
          "------------THREAD SYNCHRO SQL DOMAIN LANCE-----------");
    } else {
      SilverTrace
          .info("jobDomainPeas",
              "JobDomainPeasSessionController.synchroSQLDomain()",
              "root.MSG_GEN_PARAM_VALUE",
              "------------!!!! SYNCHRO DOMAIN SQL : DEUXIEME APPEL !!!!!-----------");
    }
  }

  protected String synchronizeSilverpeasViaWebService() {
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.synchroSQLDomain()",
        "root.MSG_GEN_PARAM_VALUE",
        "------------SYNCHRO SQL DOMAIN APPELE----------- domainId="
            + m_TargetDomainId);

    String sReport = "";
    SynchroUserWebServiceItf synchroUserWebService = null;
    try {
      sReport = "Démarrage de la synchronisation...\n\n";
      // Démarrage de la synchro avec la Popup d'affichage
      SynchroReport.startSynchro();

      Domain theDomain = getTargetDomain();

      SynchroReport.warn("jobDomainPeas.synchronizeSilverpeasViaWebService",
          "Domaine : " + theDomain.getName() + " (id : " + theDomain.getId()
              + ")", null);

      // 1- Récupère la liste des groupes à synchroniser (en insert et
      // update)
      Collection listGroupToInsertUpdate;
      try {
        listGroupToInsertUpdate = JobDomainPeasDAO
            .selectGroupSynchroInsertUpdateTableDomain_Group(theDomain);
      } catch (SQLException e1) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.synchroSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_DOMAIN", e1);
      }

      // 2- Traitement Domaine, appel aux webServices
      String propDomainFileName = theDomain.getPropFileName();

      ResourceLocator propDomainSql = new ResourceLocator(propDomainFileName,
          "");
      String nomClasseWebService = propDomainSql
          .getString("ExternalSynchroClass");
      try {
        synchroUserWebService = (SynchroUserWebServiceItf) Class.forName(
            nomClasseWebService).newInstance();
      } catch (Exception e) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.synchroSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_DOMAIN", e);
      }

      synchroUserWebService.startConnection();

      // Insertion / Update de la société
      sReport += synchroUserWebService.insertUpdateDomainWebService(theDomain
          .getId(), theDomain.getName());

      // 3- Traitement groupes, appel aux webServices
      if (listGroupToInsertUpdate != null && listGroupToInsertUpdate.size() > 0) {

        // Insertion / Update des groupes
        sReport += synchroUserWebService.insertUpdateListGroupWebService(
            theDomain.getId(), theDomain.getName(), listGroupToInsertUpdate);

        // Suppression des groupes
        /*
         * Collection listGroupSilverpeas =
         * Arrays.asList(m_AdminCtrl.getAllGroupIds()); sReport +=
         * synchroUserWebService.deleteListGroupWebService(theDomain.getId(),
         * listGroupSilverpeas);
         */

      }

      // 4- Récupère la liste des users à synchroniser (en insert et update)
      Collection listUserToInsertUpdate;
      try {
        listUserToInsertUpdate = JobDomainPeasDAO
            .selectUserSynchroInsertUpdateTableDomain_User(theDomain);
      } catch (SQLException e1) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.synchroSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_DOMAIN", e1);
      }

      // 5- Récupère la liste des users à synchroniser (en delete)
      Collection listUserToDelete;
      try {
        listUserToDelete = JobDomainPeasDAO
            .selectUserSynchroDeleteTableDomain_User(theDomain);
      } catch (SQLException e1) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.synchroSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_DOMAIN", e1);
      }

      // 6-Traitement users, appel aux webServices
      if ((listUserToInsertUpdate != null && listUserToInsertUpdate.size() > 0)
          || (listUserToDelete != null && listUserToDelete.size() > 0)) {

        // CBO : UPDATE 09.04.2008
        /*
         * //Insertion / Update des users if(listUserToInsertUpdate != null &&
         * listUserToInsertUpdate.size()>0) { sReport +=
         * synchroUserWebService.insertUpdateListUserWebService
         * (theDomain.getId(), listUserToInsertUpdate, listGroupToInsertUpdate);
         * }
         * 
         * //Suppression des users if(listUserToDelete != null &&
         * listUserToDelete.size()>0) { sReport +=
         * synchroUserWebService.deleteListUserWebService(theDomain.getId(),
         * listUserToDelete); }
         */
        // Suppression des users
        if (listUserToDelete != null && listUserToDelete.size() > 0) {
          sReport += synchroUserWebService.deleteListUserWebService(theDomain
              .getId(), listUserToDelete);
        }

        // Insertion / Update des users
        if (listUserToInsertUpdate != null && listUserToInsertUpdate.size() > 0) {
          sReport += synchroUserWebService.insertUpdateListUserWebService(
              theDomain.getId(), listUserToInsertUpdate,
              listGroupToInsertUpdate);
        }
        // CBO : FIN UPDATE 09.04.2008
      }

      sReport += "\n\nFin de la synchronisation...";

    } catch (JobDomainPeasException e) {
      SilverTrace.error("JobDomainPeasSessionController",
          "JobDomainPeasSessionController.synchronizeSilverpeasViaWebService",
          "admin.MSG_ERR_SYNCHRONIZE_DOMAIN", e);
      SynchroReport.error(
          "JobDomainPeasSessionController.synchronizeSilverpeasViaWebService",
          "Problème lors de la synchronisation : " + e.getMessage(), null);
      sReport = "Erreurs lors de la synchronisation : \n" + e.getMessage();
    } finally {
      // Fin de synchro avec la Popup d'affichage
      SynchroReport.stopSynchro();

      synchroUserWebService.endConnection();
    }
    return sReport;
  }

  public void synchroDomain(int traceLevel) {
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.synchroDomain()",
        "root.MSG_GEN_PARAM_VALUE",
        "------------SYNCHRO DOMAIN APPELE----------- domainId="
            + m_TargetDomainId);
    if (m_theThread == null) {
      SynchroReport.setTraceLevel(traceLevel);
      SynchroReport.setState(SynchroReport.STATE_WAITSTART);
      m_theThread = new SynchroLdapThread(this, m_AdminCtrl, m_TargetDomainId);
      m_ErrorOccured = null;
      m_SynchroReport = "";
      m_theThread.startTheThread();
      SilverTrace.info("jobDomainPeas",
          "JobDomainPeasSessionController.synchroDomain()",
          "root.MSG_GEN_PARAM_VALUE",
          "------------THREAD SYNCHRO DOMAIN LANCE-----------");
    } else {
      SilverTrace.info("jobDomainPeas",
          "JobDomainPeasSessionController.synchroDomain()",
          "root.MSG_GEN_PARAM_VALUE",
          "------------!!!! SYNCHRO DOMAIN : DEUXIEME APPEL !!!!!-----------");
    }
  }

  public boolean isEnCours() {
    if (m_theThread == null) {
      return false;
    } else {
      return m_theThread.isEnCours();
    }
  }

  public Exception getErrorOccured() {
    return m_ErrorOccured;
  }

  public String getSynchroReport() {
    if (m_ErrorOccured != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);

      m_ErrorOccured.printStackTrace(pw);
      return m_ErrorOccured.toString() + "\n" + sw.getBuffer().toString();
    } else {
      return m_SynchroReport;
    }
  }

  public void threadFinished() {
    m_ErrorOccured = m_theThread.getErrorOccured();
    m_SynchroReport = m_theThread.getSynchroReport();
    m_theThread = null;
  }

  public void getP12(String userId) throws JobDomainPeasException {
    UserDetail user = getUserDetail(userId);

    try {
      X509Factory.buildP12(user.getId(), user.getLogin(), user.getLastName(),
          user.getFirstName(), user.getDomainId());
    } catch (UtilException e) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.getP12()", SilverpeasException.ERROR,
          "admin.MSG_ERR_CANT_GET_P12", e);
    }
  }

  private void revocateCertificate(UserDetail user)
      throws JobDomainPeasException {
    try {
      X509Factory.revocateUserCertificate(user.getId());
    } catch (UtilException e) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.revocateCertificate()",
          SilverpeasException.ERROR, "admin.MSG_ERR_CANT_REVOCATE_CERTIFICATE",
          e);
    }
  }

  /** PAGINATION **/

  /**
   * Get list of selected users Ids
   */
  public ArrayList getListSelectedUsers() {
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.getListUsersSelected()", "",
        "listSelectedUsers (taille) = (" + listSelectedUsers.size() + ") "
            + listSelectedUsers.toString());
    return listSelectedUsers;
  }

  public void clearListSelectedUsers() {
    listSelectedUsers.clear();
  }

  public void setListSelectedUsers(ArrayList list) {
    listSelectedUsers = list;
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasSessionController.setListSelectedUsers()", "",
        "listSelectedUsers (taille) = (" + listSelectedUsers.size() + ") "
            + listSelectedUsers.toString());
  }

  public void setIndexOfFirstItemToDisplay(String index) {
    this.indexOfFirstItemToDisplay = new Integer(index).intValue();
  }

  public int getIndexOfFirstItemToDisplay() {
    return indexOfFirstItemToDisplay;
  }

}