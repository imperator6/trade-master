import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
import OrderbookTable from "./OrderbookTable";
import { observer } from "mobx-react";
//import ThemeProvider from 'styled-components';

@observer
class Period extends React.Component {
  static propTypes = {
    products: PropTypes.object.isRequired,
    period: PropTypes.string.isRequired,
    showHeader: PropTypes.bool.isRequired,
    showTitle: PropTypes.bool.isRequired
  };

  constructor(props) {
    super(props);
  }

  render() {
    let elements = [];

    this.props.products.map((p, i) =>
      elements.push(
        <OrderbookTable
          key={i}
          periodGroup={this.props.periodGroup}
          product={p}
          period={this.props.period}
          store={this.props.store}
          showHeader={this.props.showHeader}
          showPeriod={i == 0}
          showTitle={this.props.showTitle}
        />
      )
    );

    return elements;
  }
}

export default Period;
