package com.rwe.cpd.couchdb.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.Transient

@JsonInclude(JsonInclude.Include.NON_NULL)
class Orderbook {

    @JsonProperty("_id")
    String id

    @JsonProperty("_rev")
    String revision

    String type

    @Transient
    Boolean hasChanged = true // forte to persist after a fresh restart!

    Integer priceSeqNum

    @JsonProperty("orderbook")
    OrderbookEntry entries = new OrderbookEntry()


}
