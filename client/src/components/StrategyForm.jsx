import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";

import AceEditor from "react-ace";
import brace from "brace";

import "brace/mode/javascript";
import "brace/theme/monokai";

import { Row, Col, Tabs, Form, Icon, Input, Button, Select } from "antd";
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
  }

  componentDidMount() {
    this.store.loadStrategies()
  }

  onScriptChange = newScript => {
    //console.log(this.refs.strategyEditor);

    console.log(this.refs.strategyEditor);

    console.log(newScript);

    

    this.script = newScript;
  };

   handleStrategyChange = (value) => {
    this.store.selectStrategyByName(value)
   }

   saveScript = () => {
      this.store.saveScript(this.script)
   }


  render() {
    const strategyOptions = this.store.strategyList.map(s => {
        return (<Option value={s.name} key={s.name} >{ s.name }</Option>)
    });

    let selStrategy = null;
    if(this.store.selectedStrategy != null) {
        let s = this.store.selectedStrategy
        selStrategy = s.name 
    }

    console.log(selStrategy);
    console.log(strategyOptions);

    return [
        <table>
          <tbody>
            <tr>
              <th>Strategy</th>
              <td> <Select  
                  value={selStrategy}
                  style={{ width: 220 }}
                  onChange={this.handleStrategyChange}
                >
                {strategyOptions}
                </Select>
              </td>
              <th><Button
                  type="primary"
                  onClick={this.store.loadStrategies}
                >Load</Button></th>
              <th></th>
              <th></th>
              <th><Button
                  type="primary"
                  icon="plus"
                  onClick={this.store.load}
                >New</Button>
                <Button
                  type="primary"
                  onClick={this.saveScript}
                >Save</Button></th>
            </tr>
          </tbody>
          </table>,
        <AceEditor
          mode="javascript"
          theme="monokai"
          width="700px"
          height="450px"
          value={this.store.script}
          onChange={this.onScriptChange}
          name="strategyEditor"
          editorProps={{
            $blockScrolling: true
          }}
          ref="strategyEditor"
        />
      
    ];
  }
}

export default StrategyForm;
