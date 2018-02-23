import SockJS from "sockjs-client";
import { Stomp } from "stompjs";
import { observable, computed, action } from "mobx";

import logger from "../logger"

export const DEFAULT_CONFIG = {
    products: [],
    periodGroups: [],
    maxOrderbookEntries: 25
  };

class CpdConfigStore {

    log = logger.getLogger('CpdConfigStore')

    @observable show = false

    @observable config = false

    @observable config = DEFAULT_CONFIG

    @observable config_all = DEFAULT_CONFIG

    @observable productSelectMap = new Map()

    constructor(rootStore) {
        this.log.debug("New CpdConfigStore!");
        this.rootStore = rootStore
    }

    @action
    setConfig(newConfig) {
      this.log.info("Set a new config!");
      let finalConfig = { ...DEFAULT_CONFIG, ...newConfig };
      this.config = finalConfig;

      this.rootStore.orderbookStore.restartStream(this.config);
    }

    @action
    setConfigAll(newConfig) {
      this.log.info("Set a new config All!");
      let finalConfig = { ...DEFAULT_CONFIG, ...newConfig };
      this.config_all = finalConfig;
    }

    @action
    selectProduct(p, checked) {
        this.productSelectMap.set(p, checked)
    }
    
    isProductSelected(p) {
       let selected = this.productSelectMap.get(p)
       if(selected) return true
       return false
    }

    applyConfigChanges() {
        let newProductList = []

        this.productSelectMap.forEach((value, key) => {
            if(value) newProductList.push(key)
        });

        
        let newConfig = {...this.config, products: newProductList}

        this.setConfig(newConfig)

        this.log.info(newConfig)
    }


    @action
    showDialog = () => {
        
        this.log.info("showDialog!!");
        this.productSelectMap.clear()
        let productCheckboxList = this.config.products.forEach(p => {
            this.productSelectMap.set(p, true)
        })

        this.show = true
    } 

    @action
    hideDialog = () => {
        this.show = false
    }
}


export default CpdConfigStore;