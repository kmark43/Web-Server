package git.kmark43.webserver;

import java.util.Properties;

public class HttpResponse {
    private String code;
    private Properties properties;
    private byte[] body;

    public HttpResponse(String code, Properties properties, byte[] body) {
        this.code = code;
        this.properties = properties;
        this.body = body;
    }

    public String getCode() {
        return code;
    }

    public Properties getProperties() {
        return properties;
    }

    public byte[] getBody() {
        return body;
    }
}
