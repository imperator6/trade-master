import { observable, computed, action } from "mobx";
import moment from "moment";
import axios from "axios";

var _ = require("lodash");

export default class StrategyStore {
  constructor(rootStore) {
    this.rootStore = rootStore;
  }

  @observable loaded = false;

  @observable strategyList = [];

  @observable strategyNameList = [];

  @observable selectedStrategyName = "";

  @observable selectedStrategy = null;

  @action
  selectStrategyByName = name => {
    let next = this.strategyList.find(s => {
      return s.name === name;
    });
    this.selectedStrategy = next;
  };

  @action
  saveScript = (newScript) => {

    let url = this.rootStore.remoteApiUrl + "/strategy/saveScript";

    console.log('newscript');
    console.log(newScript);

    axios
      .post(url, {
        id: this.selectedStrategy.id,
        name: this.selectedStrategy.name,
        script: newScript,
        language: this.selectedStrategy.language,
        version: this.selectedStrategy.version
      })
      .then(function(response) {
        console.log(response);
      })
      .catch(function(error) {
        console.log(error);
      });
  }

  @action
  loadStrategies = () => {
    console.log("loading strategies");

    this.loaded = false;

    let url = this.rootStore.remoteApiUrl + "/strategy/";

    axios
      .get(url)
      .then((response) => {
        let strategies = response.data

        console.log('axios')
        console.log(strategies)

        this.strategyList = strategies.map(s => {
          return {
            ...s,
            key: s.id
          };
        });

        this.strategyNameList = strategies.map(s => s.name);

        if (this.selectedStrategy == null) {
          this.selectedStrategy = strategies[0];
        }

        this.loaded = true;
      })
      .catch(function(error) {
        console.log(error);
      });

    
  };
}
