
import moment from "moment"
import PouchDBStore from "./PouchDBStore"
import OrderbookModel from "./OrderbookModel"
import StompStore from "./StompStore"
import { observable, computed, action } from "mobx";

export default class RootStore {

    @observable showMainMenu = false

    constructor() {
        this.db_url = 'http://s930a3549:5984'
        this.config_id = "kafka"

        this.remoteApiUrl = 'http://127.0.0.1:8090/api'
        this.websocketUrl = 'http://127.0.0.1:8090/socket'

        moment.locale('de')

        this.orderbookStore = new OrderbookModel(this)
        this.pouchDBStore = new PouchDBStore(this)
        
        this.stompStore = new StompStore(this, this.websocketUrl);
    }

    @action
    toggeleMainMenu = () => {
            this.showMainMenu = !this.showMainMenu
            console.log(this.showMainMenu)
    }
}