/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.genericPanel;

public class PanelOperation {
  public String m_helpString = ""; // Not the Id (like GML.xxx) but the
  // language-dependant string (like
  // "Supprimer les fichiers")
  public String m_icon = ""; // Not the Id (like GI.xxx) but the
  // language-dependant string (like
  // "theicons/ico1.ico")
  public String m_command = ""; // The Command parameter sent to the URL
  public String m_confirmation = "";// The confirmation message

  public PanelOperation(String helpString, String icon, String command) {
    m_helpString = helpString;
    m_icon = icon;
    m_command = command;
  }

  public PanelOperation(String helpString, String icon, String command,
      String confirmation) {
    this(helpString, icon, command);
    m_confirmation = confirmation;
  }
}
