import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";

import { Table, Icon, Divider, Row, Col, Button, Spin, Tooltip, Select } from "antd";
const Option = Select.Option;

@inject("rootStore")
@observer
class MarketWactherControl extends React.Component {
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.marketWatcherStore;
  }

  componentDidMount() {
    this.store.init()
  }

  render() {
    const columns = [
      {
        title: "Id",
        dataIndex: "id",
        key: "id"
      },
      {
        title: "Active",
        dataIndex: "active",
        key: "active",
        render: (text, record) => {
          if (record.active) {
            return <Icon type="loading" style={{ fontSize: 24 }} spin />;
          } else {
            return <Icon type="warning" />;
          }
        }
      },
      {
        title: "Exchange",
        dataIndex: "exchange",
        key: "exchange"
      },
      {
        title: "Market",
        dataIndex: "market",
        key: "market"
      },
      {
        title: "",
        key: "action",
        render: (text, record) => {
          if (record.active) {
            return (
              <span>
                <Tooltip title="Start Wacther">
                  <Icon
                    type="pause-circle"
                    onClick={() => this.store.stop(record)}
                  />
                </Tooltip>
                <Divider type="vertical" />
                <Tooltip title="Load in chart">
                  <Icon type="to-top" onClick={() => this.store.loadToChart(record)}/>
                </Tooltip>
                <Divider type="vertical" />
                <Tooltip title="Add to chart">
                  <Icon type="plus-circle" onClick={() => this.store.addToChart(record)}/>
                </Tooltip>
              </span>
            );
          } else {
            return (
              <span>
                <Tooltip title="Start Wacther">
                  <Icon
                    type="play-circle"
                    onClick={() => this.store.start(record)}
                  />
                </Tooltip>
                <Divider type="vertical" />
                <Tooltip title="Load to chart">
                  <Icon type="to-top" onClick={() => this.store.loadToChart(record)}/>
                </Tooltip>
                <Divider type="vertical" />
                <Tooltip title="Add to chart">
                  <Icon type="plus-circle" onClick={() => this.store.addToChart(record)}/>
                </Tooltip>
              </span>
            );
          }
        }
      }
    ];

    let table = (
      <Table
        size="small"
        rowKey="id"
        columns={columns}
        dataSource={this.store.watcherList.slice()}
      />
    );

    let exchangeOptions = this.store.exchangeList.map(exc => (
      <Option key={exc}>
        {exc}
      </Option>
    ));

    let exchangeSelect = (
      <Select
        size="small"
        placeholder="Select Exchange"
        value={this.store.selectedExchange}
        onChange={(newValue) => { this.store.selectedExchange = newValue }}
        style={{ width: 170 }}
      >
        {exchangeOptions}
      </Select>
    );

    let assetOptions = null;
      if (this.store.assetMap.get(this.store.selectedExchange)) {
        assetOptions = this.store.assetMap
          .get(this.store.selectedExchange)
          .map(a => <Option key={a}>{a}</Option>);
      }

      let assetSelect = (
        <Select 
          size="small"
          showSearch
          placeholder="Select Market"
          optionFilterProp="children"
          value={this.store.selectedAsset}
          style={{ width: 130 }}
          onChange={(newMarket) => { this.store.selectedAsset = newMarket}}
          filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
        >
          {assetOptions}
        </Select>
      );

    return (
      <div className="table-operations">
          {exchangeSelect}
          {assetSelect}
          <Tooltip title="Add new Wacther">
          <Button
            disabled={!(this.store.selectedExchange && this.store.selectedAsset)}
            size="small"
            type="primary"
            icon="plus"
            onClick={() => {this.store.addWatcher()}}
          /></Tooltip>
          <Divider type="vertical" />
          <Button
            size="small"
            type="primary"
            shape="circle"
            icon="reload"
            onClick={this.store.load}
          />
         {table}
        </div>
      )
    
  }
}

export default MarketWactherControl;
