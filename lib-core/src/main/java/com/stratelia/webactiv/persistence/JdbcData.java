package com.stratelia.webactiv.persistence;

/**
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 * 
 * @author
 * @version 1.0
 */

public class JdbcData {

  public String JDBCdriverName;
  public String JDBCurl;
  public String JDBClogin;
  public String JDBCpassword;

  public JdbcData(String p_JDBCdriverName, String p_JDBCurl,
      String p_JDBClogin, String p_JDBCpassword) {
    JDBCdriverName = p_JDBCdriverName;
    JDBCurl = p_JDBCurl;
    JDBClogin = p_JDBClogin;
    JDBCpassword = p_JDBCpassword;
  }

}