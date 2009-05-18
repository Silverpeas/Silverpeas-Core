package com.stratelia.webactiv.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.MultilangMessage;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * This class provides different EJB services.
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class EJBUtilitaire {

  private static Hashtable<String, Object> homeFetcher = new Hashtable<String, Object>();

  /**
   * Return a remote object Using example : PublicationHome pubHome =
   * (PublicationHome)
   * EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATION_EJBHOME,
   * PublicationHome.class);
   * 
   * @return a remote object
   * @param name
   *          the JNDI name of the object
   * @param classObj
   *          the class file name of the object
   * @since 1.0
   */
  public static java.lang.Object getEJBObjectRef(String name, Class<?> classObj)
      throws UtilException {
    java.lang.Object objRef = homeFetcher.get(name);
    if (objRef == null) {
      Context ic = null;
      ResourceLocator resources = null;
      try {
        SilverTrace.debug("util", "EJBUtilitaire.getEJBObjectRef", name);
        Hashtable<String, String> env = new Hashtable<String, String>();
        resources = new ResourceLocator("com.stratelia.webactiv.util.jndi", "");
        String jnfi = resources.getString("java.naming.factory.initial", null);
        String jnpu = resources.getString("java.naming.provider.url", null);
        if (jnfi != null && jnfi.length() > 0) {
          env.put(Context.INITIAL_CONTEXT_FACTORY, jnfi);
        }
        if (jnpu != null && jnpu.length() > 0) {
          env.put(Context.PROVIDER_URL, jnpu);
        }
        ic = new InitialContext(env);
      } catch (Exception e) {
        UtilException ue = new UtilException("EJBUtilitaire.getEJBObjectRef",
            "util.MSG_CANT_GET_INITIAL_CONTEXT", e);
        throw ue;
      }
      try {
        objRef = ic.lookup(name);
        objRef = PortableRemoteObject.narrow(objRef, classObj);
        String withHomeFetcher = resources.getString("withHomeFetcher");
        if (withHomeFetcher == null) {
          homeFetcher.put(name, objRef);
        } else if ("yes".equalsIgnoreCase(withHomeFetcher)) {
          homeFetcher.put(name, objRef);
        }
      } catch (Exception e) {
        UtilException ue = new UtilException(
            "EJBUtilitaire.getEJBObjectRef",
            new MultilangMessage("util.MSG_EJB_REF_NOT_FOUND", name).toString(),
            e);
        // ue.setPossibleReason("util.REASON_EJB_REF_NOT_FOUND");
        throw ue;
      }
    }
    return objRef;
  }

}