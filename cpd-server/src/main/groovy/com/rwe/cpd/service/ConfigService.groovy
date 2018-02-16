package com.rwe.cpd.service

import com.rwe.cpd.couchdb.model.Config
import groovy.transform.Memoized
import groovy.util.logging.Commons
import org.ektorp.CouchDbConnector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
@Commons
class ConfigService {


    @Autowired
    CouchDbConnector configStorage


    @Async
    void addProductIfNotExsists(String product) {

        Config c = getConfig()

        if(!c.products.contains(product)){
            c.products.add(product)
            c.hasChanged = true
        }
    }

    @Memoized
    Config getConfig() {

       Config config = configStorage.get(Config.class, "config_all")

        if(config == null) {
            config = new Config()
            configStorage.create(config)
        }

        config
    }


}
