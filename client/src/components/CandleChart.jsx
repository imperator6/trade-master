import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";

import StompClient from "./../StompService";
import ReactHighstock from "react-highcharts/ReactHighstock";
import moment from "moment";

class CandleChart extends React.Component {
  constructor(props) {
    super(props);

    let chartConfig = {
      rangeSelector: {
        buttons: [
          {
            type: "minute",
            count: 1,
            text: "1min"
          },
          {
            type: "day",
            count: 1,
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
    };

    this.state = {
      firstCandle: true,
      loaded: false,
      chartConfig: chartConfig
    };
  }

  componentDidMount() {
    console.log("Chart did mount.");

    let params = {
      start: moment()
        .utc()
        .add(-100, "d")
        .toDate()
        .toISOString(),
      end: new Date().toISOString()
    };

    console.log(params.start);
    console.log(params.end);

    let esc = encodeURIComponent;
    let query = Object.keys(params)
      .map(k => esc(k) + "=" + esc(params[k]))
      .join("&");

    let url = "http://127.0.0.1:8080/api/candles/bittrex/USDT-BTC?" + query;

    fetch(url)
      .then(result => result.json())
      .then(candles => {
        const newState = {
          ...this.state,
          loaded: true
        };

        let initData = candles.map(c => {
          return this.candleToChartData(c);
        });

        newState.chartConfig.series[0].data = initData;

        this.setState(newState);
      });

    StompClient.connect({}, frame => {
      StompClient.subscribe(
        "/topic/candle/1min",
        function(data) {
          //console.log("Candle: " + JSON.stringify(data));

          let candle = JSON.parse(data.body);

          console.log(candle)
          this.updateChart(this.candleToChartData(candle));
        }.bind(this)
      );

      // send example
      StompClient.send("/app/hello", {}, JSON.stringify({ name: "Tino" }));
    });
  }

  candleToChartData(candle) {
    let newDate = new Date(candle.start);
    let dataEntry = [
      newDate.getTime(),
      candle.open,
      candle.high,
      candle.low,
      candle.close
    ];

    return dataEntry;
  }

  updateChart = data => {
    let chart = this.refs.chart.getChart();

    if (false /*this.state.firstCandle && this.state.loaded */) {
      console.log("First Candle for chart.");

      // reredner chart via react state change
      const newState = {
        ...this.state
      };

      newState.firstCandle = false;
      newState.chartConfig.series[0].data = [data];

      this.setState(newState);
    } else {
      chart.series[0].addPoint(data, true, true);
    }
  };

  render() {
    return <ReactHighstock config={this.state.chartConfig} ref="chart" />;
  }
}

export default CandleChart;
