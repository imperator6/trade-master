import axios from "axios";
import { observable, computed, action } from "mobx";

import logger from "../logger"

import moment from "moment";


class MarketWatcherStore {

    log = logger.getLogger('MarketWatcherStore')

    @observable positions = []

    @observable botList = []

    @observable baseCurrency = "BTC"
    @observable startBalance = 0
    @observable currentBalance = 0

    @observable fxDollar = 0
    @observable startBalanceDollar = 0
    @observable currentBalanceDollar = 0
    @observable totalBalanceDollar = 0
    @observable totalBotResult = 0

    @observable
    botMap = new Map()

    @observable selectedBot

    @observable selectedAsset

    constructor(rootStore) {
        this.log.debug("New MarketWatcherStore!");
        this.rootStore = rootStore
    }

    init = () =>  {

        this.log.debug("init MarketWatcherStore -> loading available exchanges")
        
        let url = this.rootStore.remoteApiUrl + "/bot/";
  
        axios
        .get(url, this.rootStore.userStore.getHeaderConfig())
        .then(response => {
          if (response.data.success) {
                this.log.debug("Loaded Bots", {...response.data.data})
             // console.log( response.data.data)
  
              let newBotList = []
              response.data.data.forEach((bot) => {
                   // console.log( exchange)
                   newBotList.push(bot.id + "_" + bot.exchange + "_" + bot.baseCurrency)
                   this.botMap.set(bot.id, bot)
                   //newBotList.push(bot)
              });
              
              this.botList = newBotList
  
              this.selectedBot = newBotList[0].split("_")[0]

              this.onBotSelected()

              this.load();
  
          } else {
            // error
            console.info(response.data.message)
          }
        })
        .catch(function(error) {
          console.log(error);
        });
    }
    
    @action
    onBotSelected =() => {

        let bot = this.botMap.get(this.selectedBot)
        if(bot != null) {
            this.baseCurrency = bot.baseCurrency
            this.currentBalance = this.botMap.get(this.selectedBot).currentBalance
            this.startBalance = bot.startBalance

            this.fxDollar = bot.fxDollar
            this.startBalanceDollar = bot.startBalanceDollar
            this.currentBalanceDollar = bot.currentBalanceDollar
            this.totalBalanceDollar = bot.totalBalanceDollar
            this.totalBotResult = bot.result

           // console.log(bot)
        }
        
    }

    addToChart = (watcher) => {
        this.rootStore.marketSelectionStore.addSeries(watcher.exchange, watcher.market)
        this.rootStore.marketSelectionStore.load()
    }

    loadToChart = (position) => {
        

        let singnals = []
        let signal = {
            type: "buy",
            date: moment(position.buyDate).toDate()
        }

        singnals.push(signal)


        this.rootStore.chartStore.updateSignales(singnals)

        let exchange = this.botMap.get(this.selectedBot).exchange
        this.rootStore.marketSelectionStore.select(exchange, position.market)
    }

    sellPosition = (position) => {

        this.log.debug("selling position ", position)

        let url = this.rootStore.remoteApiUrl + "/position/sell";

        let config = {
            params: {
                botId: this.selectedBot,
                positionId: position.id
            },
            ...this.rootStore.userStore.getHeaderConfig()
        }

        position.sellInPogress = true

        axios
        .get(url, config)
        .then(response => {
            if (response.data.success) {
                //reload position list
                this.load()
            } else {
            // error
                console.info(response.data.message)
            }
        })
        .catch(function(error) {
            console.log(error);
        });
    }

    deletePosition = (position) => {

        this.log.debug("deleteing position ", position)

        let url = this.rootStore.remoteApiUrl + "/position/delete";

        let config = {
            params: {
                botId: this.selectedBot,
                positionId: position.id
            },
            ...this.rootStore.userStore.getHeaderConfig()
        }

        axios
        .get(url, config)
        .then(response => {
            if (response.data.success) {
                //reload position list
                this.load()
            } else {
            // error
                console.info(response.data.message)
            }
        })
        .catch(function(error) {
            console.log(error);
        });
    }

    syncPosition = (position) => {

        this.log.debug("sync position ", position)

        let url = this.rootStore.remoteApiUrl + "/position/syncPosition";

        let config = {
            params: {
                botId: this.selectedBot,
                positionId: position.id
            },
            ...this.rootStore.userStore.getHeaderConfig()
        }

        axios
        .get(url, config)
        .then(response => {
            if (response.data.success) {
                //reload position list
                this.load()
            } else {
            // error
                console.info(response.data.message)
            }
        })
        .catch(function(error) {
            console.log(error);
        });
    }

    deletePosition = (position) => {

        this.log.debug("deleteing position ", position)

        let url = this.rootStore.remoteApiUrl + "/position/deletePos";

        let config = {
            params: {
                botId: this.selectedBot,
                positionId: position.id
            },
            ...this.rootStore.userStore.getHeaderConfig()
        }

        axios
        .get(url, config)
        .then(response => {
            if (response.data.success) {
                //reload position list
                this.load()
            } else {
            // error
                console.info(response.data.message)
            }
        })
        .catch(function(error) {
            console.log(error);
        });
    }

    importFromExchange = () => {

        let url = this.rootStore.remoteApiUrl + "/position/importFromExchange";

        let config = {
            params: {
                botId: this.selectedBot
            },
            ...this.rootStore.userStore.getHeaderConfig()
        }

        axios
        .get(url, config)
        .then(response => {
            if (response.data.success) {
                //reload position list
                this.load()
            } else {
            // error
                console.info(response.data.message)
            }
        })
        .catch(function(error) {
            console.log(error);
        });
    }

    syncBalance = () => {

        let url = this.rootStore.remoteApiUrl + "/bot/syncBalance";

        let config = {
            params: {
                botId: this.selectedBot
            },
            ...this.rootStore.userStore.getHeaderConfig()
        }

        axios
        .get(url, config)
        .then(response => {
            if (response.data.success) {
                //reload position list
                let bot = data.result
                this.botMap.set(bot.id, bot)
                this.onBotSelected()

                console.log(bot)
            } else {
            // error
                console.info(response.data.message)
            }
        })
        .catch(function(error) {
            console.log(error);
        });
    }


    load = () => {

      this.log.debug("loading position list");
      let url = this.rootStore.remoteApiUrl + "/position/list";

      let config = {
        params: {
            botId: this.selectedBot
        },
        ...this.rootStore.userStore.getHeaderConfig()
    } 

      axios
      .get(url, config)
      .then(response => {
        if (response.data.success) {
             //this.log.debug("Loaded positions", {...response.data.data})
             
             this.positions = response.data.data
        } else {
          // error
          console.info(response.data.message)
        }
      })
      .catch(function(error) {
        console.log(error);
      });
    }

}


export default MarketWatcherStore;