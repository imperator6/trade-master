import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";
import CryptoCell from "./CryptoCell"


@inject("rootStore")
@observer
class SellRateCell extends React.Component {
  
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.positionStore;
    this.settingsStore = this.props.rootStore.positionSettingsStore;
  }


  render() {

    let record = this.store.positionMap.get(this.props.position.id)
    let value = record.sellRate;

          if (!record.closed) {
            value = record.lastKnowRate;
          }

  return <CryptoCell position={record} value={value} amount={1} />
  }
}

export default SellRateCell;
