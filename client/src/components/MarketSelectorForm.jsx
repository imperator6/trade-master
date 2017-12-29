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

@inject("rootStore")
@observer
class MarketSelectorForm extends React.Component {
  constructor(props) {
    super(props);

    console.log("New MarketSelectorForm");
    console.log(props.store);

    this.store = this.props.rootStore.marketSelectionStore;
  }

  componentDidMount() {}

  onChange2 = (value, dateString) => {
    console.log("Selected Time: ", value);
    console.log("Formatted Selected Time: ", dateString);
  };

  render() {
    let rows = [];

    for (
      let seriesIndex = 0;
      seriesIndex < this.store.seriesCount;
      seriesIndex++
    ) {
      let exchangeOptions = this.store.exchangeLists[0].map(exc => (
        <Option key={exc} key={exc}>
          {exc}
        </Option>
      ));
      let assetOptions = this.store.assetList[this.store.selectedExchange].map(
        a => <Option key={a}>{a}</Option>
      );

      let row = null;

      let exchangeSelect = (
        <Select
          defaultValue={this.store.selectedExchange}
          onChange={this.store.onExchangeChange}
          style={{ width: 130 }}
          //onChange={this.handleProvinceChange}
        >
          {exchangeOptions}
        </Select>
      );

      let assetSelect = (
        <Select
          value={this.store.selectedAsset}
          style={{ width: 130 }}
          onChange={this.store.onAssetChange}
        >
          {assetOptions}
        </Select>
      );

      let periodSelect = null;
      let startSelect = null;
      let endSelect = null;
      let actionButton = null;

      if (seriesIndex == 0) {
        periodSelect = (
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
        );

        startSelect = (
          <DatePicker
            showTime
            format="YYYY-MM-DD HH:mm"
            placeholder="Select Time"
            value={this.store.startDate}
            onChange={this.onChange2}
            onOk={this.onOk}
          />
        );

        endSelect = (
          <DatePicker
            showTime
            format="YYYY-MM-DD HH:mm"
            placeholder="Select Time"
            value={this.store.endDate}
            onChange={this.onChange2}
            onOk={this.onOk}
          />
        );

        actionButton = (
          <Button
            type="primary"
            shape="circle"
            icon="reload"
            onClick={this.store.load}
          />
        );
      }

      row = (
        <tr key={seriesIndex}>
          <td>{seriesIndex}</td>
          <td>{exchangeSelect}</td>
          <td>{assetSelect}</td>
          <td>{periodSelect}</td>
          <td>{startSelect}</td>
          <td>{endSelect}</td>
          <td>{actionButton}</td>
        </tr>
      );

      rows.push(row);
    }

    return (
      <div>
        <table>
          <thead>
            <tr>
              <th>Series</th>
              <th>Exchange</th>
              <th>Market</th>
              <th>Period</th>
              <th>Start</th>
              <th>End</th>
              <th />
            </tr>
          </thead>
          <tbody>{rows}</tbody>
        </table>
      </div>
    );
  }
}

export default MarketSelectorForm;
