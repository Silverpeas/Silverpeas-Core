/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.silverstatistics.control;

import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.silverstatistics.volume.service.DirectoryVolumeService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.silverpeas.core.util.UnitUtil.convertTo;
import static org.silverpeas.core.util.memory.MemoryUnit.B;
import static org.silverpeas.core.util.memory.MemoryUnit.KB;

/**
 * Class declaration Get stat size directory data from database
 */
class SilverStatisticsPeasDAOVolumeServer {

  private SilverStatisticsPeasDAOVolumeServer() {
    throw new IllegalStateException("Stateless DAO class");
  }

  /**
   * Gives global stats for all users.
   * @return Collection of array of string.
   * @throws SQLException on technical error with database.
   *
   */
  static Collection<String[]> getStatsVolumeServer() throws SQLException {
    return JdbcSqlQuery
        .createSelect("dateStat, fileDir, sizeDir")
        .from("SB_Stat_SizeDirCumul")
        .orderBy("dateStat")
        .execute(r -> {
          final String date = r.getString(1);
          final String repository = r.getString(2);
          final String size = String.valueOf(convertTo(r.getLong(3), B, KB));
          return new String[]{date, repository, size};
        });
  }

  static Map<String, String[]> getStatsSizeVentil() throws SilverpeasException {
    final DirectoryVolumeService service = new DirectoryVolumeService();
    try {
      return service.getSizeVentilation(UserDetail.getCurrentRequester().getId());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new SilverpeasException(e);
    } catch (ExecutionException e) {
      throw new SilverpeasException(e);
    }
  }

  static Map<String, String[]> getStatsVentil() throws SilverpeasException {
    final DirectoryVolumeService service = new DirectoryVolumeService();
    try {
      return service.getFileNumberVentilation(UserDetail.getCurrentRequester().getId());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new SilverpeasException(e);
    } catch (ExecutionException e) {
      throw new SilverpeasException(e);
    }
  }
}
