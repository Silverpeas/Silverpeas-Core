<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>

<%!
void displayBeginFrame(JspWriter out) throws IOException {
out.println("<center>");
  out.println("<!-- Cadre exterieur -->");
    out.println("<table class=\"intfdcolor\" cellpadding=\"2\" cellspacing=\"0\" border=\"0\" width=\"98%\">");
      out.println("<tr align=\"center\">");
        out.println("<td align=\"center\">");

          out.println("<!-- Cadre interieur -->");
           out.println("<table class=\"intfdcolor4\" border=\"0\" cellspacing=\"0\" cellpadding=\"5\" width=\"100%\">");
             out.println("<tr valign=\"middle\">");
              out.println("<td align=\"center\">");

          out.println("<!-- Couleur de Fond  du Cadre interieur -->");
          out.println("<TABLE class=\"couleurFondCadre\" border=\"0\" cellPadding=\"4\" cellSpacing=\"0\" width=\"100%\">");
            out.println("<tr>");
              out.println("<td align=\"center\">");
}

void displayEndFrame(JspWriter out) throws IOException {
    out.println("</TD></TR></TABLE>");
    out.println("</TD></TR></TABLE> <!-- Fin cadre interieur -->");
    out.println("</TD></TR></TABLE> <!-- Fin cadre exterieur -->");
    out.println("</center> ");
}

void displayTabbedPane(SILVERMAILSessionController pSC, GraphicElementFactory gef, String function, JspWriter out) throws IOException {

      // Barre d'onglet
      TabbedPane tabbedPane = gef.getTabbedPane();

      tabbedPane.addTab( pSC.getString("onglet_inbox"), "Main.jsp", function.startsWith("Main") );

      out.println(tabbedPane.print());
}

%>