package com.stratelia.webactiv.persistence;

import java.util.HashMap;
import java.util.Map;

import com.stratelia.webactiv.persistence.database.SilverpeasBeanDAOImpl;

public class SilverpeasBeanDAOFactory {

  private static Map silverpeasBeanDAOs = new HashMap();

  public static SilverpeasBeanDAO getDAO(String beanName)
      throws PersistenceException {
    SilverpeasBeanDAO result = (SilverpeasBeanDAO) silverpeasBeanDAOs
        .get(beanName);
    if (result == null) {
      result = new SilverpeasBeanDAOImpl(beanName);
      silverpeasBeanDAOs.put(beanName, result);
    }
    return result;
  }

}
