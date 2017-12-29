import { observable, computed, action } from "mobx";
import moment from "moment";
var _ = require("lodash");

export default class MarketSelectionStore {

  constructor(rootStore) {
    this.rootStore = rootStore;
  }

  @observable seriesCount = 2

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

  @observable exchangeList = ["Bittrex", "Gdax"];

  @observable selectedExchange = this.exchangeList[0];

  @observable
  assetList = {
    Bittrex: ["USDT-BTC", "USDT-ETH", "USDT-NEO", "USDT-LTC"],
    Gdax: ["USD-ETH"]
  };

  @observable selectedAsset = this.assetList[this.selectedExchange][0];

  @observable startDate = moment().subtract(2, "month");

  @observable endDate = moment().endOf('day');

  @computed
  get exchangeLists() {
      let list = []
      for(let i=0; i<2; i++) {
            list.push(["Bittrex", "Gdax"])
      }
      return list;
  }

  @action
  onAssetChange = newValue => {
    console.log(newValue);
    this.selectedAsset = newValue;
  };

  @action
  onExchangeChange = (newValue, seriesIndex) => {
    this.selectedExchange = newValue
    this.selectedAsset = this.assetList[this.selectedExchange][0]
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
