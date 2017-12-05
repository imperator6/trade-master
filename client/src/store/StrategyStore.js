import { observable, computed, action } from "mobx";
var _ = require("lodash");

export default class StrategyStore {

      constructor(rootStore) {
            this.rootStore = rootStore
      }
    
      @observable periodList = ['1m','5m','15m','30m','1h','2h','4h','1d','3d'];

      @observable selectedPeriod = this.periodList[1]
    
  
    



    
}
    