import React, { Component } from "react";
import styled from "styled-components";
import ThemeProvider from "styled-components";
import DevTools from "mobx-react-devtools";
import { observer } from "mobx-react";


import TodoListModel from "./store/TodoListModel";
import OrderbookModel from "./store/OrderbookModel";
import LiveFeedModel from "./store/LiveFeedModel";

import SockJS from "sockjs-client";
import { Stomp } from "stompjs";

import Sidebar from "./components/Sidebar"
import StrategyPage from "./components/StrategyPage"

import StompClient from "./StompService";
import ReactHighstock from "react-highcharts/ReactHighstock";
import PouchDB from "pouchdb";


import { Layout, Menu, Breadcrumb, Icon } from 'antd';
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

PouchDB.plugin(require("pouchdb-upsert"));

const db_url = "http://s930a3549:5984";
//const db_url = 'http://192.168.0.167:5984'
const config_id = "coins";

//const store = new TodoListModel();
const orderbookStore = new OrderbookModel();

const liveFeedStore = new LiveFeedModel();

@observer
class App extends Component {
  constructor(props) {
    super(props);
  }

  
  render() {
    return (
         <div className="App">
        <Layout style={{ minHeight: '100vh' }}>
      
        
        <Sidebar />
    
        <Layout>
          <HeaderDiv />
          <Content style={{ margin: '8px 8px' }}>
                 <StrategyPage />
          </Content>
          <Footer style={{ textAlign: 'center' }}>
            Ant Design ©2016 Created by Ant UED
          </Footer>
        </Layout>
      </Layout>    
      <DevTools />       
      </div>
    );
  }
}

export default App;
