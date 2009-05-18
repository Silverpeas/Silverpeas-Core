package com.silverpeas.jobDomainPeas;

import java.util.Collection;

/**
 * Interface de définition des webServices pour la synchronisation des utilisateurs d'un domaine
 *
 * @c.bonin
 */
public interface SynchroUserWebServiceItf
{ 
   public void startConnection();
   public void endConnection();
   public String insertUpdateDomainWebService (String idDomain, String nameDomain);
   public String insertUpdateListGroupWebService (String idDomain, String nameDomain, Collection listGroupToInsertUpdate);
   public String deleteListGroupWebService (String idDomain, Collection listGroupToInsertUpdate);
   public String insertUpdateListUserWebService(String idDomain, Collection listUserToInsertUpdate, Collection listGroupToInsertUpdate);
   public String deleteListUserWebService(String idDomain, Collection listUserToDelete);
}
