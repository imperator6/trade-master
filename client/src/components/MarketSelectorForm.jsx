import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";

import { Row, Col, DatePicker , Select, Button} from "antd";
const Option = Select.Option;

const OuterDiv = styled.div`
  display: felx;
  align-items: center;
  justify-content: center;
  border: 1px solid #424242;
  background-color: red;
  width: 100px;
  height: 37px;
`;

const provinceData = ["Zhejiang", "Jiangsu"];
const cityData = {
  Zhejiang: ["Hangzhou", "Ningbo", "Wenzhou"],
  Jiangsu: ["Nanjing", "Suzhou", "Zhenjiang"]
};

@inject("rootStore") @observer
class MarketSelectorForm extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      cities: cityData[provinceData[0]],
      secondCity: cityData[provinceData[0]][0]
    };
  }

  componentDidMount() {
    console.log(this)
  }

  handleProvinceChange = value => {
    this.setState({
      cities: cityData[value],
      secondCity: cityData[value][0]
    });
  };
  onSecondCityChange = value => {
    this.setState({
      secondCity: value
    });
  };

  onOk= (value) => {
  console.log('onOk: ', value);
}

onChange2 = (value, dateString) => {
  console.log('Selected Time: ', value);
  console.log('Formatted Selected Time: ', dateString);
}

  

  render() {
    const provinceOptions = provinceData.map(province => (
      <Option key={province}>{province}</Option>
    ));
    const cityOptions = this.state.cities.map(city => (
      <Option key={city}>{city}</Option>
    ));
    return (
      <div>
        <table>
          <thead>
            <tr>
              <th>Exchange</th>
              <th>Market</th>
              <th>Period</th>
              <th>Start</th>
              <th>End</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>
                <Select
                  defaultValue={provinceData[0]}
                  style={{ width: 90 }}
                  onChange={this.handleProvinceChange}
                >
                  {provinceOptions}
                </Select>
              </td>
              <td>
                <Select
                  value={this.state.secondCity}
                  style={{ width: 90 }}
                  onChange={this.onSecondCityChange}
                >
                  {cityOptions}
                </Select>
              </td>
              <td>
                <Select style={{ width: 80 }}  defaultValue={this.props.rootStore.strategyStore.selectedPeriod}>{
                    this.props.rootStore.strategyStore.periodList.map((p) => {
                      return <Option key={p} value={p}>{p}</Option>
                    })
                  }
                </Select>
              </td>
              <td ><DatePicker
      showTime
      format="YYYY-MM-DD HH:mm"
      placeholder="Select Time"
      onChange={this.onChange2}
      onOk={this.onOk}
    /></td>
    <td ><DatePicker
      showTime
      format="YYYY-MM-DD HH:mm"
      placeholder="Select Time"
      onChange={this.onChange2}
      onOk={this.onOk}
    /></td>
    <td><Button type="primary" shape="circle" icon="reload" /></td>
            </tr>
          </tbody>
        </table>
      </div>
    );
  }
}

export default MarketSelectorForm;
