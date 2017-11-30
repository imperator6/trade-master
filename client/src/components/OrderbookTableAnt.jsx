import { Table } from "antd";
import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";

import TimeDisplay from "./TimeDisplay";

@observer
class OrderbookTableAnt extends React.Component {
  buildProductColumns = product => {
    let columns = ["BidBrk", "BidQty", "BidPrc", "AscPrc", "AskQty", "AskBrk"];

    let children = [];

    columns.forEach(columnName => {
      children.push({
        title: columnName,
        dataIndex: product + "_" + columnName,
        width: "100px",
        render: (text, record, index) => {
          return text + "_" + index + "_" + record;
        }
      });
    });

    return children;
  };

  render() {
    let columnWidth = "100px";

    let productColumns = [];

    this.props.store.products.forEach(productName => {
      productColumns.push({
        title: productName,
        children: this.buildProductColumns(productName)
      });
    });

    let columns = [
      {
        title: "A",
        dataIndex: "period",
        key: "period",
        width: 100,
        fixed: "left"
      },
      ...productColumns
    ];

    let data = [];
    for (let i = 0; i < 100; i++) {
      data.push({
        key: i,
        product: "Bittrex, USDT-BTC",
        periodGroup: "Spot",
        period: "A",
        age: i + 1,
        street: "Lake Park",
        building: "C",
        number: 2035,
        companyAddress: "Lake Street 42",
        companyName: "SoftLake Co",
        gender: "M"
      });
    }

    return (
      <Table
        columns={columns}
        dataSource={data}
        bordered
        size="small"
        pagination={false}
      />
    );

    /*
    return (<Table
      columns={columns}
      dataSource={data}
      bordered
      
      pagination={false}
      scroll={{ x: '130%', y: '83%' }}
    />)*/
  }
}

export default OrderbookTableAnt;
