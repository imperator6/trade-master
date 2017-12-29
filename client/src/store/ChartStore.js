import { observable, computed, action } from "mobx";
import moment from "moment";
var _ = require("lodash");
import axios from "axios";

export default class ChartStore {
  constructor(rootStore) {
    this.rootStore = rootStore;
  }

  @observable loaded = false;

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
      text: "AAPL Stock Price"
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
    },

    series: [
      {
        type: "candlestick",
        name: "AAPL",
        id: "dataseries",
        data: [],
        tooltip: {
          valueDecimals: 2
        }
      },
      {
        type: "candlestick",
        name: "AAPL",
        id: "dataseries2",
        data: [],
        tooltip: {
          valueDecimals: 2
        }
      }
    ]
  }; // end config

  @action
  loadChart = () => {
    this.loadChart2(this.rootStore.marketSelectionStore, 0);
  };

  @action
  loadChart2 = (marketSelectionStore, seriesIndex) => {
    console.log("loading chart");

    let marketSelection = marketSelectionStore;

    this.loaded = false;

    let params = {
      start: marketSelection.startDate
        .utc()
        .toDate()
        .toISOString(),
      end: marketSelection.endDate
        .utc()
        .toDate()
        .toISOString()
    };

    console.log(params.start);
    console.log(params.end);

    let esc = encodeURIComponent;
    let query = Object.keys(params)
      .map(k => esc(k) + "=" + esc(params[k]))
      .join("&");

    let url =
      this.rootStore.remoteApiUrl +
      "/candles/" +
      marketSelection.selectedExchange.toLowerCase() +
      "/" +
      marketSelection.selectedAsset.toUpperCase() +
      "?" +
      query;

    let config = this.rootStore.userStore.getHeaderConfig();

    config = {
      ...params,
      ...config
    };

    console.log(config);

    axios
      .get(url, config)
      .then(response => {
        let candles = response.data;

        let initData = candles.map(c => {
          return this.candleToChartData(c);
        });

        // apply data
        this.config.series[seriesIndex].data = initData;

        console.log("selectedPeriod")
        console.log(marketSelection)

        // configre candle draw size only for first series
        if (seriesIndex == 0) {
          let periodSplit = marketSelection.selectedPeriod.split(" ");
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
