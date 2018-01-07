import { observable, computed, action } from "mobx";
import moment from "moment";
var _ = require("lodash");
import axios from "axios";

export default class ChartStore {
  constructor(rootStore) {
    this.rootStore = rootStore;
  }

  @observable loaded = false;

  @observable configChangeCount = 0;

  chart = null;

  config = {
    rangeSelector: {
      buttons: [
        {
          type: "day",
          count: 0.5,
          text: "1d"
        },
        {
          type: "week",
          count: 1,
          text: "1w"
        },
        {
          type: "month",
          count: 1,
          text: "1m"
        },
        {
          type: "year",
          count: 1,
          text: "1y"
        },
        {
          type: "all",
          text: "All"
        }
      ],
      selected: 1,
      inputEnabled: false
    },
    title: {
      text: "No market selected"
    },

    plotOptions: {
      candlestick: {
        color: "red",
        upColor: "green",
        dataGrouping: {
          enabled: true,
          forced: true,
          units: [
            //  ["millisecond", []],
            //  ["second", []],
            ["minute", [1]]
            //   ["hour", 1[1]],
            //   ["day", []],
            //   ["week", []],
            //   ["month", []],
            //   ["year", null]
          ]
        }
      }
    }
  }; // end config

  @action
  loadChart = () => {
    let title = "";
    let series = [];
    for (
      let index = 0;
      index < this.rootStore.marketSelectionStore.seriesCount;
      index++
    ) {

      let exchange = this.rootStore.marketSelectionStore.getSelectedExchange(index);
      let asset = this.rootStore.marketSelectionStore.getSelectedAsset(index);

      if (exchange && asset) {
        exchange = exchange.toLowerCase();
        asset = asset.toLowerCase();

        let marketName = exchange + ": " + asset;

        title += marketName + ", ";

        let decimals = 6;

        if (marketName.indexOf("usd") > -1) {
          decimals = 2;
        }

        series.push({
          type: "candlestick",
          name: marketName,
          id: "dataseries",
          data: [],
          tooltip: {
            valueDecimals: decimals
          }
        });
      }
    }

    this.config = {
      ...this.config,
      title: { text: title },
      series: series
    };

    for (
      let index = 0;
      index < this.rootStore.marketSelectionStore.seriesCount;
      index++
    ) {

      let exchange = this.rootStore.marketSelectionStore.getSelectedExchange(index)
      let asset = this.rootStore.marketSelectionStore.getSelectedAsset(index)
      let period = this.rootStore.marketSelectionStore.selectedPeriod

      let startDate = this.rootStore.marketSelectionStore.startDate
      .utc()
      .toDate()
      .toISOString()

      let endDate = this.rootStore.marketSelectionStore.endDate
      .utc()
      .toDate()
      .toISOString()
      

      this.loadChart2(index, exchange, asset, period, startDate, endDate);
    }
  };

  @action
  loadChart2 = (seriesIndex, exchange, asset, period, startDate, endDate)  => {
    
    if (!exchange || !asset) return;

    exchange = exchange.toLowerCase();
    asset = asset.toLowerCase();

    console.info(
      "loading chart for series " +
        seriesIndex +
        " exchange: " +
        exchange +
        " asset: " +
        asset
    );

    this.loaded = false;

    let params = {
      start: startDate,
      end: endDate
    };

    //console.log(params.start);
    //console.log(params.end);

    let esc = encodeURIComponent;
    let query = Object.keys(params)
      .map(k => esc(k) + "=" + esc(params[k]))
      .join("&");

    let url =
      this.rootStore.remoteApiUrl +
      "/candles/" +
      exchange +
      "/" +
      asset +
      "?" +
      query;

    let config = this.rootStore.userStore.getHeaderConfig();

    config = {
      ...params,
      ...config
    };

    axios
      .get(url, config)
      .then(response => {
        let candles = response.data;

        let initData = candles.map(c => {
          return this.candleToChartData(c);
        });

        // apply data
        this.config.series[seriesIndex].data = initData;

        // configre candle draw size only for first series
        if (seriesIndex == 0) {
          let periodSplit = period.split(" ");
          let periodSelector = periodSplit[1];
          let periodValue = periodSplit[0];

          switch (periodSelector) {
            case "m":
              periodSelector = "minute";
              break;
            case "h":
              periodSelector = "hour";
              break;
            case "d":
              periodSelector = "day";
              break;
            default:
              periodSelector = "minute";
          }

          //console.log([periodSelector, [periodValue]]);

          this.config.plotOptions.candlestick.dataGrouping.units = [
            //  ["millisecond", []],
            //  ["second", []],
            //  ["minute", []],
            [periodSelector, [periodValue]]
            //   ["hour", 1[1]],
            //   ["day", []],
            //   ["week", []],
            //   ["month", []],
            //   ["year", null]
          ];
        }

        this.loaded = true;
        this.configChangeCount++;
      })
      .catch(function(error) {
        console.log(error);
      });
  };

  @action
  removeSignals() {
    // remove prev. flags
    this.chart.getChart().series.forEach(series => {
      if ((series.name === "Buy Signals") | (series.name === "Sell Signals")) {
        console.log("Removing series  " + series.name);
        series.remove();
      }
    });
  }

  @action
  updateSignales = signals => {
    this.removeSignals();

    this.removeSignals(); // We need to call it twice -> No idea why

    let buySignales = [];
    let sellSignales = [];

    signals.forEach(function(signal) {
      if (signal.type === "buy") {
        buySignales.push({
          x: new Date(signal.date).getTime(),
          title: "B",
          text: "Buy: " + signal.value
        });
      } else if (signal.type === "sell") {
        sellSignales.push({
          x: new Date(signal.date).getTime(),
          title: "S",
          text: "Sell: " + signal.value
        });
      }
    });

    this.chart.getChart().addSeries(
      {
        name: "Buy Signals",
        type: "flags",
        id: "buy",
        onSeries: "dataseries",
        shape: "circlepin",
        width: 16,
        data: buySignales,
        color: "green",
        fillColor: "#57A289",
        states: {
          hover: {
            fillColor: "#516247"
          }
        }
      },
      false
    );

    this.chart.getChart().addSeries(
      {
        name: "Sell Signals",
        type: "flags",
        id: "sell",
        onSeries: "dataseries",
        shape: "circlepin",
        width: 16,
        data: sellSignales,
        color: "red",
        fillColor: "#8B0000",
        style: {
          // text style
          color: "white"
        },
        states: {
          hover: {
            fillColor: "#F35772"
          }
        }
      },
      false
    );

    this.chart.getChart().redraw();

    //this.config.series[0].data = initData;
  };

  candleToChartData = candle => {
    let newDate = new Date(candle.start);
    let dataEntry = [
      newDate.getTime(),
      candle.open,
      candle.high,
      candle.low,
      candle.close
    ];

    return dataEntry;
  };
}
