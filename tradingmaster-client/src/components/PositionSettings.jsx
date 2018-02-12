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
const ButtonGroup = Button.Group;

@inject("rootStore")
@observer
class PositionSettings extends React.Component {
  constructor(props) {
    super(props);

    this.showApplySettings = props.showApplySettings
    this.store = this.props.rootStore.positionStore;
    this.settingsStore = this.props.rootStore.positionSettingsStore;
  }

  render() {

    let position = this.settingsStore.selectedPosition

    let applyLink = null;
    let newPositionLink = null;
    let buyWhenForm = null;

    if(this.showApplySettings) {
      applyLink = (<a onClick={() => {this.store.applySettings(this.settingsStore.selectedPosition, this.settingsStore.builsSettings())}}>Apply</a>)
    }
    
    if(position.closed) {
      newPositionLink = (<div><br/>
        <a onClick={() => {
          let exchange = this.store.selectedExchange
          let market = this.store.getMarket()
          if(position.market) {
             market = position.market
          }
          this.store.openNewPosition( exchange, market ,this.settingsStore.builsSettings())}}>Create new Position</a>
        <br/></div>)
    }
   

    if(this.settingsStore.buyWhen.enabled || this.settingsStore.selectedPosition.closed) {
      buyWhenForm = (<div>
        Spend:  <ButtonGroup>
          <Button size="small" onClick={() => this.settingsStore.updateSpend(0.02)}>2%</Button>
          <Button size="small" onClick={() => this.settingsStore.updateSpend(0.05)}>5%</Button>
          <Button size="small" onClick={() => this.settingsStore.updateSpend(0.1)}>10%</Button>
          <Button size="small" onClick={() => this.settingsStore.updateSpend(0.25)}>25%</Button>
          <Button size="small" onClick={() => this.settingsStore.updateSpend(0.5)}>50%</Button>
          <Button size="small" onClick={() => this.settingsStore.updateSpend(1)}>100%</Button>

        </ButtonGroup>
        <br/> 
       <InputNumber size="small"  value={this.settingsStore.buyWhen.spend} onChange={(newValue) => { this.settingsStore.buyWhen.spend = newValue} } /> ${this.settingsStore.spendInDollar}
       <span> </span> Quantity: <InputNumber size="small"  value={this.settingsStore.buyWhen.quantity} onChange={(newValue) => { this.settingsStore.buyWhen.quantity = newValue}} />  <br/>
    Min Price: <InputNumber size="small"  value={this.settingsStore.buyWhen.minPrice} onChange={(newValue) => { this.settingsStore.buyWhen.minPrice = newValue }} /> 
    Max Price: <InputNumber size="small"  value={this.settingsStore.buyWhen.maxPrice} onChange={(newValue) => { this.settingsStore.buyWhen.maxPrice = newValue }} />  <br/>
    Timeout Hours: <InputNumber size="small" min={1} max={10000} defaultValue={36} value={this.settingsStore.buyWhen.timeoutHours} onChange={(newValue) => { this.settingsStore.buyWhen.timeoutHours = newValue  }} /> 
    
    <br/>{newPositionLink}<br/></div>) 
    }

    let traceClosedPosition = null
    
    if(position.closed) {
      traceClosedPosition = (<div>Trace Closed Position<br/><Switch checked={this.settingsStore.settings.traceClosedPosition} onChange={(newValue) =>{ this.settingsStore.settings.traceClosedPosition = newValue }}/></div>)
    }


    return (<div>
            {buyWhenForm}{traceClosedPosition}
             Hold Position:<br/>
           <Switch checked={this.settingsStore.settings.holdPosition} onChange={(newValue) =>{ this.settingsStore.settings.holdPosition = newValue }}/>
           <br/>
           Ping Pong: <Switch checked={this.settingsStore.settings.pingPong} onChange={(newValue) =>{ this.settingsStore.settings.pingPong = newValue}}/>
           <br/>
            TakeProfit:<br/>
           <Switch checked={this.settingsStore.takeProfit.enabled} onChange={(newValue) =>{ this.settingsStore.takeProfit.enabled = newValue }}/>
          <InputNumber
            size="small"
            defaultValue={20}
            disabled={!this.settingsStore.takeProfit.enabled}
            value={this.settingsStore.takeProfit.value}
            formatter={value => `${value}%`}
            parser={value => value.replace("%", "")}
            onChange={(newValue) =>{ this.settingsStore.takeProfit.value= newValue } }
          /><br/>
          StopLoss:<br/>
           <Switch checked={this.settingsStore.stopLoss.enabled} onChange={(newValue) =>{ this.settingsStore.stopLoss.enabled = newValue} }/>
          <InputNumber
            size="small"
            defaultValue={-10}
            disabled={!this.settingsStore.stopLoss.enabled}
            value={this.settingsStore.stopLoss.value}
            formatter={value => `${value}%`}
            parser={value => value.replace("%", "")}
            onChange={(newValue) =>{ this.settingsStore.stopLoss.value = newValue } }
          /><br/>
          Trailing StopLoss:<br/>
           <Switch checked={this.settingsStore.trailingStopLoss.enabled} onChange={(newValue) =>{ this.settingsStore.trailingStopLoss.enabled = newValue } }/>
           <br/>Start at
          <InputNumber
            size="small"
            defaultValue={12}
            disabled={!this.settingsStore.trailingStopLoss.enabled}
            value={this.settingsStore.trailingStopLoss.startAt}
            formatter={value => `${value}%`}
            parser={value => value.replace("%", "")}
            onChange={(newValue) =>{ this.settingsStore.trailingStopLoss.startAt = newValue } }
          /><br/>
          Stop at<InputNumber
            size="small"
            defaultValue={2}
            disabled={!this.settingsStore.trailingStopLoss.enabled}
            value={this.settingsStore.trailingStopLoss.value}
            formatter={value => `${value}%`}
            parser={value => value.replace("%", "")}
            onChange={(newValue) =>{ this.settingsStore.trailingStopLoss.value = newValue } }
          /><br/>
          Keep hours
          <InputNumber
            size="small"
            defaultValue={0}
            disabled={!this.settingsStore.trailingStopLoss.enabled}
            value={this.settingsStore.trailingStopLoss.keepAtLeastForHours}
            formatter={value => `${value}h`}
            parser={value => value.replace("h", "")}
            onChange={(newValue) =>{ this.settingsStore.trailingStopLoss.keepAtLeastForHours = newValue } }
          />
        <br/>
        <br/>
        {applyLink}  
      </div>
    );
  }
}

export default PositionSettings;
