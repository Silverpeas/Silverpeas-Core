package com.stratelia.webactiv.searchEngine.model;

import java.io.Serializable;
import java.net.URLEncoder;

import com.stratelia.webactiv.util.indexEngine.model.*;

/**
 * A MatchingIndexEntry is an IndexEntry completed
 * with a score by the search engine.
 */
public class MatchingIndexEntry extends IndexEntry
                                implements Serializable
{
  /**
   * The constructor set only the key part of the entry.
   */
  public MatchingIndexEntry(String space,
                            String component,
                            String objectType,
                            String objectId)
  {
    super(component, objectType, objectId);
  }

  /**
   * The constructor set only the key part of the entry.
   */
  public MatchingIndexEntry(IndexEntryPK pk)
  {
    super(pk);
  }
  /**
   * Return the score of this entry according the request.
   */
  public float getScore()
  {
    return score;
  }

  /**
   * Set the score of this entry.
   *
   * Only the searchEngine should call this method.
   */
  public void setScore(float score)
  {
    this.score = score;
  }

  /**
   * Returns web'activ logic parameters for the URL used to displayed this entry.
   *
   */
  public String getPageAndParams()
  { 
        String type = URLEncoder.encode(getObjectType());
        String id = URLEncoder.encode(getObjectId());

        return "searchResult?Type=" + type + "&Id="+id;
  }

  public String getParams()
  { 
        String id = URLEncoder.encode(getObjectId());

        return "documentId%3d"+id;
  }

  /**
   * The score defaults to 0 as if the entry wasn't a matching entry.
   */
  private float score = 0;
}
