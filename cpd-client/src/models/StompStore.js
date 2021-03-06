import SockJS from "sockjs-client";
import { Stomp } from "stompjs";
import { observable, computed, action } from "mobx";

import logger from "../logger"


class StompStore {

    log = logger.getLogger('StompStore')

    @observable conected = false

    @observable debug = true

    constructor(rootStore, url) {

        this.log.debug("New StompClient!");
        this.rootStore = rootStore
        this.url = url 

        this.channles = new Map()
        this.subscriptions = new Map()
        this.subscriptionDoneCallbacks = new Map()
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
            let subscriptionDoneCb = this.subscriptionDoneCallbacks.get(channel)
            this.addSubscription(channel, subscription, subscriptionDoneCb)
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

        let url = this.url //  + '?token=' + encodeURIComponent('test123')
        
        this.sockjs = new SockJS(url)
        this.client = Stomp.over(this.sockjs)
        this.client.debug = null // disable underlying  logging
        this.client.connect(header, this.onConnect, this.onError);
    }

    @action
    subscribe(channel, cb, subscriptionDoneCallback) {
        if(this.conected) {
            this.log.debug("StompClient: Adding new subscription for channel: " + channel)
            let subscription = this.client.subscribe(channel, cb)
            this.addSubscription(channel, subscription, subscriptionDoneCallback)
        } else {
            this.log.debug("StompClient: New Subscription but is not connected. -> Let's connect!. ")
            this.channles.set(channel, cb)
            this.subscriptionDoneCallbacks.set(channel, subscriptionDoneCallback)
            this.connect()
        }
    }

    addSubscription = (channel, subscription, subscriptionDoneCallback) => {
        let old = this.subscriptions.get(channel)
        if(old) {
            this.unsubscribe(channel)
        }
        this.subscriptions.set(channel, subscription)
        this.subscriptionDoneCallbacks.set(channel, subscriptionDoneCallback)

        if(subscriptionDoneCallback)
                subscriptionDoneCallback(subscription)
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
        this.subscriptionDoneCallbacks.set(channel)
    }
}


export default StompStore;