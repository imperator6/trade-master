import React, { Component } from "react";
import styled from "styled-components";
import ThemeProvider from "styled-components";
import DevTools from "mobx-react-devtools";
import { observer, inject } from "mobx-react";

import { Switch, Route } from "react-router-dom";

import SockJS from "sockjs-client";
import { Stomp } from "stompjs";

import Sidebar from "./components/Sidebar";
import StompDisplay from "./components/StompConnectionDisplay";
import StrategyPage from "./components/StrategyPage";
import ReactHighstock from "react-highcharts/ReactHighstock";

import TimeDisplay from "./components/TimeDisplay";
import LoginPage from "./components/LoginPage";

import { Layout, Menu, Breadcrumb, Icon } from "antd";
const { Header, Content, Footer, Sider } = Layout;
const SubMenu = Menu.SubMenu;

const HeaderDiv = styled.div`
  height: 32px;
  background: #001529;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
`;

@inject("rootStore")
@observer
class App extends Component {
  constructor(props) {
    super(props);
    this.store = this.props.rootStore.userStore;
  }

  render() {
    let html;

    if (this.store.loggedIn) {
      html = (
        <Layout style={{ minHeight: "100vh" }}>
          <Sidebar />
          <Layout>
            <HeaderDiv>
              <StompDisplay />
            </HeaderDiv>
            <Content style={{ margin: "8px 8px" }}>
              <Switch>
                <Route exact path="/cpd" component={TimeDisplay} />
                <Route path="/strategy" component={StrategyPage} />
           
              </Switch>
            </Content>
          </Layout>
        </Layout>
      );
    } else {
      html = (<Layout style={{ minHeight: "100vh" }}>
      <Layout>
        <HeaderDiv>
          Login
        </HeaderDiv>
        <Content style={{ margin: "8px 8px" }}>
          <LoginPage />
        </Content>
      </Layout>
    </Layout>)
    }

    return (
      <div className="App">
        {html}
        <DevTools />
      </div>
    );
  }
}

export default App;
