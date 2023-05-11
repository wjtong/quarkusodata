package com.banfftech.odata;

import com.banfftech.model.GenericEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.List;

@ApplicationScoped
public class EdmConfigLoader {
    @Inject
    EdmConfig edmconfig;

    public EdmService loadService(String serviceName) throws IOException, ClassNotFoundException {
        EdmService edmService = new EdmService();
        ObjectMapper objectMapper = new ObjectMapper();
        String filePath = Paths.get("config", serviceName + ".json").toString();
        EdmServiceConfig edmServiceConfig = objectMapper.readValue(new File(filePath), EdmServiceConfig.class);


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
        try {
            Class<?> genericEntityClazz = Class.forName(entityType);
            GenericEntity genericEntity = (GenericEntity) genericEntityClazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return csdlEntityType;
    }
}
