import React, { Component } from "react";
import PriceDisplay from "./components/PriceDisplay";
import ThemeProvider from "styled-components";
import DevTools from "mobx-react-devtools";
import { observer, inject } from "mobx-react";

import TopNavBar from "./components/TopNavBar";

import MainMenu from "./components/MainMenu"

// material-ui
import {
  MuiThemeProvider,
  createMuiTheme,
  withStyles,
  withTheme
} from "material-ui/styles";
import AppBar from "material-ui/AppBar";
import Toolbar from "material-ui/Toolbar";
import Typography from "material-ui/Typography";
import Button from "material-ui/Button";
import IconButton from "material-ui/IconButton";
import MenuIcon from "material-ui-icons/Menu";

import grey from "material-ui/colors/grey";
import blueGrey from "material-ui/colors/blueGrey";
import red from "material-ui/colors/red";

const colors = require("material-ui/colors");

const theme = createMuiTheme({
  palette: {
    primary: {
      light: "#f5f5f5",
      main: "#212121",
      dark: "#e0e0e0",
      contrastText: "#fff"
    },
    secondary: grey,
    error: red,
    // Used by `getContrastText()` to maximize the contrast between the background and
    // the text.
    contrastThreshold: 3,
    // Used to shift a color's luminance by approximately
    // two indexes within its tonal palette.
    // E.g., shift from Red 500 to Red 300 or Red 700.
    tonalOffset: 0.2
  },
  typography: {
    fontSize: 11,
    fontWeightLight: 200,
    fontWeightRegular: 300,
    fontWeightMedium: 400
  },
  mixins: {
    toolbar: { height: 24 }
  }
});

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
    console.log(this.classes);
    return (
      <MuiThemeProvider theme={theme}>
        <div>
          <DevTools />
          <TopNavBar />
          <MainMenu />
          <PriceDisplay store={this.orderbookStore} sdfsdf={11}/>
        </div>
      </MuiThemeProvider>
    );
  }
}

export default App;
