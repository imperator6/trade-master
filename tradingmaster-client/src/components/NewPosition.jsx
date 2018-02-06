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

    this.state = {

    }

  }

  componentDidMount() {
    this.store.loadExchanges();
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

      let position = { settings: { buyWhen: {enabled: true, quantity: 0, minPrice: 0, maxPrice: 0 ,timeoutHours: 36} }, closed: true }

      let positionSettingsRef = null

      let positionSettings = (<PositionSettings ref={(r) => {positionSettingsRef=r }} position={position}  showApplySettings={false}/>)
  
    return (<div>
           Exchange: {exchangeSelect}  <br/>
           Asset: {assetSelect}  <br/>
          
           <br/>
           {positionSettings}
        <br/>
      </div>
    );
  }
}                                                  

export default NewPosition;
