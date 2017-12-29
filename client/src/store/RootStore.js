import StrategyStore from "./StrategyStore"
import MarketSelectionStore from "./MarketSelectionStore"
import ChartStore from "./ChartStore"
import StompStore from "./StompStore"
import UserStore from "./UserStore"

import moment from "moment"

export default class RootStore {

  constructor() {
    this.remoteApiUrl = 'http://127.0.0.1:8080/api'
    this.websocketUrl = "http://127.0.0.1:8080/socket"

    moment.locale('de')

    this.userStore = new UserStore(this);
    this.strategyStore = new StrategyStore(this);
    this.marketSelectionStore = new MarketSelectionStore(this);
    this.chartStore = new ChartStore(this);

    this.stompStore = new StompStore(this, this.websocketUrl);
  }
}
