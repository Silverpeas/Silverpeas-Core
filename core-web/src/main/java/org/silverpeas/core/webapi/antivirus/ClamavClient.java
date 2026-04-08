/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.antivirus;

import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.InputStream;

/**
 * Implementation of {@link AntivirusClient} using ClamAV for virus scanning.
 * <p>
 * Connects to a ClamAV server based on configuration settings and scans files
 * for viruses. The scan result is returned as an {@link AntivirusResult}.
 */
@Named
@ApplicationScoped
public class ClamavClient implements AntivirusClient {

    private SettingBundle settings;

    @Override
    public String getName() {
        return "clamav";
    }

    @Override
    public AntivirusResult checkVirus(InputStream file) {
        AntivirusResult result = new AntivirusResult();

        if (settings == null) {
            settings = ResourceLocator.getSettingBundle("org.silverpeas.util.attachment.Antivirus");
        }

        xyz.capybara.clamav.ClamavClient client = new xyz.capybara.clamav.ClamavClient(
                settings.getString("clamav.host", "localhost"),
                settings.getInteger("clamav.port", 3310)
        );

        ScanResult scanResult;
        try {
            scanResult = client.scan(file);
        } catch (Exception e) {
            result.setError(true);
            result.setErrorMessage(e.getMessage());
            return result;
        }
        String scanOutput = scanResult.toString();

        result.setSafe(false);
        if (scanOutput.contains("FOUND")) {
            // Virus found
            String virusName = scanOutput.substring(scanOutput.indexOf(": ") + 2, scanOutput.indexOf(" FOUND"));
            result.setSafe(false);
            result.setVirusName(virusName);
        } else if (scanOutput.contains("OK")) {
            // No virus detected
            result.setSafe(true);
        } else {
            result.setError(true);
            result.setErrorMessage(scanOutput);
        }

        return result;
    }
}
