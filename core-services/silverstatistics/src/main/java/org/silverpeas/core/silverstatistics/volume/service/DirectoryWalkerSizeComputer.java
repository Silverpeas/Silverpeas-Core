/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.silverstatistics.volume.service;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.silverpeas.core.silverstatistics.volume.model.DirectoryStats;

/**
 * Compute the size in terms of number of files and total size of a directory.
 */
public class DirectoryWalkerSizeComputer implements Callable<DirectoryStats> {

  private final File directory;


  public DirectoryWalkerSizeComputer(File directory) {
    this.directory = directory;
  }

  @Override
  public DirectoryStats call() throws Exception {
    StatisticDirectoryWalker walker = new StatisticDirectoryWalker(directory.getName(),
        TrueFileFilter.TRUE, -1);
    return walker.scan(directory);
  }
}
