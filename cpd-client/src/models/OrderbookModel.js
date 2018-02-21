import { observable, computed, action, toJS } from "mobx";
import axios from "axios";
import logger from "../logger";
var _ = require("lodash");

export const EMPTY_DATA = { broker: null, quantity: null, price: null };

export const DEFAULT_CONFIG = {
  products: [],
  periodGroups: [],
  maxOrderbookEntries: 25
};

export default class OrderbookModel {
  
  log = logger.getLogger("OrderbookModel");

  @observable orderbookMap = new Map();

  @observable dataMap = new Map();

  @observable keyList = [];

  @observable config = DEFAULT_CONFIG;

  @observable initDone = false;

  priceSeq = null;

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
  setConfig(newConfig) {
    this.log.info("Set a new config!");
    let finalConfig = { ...DEFAULT_CONFIG, ...newConfig };
    this.config = finalConfig;
    this.calculateOrderbook()
    this.startPriceStream();
  }

  @action
  calculateOrderbook() {
    this.log.info("Calculation the new orderbook layout");
    this.orderbookMap.clear();

    let productNames = this.config.products.map(p => {
      return p;
    });
    let periodNames = this.config.periodGroups.map(pg => {
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
    let bid = observable(new Map());
    let ask = observable(new Map());

    //let ph = observable({ bid: bid, ask: ask });
    let ph = { bid: bid, ask: ask };
    return ph;
  }

  @action
  loadLatestOrderbook = (loadingDoneActionCb) => {
    let url = this.rootStore.remoteApiUrl + "/orderbook/latest";
    let oderbookIds = Array.from(this.orderbookMap.keys());

     let config = {
      params: {
        maxOrderbookEntries: this.config.maxOrderbookEntries
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
        maxPriceSeq = Math.max(maxPriceSeq, ob.priceSeqNum);
        //let orderbook = this.orderbookMap.get(ob._id)

      if (ob.orderbook.ask) {
        ob.orderbook.ask.forEach(ask => {
          // create a dummy insert object !
          let insert = {
            Broker: ask.broker,
            Volume: ask.quantity,
            Price: ask.price,
            Action: "Insert",
            BuySell: "Offer",
            PriceID: ask.id
          };
          this.updateOrderbookWithPrice(ob._id, insert);
        });
      }

      if (ob.orderbook.bid) {
        ob.orderbook.bid.forEach(bid => {
          // create a dummy insert object !
          let insert = {
            Broker: bid.broker,
            Volume: bid.quantity,
            Price: bid.price,
            Action: "Insert",
            BuySell: "Bid",
            PriceID: bid.id
          };
          this.updateOrderbookWithPrice(ob._id, insert);
        });
      }
    });

    if(callback) callback(maxPriceSeq)
  }

  @action
  updateOrderbookWithPrice(key, price) {
    let orderbook = this.orderbookMap.get(key);

    if (!orderbook) {
      // not configured... skip it
      return;
    }

    let bidAskKey = key;
    let action = price.Action;
    let bidOrAsk = price.BuySell;

    let bidOrAskMap = null;

    if (bidOrAsk === "Bid") {
      bidOrAskMap = orderbook.bid;
    } else if (bidOrAsk === "Offer") {
      bidOrAskMap = orderbook.ask;
    } else {
      this.log.error(
        `Skipping ${key} Action ${action} -> Unknown 'BuySell' flag '${
          price.BuySell
        }'`
      );
      return;
    }

    let priceId = price.PriceID;
    let activeBidArAsk = null;
    let active = this.initDone

    // We are in the game...
    if ("Remove" === action) {
      if (bidOrAskMap.has(priceId)) {
        bidOrAskMap.delete(priceId);
        activeBidArAsk = bidOrAskMap.values().next(); // first element
      }

      //bidOrAsk.remove(priceId)
    } else if ("Insert" === action) {
      activeBidArAsk = {
        broker: price.Broker,
        quantity: price.Volume,
        price: price.Price,
        active: active
      };
      bidOrAskMap.set(priceId, activeBidArAsk);
    } else if ("Update" === action) {
      //Integer oldPriceId = data.OrigPriceID as Integer
      //bidOrAsk.remove(oldPriceId)
      activeBidArAsk = {
        broker: price.Broker,
        quantity: price.Volume,
        price: price.Price,
        active: active
      };
      let oldPriceId = price.OrigPriceID;
      bidOrAskMap.delete(oldPriceId);
      bidOrAskMap.set(priceId, activeBidArAsk);
    } else {
      this.log.error(
        `Skipping ${key} Action ${action} -> Unknown 'Action'  ${action}.`
      );
      return;
    }

    if (activeBidArAsk && this.initDone) {
      setTimeout(() => {
        bidOrAskMap.set(priceId, { ...activeBidArAsk, active: false });
      }, 4000);
    }
  }

  isValidePriceSequenze = price => {
    let currentSeq = price.priceSeqNum;
    let expectedSeq = this.priceSeq + 1;

    if (currentSeq !== expectedSeq) {
      console.error(
        `Error in price Sequenze: expected: ${expectedSeq} current: ${currentSeq}`
      );

      return false;
    }
  };

 

  applyPrice(nextPrice) {
    
    this.priceBySeqMap.set(nextPrice.priceSeqNum, nextPrice);

    if(this.isFirstPrice) {
      this.log.info("Frist price received with seq id: " + nextPrice.priceSeqNum )
      this.isFirstPrice = false
    }

    if (this.initDone) {
      let seqIds = Array.from(this.priceBySeqMap.keys());
      let minSeq = Math.min(...seqIds);
      let maxSeq = Math.max(...seqIds);

      let execute = nextPrice.priceSeqNum == this.priceSeq + 1;

      if(this.firstPriceExecution) {
        
        // remove all actions wich already part of the orderbook
        let end = this.priceSeq
        for (let i = minSeq; i <= end; i++) {
          this.priceBySeqMap.delete(i);
        }

        minSeq = Math.max(minSeq, this.priceSeq+1)

        this.log.info("Frist price to execute after initDone: " + minSeq )

        this.firstPriceExecution = false
        execute = true
      }

      maxSeq = Math.max(minSeq, maxSeq);

      if (execute) {

        for (let i = minSeq; i <= maxSeq; i++) {

          this.log.debug("Executing price seq  " + i + " range from  " + minSeq + " to: " + maxSeq)

          let price = this.priceBySeqMap.get(i);

          if (price != null) {
            let isValidPrice = this.isValidePriceSequenze(price);
            this.updateOrderbookWithPrice(price._id, price);
            this.priceSeq++;
            this.priceBySeqMap.delete(i);
          } else {
            console.warn("No price for for seq id " + i);
            this.priceSeqErrorCount++;
            i = maxSeq;
          }
        }
      } else {
        this.log.warn("Skipping price seq range from  " + minSeq + " to: " + maxSeq)
      }
    }
  }

  @action
  startPriceStream = () => {
    let channel = "/topic/price";

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
      }, 2000) 
    }

    this.rootStore.stompStore.unsubscribe(channel)
    this.rootStore.stompStore.subscribe(channel, data => {
      let price = JSON.parse(data.body);
      this.applyPrice(price);
    }, loadObCallback)

  };
}
