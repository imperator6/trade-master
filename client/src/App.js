import React, { Component } from "react";
import PriceDisplay from "./components/PriceDisplay";
import ThemeProvider from "styled-components";
import DevTools from "mobx-react-devtools";
import { observer } from "mobx-react";

import TodoList from "./components/TodoList";
import TodoListModel from "./models/TodoListModel";
import OrderbookModel from "./models/OrderbookModel";
import LiveFeedModel from "./models/LiveFeedModel";

import SockJS from "sockjs-client";
import { Stomp } from "stompjs";

import CandleChart from "./components/CandleChart"

import StompClient from "./StompService"
import ReactHighstock from 'react-highcharts/ReactHighstock'
import PouchDB from "pouchdb";
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
        <h1>1 Minutes Candels</h1>
        <CandleChart />
      </div>
    );
  }
}

export default App;
