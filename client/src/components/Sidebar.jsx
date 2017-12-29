import React from "react";
import { withRouter } from 'react-router'
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer } from "mobx-react";

import { Link } from "react-router-dom";

import { Layout, Menu, Breadcrumb, Icon } from "antd";
const { Header, Content, Footer, Sider } = Layout;
const SubMenu = Menu.SubMenu;

const Logo = styled.div`
  height: 32px;
  background: rgba(255, 255, 255, 0.2);
  margin: 16px;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 2px dashed #1890ff;
`;



@observer
class Sidebar extends React.Component {

   

  constructor(props) {
    super(props);

    this.state = {
      collapsed: true,
      title: "T M"
    };
  }

  onCollapse = collapsed => {
    console.log(collapsed);

    if (collapsed) {
      this.setState({ title: "T M" });
    } else {
      this.setState({ title: "Trading Master" });
    }

    this.setState({ collapsed });
  };

  render() {


     console.log(this.props.location)
    return (
      <Sider
        collapsible
        collapsed={this.state.collapsed}
        onCollapse={this.onCollapse}
      >
        <Logo>{this.state.title}</Logo>,
        <Menu theme="dark" defaultSelectedKeys={[this.props.location.pathname]} mode="inline">
          <Menu.Item key="/cpd">
            <Link to="/cpd">
              <Icon type="table" />
              <span>CPD</span>
            </Link>
          </Menu.Item>
          <Menu.Item key="/strategy">
            <Link to="/strategy">
              <Icon type="line-chart" />
              <span>Strategy Runner</span>{" "}
            </Link>
          </Menu.Item>
          <Menu.Item key="/login">
            <Link to="/login">
              <Icon type="user" />
              <span>My Account</span>{" "}
            </Link>
          </Menu.Item>
          <SubMenu
            key="sub2"
            title={
              <span>
                <Icon type="team" />
                <span>Team</span>
              </span>
            }
          >
            <Menu.Item key="6">Team 1</Menu.Item>
            <Menu.Item key="8">Team 2</Menu.Item>
          </SubMenu>
          <Menu.Item key="9">
            <Icon type="file" />
            <span>File</span>
          </Menu.Item>
        </Menu>
      </Sider>
    );
  }
}

export default withRouter(Sidebar);
