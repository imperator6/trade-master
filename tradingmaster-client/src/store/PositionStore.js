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
  @observable dollarMode = 'dollar'; // otherwise BTC

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

  @observable positionMap = new Map();

  @observable selectedExchange;

  @observable selectedAsset;

  @observable selectedPosition;

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
    this.loadExchanges();
  };

  switchDollarMode = (newMode) => {
    this.dollarMode = newMode
    console.info(newMode)
  }

  /*selectPosition = (pos) => {

    let bot = this.getSelectedBot()

    if(!pos.settings)
      pos.settings = {}

    if(!pos.settings.holdPosition) {
      pos.settings.holdPosition = false
    }

    if(!pos.settings.traceClosedPosition) {
      pos.settings.traceClosedPosition = false
    }

    if(!pos.settings.pingPong) {
      pos.settings.pingPong = false
    }

    if(!pos.settings.buyWhen) {
      // init if not exist
      pos.settings.buyWhen = { enabled: false, quantity: 0, spend: 0, minPrice: 0, maxPrice: 0, timeoutHours: 36}
    }

    if(pos.closed) {
      if(pos.settings.buyWhen.quantity === 0) {
        pos.settings.buyWhen.quantity = pos.amount
      }

      if(pos.settings.buyWhen.minPrice === 0) {
        pos.settings.buyWhen.minPrice = pos.sellRate
      }

      if(pos.settings.buyWhen.maxPrice === 0) {
        pos.settings.buyWhen.maxPrice = pos.sellRate
      }

    }
    
    if(!pos.settings.takeProfit) {
       // init if not exist
        pos.settings.takeProfit = {enabled: false, value: 20}
    }

    if(pos.settings.takeProfit && !pos.settings.takeProfit.enabled) {
       // overwrite with bot settings 
      pos.settings.takeProfit = {enabled: false, value: bot.config.takeProfit.value}
  }

    if(!pos.settings.stopLoss) {
      // init if not exsist
      pos.settings.stopLoss = {enabled: false, value: -10}
    }

    if(pos.settings.stopLoss && !pos.settings.stopLoss.enabled) {
       // overwrite with bot settings 
      pos.settings.stopLoss = {enabled: false, value: bot.config.stopLoss.value }
    }

    if(!pos.settings.trailingStopLoss) {
      // init if not exsist
      pos.settings.trailingStopLoss = {enabled: false, value: 5, startAt: 20, keepAtLeastForHours: 0}
    }

    if(pos.settings.trailingStopLoss && !pos.settings.trailingStopLoss.enabled) {
      // overwrite with bot settings 
      pos.settings.trailingStopLoss = {enabled: false, value: bot.config.trailingStopLoss.value, startAt: bot.config.trailingStopLoss.startAt, keepAtLeastForHours: bot.config.trailingStopLoss.keepAtLeastForHours}
    }



    this.selectedPosition = pos
  } */

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
              bot.id + "_" + bot.exchange + "_" + bot.baseCurrency + "_" + bot.backtest
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
      this.startBalance = bot.startBalance

      this.fxDollar = bot.fxDollar
      this.startBalanceDollar = bot.startBalanceDollar || 0
      this.currentBalanceDollar = bot.currentBalanceDollar || 0
      this.totalBalanceDollar = bot.totalBalanceDollar || 0
      this.totalBaseCurrencyValue = bot.totalBaseCurrencyValue || 0
      this.totalBotResult = bot.result || 0

      this.selectedExchange = bot.exchange

      this.load();

      // load a chart 
      if(this.rootStore.marketSelectionStore.getSelectedExchange(0) == null)
        this.rootStore.marketSelectionStore.select('Binance', 'USDT-BTC')


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

  savePosition = (position) => {
    console.log(position);

    let url = this.rootStore.remoteApiUrl + "/position/save";

    let config = {
      params: {
       // botId: this.selectedBot
      },
      ...this.rootStore.userStore.getHeaderConfig()
    }

    axios
      .post(url, position, config)
      .then(response => {
        if (response.data.success) {
          message.success("Position Comment saved! " + position.market);
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

  clonePosition = position => {
    this.log.debug("cloning position ", position);

    let url = this.rootStore.remoteApiUrl + "/position/clone";

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

  @action
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
      .then(this.positionReceived)
      .catch(function(error) {
        console.log(error);
      });
  };

  @action.bound
  positionReceived(response) {
    
      if (response.data.success) {
        this.log.debug("Loaded positions", {...response.data.data})
        this.positionMap.clear()

        response.data.data.forEach( (p) => {
          this.positionMap.set(p.id, p)
        })
        this.positions = response.data.data;
        this.log.debug("Loaded map", this.positionMap)

      } else {
        // error
        console.info(response.data.message);
      }
    

  }

  loadExchanges = () => {
    let url = this.rootStore.remoteApiUrl + "/exchange/";

    console.log("position store: loading exchanges")

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
              //return market.asset;
              return market.currency + '-' + market.asset
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

  getAssetList = () => {
    if(this.selectedExchange != null) {
      let assetList = this.assetMap.get(this.selectedExchange.toLowerCase())
      console.log(this.assetMap)
      console.log(assetList)
      return assetList
    }

    return []

  }

  getChartLink = (market) => {
    if(market == null) {
      return "#"
    }
    let bot = this.getSelectedBot();
    let chartLink = bot.config.chartLink;

    if (chartLink) {
      //let baseCurrency = bot.config.baseCurrency;
      let split = market.split("-")
      chartLink = chartLink.replace("$market", market);
      chartLink = chartLink.replace("$asset", split[1]);
      chartLink = chartLink.replace("$baseCurrency", split[0]);
    }
    return chartLink
  }

  getMarket = () => {
      //return this.getSelectedBot().config.baseCurrency + "-" + this.selectedAsset
      return this.selectedAsset
  }

  openNewPosition = (exchange, market, positionSettings) => {
    console.log(positionSettings);

    let url = this.rootStore.remoteApiUrl + "/position/newPosition";

    positionSettings.buyWhen.enabled = true

    let config = {
      params: {
        botId: this.getSelectedBot().id,
        market: market
      },
      ...this.rootStore.userStore.getHeaderConfig()
    };

    axios
      .post(url, positionSettings, config)
      .then(response => {
      
        if (response.data.success) {
          message.success("Position for market " + market + " created!");
          this.load()
        } else {
          message.error(response.data.message);
        }
      })
      .catch(function(error) {
        console.log(error);
      });
  };

  loadTicker = (exchange, market, callback) => {
    let url = this.rootStore.remoteApiUrl + "/position/ticker";

    let config = {
      params: {
        exchangeName: exchange,
        market: market
      },
      ...this.rootStore.userStore.getHeaderConfig()
    };

    axios
      .get(url, config)
      .then(response => {
        if (response.data.success) {
        
          if(callback) {
             callback(response.data.data)
          }
  
        } else {
          // error
          console.info(response.data.message);
        }
      })
      .catch(function(error) {
        console.log(error);
      });
  }

  updateDollarFx = (newFxDollarCandle) => {
      this.fxDollar = newFxDollarCandle.close
  }

  updatePosition = (newPosition) => {
    this.log.info("New Position update!", newPosition)
    this.positionMap.set(newPosition.id, newPosition)
}

  formatNumber(value, decimals, satoshis) {
    if (value == null || value === 0.0) return 0;

    if(Math.abs(value) >= 1) {
      if(decimals) return value.toFixed(decimals)

      if(Math.abs(value) > 1000) return value.toFixed(0)

      return value.toFixed(2)
     } else {
      if(satoshis) return value.toFixed(satoshis)
      return value.toFixed(8)
     }
  }

}

export default MarketWatcherStore;
