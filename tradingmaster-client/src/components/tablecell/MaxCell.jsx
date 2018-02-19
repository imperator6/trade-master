import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";
import CryptoCell from "./CryptoCell"


@inject("rootStore")
@observer
class MaxCell extends React.Component {
  
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.positionStore;
    this.settingsStore = this.props.rootStore.positionSettingsStore;

    
  }


  render() {

    let position = this.store.positionMap.get(this.props.position.id)
    let record = position

    let percentValue = null
    if(this.props.min) {
      percentValue = record.minResult
    } else {
      percentValue = record.maxResult
    }

    
  
    let resultRate = record.buyRate * percentValue / 100
    let fxIncluded = false

  

    return <CryptoCell key='result' position={record} value={resultRate} amount={record.amount} formatter='percent' percentValue={percentValue} />
  }
}

export default MaxCell;
