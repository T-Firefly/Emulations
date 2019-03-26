package com.firefly.emulationstation.data.bean;

import com.firefly.emulationstation.BuildConfig;
import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.utils.I18nHelper;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class Version implements Serializable {
    private int versionCode;
    private String version;
    private String url;
    private Map<String, String> logs;

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        if (url.startsWith("http://")
                || url.startsWith("https://")) {
            return url;
        }

        if (url.startsWith("/")) {
            try {
                URL urlObj = new URL(BuildConfig.VERSION_CHECK_URL);
                return new URL(urlObj.getProtocol(), urlObj.getHost(), urlObj.getPort(), url).toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return url;
            }
        } else {
            return BuildConfig.VERSION_CHECK_URL
                    .substring(0, BuildConfig.VERSION_CHECK_URL.lastIndexOf('/') + 1)
                    + url;
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getLogs() {
        return logs;
    }

    public void setLogs(Map<String, String> logs) {
        this.logs = logs;
    }

    public String getLocalizeLog() {
        if (logs == null) {
            return null;
        }

        return I18nHelper.getValueFromMap(logs);
    }

    public boolean hasNewVersion() {
        return versionCode > BuildConfig.VERSION_CODE;
    }
}
