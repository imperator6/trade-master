import React, { Component } from 'react';
import { observer, inject } from "mobx-react";



// material-ui

import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import IconButton from 'material-ui/IconButton';
import MenuIcon from 'material-ui-icons/Menu';
import { withRouter } from 'react-router'
import { Link } from "react-router-dom";

import { Menu, Icon } from 'antd';

const SubMenu = Menu.SubMenu;
const MenuItemGroup = Menu.ItemGroup;


@withRouter
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
    <Menu.Item key="cpd" >
  
          <Link to="/cpd">
              <Icon type="setting" /> <span>Web Cpd</span>
          </Link>
    </Menu.Item>

    <Menu.Item key="positions" >
          <Link to="/positions">
          <Icon type="dot-chart" /> <span>Live Positions</span>
          </Link>
    </Menu.Item>
      
    </Menu>  
   
    );
  }
}

export default TopNavBar;
