package cc.blynk.server.core.dao.ota;

import static cc.blynk.utils.StringUtils.BODY_SEPARATOR;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 22.08.17.
 */
public class OTAInfo {

    public final long initiatedAt;
    public final String initiatedBy;
    public final String pathToFirmware;
    public final String build;
    public final String projectName;

    OTAInfo(String initiatedBy, String pathToFirmware, String build, String projectName) {
        this.initiatedAt = System.currentTimeMillis();
        this.initiatedBy = initiatedBy;
        this.pathToFirmware = pathToFirmware;
        this.build = build;
        this.projectName = projectName;
    }

    public static String makeHardwareBody(String serverHostUrl, String pathToFirmware, String token) {
        return "ota" + BODY_SEPARATOR + serverHostUrl + pathToFirmware + "?token=" + token;
    }

    public boolean matches(String dashName) {
        return projectName == null || projectName.equalsIgnoreCase(dashName);
    }

    @Override
    public String toString() {
        return "OTAInfo{"
                + "initiatedAt=" + initiatedAt
                + ", initiatedBy='" + initiatedBy + '\''
                + ", pathToFirmware='" + pathToFirmware + '\''
                + ", build='" + build + '\''
                + ", projectName='" + projectName + '\''
                + '}';
    }
}
