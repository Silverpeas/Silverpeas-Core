package com.silverpeas.thesaurus.ejb;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.EJBObject;

public interface ThesaurusBm extends EJBObject {

  /**
   * Retourne les synonymes d'une valeur pour un vocabulaire donné
   * 
   * @param idTree
   * @param idTerm
   * @param idVoca
   * @return une liste de String
   * @throws RemoteException
   */
  public List getSynonyms(long idTree, long idTerm, long idVoca)
      throws RemoteException;

  /**
   * Retourne les synonymes de toutes les valeurs d'un axe pour un vocabulaire
   * donné
   * 
   * @param idTree
   * @param idTerm
   * @param idVoca
   * @return une liste de Synonym
   * @throws RemoteException
   */
  public List getSynonymsByTree(long idTree, long idVoca)
      throws RemoteException;

}