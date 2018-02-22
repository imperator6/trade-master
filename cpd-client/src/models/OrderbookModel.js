import { observable, computed, action, toJS } from "mobx";
import axios from "axios";
import logger from "../logger";
var _ = require("lodash");

export const EMPTY_DATA = { broker: null, quantity: null, price: null, active: false};


export default class OrderbookModel {
  
  log = logger.getLogger("OrderbookModel")

  @observable orderbookMap = new Map()

  @observable dataMap = new Map()

  @observable keyList = []

  @observable initDone = false

  priceSeq = null

  //priceFiFo = [];

  priceBySeqMap = new Map();

  //priceParkingBox = new Map(); // in case we receive a higher seq as expected

  priceSeqErrorCount = 0;

  isFirstPrice = true // first price via websocket stream!

  firstPriceExecution = true // first execution of collected prices 

  constructor(rootStore) {
    this.log.debug("New OrderbookModel!");
    this.rootStore = rootStore;
  }

  @computed
  get totalCells() {
    return this.products.length * this.periodGroups.length;
  }

  @action
  calculateOrderbook(config) {
    this.log.info("Calculation the new orderbook layout");
    this.orderbookMap.clear();

    let productNames = config.products.map(p => {
      return p;
    });
    let periodNames = config.periodGroups.map(pg => {
      let periods = pg.periods.map(p => p);
      return periods;
    });

    // join al array to one big
    periodNames = _.flatten(periodNames);

    let f = (a, b) => [].concat(...a.map(a => b.map(b => [].concat(a, b))));
    let cartesian = (a, b, ...c) => (b ? cartesian(f(a, b), ...c) : a);

    // all combinations as key
    let keyMap = cartesian(productNames, periodNames).map(
      o => o[0] + "_" + o[1]
    );

    this.keyList = keyMap;

    keyMap.forEach(key => {
      this.orderbookMap.set(key, this.createPlaceholder());
    });
  }

  createPlaceholder() {
    let bid = [] //observable(new Map());
    let ask = [] //observable(new Map());

     for (let i = 0; i < this.rootStore.cpdConfigStore.config.maxOrderbookEntries; i++) {
      bid.push( EMPTY_DATA)
      ask.push( EMPTY_DATA) 
     }

    //let ph = observable({ bid: bid, ask: ask });
     
  //  let ph = { bid:  observable(bid), ask: observable(ask) }

    let ph = observable({ bid: bid, ask: ask });
    return ph
  }

  @action
  updateOrderbook(key, next) {
    //let old = this.orderbookMap.get(key);

    let changedKeys = [];

    if (next && next.ask) {
      this.updatePriceData(key + "_ask_", next.ask, changedKeys);
    }

    if (next && next.bid)
      this.updatePriceData(key + "_bid_", next.bid, changedKeys);

    // blink unmark as inactive after x seconds!
    if (changedKeys.length > 0) {
      setTimeout(() => {
        changedKeys.forEach(key => {
          let b = this.dataMap.get(key);
          b.active = false;
        });
      }, 4000);
    }
  }

  updatePriceData(key, dataArray, changedKeys) {
    if (dataArray) {
      dataArray.forEach((b, i) => {
        let dataKey = "" + key + i;
        let old = this.dataMap.get(dataKey);

        if (old) {
          let diff = this.difference(b, old);

          if (!_.isEmpty(diff)) {
            b.active = true;
            this.dataMap.set(dataKey, b);
            changedKeys.push(dataKey);
          }
        } else {
          this.dataMap.set(dataKey, b);
        }
      });
    }
  }

  difference(object, base) {
    function changes(object, base) {
      return _.transform(object, function(result, value, key) {
        if (!_.isEqual(value, base[key])) {
          result[key] =
            _.isObject(value) && _.isObject(base[key])
              ? changes(value, base[key])
              : value;
        }
      });
    }
    return changes(object, base);
  }

  @action
  loadLatestOrderbook = (loadingDoneActionCb) => {
    let url = this.rootStore.remoteApiUrl + "/orderbook/latest";
    let oderbookIds = Array.from(this.orderbookMap.keys());

     let config = {
      params: {
        maxOrderbookEntries: this.rootStore.cpdConfigStore.config.maxOrderbookEntries
      },
     // ...this.rootStore.userStore.getHeaderConfig()
    };

    axios
      .post(url, oderbookIds, config)
      .then(response => {
        let orderbookList = response.data.data;

        this.fillOrderbooks(orderbookList, loadingDoneActionCb)
      })
      .catch(function(error) {
        console.log(error);
      });
  };

  @action.bound
  fillOrderbooks = (orderbookList, callback) => {
    let maxPriceSeq = 0;

    orderbookList.forEach(ob => {
      
      if (ob.priceSeqNum)
         maxPriceSeq = Math.max(maxPriceSeq, ob.priceSeqNum)
         this.updateOrderbook(ob._id, ob.orderbook)
    }); 

    if(callback) callback(maxPriceSeq)
  }

  @action
  restartStream = (config) => {
      this.calculateOrderbook(config)
      this.startPriceStream()
  }

  @action
  startPriceStream = () => {
    let channel = "/topic/orderbook";

    this.rootStore.stompStore.unsubscribe(channel)

    this.initDone = false
    this.priceSeq = null
    this.isFirstPrice = true
    this.firstInitDone = true
    this.priceBySeqMap.clear()

    let orderbookLoadedCallback = (maxPriceSeq) => {
      this.log.info("Orderbooks loaded. Max price seq is " + maxPriceSeq)
      this.priceSeq = maxPriceSeq;
      this.initDone = true;
    }

    let loadObCallback = () => {
      this.log.info("Subscription for channel " + channel + " is done. Lets load the initial orderbook!")
      setTimeout(() => {
        this.loadLatestOrderbook(orderbookLoadedCallback)
      }, 500) 
    }

    
    this.rootStore.stompStore.subscribe(channel, data => {
      let orderbookList = JSON.parse(data.body);
      //this.applyPrice(price);

      orderbookList.forEach((ob) => {
        this.updateOrderbook(ob._id, ob.orderbook)
      }) 

    }, loadObCallback)

  };
}
