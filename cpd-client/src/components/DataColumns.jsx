import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
//import ThemeProvider from 'styled-components';
import { observer } from "mobx-react";
import accounting from "accounting";
import { TD } from "./OrderbookTable";

@observer
class DataColumns extends React.Component {

  constructor(props) {
    super(props)
  }

  render() {
    let columns = [];

    let bcolor = '#6666ff'

    let data = this.props.data
    if (data) {
      if (this.props.reverse) {
        columns.push(<TD key="broker" style={{ backgroundColor: (data.active ? bcolor : '') }}>{data.broker}</TD>);
        columns.push(
          <TD key="quantity" style={{ backgroundColor: (data.active ? bcolor : '') }}>{accounting.toFixed(data.quantity, 2)}</TD>
        );
        columns.push(
          <TD key="price" style={{ backgroundColor: (data.active ? bcolor : '') }}>
            {accounting.formatMoney(data.price, {
              format: "%s %v",
              symbol: "",
              decimal: ",",
              thousand: ".",
              precision: 2
            })}
          </TD>
        );
      } else {
        columns.push(
          <TD key="price" style={{ backgroundColor: (data.active ? bcolor : '') }}>
            {accounting.formatMoney(data.price, {
              format: "%v %s",
              symbol: "",
              decimal: ",",
              thousand: ".",
              precision: 2
            })}
          </TD>
        );
        columns.push(
          <TD key="quantity" style={{ backgroundColor: (data.active ? bcolor : '') }}>{accounting.toFixed(data.quantity, 2)}</TD>
        );
        columns.push(<TD key="broker" style={{ backgroundColor: (data.active ? bcolor : '') }}>{data.broker}</TD>);
      }
    } else {
      columns.push(<TD key="broker" />);
      columns.push(<TD key="quantity" />);
      columns.push(<TD key="price" />);
    }

    

    return columns.reverse();
  }
}

export default DataColumns;
