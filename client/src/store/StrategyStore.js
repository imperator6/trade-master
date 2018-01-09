import { observable, computed, action } from "mobx";
import moment from "moment";
import axios from "axios";

var _ = require("lodash");

export default class StrategyStore {
  constructor(rootStore) {
    this.rootStore = rootStore;
  }

  @observable activeTab = "param"

  @observable loaded = false;

  @observable strategyList = [];

  @observable strategyNameList = [];

  @observable selectedStrategyName = "";

  @observable selectedStrategy = null; //{ id: -1, script: '', language: 'javascript', version: 1 };

  @observable portfolioChanges = []

  @observable portfolioResult = {}

  newStrategyCount = 0;

  @action
  selectTab = (newTab) => {
    if(newTab == 'watcher')
      this.rootStore.marketWatcherStore.load()

    this.activeTab = newTab
  };

  @action
  selectStrategyByName = name => {
    let next = this.strategyList.find(s => {
      return s.name === name;
    });
    this.selectedStrategy = next;
  };

  @action
  newStrategy = () => {
    this.newStrategyCount++;
    let nextStrategy = {
      script: "",
      name: "newStrategy" + this.newStrategyCount,
      language: "javascript",
      version: 1
    };

    this.strategyList.push(nextStrategy);
    this.selectedStrategy = nextStrategy;
  };

  @action
  saveScript = newScript => {
    let url = this.rootStore.remoteApiUrl + "/strategy/saveScript";

    this.selectedStrategy.script = newScript;

    axios
      .post(
        url,
        {
          id: this.selectedStrategy.id,
          name: this.selectedStrategy.name,
          script: this.selectedStrategy.script,
          language: this.selectedStrategy.language,
          version: this.selectedStrategy.version
        },
        this.rootStore.userStore.getHeaderConfig()
      )
      .then(response => {
        this.selectedStrategy = response.data;
      })
      .catch(function(error) {
        console.log(error);
      });
  };

  @action
  loadStrategies = () => {
    console.log("loading strategies");

    this.loaded = false;

    let url = this.rootStore.remoteApiUrl + "/strategy/";

    axios
      .get(url, this.rootStore.userStore.getHeaderConfig())
      .then(response => {
        let strategies = response.data;

        this.strategyList = strategies.map(s => {
          return {
            ...s,
            key: s.id
          };
        });

        this.strategyNameList = strategies.map(s => s.name);

        if (this.selectedStrategy == null) {
          this.selectedStrategy = strategies[0];
        }

        this.loaded = true;
      })
      .catch(function(error) {
        console.log(error);
      });
  };

  @action
  runStrategy = () => {
    console.log("run strategy");

    let url = this.rootStore.remoteApiUrl + "/strategy/runStrategy";

    axios
      .post(url, {
        strategyId: this.selectedStrategy.id,
        strategyParams: { param: 1, param2: 2 },
        exchange: this.rootStore.marketSelectionStore.getSelectedExchange(0),
        market: this.rootStore.marketSelectionStore.getSelectedAsset(0),
        candleSize: this.rootStore.marketSelectionStore.getCandleSize()
      },  this.rootStore.userStore.getHeaderConfig())
      .then(response => {
        // this.selectedStrategy = response.data
      })
      .catch(function(error) {
        console.log(error);
      });
  };

  @action
  backtestStrategy = () => {
    console.log("backtest strategy");

    let url = this.rootStore.remoteApiUrl + "/strategy/backtestStrategy";

    let start = this.rootStore.marketSelectionStore.startDate
      .utc()
      .toDate()
      .toISOString();

    let end = this.rootStore.marketSelectionStore.endDate
      .utc()
      .toDate()
      .toISOString();

    axios
      .post(url, {
        strategyId: this.selectedStrategy.id,
        strategyParams: { param: 1, param2: 2 },
        exchange: this.rootStore.marketSelectionStore.getSelectedExchange(0),
        market: this.rootStore.marketSelectionStore.getSelectedAsset(0),
        candleSize: this.rootStore.marketSelectionStore.getCandleSize(),
        backtest: true,
        start: start,
        end: end
      },  this.rootStore.userStore.getHeaderConfig())
      .then(response => {
        let backtestId = response.data.id;
        this.fetchBacktestResult(backtestId);
      })
      .catch(function(error) {
        console.log(error);
      });
  };

  @action
  fetchBacktestResult = backtestId => {
    let url = this.rootStore.remoteApiUrl + "/strategy/backtestResults";

    let config =  this.rootStore.userStore.getHeaderConfig()

    axios
      .get(url, {
        params: {
          backtestId: backtestId
        },
        ...config
      })
      .then(response => {
        if (response.data.success) {
          let results = response.data.data;

          if (results.complete) {
            console.info("Backtest is complete.");

            //console.log(response.data.data);
            this.activeTab = "result"


            this.portfolioChanges = response.data.data.changes
            this.portfolioResult = response.data.data.portfolio

            this.rootStore.chartStore.updateSignales(
              response.data.data.signals
            )

          } else {
            console.info("Backtest is not complete. Will try again in 2sec.");
            setTimeout(() => {
              this.fetchBacktestResult(backtestId);
            }, 2000);
          }
        } else {
          // error
          console.info(response.data.message);
        }
      })
      .catch(function(error) {
        console.log(error);
      });
  };
}
