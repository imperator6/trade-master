import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";
import CryptoCell from "./CryptoCell"
import {Tooltip, Icon, Popover, Divider, Popconfirm} from "antd"


@inject("rootStore")
@observer
class ResultCell extends React.Component {
  
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.positionStore;
    this.settingsStore = this.props.rootStore.positionSettingsStore;

    
  }


  render() {

    let position = this.store.positionMap.get(this.props.position.id)
    let record = position

    let percentValue = record.result
    let sellRate = record.sellRate;
    if (!record.closed) {
      sellRate = record.lastKnowRate;
    }

    let resultRate = (sellRate - record.buyRate) 
    let fxIncluded = false

    if(this.store.dollarMode == 'dollarOnBuy') {
      let buyDollarRate = record.buyRate * record.buyFx
      let sellDollarRate = sellRate * this.store.fxDollar
      resultRate = sellDollarRate - buyDollarRate

      percentValue = (sellDollarRate / buyDollarRate * 100 ) -100
      fxIncluded = true
    }


    return <CryptoCell key='result' position={record} value={resultRate} amount={record.amount} formatter='percent' percentValue={percentValue} allowDollarOnBuy fxAlreadyIncluded={fxIncluded}/>
  }
}

export default ResultCell;
