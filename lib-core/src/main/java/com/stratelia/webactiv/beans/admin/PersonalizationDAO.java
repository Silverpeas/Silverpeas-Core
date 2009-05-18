package com.stratelia.webactiv.beans.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;

/**
 * Class declaration
 *
 *
 * @author
 */
public class PersonalizationDAO
{
    public static final String PERSONALTABLENAME = "Personalization";
  
    public static Hashtable getUsersLanguage(Connection con, List userIds) throws SQLException
    {
    	Hashtable usersLanguage = new Hashtable();
    	if (userIds == null || userIds.size() == 0)
    		return usersLanguage;
    	
    	SilverTrace.info("personalization","PersonalizationDAO.getUsersLanguage()", "root.MSG_GEN_ENTER_METHOD", "# of userId =" + userIds.size());
    	
    	StringBuffer select = new StringBuffer("select id, languages from ").append(PERSONALTABLENAME).append(" WHERE (");
    	String userId = null;
    	Iterator it = userIds.iterator();
    	while (it.hasNext())
    	{
    		userId = (String) it.next();
    		select.append("id = '").append(userId).append("'");
    		if (it.hasNext())
    			select.append(" OR ");
    	}
    	select.append(")");
    	
    	PreparedStatement stmt = null;
    	ResultSet results = null;

    	try {
			stmt = con.prepareStatement(select.toString());
			results = stmt.executeQuery();
			String language = null;
			while (results.next())
			{
				userId = results.getString(1);
				language = results.getString(2);
				language = language.substring(0, language.indexOf(",")); //remove ended ','
				usersLanguage.put(userId, language);
			}
			
			//All requested users are not in the table
			//For them, put default language
			if (usersLanguage.size() < userIds.size())
			{
				Set sUserIds = usersLanguage.entrySet();
				userIds.removeAll(sUserIds);
				it = userIds.iterator();
				while (it.hasNext())
				{
					userId = (String) it.next();
					usersLanguage.put(userId, "fr");
				}
			}
			
		}
    	finally
		{
			DBUtil.close(results, stmt);
		}
    	return usersLanguage;
    }
}