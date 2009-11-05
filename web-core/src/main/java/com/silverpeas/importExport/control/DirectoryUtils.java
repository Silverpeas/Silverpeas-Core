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
/*
 * Created on 22 févr. 2005
 */
package com.silverpeas.importExport.control;

import java.io.File;

/**
 * @author sdevolder Méthodes à factoriser
 */
public class DirectoryUtils {

  /**
   * Retourne une chaine ne contenant que des caractères autorisés pour le
   * nommage des dossiers. en évitant de convertir les séparateurs de fichier.
   * On suppose que ? ne pourra jamais être un séparateur de fichiers.
   * 
   * @param directoryName
   * @return
   */
  public static String formatToDirectoryPathNamingCompliant(String directoryPath) {

    String tempDir = directoryPath.trim();

    tempDir = tempDir.replace('?', '_');
    tempDir = tempDir.replace(File.separatorChar, '?');
    tempDir = tempDir.replace('\\', '_');
    tempDir = tempDir.replace('/', '_');
    tempDir = tempDir.replace(':', '_');
    tempDir = tempDir.replace('*', '_');
    tempDir = tempDir.replace('\"', '_');
    tempDir = tempDir.replace('<', '_');
    tempDir = tempDir.replace('>', '_');
    tempDir = tempDir.replace('|', '_');

    tempDir = tempDir.replace('?', File.separatorChar);

    return tempDir;
  }

  /**
   * Retourne une chaine ne contenant que des caractères autorisés pour le
   * nommage des dossiers y compris les séparateurs de fichier éventuellement.
   * 
   * @param directoryName
   * @return
   */
  public static String formatToDirectoryNamingCompliant(String directoryName) {

    String tempDir = directoryName.trim();

    tempDir = tempDir.replace('?', '_');
    tempDir = tempDir.replace('\\', '_');
    tempDir = tempDir.replace('/', '_');
    tempDir = tempDir.replace(':', '_');
    tempDir = tempDir.replace('*', '_');
    tempDir = tempDir.replace('\"', '_');
    tempDir = tempDir.replace('<', '_');
    tempDir = tempDir.replace('>', '_');
    tempDir = tempDir.replace('|', '_');

    return tempDir;
  }
}