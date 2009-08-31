/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.domains.ldapdriver;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.novell.ldap.LDAPEntry;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * This class manage groups that are described as follows :
 * The group object contains an attribute that point to each users and sub-groups DN
 * Example (with the attribute 'member') :
 * member cn=user1,ou=people,o=stratelia
 * member cn=user2,ou=people,o=stratelia
 * ....
 * THOSE GROUPS ARE ALL CONSIDERED AS ROOT GROUPS
 * ALL DESCENDENT USERS ARE PUT IN FIRST LEVEL USERS
 * 
 * @author tleroi
 */

public class LDAPGroupAllRoot extends AbstractLDAPGroup
{
    protected Vector getMemberGroupIds(String lds, String memberId, boolean isGroup) throws AdminException
    {
        Vector      groupsVector = new Vector();
        LDAPEntry[] theEntries = null;
        LDAPEntry   memberEntry = null;
        int         i;

        SilverTrace.info("admin", "LDAPGroupAllRoot.getMemberGroupIds()", "root.MSG_GEN_ENTER_METHOD", "MemberId=" + memberId + ", isGroup=" + isGroup);
        if (isGroup)
        {
            memberEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(), driverSettings.getGroupsIdFilter(memberId), driverSettings.getGroupAttributes());
        }
        else
        {
            memberEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getLDAPUserBaseDN(), driverSettings.getScope(), driverSettings.getUsersIdFilter(memberId), driverSettings.getGroupAttributes());
        }
        if (memberEntry == null)
        {
			throw new AdminException("LDAPGroupAllRoot.getMemberGroupIds", SilverpeasException.ERROR, "admin.EX_ERR_LDAP_USER_ENTRY_ISNULL",  "Id="+memberId+" IsGroup="+isGroup);
        }
        String groupsMemberField = driverSettings.getGroupsMemberField();
        if ("memberUid".equals(groupsMemberField))
        {
        	theEntries = LDAPUtility.search1000Plus(lds, driverSettings.getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(), "(&" + driverSettings.getGroupsFullFilter() + "(" + driverSettings.getGroupsMemberField() + "=" + memberId + "))", driverSettings.getGroupsNameField(), driverSettings.getGroupAttributes());
        }
        else
        {
        	theEntries = LDAPUtility.search1000Plus(lds, driverSettings.getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(), "(&" + driverSettings.getGroupsFullFilter() + "(" + driverSettings.getGroupsMemberField() + "=" + LDAPUtility.dblBackSlashesForDNInFilters(memberEntry.getDN()) + "))", driverSettings.getGroupsNameField(), driverSettings.getGroupAttributes());
        }
        for (i = 0; i < theEntries.length; i++)
        {
            SilverTrace.info("admin", "LDAPGroupAllRoot.getMemberGroupIds()", "root.MSG_GEN_PARAM_VALUE", "GroupFound="+theEntries[i].getDN());
            groupsVector.add(LDAPUtility.getFirstAttributeValue(theEntries[i],driverSettings.getGroupsIdField()));
        }
        return groupsVector;
    }

    public String[] getGroupMemberGroupIds(String lds, String groupId) throws AdminException
    {
        // All root groups, so, no group belongs to another...
        return new String[0];
    }

    public String[] getUserMemberGroupIds(String lds, String userId) throws AdminException
    {
        Vector groupsIdsSet;
        Vector groupsCur;
        Iterator it;
        String grId;
        HashSet groupsManaged = new HashSet();

        groupsIdsSet = getMemberGroupIds(lds, userId, false);
        // Go recurs to all group's ancestors
        while (groupsIdsSet.size() > 0)
        {
            it = groupsIdsSet.iterator();
            groupsCur = new Vector();
            while (it.hasNext())
            {
                grId = (String)it.next();
                if (!groupsManaged.contains(grId))
                {
                    groupsManaged.add(grId);
                    groupsCur.addAll(getMemberGroupIds(lds, grId, true));
                }
            }
            groupsIdsSet = groupsCur;
        }
        return (String[])groupsManaged.toArray(new String[0]);
    }

    /**
     * Method declaration
     *
     *
     * @param lds
     * @param groupEntry
     *
     * @return
     *
     * @throws AdminException
     *
     * @see
     */
    protected String[] getUserIds(String lds, LDAPEntry groupEntry) throws AdminException
    {
        HashSet     usersManaged = new HashSet();
        HashSet     groupsManaged = new HashSet();
        Vector      groupsSet = new Vector();
        Vector      groupsCur;
        Iterator    it;
        LDAPEntry   curGroup;
        String      grId;

        groupsSet.add(groupEntry);
        while (groupsSet.size() > 0)
        {
            it = groupsSet.iterator();
            groupsCur = new Vector();
            while (it.hasNext())
            {
                curGroup = (LDAPEntry)it.next();
                if (curGroup != null)
                {
                    grId = "???";
                    try
                    {
                        grId = LDAPUtility.getFirstAttributeValue(curGroup, driverSettings.getGroupsIdField());
                        if (!groupsManaged.contains(grId))
                        {
                            groupsManaged.add(grId);
                            usersManaged.addAll(getTRUEUserIds(lds, curGroup));
                            groupsCur.addAll(getTRUEChildGroupsEntry(lds, grId, curGroup));
                        }
                    }
                    catch (AdminException e)
                    {
                        SilverTrace.info("admin", "LDAPGroupAllRoot.getUserIds()", "admin.MSG_ERR_LDAP_GENERAL", "GROUP NOT FOUND : " + grId, e);
                    }
                }
            }
            groupsSet = groupsCur;
        }
        return (String[])usersManaged.toArray(new String[0]);
    }

    protected Vector getTRUEUserIds(String lds, LDAPEntry groupEntry) throws AdminException
    {
    	SilverTrace.info("admin", "LDAPGroupAllRoot.getTRUEUserIds()", "root.MSG_GEN_ENTER_METHOD", "lds = "+lds+", group = "+groupEntry.getDN());
    	
        Vector    usersVector = new Vector();
        LDAPEntry userEntry = null;
        String[]  stringVals = null;
        int       i;

        String groupsMemberField = driverSettings.getGroupsMemberField();
        
        stringVals = LDAPUtility.getAttributeValues(groupEntry, groupsMemberField);
        for (i = 0; i < stringVals.length; i++)
        {
        	SilverTrace.info("admin", "LDAPGroupAllRoot.getTRUEUserIds()", "root.MSG_GEN_PARAM_VALUE", "stringVals["+i+"] = "+stringVals[i]);
            try
            {
            	if ("memberUid".equals(groupsMemberField))
            	{
            		//Case of most common OpenLDAP implementation.
            		//memberUid = specificId
            		userEntry = LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getLDAPUserBaseDN(), driverSettings.getScope(), driverSettings.getUsersIdFilter(stringVals[i]), driverSettings.getGroupAttributes());
                    if (userEntry != null)
                    {
                    	usersVector.add(stringVals[i]);
                    }
            	}
            	else
            	{
            		//Case of ActiveDirectory or NDS
            		//member = dn
	                userEntry = LDAPUtility.getFirstEntryFromSearch(lds, stringVals[i], driverSettings.getScope(), driverSettings.getUsersFullFilter(), driverSettings.getGroupAttributes());
	                if (userEntry != null)
	                {
	                    String userSpecificId = LDAPUtility.getFirstAttributeValue(userEntry, driverSettings.getUsersIdField());
	                    // Verify that the user exist in the scope
	                    if (LDAPUtility.getFirstEntryFromSearch(lds,driverSettings.getLDAPUserBaseDN(),driverSettings.getScope(),driverSettings.getUsersIdFilter(userSpecificId), driverSettings.getGroupAttributes()) != null)
	                    	usersVector.add(userSpecificId);
	                }
            	}
            }
            catch (AdminException e)
            {
                SilverTrace.error("admin", "LDAPGroupAllRoot.getTRUEUserIds()", "admin.MSG_ERR_LDAP_GENERAL", "USER NOT FOUND : " + LDAPUtility.dblBackSlashesForDNInFilters(stringVals[i]), e);
            }
        }
        stringVals = null;
        SilverTrace.info("admin", "LDAPGroupAllRoot.getTRUEUserIds()", "root.MSG_GEN_EXIT_METHOD");
        return usersVector;
    }

    /**
     * Method declaration
     * THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
     *
     *
     * @param lds
     * @param parentId
     *
     * @return
     *
     * @throws AdminException
     *
     * @see
     */
    protected LDAPEntry[] getChildGroupsEntry(String lds, String parentId, String extraFilter) throws AdminException
    {
        if ((parentId != null) && (parentId.length() > 0))
        { // ALL ROOT GROUPS
            return new LDAPEntry[0];
        }
        else
        {
            LDAPEntry[] theEntries = null;
            String         theFilter;
            
            if ((extraFilter != null) && (extraFilter.length() > 0))
            {
                theFilter = "(&" + extraFilter + driverSettings.getGroupsFullFilter() + ")";
            }
            else
            {
                theFilter = driverSettings.getGroupsFullFilter();
            }
            try
            {
                SilverTrace.info("admin", "LDAPGroupAllRoot.getChildGroupsEntry()", "root.MSG_GEN_PARAM_VALUE", "Root Group Search");
                theEntries = LDAPUtility.search1000Plus(lds, driverSettings.getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(), theFilter, driverSettings.getGroupsNameField(), driverSettings.getGroupAttributes());
				SynchroReport.debug("LDAPGroupAllRoot.getChildGroupsEntry()", "Récupération de " +  theEntries.length + " groupes racine",null);
            }
            catch (AdminException e)
            {
                if (synchroInProcess)
                {
                    SilverTrace.warn("admin", "LDAPGroupAllRoot.getChildGroupsEntry()", "admin.EX_ERR_CHILD_GROUPS", "ParentGroupId=" + parentId, e);
                    synchroReport.append("PB getting Group's subgroups : " + parentId + "\n");
					SynchroReport.error("LDAPGroupAllRoot.getChildGroupsEntry()", "Erreur lors de la récupération des groupes racine (parentId = "+parentId+")", e);
                }
                else
                {
                    throw e;
                }
            }
            return theEntries;
        }
    }
    
    

    /**
     * Method declaration
     * THIS FUNCTION THROW EXCEPTION ONLY WHEN NO SYNCHRO IS RUNNING
     *
     *
     * @param lds
     * @param parentId
     *
     * @return
     *
     * @throws AdminException
     *
     * @see
     */
    protected Vector getTRUEChildGroupsEntry(String lds, String parentId, LDAPEntry theEntry)
    {
        LDAPEntry   childGroupEntry = null;
        Vector      entryVector = new Vector();
        String[]    stringVals = null;
        int         i;

        if ((parentId != null) && (parentId.length() > 0))
        {
            SilverTrace.info("admin", "LDAPGroupAllRoot.getTRUEChildGroupsEntry()", "root.MSG_GEN_PARAM_VALUE", "Root Group Search : " + parentId);
            stringVals = LDAPUtility.getAttributeValues(theEntry, driverSettings.getGroupsMemberField());
            for (i = 0; i < stringVals.length; i++)
            {
                try
                {
                    childGroupEntry = LDAPUtility.getFirstEntryFromSearch(lds, stringVals[i], driverSettings.getScope(), driverSettings.getGroupsFullFilter(), driverSettings.getGroupAttributes());
                    if (childGroupEntry != null)
                    {
                        // Verify that the group exist in the scope
                        String groupSpecificId = LDAPUtility.getFirstAttributeValue(childGroupEntry, driverSettings.getGroupsIdField());
                        if (LDAPUtility.getFirstEntryFromSearch(lds, driverSettings.getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(), driverSettings.getGroupsIdFilter(groupSpecificId), driverSettings.getGroupAttributes()) != null)
                        {
                            entryVector.add(childGroupEntry);
                        }
                    }
                }
                catch (AdminException e)
                {
                    SilverTrace.error("admin", "LDAPGroupAllRoot.getTRUEChildGroupsEntry()", "admin.MSG_ERR_LDAP_GENERAL", "GROUP NOT FOUND : " + stringVals[i], e);
                }
            }
        }
        return entryVector;
    }

    public Group[] getAllChangedGroups(String lds, String extraFilter) throws AdminException
    {
        Vector groupsIdsSet;
        Vector groupsCur;
        Iterator it;
        int i;
        Hashtable groupsManaged = new Hashtable();
        LDAPEntry[] les = getChildGroupsEntry(lds, null, extraFilter);
        LDAPEntry theGroup;

        groupsIdsSet = new Vector(les.length);
        for (i = 0; i < les.length ; i++)
        {
            groupsIdsSet.add(les[i]);
            groupsManaged.put(les[i].getDN().toString(),translateGroup(lds,les[i]));
        }
        // Go recurs to all group's ancestors
        while (groupsIdsSet.size() > 0)
        {
            it = groupsIdsSet.iterator();
            groupsCur = new Vector();
            while (it.hasNext())
            {
                theGroup = (LDAPEntry)it.next();
                SilverTrace.info("admin", "LDAPGroupAllRoot.getAllChangedGroups()", "root.MSG_GEN_PARAM_VALUE", "GroupTraite2="+theGroup.getDN().toString());
                les = LDAPUtility.search1000Plus(lds, driverSettings.getGroupsSpecificGroupsBaseDN(), driverSettings.getScope(), "(&" + driverSettings.getGroupsFullFilter() + "(" + driverSettings.getGroupsMemberField() + "=" + LDAPUtility.dblBackSlashesForDNInFilters(theGroup.getDN()) + "))", driverSettings.getGroupsNameField(), driverSettings.getGroupAttributes());
                for (i = 0; i < les.length; i++)
                {
                    SilverTrace.info("admin", "LDAPGroupAllRoot.getAllChangedGroups()", "root.MSG_GEN_PARAM_VALUE", "GroupFound2="+les[i].getDN());
                    if (!groupsManaged.containsKey(les[i].getDN().toString()))
                    {
                        SilverTrace.info("admin", "LDAPGroupAllRoot.getAllChangedGroups()", "root.MSG_GEN_PARAM_VALUE", "GroupAjoute2="+les[i].getDN().toString());
                        groupsCur.add(les[i]);
                        groupsManaged.put(les[i].getDN().toString(),translateGroup(lds,les[i]));
                    }
                }
            }
            groupsIdsSet = groupsCur;
        }
        return (Group[])groupsManaged.values().toArray(new Group[0]);
    }
}
