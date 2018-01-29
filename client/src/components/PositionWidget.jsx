import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
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
  Input
} from "antd";
const Option = Select.Option;

@inject("rootStore")
@observer
class PositionWidget extends React.Component {
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.positionStore;
  }

  componentDidMount() {
    this.store.init();
  }

  formtatPercent(value) {
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
    return <Tag color={tagColor}>{sing + v + " %"}</Tag>;
  }

  formatDate(value) {
    if (value == null) return "Unknown";
    return moment(value).format("DD.MM.YY HH:mm");
  }

  showPositionDates(record) {
    return (
      <span>
        Age: {record.age} <br />
        Created: {this.formatDate(record.created)} <br />
        Buy Date: {this.formatDate(record.buyDate)} <br />
        Sell Date: {this.formatDate(record.sellDate)} <br />
        Last Update: {this.formatDate(record.lastUpdate)}
      </span>
    );
  }

  buildResultCell(record) {
    let content = [];
    content.push(
      <span key="result">{this.formtatPercent(record.result)}</span>
    );

    if (!record.closed && !record.sellInPogress) {
      content.push(
        <Tooltip key="sellButton" placement="bottom" title="Sell Position">
          <Popconfirm
            title="Are you sure to close this position?"
            onConfirm={() => {
              this.store.sellPosition(record);
            }}
            okText="Yes I'm sure!"
            cancelText="No"
          >
            <Icon type="shopping-cart" />
          </Popconfirm>
        </Tooltip>
      );

      content.push(<Divider key="div0" type="vertical" />);

      let settingsFrom = (
        <div>
            <Input addonBefore="Fix Result Target" addonAfter="%" defaultValue={record.fixResultTarget} />
            <a>Apply Settings</a>
        </div>
      );

      content.push(
        <Tooltip key="settingsButton" placement="bottom" title="Settings">
          <Popover
            title="Fix Result Target"
            content={settingsFrom}
            trigger="click"
          >
            <Icon type="setting" />
          </Popover>
        </Tooltip>
      );
    }
    return content;
  }

  buildPositionActions(record) {
    let actionButtons = [];

    actionButtons.push(
      <Tooltip
        key="exchangeChart"
        placement="bottom"
        title="Open Exchange Chart"
      >
        <a
          href={"https://bittrex.com/Market/Index?MarketName=" + record.market}
          target="_blank"
        >
          <Icon
            type="line-chart"
            onClick={() => this.store.loadToChart(record)}
          />
        </a>
      </Tooltip>
    );
    actionButtons.push(<Divider key="div0" type="vertical" />);

    actionButtons.push(
      <Tooltip key="loadToChart" placement="bottom" title="Load in chart">
        <Icon type="to-top" onClick={() => this.store.loadToChart(record)} />
      </Tooltip>
    );

    actionButtons.push(<Divider key="div1" type="vertical" />);
    actionButtons.push(
      <Tooltip key="sync" placement="bottom" title="Sync with exchange">
        <Icon type="sync" onClick={() => this.store.syncPosition(record)} />
      </Tooltip>
    );

    actionButtons.push(<Divider key="div3" type="vertical" />);
    actionButtons.push(
      <Tooltip key="delButton" placement="bottom" title="DELETE Position">
        <Popconfirm
          title="Are you sure to DELTE this position?"
          onConfirm={() => {
            this.store.deletePosition(record);
          }}
          okText="Yes, please DELETE!"
          cancelText="No"
        >
          <Icon type="delete" />
        </Popconfirm>
      </Tooltip>
    );

    return <span key="buttons">{actionButtons}</span>;
  }

  render() {
    const columns = [
      {
        title: "Id",
        dataIndex: "id",
        key: "id"
      },
      {
        title: "Market",
        dataIndex: "market",
        key: "market"
      },
      {
        title: "Age",
        key: "created",
        render: (text, record) => {
          let elements = [];
          let age = null;
          if (!record.closed) {
            age = record.age;
            elements.push(age);
            elements.push(<Divider type="vertical" />);
          }

          return (
            <Tooltip title={this.showPositionDates(record)}>
              {elements}
              <Icon type="clock-circle-o" />
            </Tooltip>
          );
        }
      },
      {
        title: "Quantity",
        dataIndex: "amount",
        key: "amount"
      },
      {
        title: "Buy Rate",
        dataIndex: "buyRate",
        key: "buyRate"
      },
      {
        title: "Sell Rate",
        key: "sellRate",
        dataIndex: "sellRate"
      },
      {
        title: "Min",
        key: "minResult",
        render: (text, record) => {
          return this.formtatPercent(record.minResult);
        }
      },
      {
        title: "Max",
        key: "maxResult",
        render: (text, record) => {
          return this.formtatPercent(record.maxResult);
        }
      },
      {
        title: "result",
        key: "result",
        render: (text, record) => {
          return this.buildResultCell(record);
        }
      },
      {
        title: "Closed",
        key: "closed",
        filters: [{ text: "Open", value: "o" }, { text: "Closed", value: "c" }],
        filtered: true,
        filteredValue: ["o"],
        onFilter: (value, record) => {
          if (value === "o" && !record.closed) return true;
          if (value === "c" && record.closed) return true;
          return false;
        },
        render: (text, record) => {
          if (!record.closed) {
            return (
              <Tooltip title="Position is open!">
                <Icon type="loading" />
              </Tooltip>
            );
          } else {
            return (
              <Tooltip title="Position is closed!">
                <Icon type="check-circle-o" />
              </Tooltip>
            );
          }
        }
      },
      {
        title: "Hold",
        dataIndex: "holdPosition",
        key: "holdPosition"
      },
      {
        title: "Error",
        dataIndex: "error",
        key: "erros",
        render: (text, record) => {
          if (record.error) {
            return (
              <Tooltip title={record.errorMsg}>
                <Icon type="warning" />
              </Tooltip>
            );
          }
        }
      },

      {
        title: "",
        key: "action",
        render: (text, record) => {
          return this.buildPositionActions(record);
        }
      }
    ];

    let paginationSettings = {
      pageSize: 30
    };

    let table = (
      <Table
        size="small"
        rowKey="id"
        columns={columns}
        pagination={paginationSettings}
        dataSource={this.store.positions.slice()}
      />
    );

    let botOptions = this.store.botList.map(botString => {
      let bot = botString.split("_");

      return <Option key={bot[0]}>{botString}</Option>;
    });

    let botSelect = (
      <Select
        size="small"
        placeholder="Select TradeBot"
        value={this.store.selectedBot}
        onChange={newValue => {
          this.store.selectedBot = newValue;
        }}
        style={{ width: 170 }}
      >
        {botOptions}
      </Select>
    );

    return (
      <div className="table-operations">
        {botSelect}
        <Divider type="vertical" />
        <Tooltip title="Reload list for selected Bot">
          <Button
            size="small"
            type="primary"
            shape="circle"
            icon="reload"
            onClick={this.store.load}
          />
        </Tooltip>
        <Divider type="vertical" />
        <Tooltip title="Import open balances from Exchange">
          <Popconfirm
            title="Are you sure you wabt to import the balance from the Exchange?"
            onConfirm={() => {
              this.store.importFromExchange();
            }}
            okText="Yes, please Import from Exchange!"
            cancelText="No"
          >
            <Button
              size="small"
              type="primary"
              shape="circle"
              icon="cloud-download-o"
            />
          </Popconfirm>
        </Tooltip>

        <Tooltip
          title={
            "Bot has started with this amount of " + this.store.baseCurrency
          }
        >
          <Tag color="geekblue">
            Start Balance: {this.store.startBalance} {this.store.baseCurrency}{" "}
            (${this.store.startBalanceDollar})
          </Tag>
        </Tooltip>
        <Tooltip title="Balance left for opening new Positions">
          <Tag color="orange">
            Available: {this.store.currentBalance} {this.store.baseCurrency} (${
              this.store.currentBalanceDollar
            })
          </Tag>
        </Tooltip>
        <Tooltip title="Total Balance incl. open positions">
          <Tag color="purple">
            Total: {this.store.totalBaseCurrencyValue} {this.store.baseCurrency}{" "}
            (${this.store.totalBalanceDollar})
          </Tag>
        </Tooltip>
        <Tooltip title="Total PnL in percent">
          {this.formtatPercent(this.store.totalBotResult)}
        </Tooltip>
        <Tooltip title="Sync Balance with Exchange">
          <Button
            size="small"
            type="primary"
            shape="circle"
            icon="retweet"
            onClick={this.store.syncBalance}
          />
        </Tooltip>
        {table}
      </div>
    );
  }
}

export default PositionWidget;
