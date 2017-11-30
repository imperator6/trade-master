import { observable, computed, action } from "mobx";
var _ = require('lodash');

export default class OrderbookModel {
    
    @observable products = [];
    @observable periodGroups = [];

    @observable orderbookMap = new Map();

    @observable keyList = [];
  
    @computed
    get totalCells() {
      return this.products.length * this.periodGroups.length;
    }
  
    @action
    setProducts(products) {
      this.products = products;
    }

    @action
    setPeriodGroups(periodGroups) {
      this.periodGroups = periodGroups;
    }

    @action
    calculateOrderbook() {
        this.orderbookMap.clear();

        let productNames = this.products.map( p => { return p } );
        let periodNames = this.periodGroups.map( pg => {
            let periods = pg.periods.map( p => p );
            return periods;
        });

        // join al array to one big
        periodNames = _.flatten(periodNames);
        
        let f = (a, b) => [].concat(...a.map(a => b.map(b => [].concat(a, b))));
        let cartesian = (a, b, ...c) => b ? cartesian(f(a, b), ...c) : a;        
        
        // all combinations as key
        let keyMap = cartesian(productNames, periodNames).map( o =>  o[0] + '_' + o[1] )

        this.keyList = keyMap;
    
        keyMap.forEach((key) => {
            this.orderbookMap.set(key, {bid:[],  ask:[]})
        });
    }
  }