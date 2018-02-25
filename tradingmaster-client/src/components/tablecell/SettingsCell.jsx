import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";
import CryptoCell from "./CryptoCell"
import {Tooltip, Icon, Popover, Divider, Popconfirm} from "antd"
import PositionSettings from "../PositionSettings"


@inject("rootStore")
@observer
class SettingsCell extends React.Component {
  
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.positionStore;
    this.settingsStore = this.props.rootStore.positionSettingsStore;

    
  }


  render() {

    let position = this.store.positionMap.get(this.props.position.id)
    let record = position

    let content = [];

   

    content.push(
      <Tooltip key="settingsButton" placement="bottom" title="Settings">
        <Popover
          title={record.market}
          content={
            <PositionSettings position={record} showApplySettings={true} />
          }
          onClick={() => {
            this.settingsStore.selectPosition(record);
          }}
          trigger="click"
        >
          <Icon type="setting" />
        </Popover>
      </Tooltip>
    );

    let alertActive = false

    position.settings.alerts.forEach(a => {
      if(a.enabled) alertActive = true
    });
    if(alertActive) {
      content.push(<Divider key="div1" type="vertical" />);
     
      content.push( <Tooltip key="alert" placement="bottom" title="Alert is active">
             <Icon type="bell" />
            </Tooltip>)
    }

    content.push(<Divider key="div2" type="vertical" />);

    if (
      !record.closed &&
      !record.sellInPogress &&
      record.buyDate &&
      !record.settings.holdPosition
    ) {
      content.push(
        <Tooltip key="sellButton" placement="bottom" title="Sell Position">
          <Popconfirm
            title="Are you sure to close this position?"
            onConfirm={() => {
              this.store.sellPosition(record);
            }}
            okText="Yes I'm sure!"
            cancelText="No"
          >
            <Icon type="logout" />
          </Popconfirm>
        </Tooltip>
      );
    }
  

    return <span style={{whiteSpace: 'nowrap'}}>{content}</span>
  }
}

export default SettingsCell;
