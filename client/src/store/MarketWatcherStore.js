import axios from "axios";
import { observable, computed, action } from "mobx";

import logger from "../logger"


class MarketWatcherStore {

    log = logger.getLogger('MarketWatcherStore')

    @observable watcherList = []

    @observable exchangeList = []

    @observable
    assetMap = new Map()

    @observable selectedExchange

    @observable selectedAsset

    constructor(rootStore) {
        this.log.debug("New MarketWatcherStore!");
        this.rootStore = rootStore
    }

    init = () =>  {

        this.log.debug("init MarketWatcherStore -> loading available exchanges")
        
        let url = this.rootStore.remoteApiUrl + "/exchange/";
  
        axios
        .get(url, this.rootStore.userStore.getHeaderConfig())
        .then(response => {
          if (response.data.success) {
                this.log.debug("Loaded Exchanges", {...response.data.data})
             // console.log( response.data.data)
  
              let newExchangeList = []
              response.data.data.forEach((exchange) => {
                   // console.log( exchange)
                   newExchangeList.push(exchange.name)
                   
                   let newAssetList = exchange.markets.map((market) => {
                         return market.currency + '-' + market.asset
                   })
              
                   this.assetMap.set(exchange.name, newAssetList)
              //     this.selectedAssetBySeries.set(exchange.name, newAssetList[0])
              });
              
              this.exchangeList = newExchangeList
  
    
  
          } else {
            // error
            console.info(response.data.message)
          }
        })
        .catch(function(error) {
          console.log(error);
        });
    }

    addToChart = (watcher) => {
        this.rootStore.marketSelectionStore.addSeries(watcher.exchange, watcher.market)
        this.rootStore.marketSelectionStore.load()
    }

    loadToChart = (watcher) => {
        this.rootStore.marketSelectionStore.select(watcher.exchange, watcher.market)
    }

    stop = (watcher) => {

        this.log.debug("stopping watcher ", watcher.exchange, " ", watcher.market)

        let url = this.rootStore.remoteApiUrl + "/marketWatcher/stop";

        let config = {
            params: {
                watcherId: watcher.id
            },
            ...this.rootStore.userStore.getHeaderConfig()
        }

        axios
        .get(url, config)
        .then(response => {
            if (response.data.success) {
                this.log.debug("Wacther stopped Markets", {...response.data.data})
                
                //reload wacther list
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

    addWatcher = () => {
        let watcher = {exchange: this.selectedExchange, market: this.selectedAsset }
        this.start( watcher )
        this.loadToChart( watcher )
    }

    start = (watcher) => {

        this.log.debug("starting watcher for ", watcher.exchange, " ", watcher.market)

        let url = this.rootStore.remoteApiUrl + "/marketWatcher/start";

        let config = {
            params: {
                exchange: watcher.exchange,
                market: watcher.market
            },
            ...this.rootStore.userStore.getHeaderConfig()
        }

        axios
        .get(url, config)
        .then(response => {
            if (response.data.success) {
                this.log.debug("Wacther started Markets", {...response.data.data})
                
                //reload wacther list
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

    load = () => {

      this.log.debug("loading watcher list");
      let url = this.rootStore.remoteApiUrl + "/marketWatcher/list";

      axios
      .get(url, this.rootStore.userStore.getHeaderConfig())
      .then(response => {
        if (response.data.success) {
             this.log.debug("Loaded watcher", {...response.data.data})
             
             this.watcherList = response.data.data
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