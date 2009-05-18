/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.pdc.control;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdc.model.UsedAxisPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.searchEngine.model.AxisFilter;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/*
 * CVS Informations
 * 
 * $Id: PdcUtilizationBmImpl.java,v 1.7 2008/09/01 07:36:57 neysseri Exp $
 *
 * $Id: PdcUtilizationBmImpl.java,v 1.7 2008/09/01 07:36:57 neysseri Exp $
 * 
 * $Log: PdcUtilizationBmImpl.java,v $
 * Revision 1.7  2008/09/01 07:36:57  neysseri
 * no message
 *
 * Revision 1.6.12.1  2008/08/08 12:45:48  neysseri
 * no message
 *
 * Revision 1.6  2004/07/23 16:11:04  neysseri
 * PDC - Recherche : Optimisation de l'exploration d'un axe
 *
 * Revision 1.5  2004/06/22 15:19:25  neysseri
 * nettoyage eclipse
 *
 * Revision 1.4  2003/05/15 19:30:32  neysseri
 * no message
 *
 * Revision 1.3  2002/10/28 16:09:19  neysseri
 * Branch "InterestCenters" merging
 *
 *
 * Revision 1.2  2002/10/17 13:33:21  neysseri
 * Glossary report from VSIC to KMedition
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.11  2002/04/04 13:10:06  santonio
 * Tient compte de la recherche global (PDC + Classique)
 * Généralisation de certaines méthodes
 *
 * Revision 1.10  2002/03/05 12:51:30  neysseri
 * no message
 *
 * Revision 1.9  2002/03/01 16:31:28  neysseri
 * no message
 *
 * Revision 1.8  2002/03/01 15:54:08  santonio
 * no message
 *
 * Revision 1.7  2002/02/28 16:06:28  neysseri
 * no message
 *
 * Revision 1.6  2002/02/27 16:42:05  neysseri
 * gestion des transactions
 *
 * Revision 1.5  2002/02/27 14:53:26  santonio
 * no message
 *
 * Revision 1.4  2002/02/22 12:01:21  santonio
 * no message
 *
 * Revision 1.3  2002/02/21 18:33:07  santonio
 * no message
 *
 * Revision 1.2  2002/02/19 17:16:44  neysseri
 * jindent + javadoc
 *
 */
 
/**
 * Class declaration
 *
 *
 * @author
 */
public class PdcUtilizationBmImpl implements PdcUtilizationBm
{

    /**
     * SilverpeasBeanDAO is the main link with the SilverPeas persitence.
     * We indicate the Object SilverPeas which map the database.
     */
    private SilverpeasBeanDAO dao = null;

    public PdcUtilizationBmImpl()
    {
        try
        {
            dao = SilverpeasBeanDAOFactory.getDAO("com.stratelia.silverpeas.pdc.model.UsedAxis");
        }
        catch (PersistenceException exce_DAO)
        {
            SilverTrace.error("PDC", "PdcUtilizationBmImpl", "Pdc.CANNOT_CONSTRUCT_PERSISTENCE", exce_DAO);
        }
    }

    /**
     * Returns an axis used by an instance
     * @param usedAxisId - the whished used axis.
     * @return an UsedAxis
     */
    public UsedAxis getUsedAxis(String usedAxisId) throws PdcException
    {
        UsedAxis usedAxis = null;

        try
        {
            usedAxis = (UsedAxis) dao.findByPrimaryKey(new UsedAxisPK(usedAxisId));
        }
        catch (PersistenceException exce_select)
        {
            throw new PdcException("PdcUtilizationBmImpl.getUsedAxis", SilverpeasException.ERROR, "Pdc.CANNOT_FIND_USED_AXIS", exce_select);
        }
        return usedAxis;
    }

    /**
     * Returns a list of used axis sorted.
     * @return a list sorted or null otherwise
     */
    public List getUsedAxisByInstanceId(String instanceId) throws PdcException
    {
        List       usedAxis = null;
        Connection con = openConnection();

        try
        {
            usedAxis = PdcUtilizationDAO.getUsedAxisByInstanceId(con, instanceId);
        }
        catch (Exception e)
        {
            throw new PdcException("PdcUtilizationBmImpl.getUsedAxisByInstanceId", SilverpeasException.ERROR, "Pdc.CANNOT_FIND_USED_AXIS", e);
        }
        finally
        {
            closeConnection(con);
        }

        return usedAxis;
    }

	/**
     * Returns a list of axis header sorted.
     * @return a list sorted or null otherwise
     */
    public List getAxisHeaderUsedByInstanceId(String instanceId) throws PdcException
	{
 		List instanceIds = (List)new ArrayList();
		instanceIds.add(instanceId);
		return getAxisHeaderUsedByInstanceIds(instanceIds);
    }

    public List getAxisHeaderUsedByInstanceIds(List instanceIds) throws PdcException
	{
		return getAxisHeaderUsedByInstanceIds(instanceIds, new AxisFilter());
	}

	public List getAxisHeaderUsedByInstanceIds(List instanceIds, AxisFilter filter) throws PdcException
	{
        List       axisHeaders = null;
        Connection con = openConnection();

        try
        {
            axisHeaders = PdcUtilizationDAO.getAxisUsedByInstanceId(con, instanceIds, filter);
        }
        catch (Exception e)
        {
            throw new PdcException("PdcUtilizationBmImpl.getUsedAxisByInstanceId", SilverpeasException.ERROR, "Pdc.CANNOT_FIND_USED_AXIS", e);
        }
        finally
        {
            closeConnection(con);
        }

        return axisHeaders;
    }


	/**
     * Returns the usedAxis based on a defined axis
     * @param axisId - the id of the axis
     */
    private List getUsedAxisByAxisId(Connection con, int axisId) throws PdcException
    {
		List       usedAxis = null;

        try
        {
            usedAxis = (List) dao.findByWhereClause(con, new UsedAxisPK("useless"), "axisId = "+axisId);
        }
        catch (Exception e)
        {
            throw new PdcException("PdcUtilizationBmImpl.getUsedAxisByAxisId", SilverpeasException.ERROR, "Pdc.CANNOT_FIND_USED_AXIS", e);
        }

		return usedAxis;
    }

    /**
     * Create an used axis into the data base.
     * @param usedAxis - the object which contains all data about utilization of an axis
     * @return usedAxisId
     */
    public int addUsedAxis(UsedAxis usedAxis, String treeId) throws PdcException
    {
        Connection con = openConnection();

        try
        {
            if (PdcUtilizationDAO.isAlreadyAdded(con, usedAxis.getInstanceId(), new Integer(usedAxis.getPK().getId()).intValue(), usedAxis.getAxisId(), usedAxis.getBaseValue(), treeId))
            {
                return 1;
            }
            else
            {
                dao.add(usedAxis);
				// une fois cette axe rajouté, il faut tenir compte de la propagation des choix aux niveaux 
				// obligatoire/facultatif et variant/invariante
				// PAS ENCORE UTILE
				// PdcUtilizationDAO.updateAllUsedAxis(con, usedAxis);
            }
        }
        catch (Exception exce_create)
        {
            throw new PdcException("PdcUtilizationBmImpl.addUsedAxis", SilverpeasException.ERROR, "Pdc.CANNOT_ADD_USED_AXIS", exce_create);
        }
        finally
        {
            closeConnection(con);
        }
        return 0;
    }

    /**
     * Update an used axis into the data base.
     * @param usedAxis - the object which contains all data about utilization of the axis
     */
    public int updateUsedAxis(UsedAxis usedAxis, String treeId) throws PdcException
    {
        Connection con = openConnection();

        try
        {
			//test si la valeur de base a été modifiée
			int newBaseValue = usedAxis.getBaseValue();
			int oldBaseValue = (getUsedAxis(usedAxis.getPK().getId())).getBaseValue();
			// si elle a été modifiée alors on reporte la modification.
			if (newBaseValue != oldBaseValue){
				if (PdcUtilizationDAO.isAlreadyAdded(con, usedAxis.getInstanceId(), new Integer(usedAxis.getPK().getId()).intValue(), usedAxis.getAxisId(), usedAxis.getBaseValue(), treeId))
					return 1;
			}
            dao.update(usedAxis);
			// une fois cette axe modifié, il faut tenir compte de la propagation des choix aux niveaux 
			// obligatoire/facultatif et variant/invariante
			PdcUtilizationDAO.updateAllUsedAxis(con, usedAxis);
            return 0;
        }
        catch (Exception exce_create)
        {
            throw new PdcException("PdcUtilizationBmImpl.updateUsedAxis", SilverpeasException.ERROR, "Pdc.CANNOT_UPDATE_USED_AXIS", exce_create);
        }
        finally
        {
            closeConnection(con);
        }
    }

    /**
     * delete the used axis from the data base
     * @param usedAxisId - the id of the used axe
     */
    public void deleteUsedAxis(String usedAxisId) throws PdcException
    {
        try
        {
            dao.remove(new UsedAxisPK(usedAxisId));
        }
        catch (Exception exce_delete)
        {
            throw new PdcException("PdcUtilizationBmImpl.deleteUsedAxis", SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_USED_AXIS", exce_delete);
        }
    }

    /**
     * Method declaration
     *
     *
     * @param usedAxisIds
     *
     * @throws PdcException
     *
     * @see
     */
    public void deleteUsedAxis(Collection usedAxisIds) throws PdcException
    {
        try
        {
            Iterator it = usedAxisIds.iterator();
            String   usedAxisId = "";
            String   whereClause = " 0 = 1 ";

            while (it.hasNext())
            {
                usedAxisId = (String) it.next();
                whereClause += " or " + usedAxisId;
            }
            dao.removeWhere(new UsedAxisPK("useless"), whereClause);
        }
        catch (Exception exce_delete)
        {
            throw new PdcException("PdcUtilizationBmImpl.deleteUsedAxis", SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_USED_AXIS", exce_delete);
        }
    }

    /**
     * Method declaration
     *
     *
     * @param axisId
     *
     * @throws PdcException
     *
     * @see
     */
    public void deleteUsedAxisByAxisId(Connection con, String axisId) throws PdcException
    {
        try
        {
            dao.removeWhere(con, new UsedAxisPK("useless"), " axisId = " + axisId);
        }
        catch (Exception exce_delete)
        {
            throw new PdcException("PdcUtilizationBmImpl.deleteUsedAxisByAxisId", SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_USED_AXIS", exce_delete);
        }
    }

    /**
     * Method declaration
     *
     *
     * @param valueId
     *
     * @throws PdcException
     *
     * @see
     */
    private void deleteUsedAxisByValueId(Connection con, int valueId, int axisId) throws PdcException
    {
        try
        {
            dao.removeWhere(con, new UsedAxisPK("useless"), " axisId = "+axisId+" and baseValue = " + valueId);
        }
        catch (Exception exce_delete)
        {
            throw new PdcException("PdcUtilizationBmImpl.deleteUsedAxisByValueId", SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_USED_AXIS", exce_delete);
        }
    }
    /**
     * Method declaration
     *
     *
     * @param valueId
     *
     * @throws PdcException
     *
     * @see
     */

	public void deleteUsedAxisByMotherValue(Connection con, String valueId, String axisId, String treeId) throws PdcException
    {
        try
        {
            dao.removeWhere(con, new UsedAxisPK("useless"), " axisId = "+axisId+" and baseValue in ( select id from SB_Tree_Tree where treeId = "+treeId+" and (path like '%/"+valueId+"/%' or id = " +valueId+" ))");
        }
        catch (Exception exce_delete)
        {
            throw new PdcException("PdcUtilizationBmImpl.deleteUsedAxisByValueId", SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_USED_AXIS", exce_delete);
        }
    }

	/**
	* Update a base value from the PdcUtilizationDAO 
	* @param valueId - the base value that must be updated
	*/
	private void updateBaseValue(Connection con, int oldBaseValue, int newBaseValue, int axisId, String treeId, String instanceId) throws PdcException {
		try{
			PdcUtilizationDAO.updateBaseValue(con, oldBaseValue, newBaseValue, axisId, treeId, instanceId);
        } catch (Exception exce_update){
            throw new PdcException("PdcUtilizationBmImpl.updateUsedAxis", SilverpeasException.ERROR, "Pdc.CANNOT_UPDATE_USED_AXIS", exce_update);
        }
	}

	public void updateOrDeleteBaseValue(Connection con, int baseValueToUpdate, int newBaseValue, int axisId, String treeId) throws PdcException {
		SilverTrace.info("Pdc", "PdcBmImpl.updateOrDeleteBaseValue", "root.MSG_GEN_PARAM_VALUE", "baseValueToUpdate = "+baseValueToUpdate);
		SilverTrace.info("Pdc", "PdcBmImpl.updateOrDeleteBaseValue", "root.MSG_GEN_PARAM_VALUE", "newBaseValue = "+newBaseValue);
		SilverTrace.info("Pdc", "PdcBmImpl.updateOrDeleteBaseValue", "root.MSG_GEN_PARAM_VALUE", "axisId = "+axisId);
		SilverTrace.info("Pdc", "PdcBmImpl.updateOrDeleteBaseValue", "root.MSG_GEN_PARAM_VALUE", "treeId = "+treeId);

		List usedAxisList = getUsedAxisByAxisId(con, axisId);
		//pour chaque instance, on vérifie que la modification est possible
		String instanceId = null;
		UsedAxis usedAxis = null;
		boolean updateAllowed = false;
		for (int i = 0; i < usedAxisList.size() ; i++) {
			usedAxis = (UsedAxis) usedAxisList.get(i);
			instanceId = usedAxis.getInstanceId();
			SilverTrace.info("Pdc", "PdcBmImpl.updateOrDeleteBaseValue", "root.MSG_GEN_PARAM_VALUE", "instanceId = "+instanceId);

			if (usedAxis.getBaseValue() == baseValueToUpdate) {
				try
				{
					//test si la nouvelle valeur est autorisée comme nouvelle valeur de base
					updateAllowed = !PdcUtilizationDAO.isAlreadyAdded(con, instanceId, new Integer(usedAxis.getPK().getId()).intValue(), axisId, newBaseValue, treeId);
				}
				catch (Exception e)
				{
					throw new PdcException("PdcUtilizationBmImpl.updateOrDeleteBaseValue", SilverpeasException.ERROR, "Pdc.CANNOT_UPDATE_USED_AXIS", e);
				}

				SilverTrace.info("Pdc", "PdcBmImpl.updateOrDeleteBaseValue", "root.MSG_GEN_PARAM_VALUE", "updateAllowed = "+updateAllowed);

				if (updateAllowed) {
					updateBaseValue(con, baseValueToUpdate, newBaseValue, axisId, treeId, instanceId);		// replace this value by its mother
				}
			}
		}
		deleteUsedAxisByValueId(con, baseValueToUpdate, axisId);
	}

    /**
     * *********************************************
     */

    /**
     * ******** DATABASE CONNECTION MANAGER ********
     */

    /**
     * *********************************************
     */

    /**
     * Method declaration
     * 
     * 
     * @return
     * 
     * @see
     */
    private Connection openConnection() throws PdcException
    {
        try
        {
            Connection con = DBUtil.makeConnection(JNDINames.PDC_DATASOURCE);

            return con;
        }
        catch (Exception e)
        {
            throw new PdcException("PdcUtilizationBmImpl.openConnection()", SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
        }
    }

    /**
     * Method declaration
     * 
     * 
     * @param con
     * 
     * @see
     */
    private void closeConnection(Connection con)
    {
        if (con != null)
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
                SilverTrace.error("Pdc", "PdcUtilizationBmImpl.closeConnection()", "root.EX_CONNECTION_CLOSE_FAILED", "", e);
            }
        }
    }

}
