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

  @observable seriesType = "line";

  @observable seriesTypes = ["candlestick", "line"]

  buySignales = []

  sellSignales = []

  chart = null;

  config = {
    title: {
      text: "No market selected"
    }
  }; // end config

  cleanSeriesAndPrepareConfig = () => {
    let title = "";
    let series = [];
    let plotOptions = {};

    let rangeSelector = {
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
      selected: 4,
      inputEnabled: false
    }

    for (
      let index = 0;
      index < this.rootStore.marketSelectionStore.seriesCount;
      index++
    ) {
      let exchange = this.rootStore.marketSelectionStore.getSelectedExchange(
        index
      );
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
          type: this.seriesType,
          name: marketName,
          id: "dataseries_" + index,
          data: [],
          tooltip: {
            valueDecimals: decimals
          }
        });

        // configure plot options
        if (index == 0 && this.seriesType == "candlestick") {
          let period = this.rootStore.marketSelectionStore.selectedPeriod;

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

          plotOptions = {
            candlestick: {
              color: "red",
              upColor: "green",
              dataGrouping: {
                enabled: true,
                forced: true,
                units: [
                  //  ["millisecond", []],
                  //  ["second", []],
                  [periodSelector, [periodValue]]
                  //   ["hour", 1[1]],
                  //   ["day", []],
                  //   ["week", []],
                  //   ["month", []],
                  //   ["year", null]
                ]
              }
            }
          };
        }
      }
    }

    series.push(
    {
      name: "Sell Signals",
      type: "flags",
      id: "sell",
      onSeries: "dataseries_0",
      shape: "circlepin",
      width: 16,
      data: this.sellSignales.slice(),
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
    })

    series.push({
      name: "Buy Signals",
      type: "flags",
      id: "buy",
      onSeries: "dataseries_0",
      shape: "circlepin",
      width: 16,
      data: this.buySignales.slice(),
      color: "green",
      fillColor: "#57A289",
      states: {
        hover: {
          fillColor: "#516247"
        }
      }
    })

    this.config = {
      ...this.config,
      rangeSelector: rangeSelector,
      title: { text: title },
      plotOptions: plotOptions,
      series: series
    };

    console.log(this.config)
  };

  @action
  loadChart = () => {
    this.cleanSeriesAndPrepareConfig();

    let loadTasks = [];

    for (
      let index = 0;
      index < this.rootStore.marketSelectionStore.seriesCount;
      index++
    ) {
      let exchange = this.rootStore.marketSelectionStore.getSelectedExchange(
        index
      );
      let asset = this.rootStore.marketSelectionStore.getSelectedAsset(index);
      let period = this.rootStore.marketSelectionStore.selectedPeriod;

      let startDate = this.rootStore.marketSelectionStore.startDate
        .utc()
        .toDate()
        .toISOString();

      let endDate = this.rootStore.marketSelectionStore.endDate
        .utc()
        .toDate()
        .toISOString();

      let loadPromise = this.loadChart2(
        index,
        exchange,
        asset,
        period,
        startDate,
        endDate
      );
      loadTasks.push(loadPromise);
    }

    this.loaded = false;

    Promise.all(loadTasks).then( (results) => {

      results.forEach(seriesData => {
          // apply data
          if(seriesData)
            this.config.series[seriesData.seriesIndex].data = seriesData.data;
      })

      this.loaded = true
      this.configChangeCount++
    });
  };

  @action
  loadChart2 = (seriesIndex, exchange, asset, period, startDate, endDate) => {

    return new Promise(resolve => {

      if (!exchange || !asset) resolve(null);

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

      axios.get(url, config).then(response => {
        let candles = response.data;

        let initData = candles.map(c => {
          return this.candleToChartData(c);
        });

        resolve({seriesIndex: seriesIndex, data: initData});
      });
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

  clearSignals = () => {
    console.log('Clearing signals.')
    this.buySignales = []
    this.sellSignales = []
    this.loadChart()
  }

  @action
  addSignal = signal => {


    let date = new Date(signal.signalDate)

    console.log(signal)

    if(signal.buySell == "buy") {
      
      let point = {
        x: date.getTime(),
        title: "B",
        text: signal.triggerName + " Buy: " + signal.price
      }
      
      this.buySignales.push(point)

      
      this.chart.getChart().series.forEach(series => {

        if ((series.name === "Buy Signals")) {

          console.log("Adding new Signal to series " + series.name + " for date " + date )
         
          series.addPoint(point);

         // this.chart.redraw()
        }
      });

      
    } else {
      
      let point = {
        x: date.getTime(),
        title: "S",
        text: signal.triggerName + " Sell: " + signal.price
      }

      this.sellSignales.push(point)

      this.chart.getChart().series.forEach(series => {

        if ((series.name === "Sell Signals")) {

          console.log("Adding new Signal to series " + series.name + " for date " + date )
         
          series.addPoint(point);

         // this.chart.redraw()
        }
      });
    }
  }

  @action
  updateSignales = signals => {
    this.removeSignals();

    this.removeSignals(); // We need to call it twice -> No idea why

    console.log("Update signals")
    console.log(signals)

    let buySignales = [];
    let sellSignales = [];

    signals.forEach(function(signal) {
      if (signal.type === "buy") {
        buySignales.push({
          x: new Date(signal.date).getTime(),
          title: "B",
          text: signal.tiggerName + " Buy: " + signal.value
        });
      } else if (signal.type === "sell") {
        sellSignales.push({
          x: new Date(signal.date).getTime(),
          title: "S",
          text: signal.tiggerName + " Sell: " + signal.value
        });
      }
    });

    this.chart.getChart().addSeries(
      {
        name: "Buy Signals",
        type: "flags",
        id: "buy",
        onSeries: "dataseries_0",
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
        onSeries: "dataseries_0",
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

    let  dataEntry = [
        newDate.getTime(),
        candle.open,
        candle.high,
        candle.low,
        candle.close
      ];

   

    return dataEntry
  };
}
