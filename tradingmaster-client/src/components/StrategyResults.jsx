import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";

import MarketWatcherControl from "./MarketWatcherControl";
import PositionWidget from "./PositionWidget";
import StrategyForm from "./StrategyForm"
import ConfigForm from "./ConfigForm"

import { Tabs, Timeline, Badge } from "antd";
const TabPane = Tabs.TabPane;

const Scrollarea = styled.div`
  overflow-y: scroll;
  height: 300px;
`;

@inject("rootStore")
@observer
class StrategyResults extends React.Component {

  constructor(props) {
    super(props);
    this.store = this.props.rootStore.strategyStore;
  }

  tabChangeCallback = key => {
    this.store.selectTab(key);
  };

  formatNumber = number => {
    if (number) {
      if (number > 1) {
        return number.toFixed(2);
      } else if (number == 0) {
        return number.toFixed(0);
      } else {
        if (number < -1) {
          return number.toFixed(2);
        } else {
          return number.toFixed(7);
        }
      }
    } else {
      return "";
    }
  };

  render() {
    let portfolioResult = this.store.portfolioResult;

    let timeLineItems = this.store.portfolioChanges.reverse().map(c => {
      let color = null;
      let item = null;

      if (c.type == "sell") {
        color = "red";
        item =
          this.formatNumber(c.assetOld) +
          " " +
          portfolioResult.assetName +
          " for " +
          this.formatNumber(c.value) +
          " " +
          portfolioResult.currencyName;
      } else {
        color = "green";
        item =
          this.formatNumber(c.assetNew) +
          " " +
          portfolioResult.assetName +
          " for " +
          this.formatNumber(c.value) +
          " " +
          portfolioResult.currencyName;
      }

      return (
        <Timeline.Item color={color} key={c.tradeNumber}>
          {moment(c.date).format("L")} {moment(c.date).format("LT")}
          <b> {c.type} </b> {item}
          -> <b>new balance:</b> {this.formatNumber(c.balanceNew)}{" "}
          {portfolioResult.currencyName}
        </Timeline.Item>
      );
    });

    if (timeLineItems.length > 0) {
      let profit = portfolioResult.balance - portfolioResult.holdBalance;
      let profitStatus = profit > 0 ? "success" : "error";
      let resultColor = profit > 0 ? "green" : "red";

      let result = (
        <Timeline.Item color={resultColor} key={-1}>
          <p>
            Backtest Result:{" "}
            <b style={{ color: resultColor }}>{this.formatNumber(profit)}</b>{" "}
            <b>{portfolioResult.currencyName}</b>{" "}
          </p>
          <p>
            Trades<b> {portfolioResult.trades}</b>
          </p>
          <p>
            Final Balance{" "}
            <b>
              {this.formatNumber(portfolioResult.balance)}{" "}
              {portfolioResult.currencyName} vs.{" "}
              {this.formatNumber(portfolioResult.holdBalance)}{" "}
              {portfolioResult.currencyName}
            </b>{" "}
            Hold Balance
          </p>
          <p>
            Start Asset{" "}
            <b>
              {this.formatNumber(portfolioResult.startAsset)}{" "}
              {portfolioResult.assetName} vs.{" "}
              {this.formatNumber(portfolioResult.asset)}{" "}
              {portfolioResult.assetName}{" "}
            </b>Final Asset
          </p>
          <p>
            Start Price{" "}
            <b>
              {this.formatNumber(portfolioResult.startPrice)}{" "}
              {portfolioResult.currencyName} vs.{" "}
              {this.formatNumber(portfolioResult.endPrice)}{" "}
              {portfolioResult.currencyName}{" "}
            </b>End Price
          </p>
        </Timeline.Item>
      );

      timeLineItems = [result].concat(timeLineItems);
    }

    return (
      <Tabs
        onChange={this.tabChangeCallback}
        type="card"
        activeKey={this.store.activeTab}
        size="small"
      >
      <TabPane tab="Positions" key="position">
          <PositionWidget />
        </TabPane> 
        <TabPane tab="Config" key="config">
          <ConfigForm />
        </TabPane>
        <TabPane tab="Market Watcher" key="watcher">
          <MarketWatcherControl />
        </TabPane>
        <TabPane tab="Backtest Result" key="result">
          <Scrollarea>
            <Timeline>{timeLineItems}</Timeline>
          </Scrollarea>
        </TabPane>
      </Tabs>
    );
  }
}

export default StrategyResults;
