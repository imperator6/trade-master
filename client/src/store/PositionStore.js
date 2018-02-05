import axios from "axios";
import { observable, computed, action } from "mobx";

import logger from "../logger";

import moment from "moment";

import { message } from "antd";

class MarketWatcherStore {
  log = logger.getLogger("MarketWatcherStore");

  @observable positions = [];

  @observable botList = [];

  @observable baseCurrency = "BTC";
  @observable startBalance = 0;
  @observable currentBalance = 0;
  @observable totalBaseCurrencyValue = 0;



  @observable fxDollar = 0;
  @observable startBalanceDollar = 0;
  @observable currentBalanceDollar = 0;
  @observable totalBalanceDollar = 0;
  @observable totalBotResult = 0;

  @observable botMap = new Map();

  @observable selectedBot;

  // @observable selectedAsset;

  // for a new position
  @observable exchangeList = [];

  @observable assetMap = new Map();

  @observable selectedExchange;

  @observable selectedAsset;

  constructor(rootStore) {
    this.log.debug("New MarketWatcherStore!");
    this.rootStore = rootStore;
  }

  init = () => {

    let cb = () => {
        // select the first bot :-)
        this.selectedBot = this.botList[0].split("_")[0];
        this.onBotSelected(this.selectedBot);
    }
    
    this.loadBotList(cb);
  };

  loadBotList = (callback) => {
    let url = this.rootStore.remoteApiUrl + "/bot/";

    axios
      .get(url, this.rootStore.userStore.getHeaderConfig())
      .then(response => {
        if (response.data.success) {
          this.log.debug("Loaded Bots", { ...response.data.data });
          // console.log( response.data.data)

          let newBotList = [];
          response.data.data.forEach(bot => {
            // console.log( exchange)
            newBotList.push(
              bot.id + "_" + bot.exchange + "_" + bot.baseCurrency
            );
            this.botMap.set(bot.id, bot);
            //newBotList.push(bot)
          });

          this.botList = newBotList;

          if(callback) {
             callback()
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

  getSelectedBot = () => {
    return this.botMap.get(this.selectedBot);
  };

  @action
  onBotSelected = botId => {
    this.selectedBot = botId;
    let bot = this.botMap.get(botId);
    if (bot != null) {
      this.baseCurrency = bot.baseCurrency;
      this.currentBalance = this.botMap.get(botId).currentBalance;
      this.startBalance = bot.startBalance;

      this.fxDollar = bot.fxDollar;
      this.startBalanceDollar = bot.startBalanceDollar;
      this.currentBalanceDollar = bot.currentBalanceDollar;
      this.totalBalanceDollar = bot.totalBalanceDollar;
      this.totalBaseCurrencyValue = bot.totalBaseCurrencyValue
      this.totalBotResult = bot.result;

      this.selectedExchange = bot.exchange

      this.load();

      // console.log(bot)
    }
  };

  addToChart = watcher => {
    this.rootStore.marketSelectionStore.addSeries(
      watcher.exchange,
      watcher.market
    );
    this.rootStore.marketSelectionStore.load();
  };

  loadToChart = position => {
    let singnals = [];
    let signal = {
      type: "buy",
      date: moment(position.buyDate).toDate()
    };

    singnals.push(signal);

    this.rootStore.chartStore.updateSignales(singnals);

    let exchange = this.botMap.get(this.selectedBot).exchange;
    this.rootStore.marketSelectionStore.select(exchange, position.market);
  };

  sellPosition = position => {
    this.log.debug("selling position ", position);

    let url = this.rootStore.remoteApiUrl + "/position/sell";

    let config = {
      params: {
        botId: this.selectedBot,
        positionId: position.id
      },
      ...this.rootStore.userStore.getHeaderConfig()
    };

    position.sellInPogress = true;

    axios
      .get(url, config)
      .then(response => {
        if (response.data.success) {
          //reload position list
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

  applySettings = (position, settings) => {
    console.log(position, settings);

    let url = this.rootStore.remoteApiUrl + "/position/applySettings";

    let config = {
      params: {
        botId: this.selectedBot,
        positionId: position.id
      },
      ...this.rootStore.userStore.getHeaderConfig()
    };

    axios
      .post(url, settings, config)
      .then(response => {
        console.log(response);

        if (response.data.success) {
          message.success("Settings saved for position " + position.market);
        } else {
          message.error(response.data.message);
        }
      })
      .catch(function(error) {
        console.log(error);
      });
  };

  deletePosition = position => {
    this.log.debug("deleteing position ", position);

    let url = this.rootStore.remoteApiUrl + "/position/delete";

    let config = {
      params: {
        botId: this.selectedBot,
        positionId: position.id
      },
      ...this.rootStore.userStore.getHeaderConfig()
    };

    axios
      .get(url, config)
      .then(response => {
        if (response.data.success) {
          //reload position list
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

  syncPosition = position => {
    this.log.debug("sync position ", position);

    let url = this.rootStore.remoteApiUrl + "/position/syncPosition";

    let config = {
      params: {
        botId: this.selectedBot,
        positionId: position.id
      },
      ...this.rootStore.userStore.getHeaderConfig()
    };

    axios
      .get(url, config)
      .then(response => {
        if (response.data.success) {
          //reload position list
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

  deletePosition = position => {
    this.log.debug("deleteing position ", position);

    let url = this.rootStore.remoteApiUrl + "/position/deletePos";

    let config = {
      params: {
        botId: this.selectedBot,
        positionId: position.id
      },
      ...this.rootStore.userStore.getHeaderConfig()
    };

    axios
      .get(url, config)
      .then(response => {
        if (response.data.success) {
          //reload position list
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

  importFromExchange = () => {
    let url = this.rootStore.remoteApiUrl + "/position/importFromExchange";

    let config = {
      params: {
        botId: this.selectedBot
      },
      ...this.rootStore.userStore.getHeaderConfig()
    };

    axios
      .get(url, config)
      .then(response => {
        if (response.data.success) {
          //reload position list
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

  syncBalance = () => {
    let url = this.rootStore.remoteApiUrl + "/bot/syncBalance";

    let config = {
      params: {
        botId: this.selectedBot
      },
      ...this.rootStore.userStore.getHeaderConfig()
    };

    axios
      .get(url, config)
      .then(response => {
        if (response.data.success) {

            let cb = () => {
                this.onBotSelected(this.selectedBot);
            }
           
            this.loadBotList(cb);
        } else {
          // error
          console.info(response.data.message);
        }
      })
      .catch(function(error) {
        console.log(error);
      });
  };

  load = () => {
    this.log.debug("loading position list");
    let url = this.rootStore.remoteApiUrl + "/position/list";

    let config = {
      params: {
        botId: this.selectedBot
      },
      ...this.rootStore.userStore.getHeaderConfig()
    };

    axios
      .get(url, config)
      .then(response => {
        if (response.data.success) {
          //this.log.debug("Loaded positions", {...response.data.data})

          this.positions = response.data.data;
        } else {
          // error
          console.info(response.data.message);
        }
      })
      .catch(function(error) {
        console.log(error);
      });
  };

  loadExchanges = () => {
    let url = this.rootStore.remoteApiUrl + "/exchange/";

    axios
      .get(url, this.rootStore.userStore.getHeaderConfig())
      .then(response => {
        if (response.data.success) {
          this.log.debug("Loaded Exchanges", { ...response.data.data });
          // console.log( response.data.data)

          let newExchangeList = [];
          response.data.data.forEach(exchange => {
            // console.log( exchange)
            newExchangeList.push(exchange.name.toLowerCase());

            let newAssetList = exchange.markets.map(market => {
              return market.asset;
            });

            this.assetMap.set(exchange.name.toLowerCase(), newAssetList);
            //     this.selectedAssetBySeries.set(exchange.name, newAssetList[0])
          });

          this.exchangeList = newExchangeList;
        } else {
          // error
          console.info(response.data.message);
        }
      })
      .catch(function(error) {
        console.log(error);
      });
  };

  openNewPosition = (positionSettings) => {
    console.log(positionSettings);

    let url = this.rootStore.remoteApiUrl + "/position/newPosition";

    let config = {
      params: {
        exchange: this.selectedExchange,
        market: this.getSelectedBot().config.baseCurrency + "-" + this.selectedAsset
      },
      ...this.rootStore.userStore.getHeaderConfig()
    };

    axios
      .post(url, positionSettings, config)
      .then(response => {
        console.log(response);

        if (response.data.success) {
          message.success("Settings saved for position " + position.market);
        } else {
          message.error(response.data.message);
        }
      })
      .catch(function(error) {
        console.log(error);
      });
  };

}

export default MarketWatcherStore;
