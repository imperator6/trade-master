import React, { Component } from "react";
import { observer, inject } from "mobx-react";
import { Button } from 'antd-mobile';

import { Menu, ActivityIndicator, NavBar } from 'antd-mobile';
import 'antd-mobile/lib/menu/style/css';
import 'antd-mobile/lib/activity-indicator/style/css';
import 'antd-mobile/lib/nav-bar/style/css';

const data = [
  {
    value: '1',
    label: 'Food',
  }, {
    value: '2',
    label: 'Supermarket',
  },
  {
    value: '3',
    label: 'Extra',
    isLeaf: true,
  },
];

@inject("rootStore")
@observer
class Mobile extends React.Component {
  
  constructor(props) {
    super(props);
    this.store = this.props.rootStore.userStore;

    this.state = {
      initData: '',
      show: false,
    };
  }

  onChange = (value) => {
    let label = '';
    data.forEach((dataItem) => {
      if (dataItem.value === value[0]) {
        label = dataItem.label;
        if (dataItem.children && value[1]) {
          dataItem.children.forEach((cItem) => {
            if (cItem.value === value[1]) {
              label += ` ${cItem.label}`;
            }
          });
        }
      }
    });
    console.log(label);
  }
  handleClick = (e) => {
    e.preventDefault(); // Fix event propagation on Android
    this.setState({
      show: !this.state.show,
    });
    // mock for async data loading
    if (!this.state.initData) {
      setTimeout(() => {
        this.setState({
          initData: data,
        });
      }, 500);
    }
  }

  onMaskClick = () => {
    this.setState({
      show: false,
    });
  }


  render() {
    const { initData, show } = this.state;
    const menuEl = (
      <Menu
        className="single-foo-menu"
        data={initData}
        value={['1']}
        level={1}
        onChange={this.onChange}
        height={document.documentElement.clientHeight * 0.6}
      />
    );
    const loadingEl = (
      <div style={{ position: 'absolute', width: '100%', height: document.documentElement.clientHeight * 0.6, display: 'flex', justifyContent: 'center' }}>
        <ActivityIndicator size="large" />
      </div>
    );
    return (
      <div className={show ? 'single-menu-active' : ''}>
        <div>
          <NavBar
            leftContent="Menu"
            mode="light"
            onLeftClick={this.handleClick}
            className="single-top-nav-bar"
          >
            OneLevel menu
          </NavBar>
        </div>
        {show ? initData ? menuEl : loadingEl : null}
        {show ? <div className="menu-mask" onClick={this.onMaskClick} /> : null}
      </div>
    );
  }

}

export default Mobile;