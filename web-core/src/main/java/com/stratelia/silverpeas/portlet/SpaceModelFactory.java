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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.portlet;

/**
 * coucou
 * Title:        portlets
 * Description:  Enable portlet management in Silverpeas
 * Copyright:    Copyright (c) 2001
 * Company:      Stratelia
 * @author       Eric BURGEL
 * @version 1.0
 */

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.portlet.model.PortletColumnRow;
import com.stratelia.silverpeas.portlet.model.PortletColumnTable;
import com.stratelia.silverpeas.portlet.model.PortletRowRow;
import com.stratelia.silverpeas.portlet.model.PortletRowTable;
import com.stratelia.silverpeas.portlet.model.PortletSchema;
import com.stratelia.silverpeas.portlet.model.PortletStateRow;
import com.stratelia.silverpeas.portlet.model.PortletStateTable;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.instance.control.WAComponent;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * The SpaceModelFactory is a class used for its statics methods. its construct
 * and maintain the SpaceModel hierarchie in memory. It implement the
 * persistance for the SpaceModel class, ie : it provide read and save methods
 * from/to a database. It can read/save the user specific state for the portlets
 * (min, max or normal) either.
 */
public class SpaceModelFactory {
  static protected OrganizationController oc = null;
  static protected Hashtable compoDescriptors = null;

  static {
    oc = new OrganizationController();
    compoDescriptors = (new AdminController(null)).getAllComponents();
  }

  // private static java.util.HashMap spaceModels ;

  /*
   * static private DataSource dataSource ;
   * 
   * // Static initializer : i.e. executed only one time for the class static {
   * try { // Get the initialContext Context ctx = new InitialContext(); // Look
   * up myDataSource dataSource = (DataSource) ctx.lookup ("jdbc/Silverpeas");
   * //Create a connection object } catch(NamingException e) {
   * e.printStackTrace(); } }
   */

  /**
   * As the SpaceModelFactory is a class used for its statics methods, It
   * doesn't need any contructor
   */

  private SpaceModelFactory() {
  }

  /**
   * Read a spaceModel from database and construct a SpaceModel in memory
   * 
   * @param aSpaceId
   *          the space database Id
   * @return a spaceModel
   */
  public static SpaceModel getSpaceModel(PortletSchema os, String aSpaceId)
      throws PortletException {
    SpaceModel sm;
    // Test the arguments
    if (aSpaceId == null) {
      throw new PortletException(
          "SpaceModelFactory.getSpaceModel(PortletSchema os, String aSpaceId)",
          SilverpeasException.ERROR, "portlet.EX_SPACE_NULL");
    }
    if (aSpaceId.equalsIgnoreCase("")) {
      throw new PortletException(
          "SpaceModelFactory.getSpaceModel(PortletSchema os, String aSpaceId)",
          SilverpeasException.ERROR, "portlet.EX_SPACE_EMPTY");
    }
    int spaceNum = Integer.parseInt(aSpaceId);

    try {
      PortletColumnTable tPortletColumn = os.portletColumn;
      PortletRowTable tPortletRow = os.portletRow;

      // Get all the columns for this spaceId
      PortletColumnRow[] portletColumns = tPortletColumn.getAllBySpaceId(
          spaceNum, "nbCol");

      sm = new SpaceModel(aSpaceId);

      // for each of the columns
      for (int i = 0; i < portletColumns.length; i++) {
        PortletColumnRow pc = portletColumns[i];
        SpaceColumn sc = new SpaceColumn(i, pc.getColumnWidth());
        PortletRowRow[] portletRows = tPortletRow.getAllByPortletColumnId(pc
            .getId(), "nbRow");

        // for each row in the column
        for (int j = 0; j < portletRows.length; j++) {
          PortletRowRow prr = portletRows[j];
          Portlet portlet = getPortlet(prr.getInstanceId(), prr.getId());

          sc.addPortlet(portlet);
        }
        sm.addColumn(sc);
      }
    } catch (UtilException e) {
      throw new PortletException(
          "SpaceModelFactory.getSpaceModel(PortletSchema os, String aSpaceId)",
          SilverpeasException.ERROR, "portlet.EX_CANT_GET_SPACE_MODEL", e);
    }

    return sm;
  }

  /**
   * get a spaceModel and remove the component that are not allowed for this
   * user
   * 
   * @param msc
   *          parameter for getSpaceModel
   * @param aSpaceId
   *          the required space Id
   * @param isAdmin
   *          true if the user is editing the spaceModel. in this case, the
   *          unallowed portlets are not remove from the spaceModel.
   * @return the returned SpaceModel
   * @throws PortletException
   *           -
   */
  public static SpaceModel getSpaceModel(MainSessionController msc,
      String aSpaceId, boolean isAdmin) throws PortletException {
    SpaceModel sm = null;
    PortletSchema schema = null;

    try {
      // Create a schema for accessing tables
      // PortletSchema schema = new PortletSchema(dataSource.getConnection()) ;
      schema = new PortletSchema(0);

      // get the standard spaceModel for this spaceId
      sm = getSpaceModel(schema, aSpaceId);

      int userId = Integer.parseInt(msc.getUserId());

      // Save the database userId in the space model
      sm.setUserId(userId);

      if (isAdmin) {
        sm.setIsAdministrator(true);
      } else {
        // Now we remove the non available components for this user
        // Get all the available components for this userIs and this spaceId
        String[] availComponents = msc.getUserAvailComponentIds();

        // Extract the componentId from components
        for (int i = 0; i < availComponents.length; i++) {
          availComponents[i] = extractLastNumber(availComponents[i]);
        }

        for (int col = 0; col < sm.getcolumnsCount(); col++) {
          SpaceColumn sc = sm.getColumn(col);

          for (int row = 0; row < sc.getPortletCount(); row++) {
            Portlet portlet = sc.getPortlets(row);
            String instanceId = String.valueOf(portlet.getId());
            boolean ok = false;

            // check if the user is allowed to access the required component
            for (int nI = 0; (nI < availComponents.length && !ok); nI++) {

              if (availComponents[nI].equalsIgnoreCase(instanceId)) {
                ok = true;
              }
            }
            // if the component is not allowed for this user,
            // we remove it from the in memory spaceModel.
            if (ok == false) {
              sm.removePortlet(col, row);
              row--;
              if (sc.getPortletCount() == 0) {
                col--;
              }
            }
          }
        }
      }

      // Now read the portlet states for this user
      PortletStateTable tPortletState = schema.portletState;

      for (int i = 0; i < sm.getPortletCount(); i++) {
        Portlet portlet = sm.getPortlets(i);
        String req = "Select * from ST_PortletState" + " Where userId = "
            + sm.getUserId() + " and portletRowId = " + portlet.getRowId();
        PortletStateRow psr = tPortletState.getPortletState(req);

        if (psr != null) {
          portlet.setState(psr.getState());
        }
      }
    } catch (UtilException e) {
      throw new PortletException(
          "SpaceModelFactory.getSpaceModel(MainSessionController msc, String aSpaceId, boolean isAdmin)",
          SilverpeasException.ERROR, "portlet.EX_CANT_GET_SPACE_MODEL", e);
    } finally {
      schema.close();
    }
    return sm;
  }

  /**
   * portletSaveState Save the portlet state : minimized, normal or maximized
   * 
   * @param aSpaceId
   *          parameter for portletSaveState
   * @param aPortlet
   *          the portlet to save the state
   * @throws PortletException
   *           -
   */

  /*
   * public static void portletSaveState(String aSpaceId, String aUserId,
   * Portlet aPortlet) throws PortletException { PortletSchema os = null ; //
   * Test the arguments if (aSpaceId == null) {throw new
   * PortletException("spaceId is null");} if (aSpaceId.equalsIgnoreCase(""))
   * {throw new PortletException("spaceId is empty");}
   * 
   * if (aUserId == null) {throw new PortletException("userId is null");} if
   * (aUserId.equalsIgnoreCase("")) {throw new
   * PortletException("UserId is empty");}
   * 
   * int spaceNum = Integer.parseInt(aSpaceId) ;
   * 
   * try { // Create a schema for accessing tables // os = new
   * PortletSchema(dataSource.getConnection()) ; os = new PortletSchema() ;
   * PortletStateTable tPortletState = os.portletState ;
   * 
   * // To be atomised in the future UserTable tUser = os.user ; UserRow ur =
   * tUser.getByLdapId(aLDAPUserId) ; int aUserId = ur.getId() ;
   * 
   * String req = "Select * from ST_PortletState" + "  Where portletRowId = " +
   * aPortlet.getRowId() + "  and userId = " + aUserId ; PortletStateRow psr =
   * tPortletState.getPortletState(req) ; // If there is not yet a State for
   * this portlet and this user if (psr == null) { //We have to create one psr =
   * new PortletStateRow(-1, aPortlet.getState(), aUserId, aPortlet.getRowId())
   * ; } else { psr.setState(aPortlet.getState()); } tPortletState.save(psr) ;
   * os.commit();
   * 
   * } catch (UtilException e) { throw new PortletException(e,
   * "Error Saving the portlet State") ; } finally { try { if (os != null) {
   * os.close(); } } catch (UtilException e) { e.printStackTrace(); } } }
   */
  public static void portletSaveState(SpaceModel space, Portlet aPortlet)
      throws PortletException {
    PortletSchema os = null;

    try {
      // Create a schema for accessing tables
      os = new PortletSchema(0);
      PortletStateTable tPortletState = os.portletState;

      String req = "Select * from ST_PortletState" + "  Where portletRowId = "
          + aPortlet.getRowId() + "  and userId = " + space.getUserId();
      PortletStateRow psr = tPortletState.getPortletState(req);

      // If there is not yet a State for this portlet and this user
      if (psr == null) {
        // We have to create one
        psr = new PortletStateRow(-1, aPortlet.getState(), space.getUserId(),
            aPortlet.getRowId());
      } else {
        psr.setState(aPortlet.getState());
      }
      tPortletState.save(psr);
      os.commit();

    } catch (UtilException e) {
      throw new PortletException(
          "SpaceModelFactory.portletSaveState(SpaceModel space, Portlet aPortlet)",
          SilverpeasException.ERROR, "portlet.EX_CANT_SAVED_STATE", e);
    } finally {
      if (os != null) {
        os.close();
      }
    }
  }

  /**
   * getPortlet Read an instance from database and contruct a Portlet in memory
   * 
   * @param instanceId
   *          parameter for getPortlet
   * @param rowId
   *          -1 means that there is no PortletRow record associated to the
   *          portlet
   * 
   * @return the returned Portlet
   * @throws PortletException
   *           -
   */
  private static Portlet getPortlet(int instanceId, int rowId)
      throws PortletException {
    Portlet portlet = null;

    try {

      boolean maximizable = true;
      boolean minimizable = true;

      String sComponentId = String.valueOf(instanceId);
      ComponentInst cir = oc.getComponentInst(sComponentId);
      String sSpaceId = cir.getDomainFatherId();

      portlet = new Portlet(instanceId, rowId, URLManager.getURL(cir.getName(),
          sSpaceId, cir.getName() + sComponentId), cir.getName(), sComponentId,
          cir.getLabel(), cir.getDescription(),
          null, // TitlebarUrl
          null, // IconUrl
          null, // ContentUrl
          null, // MaxContentUrl
          "headerUrl", "footerUrl", "helpUrl", maximizable, minimizable,
          Portlet.NORMAL);
    } catch (Exception e) {
      throw new PortletException(
          "SpaceModelFactory.portletSaveState(SpaceModel space, Portlet aPortlet)",
          SilverpeasException.ERROR, "portlet.EX_CANT_GET_PORTLET", e);
    }
    return portlet;
  }

  /**
   * getPortlet Read an instance from database and contruct a Portlet in memory
   * 
   * @param aInstanceId
   *          parameter for getPortlet
   * @return the returned Portlet
   * @throws PortletException
   *           -
   */
  public static Portlet getPortlet(int aInstanceId) throws PortletException {
    // -1 means that there is no PortletRow record associated to the portlet
    return getPortlet(aInstanceId, -1);
  }

  /**
   * getPortletList Construct the list of portlet that can be added to a space :
   * All instances that are not allready added the this space minus instance
   * that are not "portlettizable" yet.
   * 
   * @param space
   *          parameter for getPortletList
   * @return the returned ComponentInstanceRow[]
   */
  public static PortletComponent[] getPortletList(SpaceModel space) {
    SpaceInst sinst = oc.getSpaceInstById("WA" + space.getSpaceId());
    ArrayList clist = sinst.getAllComponentsInst();
    Iterator it = clist.iterator();
    ArrayList ar = new ArrayList();
    ComponentInst cinst;
    WAComponent cdesc;
    int cid;

    // Remove the components already used from the list
    while (it.hasNext()) {
      cinst = (ComponentInst) it.next();
      cdesc = (WAComponent) compoDescriptors.get(cinst.getName());
      cid = Integer.parseInt(extractLastNumber(cinst.getId()));
      if (cdesc != null && cdesc.isPortlet()
          && (space.getPortletIndex(cid) < 0)) {
        ar.add(new PortletComponent(cid, space.getSpaceId(), cinst.getLabel(),
            cinst.getName(), cinst.getDescription()));
      }
    }
    return (PortletComponent[]) ar.toArray(new PortletComponent[0]);
  }

  /**
   * saveSpaceModel Save all the spaceModel structure in database. the save in
   * done by deleting and recreating the structure.
   * 
   * @param space
   *          parameter for saveSpaceModel
   * @throws PortletException
   *           -
   */
  public static void saveSpaceModel(SpaceModel space) throws PortletException {
    PortletSchema os = null;

    try {
      // Create a schema for accessing tables
      os = new PortletSchema(0);
      PortletColumnTable tPortletColumn = os.portletColumn;
      PortletRowTable tPortletRow = os.portletRow;

      // Retrieve all the column for this spaceId
      PortletColumnRow[] pRows = tPortletColumn.getAllBySpaceId(space
          .getSpaceId(), null);

      // Delete old configuration from database
      // Cascading delete of all the column for this spaceId
      for (int i = 0; i < pRows.length; i++) {
        PortletColumnRow pr = pRows[i];

        tPortletColumn.delete(pr.getId());
      }

      // Create new configuration in database

      int nbrColumn = space.getcolumnsCount();

      for (int i = 0; i < nbrColumn; i++) {

        // Create the columns
        SpaceColumn sc = space.getColumn(i);
        PortletColumnRow pcr = new PortletColumnRow(-1, space.getSpaceId(), sc
            .getColumnWidth(), i);

        tPortletColumn.create(pcr);
        int columnId = pcr.getId();

        // Create the rows in the column
        int nbrRow = sc.getPortletCount();

        for (int j = 0; j < nbrRow; j++) {
          Portlet portlet = sc.getPortlets(j);
          PortletRowRow prr = new PortletRowRow(-1, // Primary key
              portlet.getId(), columnId, 0, // rowHeight : not used
              j); // nbRow

          tPortletRow.create(prr);
        }
      }

      // if all is ok commit the changes
      os.commit();
    } catch (UtilException e) {
      throw new PortletException(
          "SpaceModelFactory.saveSpaceModel(SpaceModel space)",
          SilverpeasException.ERROR, "portlet.EX_CANT_SAVE_SPACE", e);
    } finally {
      if (os != null) {
        os.close();
      }
    }
  }

  /**
   * Extract the last number from the string
   * 
   * @param chaine
   *          The String to clean
   * @return the clean String Example 1 : kmelia47 -> 47 Example 2 : b2b34 -> 34
   * 
   */
  static String extractLastNumber(String chaine) {
    String s = "";

    for (int i = 0; i < chaine.length(); i++) {
      char car = chaine.charAt(i);

      switch (car) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          s = s + car;
          break;
        default:
          s = "";
      }
    }
    return s;
  }

  /**
   * Compute if there is any portlets avaible for this user in this space
   * 
   * @param mainSessionCtrl
   * @param spaceId
   * @return true if there is at leat one portlet avalaible for this user
   * 
   */
  public static boolean portletAvailable(MainSessionController mainSessionCtrl,
      String spaceId) {
    boolean ret = false;
    PortletSchema os = null;
    PortletRowRow[] ciRows = null;

    try {
      // Create a schema for accessing tables
      os = new PortletSchema(0);
      PortletRowTable prt = os.portletRow;
      // 
      String req = "Select R.* from ST_PortletColumn C, ST_PortletRow R Where C.spaceId = "
          + spaceId + " and R.portletColumnId = C.id";

      ciRows = prt.getPortletRows(req);

      SilverTrace.info("portlet", "SpaceModelFactory.portletAvailable()",
          "root.MSG_GEN_PARAM_VALUE", "NbRows = " + ciRows.length);
      // Get all the available components for this userIs and this spaceId
      String[] availComponents = mainSessionCtrl.getUserAvailComponentIds();

      // Extract the componentId from components
      for (int i = 0; i < availComponents.length; i++) {
        availComponents[i] = extractLastNumber(availComponents[i]);
      }

      search: for (int i = 0; i < ciRows.length; i++) {
        PortletRowRow cir = ciRows[i];
        // Get the space id and the component id required by the user
        String componentId = String.valueOf(cir.getInstanceId());

        // check if the user is allowed to access the required component
        for (int nI = 0; nI < availComponents.length; nI++) {

          if (availComponents[nI].equalsIgnoreCase(componentId)) {
            ret = true;
            break search;
          }
        }
      }

    } catch (UtilException e) {
      SilverTrace.error("portlet", "SpaceModelFactory.portletAvailable()",
          "root.MSG_ERR_UNKNOWN", "PB !!!", e);
      ret = false;
    } finally {
      if (os != null) {
        os.close();
      }
    }
    return ret;
  }

}
