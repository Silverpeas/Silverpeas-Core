package com.silverpeas.tools.domainSP2LDAP;

import java.util.ArrayList;
import java.util.HashMap;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
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
	  AdminController adminController = getAdminController();
	  
	  ArrayList<HashMap<String, UserDetail>> returnListLDAPUsers = new ArrayList<HashMap<String, UserDetail>>();
	  
    HashMap<String,UserDetail> processedUsers = new HashMap<String, UserDetail>();
    HashMap<String,UserDetail> notProcessedSPUsers =  new HashMap<String, UserDetail>();
    HashMap<String,UserDetail> listLDAPUsers =  new HashMap<String, UserDetail>();
	  
		try
		{
			// get all users from ldap
		  String[] listLDAPUsersIds = adminController.getUserIdsOfDomain(domainLDAP_Id);
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
      String[] listSilverpeasUsersIds = adminController.getUserIdsOfDomain(DOMAIN_SILVERPEAS_ID);
      boolean processGroups = false;
      for (int i=0; i<listSilverpeasUsersIds.length; i++)
      {
        UserDetail userDetail = getOrganizationController().getUserDetail(listSilverpeasUsersIds[i]);
        String keyName = (userDetail.getFirstName()+userDetail.getLastName()).toLowerCase();
        //user to migrate
        if (listLDAPUsers.containsKey(keyName))
        {
          //Delete ldap user entry
          UserDetail userDetailLDAP = listLDAPUsers.get(keyName);
          
          adminController.synchronizeRemoveUser(userDetailLDAP.getId());

          //update silverpeas user entry (withe ldap infos)
          userDetail.setSpecificId(userDetailLDAP.getSpecificId());
          userDetail.setDomainId(userDetailLDAP.getDomainId());
          adminController.updateUser(userDetail);
          //Users processed
          processedUsers.put(keyName, userDetail);
          processGroups = true;
        }
        else
          notProcessedSPUsers.put(keyName, userDetail);
      }
      
      if (processGroups) {
        // Move groups from domainSP to mixtDomain
        Group[] groups = getOrganizationController().getAllGroups();
        SynchroReport.info("DomainSP2LDAPBatch.processMigration()", "DEBUT Migration des groupes du domaine SP vers le domaine mixte...", null);
        for (int i=0; i<groups.length; i++)
        {
          Group group = groups[i];
          boolean processGroup = false;
          
          if (DOMAIN_SILVERPEAS_ID.equals(group.getDomainId()))
          {
            UserDetail[] usersOfGroup = getAdminController().getAllUsersOfGroup(group.getId());
            for (int j=0; j<usersOfGroup.length && !processGroup; j++) {
              UserDetail userDetail = (UserDetail) usersOfGroup[j];  
              String userKeyName = (userDetail.getFirstName()+userDetail.getLastName()).toLowerCase();
              if (processedUsers.containsKey(userKeyName))
                processGroup = true;
            }
              
            if (processGroup)
            {
              int nextId = DBUtil.getNextId("ST_GROUP", "specificId");
              group.setSpecificId(new Integer(nextId).toString());
              group.setDomainId(null);
              adminController.updateGroup(group);
              SynchroReport.info("DomainSP2LDAPBatch.processMigration()", "- Groupe "+group.getName()+" avec "+group.getUserIds().length+" utilisateurs d&eacute;plac&eacute;s dans le domaine Mixte", null);
            }
          }
        }
        SynchroReport.info("DomainSP2LDAPBatch.processMigration()", "FIN Migration des groupes du domaine SP vers le domaine mixte...", null);
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
