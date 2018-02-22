import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import { observable, action } from "mobx";
import { observer, inject } from "mobx-react";

import TimeDisplay from "./TimeDisplay";
import PeriodGroup from "./PeriodGroup";

var _ = require("lodash");


const HeaderItem = styled.div`flex: 0 1 auto;`;

@inject("rootStore")
@observer
class PriceDisplay extends React.Component {
  constructor(props) {
    super(props);

    this.store = this.props.rootStore
  }

  static propTypes = {
    //products: PropTypes.arrayOf(PropTypes.string).isRequired,
    //periodGroups: PropTypes.arrayOf(PropTypes.object).isRequired
  };
  

  render() {

    let config = this.store.cpdConfigStore.config

   // let splittedProducts = _.chunk(config.products, 6);

    return (
      <div>
        {config.periodGroups.map((p, i) => (
          <PeriodGroup
            key={p.groupName + "_" + i}
            periodGroup={p}
            products={config.products}
            store={this.props.store}
          />
        ))}
       
    </div>
    );
  }
}

export default PriceDisplay;
