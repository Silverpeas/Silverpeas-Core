/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.importExport.report;

import java.util.List;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.silverpeas.importExport.control.ImportExport;
import com.silverpeas.node.importexport.NodeTreeType;
import com.silverpeas.node.importexport.NodeTreesType;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.viewGenerator.html.Encode;

/**
 * @author sdevolder Classe generant le code html du sommaire d une exportation
 */
public class HtmlExportGenerator {

  // Variables
  private String fileExportDir;
  private ExportReport exportReport;
  private ResourceLocator resourceLocator;

  // Constructeurs
  private HtmlExportGenerator() {
  }

  public HtmlExportGenerator(ExportReport exportReport, String fileExportDir) {
    this.fileExportDir = fileExportDir;
    this.exportReport = exportReport;
  }

  public HtmlExportGenerator(ExportReport exportReport, String fileExportDir,
      ResourceLocator resourceLocator) {
    this.fileExportDir = fileExportDir;
    this.exportReport = exportReport;
    this.resourceLocator = resourceLocator;
  }

  /**
   * @param javastring
   * @return
   */
  public static String encode(String javastring) {
    String res = "";

    if (javastring == null) {
      return res;
    }
    res = Encode.javaStringToHtmlString(res);
    for (int i = 0; i < javastring.length(); i++) {
      switch (javastring.charAt(i)) {
        case '\n':
          res += "<br>";
          break;
        case '\t':
          res += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
          break;
        default:
          res += javastring.charAt(i);
      }
    }
    return res;
  }

  public static String getHtmlStyle() {
    StringBuilder sb = new StringBuilder();
    // Ajout d'un style a tout le document
    sb.append("<style type=\"text/css\">\n");
    sb.append("<!--\n");
    sb
        .append("body,td,th {font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 10px; color: #000000;}\n");
    sb
        .append("body {margin-left: 5px; margin-top: 5px; margin-right: 5px; margin-bottom: 5px;}\n");
    sb
        .append("A { font-family: Verdana,Arial, sans-serif; font-size: 10px; text-decoration: none; color: #000000}\n");
    sb.append("A:hover {color: #666699;}\n");
    sb.append("// -->\n");
    sb.append("</style>\n");
    return sb.toString();
  }

  /**
   * @param text
   * @return
   */
  private String writeEnTeteSommaire(String text) {

    String htmlText = encode(text);

    StringBuilder sb = new StringBuilder();

    sb
        .append(
            "<TABLE border=\"0\" width=\"100%\" align=\"center\" bgcolor=\"#B3BFD1\">")
        .append("\n");
    sb.append("<TR>").append("\n");
    sb.append("<TD align=\"center\">").append("\n");
    sb.append("<b>").append(htmlText).append("</b><BR>").append("\n");
    sb.append("</TD>").append("\n");
    sb.append("</TABLE>").append("\n");

    return sb.toString();
  }

  /**
   * @return
   */
  public String toHTML() {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileExportDir);

    sb.append("<HTML>\n");
    sb.append("<HEAD>\n");

    sb.append("<TITLE>").append("Sommaire de ").append(htmlFileExportDir)
        .append("</TITLE>");
    sb.append(getHtmlStyle());
    sb.append("</HEAD>\n");
    sb.append("<BODY>\n");

    HashMap map = exportReport.getMapIndexHtmlPaths();
    if (map != null) {
      sb.append(writeEnTeteSommaire(map.size() + " Documents dans "
          + htmlFileExportDir));
      sb.append("<BR>\n");
      Set setPubName = map.keySet();
      Iterator itSetPubName = setPubName.iterator();
      while (itSetPubName.hasNext()) {
        String pubId = (String) itSetPubName.next();
        HtmlExportPublicationGenerator s = (HtmlExportPublicationGenerator) map
            .get(pubId);
        sb.append(s.toHtmlSommairePublication());
      }
    }
    sb.append("</BODY>\n");
    sb.append("</HTML>\n");

    return sb.toString();
  }

  public String toHTML(String fileName, Collection pubIds) {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileName);

    sb.append("<HTML>\n");
    sb.append("<HEAD>\n");

    sb.append("<TITLE>").append(htmlFileExportDir).append("</TITLE>");
    sb.append(getHtmlStyle());
    sb.append("</HEAD>\n");
    sb.append("<BODY>\n");

    // ajout des publications dans le fichier HTML
    HashMap map = exportReport.getMapIndexHtmlPaths();
    if (map != null) {
      if (pubIds.size() == 1)
        sb.append(writeEnTeteSommaire(pubIds.size() + " "
            + resourceLocator.getString("importExport.document")));
      else
        sb.append(writeEnTeteSommaire(pubIds.size() + " "
            + resourceLocator.getString("importExport.documents")));
      sb.append("<BR>\n");
      Set setPubName = map.keySet();
      Iterator itSetPubName = setPubName.iterator();
      while (itSetPubName.hasNext()) {
        String pubId = (String) itSetPubName.next();
        if (pubIds.contains(pubId)) {
          HtmlExportPublicationGenerator s = (HtmlExportPublicationGenerator) map
              .get(pubId);
          sb.append(s.toHtmlSommairePublication());
        }
      }
    }
    sb.append("</BODY>\n");
    sb.append("</HTML>\n");

    return sb.toString();
  }

  public String toHTML(String fileName) {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileName);

    sb.append("<HTML>\n");
    sb.append("<HEAD>\n");

    sb.append("<TITLE>").append(htmlFileExportDir).append("</TITLE>");
    sb.append(getHtmlStyle());
    sb.append("</HEAD>\n");
    sb.append("<BODY>\n");
    sb.append(writeEnTeteSommaire("0 "
        + resourceLocator.getString("importExport.document")));
    sb.append("<BR>\n");
    sb.append(resourceLocator.getString("importExport.empty"));
    sb.append("</BODY>\n");
    sb.append("</HTML>\n");

    return sb.toString();
  }

  public String indexToHTML(String fileName, Set topicIds,
      NodeTreesType nodeTreesType, String rootId) {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileName);

    sb.append("<HTML>\n");
    sb.append("<HEAD>\n");

    sb.append("<TITLE>")
        .append(resourceLocator.getString("importExport.index")).append(" ")
        .append(htmlFileExportDir).append("</TITLE>\n");
    sb.append(getHtmlStyle());
    // pour le treeview
    sb
        .append("<script type=\"text/javascript\" src=\"treeview/TreeView.js\"></script>\n");
    sb
        .append("<script type=\"text/javascript\" src=\"treeview/TreeViewElements.js\"></script>\n");
    sb
        .append("<link type=\"text/css\" rel=\"stylesheet\" href=\"treeview/treeview.css\">\n");
    sb.append("</HEAD>\n");
    sb.append("<BODY>\n");

    sb.append("<table>\n");
    sb.append("<tr>\n");
    sb.append("<td nowrap>\n");

    // Le fameux treeview
    // creation du treeview avec la liste des topics
    sb.append("<SCRIPT language=\"JavaScript\" type=\"text/javascript\">\n");
    sb.append("var elements_treeview = new TreeViewElements();\n");
    List nodeTrees = nodeTreesType.getListNodeTreeType();
    sb.append(nodeTreeToHTML(nodeTrees, topicIds, rootId));

    // CONTROLE TREEVIEW
    sb.append("var treeview = new TreeView(\"treeview\");\n");
    sb.append("treeview.define (elements_treeview);\n");
    sb.append("treeview.validate();\n");
    sb.append("treeview.height = \"590px\";\n");
    sb.append("treeview.width = \"300px\";\n");

    // Prechargeur
    sb.append("treeview.load_all = false;\n");
    sb.append("treeview.use_preloader_feature = false;\n");
    sb.append("treeview.preloader_position = \"top\";\n");
    sb.append("treeview.preloader_addButton = true;\n");

    // Dossier
    sb.append("treeview.use_folder_feature = true;\n");

    // Liens
    sb.append("treeview.use_link_feature = true;\n");
    sb.append("treeview.link_target = \"publis\";\n");
    sb.append("treeview.link_prefix = \"indexTopic\";\n");
    sb.append("treeview.link_suffix = \".html\";\n");
    sb.append("treeview.link_add_nodeId = false;\n");

    // Affichage
    sb.append("treeview.display();\n");

    // Elements HTML
    sb
        .append("treeview.control.ondblclick = function ( ) { window.status = \"control\" ; }\n");

    sb.append("</SCRIPT>\n");
    sb.append("</td>");
    sb
        .append("<td><iframe name=\"publis\" width=\"600\" height=\"600\" frameborder=\"0\" scrolling=\"auto\"></iframe></td>");
    sb.append("</tr>\n");
    sb.append("</table>\n");

    sb.append("</BODY>\n");
    sb.append("</HTML>\n");

    return sb.toString();
  }

  private String nodeTreeToHTML(List nodeTrees, Set topicIds, String rootId) {
    StringBuffer sb = new StringBuffer();
    Iterator it = nodeTrees.iterator();

    OrganizationController orgaController = null;

    while (it.hasNext()) {
      NodeTreeType nodeTree = (NodeTreeType) it.next();
      NodeDetail node = nodeTree.getNodeDetail();
      String fatherId = node.getFatherPK().getId();
      String nodeId = node.getNodePK().getId();
      if ("-1".equals(fatherId))
        fatherId = "0";

      if (rootId.equals("0")) {
        if (orgaController == null)
          orgaController = new OrganizationController();

        String componentName = orgaController.getComponentInstLight(
            node.getNodePK().getInstanceId()).getLabel();

        // regarder si ce topic contient des publications
        if (topicIds.contains(nodeId))
          sb.append("elements_treeview.addElement(\"" + componentName + "\", "
              + nodeId + ", " + fatherId + ", \"dossier\", \"folder\", "
              + nodeId + ");\n");
        else
          sb.append("elements_treeview.addElement(\"" + componentName + "\", "
              + nodeId + ", " + fatherId
              + ", \"dossier\", \"folder\", \"Empty\");\n");
      }
      List childrens = (List) node.getChildrenDetails();
      if (childrens != null)
        sb.append(topicToHTML(childrens, topicIds, rootId, false));
    }
    return sb.toString();
  }

  private String topicToHTML(List nodes, Set topicIds, String rootId,
      boolean found) {
    StringBuffer sb = new StringBuffer();
    String fatherId = null;
    Iterator it = nodes.iterator();
    while (it.hasNext()) {
      NodeDetail node = (NodeDetail) it.next();
      fatherId = node.getFatherPK().getId();
      String nodeId = node.getNodePK().getId();
      if ("-1".equals(fatherId))
        fatherId = "0";
      if (!rootId.equals("0") && !found) {
        List childrens = (List) node.getChildrenDetails();
        // regarder si on est sur le topic "rootId" donc on met "0" pour son Id
        // pour en faire la racine (et "0" pour son p�re)
        if (nodeId.equals(rootId)) {
          // on est sur "rootId", donc on met "0" pour son Id pour en faire la
          // racine (et "0" pour son p�re)
          if (topicIds.contains(nodeId))
            sb.append("elements_treeview.addElement(\"" + node.getName()
                + "\", " + "0" + ", " + "0" + ", \"dossier\", \"folder\", "
                + nodeId + ");\n");
          else
            sb.append("elements_treeview.addElement(\"" + node.getName()
                + "\", " + "0" + ", " + "0"
                + ", \"dossier\", \"folder\", \"Empty\");\n");
          if (childrens != null)
            sb.append(topicToHTML(childrens, topicIds, rootId, true));
        } else {
          // on est pas sur le "rootId" donc on continue � le chercher dans
          // les enfants
          if (childrens != null)
            sb.append(topicToHTML(childrens, topicIds, rootId, false));
        }
      } else {
        if (found) {
          // on est dans les enfants du "rootId" donc on met "0" dans fatherId
          // pour les racrocher
          if (topicIds.contains(nodeId))
            sb.append("elements_treeview.addElement(\"" + node.getName()
                + "\", " + nodeId + ", " + "0" + ", \"dossier\", \"folder\", "
                + nodeId + ");\n");
          else
            sb.append("elements_treeview.addElement(\"" + node.getName()
                + "\", " + nodeId + ", " + "0"
                + ", \"dossier\", \"folder\", \"Empty\");\n");
        } else {
          // on n'affiche pas ni la corbeille ni les d�class�s
          if (!nodeId.equals("1") && !nodeId.equals("2")) {
            // regarder si ce topic contient des publications
            if (topicIds.contains(nodeId))
              sb.append("elements_treeview.addElement(\"" + node.getName()
                  + "\", " + nodeId + ", " + fatherId
                  + ", \"dossier\", \"folder\", " + nodeId + ");\n");
            else
              sb.append("elements_treeview.addElement(\"" + node.getName()
                  + "\", " + nodeId + ", " + fatherId
                  + ", \"dossier\", \"folder\", \"Empty\");\n");
          }
        }
        List childrens = (List) node.getChildrenDetails();
        if (childrens != null)
          sb.append(topicToHTML(childrens, topicIds, "0", false));
      }
    }
    return sb.toString();
  }

  /**
   * Specific Kmax Return labels of positions exported
   * 
   * @param combinationLabels
   * @return
   */
  private String combinationToHTML(List combinationLabels) {
    StringBuffer sb = new StringBuffer();
    Iterator it = combinationLabels.iterator();
    while (it.hasNext()) {
      String combinationLabel = (String) it.next();
      sb.append(combinationLabel + "\n");
    }
    return sb.toString();
  }

  /**
   * Specific Kmax /**
   * 
   * @return
   */
  public String kmaxAxisToHTML(List axis, String language) {
    StringBuffer sb = new StringBuffer();

    sb.append("<HTML>\n");
    sb.append("<HEAD>\n");
    String htmlFileExportDir = encode(fileExportDir);

    sb.append("<TITLE>")
        .append(resourceLocator.getString("importExport.index")).append(
            htmlFileExportDir).append("</TITLE>");
    sb.append(getHtmlStyle());
    sb.append("</HEAD>\n");
    sb.append("<BODY>\n");

    sb.append("<SCRIPT language=\"JavaScript\" type=\"text/javascript\">\n");
    sb.append("function submit(nbAxis) { \n");
    sb.append("var selection=\"\"; \n");
    sb.append("var positionPath=\"\"; \n");
    sb.append("var num=0;\n");
    sb.append("var fileHtml=\"index-\";\n");

    sb.append("for (i=1; i<=nbAxis; i++) {\n");
    sb
        .append("position = document.getElementById(i).options[document.getElementById(i).options.selectedIndex].value; \n");
    sb.append("positionPath += position; \n");
    sb.append("if (i<nbAxis) \n");
    sb.append("positionPath += \"-\";\n");
    sb.append("}\n");
    sb.append("fileHtml += positionPath + \".html\";\n");
    // sb.append("alert(fileHtml);\n");
    sb.append(ImportExport.iframePublication).append(
        ".location.href = \"empty.html\";\n");
    sb.append(ImportExport.iframeIndexPublications).append(
        ".location.href = fileHtml;\n");
    sb.append("}\n");
    sb.append("</script>\n");
    sb.append("</head>\n");
    sb.append("<body>\n");

    sb.append("<BR>\n");
    sb
        .append("<TABLE width=\"100%\" align=\"center\" cellspacing=\"0\" cellpadding=\"2\" bgcolor=\"#B3BFD1\">\n");
    sb.append("<TR>\n");
    sb.append("<TD width=\"100%\" align=\"center\">\n");
    sb.append("<b>").append(resourceLocator.getString("importExport.criteria"))
        .append("<b>\n");
    sb.append("</TD>\n");
    sb.append("</TR>\n");
    sb.append("</TABLE>\n");
    Iterator itAxis = axis.iterator();
    String axisName = "";
    int axisNb = 0;
    sb
        .append("<TABLE border=0 width=\"100%\" valign=center cellspacing=\"0\" cellpadding=\"2\" class=\"intfdcolor\">\n");
    sb.append("<TR>\n");
    while (itAxis.hasNext()) {
      NodeDetail node = (NodeDetail) itAxis.next();
      if (node.getLevel() == 2) { // It's an axis
        axisName = node.getName();
        axisNb++;
        if (axisNb > 1) {
          sb.append("</select>\n");
          sb.append("</TD>\n");
        }
        sb.append("<TD align=\"center\"><b>" + axisName + "</b><br>\n");
        sb.append("<select id=\"" + axisNb + "\" name=\"sel" + axisNb
            + "\" size=\"1\">");
        sb.append("<option value=\"" + node.getNodePK().getId() + "\">")
            .append(resourceLocator.getString("importExport.allCategories"))
            .append("</option>");
      } else if (node.getLevel() == 3) {
        sb.append("<option value=\"" + node.getNodePK().getId()
            + "\" class=\"intfdcolor51\">"
            + Encode.javaStringToHtmlString(node.getName(language))
            + "</option>\n");
      } else {
        String spaces = "";
        for (int i = 0; i < node.getLevel() - 3; i++) {
          spaces += "&nbsp;&nbsp;";
          sb.append("<option value=\"" + node.getNodePK().getId()
              + "\" class=\"intfdcolor5\">" + spaces
              + Encode.javaStringToHtmlString(node.getName(language))
              + "</option>\n");
        }
      }
    }
    sb.append("</TR>\n");
    sb.append("<TR><td>&nbsp;</td></tr>\n");
    sb.append("<TR><td><a target=\"indexPublications\" href=\"index-2.html\">")
        .append(resourceLocator.getString("importExport.unbalanced")).append(
            "</a></td></tr>\n");
    sb.append("<TR><td colspan=\"").append(axisNb).append(
        "\" align=\"center\"><input type=\"button\" value=\"").append(
        resourceLocator.getString("importExport.validate")).append(
        "\" onClick=\"javascript:submit(" + axisNb + ")\"></td></tr>\n");
    sb.append("</TABLE>\n");
    sb.append("<div align=\"center\">\n");
    sb.append("</div>\n");

    sb.append("<TABLE border=0 height=\"100%\" width=\"100%\">\n");
    sb.append("<tr>\n");
    // Iframe indexPublications
    sb.append("<td height=\"100%\" width=\"40%\">\n");
    sb
        .append("<iframe name=\"indexPublications\" width=\"100%\" height=\"100%\" frameborder=\"0\" scrolling=\"auto\"></iframe>\n");
    sb.append("</td>\n");
    sb.append("<td height=\"100%\" width=\"60%\">\n");
    // Iframe publications
    sb
        .append("<iframe name=\"publications\" width=\"100%\" height=\"100%\" frameborder=\"0\" scrolling=\"auto\"></iframe>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("</TABLE>\n");

    sb.append("</BODY>\n");
    sb.append("</HTML>\n");
    return sb.toString();
  }

  /**
   * @return
   */
  public String kmaxPublicationsToHTML(List combinationLabels,
      String timeCriteria, String iframe) {
    StringBuffer sb = new StringBuffer();
    String htmlFileExportDir = encode(fileExportDir);

    sb.append("<HTML>\n");
    sb.append("<HEAD>\n");

    sb.append("<TITLE>")
        .append(resourceLocator.getString("importExport.index")).append(
            htmlFileExportDir).append("</TITLE>");
    sb.append(getHtmlStyle());
    sb.append("</HEAD>\n");
    sb.append("<BODY>\n");
    if (combinationLabels.size() != 0) {
      sb
          .append("<TABLE width=\"100%\" align=center cellspacing=\"0\" cellpadding=\"2\" bgcolor=\"#B3BFD1\">\n");
      sb.append("<TR>\n");
      sb.append("<TD width=\"100%\" align=\"center\">\n");
      sb.append("<b>").append(
          resourceLocator.getString("importExport.criteria")).append("</b>\n");
      sb.append("</TD>\n");
      sb.append("</TR>\n");
      sb.append("</TABLE>\n");
      sb.append("<BR>\n");

      sb.append(combinationToHTML(combinationLabels));
      sb.append("<BR>\n");
      if (StringUtil.isDefined(timeCriteria))
        sb.append(timeCriteria);
    }
    sb.append("<TABLE border=0 height=\"100%\" width=\"100%\">\n");
    sb.append("<tr>\n");
    sb.append("<td height=\"100%\" valign=\"top\" width=\"40%\">\n");
    HashMap map = exportReport.getMapIndexHtmlPaths();
    if (map != null) {
      sb.append(writeEnTeteSommaire(map.size() + " "
          + resourceLocator.getString("importExport.documentsIn") + " "
          + htmlFileExportDir));
      sb.append("<BR>\n");
      sb.append("<BR>\n");
      sb.append("<BR>\n");
      Set setPubName = map.keySet();
      Iterator itSetPubName = setPubName.iterator();
      while (itSetPubName.hasNext()) {
        String pubId = (String) itSetPubName.next();
        HtmlExportPublicationGenerator s = (HtmlExportPublicationGenerator) map
            .get(pubId);
        sb.append(s.toHtmlSommairePublication(iframe));
      }
    }
    sb.append("</td>\n");
    sb.append("<td height=\"100%\" width=\"60%\">\n");
    // Iframe publications
    sb
        .append("<iframe name=\"publications\" width=\"100%\" height=\"100%\" frameborder=\"0\" scrolling=\"auto\"></iframe>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("</TABLE>\n");

    sb.append("</BODY>\n");
    sb.append("</HTML>\n");

    return sb.toString();
  }

  public String toHtmlPublicationsByPositionStart() {

    StringBuffer sb = new StringBuffer();

    // Ent�te du fichier HTML
    sb.append("<HTML>\n").append("<HEAD>\n");
    // ajout du style
    sb.append(HtmlExportGenerator.getHtmlStyle());
    sb.append("<TITLE>").append("</TITLE>");

    sb.append("</HEAD>\n").append("<BODY>\n");

    return sb.toString();
  }

  public String toHtmlPublicationsNumber(String positionPathName) {
    StringBuffer sb = new StringBuffer();
    HashMap map = exportReport.getMapIndexHtmlPaths();
    if (map != null) {
      sb.append(writeEnTeteSommaire(map.size() + " "
          + resourceLocator.getString("importExport.documentsIn") + " "
          + positionPathName));
      sb.append("<BR>\n");
    }
    return sb.toString();
  }

  public String toHtmlPublicationsByPositionEnd() {

    StringBuffer sb = new StringBuffer();
    sb.append("</BODY>").append("\n");
    sb.append("</HTML>").append("\n");
    return sb.toString();
  }

}
