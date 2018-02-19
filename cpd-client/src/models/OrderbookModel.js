import { observable, computed, action } from "mobx";
import logger from "../logger";
var _ = require("lodash");

export const EMPTY_DATA = { broker: null, quantity: null, price: null };

export const DEFAULT_CONFIG = {
  products: [],
  periodGroups: [],
  maxOrderbookEntries: 40
};

export default class OrderbookModel {
  log = logger.getLogger("OrderbookModel");

  @observable orderbookMap = new Map();

  @observable dataMap = new Map();

  @observable keyList = [];

  @observable config = DEFAULT_CONFIG;

  constructor(rootStore) {
    this.log.debug("New OrderbookModel!");
    this.rootStore = rootStore;

    setTimeout(() => {
      this.startPriceStream();
    }, 1000);
  }

  @computed
  get totalCells() {
    return this.products.length * this.periodGroups.length;
  }

  @action
  setConfig(newConfig) {
    let finalConfig = { ...DEFAULT_CONFIG, ...newConfig };
    console.log(finalConfig);
    this.config = finalConfig;
  }

  @action
  calculateOrderbook() {
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
    let bid = [];
    let ask = [];
    for (let i = 0; i < this.config.maxOrderbookEntries; i++) {
      bid.push(EMPTY_DATA);
      ask.push(EMPTY_DATA);
    }

    let ph = observable({ bid: bid, ask: ask });
    return ph;
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
  updateOrderbookWithPrice(key, price) {
    if (!this.orderbookMap.get(key)) {
      // not configured... skip it
      return;
    }

    let bidAskKey = key;
    let action = price.Action;
    let bidOrAsk = price.BuySell;

    if (bidOrAsk === "Bid") {
      bidAskKey = key + "_bid_";
    } else if (bidOrAsk === "Offer") {
      bidAskKey = key + "_ask_";
    } else {
      this.log.error(
        `Skipping ${key} Action ${action} -> Unknown 'BuySell' flag '${
          price.BuySell
        }'`
      );
      return;
    }

    // We are in the game...
    if ("Remove" === action) {
      //bidOrAsk.remove(priceId)
    } else if ("Insert" === action) {
      //bidOrAsk.put(priceId, extractBidOrAsk(data))
    } else if ("Update" === action) {
      //Integer oldPriceId = data.OrigPriceID as Integer
      //bidOrAsk.remove(oldPriceId)
      //bidOrAsk.put(priceId, extractBidOrAsk(data))
    } else {
      this.log.error(
        `Skipping ${key} Action ${action} -> Unknown 'Action'  ${action}.`
      );
      return;
    }
  }

  startPriceStream = () => {
    let channel = "/topic/price";

    this.rootStore.stompStore.subscribe(channel, data => {
      let price = JSON.parse(data.body);

      this.updateOrderbookWithPrice(price._id, price);
    });
  };
}
