package com.stratelia.webactiv.organization;

import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.SchemaPool;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * The OrganizationSchemaPool class manages a pool of OrganizationSchema shared
 * by all the client (admin classes).
 *
 * All the public methods are static and the calls are deferred to a singleton.
 */
public class OrganizationSchemaPool extends SchemaPool
{
  /**
   * The unique OrganizationSchemaPool built to serve all the requests.
   */
  static private OrganizationSchemaPool singleton= new OrganizationSchemaPool();

  /**
   * The constructor is private, so we can ensure that only one pool
   * will be created in the JVM.
   */
  private OrganizationSchemaPool()
  {
  }

  /**
   * Returns an Shema.
   *
   * The returned schema must be released after use.
   */
  static public OrganizationSchema getOrganizationSchema()  throws AdminPersistenceException
  {
      try
      {
          return (OrganizationSchema)singleton.getInstance();
      }
      catch (UtilException ue)
      {
          throw new AdminPersistenceException("OrganizationSchemaPool.getSchema", SilverpeasException.ERROR, "root.EX_DATASOURCE_INVALID", ue);
      }
  }

  /**
   * Release an Scheme previously returned by the pool.
   */
  static public void releaseOrganizationSchema(OrganizationSchema s)
  {
      singleton.release(s);
  }
  
  static public void releaseConnections() {
      singleton.releaseSchemas();
  }

  protected Schema newSchema(int connectionLot) throws UtilException
  {
      return (Schema)(new OrganizationSchema(connectionLot));
  }
}
