package com.silverpeas.workflow.engine.user;

import com.silverpeas.workflow.api.user.User;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;

/**
 * A User implementation
 * built upon the silverpeas user management system.
 */
public final class UserImpl implements User
{
   /**
    * A UserImpl is a facade to silverpeas UserDetail
    */
   private UserDetail userDetail = null;

   /**
    * A UserImpl is a facade to silverpeas UserFull too
	* only loaded on demand
    */
   private UserFull userFull = null;

   /**
    * The UserImpl shares a silverpeas Admin object
	*/
   static private Admin admin = null;

   /**
    * UserImpl is built from a UserDetail and admin .
    */
   public UserImpl(UserDetail userDetail, Admin admin)
   {
      this.userDetail = userDetail;
	  if (this.admin == null)
		  this.admin = admin;
   }
   
   /**
    * Returns the user id
    */
   public String getUserId()
   {
      return userDetail.getId();
   }

   /**
    * Returns the user full name (firstname lastname)
    */
   public String getFullName()
   {
      return userDetail.getFirstName() + " " + userDetail.getLastName();
   }
   
   /**
    * returns all the known info for an user;
    * Each returned value can be used as a parameter to the
    * User method getInfo().
    */
   static public String[] getUserInfoNames()
   {
      return infoNames;
   }
   static private String[] infoNames = { "bossId" };
   
	/**
	 * Returns the named info
	 */
	public String getInfo(String infoName)
	{
		if (userFull == null)
		{
			if (admin == null)
				return "";

			try
			{
				userFull = admin.getUserFull( getUserId() );
			}
			catch (AdminException e)
			{
				return "";
			}

			if (userFull == null)
				return "";
		}

		return userFull.getValue(infoName);
	}

   /**
    * compare this user with another
	* @return	true if two users are the same
	*/
	public boolean equals(Object user)
	{
		return this.getUserId().equals( ((UserImpl) user).getUserId());
	}
}
