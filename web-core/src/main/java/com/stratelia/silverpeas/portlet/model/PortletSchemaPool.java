package com.stratelia.silverpeas.portlet.model;

import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.SchemaPool;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * The PortletSchemaPool class manages a pool of PortletSchema shared by all the
 * client (admin classes).
 * 
 * All the public methods are static and the calls are deferred to a singleton.
 */
public class PortletSchemaPool extends SchemaPool {
  /**
   * The unique PortletSchemaPool built to serve all the requests.
   */
  static private PortletSchemaPool singleton = new PortletSchemaPool();

  /**
   * The constructor is private, so we can ensure that only one pool will be
   * created in the JVM.
   */
  private PortletSchemaPool() {
  }

  /**
   * Returns an Shema.
   * 
   * The returned schema must be released after use.
   */
  static public PortletSchema getPortletSchema() throws UtilException {
    return (PortletSchema) singleton.getInstance();
  }

  /**
   * Release an Scheme previously returned by the pool.
   */
  static public void releasePortletSchema(PortletSchema s) {
    singleton.release(s);
  }

  static public void releaseConnections() {
    singleton.releaseSchemas();
  }

  protected Schema newSchema(int connectionLot) throws UtilException {
    return (Schema) (new PortletSchema(connectionLot));
  }
}
