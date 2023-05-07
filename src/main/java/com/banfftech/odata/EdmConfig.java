package com.banfftech.odata;

import io.smallrye.config.ConfigMapping;
import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@ConfigMapping(prefix = "edm-config")
public interface EdmConfig {
    public String backend();
    public List<OdataService> services();
    interface OdataService {
        public String serviceName();
        public List<String> entityType();
    }
}
