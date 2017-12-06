import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";

import StompClient from "./../StompService";
import ReactHighstock from "react-highcharts/ReactHighstock";
import moment from "moment";

import { observer, inject } from "mobx-react";

@inject("rootStore") @observer
class CandleChart extends React.Component {
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.chartStore

    console.log(this.store)

    this.state = {
      firstCandle: true,
      loaded: false,
      chartConfig: this.store.config
    };
  }

  showLoading = () => {
      if(!this.store.loaded) {
        let chart = this.refs.chart.getChart();
        chart.showLoading();
      }
  }

  componentDidUpdate() {
    console.log("Chart did update.");

    this.showLoading();
  }

  componentDidMount() {
    console.log("Chart did mount.");

    this.showLoading()

    this.store.loadChart(this.store.rootStore.marketSelectionStore)

    StompClient.connect({}, frame => {
      StompClient.subscribe(
        "/topic/candle/1min",
        function(data) {
          //console.log("Candle: " + JSON.stringify(data));

          let candle = JSON.parse(data.body);

          console.log(candle);
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

    if (this.store.loaded) {
      chart.series[0].addPoint(data, true, true);
    }
  };

  render() {
    if(this.store.loaded) {
      return <ReactHighstock config={this.store.config} ref="chart" />;
    } else {
      return <ReactHighstock config={this.store.config} ref="chart" />;
    }
  }
}

export default CandleChart;
