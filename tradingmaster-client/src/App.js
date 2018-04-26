import React, { Component } from "react";
import Media from "react-media";
import styled from "styled-components";
import ThemeProvider from "styled-components";
import DevTools from "mobx-react-devtools";
import { observer, inject } from "mobx-react";

import { Switch, Route } from "react-router-dom";

import SockJS from "sockjs-client";
import { Stomp } from "stompjs";

import Sidebar from "./components/Sidebar";
import StompDisplay from "./components/StompConnectionDisplay";
import BotSelectWidget from "./components/BotSelectWidget";
import StrategyPage from "./components/StrategyPage";
import ReactHighstock from "react-highcharts/ReactHighstock";

import TimeDisplay from "./components/TimeDisplay";
import LoginPage from "./components/LoginPage";
import PositionWidget from "./components/PositionWidget"

//import Mobile from "./components/mobile/Mobile"

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

const CenterDiv = styled.div`
  display: flex;
  justify-content: center;
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

    let desktop = ( <Layout style={{ minHeight: "100vh" }}>
    <Sidebar />
          <Layout>
            <HeaderDiv>
              <BotSelectWidget />
              <StompDisplay />
            </HeaderDiv>
            <Content style={{ margin: "8px 8px" }}>
              <Switch>
                <Route path="/positions" component={PositionWidget} />
                <Route exact path="/cpd" component={TimeDisplay} />
                <Route path="/strategy" component={StrategyPage} />           
              </Switch>
            </Content>
          </Layout>
        </Layout>)

let mobile = ( <Layout style={{ minHeight: "100vh" }}>
      <Layout>
        <HeaderDiv>
          <StompDisplay />
        </HeaderDiv>
        <Content style={{ margin: "8px 8px" }}>
          <Switch>
            <Route exact path="/cpd" component={TimeDisplay} />
            <Route path="/strategy" component={StrategyPage} />
            <Route path="/positions" component={PositionWidget} />
          </Switch>
        </Content>
      </Layout>
    </Layout>)

    //let mobile2 = <Mobile/>

    if (this.store.loggedIn) {
      html = (
       
        <Media query={{ maxDeviceWidth: 700 }}>
          {matches =>
            matches ? (
              mobile
            ) : (
              desktop
            )
          }
        </Media>
          
      );
    } else {
      html = (
        
          <Layout style={{ minHeight: "100vh" }}>
            <HeaderDiv>Login</HeaderDiv>
            <Content>
              <CenterDiv>
                <LoginPage />
              </CenterDiv>
            </Content>
        </Layout>
        
   )
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
