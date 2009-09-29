/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.searchEngine.control.ejb;

import java.rmi.RemoteException;

import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.ParseException;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;

/**
 * With the SearchEngineBmBusinessSkeleton interface, which is extended by
 * SearchEngineBm and SearchEngineEJB, we can verify at compile time that
 * SearchEngineEJB expose the expected interface : the SearchEngineBm's one.
 */
public interface SearchEngineBmBusinessSkeleton {
  /**
   * Search the index for the required documents.
   */
  void search(QueryDescription query) throws RemoteException, ParseException;

  /**
   * Return the count of matching document retrived by the last search.
   * 
   * Return 0 if called before the fisrt search.
   */
  int getResultLength() throws RemoteException;

  /**
   * Return the index entry at the given position i (counted from 0) from all
   * the entries retrived by the last search.
   * 
   * Return null if the given position i is out of bounds.
   */
  MatchingIndexEntry get(int i) throws RemoteException;

  /**
   * Return the entries comprised between the position min upto the position
   * max, from all the entries retrived by the last search.
   * 
   * The positions min and max are counted from 0 and included in the range.
   */
  MatchingIndexEntry[] getRange(int min, int max) throws RemoteException;
}
