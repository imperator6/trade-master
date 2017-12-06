import { observable, computed, action } from "mobx";
import moment from 'moment';
var _ = require("lodash");

export default class StrategyStore {

      constructor(rootStore) {
            this.rootStore = rootStore
      }

      @observable periodList = ['1m','5m','15m','30m','1h','2h','4h','1d','3d'];

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


    
  
    



    
}
    