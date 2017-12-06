import { observable, computed, action } from "mobx";
import moment from "moment";
var _ = require("lodash");

export default class ChartStore {
  constructor(rootStore) {
    this.rootStore = rootStore;
  }

  @observable loaded = false;

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
        data: [],
        tooltip: {
          valueDecimals: 2
        }
      }
    ]
  }; // end config

  @action
  loadChart = marketSelection => {
    console.log("loading chart");

    this.loaded = false;

    let params = {
      start: marketSelection.startDate
        .utc()
        //.add(-100, "d")
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

    fetch(url)
      .then(result => result.json())
      .then(candles => {
        let initData = candles.map(c => {
          return this.candleToChartData(c);
        });

        // apply data
        this.config.series[0].data = initData;

        // configre candle draw size
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

        console.log([periodSelector, [periodValue]]);

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

        this.loaded = true;
      });
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
