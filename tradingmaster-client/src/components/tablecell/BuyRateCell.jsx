import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";
import CryptoCell from "./CryptoCell"
import { InputNumber, Icon } from 'antd';

@inject("rootStore")
@observer
class BuyRateCell extends React.Component {
  
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.positionStore;
    this.settingsStore = this.props.rootStore.positionSettingsStore;

    this.state = { editMode: false, rate: 0};
  }

  onChangeRate= (newValue) => {
    console.log(e)
    this.setState({ rate: newValue });
  }

  switchEditMode = () => {
    let position = this.store.positionMap.get(this.props.position.id);
    super.setState({ editMode: !this.state.editMode, rate: position.buyRate});
  };

  applyRate = (newComment) => {
    let position = this.store.positionMap.get(this.props.position.id);
    position.buyRate = this.state.rate

    this.store.savePosition(position)
    this.switchEditMode()
  };


  render() {

    let record = this.store.positionMap.get(this.props.position.id)

    let result = null;

    if (this.state.editMode) {
      result = (
          <table>
            <tbody>
            <tr>
              <td rowSpan="2">
                  <InputNumber size="small" style={{width: '115px'}} value={this.state.rate} onChange={this.onChangeRate} />
              </td>
              <td >
              <Icon type="close" onClick={this.switchEditMode} />
              </td>
            </tr>
            <tr>
              <td >
              <Icon type="check" onClick={this.applyRate}/>
              </td>
            </tr>
            </tbody>
          </table> 
      );
    } else {
      result = <span onClick={this.switchEditMode}> 
      <CryptoCell position={record} value={record.buyRate} amount={1} allowDollarOnBuy /></span>
    }

    return result;

  }
}

export default BuyRateCell;
