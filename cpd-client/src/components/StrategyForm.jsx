import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";

import AceEditor from "react-ace";
import brace from "brace";

import "brace/mode/javascript";
// import "brace/mode/json";
import "brace/theme/monokai";

import {
  Row,
  Col,
  Tabs,
  Form,
  Icon,
  Input,
  Button,
  Select,
  Tooltip
} from "antd";
const TabPane = Tabs.TabPane;
const FormItem = Form.Item;
const Option = Select.Option;

const OuterDiv = styled.div`
  display: felx;
  align-items: center;
  justify-content: center;
  border: 1px solid #424242;
  background-color: red;
  width: 100px;
  height: 37px;
`;

@inject("rootStore")
@observer
class StrategyForm extends React.Component {
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.strategyStore;

    this.state = {
      editStrategyName: false
    };
  }

  componentDidMount() {
    this.store.loadStrategies();
  }

  componentDidUpdate() {
    if (this.state.editStrategyName) {
      this.strategyNameInput.focus();
    }
  }

  onScriptChange = newScript => {
    //console.log(this.refs.strategyEditor);
    //console.log(this.refs.strategyEditor);
    //this.script = newScript;
  };

  handleStrategyChange = value => {
    this.store.selectStrategyByName(value);
  };

  saveScript = () => {
    let newScript = this.refs.strategyEditor.editor.getValue();
    this.store.saveScript(newScript);
    this.setState({ editStrategyName: false });
  };

  loadStrategies = () => {
    this.store.loadStrategies();
    this.setState({ editStrategyName: false });
  };

  updateStrategyName = e => {
    e.stopPropagation();
    console.log(e.target.value);
    this.store.selectedStrategy.name = e.target.value;
    this.setState({ editStrategyName: !this.state.editStrategyName });
  };

  onStrategyNameChange = event => {
    this.store.selectedStrategy.name = newName;
  };

  toggleEditStrategyName = () => {
    let next = !this.state.editStrategyName;
    this.setState({ editStrategyName: next });
  };

  newStrategy = () => {
    this.store.newStrategy();
    this.setState({ editStrategyName: true });
  };

  render() {
    const strategyOptions = this.store.strategyList.map(s => {
      return (
        <Option value={s.name} key={s.name}>
          {s.name}
        </Option>
      );
    });

    let selStrategy = null;
    let scriptValue = "";
    if (this.store.selectedStrategy != null) {
      let s = this.store.selectedStrategy;
      selStrategy = s.name;
      scriptValue = this.store.selectedStrategy.script;
    }

    //console.log(selStrategy);
    //console.log(strategyOptions);

    let strategyComp = (
      <Select
        value={selStrategy}
        style={{ width: 220 }}
        onChange={this.handleStrategyChange}
      >
        {strategyOptions}
      </Select>
    );

    if (this.state.editStrategyName) {
      strategyComp = (
        <Input
          style={{ width: 220 }}
          defaultValue={selStrategy}
          placeholder="Enter Strategy Name"
          // onChange={this.updateStrategyName}
          onPressEnter={this.updateStrategyName}
          ref={node => (this.strategyNameInput = node)}
        />
      );
    }

    return (
      <table>
        <tbody>
          <tr>
            <th>Strategy</th>
            <td>
              {strategyComp}
              <Select
                value={this.store.selectedScriptType}
                style={{ width: 100 }}
                onChange={(newValue) => { 
                  this.store.selectedScriptType = newValue 
                  this.store.selectedStrategy.language = newValue
                }}
              >
                {this.store.scriptTypes.map(p => {
              return (
                <Option key={p} value={p}>
                  {p}
                </Option>
              );
            })}
              </Select>
              <Tooltip placement="top" title="Edit Strategy Name">
                <Button
                  shape="circle"
                  icon="edit"
                  onClick={this.toggleEditStrategyName}
                />
              </Tooltip>
              <Tooltip placement="top" title="Add a new Strategy">
                <Button icon="plus" shape="circle" onClick={this.newStrategy} />
              </Tooltip>
            </td>
            <th>
              <Tooltip placement="top" title="Reload Strategies from DB">
                <Button
                  shape="circle"
                  icon="reload"
                  onClick={this.loadStrategies}
                />
              </Tooltip>
            </th>
            <th>
              <Tooltip placement="top" title="Save Strategy">
                <Button type="primary" icon="save" onClick={this.saveScript} />
              </Tooltip>
            </th>
            <th>
              <Tooltip placement="top" title="Backtest Strategy">
                <Button
                  icon="play-circle-o"
                  shape="circle"
                  onClick={this.store.backtestStrategy}
                />
              </Tooltip>
            </th>
            <th>
              <Tooltip placement="top" title="Run Strategy">
                <Button
                  type="primary"
                  icon="play-circle"
                  shape="circle"
                  onClick={this.store.runStrategy}
                />
              </Tooltip>
            </th>
          </tr>
          <tr>
            <td colSpan={6}>
              <AceEditor
                mode={'javascript'}
                theme="monokai"
                width="700px"
                height="450px"
                value={scriptValue}
                onChange={this.onScriptChange}
                name="strategyEditor"
                editorProps={{
                  $blockScrolling: true
                }}
                ref="strategyEditor"
              />
            </td>
          </tr>
        </tbody>
      </table>
    );
  }
}

export default StrategyForm;
