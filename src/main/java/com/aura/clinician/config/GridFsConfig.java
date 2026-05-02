package com.aura.clinician.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
public class GridFsConfig {

    @Bean
    public GridFsTemplate prescriptionAssetsGridFs(
        MongoDatabaseFactory dbFactory,
        MongoConverter mongoConverter
    ) {
        return new GridFsTemplate(dbFactory, mongoConverter, "prescription_assets");
    }
}
