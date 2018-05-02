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
class ConfigForm extends React.Component {
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.positionStore;

  
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
  };


  render() {
    

    return (
      <table>
        <tbody>
          <tr>
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
          </tr>
          <tr>
            <td colSpan={6}>
              <AceEditor
                mode={'javascript'}
                theme="monokai"
                width="700px"
                height="450px"
                value={this.store.selectedConfig}
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

export default ConfigForm;
