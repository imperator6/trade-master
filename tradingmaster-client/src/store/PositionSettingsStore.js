import axios from "axios";
import { observable, computed, action, toJS } from "mobx";

import logger from "../logger";

import moment from "moment";

import { message } from "antd";

class PositionSettingsStore {
  log = logger.getLogger("PositionSettingsStore");

  DEFAULT_NEW_POSITION = {
      settings: {
        holdPosition: false,
        traceClosedPosition: false,
        rebuy: { enabled: false, value: -4 },
         buyWhen: {
          enabled: false,
          quantity: 0,
          spend: 0,
          minPrice: 0,
          maxPrice: 0,
          timeoutHours: 36
        },
        takeProfit: { enabled: false, value: 50, activeAfterHours: 0},
        stopLoss: { enabled: false, value: -10, activeAfterHours: 0},
        trailingStopLoss:  {
          enabled: false,
          value: 5,
          startAt: 20,
          activeAfterHours: 0,
          checkInterval: 1
        }
      },
      closed: true
  }

  @observable
  settings = {
    holdPosition: false,
    traceClosedPosition: false
  };

  @observable
  buyWhen = {
    enabled: false,
    quantity: 0,
    spend: 0,
    minPrice: 0,
    maxPrice: 0,
    timeoutHours: 36
  };

  @observable takeProfit = { enabled: false, value: 50 };

  @observable stopLoss = { enabled: false, value: -10 };

  @observable rebuy = { enabled: false, value: -4 };

  @observable
  trailingStopLoss = {
    enabled: false,
    value: 5,
    startAt: 20,
    keepAtLeastForHours: 0
  };

  @observable selectedPosition = { closed: true };

  @observable alerts = [{ enabled: false, value: -10, once: true }];

  constructor(rootStore) {
    this.rootStore = rootStore;
  }

  buildSettings() {
    return {
      ...this.settings,
      buyWhen: { ...this.buyWhen },
      rebuy: { ...this.rebuy },
      takeProfit: { ...this.takeProfit },
      stopLoss: { ...this.stopLoss },
      trailingStopLoss: { ...this.trailingStopLoss }, 
      alerts: toJS(this.alerts)
    };
  }
 
  @action
  updateSpend = (percent) => {
    let bot = this.rootStore.positionStore.getSelectedBot()
    this.buyWhen.spend = bot.currentBalance * percent 
  }

  @computed get spendInDollar() {
   
    let bot = this.rootStore.positionStore.getSelectedBot()

    return (bot.fxDollar * this.buyWhen.spend).toFixed(2) 
  }

  selectPosition = pos => {
    let bot = this.rootStore.positionStore.getSelectedBot();

    this.selectedPosition = pos

    this.settings = pos.settings

    this.buyWhen = pos.settings.buyWhen

    this.takeProfit = pos.settings.takeProfit

    this.stopLoss = pos.settings.stopLoss

    this.rebuy = pos.settings.rebuy

    this.trailingStopLoss = pos.settings.trailingStopLoss

    this.alerts = pos.settings.alerts

    if (pos.closed) {
      if (this.buyWhen.quantity === 0) {
        this.buyWhen.quantity = pos.amount;
      }

      if (this.buyWhen.minPrice === 0) {
        this.buyWhen.minPrice = pos.sellRate;
      }

      if (this.buyWhen.maxPrice === 0) {
        this.buyWhen.maxPrice = pos.sellRate;
      }
    }

    if (!pos.settings.takeProfit) {
      // init if not exist
      this.takeProfit = { enabled: false, value: 20 };
    }

    if (this.takeProfit && !this.takeProfit.enabled) {
      // overwrite with bot settings
      this.takeProfit = { enabled: false, value: bot.config.takeProfit.value };
    }

    if (!this.stopLoss) {
      // init if not exsist
      this.stopLoss = { enabled: false, value: -10 };
    }

    if (!this.alerts) {
      // init if not exsist
      this.alerts = [{ enabled: false, value: -10, once: true }, { enabled: false, value: 5, once: true }, { enabled: false, value: 10, once: true }, { enabled: false, value: 25, once: true }]
    }

    if (this.stopLoss && !this.stopLoss.enabled) {
      // overwrite with bot settings
      this.stopLoss = { enabled: false, value: bot.config.stopLoss.value };
    }

    if (!this.trailingStopLoss) {
      // init if not exsist
      this.trailingStopLoss = {
        enabled: false,
        value: 5,
        startAt: 20,
        activeAfterHours: 0,
        checkInterval: 1
      };
    }

    if (this.trailingStopLoss && !this.trailingStopLoss.enabled) {
      // overwrite with bot settings
      this.trailingStopLoss = {
        enabled: false,
        value: bot.config.trailingStopLoss.value,
        startAt: bot.config.trailingStopLoss.startAt,
        activeAfterHours: bot.config.trailingStopLoss.activeAfterHours,
        checkInterval: bot.config.trailingStopLoss.checkInterval
      };
    }

    if (this.alerts && !this.stopLoss.enabled) {
      // overwrite with bot settings
      this.stopLoss = { enabled: false, value: bot.config.stopLoss.value };
    }
  };

 
}

export default PositionSettingsStore;
