import React, { Component } from "react";
import PriceDisplay from "./components/PriceDisplay";
import PositionPage from "./components/PositionPage";
import ThemeProvider from "styled-components";
import DevTools from "mobx-react-devtools";
import { observer, inject } from "mobx-react";
import TopNavBar from "./components/TopNavBar";

import { Switch, Route } from "react-router-dom";

require('./agGridStyle.scss');




@inject("rootStore")
@observer
class App extends Component {
  constructor(props) {
    super(props);
    let { classes } = props;
    this.classes = classes;
  }

  render() {

  
    return (

        <div>
          <DevTools />
          <TopNavBar />
          <Switch>
                <Route path="/positions" component={PositionPage} />
                <Route exact path="/cpd" component={PriceDisplay} />
          </Switch>
        </div>

    );
  }
}

export default App;
