import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
import PositionSettings from "./PositionSettings";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";

import {
  Table,
  Icon,
  Divider,
  Row,
  Col,
  Button,
  Spin,
  Tooltip,
  Select,
  Popconfirm,
  Tag,
  Pagination,
  Popover,
  Input,
  Tabs,
  InputNumber,
  Switch
} from "antd";
const Option = Select.Option;
const TabPane = Tabs.TabPane;

@inject("rootStore")
@observer
class BotResult extends React.Component {
  constructor(props) {
    super(props);
    this.store = this.props.rootStore.positionStore;
  }



  render() {

          return (<span>
          <Tooltip title={this.store.baseCurrency + "/USD"}>
            <Tag color="gold">{this.store.formatNumber(this.store.fxDollar)}</Tag>
          </Tooltip>
          <Tooltip
            title={
              "Start Balance: Bot has started with this amount of " +
              this.store.baseCurrency
            }
          >
            <Tag color="geekblue">
              SB: {this.store.formatNumber(this.store.startBalance)}{" "}
              {this.store.baseCurrency} (${this.store.startBalanceDollar.toFixed(
                2
              )})
            </Tag>
          </Tooltip>
          <Tooltip title="Available: Balance left for opening new Positions">
            <Tag color="orange">
              A: {this.store.formatNumber(this.store.currentBalance)}{" "}
              {this.store.baseCurrency} (${this.store.currentBalanceDollar.toFixed(
                2
              )})
            </Tag>
          </Tooltip>
          <Tooltip title="Total Balance incl. open positions">
            <Tag color="purple">
              T: {this.store.formatNumber(this.store.totalBaseCurrencyValue)}{" "}
              {this.store.baseCurrency} (${this.store.totalBalanceDollar.toFixed(
                2
              )})
            </Tag>
          </Tooltip>
          <Tooltip title="Total Diff incl. open positions">
            <Tag color="purple">
              D:{" "}
              {this.store.formatNumber(
                this.store.totalBaseCurrencyValue - this.store.startBalance
              )}{" "}
              {this.store.baseCurrency} (${(
                this.store.totalBalanceDollar - this.store.startBalanceDollar
              ).toFixed(2)})
            </Tag>
          </Tooltip>
          <Tooltip title="Total PnL in percent">
            {this.formtatPercent(this.store.totalBotResult)}
          </Tooltip>
          <Divider type="vertical" />
          <Tooltip title="Sync Balance with Exchange">
            <Button
              size="small"
              type="primary"
              shape="circle"
              icon="retweet"
              onClick={this.store.syncBalance}
            />
          </Tooltip>
         
          </span>)

  }

  formtatPercent(value, color) {
    //color 35823fde
    // bg #dfffbe
    // border: b7eb8f
    let v = 0;
    let sing = "+";
    let tagColor = "green";
    if (value) v = Math.abs(value).toFixed(2);
    if (value < 0) {
      sing = "-";
      tagColor = "red";
    }

    if (color) {
      tagColor = color;
    }

    return <Tag color={tagColor}>{sing + v + " %"}</Tag>;
  }

    
}                                                  

export default BotResult;
