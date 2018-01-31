import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";

import {
  Table,
  Icon,
  Divider,
  Row,
  Col,
  Button,
  Spin,
  Tooltip,
  Select,
  Popconfirm,
  Tag,
  Pagination,
  Popover,
  Input,
  Tabs,
  InputNumber,
  Switch
} from "antd";
const Option = Select.Option;
const TabPane = Tabs.TabPane;

@inject("rootStore")
@observer
class PositionSettings extends React.Component {
  constructor(props) {
    super(props);

    this.position = this.props.position;
    this.store = this.props.rootStore.positionStore;
    if(!this.position.settings)
        this.position.settings = {}

    this.state = {
        takeProfit: {enabled: false, value: 20},
        stopLoss: {enabled: false, value: 20},
        ...this.position.settings
    }

  }

  render() {
      console.log(this.state)
      
    return (<div>
      <Tabs size="small" defaultActiveKey="1">
        <TabPane tab="Buy" key="1">
            TakeProfit:
           <Switch checked={this.state.takeProfit.enabled} onChange={(newValue) =>{ this.setState({takeProfit: {...this.state.takeProfit, enabled: newValue}}) } }/>
          <InputNumber
            size="small"
            defaultValue={20}
            disabled={!this.state.takeProfit.enabled}
            value={this.state.takeProfit.value}
            formatter={value => `${value}%`}
            parser={value => value.replace("%", "")}
            onChange={(newValue) =>{ this.setState({takeProfit: {...this.state.takeProfit, value: newValue}}) } }
          />
        </TabPane>
        <TabPane tab="Sell" key="2">
          Content of Tab Pane 2
          </TabPane>
      </Tabs>
      <a onClick={() => {this.store.applySettings(this.position, this.state)}}>Apply</a>
      </div>
    );
  }
}

export default PositionSettings;
