import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";
import Period from "./Period";
import { observer } from "mobx-react";
//import ThemeProvider from 'styled-components';

const Outer = styled.div`
  display: flex;
  flex-direction: column;
`;

const Row = styled.div`
  display: flex;
  flex-direction: row;
`;

const PeriodGroup = styled.div`
  flex: 0 1 auto;
  color: #ffffff;
  background-color: #212121;
  padding: 2px;
  width: 98px;
`;

@observer
class PeriodGrop extends React.Component {
  static propTypes = {
    // products: PropTypes.arrayOf(PropTypes.string).isRequired,
    // periodGroup: PropTypes.array.isRequired
  };

  constructor(props) {
    super(props);
  }

  render() {
    let rows = [];

    this.props.periodGroup.periods.map((p, i) =>
      rows.push(
        <Row key={this.props.periodGroup.groupName + "_" + i + "_" + p}>
          <Period
            periodGroup={this.props.periodGroup}
            products={this.props.products}
            period={p}
            store={this.props.store}
            showHeader={i == 0}
            showTitle={"Month" === this.props.periodGroup.groupName}
          />
        </Row>
      )
    );

    return rows;
  }
}

export default PeriodGrop;
