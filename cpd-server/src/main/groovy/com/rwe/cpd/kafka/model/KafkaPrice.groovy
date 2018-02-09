package com.rwe.cpd.kafka.model

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.ToString

@ToString(includeFields = true, includeNames=true)
class KafkaPrice {

    @JsonProperty("Action") /* "Action" : "Update" */
    String Action

    @JsonProperty("AllOrNone") /* "AllOrNone" : false */
    boolean AllOrNone

    @JsonProperty("BuySell") /* "BuySell" : "Bid" */
    String BuySell

    @JsonProperty("S_Clearing") /* "S_Clearing" : "1" */
    int S_Clearing

    @JsonProperty("S_Commodity") /* "S_Commodity" : "POWER" */
    String S_Commodity

   // @JsonProperty("Company")
   // String Company

    @JsonProperty("DateTime") /* "DateTime" : "2017-10-18T09:14:37.703Z" */
    Date CreationDate

  //  @JsonProperty("DeliveryPeriodId")
  //  int DeliveryPeriodId

  //  @JsonProperty("DeliveryPeriodMarketName")
  //  String DeliveryPeriodMarketName

  //  @JsonProperty("DeliveryPeriodShortName")
  //  String DeliveryPeriodShortName

    @JsonProperty("ExpiryID") /*  "ExpiryID" : 12018 */
    int ExpiryId

    @JsonProperty("S_ContractName") /* "S_ContractName" : "Cal18" */
    String S_ContractName

    @JsonProperty("PriceID") /* "PriceID" : 220417 */
    String PriceID

    @JsonProperty("CounterPartyOk") /* "CounterPartyOk" : true */
    boolean CounterpartyOk

    @JsonProperty("MsgTime") /* "MsgTime" : "2017-10-18T09:18:15Z" */
    Date MsgTime

    @JsonProperty("OldPriceID") /* "OldPriceID" : 220021 */
    long OldPriceID

    @JsonProperty("PeriodTypeID") /* "PeriodTypeID" : 2 */
    long PeriodTypeId

    @JsonProperty("PeriodTypeSpan") /* "PeriodTypeSpan" : "Single" */
    String PeriodTypeSpan

    @JsonProperty("ProductID") /* "ProductID" : 3133 */
    long ProductId

    //@JsonProperty("ProductTypeId")
    //long ProductTypeId

    @JsonProperty("S_ProductType") /* "S_ProductType" : "BASE" */
    String S_ProductType


    @JsonProperty("ReceiveTime")
    String ReceiveTime
    //Date ReceiveTime

   // @JsonProperty("RegionId")
   // long RegionId

    @JsonProperty("S_Country") /*  "S_Country" : "FRANCE", */
    String S_Country

   // @JsonProperty("SecondExpiryId")
   // long SecondExpiryId

    //@JsonProperty("SecondndExpiryTrueName")
    //String SecondndExpiryTrueName

    //@JsonProperty("SequenceId")
    //long SeqId

    @JsonProperty("Status") /* "Status" : "Firm" */
    String Status

    @JsonProperty("System") /*  "System" : "EEX", */
    String System

    //@JsonProperty("TraderName")
    //String TraderName

    @JsonProperty("Price") /* "Price" : 42.0 */
    double Price

    @JsonProperty("Volume") /* "Volume" : 5.0 */
    double Volume

    //@JsonProperty("PeriodEndDate")
    //Date PeriodEndDate

    //@JsonProperty("PeriodStartDate")
    //Date PeriodStartDate

    //@JsonProperty("CreationDateTime")
    //Date CreationDateTime

    // ----------------- only in KafkaPrice


    @JsonProperty("Reason") /* "Reason" : "OrderbookSnapshot" */
    String Reason

    @JsonProperty("HiddenVolume") /* "HiddenVolume" : 0.0 */
    long HiddenVolume

    @JsonProperty("Product") /* "Product" : "F7BY" */
    String Product

    @JsonProperty("Expiry") /*     "Expiry" : "Cal18" */
    String Expiry

    @JsonProperty("Broker") /*  "Broker" : "EEX" */
    String Broker

    @JsonProperty("AdditionalTerm") /*  " "AdditionalTerm" : "0" */
    String AdditionalTerm

    @JsonProperty("S_DeliveryPeriod") /* "S_DeliveryPeriod" : "Y" */
    String S_DeliveryPeriod

    @JsonProperty("S_StartDate")
    Date S_StartDate  /* "S_StartDate" : "2018-01-01T00:00:00Z" */

    @JsonProperty("S_EndDate")
    Date S_EndDate  /* "S_EndDate" : "2018-12-31T00:00:00Z" */
    




    // TTF,H GAS,GAS,ALL Sat 04/11/17
}
