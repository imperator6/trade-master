import SockJS from "sockjs-client";
import { Stomp } from "stompjs";


const sockjs = new SockJS("http://127.0.0.1:8080/portfolio")


const stompClient = Stomp.over(sockjs)

// disable logging
stompClient.debug = null


class StompClient {

    constructor(url) {
        console.log("New StompClient!");

        this.sockjs = new SockJS("http://127.0.0.1:8080/portfolio")
        this.client = Stomp.over(sockjs)

        this.connected = false
    }

    connect() {
        this.client.connect();
    }

    subscribe(channel, cb) {
        
        

    }
}


export default stompClient;