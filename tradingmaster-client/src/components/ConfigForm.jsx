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

  saveScript = (callback) => {
    let newScript = this.refs.strategyEditor.editor.getValue();
    this.store.saveConfig(newScript, callback);
  };

  startBacktest = () => {

    let cb = () => {

      let botId = this.store.getSelectedBot().id

     

      this.store.rootStore.strategyStore.backtestStrategy(botId)

      // reload bot list to refresh config as backtest start/end dates hav changed
      setTimeout(() => { 
          this.store.selectedConfig = ""
          this.store.loadBotList( () => {
            this.store.onBotSelected(botId)
          }) 
      },2000)
   }

   this.saveScript(cb)

  }

  useBotDates = () => {

    let bot = this.store.getSelectedBot()
     //if(bot.config.backtest.startDate != null)
     this.store.rootStore.marketSelectionStore.startDate = moment(bot.config.backtest.startDate)

     //if(bot.config.backtest.endDate != null)
       this.store.rootStore.marketSelectionStore.endDate = moment(bot.config.backtest.endDate)

  }

  refreshConfig() {

    let botId = this.store.getSelectedBot().id

    this.store.selectedConfig = ""
        this.store.loadBotList( () => {
          this.store.onBotSelected(botId)
        }) 

  }


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
                  onClick={() => {this.refreshConfig()}}
                />
              </Tooltip>
            </th>
            <th>
              <Tooltip placement="top" title="Clone Config">
                <Button  icon="copy" onClick={() => {this.store.cloneBot()}} />
              </Tooltip>
            </th>
            <th>
              <Tooltip placement="top" title="Save Config">
                <Button icon="save" onClick={this.saveScript} />
              </Tooltip>
            </th>
            <th>
              <Tooltip placement="top" title="Set Dates from config to selection">
                <Button
                  icon="clock-circle"
                  shape="circle"
                  onClick={() => {this.useBotDates()}}
                />
              </Tooltip>
            </th>
            <th>
              <Tooltip placement="top" title="Backtest Strategy">
                <Button
                  icon="play-circle-o"
                  shape="circle"
                  onClick={() => {this.startBacktest()}}
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
