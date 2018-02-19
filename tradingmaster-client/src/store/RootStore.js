import StrategyStore from "./StrategyStore"
import MarketSelectionStore from "./MarketSelectionStore"
import ChartStore from "./ChartStore"
import StompStore from "./StompStore"
import UserStore from "./UserStore"
import PositionStore from "./PositionStore"
import PositionSettingsStore from "./PositionSettingsStore"
import MarketWatcherStore from "./MarketWatcherStore"

import moment from "moment"

export default class RootStore {

  constructor() {
    this.remoteApiUrl = SERVICE_URL + '/api'
    this.websocketUrl = SERVICE_URL + '/socket'

    moment.locale('de')

    this.userStore = new UserStore(this)
    this.strategyStore = new StrategyStore(this)
    this.marketSelectionStore = new MarketSelectionStore(this)
    this.chartStore = new ChartStore(this)
    this.marketWatcherStore = new MarketWatcherStore(this)
    this.positionStore = new PositionStore(this)
    this.positionSettingsStore = new PositionSettingsStore(this)

    this.stompStore = new StompStore(this, this.websocketUrl);

    
  }
}
