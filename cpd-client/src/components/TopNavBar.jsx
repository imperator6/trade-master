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

import { Menu, Icon } from 'antd';

const SubMenu = Menu.SubMenu;
const MenuItemGroup = Menu.ItemGroup;

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
    this.store = this.props.rootStore.cpdConfigStore
  }

  render() {
    return ( <Menu style={{lineHeight: '24px'}}
      
      mode="horizontal"
    >
    <Menu.Item key="mail" >
          <a onClick={() => {this.store.showDialog()}}><Icon type="setting" /> Web CPD </a>
    </Menu.Item>
      
    </Menu>  
   
    );
  }
}

export default TopNavBar;
