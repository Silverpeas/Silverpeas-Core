package com.stratelia.webactiv.searchEngine.control.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.ParseException;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.searchEngine.model.WAIndexSearcher;

/**
 * A SearchEngineEJB search the web'activ index
 * and give access to the retrieved index entries.
 */
public class SearchEngineEJB implements SessionBean,
                                        SearchEngineBmBusinessSkeleton
{
  /**
   * Create a SearchEngineBm.
   *
   * The results set is initialized empty.
   */
  public void ejbCreate() throws CreateException, RemoteException
  {
    results = new MatchingIndexEntry[0];
  }

  /**
   * Search the index for the required documents.
   */
  public void search(QueryDescription query) throws RemoteException,
                                                    ParseException
  {
    results = new WAIndexSearcher().search(query);
  }

  /**
   * Return the count of matching document retrived by the last search.
   *
   * Return 0 if called before the fisrt search.
   */
  public int getResultLength() throws RemoteException
  {
    return results.length;
  }

  /**
   * Return the index entry at the given position i (counted from 0)
   * from all the entries retrived by the last search.
   *
   * Return null if the given position i is out of bounds.
   */
  public MatchingIndexEntry get(int i) throws RemoteException
  {
    if ( 0 <= i && i < results.length ) return results[i];
    else return null;
  }

  /**
   * Return the entries comprised between the position min
   * upto the position max,
   * from  all the entries retrived by the last search.
   *
   * The positions min and max are counted from 0 and included in the range.
   * If min or max is out of bounds : the range is resize to fit the
   * results set.
   */
  public MatchingIndexEntry[] getRange(int min, int max) throws RemoteException
  {
    if (min < 0) min = 0;
    if (max >= results.length) max = results.length -1;
    if (min <= max)
    {
      MatchingIndexEntry[] extraction = new MatchingIndexEntry[max-min+1];
      for (int i=0; i<max-min+1 ; i++)
      {
        extraction[i] = results[min + i];
      }
      return extraction;
    }
    else return new MatchingIndexEntry[0];
  }

  /**
   * The no parameters constructor is required for an EJB.
   */
  public SearchEngineEJB()
  {
  }

  /**
   * The last results set is released.
   */
  public void ejbRemove()
  {
    results = null;
  }

  /**
   * The session context is useless.
   */
  public void setSessionContext(SessionContext sc)
  {
  }

  /**
   * There is no ressources to be released.
   */
  public void ejbPassivate()
  {
  }

  /**
   * There is no ressources to be restored.
   */
  public void ejbActivate()
  {
  }

  /**
   * The results of the last search.
   */
  private MatchingIndexEntry[] results = null;
}
