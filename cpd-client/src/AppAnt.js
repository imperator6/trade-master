import React, { Component } from 'react';
import PriceDisplay from './components/PriceDisplay';
import ThemeProvider from 'styled-components';
import DevTools from "mobx-react-devtools";
import { observer } from "mobx-react";

import TodoList from "./components/TodoList";
import TodoListModel from "./models/TodoListModel";
import OrderbookModel from "./models/OrderbookModelAnt";
import LiveFeedModel from "./models/LiveFeedModel";
import OrderbookTableAnt from './components/OrderbookTableAnt'


import { DatePicker } from 'antd';


import PouchDB from "pouchdb";
PouchDB.plugin(require('pouchdb-upsert'));

//const db_url = 'http://s930a3549:5984'
const db_url = 'http://192.168.0.167:5984'
const config_id = "coins"

//const store = new TodoListModel();
const orderbookStore = new OrderbookModel();

const liveFeedStore = new LiveFeedModel();

const products = ['Germany,Base,Power,All', 'Germany,Peak,Power,All', 'France,Base,Power,All']

const periodGroups = [{ groupName: 'Month', periods: ['Nov-17','Dec-17','Jan-18']},
{ groupName: 'Year', periods: ['Cal17','Cal18','Cal19']}];


const config_db = new PouchDB( db_url + '/cpd_config', {revs_limit: 10});

const orderbook_db = new PouchDB(db_url + '/cpd_orderbook', {revs_limit: 1});

@observer
class App extends Component {

  constructor(props) {
    super(props);
  }

  componentDidMount() {
    this.fillAll();
  }

  fillAll() {
    orderbook_db.allDocs({
        include_docs: true,
        attachments: true
      }).then(function (result) {
        
        //console.log(result.rows);

        result.rows.forEach((d) => {
            orderbookStore.orderbookMap.set(d.doc._id, d.doc.orderbook);
            //console.log(d.doc);
        });
        

      }).catch(function (err) {
        console.log(err);
      });

  }

  componentWillMount() {

    config_db.get('config_' + config_id) .then((doc) => {
        console.log(doc);
        orderbookStore.setProducts(doc.products);
        orderbookStore.setPeriodGroups(doc.periodGroups);
        orderbookStore.calculateOrderbook();
    }).catch(function (err) {
        console.log(err);
      });

      config_db.changes({
        since: 'now',
        live: true,
        include_docs: true
      }).on('change', (change) => {
        console.log(change.doc);

        let doc = change.doc;

        if(doc._id === "config_" + config_id ) {
            orderbookStore.setProducts(doc.products);
            orderbookStore.setPeriodGroups(doc.periodGroups);
            orderbookStore.calculateOrderbook();
            this.fillAll();
        }
      });


      orderbook_db.changes({
        since: 'now',
        live: true,
        include_docs: true
      }).on('change', function(change) {
        //console.log(change.doc);
        let doc = change.doc;
        if(orderbookStore.orderbookMap.get(doc._id) != null) {

            orderbookStore.orderbookMap.set(doc._id, doc.orderbook);
        }
      });
  }

  render() {

    return (
      <div className="App">
          <DevTools />
         
      
         <OrderbookTableAnt store={orderbookStore}/>

      </div>
    );
  }
}

export default App;
