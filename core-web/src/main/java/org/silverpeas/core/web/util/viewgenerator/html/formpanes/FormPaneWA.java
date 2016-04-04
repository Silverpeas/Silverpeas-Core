/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * FormPaneWA.java
 *
 * Created on 13 decembre 2000
 */

package org.silverpeas.core.web.util.viewgenerator.html.formpanes;

import javax.servlet.jsp.PageContext;

/**
 * The default implementation of FormPane abstract class
 * @author frageade
 * @version 1.0
 */
public class FormPaneWA extends FormPane {

  /**
   * Generic class to display a typical WA form pane.
   * @param String form name
   * @param String form action url
   * @param PageContext form page context
   */
  public FormPaneWA(String nam, String url, PageContext pc) {
    super(nam, url, pc);
  }

  // Methodes

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    int i;
    String result = "\n<!-- Formulaire genere automatiquement par Activ'Portal -->\n";

    result = result + "\n<form name=\"" + name + "\" action=\"" + actionURL
        + "\" method=\"" + formMethod + "\">";

    // Champs "hidden"
    if (formHiddenFields.size() > 0) {
      for (i = 0; i < formHiddenFields.size(); i++) {
        result = result + ((FormLine) formHiddenFields.elementAt(i)).print();
      }
    }

    // Champs de saisie du formulaire
    if (formLines.size() > 0) {
      result = result + "\n<table cellpadding=0 cellspacing=0 border=0>";
      for (i = 0; i < formLines.size(); i++) {
        result = result + "\n<tr>"
            + ((FormLine) formLines.elementAt(i)).print() + "\n</tr>";
      }
      result = result + "\n</table>";
    }

    // Boutons d'actions
    if (formActionButtons.size() > 0) {
      result = result + "\n<table cellpadding=0 cellspacing=0 border=0>";
      for (i = 0; i < formActionButtons.size(); i++) {
        result = result + "\n<tr>"
            + ((FormLine) formActionButtons.elementAt(i)).print() + "\n</tr>";
      }
      result = result + "\n</table>";
    }

    result = result + "\n</form>";
    return result;
  }

  /**
   * Method declaration
   * @param trueActionPage
   * @param submitPage
   * @param modifyActionCode
   * @return
   * @see
   */
  public String printHeader(String trueActionPage, String submitPage,
      String modifyActionCode) {
    String retour = "<!--JAVASCRIPT UTIL -->";

    retour = retour + "\n<script language=\"JavaScript\">";
    retour = retour + "\nfunction addReply() {";
    retour = retour + "\ndocument.replyFormRequest.formName.value = document."
        + name + ".formName.value";
    retour = retour
        + "\ndocument.replyFormRequest.formDescription.value = document."
        + name + ".formDescription.value";
    retour = retour + "\ndocument.replyFormRequest.submit();";
    retour = retour + "\n}";
    retour = retour + "\nfunction clickPubliArchiDemandes() {";
    retour = retour + "\ndocument." + name
        + ".utilisateurEnvoiDemandes[0].click();";
    retour = retour + "\n}";
    retour = retour + "\nfunction clickUtilEnvoiDemandes() {";
    retour = retour + "\ndocument." + name
        + ".publieurArchivageDemandes[1].click();";
    retour = retour + "\n}";
    retour = retour + "\n</script>";
    retour = retour + "\n<!--TABLE CONTENU -->";
    retour =
        retour
            +
            "\n<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=couleurFondCadre>";
    retour = retour + "\n<FORM name=\"" + name + "\" action=\"" + submitPage
        + "\" method=\"post\" size=30>";
    if (reply) {
      retour = retour
          + "\n<input name=\"publieurArchivageDemandes\" type=\"hidden\" value=\"true\">";
      retour = retour
          + "\n<input name=\"utilisateurEnvoiDemandes\" type=\"hidden\" value=\"true\">";
    }
    retour = retour + "\n<tr>";
    retour = retour
        + "\n<td colspan=\"2\" class=\"couleurCadreInterieur\"><span class=\"titremodule\">&nbsp;"
        + message.getString("Form");
    retour = retour + "\n</span></td>";
    retour = retour + "\n</tr><tr>";
    retour = retour
        + "\n<td width=\"52%\" align=\"right\">&nbsp;<span class=\"txtnote\">"
        + message.getString("Name") + " :&nbsp;</span></td>";
    retour = retour + "\n<td width=\"48%\">";
    retour = retour + "\n<input type=\"text\" name=\"formName\" value=\""
        + displayName + "\">";
    retour = retour + "\n</td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n<tr bgcolor=\"#999999\">";
    retour =
        retour
            +
            "\n<td colspan=\"2\" align=\"right\"><img src=\"icons/1px.gif\" width=\"1\" height=\"1\"></td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n<tr>";
    retour = retour
        + "\n<td width=\"52%\" align=\"right\">&nbsp;<span class=\"txtnote\">"
        + message.getString("Description") + " :&nbsp;</span></td>";
    retour = retour + "\n<td width=\"48%\"><font size=\"1\">";
    retour = retour
        + "\n<textarea name=\"formDescription\" cols=\"45\" rows=\"6\" wrap=VIRTUAL>"
        + description + "</textarea>";
    retour = retour + "\n</font></td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n<tr bgcolor=\"#999999\">";
    retour =
        retour
            +
            "\n<td colspan=\"2\" align=\"right\"><img src=\"icons/1px.gif\" width=\"1\" height=\"1\"></td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n<tr>";
    retour = retour
        + "\n<td width=\"52%\" align=\"right\"><span class=\"txtnote\">"
        + message.getString("AskReply") + " :&nbsp;</span></td>";
    retour = retour + "\n<td width=\"48%\">";
    retour = retour
        + "\n<input type=\"checkbox\" name=\"demandeReponse\" value=\"1\" ";
    if (reply) {
      retour = retour + "checked ";
    }
    retour = retour + "onClick=\"javascript:addReply();\">";
    retour = retour + "\n</td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n<tr bgcolor=\"#999999\">";
    retour =
        retour
            +
            "\n<td colspan=\"2\" align=\"right\"><img src=\"icons/1px.gif\" width=\"1\" height=\"2\"></td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n<tr>";
    retour = retour
        + "\n<td colspan=\"2\" class=\"couleurCadreInterieur\"><span class=\"titremodule\">&nbsp;"
        + message.getString("EditorMode") + "</span></td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n<tr>";
    retour = retour
        + "\n<td width=\"52%\" align=\"right\">&nbsp;<span class=\"txtnote\">"
        + message.getString("RequestsStorage") + " :</span></td>";
    retour = retour + "\n<td width=\"48%\"><span class=\"txtnote\">&nbsp;"
        + message.getString("Oui") + " ";
    retour = retour
        + "\n<input type=\"radio\" name=\"publieurArchivageDemandes\" value=\"true\" ";
    if (publieurArchivageDemandes) {
      retour = retour + " checked ";
    }
    if (reply) {
      retour = retour + " disabled ";
    }
    retour = retour + "onClick=\"javascript:clickPubliArchiDemandes();\">";
    retour =
        retour
            +
            "\n<img src=\"icons/1pxg.gif\" width=\"1\" height=\"20\" valign=\"absmiddle\" align=\"absmiddle\">&nbsp;"
            + message.getString("Non") + " ";
    retour = retour
        + "\n<input type=\"radio\" name=\"publieurArchivageDemandes\" value=\"false\"";
    if (!publieurArchivageDemandes) {
      retour = retour + " checked ";
    }
    if (reply) {
      retour = retour + " disabled ";
    }
    retour = retour + ">\n</span></td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n<tr bgcolor=\"#999999\">";
    retour =
        retour
            +
            "\n<td colspan=\"2\" align=\"right\"><img src=\"icons/1px.gif\" width=\"1\" height=\"1\"></td>";
    retour = retour + "\n</tr>";
    if (reply) {
      retour = retour + "\n<tr>";
      retour = retour
          + "\n<td width=\"52%\" align=\"right\">&nbsp;<span class=\"txtnote\">"
          + message.getString("ResponsesStorage") + "&nbsp;:</span></td>";
      retour = retour + "\n<td width=\"48%\"><span class=\"txtnote\">&nbsp;"
          + message.getString("Oui") + " ";
      retour = retour
          + "\n<input type=\"radio\" name=\"publieurArchivageReponses\" value=\"true\"";
      if (publieurArchivageReponses) {
        retour = retour + " checked";
      }
      retour =
          retour
              +
              ">\n<img src=\"icons/1pxg.gif\" width=\"1\" height=\"20\" valign=\"absmiddle\" align=\"absmiddle\">&nbsp;"
              + message.getString("Non") + " ";
      retour = retour
          + "\n<input type=\"radio\" name=\"publieurArchivageReponses\" value=\"false\"";
      if (!publieurArchivageReponses) {
        retour = retour + " checked";
      }
      retour = retour + ">\n</span></td>";
      retour = retour + "\n</tr>";
    }
    retour = retour + "\n<tr bgcolor=\"#999999\">";
    retour =
        retour
            +
            "\n<td colspan=\"2\" align=\"right\"><img src=\"icons/1px.gif\" width=\"1\" height=\"2\"></td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n<tr>";
    retour = retour
        + "\n<td colspan=\"2\" class=\"couleurCadreInterieur\"><span class=\"titremodule\">&nbsp;"
        + message.getString("UserMode") + "</span></td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n<tr>";
    retour = retour
        + "\n<td width=\"52%\" align=\"right\">&nbsp;<span class=\"txtnote\">"
        + message.getString("RequestsStorage") + " :</span></td>";
    retour = retour + "\n<td width=\"48%\"><span class=\"txtnote\">&nbsp;"
        + message.getString("Oui") + " ";
    retour = retour
        + "\n<input type=\"radio\" name=\"utilisateurArchivageDemandes\" value=\"true\"";
    if (utilisateurArchivageDemandes) {
      retour = retour + " checked";
    }
    retour =
        retour
            +
            ">\n<img src=\"icons/1pxg.gif\" width=\"1\" height=\"20\" valign=\"absmiddle\" align=\"absmiddle\">&nbsp;"
            + message.getString("Non") + " ";
    retour = retour
        + "\n<input type=\"radio\" name=\"utilisateurArchivageDemandes\" value=\"false\"";
    if (!utilisateurArchivageDemandes) {
      retour = retour + " checked";
    }
    retour = retour + ">\n</span></td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n<tr bgcolor=\"#999999\">";
    retour =
        retour
            +
            "\n<td colspan=\"2\" align=\"right\"><img src=\"icons/1px.gif\" width=\"1\" height=\"1\"></td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n<tr>";
    retour = retour
        + "\n<td width=\"52%\" align=\"right\">&nbsp;<span class=\"txtnote\">"
        + message.getString("RequestsSending") + " :</span></td>";
    retour = retour + "\n<td width=\"48%\"><span class=\"txtnote\">&nbsp;"
        + message.getString("Oui") + " ";
    retour = retour
        + "\n<input type=\"radio\" name=\"utilisateurEnvoiDemandes\" value=\"true\" ";
    if (utilisateurEnvoiDemandes) {
      retour = retour + " checked ";
    }
    if (reply) {
      retour = retour + " disabled ";
    }
    retour =
        retour
            +
            ">\n<img src=\"icons/1pxg.gif\" width=\"1\" height=\"20\" valign=\"absmiddle\" align=\"absmiddle\">&nbsp;"
            + message.getString("Non") + " ";
    retour = retour
        + "\n<input type=\"radio\" name=\"utilisateurEnvoiDemandes\" value=\"false\" ";
    if (!utilisateurEnvoiDemandes) {
      retour = retour + " checked ";
    }
    if (reply) {
      retour = retour + " disabled ";
    }
    retour = retour + "onClick=\"javascript:clickUtilEnvoiDemandes();\">";
    retour = retour + "\n</span></td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n<tr bgcolor=\"#999999\">";
    retour =
        retour
            +
            "\n<td colspan=\"2\" align=\"right\"><img src=\"icons/1px.gif\" width=\"1\" height=\"1\"></td>";
    retour = retour + "\n</tr>";
    if (reply) {
      retour = retour + "\n<tr>";
      retour = retour
          + "\n<td width=\"52%\" align=\"right\">&nbsp;<span class=\"txtnote\">"
          + message.getString("ResponsesStorage") + " :</span></td>";
      retour = retour + "\n<td width=\"48%\"><span class=\"txtnote\">&nbsp;"
          + message.getString("Oui") + " ";
      retour = retour
          + "\n<input type=\"radio\" name=\"utilisateurArchivageReponses\" value=\"true\"";
      if (utilisateurArchivageReponses) {
        retour = retour + " checked";
      }
      retour =
          retour
              +
              ">\n<img src=\"icons/1pxg.gif\" width=\"1\" height=\"20\" valign=\"absmiddle\" align=\"absmiddle\">&nbsp;"
              + message.getString("Non") + " ";
      retour = retour
          + "\n<input type=\"radio\" name=\"utilisateurArchivageReponses\" value=\"false\"";
      if (!utilisateurArchivageReponses) {
        retour = retour + " checked";
      }
      retour = retour + ">\n</span></td>";
      retour = retour + "\n</tr>";
    }
    retour = retour + "\n<tr bgcolor=\"#999999\">";
    retour =
        retour
            +
            "\n<td colspan=\"2\" valign=\"top\"><img src=\"icons/1px.gif\" width=\"1\" height=\"2\"></td>";
    retour = retour + "\n</tr>";
    retour = retour + "\n</FORM>";
    retour = retour + "\n</table>";
    retour = retour + "\n<!--  -->";
    retour = retour + "\n<form name=\"replyFormRequest\" action=\""
        + trueActionPage + "&action=";
    retour = retour + modifyActionCode + "&params=reply\" method=\"post\">";
    retour = retour + "\n<input type=\"hidden\" name=\"formName\" value=\"\">";
    retour = retour
        + "\n<input type=\"hidden\" name=\"formDescription\" value=\"\">";
    retour = retour + "\n</form>";
    return retour;
  }

  /**
   * Method declaration
   * @param trueActionPage
   * @param deleteActionCode
   * @param modifyActionCode
   * @return
   * @see
   */
  public String printDemo(String trueActionPage, String deleteActionCode,
      String modifyActionCode) {
    int i;
    String result = "\n<!-- Formulaire genere automatiquement par Activ'Portal -->\n";

    // Champs "hidden"
    if (formHiddenFields.size() > 0) {
      result = result + "\n<table width=\"100%\">";
      for (i = 0; i < formHiddenFields.size(); i++) {
        result = result + "\n<tr>"
            + ((FormLine) formHiddenFields.elementAt(i)).printDemo();
        result = result + "\n<td>";
        if (((FormLine) formHiddenFields.elementAt(i)).isLocked()) {
          result = result + "&nbsp;";
        } else {
          result = result + "<a href=\"" + trueActionPage + "&action="
              + deleteActionCode;
          result = result + "&params="
              + ((FormLine) formHiddenFields.elementAt(i)).getName() + "\">"
              + message.getString("Supprimer") + "</a>";
        }
        result = result + "\n</td>\n</tr>";
      }
      result = result + "\n</table>";
    }

    // Champs de saisie du formulaire
    if (formLines.size() > 0) {
      result = result + "\n<table width=\"100%\">";
      for (i = 0; i < formLines.size(); i++) {
        result = result + "\n<tr>"
            + ((FormLine) formLines.elementAt(i)).printDemo();
        result = result + "\n<td>";
        if (((FormLine) formLines.elementAt(i)).isLocked()) {
          result = result + "&nbsp;";
        } else {
          result = result + "<a href=\"" + trueActionPage + "&action="
              + deleteActionCode;
          result = result + "&params="
              + ((FormLine) formLines.elementAt(i)).getName() + "\">"
              + message.getString("Supprimer") + "</a>";
        }
        result = result + "\n</td>\n</tr>";
      }
      result = result + "\n</table>";
    }

    // Boutons d'actions
    if (formActionButtons.size() > 0) {
      result = result + "\n<table width=\"100%\">";
      for (i = 0; i < formActionButtons.size(); i++) {
        result = result + "\n<tr>"
            + ((FormLine) formActionButtons.elementAt(i)).printDemo();
        result = result + "\n<td>";
        if (((FormLine) formActionButtons.elementAt(i)).isLocked()) {
          result = result + "&nbsp;";
        } else {
          result = result + "<a href=\"" + trueActionPage + "&action="
              + deleteActionCode;
          result = result + "&params="
              + ((FormLine) formActionButtons.elementAt(i)).getName() + "\">"
              + message.getString("Supprimer") + "</a>";
        }
        result = result + "\n</td>\n</tr>";
      }
      result = result + "\n</table>";
    }

    result = result + "\n</form>";
    return result;
  }

}
