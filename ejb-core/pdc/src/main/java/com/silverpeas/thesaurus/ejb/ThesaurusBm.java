/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.thesaurus.ejb;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.EJBObject;

import com.silverpeas.thesaurus.model.Synonym;

public interface ThesaurusBm extends EJBObject {

  /**
   * Retourne les synonymes d'une valeur pour un vocabulaire donne
   * @param idTree
   * @param idTerm
   * @param idVoca
   * @return une liste de String
   * @throws RemoteException
   */
  public List<String> getSynonyms(long idTree, long idTerm, long idVoca)
      throws RemoteException;

  /**
   * Retourne les synonymes de toutes les valeurs d'un axe pour un vocabulaire donne
   * @param idTree
   * @param idTerm
   * @param idVoca
   * @return une liste de Synonym
   * @throws RemoteException
   */
  public List<Synonym> getSynonymsByTree(long idTree, long idVoca)
      throws RemoteException;

}