package com.rwe.cpd.couchdb.model

import com.fasterxml.jackson.annotation.JsonProperty

class OrderbookEntry {

    String market

    @JsonProperty("bid")
    List<Bid> bids

    @JsonProperty("ask")
    List<Ask> asks


}
