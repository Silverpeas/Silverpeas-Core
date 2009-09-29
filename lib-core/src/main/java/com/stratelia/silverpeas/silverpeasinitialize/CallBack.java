package com.stratelia.silverpeas.silverpeasinitialize;

/**
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 * 
 * @author TLE
 * @version 1.0
 */
abstract public class CallBack {
  abstract public void subscribe();

  abstract public void doInvoke(int action, int iParam, String sParam,
      Object extraParam);
}
