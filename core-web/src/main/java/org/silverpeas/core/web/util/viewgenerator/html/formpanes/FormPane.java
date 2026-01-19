/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/*
 * FormPane.java
 *
 * Created on 13 decembre 2000
 */
package org.silverpeas.core.web.util.viewgenerator.html.formpanes;

import jakarta.servlet.jsp.PageContext;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;

import java.util.ArrayList;
import java.util.List;

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
  protected List<FormLine> formLines;
  protected List<FormLine> formHiddenFields;
  protected List<FormLine> formActionButtons;
  protected String actionURL;
  protected String formMethod;
  protected String title = null;
  protected String name;
  protected String displayName;
  protected String description;
  protected PageContext pageContext;
  protected boolean reply;
  protected LocalizationBundle message;
  // Modes de gestion
  protected boolean utilisateurArchivageDemandes;
  protected boolean publieurArchivageDemandes;
  protected boolean utilisateurArchivageReponses;
  protected boolean publieurArchivageReponses;
  protected boolean utilisateurEnvoiDemandes;

  public FormPane(String nam, String url, PageContext pc) {
    formLines = new ArrayList<>();
    formHiddenFields = new ArrayList<>();
    formActionButtons = new ArrayList<>();
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

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public String getName() {
    return name;
  }

  public void setName(String nam) {
    name = nam;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String nam) {
    displayName = nam;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String des) {
    description = des;
  }

  public PageContext getPageContext() {
    return pageContext;
  }

  public List<FormLine> getFormLines() {
    return formLines;
  }

  public int getNbLines() {
    return formLines.size() + formHiddenFields.size()
        + formActionButtons.size();
  }

  public boolean hasReply() {
    return reply;
  }

  public void setReply(boolean rep) {
    reply = rep;
    if (rep) {
      utilisateurEnvoiDemandes = true;
      publieurArchivageDemandes = true;
    }
  }

  public boolean getUtilisateurArchivageDemandes() {
    return utilisateurArchivageDemandes;
  }

  public void setUtilisateurArchivageDemandes(boolean uad) {
    utilisateurArchivageDemandes = uad;
  }

  public boolean getPublieurArchivageDemandes() {
    return publieurArchivageDemandes;
  }

  public void setPublieurArchivageDemandes(boolean uad) {
    publieurArchivageDemandes = uad;
  }

  public boolean getUtilisateurArchivageReponses() {
    return utilisateurArchivageReponses;
  }

  public void setUtilisateurArchivageReponses(boolean uad) {
    utilisateurArchivageReponses = uad;
  }

  public boolean getPublieurArchivageReponses() {
    return publieurArchivageReponses;
  }

  public void setPublieurArchivageReponses(boolean uad) {
    publieurArchivageReponses = uad;
  }

  public boolean getUtilisateurEnvoiDemandes() {
    return utilisateurEnvoiDemandes;
  }

  public void setUtilisateurEnvoiDemandes(boolean uad) {
    utilisateurEnvoiDemandes = uad;
  }

  public void setLanguage(String language) {
    if (language != null) {
      message = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.util.viewGenerator.formPane.formPaneBundle",
          language);
    }
  }

  public void add(FormLine line) {
    line.setPane(this);
    line.setLanguage(message.getLocale().getLanguage());
    switch (line.getType()) {
      case "hidden":
        formHiddenFields.add(line);
        break;
      case "button":
      case "buttonLine":
        formActionButtons.add(line);
        break;
      default:
        formLines.add(line);
        break;
    }
  }

  public void remove(String lineName) {
    boolean search = removeLine(lineName, true, formLines);
    search = removeLine(lineName, search, formHiddenFields);
    removeLine(lineName, search, formActionButtons);
  }

  private boolean removeLine(String lineName, boolean search, List<FormLine> lines) {
    if (search && !lines.isEmpty()) {
      int i = 0;
      while ((search) && (i < lines.size())) {
        FormLine fl = lines.get(i);

        if (fl.getName().equals(lineName)) {
          lines.remove(i);
          search = false;
        }
        i++;
      }
    }
    return search;
  }

  public String toXML() {
    int i;
    StringBuilder result = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

    result.append("\n\n").append(getDTD());
    result.append("\n\n<formulaire>");
    result.append("\n<name>").append(name).append("</name>");
    result.append("\n<displayName>").append(displayName).append("</displayName>");
    result.append("\n<description>").append(description).append("</description>");
    result.append("\n<reply>").append(reply).append("</reply>");
    result.append("\n<utilisateurArchivageDemandes>")
        .append(utilisateurArchivageDemandes)
        .append("</utilisateurArchivageDemandes>");
    result.append("\n<publieurArchivageDemandes>")
        .append(publieurArchivageDemandes)
        .append("</publieurArchivageDemandes>");
    result.append("\n<utilisateurArchivageReponses>")
        .append(utilisateurArchivageReponses)
        .append("</utilisateurArchivageReponses>");
    result.append("\n<publieurArchivageReponses>")
        .append(publieurArchivageReponses)
        .append("</publieurArchivageReponses>");
    result.append("\n<utilisateurEnvoiDemandes>")
        .append(utilisateurEnvoiDemandes)
        .append("</utilisateurEnvoiDemandes>");

    // Champs "hidden"
    if (!formHiddenFields.isEmpty()) {
      for (i = 0; i < formHiddenFields.size(); i++) {
        result.append(formHiddenFields.get(i).toXML());
      }
    }

    // Champs de saisie du formulaire
    if (!formLines.isEmpty()) {
      for (i = 0; i < formLines.size(); i++) {
        result.append("\n").append(formLines.get(i).toXML());
      }
    }

    // Boutons d'actions
    if (!formActionButtons.isEmpty()) {
      for (i = 0; i < formActionButtons.size(); i++) {
        result.append("\n").append(formActionButtons.get(i).toXML());
      }
    }
    result.append("\n</formulaire>");
    return toXMLString(result.toString());
  }

  private String toXMLString(String s) {
    byte[] readbytes = s.getBytes(Charsets.UTF_8);
    return new String(readbytes);
  }

  public String getBuildRequest() {
    StringBuilder result = new StringBuilder();
    int i;

    // Champs de saisie du formulaire
    if (!formLines.isEmpty()) {
      for (i = 0; i < formLines.size(); i++) {
        result.append(formLines.get(i).getDBColumnCreationRequest());
      }
    }

    return result.toString();
  }

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

  public abstract String print();

  public abstract String printDemo(String trueActionPage,
      String deleteActionCode, String modifyActionCode);

  public abstract String printHeader(String trueActionPage, String submitPage,
      String modifyActionCode);
}
