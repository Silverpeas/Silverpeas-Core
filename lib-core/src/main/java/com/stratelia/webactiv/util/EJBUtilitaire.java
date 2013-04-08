/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import sun.security.jca.GetInstance;
import com.silverpeas.util.StringUtil;
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

  private ResourceLocator resources = new ResourceLocator("org.silverpeas.util.jndi", "");
  private static EJBUtilitaire instance;
   private static EJBUtilitaire realInstance = null;
  private Map<String, Object> homeFetcher = new HashMap<String, Object>();

  private EJBUtilitaire() {
  }

  protected static EJBUtilitaire getInstance() {
    synchronized (EJBUtilitaire.class) {
      if (instance == null) {
        instance = new EJBUtilitaire();
      }
    }
    return instance;
  }

  public static void mock(EJBUtilitaire mock) {
    synchronized (EJBUtilitaire.class) {
      realInstance = instance;
      instance = mock;
    }
  }
  
  public static void unmock() {
    synchronized (EJBUtilitaire.class) {
      instance = realInstance;
      realInstance = null;
    }
  }

  private Context initialiseContext() {
    Context ic = null;
    try {
      Hashtable<String, String> env = new Hashtable<String, String>();
      String jnfi = resources.getString("java.naming.factory.initial", null);
      String jnpu = resources.getString("java.naming.provider.url", null);
      if (jnfi != null && jnfi.length() > 0) {
        env.put(Context.INITIAL_CONTEXT_FACTORY, jnfi);
      }
      if (jnpu != null && jnpu.length() > 0) {
        env.put(Context.PROVIDER_URL, jnpu);
      }
      ic = new InitialContext(env);
    } catch (NamingException e) {
      UtilException ue = new UtilException("EJBUtilitaire.getEJBObjectRef",
          "util.MSG_CANT_GET_INITIAL_CONTEXT", e);
      throw ue;
    }
    return ic;
  }

  /**
   * Return a remote object. Using example : PublicationHome pubHome = (PublicationHome)
   * EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATION_EJBHOME, PublicationHome.class);
   *
   * @return a remote object
   * @param name the JNDI name of the object
   * @param classObj the class file name of the object
   * @since 1.0
   */
  @SuppressWarnings({"unchecked", "unchecked"})
  public <T> T getObjectReference(String name, Class<T> classObj) throws UtilException {
    T objRef = (T) homeFetcher.get(name);
    if (objRef == null) {
      SilverTrace.debug("util", "EJBUtilitaire.getEJBObjectRef", name);
      Context ic = initialiseContext();
      try {
        Object ref = ic.lookup(name);
        objRef = (T) PortableRemoteObject.narrow(ref, classObj);
        String withHomeFetcher = resources.getString("withHomeFetcher");
        if (withHomeFetcher == null || StringUtil.getBooleanValue(withHomeFetcher)) {
          homeFetcher.put(name, objRef);
        }
      } catch (Exception e) {
        UtilException ue = new UtilException("EJBUtilitaire.getEJBObjectRef", new MultilangMessage(
            "util.MSG_EJB_REF_NOT_FOUND", name).toString(), e);
        throw ue;
      }
    }
    return objRef;
  }

  /**
   * Return a remote object Using example : PublicationHome pubHome = (PublicationHome)
   * EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATION_EJBHOME, PublicationHome.class);
   *
   * @param <T> 
   * @return a remote object
   * @throws UtilException 
   * @param name the JNDI name of the object
   * @param classObj the class file name of the object
   * @since 1.0
   */
  @SuppressWarnings("unchecked")
  public static <T> T getEJBObjectRef(String name, Class<T> classObj) throws UtilException {
    return getInstance().getObjectReference(name, classObj);
  }
}