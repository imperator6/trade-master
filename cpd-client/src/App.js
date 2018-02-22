import React, { Component } from "react";
import PriceDisplay from "./components/PriceDisplay";
import ThemeProvider from "styled-components";
import DevTools from "mobx-react-devtools";
import { observer, inject } from "mobx-react";
import TopNavBar from "./components/TopNavBar";
import CpdConfig from './components/CpdConfig';




@inject("rootStore")
@observer
class App extends Component {
  constructor(props) {
    super(props);
    let { classes } = props;
    this.classes = classes;
    this.orderbookStore = this.props.rootStore.orderbookStore;
  }

  render() {
    return (

        <div>
          <DevTools />
          <TopNavBar />
          <CpdConfig />
          <PriceDisplay store={this.orderbookStore} />
        </div>

    );
  }
}

export default App;
