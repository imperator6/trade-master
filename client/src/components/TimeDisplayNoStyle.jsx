import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer } from "mobx-react";

@observer
class TimeDisplay extends React.Component {
  constructor(props) {
    super(props);
    this.state = { time2: this.getTime() };
  }

  componentDidMount() {
    let timer = setInterval(() => {
      this.setState({ time2: this.getTime() });
    }, 1000);

    this.setState({ timer: timer });
  }

  componentWillUnmount() {
    clearInterval(this.state.timer);
  }

  getTime = function() {
    let time = moment().format("DD.MM HH:mm:ss");
    return time;
  };

  render() {
    return (
      <Div className='timeStyle'>
        <p>{this.state.time2}</p>
      </Div>
    );
  }
}

export default TimeDisplay;
