package com.stratelia.silverpeas.domains.ldapdriver;

import java.util.Collection;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Interface de définition des méthodes pour la synchronisation des utilisateurs d'un domaine LDAP
 * @c.bonin
 */
public interface LDAPSynchroUserItf {
  public void processUsers(Collection<UserDetail> listUserCreate,
      Collection<UserDetail> listUserUpdate, Collection<UserDetail> listUserDelete)
      throws SilverpeasException;

  public UserFull getCacheUserFull(UserDetail user) throws SilverpeasException;
}
