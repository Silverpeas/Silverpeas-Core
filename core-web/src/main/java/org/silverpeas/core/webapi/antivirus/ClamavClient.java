package org.silverpeas.core.webapi.antivirus;

import org.silverpeas.kernel.bundle.SettingBundle;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.InputStream;

@Named
@ApplicationScoped
public class ClamavClient implements AntivirusClient {
    @Override
    public String getName() {
        return "clamav";
    }

    @Override
    public AntivirusResult checkVirus(InputStream file, SettingBundle settings) {
        AntivirusResult result = new AntivirusResult();

        xyz.capybara.clamav.ClamavClient client = new xyz.capybara.clamav.ClamavClient(
                settings.getString("clamav.host", "localhost"),
                settings.getInteger("clamav.port", 3310)
        );

        ScanResult scanResult = null;
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
