import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
import PositionSettings from "./PositionSettings";
import NewPosition from "./NewPosition";
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
const ButtonGroup = Button.Group;

const NumberDiv = styled.div`
  font-size: 0.9em;
`;

const NumberDivBorder = NumberDiv.extend`
  font-size: 0.8em;
  box-sizing: border-box;
  margin: 0;
  padding: 0 7px;
  display: inline-block;
  line-height: 20px;
  height: 22px;
  border-radius: 4px;
  border: 1px solid;
  opacity: 1;
  margin-right: 8px;
  white-space: nowrap;
`;

const NumberDivRed = NumberDivBorder.extend`
  color: #f5222d;
  background: #fff1f0;
  border-color: #ffa39e;
`;

const NumberDivGreen = NumberDivBorder.extend`
  color: #52c41a;
  background: #f6ffed;
  border-color: #b7eb8f;
`;

const SmallSpan = styled.span`
  font-size: 0.8em;
`;

@inject("rootStore")
@observer
class CryptoCell extends React.Component {
  
  constructor(props) {
    super(props);

    this.state = {
        position: this.props.position
    }

    this.store = this.props.rootStore.positionStore;
    this.settingsStore = this.props.rootStore.positionSettingsStore;
  }


  render() {

    let fxRate = 1
    let value = this.props.value
    let percentValue = this.props.percentValue || 0
    let amount = this.props.amount || 1
    let formatter = this.props.formatter || 'number'
    let color = 'none'
    
    if(!this.props.fxAlreadyIncluded) {
        if(this.store.dollarMode == 'dollar' || this.store.dollarMode == 'dollarOnBuy') {
            fxRate = this.store.fxDollar
        } 
        
        if (this.store.dollarMode == 'dollarOnBuy' && this.props.allowDollarOnBuy ) {
            fxRate = this.props.position.buyFx
        } 
    }

    let fxValue = value * amount * fxRate
    let formattedValue = value

    let formattedFx = "$" + this.store.formatNumber(fxValue)

    if(this.store.dollarMode == 'baseCurrency') {
        formattedFx = this.store.formatNumber(fxValue) + ' ' + this.store.baseCurrency
    }

    if(formatter && formatter === 'percent') {
       
        let sing = "+";
        color = "green";
        formattedValue = Math.abs(percentValue).toFixed(2);
        if (percentValue < 0) {
          sing = "-";
          color = "red";
        }

        formattedValue = sing + formattedValue + ' %'

    } else {
        formattedValue = this.store.formatNumber(value)
    }
    
    let body = <span>{formattedValue}<br/><SmallSpan>{formattedFx}</SmallSpan></span>
    let div = null

    if('red' === color) {
      div = (<NumberDivRed>{body}</NumberDivRed>)
    } else if ('green' === color) {
      div = (<NumberDivGreen>{body}</NumberDivGreen>)
    } else {
      div = (<NumberDiv>{body}</NumberDiv>)
    }

    return div
  }

}

export default CryptoCell;
