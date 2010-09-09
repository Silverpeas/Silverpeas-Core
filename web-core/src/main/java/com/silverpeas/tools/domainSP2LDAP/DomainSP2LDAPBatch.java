package com.silverpeas.tools.domainSP2LDAP;

import java.util.ArrayList;
import java.util.HashMap;

import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class DomainSP2LDAPBatch
{
  private OrganizationController organizationController;
  private AdminController adminController;
  public final String DOMAIN_SILVERPEAS_ID = "0";
  
  public OrganizationController getOrganizationController() {
    if (organizationController == null)
      organizationController = new OrganizationController();
    return organizationController;
  }

  public AdminController getAdminController() {
    if (adminController == null)
      adminController = new AdminController("0");
    return adminController;
  }

  public DomainSP2LDAPBatch()
	{
	}

  /**
   * Process migration
   * @param domainLDAP_Id
   * @return ArrayList of 2 HashMap (list1:users processed, list2: users not processed)
   * @throws Exception
   */
	public ArrayList<HashMap<String, UserDetail>> processMigration(String domainLDAP_Id) throws Exception
	{
	  SynchroReport.info("DomainSP2LDAPBatch.processMigration()", "root.MSG_ENTER_METHOD",null);

	  ArrayList<HashMap<String, UserDetail>> returnListLDAPUsers = new ArrayList<HashMap<String, UserDetail>>();
	  
    HashMap<String,UserDetail> processedUsers = new HashMap<String, UserDetail>();
    HashMap<String,UserDetail> notProcessedSPUsers =  new HashMap<String, UserDetail>();
    HashMap<String,UserDetail> listLDAPUsers =  new HashMap<String, UserDetail>();
	  
		try
		{
			// get all users from ldap
		  String[] listLDAPUsersIds = getAdminController().getUserIdsOfDomain(domainLDAP_Id);
		  SynchroReport.info("DomainSP2LDAPBatch.processMigration()", "Utilisateurs du domaine Silverpeas="+listLDAPUsersIds.length, null);
		  if (listLDAPUsersIds.length==0)
		    return returnListLDAPUsers;
		  
		  for (int i=0; i<listLDAPUsersIds.length; i++)
		  {
		    UserDetail userDetailLDAP = getOrganizationController().getUserDetail(listLDAPUsersIds[i]);
		    String keyName = (userDetailLDAP.getFirstName()+userDetailLDAP.getLastName()).toLowerCase();
		    listLDAPUsers.put(keyName, userDetailLDAP);
		  }
		  
      // get all users from domainSilverpeas
      String[] listSilverpeasUsersIds = getAdminController().getUserIdsOfDomain(DOMAIN_SILVERPEAS_ID);
      for (int i=0; i<listSilverpeasUsersIds.length; i++)
      {
        UserDetail userDetail = getOrganizationController().getUserDetail(listSilverpeasUsersIds[i]);
        String keyName = (userDetail.getFirstName()+userDetail.getLastName()).toLowerCase();
        //user to migrate
        if (listLDAPUsers.containsKey(keyName))
        {
          //Delete ldap user entry
          UserDetail userDetailLDAP = listLDAPUsers.get(keyName);
          
          getAdminController().synchronizeRemoveUser(userDetailLDAP.getId());

          //update silverpeas user entry (withe ldap infos)
          userDetail.setSpecificId(userDetailLDAP.getSpecificId());
          userDetail.setDomainId(userDetailLDAP.getDomainId());
          getAdminController().updateUser(userDetail);
          //Users processed
          processedUsers.put(keyName, userDetail);
        }
        else
          notProcessedSPUsers.put(keyName, userDetail);

      }
		}
    catch (Exception e)
		{
			e.printStackTrace();
			throw new AdminException("DomainSP2LDAPBatch.processMigration()",
          SilverpeasException.ERROR,
          "Erreur lors de la migration des comptes Silverpeas", e);
		}
    SynchroReport.info("DomainSP2LDAPBatch.processMigration()", "DEBUT Synchronisation post migration du domaine "+domainLDAP_Id, null);
    SynchroReport.info("DomainSP2LDAPBatch.processMigration()", getAdminController().synchronizeSilverpeasWithDomain(domainLDAP_Id),null);
    SynchroReport.info("DomainSP2LDAPBatch.processMigration()", "FIN Synchronisation post migration du domaine "+domainLDAP_Id,null);
    returnListLDAPUsers.add(processedUsers);
    returnListLDAPUsers.add(notProcessedSPUsers);
    returnListLDAPUsers.add(listLDAPUsers);
    SynchroReport.info("DomainSP2LDAPBatch.processMigration()", "root.MSG_EXIT_METHOD", null);
    SynchroReport.setTraceLevel(SynchroReport.TRACE_LEVEL_UNKNOWN);
    return returnListLDAPUsers;
	}

	/**
	 * Get all Domains
	 * @return
	 * @throws AdminException
	 */
	public Domain[] getDomains() throws AdminException
	{
	  return getOrganizationController().getAllDomains();
	}
	
	public int getNbUsers(String domainId)
	{
    return getAdminController().getUserIdsOfDomain(domainId).length;
	}
}
