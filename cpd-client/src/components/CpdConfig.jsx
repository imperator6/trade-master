import React from "react";

import { observable, observer, inject} from "mobx-react";

import { Modal, Button, Transfer } from 'antd';

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

  handleChange = (nextTargetKeys, direction, moveKeys) => {
    this.store.targetKeys = nextTargetKeys

    console.log('targetKeys: ', nextTargetKeys);
    console.log('direction: ', direction);
    console.log('moveKeys: ', moveKeys);
  }

  handleSelectChange = (sourceSelectedKeys, targetSelectedKeys) => {
    this.store.selectedKeys = [...sourceSelectedKeys, ...targetSelectedKeys]

    console.log('sourceSelectedKeys: ', sourceSelectedKeys);
    console.log('targetSelectedKeys: ', targetSelectedKeys);
  }

  handleScroll = (direction, e) => {
    console.log('direction:', direction);
    console.log('target:', e.target);
  }


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
          okType={'primary'}
          onCancel={this.handleClose}
        >
          <p>Products</p>

         <Transfer
        dataSource={this.store.productList}
        titles={['Source', 'Target']}
        targetKeys={this.store.targetKeys}
        selectedKeys={this.store.selectedKeys}
        onChange={this.handleChange}
        onSelectChange={this.handleSelectChange}
        onScroll={this.handleScroll}
        render={item => item.key}
      />
       
        </Modal>
      </div>
    );
  }
}

export default CpdConfig;
