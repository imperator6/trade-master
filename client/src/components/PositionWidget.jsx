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
  Popconfirm
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
    let v = 0
    let sing = "+"
    if(value) v = Math.abs(value).toFixed(2)
    if(value < 0)sing = "-"
    return sing + v + " %"
  }

  formatDate(value) {
    if(value == null) return "Unknown"
   return moment(value).format('DD.MM.YY HH:mm')
  }

  showPositionDates(record) {
    return <span>
      Created: {this.formatDate(record.created)} <br/>
      Buy Date: {this.formatDate(record.buyDate)} <br/>
      Sell Date: {this.formatDate(record.sellDate)}
    </span>
  }

  buildPositionActions(record) {

    let actionButtons = []

    actionButtons.push(<Tooltip key="loadToChart" title="Load in chart">
    <Icon
      type="to-top"
      onClick={() => this.store.loadToChart(record)}
    />
  </Tooltip>)

    if (!record.closed && !record.sellInPogress) {

      actionButtons.push( <Divider key="div1" type="vertical" />)
      actionButtons.push(  <Tooltip key="selButton" title="Sell Position">
        <Popconfirm title="Are you sure to close this position?" onConfirm={() => {this.store.sellPosition(record.id)}} okText="Yes I'm sure!" cancelText="No">
      <Icon
        type="shopping-cart"
       
      />
      </Popconfirm>
    </Tooltip>)
           
    } 

    return ( <span >{actionButtons}</span>)

  }

  render() {
    const columns = [
      {
        title: "Id",
        dataIndex: "id",
        key: "id"
      },
      {
        title: "Closed",
        key: "closed",
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
        title: "Market",
        dataIndex: "market",
        key: "market"
      },
      {
        title: "Date",
        key: "created",
        render: (text, record) => {
            return <Tooltip title={this.showPositionDates(record)}>
                <Icon type="clock-circle-o" />
              </Tooltip>
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
          return this.formtatPercent(record.minResult)
        }
      },
      {
        title: "Max",
        key: "maxResult",
        render: (text, record) => {
          return this.formtatPercent(record.maxResult)
        }
      },
      {
        title: "result",
        key: "result",
        render: (text, record) => {
          return this.formtatPercent(record.result)
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
            return   <Tooltip title={record.errorMsg}><Icon type="warning" /></Tooltip>
          }
        }
      },

      {
        title: "",
        key: "action",
        render: (text, record) => {
          return this.buildPositionActions(record)
        }
      }
    ];

    let table = (
      <Table
        size="small"
        rowKey="id"
        columns={columns}
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
        <Tooltip title="Reload list fo selected Bot">
          <Button
            size="small"
            type="primary"
            shape="circle"
            icon="reload"
            onClick={this.store.load}
          />
        </Tooltip>
        <Divider type="vertical" />

        {table}
      </div>
    );
  }
}

export default PositionWidget;
