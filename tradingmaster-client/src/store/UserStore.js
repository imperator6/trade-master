import { observable, computed, action } from "mobx";
import moment from "moment";
var _ = require("lodash");
import { browserHistory } from 'react-router'

import axios from "axios";
import base64 from "base-64";
import utf8 from "utf8";

export default class UserStore {
  constructor(rootStore) {
    this.rootStore = rootStore;
  }

  @observable userToken = null

  @observable loggedIn = false

  @observable user = null

  @action
  loadUser = () => {
    
      let url = this.rootStore.remoteApiUrl + "/user"
      var config = this.getHeaderConfig()

      axios
        .get(url, config)
        .then(response => {
          this.user = response.data
          this.loggedIn = true;
        })
        .catch(err => {
            // IS_NOT_AUTHENTICATED
            this.clearUser();
        });
  };

  @action
  doLogin = (username, password) => {
    if (username && password) {
      let url =
        this.rootStore.remoteApiUrl +
        "/authentication?username=" +
        username +
        "&password=" +
        base64.encode(utf8.encode(password));
      axios
        .post(url)
        .then(response => {
          this.userToken = response.data.token;
          this.loadUser()
          
        })
        .catch(err => {
          this.clearUser();
          console.log(err);
        });
    }
  };

  @action
  clearUser() {
    this.loggedIn = false;
    this.userToken = null;
  }

  getHeaderConfig() {
    var config = {
      headers: { Authorization: "Bearer " + this.userToken }
    };

    return config;
  }
}
