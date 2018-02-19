import React, { Component } from 'react';
import { observer, inject } from "mobx-react";

// material-ui
import { MuiThemeProvider, createMuiTheme, withStyles, withTheme  } from 'material-ui/styles';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import IconButton from 'material-ui/IconButton';
import MenuIcon from 'material-ui-icons/Menu';

const styles = {
  root: {
    width: '100%',
  },
  flex: {
    flex: 1,
  },
  menuButton: {
    marginLeft: -12,
    marginRight: 20,
  },
};

@withStyles(styles)
@inject("rootStore")
@observer
class TopNavBar extends Component {

  constructor(props) {
    super(props);
    let { classes } = props
    this.classes = classes
    this.store = this.props.rootStore
  }

  render() {
    console.log(this.classes)
    return (   
    <AppBar position="static">
        <Toolbar>
        <IconButton className={this.classes.menuButton} color="inherit" aria-label="Menu" onClick={() => this.store.toggeleMainMenu()}>
            <MenuIcon  />
        </IconButton>
        <Typography variant="title" color="inherit" className={this.classes.flex}>
            Web CPD
        </Typography>
            <Button color="inherit">Login</Button>
        </Toolbar>
    </AppBar>
    );
  }
}

export default TopNavBar;
