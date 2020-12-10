package com.zres.project.localnet.portal.util;

public class FtpConfigDto {
    private String ip;
    private String port;
    private String username;
    private String password;
    private String localdir;
    private String remotedir;


    public FtpConfigDto(String ip, String port, String username,
                        String password, String path, String holdpath) {
        super();
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.localdir = path;
        this.remotedir = holdpath;
    }

    public FtpConfigDto() {

    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLocaldir() {
        return localdir;
    }

    public void setLocaldir(String local_dir) {
        this.localdir = local_dir;
    }

    public String getRemotedir() {
        return remotedir;
    }

    public void setRemotedir(String remote_dir) {
        this.remotedir = remote_dir;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result
                + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((port == null) ? 0 : port.hashCode());
        result = prime * result
                + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FtpConfigDto other = (FtpConfigDto) obj;
        if (ip == null) {
            if (other.ip != null) {
                return false;
            }
        }
        else if (!ip.equals(other.ip)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        }
        else if (!password.equals(other.password)) {
            return false;
        }
        if (port == null) {
            if (other.port != null) {
                return false;
            }
        }
        else if (!port.equals(other.port)) {
            return false;
        }
        if (username == null) {
            if (other.username != null) {
                return false;
            }
        }
        else if (!username.equals(other.username)) {
            return false;
        }
        return true;
    }

    public boolean hasFtpConfig() {
        boolean res = true;
        if (ip == null || password == null || port == null || username == null
                || "".equals(ip) || "".equals(password) || "".equals(port) || "".equals(username)) {
            res = false;
        }
        return res;
    }
}