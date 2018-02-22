import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import { observable, computed, action, toJS } from "mobx";
import { observer } from "mobx-react";
//import ThemeProvider from 'styled-components';
import accounting from "accounting";
import DataColumns from "./DataColumns";
import { Popover, Button } from "antd";

import IE from "ie-version"

const Table = styled.table`
  border: 1px solid #424242;
  border-collapse: collapse;
  font-family: Verdana, Geneva, sans-serif;
  color: black;
  display: ${props => IE.version ? 'inline' : ''};
`;


export const TD = styled.td`
  font-size: 11px;
  border: 1px solid #424242;
  background-color: #f5f5f5;
  white-space: nowrap;
  text-align: center;
  width: 47px;
  height: 20px;
  padding: 2px;
`;

const TH = styled.th`
  font-size: 12px;
  color: #ffffff;
  background-color: #212121;
  padding: 2px;
  text-align: center;
`;

const TD_HEADER = styled.td`
  font-size: 10px;
  padding: 2px;
  border: 1px solid #424242;
  background-color: #e0e0e0;
  width: 40px;
`;

const TD_GROUP = styled.td`
  font-size: 10px;
  padding: 2px;
  border: 1px solid #424242;
  color: #ffffff;
  background-color: #212121;
  width: 70px;
`;

const TD_PERIOD = styled.td`
  font-size: 12px;
  color: #ffffff;
  background-color: #212121;
  padding: 2px;
  text-align: right;
  vertical-align: top;
  width: 70px;
`;

@observer
class OrderbookTable extends React.Component {
  static propTypes = {
    product: PropTypes.string.isRequired,
    period: PropTypes.string.isRequired,
    store: PropTypes.object.isRequired
  };

  @observable maxEntries = 1;
  @observable showHeader = true;
  @observable showPeriod = true;

  constructor(props) {
    super(props);
    this.showHeader = this.props.showHeader;
    this.showPeriod = this.props.showPeriod;
    this.showTitle = this.props.showTitle;
  }

  @action
  toggleMaxEntries = event => {
    event.preventDefault();

    if (this.maxEntries > 1) {
      this.maxEntries = 1;
    } else {
      this.maxEntries = this.props.store.rootStore.cpdConfigStore.config.maxOrderbookEntries;
    }
  };

  @action
  toggleHeader = event => {
    event.preventDefault();
    this.showHeader = !this.showHeader;
  };

  render() {
    let key = this.props.product + "_" + this.props.period;
    let orderbook = this.props.store.orderbookMap.get(key);
    let rows = [];

    if (orderbook != null) {
      let rowCount = Math.max(orderbook.ask.length, orderbook.bid.length);
      rowCount = Math.min(rowCount, this.maxEntries);

      for (let i = 0; i < rowCount; i++) {
        let bidDataKey = key + "_bid_" + i;
        let askDataKey = key + "_ask_" + i;

        let bidData = this.props.store.dataMap.get(bidDataKey);
        let askData = this.props.store.dataMap.get(askDataKey);

        let noData = false;
        if (
          bidData === this.props.store.EMPTY_DATA &&
          askData === this.props.store.EMPTY_DATA
        ) {
          noData = true;
        }

        if (!noData || i == 0) {
          rows.push(
            <tr key={key + "_" + i}>
              {this.showPeriod &&
                i == 0 && (
                  <TD_PERIOD rowSpan={rowCount}>{this.props.period}</TD_PERIOD>
                )}
              <DataColumns
                store={this.props.store}
                data={bidData}
                reverse={false}
              />
              <DataColumns
                store={this.props.store}
                data={askData}
                reverse={true}
              />
            </tr>
          );
        }
      }
    }

    let headerColSpan = 6;
    if (this.showPeriod) headerColSpan = 7;

    let result = (
      <Table>
        {this.showHeader && (
          <thead>
            {this.showTitle && (
              <tr>
                <TH colSpan={headerColSpan}>{this.props.product}</TH>
              </tr>
            )}
            <tr>
              {this.showPeriod && (
                <TD_GROUP>{this.props.periodGroup.groupName}</TD_GROUP>
              )}
              <TD_HEADER>Broker</TD_HEADER>
              <TD_HEADER>BidQty</TD_HEADER>
              <TD_HEADER>BidPrc</TD_HEADER>
              <TD_HEADER>AscPrc</TD_HEADER>
              <TD_HEADER>AskQty</TD_HEADER>
              <TD_HEADER>Broker</TD_HEADER>
            </tr>
          </thead>
        )}
        <tbody onClick={this.toggleMaxEntries}>{rows}</tbody>
      </Table>
    );

    return result;
  }
}

export default OrderbookTable;
