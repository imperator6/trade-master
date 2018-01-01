import { observable, computed, action } from "mobx";
import moment from "moment";
var _ = require("lodash");

export default class MarketSelectionStore {

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

  @observable selectedPeriod = "5 m";

  @observable seriesCount = 1

  @observable exchangeList = ["Bittrex", "Gdax"]

  @observable selectedExchangeBySeries = new Map()

  @observable selectedAssetBySeries = new Map()

  @observable
  assetList = {
    Bittrex: ["USDT-BTC", "BTC-ETH", "USDT-ETH", "USDT-NEO", "USDT-LTC"],
    Gdax: ["USD-ETH", "BTC-ETH", "USD-BTC",]
  };

  //@observable selectedAsset = "USDT-BTC" //this.assetList[this.selectedExchange][0];

  @observable startDate = moment().subtract(2, "month");

  @observable endDate = moment().endOf('day');

  addSeries = () => {
    this.seriesCount++
  };

  removeSeries = () => {
    if(this.seriesCount >1)
      this.seriesCount--
  };

  @action
  onExchangeChange = (newValue, seriesIndex) => {
    this.selectedExchange = newValue
    this.selectedAssetBySeries.delete(seriesIndex)

    this.selectedExchangeBySeries.set(seriesIndex, newValue)
  };

  getSelectedExchange(seriesIndex) {
    let selectedExchange = this.selectedExchangeBySeries.get(seriesIndex)
      if(!selectedExchange) {
        selectedExchange = this.exchangeList[0]
      }
      //console.log("selected exchange for index " + seriesIndex + " is " + selectedExchange);
      return selectedExchange
  }

  getSelectedAsset(seriesIndex) {
    //console.log("get selected asset for index " + seriesIndex)
    let selected = this.selectedAssetBySeries.get(seriesIndex)
      if(!selected) {
        selected = this.assetList[this.getSelectedExchange(seriesIndex)][0]
      }

      //console.log("selected asset for index " + seriesIndex + " is " + selected);
      return selected
  }

  @action
  onAssetChange = (newValue, seriesIndex) => {
    console.log(newValue);
    this.selectedAsset = newValue;
    this.selectedAssetBySeries.set(seriesIndex, newValue)
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
  }
}
