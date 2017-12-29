import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";

import ReactHighstock from "react-highcharts/ReactHighstock";
import moment from "moment";

import { observer, inject } from "mobx-react";

@inject("rootStore") @observer
class CandleChart extends React.Component {
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.chartStore
    this.stompStore = this.props.rootStore.stompStore

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

  componentDidUpdate = () => {
    console.log("Chart did update.");

    let marketName = this.store.rootStore.marketSelectionStore.selectedAsset
    let channel = "/topic/candle/" + marketName + "/1min"

    if(this.prevCannel && this.prevCannel !== channel) {
      this.stompStore.unsubscribe(this.prevCannel)
    }

    this.prevCannel = channel

    let ts = new Date()

    this.stompStore.subscribe( channel , (data) => {
      let date = ts;
      let candle = JSON.parse(data.body);
      console.log('subscribe created at: ' + ts + ' Next candle: ' +  data.body);
      this.updateChart(this.candleToChartData(candle));
    })

    this.showLoading();
  }

  componentDidMount() {
    console.log("Chart did mount.");

    this.showLoading()

    this.store.loadChart(this.store.rootStore.marketSelectionStore)

    this.store.chart = this.refs.chart

    this.report = null;

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
