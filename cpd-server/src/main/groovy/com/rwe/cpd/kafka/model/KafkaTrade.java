package com.rwe.cpd.kafka.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class KafkaTrade {

    @JsonProperty("System") /* "System" : "TRAYPORT" */
    private String System; // Deal.class line 196 - 199

    @JsonProperty("TransactionID") /* "TransactionID" : "BGC1-101573" */
    private String TransactionID;

    @JsonProperty("Action") /*  "Action" : "Insert" */
    private String Action;

    @JsonProperty("DateTime") /*  "DateTime" : "2017-10-18T15:33:32.21Z" */
    private Date DateTime;

    @JsonProperty("Price") /*  "Price" : 18.3  */
    private String Price;

    @JsonProperty("Volume") /* "Volume" : 60.0, */
    private String Volume;

    @JsonProperty("AggressorCompany")  /* "AggressorCompany" : "RWE Supply & Trading GmbH", */
    private String AggressorCompany;

    @JsonProperty("AggressorTrader") /* "AggressorTrader" : "Dave Matthews" */
    private String AggressorTrader;

    @JsonProperty("AggressorAction") /* "AggressorAction" : "Buy" */
    private String AggressorAction;

    @JsonProperty("InitiatorCompany") /* "InitiatorCompany" : "SPEMAL as agent on behalf of ScottishPower Energy Management Ltd", */
    private String InitiatorCompany;

    @JsonProperty("InitiatorTrader") /* "InitiatorTrader" : "Dave Mooney" */
    private String InitiatorTrader;

    @JsonProperty("InitiatorAction") /* "InitiatorAction" : "Sell" */
    private String InitiatorAction;

//    @JsonProperty("Clearing")
//    private String S_Clearing;
//
//    @JsonProperty("DeliveryPeriodTrueName")
//    private String S_DeliveryPeriod;
//
//    @JsonProperty("TrueStartDate")
//    private String S_StartDate;
//
//    @JsonProperty("TrueEndDate")
//    private String S_EndDate;
//
//    @JsonProperty("RegionMarketName")
//    private String S_Country;
//
//    @JsonProperty("ProductTypeMarketName")
//    private String S_ProductType;
//
//    @JsonProperty("CommodityMarketName")
//    private String S_Commodity;
//
//    @JsonProperty("ContractName")
//    private String S_ContractName;
//
//    @JsonProperty("AdditionalTerm")
//    private String k; // Deal.class line 231

    // -------------- only in Kafka

    @JsonProperty("AggressorBroker") /* "AggressorBroker" : "BGC_RWE1"*/
    private String AggressorBroker;

    @JsonProperty("InitiatorBroker") /* "InitiatorBroker" : "BGC_RWE1" */
    private String InitiatorBroker;

    @JsonProperty("MsgTime") /* "MsgTime" : "2017-10-18T15:34:43Z" */
    private Date MsgTime;

    @JsonProperty("Reason") /* "Reason" : "AllTradeConfirmation" */
    private String Reason;

    @JsonProperty("Product") /*  "Product" : "TTF Hi Cal 51.6 Months" */
    private String Product;

    @JsonProperty("PeriodTypeSpan") /* "PeriodTypeSpan" : "Single" */
    private String PeriodTypeSpan;

    @JsonProperty("Expiry") /*   "Expiry" : "Nov-17" */
    private String Expiry;

    @JsonProperty("ProductID") /*   "ProductID" : 0 */
    private int ProductID;

    @JsonProperty("ExpiryID") /*   "ExpiryID" : 0 */
    private int ExpiryId;

}
