package com.banfftech.odata;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;

import java.util.List;

@ApplicationScoped
public class EdmConfigLoader {
    @Inject
    EdmConfig edmconfig;

    public EdmService loadService(String serviceName) {
        EdmService edmService = new EdmService();
        List<EdmConfig.OdataService> odataServiceList = edmconfig.services();
        for (EdmConfig.OdataService odataService:odataServiceList) {
            if (odataService.serviceName() == serviceName) {
                List<String> entityTypes = odataService.entityType();
                for (String entityType:entityTypes) {
                    edmService.addEntityType(loadEntityType(entityType));
                }
            }
        }
        return edmService;
    }

    private CsdlEntityType loadEntityType(String entityType) {
        CsdlEntityType csdlEntityType = new CsdlEntityType();
        return csdlEntityType;
    }
}
