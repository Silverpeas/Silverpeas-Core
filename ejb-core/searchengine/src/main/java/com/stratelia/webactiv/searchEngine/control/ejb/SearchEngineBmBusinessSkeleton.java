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
