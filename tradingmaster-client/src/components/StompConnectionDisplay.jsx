import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";
import { Icon, Tooltip } from "antd";

const OuterDiv = styled.div`
  display: felx;
  align-items: center;
  justify-content: center;
  border: 1px solid #424242;
  background-color: red;
  width: 100px;
  height: 37px;
`;

@inject("rootStore")
@observer
class StompConnectionDisplay extends React.Component {
  
  constructor(props) {
    super(props);
    this.stompStore = this.props.rootStore.stompStore
  }


  render() {
    let icon = <Icon type="heart" />

    let text = "Websocket is connected"

    if(!this.stompStore.conected) {
      icon = <Icon type="warning" /> 
      text = "Websocket is disconnected"
    }
    
    return (<Tooltip title={text}>
    {icon}
  </Tooltip>)
    
  }
}

export default StompConnectionDisplay;
