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

    let bot = this.store.getSelectedBot()

    if(!this.position.settings)
        this.position.settings = {}

    if(!this.position.settings.holdPosition) {
      this.position.settings.holdPosition = false
    }

    if(!this.position.settings.traceClosedPosition) {
      this.position.settings.traceClosedPosition = false
    }

    if(!this.position.settings.pingPong) {
      this.position.settings.pingPong = false
    }

    if(!this.position.settings.buyWhen) {
      // init if not exist
      this.position.settings.buyWhen = { enabled: false, quantity: 0, spend: 0, minPrice: 0, maxPrice: 0, timeoutHours: 36}
    }

    if(this.position.closed) {
      if(this.position.settings.buyWhen.quantity === 0) {
        this.position.settings.buyWhen.quantity = this.position.amount
      }

      if(this.position.settings.buyWhen.minPrice === 0) {
        this.position.settings.buyWhen.minPrice = this.position.sellRate
      }

      if(this.position.settings.buyWhen.maxPrice === 0) {
        this.position.settings.buyWhen.maxPrice = this.position.sellRate
      }

    }
    
    if(!this.position.settings.takeProfit) {
       // init if not exist
        this.position.settings.takeProfit = {enabled: false, value: 20}
    }

    if(this.position.settings.takeProfit && !this.position.settings.takeProfit.enabled) {
       // overwrite with bot settings 
      this.position.settings.takeProfit = {enabled: false, value: bot.config.takeProfit.value}
  }

    if(!this.position.settings.stopLoss) {
      // init if not exsist
      this.position.settings.stopLoss = {enabled: false, value: -10}
    }

    if(this.position.settings.stopLoss && !this.position.settings.stopLoss.enabled) {
       // overwrite with bot settings 
      this.position.settings.stopLoss = {enabled: false, value: bot.config.stopLoss.value }
    }

    if(!this.position.settings.trailingStopLoss) {
      // init if not exsist
      this.position.settings.trailingStopLoss = {enabled: false, value: 5, startAt: 20, keepAtLeastForHours: 0}
    }

    if(this.position.settings.trailingStopLoss && !this.position.settings.trailingStopLoss.enabled) {
      // overwrite with bot settings 
      this.position.settings.trailingStopLoss = {enabled: false, value: bot.config.trailingStopLoss.value, startAt: bot.config.trailingStopLoss.startAt, keepAtLeastForHours: bot.config.trailingStopLoss.keepAtLeastForHours}
    }

    this.state = {
        ...this.position.settings,
        closed: this.position.closed,
        market: this.position.market
    }

  }

  render() {

  
    let applyLink = null;
    let newPositionLink = null;
    let buyWhenForm = null;

    if(this.showApplySettings) {
      applyLink = (<a onClick={() => {this.store.applySettings(this.position, this.state)}}>Apply</a>)
    }
    
    if(this.state.closed) {
      newPositionLink = (<div><br/>
        <a onClick={() => {
          let exchange = this.store.selectedExchange
          let market = this.store.getMarket()
          if(this.state.market) {
             market = this.state.market
          }
          this.store.openNewPosition( exchange, market ,{...this.state})}}>Create new Position</a>
        <br/></div>)
    }
   

    if(this.state.buyWhen.enabled || this.state.closed) {
      buyWhenForm = (<div>Spend: <InputNumber size="small"  value={this.state.buyWhen.spend} onChange={(newValue) => { this.setState({buyWhen: {...this.state.buyWhen, spend: newValue}})}} /> Quantity: <InputNumber size="small"  value={this.state.buyWhen.quantity} onChange={(newValue) => { this.setState({buyWhen: {...this.state.buyWhen, quantity: newValue}})}} />  <br/>
    Min Price: <InputNumber size="small"  value={this.state.buyWhen.minPrice} onChange={(newValue) => { this.setState({buyWhen: {...this.state.buyWhen, minPrice: newValue}})}} /> 
    Max Price: <InputNumber size="small"  value={this.state.buyWhen.maxPrice} onChange={(newValue) => { this.setState({buyWhen: {...this.state.buyWhen, maxPrice: newValue}})}} />  <br/>
    Timeout Hours: <InputNumber size="small" min={1} max={10000} defaultValue={36} value={this.state.buyWhen.timeoutHours} onChange={(newValue) => { this.setState({buyWhen: {...this.state.buyWhen, timeoutHours: newValue}})}} /> 
    
    <br/>{newPositionLink}<br/></div>) 
    }

    let traceClosedPosition = null
    
    if(this.state.closed) {
      traceClosedPosition = (<div>Trace Closed Position<br/><Switch checked={this.state.traceClosedPosition} onChange={(newValue) =>{ this.setState({...this.state, traceClosedPosition: newValue})}}/></div>)
    }


    return (<div>
            {buyWhenForm}{traceClosedPosition}
             Hold Position:<br/>
           <Switch checked={this.state.holdPosition} onChange={(newValue) =>{ this.setState({...this.state, holdPosition: newValue})}}/>
           <br/>
           Ping Pong: <Switch checked={this.state.pingPong} onChange={(newValue) =>{ this.setState({...this.state, pingPong: newValue})}}/>
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
          /><br/>
          Keep hours
          <InputNumber
            size="small"
            defaultValue={0}
            disabled={!this.state.trailingStopLoss.enabled}
            value={this.state.trailingStopLoss.keepAtLeastForHours}
            formatter={value => `${value}h`}
            parser={value => value.replace("h", "")}
            onChange={(newValue) =>{ this.setState({trailingStopLoss: {...this.state.trailingStopLoss, keepAtLeastForHours: newValue}}) } }
          />
        <br/>
        <br/>
        {applyLink}  
      </div>
    );
  }
}

export default PositionSettings;
