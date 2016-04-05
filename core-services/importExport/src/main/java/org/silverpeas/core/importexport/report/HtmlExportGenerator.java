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

package org.silverpeas.core.importexport.report;

import org.silverpeas.core.importexport.control.ImportExport;
import org.silverpeas.core.node.importexport.NodeTreeType;
import org.silverpeas.core.node.importexport.NodeTreesType;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.head;
import org.apache.ecs.xhtml.link;
import org.apache.ecs.xhtml.meta;
import org.apache.ecs.xhtml.script;
import org.apache.ecs.xhtml.title;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HTML summary export generator
 * @author sdevolder
 */
public class HtmlExportGenerator {

  private static final String NEW_LINE = "<br/>\n";
  private String fileExportDir;
  private ExportReport exportReport;
  private LocalizationBundle messages;

  public HtmlExportGenerator(ExportReport exportReport, String fileExportDir) {
    this.fileExportDir = fileExportDir;
    this.exportReport = exportReport;
    this.messages = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.importExport.multilang.importExportBundle");
  }

  public HtmlExportGenerator(ExportReport exportReport, String fileExportDir,
      LocalizationBundle messages) {
    this.fileExportDir = fileExportDir;
    this.exportReport = exportReport;
    this.messages = messages;
  }

  /**
   * @param javastring
   * @return
   */
  public static String encode(String javastring) {
    StringBuilder sb = new StringBuilder("");
    if (javastring != null) {
      String res = EncodeHelper.javaStringToHtmlString(javastring);
      for (int i = 0; i < res.length(); i++) {
        switch (res.charAt(i)) {
          case '\n':
            sb.append("<br/>");
            break;
          case '\t':
            sb.append("&nbsp;&nbsp;");
            break;
          default:
            sb.append(res.charAt(i));
        }
      }
    }
    return sb.toString();
  }

  public static String getHtmlStyle() {
    ElementContainer xhtmlcontainer = new ElementContainer();
    xhtmlcontainer.addElement(new link().setType("text/css").setRel("stylesheet").setHref(
        "treeview/display.css"));
    return xhtmlcontainer.toString();
  }

  /**
   * @param text
   * @return xhtml string of summary header
   */
  String writeEnTeteSommaire(String text) {
    ElementContainer xhtmlcontainer = new ElementContainer();
    div entete = new div();
    entete.setClass("numberOfDocument");
    entete.addElement(encode(text));
    xhtmlcontainer.addElement(entete);
    return xhtmlcontainer.toString();
  }

  String getBeginningOfPage(String title, boolean treeview) {
    ElementContainer xhtmlcontainer = new ElementContainer();
    head header = new head();
    header.addElement(new title(title));
    header.addElement(new meta().setContent("text/html; charset=UTF-8")
        .setHttpEquiv("Content-Type"));
    header.addElement(getHtmlStyle());
    if (treeview) {
      header.addElement(new script().setType("text/javascript").setSrc("treeview/TreeView.js"));
      header.addElement(new script().setType("text/javascript").setSrc(
          "treeview/TreeViewElements.js"));
      header.addElement(new link().setType("text/css").setRel("stylesheet").setHref(
          "treeview/treeview.css"));
    }
    xhtmlcontainer.addElement("<html>");
    xhtmlcontainer.addElement(header);
    return xhtmlcontainer.toString();
  }

  /**
   * @return
   */
  public String toHTML() {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileExportDir);
    sb.append(getBeginningOfPage("Sommaire de " + htmlFileExportDir, false));
    sb.append("<body>\n");
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
   * @param fileName the file export directory name
   * @param pubIds a list of publication identifier
   * @return
   */
  public String toHTML(String fileName, Collection<String> pubIds) {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileName);

    sb.append(getBeginningOfPage(htmlFileExportDir, false));
    sb.append("<body>\n");
    // Add publications inside HTML file
    Map<String, HtmlExportPublicationGenerator> map = exportReport.getMapIndexHtmlPaths();
    if (map != null) {
      StringBuilder header = new StringBuilder();
      header.append(pubIds.size()).append(" ");
      if (pubIds.size() == 1) {
        header.append(messages.getString("importExport.document"));
      } else {
        header.append(messages.getString("importExport.documents"));
      }
      sb.append(writeEnTeteSommaire(header.toString()));
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
    sb.append("<body>\n");
    sb.append(writeEnTeteSommaire("0 " + messages.getString("importExport.document")));
    sb.append(NEW_LINE);
    sb.append(messages.getString("importExport.empty"));
    sb.append(getEndOfPage());

    return sb.toString();
  }

  public String indexToHTML(String fileName, Set<String> topicIds, NodeTreesType nodeTreesType,
      String rootId) {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileName);

    sb.append(getBeginningOfPage(messages.getString("importExport.index") + " "
        + htmlFileExportDir, true));
    sb.append("<body>\n");
    // creation du treeview avec la liste des topics
    sb.append("<div id=\"treeview\">\n");
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

    // select root by default
    sb.append("goto_link(\"treeview\", 0, treeview.link_target, treeview.link_prefix + elements_treeview.attribute(0, \"Lien\") + treeview.link_suffix);\n");

    sb.append("</script>\n");
    sb.append("</div>");
    sb.append("<div id=\"frameContent\">");
    sb.append(
        "<iframe name=\"publis\" width=\"100%\" height=\"100%\" frameborder=\"0\" scrolling=\"auto\"/>");
    sb.append("</div>\n");
    sb.append(getEndOfPage());
    return sb.toString();
  }

  private String nodeTreeToHTML(List<NodeTreeType> nodeTrees, Set<String> topicIds, String rootId) {
    StringBuilder sb = new StringBuilder();
    for (NodeTreeType nodeTree : nodeTrees) {
      NodeDetail node = nodeTree.getNodeDetail();
      String fatherId = node.getFatherPK().getId();
      String nodeId = node.getNodePK().getId();
      if ("-1".equals(fatherId)) {
        fatherId = "0";
      }

      if (NodePK.ROOT_NODE_ID.equals(rootId)) {
        String componentName = OrganizationControllerProvider.getOrganisationController()
            .getComponentInstLight(node.getNodePK().getInstanceId()).getLabel();

        // regarder si ce topic contient des publications
        if (topicIds.contains(nodeId)) {
          sb.append(filledTreeElement(componentName, nodeId, fatherId));
        } else {
          sb.append(emptyTreeElement(node.getName(), nodeId, fatherId));
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
        fatherId = NodePK.ROOT_NODE_ID;
      }
      if (!NodePK.ROOT_NODE_ID.equals(rootId) && !found) {
        Collection<NodeDetail> childrens = node.getChildrenDetails();
        // regarder si on est sur le topic "rootId" donc on met "0" pour son Id
        // pour en faire la racine (et "0" pour son pere)
        if (nodeId.equals(rootId)) {
          // on est sur "rootId", donc on met "0" pour son Id pour en faire la
          // racine (et "0" pour son pere)
          if (topicIds.contains(nodeId)) {
            sb.append("elements_treeview.addElement(\"").append(node.getName());
            sb.append("\", 0, 0, \"dossier\", \"folder\", ");
            sb.append(nodeId).append(");\n");
          } else {
            sb.append(emptyTreeElement(node.getName(), "0", "0"));
          }
          if (childrens != null) {
            sb.append(topicToHTML(childrens, topicIds, rootId, true));
          }
        } else {
          // on est pas sur le "rootId" donc on continue à le chercher dans
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
            sb.append(filledTreeElement(node.getName(), nodeId, "0"));
          } else {
            sb.append(emptyTreeElement(node.getName(), nodeId, "0"));
          }
        } else {
          // on n'affiche pas ni la corbeille ni les declasses
          if (!node.getNodePK().isTrash() && !node.getNodePK().isUnclassed()) {
            // regarder si ce topic contient des publications
            if (topicIds.contains(nodeId)) {
              sb.append(filledTreeElement(node.getName(), nodeId, fatherId));
            } else {
              sb.append(emptyTreeElement(node.getName(), nodeId, fatherId));
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

  String emptyTreeElement(String name, String nodeId, String fatherId) {
    return "elements_treeview.addElement(\"" + name + "\", " + nodeId + ", " + fatherId +
        ", \"dossier\", \"folder\", \"Empty\");\n";
  }

  String filledTreeElement(String name, String nodeId, String fatherId) {
    return "elements_treeview.addElement(\"" + name + "\", " + nodeId + ", " + fatherId +
        ", \"dossier\", \"folder\", " + nodeId + ");\n";
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
    sb.append(getBeginningOfPage(messages.getString("importExport.index") +
        htmlFileExportDir,
        false));
    sb.append("<body>\n");
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
    sb
        .append("<table width=\"100%\" align=\"center\" cellspacing=\"0\" cellpadding=\"2\" " +
                "bgcolor=\"#B3BFD1\">\n");
    sb.append("<tr>\n");
    sb.append("<td width=\"100%\" align=\"center\">\n");
    sb.append("<b>").append(messages.getString("importExport.criteria")).append("<b>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("</table>\n");
    String axisName;
    int axisNb = 0;
    sb
        .append(
            "<table border=0 width=\"100%\" valign=center cellspacing=\"0\" cellpadding=\"2\" " +
                "class=\"intfdcolor\">\n");
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
        sb.append(messages.getString("importExport.allCategories")).append("</option>");
      } else if (node.getLevel() == 3) {
        sb.append("<option value=\"").append(node.getNodePK().getId());
        sb.append("\" class=\"intfdcolor51\">");
        sb.append(EncodeHelper.javaStringToHtmlString(node.getName(language)))
            .append("</option>\n");
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
    sb.append(messages.getString("importExport.unbalanced")).append("</a></td></tr>\n");
    sb.append("<t><td colspan=\"").append(axisNb);
    sb.append("\" align=\"center\"><input type=\"button\" value=\"");
    sb.append(messages.getString("importExport.validate"));
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
   * @param combinationLabels
   * @param timeCriteria
   * @param iframe
   * @return
   */
  public String kmaxPublicationsToHTML(List<String> combinationLabels, String timeCriteria,
      String iframe) {
    StringBuilder sb = new StringBuilder();
    String htmlFileExportDir = encode(fileExportDir);

    sb.append("<html>\n<head>\n<title>").append(messages.getString("importExport.index"));
    sb.append(htmlFileExportDir).append("</title>");
    sb.append(getHtmlStyle());
    sb.append("</head>\n<body>\n");
    if (!combinationLabels.isEmpty()) {
      sb
          .append(
          "<table width=\"100%\" align=center cellspacing=\"0\" cellpadding=\"2\" bgcolor=\"#B3BFD1\">\n");
      sb.append("<tr>\n");
      sb.append("<td width=\"100%\" align=\"center\">\n");
      sb.append("<b>").append(messages.getString("importExport.criteria")).append("</b>\n");
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
      entete.append(messages.getString("importExport.documentsIn")).append(" ");
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
      entete.append(messages.getString("importExport.documentsIn")).append(" ");
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
