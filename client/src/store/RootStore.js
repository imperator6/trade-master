import StrategyStore from "./StrategyStore"
import MarketSelectionStore from "./MarketSelectionStore"
import ChartStore from "./ChartStore"

export default class RootStore {

  constructor() {
    this.strategyStore = new StrategyStore(this);
    this.marketSelectionStore = new MarketSelectionStore(this);
    this.chartStore = new ChartStore(this);

    this.remoteApiUrl = 'http://127.0.0.1:8080/api'
  }
}
