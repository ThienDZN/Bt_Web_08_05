package vn.iotstar.config;

import java.io.File;

public final class UploadConstants {
    public static final String DIR = AppProperties.get("app.upload.dir",
            System.getProperty("user.home") + File.separator + "uploads" + File.separator + "assignment08-exercise05-graphql");

    private UploadConstants() {
    }
}
