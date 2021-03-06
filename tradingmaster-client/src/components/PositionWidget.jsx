import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
import PositionSettings from "./PositionSettings";
import NewPosition from "./NewPosition";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";
import CryptoCell from "./tablecell/CryptoCell";
import ResultCell from "./tablecell/ResultCell";
import SellRateCell from "./tablecell/SellRateCell";
import BuyRateCell from "./tablecell/BuyRateCell";
import QuantityCell from "./tablecell/QuantityCell";
import CommentCell from "./tablecell/CommentCell";
import SettingsCell from "./tablecell/SettingsCell";
import MaxCell from "./tablecell/MaxCell";

import BotResult from "./BotResult";

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
const ButtonGroup = Button.Group;


@inject("rootStore")
@observer
class PositionWidget extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      filteredInfo: { closed: ["o"] },
      sortedInfo: {
        order: "descend",
        columnKey: "created"
      },

      newPositionFormVisible: false
    };

    this.store = this.props.rootStore.positionStore;
    this.settingsStore = this.props.rootStore.positionSettingsStore;
  }

  clearFilters = () => {
    this.setState({
      ...this.state,
      filteredInfo: null,
      sortedInfo: {
        order: "descend",
        columnKey: "created"
      }
    });
  };

  setFilterOpen = () => {
    this.setState({
      ...this.state,
      filteredInfo: { closed: ["o"] }
    });
  };

  setFilterClosed = () => {
    this.setState({
      ...this.state,
      filteredInfo: { closed: ["c"] }
    });
  };

  setAgeSort = () => {
    this.setState({
      ...this.state,
      sortedInfo: {
        order: "descend",
        columnKey: "created"
      }
    });
  };

  handleTableChange = (pagination, filters, sorter) => {
    this.setState({
      ...this.state,
      filteredInfo: filters,
      sortedInfo: sorter
    });
  };

  componentDidMount() {
    this.store.init();
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
      <ResultCell key='result' position={record} />
    );

    if (record.settings && record.settings.traceClosedPosition) {
      content.push(
        <Tooltip
          key="traceResultTip"
          placement="bottom"
          title={"Last known Rate: " + record.lastKnowRate}
        >
          <span key="traceResult">
            T:{this.formtatPercent(record.traceResult, "purple")}
          </span>
        </Tooltip>
      );
    }

    return content;
  }

  buildSettingsCell(record) {
    let content = [];

    content.push(
      <Tooltip key="settingsButton" placement="bottom" title="Settings">
        <Popover
          title={record.market}
          content={
            <PositionSettings position={record} showApplySettings={true} />
          }
          onClick={() => {
            this.settingsStore.selectPosition(record);
          }}
          trigger="click"
        >
          <Icon type="setting" />
        </Popover>
      </Tooltip>
    );

    content.push(<Divider key="div1" type="vertical" />);

    if (
      !record.closed &&
      !record.sellInPogress &&
      record.buyDate &&
      !record.settings.holdPosition
    ) {
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
            <Icon type="logout" />
          </Popconfirm>
        </Tooltip>
      );
    }
    return content;
  }

  buildPositionActions(record) {
    let actionButtons = [];
    let bot = this.store.getSelectedBot();

    actionButtons.push(
      <Tooltip
        key="exchangeChart"
        placement="bottom"
        title="Open Exchange Chart"
      >
        <a href={this.store.getChartLink(record.market)} target="_blank">
          <Icon type="line-chart" />
        </a>
      </Tooltip>
    );
    actionButtons.push(<Divider key="div0" type="vertical" />);

    actionButtons.push(
      <Tooltip key="cloneButton" placement="bottom" title="Clone Position">
        <Popconfirm
          title="Are you sure to Clone this position?"
          onConfirm={() => {
            this.store.clonePosition(record);
          }}
          okText="Yes, please CLONE!"
          cancelText="No"
        >
          <Icon type="copy" />
        </Popconfirm>
      </Tooltip>
    );

    actionButtons.push(<Divider key="div4" type="vertical" />);

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
          title="Are you sure to DELETE this position?"
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
    let { sortedInfo, filteredInfo } = this.state;
    sortedInfo = sortedInfo || {};
    filteredInfo = filteredInfo || {};

    const columns = [
      {
        title: "Id",
        dataIndex: "id",
        key: "id",
        sorter: (a, b) => a.id - b.id,
        sortOrder: sortedInfo.columnKey === "id" && sortedInfo.order
      },
      {
        title: "Market",
        dataIndex: "market",
        key: "market",
        sorter: (a, b) => (a.market > b.market) - (a.market < b.market),
        sortOrder: sortedInfo.columnKey === "market" && sortedInfo.order
      },
      {
        title: "Age",
        key: "created",
        sorter: (a, b) => {
          if (a.created > b.created) return 1;
          if (a.created < b.created) return -1;
          return 0;
        },
        sortOrder: sortedInfo.columnKey === "created" && sortedInfo.order,
        render: (text, record) => {
          let elements = [];
          let age = null;
          if (!record.closed) {
            age = record.age;
            elements.push(age);
            elements.push(<Divider key="div0" type="vertical" />);
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
        key: "amount",
        sorter: (a, b) => a.amount - b.amount,
        sortOrder: sortedInfo.columnKey === "amount" && sortedInfo.order,
        render: (text, record) => {
          return <QuantityCell position={record} />;
        }
      },
      {
        title: "Buy Rate",
        dataIndex: "buyRate",
        key: "buyRate",
        sorter: (a, b) => a.buyRate - b.buyRate,
        sortOrder: sortedInfo.columnKey === "buyRate" && sortedInfo.order,
        render: (text, record) => {
          return <BuyRateCell position={record} />;
        }
      },
      {
        title: "Sell Rate",
        key: "sellRate",
        dataIndex: "sellRate",
        sorter: (a, b) => a.sellRate - b.sellRate,
        sortOrder: sortedInfo.columnKey === "sellRate" && sortedInfo.order,
        render: (text, record) => {
          return <SellRateCell position={record} />;
        }
      },
      {
        title: "Min",
        key: "minResult",
        sorter: (a, b) => a.minResult - b.minResult,
        sortOrder: sortedInfo.columnKey === "minResult" && sortedInfo.order,
        render: (text, record) => {
          if (record.buyDate == null) return null;
          return <MaxCell position={record} min/>
        }
      },
      {
        title: "Max",
        key: "maxResult",
        sorter: (a, b) => a.maxResult - b.maxResult,
        sortOrder: sortedInfo.columnKey === "maxResult" && sortedInfo.order,
        render: (text, record) => {
          if (record.buyDate == null) return null;
          return <MaxCell position={record} />
        }
      },
      {
        title: "Result",
        key: "result",
        sorter: (a, b) => a.result - b.result,
        sortOrder: sortedInfo.columnKey === "result" && sortedInfo.order,
        render: (text, record) => {
          return this.buildResultCell(record);
        }
      },
      {
        title: "Settings",
        key: "settings",
        //sorter: (a, b) => a.result - b.result,
        //sortOrder: sortedInfo.columnKey === "result" && sortedInfo.order,
        render: (text, record) => {
          return <SettingsCell position={record} />
        }
      },
      {
        title: "Closed",
        key: "closed",
        filters: [{ text: "Open", value: "o" }, { text: "Closed", value: "c" }],
        filteredValue: filteredInfo.closed || null,
        onFilter: (value, record) => {
          if (value === "o" && !record.closed) return true;
          if (value === "c" && record.closed) return true;
          return false;
        },
        sorter: (a, b) => {
          return a.closed === b.closed ? 0 : a.closed ? -1 : 1;
        },
        sortOrder: sortedInfo.columnKey === "closed" && sortedInfo.order,
        render: (text, record) => {
          if (!record.closed) {
            let icon = (
              <Tooltip title="Position is open!">
                <Icon type="loading" />
              </Tooltip>
            );

            if (record.sellInPogress) {
              icon = (
                <span>
                  Selling <Spin />
                </span>
              );
            } else if (record.buyInPogress && record.buyDate == null) {
              icon = (
                <span>
                  Buying <Spin />
                </span>
              );
            }

            if (record.buyDate == null) {
              icon = (
                <Tooltip title="Want to buy, but price is not in the target range.">
                  <Icon type="ellipsis" />
                </Tooltip>
              );
            }

            if (record.settings && record.settings.holdPosition) {
              icon = <Icon size="small" type="lock" />;
            }

            return icon;
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
        title: "Comment",
        dataIndex: "comment",
        key: "comment",
        sorter: (a, b) => {
          return a.comment === b.comment ? 0 : a.comment ? -1 : 1;
        },
        sortOrder: sortedInfo.columnKey === "comment" && sortedInfo.order,
        render: (text, record) => {
          return <CommentCell  position={record} />
        }
      },
      {
        title: "Error",
        dataIndex: "error",
        key: "error",
        sorter: (a, b) => {
          return a.error === b.error ? 0 : a.error ? -1 : 1;
        },
        sortOrder: sortedInfo.columnKey === "error" && sortedInfo.order,
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
      pageSize: 16
    };

    let table = (
      <Table
        size="small"
        rowKey="id"
        columns={columns}
        pagination={paginationSettings}
        dataSource={this.store.positions.slice()}
        onChange={this.handleTableChange}
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
          this.store.onBotSelected(newValue);
        }}
        style={{ width: 170 }}
      >
        {botOptions}
      </Select>
    );

    return (
      <div>
        <div style={{ paddingBottom: 8 + "px" }}>
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

          <Tooltip placement="left" title="Open a new Position">
            <Popover
              visible={this.state.newPositionFormVisible}
              title="Open new Position"
              content={<NewPosition />}
              trigger="click"
            >
              <Button
                size="small"
                icon="plus"
                type="primary"
                onClick={() => {
                  this.settingsStore.selectPosition(
                    this.settingsStore.DEFAULT_NEW_POSITION
                  );
                  this.setState({
                    ...this.state,
                    newPositionFormVisible: !this.state.newPositionFormVisible
                  });
                }}
              />
            </Popover>
          </Tooltip>
          <Divider type="vertical" />
          <ButtonGroup>
            <Button size="small" onClick={this.setFilterOpen}>
              open
            </Button>
            <Button size="small" onClick={this.setFilterClosed}>
              closed
            </Button>
            <Button size="small" onClick={this.clearFilters}>
              all
            </Button>
          </ButtonGroup>

          <Select
        size="small"
        placeholder="Select TradeBot"
        value={this.store.dollarMode}
        onChange={newValue => {
          this.store.switchDollarMode(newValue);
        }}
        style={{ width: 75 }}
      >
      <Option key='baseCurrency'>{this.store.baseCurrency}</Option>
      <Option key='dollar'>$</Option>
      <Option key='dollarOnBuy'>$ Buy</Option>
        
      </Select>
      <Divider type="vertical" />
      <BotResult />
      

          <Divider type="vertical" />
          <Tooltip title="Import open balances from Exchange">
            <Popconfirm
              title="Are you sure you want to import the balance from the Exchange?"
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
        </div>
        {table}
      </div>
    );
  }
}

export default PositionWidget;
