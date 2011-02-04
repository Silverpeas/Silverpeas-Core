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
 * FLOSS exception.  You should have received a copy of the text describing
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
import java.util.Set;

import com.silverpeas.importExport.control.ImportExport;
import com.silverpeas.node.importexport.NodeTreeType;
import com.silverpeas.node.importexport.NodeTreesType;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import java.util.Map;

/**
 * @author sdevolder Classe generant le code html du sommaire d une exportation
 */
public class HtmlExportGenerator {

  private static final String NEW_LINE = "<br/>\n";
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
    res = EncodeHelper.javaStringToHtmlString(res);
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
    sb.append(
        "body,td,th {font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 10px; color: #000000;}\n");
    sb.append("body {margin-left: 5px; margin-top: 5px; margin-right: 5px; margin-bottom: 5px;}\n");
    sb.append(
        "A { font-family: Verdana,Arial, sans-serif; font-size: 10px; text-decoration: none; color: #000000}\n");
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
    sb.append("<TABLE border=\"0\" width=\"100%\" align=\"center\" bgcolor=\"#B3BFD1\">\n");
    sb.append("<TR>").append("\n");
    sb.append("<TD align=\"center\">").append("\n");
    sb.append("<b>").append(htmlText).append("</b><BR>").append("\n");
    sb.append("</TD>").append("\n");
    sb.append("</TABLE>").append("\n");

    return sb.toString();
  }

  private String getBeginningOfPage(String title, boolean treeview) {
    StringBuilder sb = new StringBuilder();
    sb.append("<html>\n");
    sb.append("<head>\n");
    sb.append("<title>").append(title).append("</title>");
    sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
    sb.append(getHtmlStyle());
    if (treeview) {
      sb.append("<script type=\"text/javascript\" src=\"treeview/TreeView.js\"></script>\n");
      sb.append("<script type=\"text/javascript\" src=\"treeview/TreeViewElements.js\"></script>\n");
      sb.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"treeview/treeview.css\">\n");
    }
    sb.append("</head>\n");
    sb.append("<body>\n");
    return sb.toString();
  }

  /**
   * @return
   */
  public String toHTML() {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileExportDir);
    sb.append(getBeginningOfPage("Sommaire de " + htmlFileExportDir, false));
    Map<String, HtmlExportPublicationGenerator> map = exportReport.getMapIndexHtmlPaths();
    if (map != null) {
      StringBuilder entete = new StringBuilder(100);
      entete.append(map.size()).append(" Documents dans ").append(htmlFileExportDir);
      sb.append(writeEnTeteSommaire(entete.toString()));
      sb.append(NEW_LINE);
      for (Map.Entry<String, HtmlExportPublicationGenerator> entry : map.entrySet()) {
        HtmlExportPublicationGenerator s = entry.getValue();
        sb.append(s.toHtmlSommairePublication());
      }
    }
    sb.append(getEndOfPage());
    return sb.toString();
  }

  /**
   * 
   * @param fileName
   * @param pubIds
   * @return 
   */
  public String toHTML(String fileName, Collection<String> pubIds) {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileName);

    sb.append(getBeginningOfPage(htmlFileExportDir, false));

    // ajout des publications dans le fichier HTML
    Map<String, HtmlExportPublicationGenerator> map = exportReport.getMapIndexHtmlPaths();
    if (map != null) {
      StringBuilder entete = new StringBuilder();
      entete.append(pubIds.size());
      if (pubIds.size() == 1) {
        entete.append(resourceLocator.getString("importExport.document"));
      } else {
        entete.append(resourceLocator.getString("importExport.documents"));
      }
      sb.append(writeEnTeteSommaire(entete.toString()));
      sb.append(NEW_LINE);
      for (Map.Entry<String, HtmlExportPublicationGenerator> entry : map.entrySet()) {
        String pubId = entry.getKey();
        if (pubIds.contains(pubId)) {
          HtmlExportPublicationGenerator generator = entry.getValue();
          sb.append(generator.toHtmlSommairePublication());
        }
      }
    }
    sb.append(getEndOfPage());
    return sb.toString();
  }

  public String toHTML(String fileName) {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileName);

    sb.append(getBeginningOfPage(htmlFileExportDir, false));
    sb.append(writeEnTeteSommaire("0 " + resourceLocator.getString("importExport.document")));
    sb.append(NEW_LINE);
    sb.append(resourceLocator.getString("importExport.empty"));
    sb.append(getEndOfPage());

    return sb.toString();
  }

  public String indexToHTML(String fileName, Set<String> topicIds, NodeTreesType nodeTreesType,
      String rootId) {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileName);

    sb.append(getBeginningOfPage(resourceLocator.getString("importExport.index") + " "
        + htmlFileExportDir, true));
    sb.append("<table>\n");
    sb.append("<tr>\n");
    sb.append("<td nowrap>\n");

    // Le fameux treeview
    // creation du treeview avec la liste des topics
    sb.append("<script language=\"JavaScript\" type=\"text/javascript\">\n");
    sb.append("var elements_treeview = new TreeViewElements();\n");
    List<NodeTreeType> nodeTrees = nodeTreesType.getListNodeTreeType();
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
    sb.append("treeview.control.ondblclick = function ( ) { window.status = \"control\" ; }\n");

    sb.append("</script>\n");
    sb.append("</td>");
    sb.append("<td>");
    sb.append(
        "<iframe name=\"publis\" width=\"600\" height=\"600\" frameborder=\"0\" scrolling=\"auto\"/>");
    sb.append("</td>");
    sb.append("</tr>\n");
    sb.append("</table>\n");
    sb.append(getEndOfPage());
    return sb.toString();
  }

  private String nodeTreeToHTML(List<NodeTreeType> nodeTrees, Set<String> topicIds, String rootId) {
    StringBuilder sb = new StringBuilder();
    OrganizationController orgaController = null;

    for (NodeTreeType nodeTree : nodeTrees) {
      NodeDetail node = nodeTree.getNodeDetail();
      String fatherId = node.getFatherPK().getId();
      String nodeId = node.getNodePK().getId();
      if ("-1".equals(fatherId)) {
        fatherId = "0";
      }

      if (NodePK.ROOT_NODE_ID.equals(rootId)) {
        if (orgaController == null) {
          orgaController = new OrganizationController();
        }
        String componentName = orgaController.getComponentInstLight(node.getNodePK().getInstanceId()).
            getLabel();

        // regarder si ce topic contient des publications
        if (topicIds.contains(nodeId)) {
          sb.append("elements_treeview.addElement(\"").append(componentName).append("\", ").
              append(nodeId).append(", ").append(fatherId).append(", \"dossier\", \"folder\", ").
              append(nodeId).append(");\n");
        } else {
          sb.append("elements_treeview.addElement(\"").append(componentName).append("\", ");
          sb.append(nodeId).append(", ").append(fatherId);
          sb.append(", \"dossier\", \"folder\", \"Empty\");\n");
        }
      }
      Collection<NodeDetail> childrens = node.getChildrenDetails();
      if (childrens != null) {
        sb.append(topicToHTML(childrens, topicIds, rootId, false));
      }
    }
    return sb.toString();
  }

  private String topicToHTML(Collection<NodeDetail> nodes, Set<String> topicIds, String rootId,
      boolean found) {
    StringBuilder sb = new StringBuilder();
    for (NodeDetail node : nodes) {
      String fatherId = node.getFatherPK().getId();
      String nodeId = node.getNodePK().getId();
      if ("-1".equals(fatherId)) {
        fatherId = "0";
      }
      if (!NodePK.ROOT_NODE_ID.equals(rootId) && !found) {
        Collection<NodeDetail> childrens = node.getChildrenDetails();
        // regarder si on est sur le topic "rootId" donc on met "0" pour son Id
        // pour en faire la racine (et "0" pour son p�re)
        if (nodeId.equals(rootId)) {
          // on est sur "rootId", donc on met "0" pour son Id pour en faire la
          // racine (et "0" pour son p�re)
          if (topicIds.contains(nodeId)) {
            sb.append("elements_treeview.addElement(\"").append(node.getName());
            sb.append("\", 0, 0, \"dossier\", \"folder\", ");
            sb.append(nodeId).append(");\n");
          } else {
            sb.append("elements_treeview.addElement(\"").append(node.getName());
            sb.append("\", 0, 0, \"dossier\", \"folder\", \"Empty\");\n");
          }
          if (childrens != null) {
            sb.append(topicToHTML(childrens, topicIds, rootId, true));
          }
        } else {
          // on est pas sur le "rootId" donc on continue � le chercher dans
          // les enfants
          if (childrens != null) {
            sb.append(topicToHTML(childrens, topicIds, rootId, false));
          }
        }
      } else {
        if (found) {
          // on est dans les enfants du "rootId" donc on met "0" dans fatherId
          // pour les racrocher
          if (topicIds.contains(nodeId)) {
            sb.append("elements_treeview.addElement(\"").append(node.getName()).append("\", ");
            sb.append(nodeId).append(", 0, \"dossier\", \"folder\", ").append(nodeId).append(");\n");
          } else {
            sb.append("elements_treeview.addElement(\"").append(node.getName()).append("\", ");
            sb.append(nodeId).append(", 0, \"dossier\", \"folder\", \"Empty\");\n");
          }
        } else {
          // on n'affiche pas ni la corbeille ni les d�class�s
          if (!node.getNodePK().isTrash() && !node.getNodePK().isUnclassed()) {
            // regarder si ce topic contient des publications
            if (topicIds.contains(nodeId)) {
              sb.append("elements_treeview.addElement(\"").append(node.getName()).append("\", ");
              sb.append(nodeId).append(", ").append(fatherId).append(", \"dossier\", \"folder\", ");
              sb.append(nodeId).append(");\n");
            } else {
              sb.append("elements_treeview.addElement(\"").append(node.getName()).append("\", ");
              sb.append(nodeId).append(", ").append(fatherId);
              sb.append(", \"dossier\", \"folder\", \"Empty\");\n");
            }
          }
        }
        Collection<NodeDetail> childrens = node.getChildrenDetails();
        if (childrens != null) {
          sb.append(topicToHTML(childrens, topicIds, NodePK.ROOT_NODE_ID, false));
        }
      }
    }
    return sb.toString();
  }

  /**
   * Specific Kmax Return labels of positions exported
   * @param combinationLabels
   * @return
   */
  private String combinationToHTML(List<String> combinationLabels) {
    StringBuilder sb = new StringBuilder();
    for (String combinationLabel : combinationLabels) {
      sb.append(combinationLabel).append("\n");
    }
    return sb.toString();
  }

  /**
   * Specific Kmax
   * @param axis
   * @param language
   * @return 
   */
  public String kmaxAxisToHTML(List<NodeDetail> axis, String language) {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileExportDir);
    sb.append(getBeginningOfPage(resourceLocator.getString("importExport.index") + htmlFileExportDir,
        false));
    sb.append("<script language=\"JavaScript\" type=\"text/javascript\">\n");
    sb.append("function submit(nbAxis) { \n");
    sb.append("var selection=\"\"; \n");
    sb.append("var positionPath=\"\"; \n");
    sb.append("var num=0;\n");
    sb.append("var fileHtml=\"index-\";\n");
    sb.append("for (i=1; i<=nbAxis; i++) {\n");
    sb.append("position = document.getElementById(i).options[");
    sb.append("document.getElementById(i).options.selectedIndex].value; \n");
    sb.append("positionPath += position; \n");
    sb.append("if (i<nbAxis) \n");
    sb.append("positionPath += \"-\";\n");
    sb.append("}\n");
    sb.append("fileHtml += positionPath + \".html\";\n");
    sb.append(ImportExport.iframePublication).append(".location.href = \"empty.html\";\n");
    sb.append(ImportExport.iframeIndexPublications).append(".location.href = fileHtml;\n");
    sb.append("}\n");
    sb.append("</script>\n");
    sb.append("</head>\n");
    sb.append("<body>\n");

    sb.append(NEW_LINE);
    sb.append(
        "<table width=\"100%\" align=\"center\" cellspacing=\"0\" cellpadding=\"2\" bgcolor=\"#B3BFD1\">\n");
    sb.append("<tr>\n");
    sb.append("<td width=\"100%\" align=\"center\">\n");
    sb.append("<b>").append(resourceLocator.getString("importExport.criteria")).append("<b>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("</table>\n");
    String axisName = "";
    int axisNb = 0;
    sb.append(
        "<table border=0 width=\"100%\" valign=center cellspacing=\"0\" cellpadding=\"2\" class=\"intfdcolor\">\n");
    sb.append("<tr>\n");
    for (NodeDetail node : axis) {
      if (node.getLevel() == 2) { // It's an axis
        axisName = node.getName();
        axisNb++;
        if (axisNb > 1) {
          sb.append("</select>\n");
          sb.append("</td>\n");
        }
        sb.append("<td align=\"center\"><b>").append(axisName).append("</b><br>\n");
        sb.append("<select id=\"").append(axisNb).append("\" name=\"sel").append(axisNb);
        sb.append("\" size=\"1\">");
        sb.append("<option value=\"").append(node.getNodePK().getId()).append("\">");
        sb.append(resourceLocator.getString("importExport.allCategories")).append("</option>");
      } else if (node.getLevel() == 3) {
        sb.append("<option value=\"").append(node.getNodePK().getId());
        sb.append("\" class=\"intfdcolor51\">");
        sb.append(EncodeHelper.javaStringToHtmlString(node.getName(language))).append("</option>\n");
      } else {
        String spaces = "";
        for (int i = 0; i < node.getLevel() - 3; i++) {
          spaces += "&nbsp;&nbsp;";
          sb.append("<option value=\"").append(node.getNodePK().getId());
          sb.append("\" class=\"intfdcolor5\">").append(spaces);
          sb.append(EncodeHelper.javaStringToHtmlString(node.getName(language)));
          sb.append("</option>\n");
        }
      }
    }
    sb.append("</tr>\n");
    sb.append("<tr><td>&nbsp;</td></tr>\n");
    sb.append("<tr><td><a target=\"indexPublications\" href=\"index-2.html\">");
    sb.append(resourceLocator.getString("importExport.unbalanced")).append("</a></td></tr>\n");
    sb.append("<t><td colspan=\"").append(axisNb);
    sb.append("\" align=\"center\"><input type=\"button\" value=\"");
    sb.append(resourceLocator.getString("importExport.validate"));
    sb.append("\" onClick=\"javascript:submit(").append(axisNb).append(")\"></td></tr>\n");
    sb.append("</table>\n");
    sb.append("<div align=\"center\">\n");
    sb.append("</div>\n");

    sb.append("<table border=0 height=\"100%\" width=\"100%\">\n");
    sb.append("<tr>\n");
    // Iframe indexPublications
    sb.append("<td height=\"100%\" width=\"40%\">\n");
    sb.append("<iframe name=\"indexPublications\" width=\"100%\" height=\"100%\" ");
    sb.append("frameborder=\"0\" scrolling=\"auto\"></iframe>\n");
    sb.append("</td>\n");
    sb.append("<td height=\"100%\" width=\"60%\">\n");
    // Iframe publications
    sb.append("<iframe name=\"publications\" width=\"100%\" height=\"100%\" ");
    sb.append("frameborder=\"0\" scrolling=\"auto\"></iframe>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("</table>\n");

    sb.append(getEndOfPage());
    return sb.toString();
  }

  /**
   * 
   * @param combinationLabels
   * @param timeCriteria
   * @param iframe
   * @return 
   */
  public String kmaxPublicationsToHTML(List<String> combinationLabels, String timeCriteria,
      String iframe) {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileExportDir);

    sb.append("<html>\n<head>\n<title>").append(resourceLocator.getString("importExport.index"));
    sb.append(htmlFileExportDir).append("</title>");
    sb.append(getHtmlStyle());
    sb.append("</head>\n<body>\n");
    if (!combinationLabels.isEmpty()) {
      sb.append(
          "<table width=\"100%\" align=center cellspacing=\"0\" cellpadding=\"2\" bgcolor=\"#B3BFD1\">\n");
      sb.append("<tr>\n");
      sb.append("<td width=\"100%\" align=\"center\">\n");
      sb.append("<b>").append(resourceLocator.getString("importExport.criteria")).append("</b>\n");
      sb.append("</td>\n");
      sb.append("</tr>\n");
      sb.append("</table>\n");
      sb.append(NEW_LINE);
      sb.append(combinationToHTML(combinationLabels));
      sb.append(NEW_LINE);
      if (StringUtil.isDefined(timeCriteria)) {
        sb.append(timeCriteria);
      }
    }
    sb.append("<table border=0 height=\"100%\" width=\"100%\">\n");
    sb.append("<tr>\n");
    sb.append("<td height=\"100%\" valign=\"top\" width=\"40%\">\n");
    Map<String, HtmlExportPublicationGenerator> map = exportReport.getMapIndexHtmlPaths();
    if (map != null) {
      StringBuilder entete = new StringBuilder(100);
      entete.append(map.size()).append(" ");
      entete.append(resourceLocator.getString("importExport.documentsIn")).append(" ");
      entete.append(htmlFileExportDir);
      sb.append(writeEnTeteSommaire(entete.toString()));
      sb.append(NEW_LINE);
      sb.append(NEW_LINE);
      sb.append(NEW_LINE);
      for (Map.Entry<String, HtmlExportPublicationGenerator> entry : map.entrySet()) {
        HtmlExportPublicationGenerator s = entry.getValue();
        sb.append(s.toHtmlSommairePublication(iframe));
      }
    }
    sb.append("</td>\n");
    sb.append("<td height=\"100%\" width=\"60%\">\n");
    // Iframe publications
    sb.append("<iframe name=\"publications\" width=\"100%\" height=\"100%\" ");
    sb.append("frameborder=\"0\" scrolling=\"auto\"></iframe>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("</table>\n");
    sb.append(getEndOfPage());

    return sb.toString();
  }

  public String toHtmlPublicationsByPositionStart() {
    StringBuilder sb = new StringBuilder();
    sb.append("<html>\n").append("<head>\n");
    sb.append(HtmlExportGenerator.getHtmlStyle());
    sb.append("<title>").append("</title>");
    sb.append("</head>\n").append("<body>\n");
    return sb.toString();
  }

  public String toHtmlPublicationsNumber(String positionPathName) {
    StringBuilder sb = new StringBuilder();
    Map<String, HtmlExportPublicationGenerator> map = exportReport.getMapIndexHtmlPaths();
    if (map != null && !map.isEmpty()) {
      StringBuilder entete = new StringBuilder(100);
      entete.append(map.size()).append(" ");
      entete.append(resourceLocator.getString("importExport.documentsIn")).append(" ");
      entete.append(positionPathName);
      sb.append(writeEnTeteSommaire(entete.toString()));
      sb.append(NEW_LINE);
    }
    return sb.toString();
  }

  public String getEndOfPage() {
    return "</body>\n</html>\n";
  }
}
