/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.beans.admin;

import com.stratelia.webactiv.util.ResourceLocator;

/*
 * CVS Informations
 * 
 * $Id: DomainProperty.java,v 1.5 2008/06/26 04:58:01 neysseri Exp $
 * 
 * $Log: DomainProperty.java,v $
 * Revision 1.5  2008/06/26 04:58:01  neysseri
 * Ajout de la description sur chaque champ personnel du domaine
 *
 * Revision 1.4  2008/03/12 16:37:35  neysseri
 * no message
 *
 * Revision 1.3.4.3  2008/01/15 08:17:30  neysseri
 * no message
 *
 * Revision 1.3.4.2  2007/12/31 14:28:24  neysseri
 * no message
 *
 * Revision 1.3.4.1  2007/12/27 13:05:47  neysseri
 * no message
 *
 * Revision 1.3  2007/04/17 09:36:13  neysseri
 * Dév FNMJ/Ganesha
 *
 * Revision 1.2.20.1  2007/02/28 09:15:54  cbonin
 * Ajout du type BOOLEAN
 *
 * Revision 1.2  2004/11/05 14:32:29  neysseri
 * Nettoyage sources
 *
 * Revision 1.1.1.1  2002/08/06 14:47:40  nchaix
 * no message
 *
 * Revision 1.1  2002/04/08 14:09:27  tleroi
 * no message
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author t.leroi
 */
public class DomainProperty {
  final static public String PROPERTY_TYPE_STRING = "STRING";
  final static public String PROPERTY_TYPE_USERID = "USERID";
  final static public String PROPERTY_TYPE_BOOLEAN = "BOOLEAN";

  final static public String PROPERTY_UPDATEALLOWED_ADMIN = "A";
  final static public String PROPERTY_UPDATEALLOWED_USER = "U";

  private String m_sName = null;
  private String m_iType = PROPERTY_TYPE_STRING;
  private String m_sMapParameter = null;
  private boolean usedToImport = false;
  private String redirectOU = null;
  private String redirectAttribute = null;
  private String updateAllowedTo = PROPERTY_UPDATEALLOWED_USER;

  private String label = null;
  private String description = null;

  public DomainProperty() {
  }

  public DomainProperty(ResourceLocator rs, String num) {
    String s;

    m_sName = rs.getString("property_" + num + ".Name");
    m_iType = PROPERTY_TYPE_STRING;
    s = rs.getString("property_" + num + ".Type");
    if ((s != null) && (s.length() > 0)) {
      if (s.equalsIgnoreCase("USERID")) {
        m_iType = PROPERTY_TYPE_USERID;
      } else if (s.equalsIgnoreCase("BOOLEAN")) {
        m_iType = PROPERTY_TYPE_BOOLEAN;
      }
    }
    m_sMapParameter = rs.getString("property_" + num + ".MapParameter");
    usedToImport = rs.getBoolean("property_" + num + ".UsedToImport", false);
    redirectOU = rs.getString("property_" + num + ".RedirectOU");
    redirectAttribute = rs.getString("property_" + num + ".RedirectAttribute");
    updateAllowedTo = rs.getString("property_" + num + ".UpdateAllowedTo",
        PROPERTY_UPDATEALLOWED_USER);
  }

  public void setName(String propertyName) {
    m_sName = propertyName;
  }

  public String getName() {
    return m_sName;
  }

  public void setType(String propertyType) {
    m_iType = propertyType;
  }

  public String getType() {
    return m_iType;
  }

  public void setMapParameter(String mapParameter) {
    m_sMapParameter = mapParameter;
  }

  public String getMapParameter() {
    return m_sMapParameter;
  }

  public boolean isUsedToImport() {
    return usedToImport;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getRedirectAttribute() {
    return redirectAttribute;
  }

  public String getRedirectOU() {
    return redirectOU;
  }

  public boolean isUpdateAllowedToUser() {
    return PROPERTY_UPDATEALLOWED_USER.equalsIgnoreCase(updateAllowedTo);
  }

}