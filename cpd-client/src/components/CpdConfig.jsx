import React from "react";

import { observer, inject } from "mobx-react";

import { Modal, Button } from 'antd';

@inject("rootStore")
@observer
class CpdConfig extends React.Component {

  constructor(props) {
    super(props);

    this.store = this.props.rootStore.cpdConfigStore;
  }

  handleClickOpen = () => {
    this.store.showDialog();
  };

  handleClose = () => {
    this.store.hideDialog();
  };

  handleOk = () => {
    this.store.applyConfigChanges();
    this.store.hideDialog();
  };

  handleProductChange = p => (event, checked) => {
    this.store.selectProduct(p, checked)
    event.preventDefault();
  };

  render() {
   /* let productCheckboxList = this.store.config_all.products.map(p => {
      return (
        <FormControlLabel
          control={
            <Checkbox
              checked={this.store.isProductSelected(p)}
              onChange={this.handleProductChange(p)}
              value={p}
            />
          }
          label={p}
        />
      );
    });
*/
    return (
      <div>
       <Modal
          title="Configuration"
          visible={this.store.show}
          onOk={this.handleOk}
          onCancel={this.handleClose}
        >
          <p>Some contents...</p>
          <p>Some contents...</p>
          <p>Some contents...</p>
          <p>Some contents...</p>
          <p>Some contents...</p>
          <p>Some contents...</p>
          <p>Some contents...</p>
          <p>Some contents...</p>
          <p>Some contents...</p>
          <p>Some contents...</p>
       
        </Modal>
      </div>
    );
  }
}

export default CpdConfig;
