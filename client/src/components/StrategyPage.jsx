import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";

import CandleChart from "./CandleChart";
import MarketSelectorForm from "./MarketSelectorForm";

import AceEditor from 'react-ace';
import brace from 'brace';

import 'brace/mode/javascript';
import 'brace/theme/monokai';

import { Row, Col, Tabs } from "antd";

import StrategyForm from "./StrategyForm"
import StrategyResults from "./StrategyResults"

import { Layout, Menu, Breadcrumb, Icon, Timeline } from "antd";
const { Header, Content, Footer, Sider } = Layout;
const SubMenu = Menu.SubMenu;

const TabPane = Tabs.TabPane;

const Logo = styled.div`
  height: 32px;
  background: rgba(255, 255, 255, 0.2)
  margin: 16px;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 2px dashed #1890ff;
`;

const Scrollarea = styled.div`
  overflow-y:scroll; 
  height: 300px;
`;

@inject("rootStore")
@observer
class StrategyPage extends React.Component {
  constructor(props) {
    super(props);

    this.store = this.props.strategyStore;
  }

  tabChangeCallback = key => {
    console.log(key);
  };

  onScriptChange = newScript => {
    //console.log(this.refs.strategyEditor);
    this.script = newScript
  };

  render() {
    return [
      <Row key="1">
          <MarketSelectorForm  strategyStore={this.props.rootStore.marketSelectionStore}/>
      </Row>,
      <Row  key="2">
        <Col span={24}>
          <CandleChart />
        </Col>
      </Row>,
      <Row  key="3">
        <Col span={1}>col-6</Col>
        <Col span={11}>
          <StrategyForm />
        </Col>
        <Col span={11}>
          <StrategyResults />
        </Col>
        <Col span={1}>col-6</Col>
      </Row>
    ];
  }
}

export default StrategyPage;
