package com.banfftech.odata.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustRequestManage extends EdmServiceConfig{
    @JsonProperty("service-name")
    private String serviceName;

    @JsonProperty("name-space")
    private String nameSpace;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }
}
