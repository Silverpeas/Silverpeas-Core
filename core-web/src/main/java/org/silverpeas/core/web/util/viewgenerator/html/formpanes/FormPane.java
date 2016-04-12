/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * FormPane.java
 *
 * Created on 13 decembre 2000
 */
package org.silverpeas.core.web.util.viewgenerator.html.formpanes;

import java.util.Vector;
import javax.servlet.jsp.PageContext;

import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;
import org.silverpeas.core.util.Charsets;

/**
 * The FormPane interface gives us the skeleton for all funtionnalities we need to display typical
 * WA form table pane.
 *
 * @author frageade
 * @version 1.0
 */
public abstract class FormPane implements SimpleGraphicElement {

  // Membres statiques
  public static final String PUBLIEUR_CODE = "publieur";
  public static final String UTILISATEUR_CODE = "utilisateur";
  public static final String DEFAULT_LANGUAGE = "fr";
  // Membres
  protected Vector formLines;
  protected Vector formHiddenFields;
  protected Vector formActionButtons;
  protected String actionURL;
  protected String formMethod;
  protected String title = null;
  protected String name;
  protected String displayName;
  protected String description;
  protected PageContext pageContext = null;
  protected boolean reply;
  protected LocalizationBundle message;
  // Modes de gestion
  protected boolean utilisateurArchivageDemandes;
  protected boolean publieurArchivageDemandes;
  protected boolean utilisateurArchivageReponses;
  protected boolean publieurArchivageReponses;
  protected boolean utilisateurEnvoiDemandes;

  /**
   * Constructor declaration
   *
   * @param nam
   * @param url
   * @param pc
   * @see
   */
  public FormPane(String nam, String url, PageContext pc) {
    formLines = new Vector();
    formHiddenFields = new Vector();
    formActionButtons = new Vector();
    name = nam;
    description = "";
    displayName = nam;
    pageContext = pc;
    actionURL = url;
    formMethod = "post";
    reply = false;
    utilisateurArchivageDemandes = false;
    publieurArchivageDemandes = false;
    utilisateurArchivageReponses = false;
    publieurArchivageReponses = false;
    utilisateurEnvoiDemandes = false;
    message = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.util.viewGenerator.formPane.formPaneBundle",
        DEFAULT_LANGUAGE);
  }

  /**
   * Method declaration
   *
   * @param title
   * @see
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getTitle() {
    return title;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getName() {
    return name;
  }

  /**
   * Method declaration
   *
   * @param nam
   * @see
   */
  public void setName(String nam) {
    name = nam;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Method declaration
   *
   * @param nam
   * @see
   */
  public void setDisplayName(String nam) {
    displayName = nam;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getDescription() {
    return description;
  }

  /**
   * Method declaration
   *
   * @param des
   * @see
   */
  public void setDescription(String des) {
    description = des;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public PageContext getPageContext() {
    return pageContext;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public Vector getFormLines() {
    return formLines;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public int getNbLines() {
    return formLines.size() + formHiddenFields.size()
        + formActionButtons.size();
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public boolean hasReply() {
    return reply;
  }

  /**
   * Method declaration
   *
   * @param rep
   * @see
   */
  public void setReply(boolean rep) {
    reply = rep;
    if (rep) {
      utilisateurEnvoiDemandes = true;
      publieurArchivageDemandes = true;
    }
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public boolean getUtilisateurArchivageDemandes() {
    return utilisateurArchivageDemandes;
  }

  /**
   * Method declaration
   *
   * @param uad
   * @see
   */
  public void setUtilisateurArchivageDemandes(boolean uad) {
    utilisateurArchivageDemandes = uad;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public boolean getPublieurArchivageDemandes() {
    return publieurArchivageDemandes;
  }

  /**
   * Method declaration
   *
   * @param uad
   * @see
   */
  public void setPublieurArchivageDemandes(boolean uad) {
    publieurArchivageDemandes = uad;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public boolean getUtilisateurArchivageReponses() {
    return utilisateurArchivageReponses;
  }

  /**
   * Method declaration
   *
   * @param uad
   * @see
   */
  public void setUtilisateurArchivageReponses(boolean uad) {
    utilisateurArchivageReponses = uad;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public boolean getPublieurArchivageReponses() {
    return publieurArchivageReponses;
  }

  /**
   * Method declaration
   *
   * @param uad
   * @see
   */
  public void setPublieurArchivageReponses(boolean uad) {
    publieurArchivageReponses = uad;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public boolean getUtilisateurEnvoiDemandes() {
    return utilisateurEnvoiDemandes;
  }

  /**
   * Method declaration
   *
   * @param uad
   * @see
   */
  public void setUtilisateurEnvoiDemandes(boolean uad) {
    utilisateurEnvoiDemandes = uad;
  }

  /**
   * Method declaration
   *
   * @param language
   * @see
   */
  public void setLanguage(String language) {
    if (language != null) {
      message = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.util.viewGenerator.formPane.formPaneBundle",
          language);
    }
  }

  /**
   * Method declaration
   *
   * @param line
   * @see
   */
  public void add(FormLine line) {
    line.setPane(this);
    line.setLanguage(message.getLocale().getLanguage());
    if (line.getType().equals("hidden")) {
      formHiddenFields.add(line);
    } else if (line.getType().equals("button")) {
      formActionButtons.add(line);
    } else if (line.getType().equals("buttonLine")) {
      formActionButtons.add(line);
    } else {
      formLines.add(line);
    }
  }

  /**
   * Method declaration
   *
   * @param lineName
   * @see
   */
  public void remove(String lineName) {
    boolean search = true;
    int i;

    if ((search) && (formLines.size() > 0)) {
      i = 0;
      while ((search) && (i < formLines.size())) {
        FormLine fl = (FormLine) formLines.elementAt(i);

        if (fl.getName().equals(lineName)) {
          formLines.removeElementAt(i);
          search = false;
        }
        i++;
      }
    }
    if ((search) && (formHiddenFields.size() > 0)) {
      i = 0;
      while ((search) && (i < formHiddenFields.size())) {
        FormLine fl = (FormLine) formHiddenFields.elementAt(i);

        if (fl.getName().equals(lineName)) {
          formHiddenFields.removeElementAt(i);
          search = false;
        }
        i++;
      }
    }
    if ((search) && (formActionButtons.size() > 0)) {
      i = 0;
      while ((search) && (i < formActionButtons.size())) {
        FormLine fl = (FormLine) formActionButtons.elementAt(i);

        if (fl.getName().equals(lineName)) {
          formActionButtons.removeElementAt(i);
          search = false;
        }
        i++;
      }
    }
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String toXML() {
    int i;
    String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    result = result + "\n\n" + getDTD();
    result = result + "\n\n<formulaire>";
    result = result + "\n<name>" + name + "</name>";
    result = result + "\n<displayName>" + displayName + "</displayName>";
    result = result + "\n<description>" + description + "</description>";
    result = result + "\n<reply>" + String.valueOf(reply) + "</reply>";
    result = result + "\n<utilisateurArchivageDemandes>"
        + String.valueOf(utilisateurArchivageDemandes)
        + "</utilisateurArchivageDemandes>";
    result = result + "\n<publieurArchivageDemandes>"
        + String.valueOf(publieurArchivageDemandes)
        + "</publieurArchivageDemandes>";
    result = result + "\n<utilisateurArchivageReponses>"
        + String.valueOf(utilisateurArchivageReponses)
        + "</utilisateurArchivageReponses>";
    result = result + "\n<publieurArchivageReponses>"
        + String.valueOf(publieurArchivageReponses)
        + "</publieurArchivageReponses>";
    result = result + "\n<utilisateurEnvoiDemandes>"
        + String.valueOf(utilisateurEnvoiDemandes)
        + "</utilisateurEnvoiDemandes>";

    // Champs "hidden"
    if (formHiddenFields.size() > 0) {
      for (i = 0; i < formHiddenFields.size(); i++) {
        result = result + ((FormLine) formHiddenFields.elementAt(i)).toXML();
      }
    }

    // Champs de saisie du formulaire
    if (formLines.size() > 0) {
      for (i = 0; i < formLines.size(); i++) {
        result = result + "\n" + ((FormLine) formLines.elementAt(i)).toXML();
      }
    }

    // Boutons d'actions
    if (formActionButtons.size() > 0) {
      for (i = 0; i < formActionButtons.size(); i++) {
        result = result + "\n"
            + ((FormLine) formActionButtons.elementAt(i)).toXML();
      }
    }
    result = result + "\n</formulaire>";
    return toXMLString(result);
  }

  private String toXMLString(String s) {
    byte[] readbytes = s.getBytes(Charsets.UTF_8);
    return new String(readbytes);
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getBuildRequest() {
    String result = "";
    int i;

    // Champs de saisie du formulaire
    if (formLines.size() > 0) {
      for (i = 0; i < formLines.size(); i++) {
        result = result
            + ((FormLine) formLines.elementAt(i)).getDBColumnCreationRequest();
      }
    }

    return result;
  }

  /**
   * Method declaration
   *
   * @param resultVector
   * @see
   */
  public void initFromVector(Vector resultVector) {
    int i;
    int j = 0;

    for (i = 0; i < formLines.size(); i++) {
      FormLine fl = (FormLine) formLines.elementAt(i);

      if (fl.isDBEntry()) {
        fl.setValue((String) resultVector.elementAt(j));
        j++;
      }
    }
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getDTD() {
    String retour = "<!DOCTYPE formulaire [";

    retour =
        retour
        + "\n<!ELEMENT formulaire (name, displayName, description, reply, utilisateurArchivageDemandes, publieurArchivageDemandes,";
    retour =
        retour
        + "\n       utilisateurArchivageReponses, publieurArchivageReponses, utilisateurEnvoiDemandes, field*)>";
    retour = retour
        + "\n<!ELEMENT field (name, label, value, rows?, cols?, size?, dbtype?, actions?)>";
    retour = retour + "\n<!ATTLIST field ";
    retour = retour + "\nid CDATA	#REQUIRED";
    retour = retour + "\ntype CDATA	#REQUIRED>";
    retour = retour + "\n<!ELEMENT name (#PCDATA)>";
    retour = retour + "\n<!ELEMENT displayName (#PCDATA)>";
    retour = retour + "\n<!ELEMENT description (#PCDATA)>";
    retour = retour + "\n<!ELEMENT reply (#PCDATA)>";
    retour = retour + "\n<!ELEMENT utilisateurArchivageDemandes (#PCDATA)>";
    retour = retour + "\n<!ELEMENT publieurArchivageDemandes 	(#PCDATA)>";
    retour = retour + "\n<!ELEMENT utilisateurArchivageReponses (#PCDATA)>";
    retour = retour + "\n<!ELEMENT publieurArchivageReponses 	(#PCDATA)>";
    retour = retour + "\n<!ELEMENT utilisateurEnvoiDemandes 	(#PCDATA)>";
    retour = retour + "\n<!ELEMENT label (#PCDATA)>";
    retour = retour + "\n<!ELEMENT value (#PCDATA)>";
    retour = retour + "\n<!ELEMENT size (#PCDATA)>";
    retour = retour + "\n<!ELEMENT rows (#PCDATA)>";
    retour = retour + "\n<!ELEMENT cols (#PCDATA)>";
    retour = retour + "\n<!ELEMENT dbtype (#PCDATA)>";
    retour = retour + "\n<!ELEMENT actions (action*)>";
    retour = retour + "\n<!ELEMENT action EMPTY>";
    retour = retour + "\n<!ATTLIST action ";
    retour = retour + "\nid CDATA #REQUIRED";
    retour = retour + "\nvalue CDATA #REQUIRED>";
    retour = retour + "\n]>";
    return retour;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public abstract String print();

  /**
   * Method declaration
   *
   * @param trueActionPage
   * @param deleteActionCode
   * @param modifyActionCode
   * @return
   * @see
   */
  public abstract String printDemo(String trueActionPage,
      String deleteActionCode, String modifyActionCode);

  /**
   * Method declaration
   *
   * @param trueActionPage
   * @param submitPage
   * @param modifyActionCode
   * @return
   * @see
   */
  public abstract String printHeader(String trueActionPage, String submitPage,
      String modifyActionCode);
}
