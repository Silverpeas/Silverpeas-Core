package com.stratelia.webactiv.util.exception;

/**
 * Cette interface doit etre implémentée par toutes les exception souhaitant
 * être monitorés. La méthode getModule() doit être implementée pour permettre
 * de connaitre le nom du module ayant généré cette exception. Par exemple, pour
 * le module d'administration, on va définir une AdminException qui "extends"
 * SilverpeasException, et "implements" FromModuleException. La méthode
 * getModule devra renvoyer une chaine du style "Admin".
 * 
 */
public interface FromModule {
  /**
   * This function must be defined by the Classes that herit from this one
   * 
   * @return The SilverTrace's module name
   **/
  public String getModule();

  public String getMessageLang();

  public String getMessageLang(String language);

  public void traceException();

  public int getErrorLevel();
}
