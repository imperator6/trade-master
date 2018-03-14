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
class QuantityCell extends React.Component {
  
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.positionStore;
    this.settingsStore = this.props.rootStore.positionSettingsStore;

    this.state = { editMode: false, rate: 0};
  }

  onChangeRate= (newValue) => {
    this.setState({ rate: newValue });
  }

  switchEditMode = () => {
    let position = this.store.positionMap.get(this.props.position.id);
    super.setState({ editMode: !this.state.editMode, rate: position.amount});
  };

  applyRate = (newComment) => {
    let position = this.store.positionMap.get(this.props.position.id);
    position.amount = this.state.rate

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
      let value = record.sellRate;

    if (!record.closed) {
      value = record.lastKnowRate;
    }

      result = <span onClick={this.switchEditMode}> 
       <CryptoCell position={record} value={record.amount} amount={value} allowDollarOnBuy/></span>
    }

    return result
  }
}

export default QuantityCell;
