
import logger from "../logger";
import PouchDB from "pouchdb";
PouchDB.plugin(require('pouchdb-upsert'));

export default class PouchDbStore {
    log = logger.getLogger("PouchDbStore");

    configDB
    orderbookDB
    orderbookStore

    orderbookChangesFeed


    constructor(rootStore) {
        this.log.debug("PouchDbStore");
        this.rootStore = rootStore;
        this.orderbookStore = rootStore.orderbookStore
        // check couch db revisions -> http://s930a3549:5984/cpd_orderbook/_revs_limit 
        this.configDB = new PouchDB( rootStore.db_url + '/cpd_config', {revs_limit: 10});
       // this.orderbookDB = new PouchDB( rootStore.db_url + '/cpd_orderbook', {revs_limit: 2});

        this.init()
        //this.fillAll()
    }

    init() {

        this.configDB.get('config_' + this.rootStore.config_id).then((doc) => {
            this.orderbookStore.setConfig({products: doc.products, periodGroups: doc.periodGroups })
            this.orderbookStore.calculateOrderbook()
    
           //this.startOrderbookFeed(this.orderbookStore.keyList)

           
        }).catch(function (err) {
            console.log(err);
        });

      //  "idFilter": "function (doc, req) {  var list = JSON.parse( req.query.ids ); return list.indexOf(doc._id) > -1; }"
      //  "idFilter": "function (doc, req) {  var list = JSON.parse( req.query.ids ); for(var i = 0; i < list.length;i++) { if(list[i] == doc._id){return true;} } return false; }"
      //   "idFilter": "function (doc, req) {  return true; }"


        this.configDB.changes({
            since: 'now',
            live: true,
            include_docs: true
          }).on('change', (change) => {
            console.log(change.doc);

            let doc = change.doc;
    
            if(doc._id === "config_" + this.rootStore.config_id ) {
                this.orderbookStore.setConfig({products: doc.products, periodGroups: doc.periodGroups })
                this.orderbookStore.calculateOrderbook();
                
                //this.fillAll();
                //this.startOrderbookFeed();
            }
          });
    }

    startOrderbookFeed() {
        if(this.orderbookChangesFeed) {
            this.orderbookChangesFeed.cancel()
        }

        let ids = JSON.stringify(this.orderbookStore.keyList)
        
        this.orderbookChangesFeed = this.orderbookDB.changes({
            since: 'now',
            live: true,
            include_docs: true,
            //filter: '_view',
            //filter: 'typeFilter',
            filter: 'idFilter',
            //view: 'mydesign/myview'
            query_params: {ids: ids}
          }).on('change', (change) => {
            //console.log(change.doc);
            let doc = change.doc
            this.orderbookStore.updateOrderbook(doc._id, doc.orderbook)
          }).on('complete', (info) => {
            // changes() was canceled
            this.log.debug("orderbookChangesFeed is canceled now!");
          }).on('error', (err) => {
            this.log.debug("Error in orderbookChangesFeed", err);
            //console.log(err);
          });
    }


    fillAll() {
        this.orderbookDB.allDocs({
            include_docs: true,
            attachments: true
          }).then((result) => {
            
            //console.log(result.rows);
            result.rows.forEach((d) => {
                this.orderbookStore.updateOrderbook(d.doc._id, d.doc.orderbook)
            });
            
          }).catch(function (err) {
            console.log(err);
          });
      }

}