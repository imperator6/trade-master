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

import PouchDB from "pouchdb";
PouchDB.plugin(require("pouchdb-upsert"));

const db_url = "http://s930a3549:5984";
//const db_url = 'http://192.168.0.167:5984'
const config_id = "coins";

//const store = new TodoListModel();
const orderbookStore = new OrderbookModel();

const liveFeedStore = new LiveFeedModel();

const products = [
  "Germany,Base,Power,All",
  "Germany,Peak,Power,All",
  "France,Base,Power,All"
];

const periodGroups = [
  { groupName: "Month", periods: ["Nov-17", "Dec-17", "Jan-18"] },
  { groupName: "Year", periods: ["Cal17", "Cal18", "Cal19"] }
];

const config_db = new PouchDB(db_url + "/cpd_config", { revs_limit: 10 });
const orderbook_db = new PouchDB(db_url + "/cpd_orderbook", { revs_limit: 2 });

@observer
class App extends Component {
  constructor(props) {
    super(props);

    this.sockjs = new SockJS("http://127.0.0.1:8080/portfolio");

    this.stompClient = Stomp.over(this.sockjs);
    this.stompClient.debug = null;

    //console.log(this.stompClient);

    this.stompClient.connect(
      {},
      function(frame) {
        //console.log('Connected!' + frame);
        this.stompClient.subscribe("/topic/trades", function(greeting) {
          console.log(greeting);
        });

        this.stompClient.subscribe("/topic/greetings", function(greeting) {
          console.log(greeting);
        });

        // send example
        this.stompClient.send(
          "/app/hello",
          {},
          JSON.stringify({ name: "Tino" })
        );
      }.bind(this)
    );
  }

  render() {
    return (
      <div className="App">
        <h1>Trades</h1>
      </div>
    );
  }
}

export default App;
