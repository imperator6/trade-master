import React, { Component } from 'react';
import { observer, inject } from "mobx-react";

// material-ui
import { withStyles  } from 'material-ui/styles';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import IconButton from 'material-ui/IconButton';
import MenuIcon from 'material-ui-icons/Menu';
import { ListItemIcon, ListItemText } from 'material-ui/List';
import { MenuList, MenuItem } from 'material-ui/Menu';
import InboxIcon from 'material-ui-icons/MoveToInbox';
import DraftsIcon from 'material-ui-icons/Drafts';
import SendIcon from 'material-ui-icons/Send';

import Drawer from 'material-ui/Drawer';




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
class MainManu extends Component {

  constructor(props) {
    super(props);
    let { classes } = props
    this.classes = classes

    this.store = this.props.rootStore;
  }

  toggle = () => {
    this.store.toggeleMainMenu()
  }

  render() {
    return (   
        <Drawer open={this.store.showMainMenu} onClose={() => this.store.toggeleMainMenu()}>
        <div
          tabIndex={0}
          role="button"
          onClick={() => this.store.toggeleMainMenu()}
        >
         <MenuList>
        <MenuItem>
          <ListItemIcon >
            <SendIcon />
          </ListItemIcon>
          <ListItemText  inset primary="Sent mail" />
        </MenuItem>
        <MenuItem >
          <ListItemIcon >
            <DraftsIcon />
          </ListItemIcon>
          <ListItemText  inset primary="Drafts" />
        </MenuItem>
        <MenuItem >
          <ListItemIcon >
            <InboxIcon />
          </ListItemIcon>
          <ListItemText />
        </MenuItem>
      </MenuList>
        </div>
      </Drawer>
    );
  }
}

export default MainManu;