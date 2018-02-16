package com.rwe.cpd.couchdb.model

import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.Transient

class Config {

    @JsonProperty("_id")
    String id

    @JsonProperty("_rev")
    String revision

    @Transient
    Boolean hasChanged = false // forte to persist after a fresh restart!

    TreeSet<String> products = [] as TreeSet

    List<PeriodGroup> periodGroups = []

}
