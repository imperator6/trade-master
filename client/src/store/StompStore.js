import SockJS from "sockjs-client";
import { Stomp } from "stompjs";
import { observable, computed, action } from "mobx";

import logger from "../logger"


class StompStore {

    log = logger.getLogger('StompStore')

    @observable conected = false

    @observable debug = true

    constructor(rootStore, url) {

        this.log.debug("New StompClient!", {id: 0});
        this.rootStore = rootStore
        this.url = url 

        this.channles = new Map()
        this.subscriptions = new Map()
    }


    @action
    onConnect = () => {
        this.log.debug("StompClient sucsessfully connected to " + this.url);
        this.conected = true
        this.rebuildChannels()
    }

    rebuildChannels = () => {
        this.channles.forEach(function(callbackfn, channel) {
            this.log.debug("StompClient: Adding new subscription for channel" + channel)
            let subscription = this.client.subscribe(channel, callbackfn)
            this.addSubscription(channel, subscription)
          }.bind(this), this.channles)
    }

    onError = (error) => {
        this.conected = false
        this.log.debug("StompClient Error: " + error)
        setTimeout(this.connect, 10000)
        this.log.debug('StompClient: Reconecting in 10 seconds')
    }

    @action
    connect = () => {
        this.log.debug('StompClient: Attempting connection to ' + this.url);

        let header = {}

        let url = this.url + '?token=' + encodeURIComponent(this.rootStore.userStore.userToken)
        
        this.sockjs = new SockJS(url)
        this.client = Stomp.over(this.sockjs)
        
        this.client.debug = null // disable underlying  logging

        this.client.connect(header, this.onConnect, this.onError);
    }

    @action
    subscribe(channel, cb) {
        if(this.conected) {
            this.log.debug("StompClient: Adding new subscription for channel" + channel)
            let subscription = this.client.subscribe(channel, cb)
            this.addSubscription(channel, subscription)
        } else {
            this.log.debug("StompClient: New Subscription but is not connected. -> Let's connect!. ")

            this.channles.set(channel, cb)

            this.connect()
        }
    }

    addSubscription = (channel, subscription) => {
        let old = this.subscriptions.get(channel)
        if(old) {
            this.unsubscribe(channel)
        }
        this.subscriptions.set(channel, subscription)
    }

    @action
    unsubscribe = (channel) => {
        this.log.debug("StompClient: Unsubscribe old channel " + channel )
        let s = this.subscriptions.get(channel)
        if(s) {
            s.unsubscribe()
        }

        this.subscriptions.delete(channel)
        this.channles.delete(channel)
    }
}


export default StompStore;