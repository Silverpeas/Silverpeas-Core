package org.silverpeas.core.webapi.antivirus;

import org.silverpeas.kernel.bundle.SettingBundle;

import java.io.InputStream;

public interface AntivirusClient {
    public String getName();
    public AntivirusResult checkVirus(InputStream file, SettingBundle settings);
}
