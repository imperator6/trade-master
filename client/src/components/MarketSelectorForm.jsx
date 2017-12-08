import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";

import { Row, Col, DatePicker, Select, Button } from "antd";
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

@inject("rootStore")
@observer
class MarketSelectorForm extends React.Component {
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.marketSelectionStore;

    
  }

  componentDidMount() {
    console.log(this);
  }

  onOk = value => {
    console.log("onOk: ", value);
  }

  onChange2 = (value, dateString) => {
    console.log("Selected Time: ", value);
    console.log("Formatted Selected Time: ", dateString);
  }

  render() {
    const exchangeOptions = this.store.exchangeList.map(exc => (
      <Option key={exc} key={exc}>{exc}</Option>
    ));
    const assetOptions = this.store.assetList[this.store.selectedExchange].map(
      a => <Option key={a}>{a}</Option>
    );
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
              <th />
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>
                <Select
                  defaultValue={this.store.selectedExchange}
                  style={{ width: 130 }}
                  //onChange={this.handleProvinceChange}
                >
                  {exchangeOptions}
                </Select>
              </td>
              <td>
                <Select
                  value={this.store.selectedAsset}
                  style={{ width: 130 }}
                  onChange={this.store.onAssetChange}
                >
                  {assetOptions}
                </Select>
              </td>
              <td>
                <Select
                  style={{ width: 80 }}
                  defaultValue={this.store.selectedPeriod}
                  onChange={this.store.onPeriodChange}
                >
                  {this.store.periodList.map(p => {
                    return (
                      <Option key={p} value={p}>
                        {p}
                      </Option>
                    );
                  })}
                </Select>
              </td>
              <td>
                <DatePicker
                  showTime
                  format="YYYY-MM-DD HH:mm"
                  placeholder="Select Time"
                  value={this.store.startDate}
                  onChange={this.onChange2}
                  onOk={this.onOk}
                />
              </td>
              <td>
                <DatePicker
                  showTime
                  format="YYYY-MM-DD HH:mm"
                  placeholder="Select Time"
                  value={this.store.endDate}
                  onChange={this.onChange2}
                  onOk={this.onOk}
                />
              </td>
              <td>
                <Button
                  type="primary"
                  shape="circle"
                  icon="reload"
                  onClick={this.store.load}
                />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    );
  }
}

export default MarketSelectorForm;
