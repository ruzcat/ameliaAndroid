package cl.brown.amelia.service;

import java.io.Serializable;

public class AmeliaResponseBody implements Serializable {
    private String result;
    private String message;
    private DataDevice data;

    public AmeliaResponseBody(String result, String message, DataDevice data) {
        this.result = result;
        this.message = message;
        this.data = data;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataDevice getData() {
        return data;
    }

    public void setData(DataDevice data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AmeliaResponseBody {");
        sb.append("\"result\":\"").append(result).append("\"");
        sb.append(", \"message\":").append(message).append("\"");
        sb.append(", \"data\":").append(data).append("");
        sb.append('}');
        return sb.toString();
    }

    public static class DataDevice implements Serializable {
        private String serial;
        private String ip;
        private String apIp;
        private String port;

        public String getSerial() {
            return serial;
        }

        public void setSerial(String serial) {
            this.serial = serial;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getApIp() {
            return apIp;
        }

        public void setApIp(String apIp) {
            this.apIp = apIp;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("{");
            sb.append("\"serial\":\"").append(getSerial()).append("\"");
            sb.append(", \"ip\":\"").append(getIp()).append("\"");
            sb.append(", \"apIp\":\"").append(getApIp()).append("\"");
            sb.append(", \"port\":").append(getPort()).append("");
            sb.append('}');
            return sb.toString();
        }
    }
}
