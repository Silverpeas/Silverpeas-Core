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
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.silverpeas.core.silverstatistics.volume.model.DirectoryStats;

/**
 * @author ehugonnet
 */
public class StatisticDirectoryWalker extends DirectoryWalker<Object> {

  private final DirectoryStats stats;

  public StatisticDirectoryWalker(String dirName) {
    super();
    this.stats = new DirectoryStats(dirName, 0L, 0L);
  }

  public StatisticDirectoryWalker(String dirName, FileFilter filter, int depthLimit) {
    super(filter, depthLimit);
    this.stats = new DirectoryStats(dirName, 0L, 0L);
  }

  public StatisticDirectoryWalker(String dirName, IOFileFilter directoryFilter,
      IOFileFilter fileFilter, int depthLimit) {
    super(directoryFilter, fileFilter, depthLimit);
    this.stats = new DirectoryStats(dirName, 0L, 0L);
  }

  public DirectoryStats scan(File directory) throws IOException {
    walk(directory, null);
    return this.stats;
  }

  @Override
  protected void handleFile(File file, int depth, Collection<Object> results) throws IOException {
    this.stats.addFile(file.length());
    super.handleFile(file, depth, results);
  }
}
