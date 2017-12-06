import { observable, computed, action } from "mobx";
import moment from 'moment';
var _ = require("lodash");

export default class MarketSelectionStore {

      constructor(rootStore) {
            this.rootStore = rootStore
      }

      @observable periodList = ['1 m','5 m','15 m','30 m','1 h','2 h','4 h','1 d','3 d'];

      @observable selectedPeriod = this.periodList[1]

      @observable exchangeList = ["Bittrex"];

      @observable selectedExchange = this.exchangeList[0]
      
      @observable assetList = {
        Bittrex: ["USDT-BTC", "USDT-ETH", "USDT-NEO"]
      };

      @observable selectedAsset = this.assetList[this.selectedExchange][0]

      @observable startDate = moment().subtract(2, 'month')

      @observable endDate = moment()

      @action
      onAssetChange = (newValue) => {
            console.log(newValue)
            this.selectedAsset = newValue
      }

      @action
      onPeriodChange = (newValue) => {
            console.log(newValue)
            this.selectedPeriod = newValue
      }

      @action
      load = () => {
          this.rootStore.chartStore.loadChart(this)

      }


    
  
    



    
}
    