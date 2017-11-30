import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import { observable, action } from "mobx";
import { observer } from "mobx-react";

import TimeDisplay from "./TimeDisplay";
import PeriodGroup from "./PeriodGroup";

const Row = styled.div`
  display: felx;
  flex-direction: row;
`;

const HeaderItem = styled.div`flex: 0 1 auto;`;
@observer
class PriceDisplay extends React.Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
    //products: PropTypes.arrayOf(PropTypes.string).isRequired,
    //periodGroups: PropTypes.arrayOf(PropTypes.object).isRequired
  };

  render() {
    return (
      <div>
        {this.props.store.config.periodGroups.map((p, i) => (
          <PeriodGroup
            key={p.groupName + "_" + i}
            periodGroup={p}
            products={this.props.store.config.products}
            store={this.props.store}
          />
        ))}
      </div>
    );
  }
}

export default PriceDisplay;
