import { observable, computed, action } from "mobx";
import moment from "moment";
import axios from "axios";
var _ = require("lodash");

import logger from "../logger";

export default class MarketSelectionStore {
  log = logger.getLogger("MarketSelectionStore");

  constructor(rootStore) {
    this.rootStore = rootStore;
  }

  @observable
  periodList = [
    "1 m",
    "5 m",
    "15 m",
    "30 m",
    "1 h",
    "2 h",
    "4 h",
    "1 d",
    "3 d"
  ];

  @observable selectedPeriod = "1 h";

  @observable seriesCount = 1;

  @observable exchangeList = [];

  @observable selectedExchangeBySeries = new Map();

  @observable selectedAssetBySeries = new Map();

  @observable assetMap = new Map();

  /*{
    Bittrex: ["USDT-BTC", "BTC-ETH", "USDT-ETH", "USDT-NEO", "USDT-LTC"],
    Gdax: ["USD-ETH", "BTC-ETH", "USD-BTC",]
  };*/

  //@observable selectedAsset = "USDT-BTC" //this.assetMap[this.selectedExchange][0];

  @observable startDate = moment().subtract(2, "month");

  @observable endDate = moment().endOf("day");

  init = () => {
    this.log.debug(
      "init Market Selection Store -> loading available exchanges"
    );

    let url = this.rootStore.remoteApiUrl + "/exchange/watchedExchanges/";

    axios
      .get(url, this.rootStore.userStore.getHeaderConfig())
      .then(response => {
        if (response.data.success) {
          this.log.debug("Loaded Exchanges", { ...response.data.data });
          // console.log( response.data.data)

          let newExchangeList = [];
          response.data.data.forEach(exchange => {
            // console.log( exchange)
            newExchangeList.push(exchange.name);

            let newAssetList = exchange.markets.map(market => {
              return market.currency + "-" + market.asset;
            });

            this.assetMap.set(exchange.name, newAssetList);
            //     this.selectedAssetBySeries.set(exchange.name, newAssetList[0])
          });

          this.exchangeList = newExchangeList;

          //  this.selectedExchangeBySeries.set(0, newExchangeList[0])

          this.load();
        } else {
          // error
          console.info(response.data.message);
        }
      })
      .catch(function(error) {
        console.log(error);
      });
  };

  @action
  addSeries = (exchange, market) => {
    if (exchange && market) {
      if (!this.getSelectedExchange(this.seriesCount - 1)) {
        this.onExchangeChange(exchange, this.seriesCount - 1);
        this.onAssetChange(market, this.seriesCount - 1);
      } else {
        this.seriesCount++;
        this.onExchangeChange(exchange, this.seriesCount - 1);
        this.onAssetChange(market, this.seriesCount - 1);
      }
    } else {
      this.seriesCount++;
    }
  };

  @action
  select = (exchange, market) => {
    this.onExchangeChange(exchange, this.seriesCount - 1);
    this.onAssetChange(market, this.seriesCount - 1);
    this.load();
  };

  @action
  removeSeries = () => {
    if (this.seriesCount > 1) this.seriesCount--;
  };

  @action
  onExchangeChange = (newValue, seriesIndex) => {
    this.log.debug(
      "new exchange selection for index ",
      seriesIndex,
      " is ",
      newValue
    );
    //this.selectedExchange = newValue
    this.selectedAssetBySeries.delete(seriesIndex);

    this.selectedExchangeBySeries.set(seriesIndex, newValue);
  };

  getSelectedExchange(seriesIndex) {
    let selectedExchange = this.selectedExchangeBySeries.get(seriesIndex);
    /*if(!selectedExchange) {
        selectedExchange = this.exchangeList[0] // select the first
        this.selectedExchangeBySeries.set(seriesIndex, selectedExchange)
        this.log.debug('No Exchange is selected for series ' + seriesIndex + "! Selecting first from list  " + selectedExchange)
      }*/
    this.log.debug(
      "Selected  Exchange for series " + seriesIndex + " is " + selectedExchange
    );
    return selectedExchange;
  }

  getSelectedAsset(seriesIndex) {
    //console.log("get selected asset for index " + seriesIndex)
    let ex = this.getSelectedExchange(seriesIndex);

    let selected = this.selectedAssetBySeries.get(seriesIndex);
    /*if(!selected) {
       //if(!this.assetMap.get(ex)) this.assetMap.set(ex, [])
        selected = this.assetMap.get(ex)[0]
        
        this.log.debug('No Market is selected for exchange ' + ex + ' on series ' + seriesIndex + "! Returning first from list  " + selected)
      } */

    this.log.debug("Selected  Market for exchange " + ex + " is " + selected);
    //console.log("selected asset for index " + seriesIndex + " is " + selected);
    return selected;
  }

  @action
  onAssetChange = (newValue, seriesIndex) => {
    this.log.debug(
      "new market selection for index ",
      seriesIndex,
      " is ",
      newValue
    );
    this.selectedAsset = newValue;
    this.selectedAssetBySeries.set(seriesIndex, newValue);
  };

  @action
  onPeriodChange = newValue => {
    console.log(newValue);
    this.selectedPeriod = newValue;
  };

  @action
  load = () => {
    this.rootStore.chartStore.loadChart();
  };

  @action
  startCandleImport = () => {
    let exchange = this.getSelectedExchange(0);
    let asset = this.getSelectedAsset(0);
    //let period = this.rootStore.marketSelectionStore.selectedPeriod;

    let startDate = this.startDate
      .utc()
      .toDate()
      .toISOString()

    let endDate = this.endDate
      .utc()
      .toDate()
      .toISOString()

    if (!exchange || !asset) return

    exchange = exchange.toLowerCase()
    asset = asset.toLowerCase()

    let params = {
      start: startDate,
      end: endDate
    };

    let esc = encodeURIComponent;
    let query = Object.keys(params)
      .map(k => esc(k) + "=" + esc(params[k]))
      .join("&");

    let url =
      this.rootStore.remoteApiUrl +
      "/candles/importCandles/" +
      exchange +
      "/" +
      asset +
      "?" +
      query;

    let config = this.rootStore.userStore.getHeaderConfig();

    config = {
      ...params,
      ...config
    };

    axios.get(url, config).then(response => {
      
      console.log(response);
    });
  };

  getCandleSize = () => {
    let periodSplit = this.selectedPeriod.split(" ");
    let periodSelector = periodSplit[1];
    let periodValue = periodSplit[0];

    let candleSize = 1;
    switch (periodSelector) {
      case "m":
        candleSize = periodValue;
        break;
      case "h":
        candleSize = periodValue * 60;
        break;
      case "d":
        candleSize = periodValue * 60 * 24;
        break;
      default:
        candleSize = 1;
    }

    return candleSize;
  };
}
