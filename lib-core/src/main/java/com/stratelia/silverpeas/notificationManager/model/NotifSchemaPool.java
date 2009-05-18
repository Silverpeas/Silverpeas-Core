package com.stratelia.silverpeas.notificationManager.model;

import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.SchemaPool;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * The NotifSchemaPool class manages a pool of NotifSchema shared
 * by all the client (admin classes).
 *
 * All the public methods are static and the calls are deferred to a singleton.
 */
public class NotifSchemaPool extends SchemaPool
{
  /**
   * The unique NotifSchemaPool built to serve all the requests.
   */
  static private NotifSchemaPool singleton= new NotifSchemaPool();

  /**
   * The constructor is private, so we can ensure that only one pool
   * will be created in the JVM.
   */
  private NotifSchemaPool()
  {
  }

  /**
   * Returns an Shema.
   *
   * The returned schema must be released after use.
   */
  static public NotifSchema getNotifSchema()  throws UtilException
  {
      return (NotifSchema)singleton.getInstance();
  }

  /**
   * Release an Scheme previously returned by the pool.
   */
  static public void releaseNotifSchema(NotifSchema s)
  {
      singleton.release(s);
  }
  
  static public void releaseConnections() {
      singleton.releaseSchemas();
  }

  protected Schema newSchema(int connectionLot) throws UtilException
  {
      return (Schema)(new NotifSchema(connectionLot));
  }
}
