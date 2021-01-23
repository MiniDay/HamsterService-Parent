package cn.hamster3.service.server.data;

import java.util.List;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class ServerConfig {
    private String serviceAddress;
    private int servicePort;

    private int nioThread;

    private List<String> acceptList;

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public int getNioThread() {
        return nioThread;
    }

    public void setNioThread(int nioThread) {
        this.nioThread = nioThread;
    }

    public List<String> getAcceptList() {
        return acceptList;
    }

    public void setAcceptList(List<String> acceptList) {
        this.acceptList = acceptList;
    }
}
