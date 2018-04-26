import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";
import { Icon, Tooltip, Select, Divider, Button } from "antd";

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
class BotSelectWidget extends React.Component {
  
  constructor(props) {
    super(props);
    this.store = this.props.rootStore.positionStore
  }

  componentDidMount() {
    //this.store.init();
  }

  render() {

    let botOptions = this.store.botList.map(botString => {
      let bot = botString.split("_");

      return <Option key={bot[0]}>{botString}</Option>;
    });

    let botSelect = (
      <Select
        size="small"
        placeholder="Select TradeBot"
        value={this.store.selectedBot}
        onChange={newValue => {
          this.store.onBotSelected(newValue);
        }}
        style={{ width: 185 }}
      >
        {botOptions}
      </Select>
    );

    let backtestButton = null

    if(this.store.getSelectedBot() && this.store.getSelectedBot().backtest) {
         backtestButton =  ([<Divider  key="2" type="vertical" />, <Tooltip key="1" placement="top" title="Backtest Strategy">
         <Button
           icon="play-circle-o"
           shape="circle"
           onClick={() => {this.store.rootStore.strategyStore.backtestStrategy(this.store.getSelectedBot().id)}}
         />
         </Tooltip>])
    }
    

    
    return ( <div style={{ padding: 8 + "px" }}>
    {botSelect}
    
    {backtestButton}

    </div>
)
    
  }
}

export default BotSelectWidget;
