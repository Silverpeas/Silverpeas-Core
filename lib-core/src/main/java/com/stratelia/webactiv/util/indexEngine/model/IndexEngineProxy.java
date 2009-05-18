package com.stratelia.webactiv.util.indexEngine.model;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;

/**
 * The IndexEngineProxy class encapsulates
 * the calls to the index engine server.
 */
public final class IndexEngineProxy
{
  /**
   * The IndexEngine class is only used via static methods
   * and no IndexEngine object will ever be constructed.
   */
  private IndexEngineProxy()
  {
  }

  /**
   * Add an entry index.
   */
  static public void addIndexEntry(FullIndexEntry indexEntry)
  {
	init();
	if (indexEngine != null)
	{
		IndexerThread.addIndexEntry(indexEntry);      
	}
	else
	{
	  SilverTrace.error("indexEngine", "IndexEngineProxy",
	                 "indexEngine.MSG_ADD_REQUEST_IGNORED");
	}
  }

  /**
   * Remove an entry index.
   */
  static public void removeIndexEntry(IndexEntryPK indexEntry)
  {
	  init();
	  if (indexEngine != null)
	  {
		  IndexerThread.removeIndexEntry(indexEntry);     
	  }
	  else
	  {
		  SilverTrace.error("indexEngine", "IndexEngineProxy",
		                 "indexEngine.MSG_REMOVE_REQUEST_IGNORED");
	  }
  }

  /**
   * Initialize the class, if this is not already done.
   */
  static private void init()
  {
	  String rootPath = FileRepositoryManager.getAbsoluteIndexPath("x", "x");

	  if (rootPath == null)
	  {
		  SilverTrace.fatal("indexEngine", "IndexEngineEJB",
		                    "indexEngine.MSG_INDEX_FILES_UNFOUND");
	      indexEngine = null;
	      return;
	  }
	    
	  IndexerThread.start(new IndexManager());
	  indexEngine = "indexEngine";
  }

  /**
   * The indexEngineBm to which all the requests are forwarded.
   */
  static private String indexEngine = null;
}
