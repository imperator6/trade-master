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
class NewPosition extends React.Component {
  constructor(props) {
    super(props);
    this.store = this.props.rootStore.positionStore;
    this.settingsStore = this.props.rootStore.positionSettingsStore;

    this.positionSettingsRef = null
  }

  componentDidMount() {
    //this.store.loadExchanges();
  }

  render() {

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
    let assetList = this.store.getAssetList()
      if (assetList) {
        assetOptions = assetList.map(a => <Option key={a}>{a}</Option>);
      }

      let assetSelect = (
        <Select 
          size="small"
          showSearch
          placeholder="Select Market"
          optionFilterProp="children"
          value={this.store.selectedAsset}
          style={{ width: 130 }}
          onChange={(newMarket) => { 
            this.store.selectedAsset = newMarket 

            let onTicker = (ticker) => {
              this.settingsStore.buyWhen.minPrice = ticker.bidPrice
              this.settingsStore.buyWhen.maxPrice = ticker.askPrice
            }
            
            // load ticker
            this.store.loadTicker(this.store.selectedExchange , newMarket, onTicker)

          }}
          filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
        >
          {assetOptions}
        </Select>
        
      );

      let chartlink = null
      if(this.store.selectedAsset != null) {
        chartlink = (<Tooltip
          key="exchangeChart"
          placement="bottom"
          title="Open Exchange Chart"
        >
          <a href={this.store.getChartLink(this.store.selectedAsset)} target="_blank">
            <Icon
              type="line-chart"
            />
          </a>
        </Tooltip> )
      }

      let positionSettings = (<PositionSettings ref={(r) => {this.positionSettingsRef=r }}  showApplySettings={false}/>)
  
    return (<div>
           Asset: {assetSelect} {chartlink}<br/>
           {positionSettings}
        <br/>
      </div>
    );
  }
}                                                  

export default NewPosition;
