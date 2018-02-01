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
  Input,
  Tabs,
  InputNumber,
  Switch
} from "antd";
const Option = Select.Option;
const TabPane = Tabs.TabPane;

@inject("rootStore")
@observer
class PositionSettings extends React.Component {
  constructor(props) {
    super(props);
    this.showApplySettings = props.showApplySettings

    this.position = this.props.position;
    this.store = this.props.rootStore.positionStore;

    if(!this.position.settings)
        this.position.settings = {}

    if(!this.position.settings.holdPosition) {
      this.position.settings.holdPosition = false
    }

    if(!this.position.settings.buyWhen) {
      this.position.settings.buyWhen = { enabled: false, quantity: 0, minPrice: 0, maxPrice: 0, timeoutHours: 36}
    }
    
    if(!this.position.settings.takeProfit) {
        this.position.settings.takeProfit = {enabled: false, value: 20}
    }

    if(!this.position.settings.stopLoss) {
      this.position.settings.stopLoss = {enabled: false, value: -10}
    }

    if(!this.position.settings.trailingStopLoss) {
      this.position.settings.trailingStopLoss = {enabled: false, value: 5, startAt: 20}
   }

    this.state = {
        ...this.position.settings
    }

  }

  render() {

    let applyLink = null;

    if(this.showApplySettings) {
      applyLink = (<a onClick={() => {this.store.applySettings(this.position, this.state)}}>Apply</a>)
    }

    let buyWhenForm = null;

    if(this.state.buyWhen.enabled) {
      buyWhenForm = (<div> Quantity: <InputNumber size="small"  value={this.state.buyWhen.quantity} onChange={(newValue) => { this.setState({buyWhen: {...this.state.buyWhen, quantity: newValue}})}} />  <br/>
    Min Price: <InputNumber size="small"  value={this.state.buyWhen.minPrice} onChange={(newValue) => { this.setState({buyWhen: {...this.state.buyWhen, minPrice: newValue}})}} />  <br/>
    Max Price: <InputNumber size="small"  value={this.state.buyWhen.maxPrice} onChange={(newValue) => { this.setState({buyWhen: {...this.state.buyWhen, maxPrice: newValue}})}} />  <br/>
    Timeout Hours: <InputNumber size="small" min={1} max={10000} defaultValue={36} value={this.state.buyWhen.timeoutHours} onChange={(newValue) => { this.setState({buyWhen: {...this.state.buyWhen, timeoutHours: newValue}})}} />  <br/><br/></div>) 
    }

    return (<div>
            {buyWhenForm}
             Hold Position:<br/>
           <Switch checked={this.state.holdPosition} onChange={(newValue) =>{ this.setState({...this.state, holdPosition: newValue})}}/>
           <br/>
     
            TakeProfit:<br/>
           <Switch checked={this.state.takeProfit.enabled} onChange={(newValue) =>{ this.setState({takeProfit: {...this.state.takeProfit, enabled: newValue}}) } }/>
          <InputNumber
            size="small"
            defaultValue={20}
            disabled={!this.state.takeProfit.enabled}
            value={this.state.takeProfit.value}
            formatter={value => `${value}%`}
            parser={value => value.replace("%", "")}
            onChange={(newValue) =>{ this.setState({takeProfit: {...this.state.takeProfit, value: newValue}}) } }
          /><br/>
          StopLoss:<br/>
           <Switch checked={this.state.stopLoss.enabled} onChange={(newValue) =>{ this.setState({stopLoss: {...this.state.stopLoss, enabled: newValue}}) } }/>
          <InputNumber
            size="small"
            defaultValue={-10}
            disabled={!this.state.stopLoss.enabled}
            value={this.state.stopLoss.value}
            formatter={value => `${value}%`}
            parser={value => value.replace("%", "")}
            onChange={(newValue) =>{ this.setState({stopLoss: {...this.state.stopLoss, value: newValue}}) } }
          /><br/>
          Trailing StopLoss:<br/>
           <Switch checked={this.state.trailingStopLoss.enabled} onChange={(newValue) =>{ this.setState({trailingStopLoss: {...this.state.trailingStopLoss, enabled: newValue}}) } }/>
           <br/>Start at
          <InputNumber
            size="small"
            defaultValue={12}
            disabled={!this.state.trailingStopLoss.enabled}
            value={this.state.trailingStopLoss.startAt}
            formatter={value => `${value}%`}
            parser={value => value.replace("%", "")}
            onChange={(newValue) =>{ this.setState({trailingStopLoss: {...this.state.trailingStopLoss, startAt: newValue}}) } }
          /><br/>
          Stop at<InputNumber
            size="small"
            defaultValue={2}
            disabled={!this.state.trailingStopLoss.enabled}
            value={this.state.trailingStopLoss.value}
            formatter={value => `${value}%`}
            parser={value => value.replace("%", "")}
            onChange={(newValue) =>{ this.setState({trailingStopLoss: {...this.state.trailingStopLoss, value: newValue}}) } }
          />
        <br/>
        <br/>
        {applyLink}  
      </div>
    );
  }
}

export default PositionSettings;
