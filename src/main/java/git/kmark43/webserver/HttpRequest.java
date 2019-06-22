package git.kmark43.webserver;

import java.io.File;
import java.util.Properties;

public class HttpRequest {
    private String type;
    private File file;
    private Properties properties;

    public HttpRequest(String type, File file, Properties properties) {
        this.type = type;
        this.file = file;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public File getFile() {
        return file;
    }

    public Properties getProperties() {
        return properties;
    }
}
