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
    this.store = this.props.rootStore.marketSelectionStore;
  }

  componentDidMount() {
    this.store.init();
  }

  onStartDateChange = (value) => {
    console.log("Selected Start Time: ", value);
    this.store.startDate = value
  };

  onEndDateChange = (value) => {
    console.log("Selected End Time: ", value);
    this.store.endDate = value
  };


  render() {
    let rows = [];

    for (
      let seriesIndex = 0;
      seriesIndex < this.store.seriesCount;
      seriesIndex++
    ) {
      let selectedExchange = this.store.getSelectedExchange(seriesIndex);
      let selectedAsset = this.store.getSelectedAsset(seriesIndex);

      let exchangeOptions = this.store.exchangeList.map(exc => (
        <Option key={exc + "_" + seriesIndex} key={exc}>
          {exc}
        </Option>
      ));

      let assetOptions = null;
      if (this.store.assetMap.get(selectedExchange)) {
        assetOptions = this.store.assetMap
          .get(selectedExchange)
          .map(a => <Option key={a + "_" + seriesIndex}>{a}</Option>);
      }

      let row = null;

      let onExchangeSelect = newValue => {
        this.store.onExchangeChange(newValue, seriesIndex);
      };

      let onAssetSelect = newValue => {
        let value = newValue.split("_")[0]; // _0

        this.store.onAssetChange(value, seriesIndex);
      };

      let exchangeSelect = (
        <Select
          size="small"
          placeholder="Select Exchange"
          value={selectedExchange}
          onChange={onExchangeSelect}
          style={{ width: 130 }}
          //onChange={this.handleProvinceChange}
        >
          {exchangeOptions}
        </Select>
      );

      let assetSelect = (
        <Select 
          size="small"
          showSearch
          placeholder="Select Market"
          optionFilterProp="children"
          value={selectedAsset}
          style={{ width: 130 }}
          onChange={onAssetSelect}
          filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
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
            size="small"
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
            size="small"
            showTime
            format="YYYY-MM-DD HH:mm"
            placeholder="Select Time"
            defaultValue={this.store.startDate}
            onOk={this.onStartDateChange}
          />
        );

        endSelect = (
          <DatePicker
            size="small"
            showTime
            format="YYYY-MM-DD HH:mm"
            placeholder="Select Time"
            defaultValue={this.store.endDate}
            onOk={this.onEndDateChange}
          />
        );

        actionButton = [
          <Button
            size="small"
            type="primary"
            icon="eye"
            onClick={this.store.startCandleImport}
          />,
          <Button
            size="small"
            type="primary"
            icon="minus"
            onClick={this.store.removeSeries}
          />,
          <Button
            size="small"
            type="primary"
            icon="plus"
            onClick={this.store.addSeries}
          />,
          <Button
            size="small"
            type="primary"
            shape="circle"
            icon="reload"
            onClick={this.store.load}
          />
        ];
      }

      let typeSelect = (
          <Select
            size="small"
            style={{ width: 80 }}
            defaultValue={this.store.rootStore.chartStore.seriesType}
            onChange={(newValue ) => { this.store.rootStore.chartStore.seriesType = newValue}}
          >
            {this.store.rootStore.chartStore.seriesTypes.map(p => {
              return (
                <Option key={p} value={p}>
                  {p}
                </Option>
              );
            })}
          </Select>)

      row = (
        <tr key={seriesIndex}>
          <td>{seriesIndex}</td>
          <td>{exchangeSelect}</td>
          <td>{assetSelect}</td>
          <td>{periodSelect}</td>
          <td>{startSelect}</td>
          <td>{endSelect}</td>
          <td>{typeSelect}</td>
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
              <th>Type</th>
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
