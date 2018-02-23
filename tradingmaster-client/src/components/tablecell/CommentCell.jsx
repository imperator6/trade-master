import React from "react";
import styled from "styled-components";
import PropTypes from "prop-types";
import moment from "moment";

//import ThemeProvider from 'styled-components';
import { observer, inject } from "mobx-react";
import CryptoCell from "./CryptoCell";

import { Input, Icon } from "antd";
const { TextArea } = Input;

@inject("rootStore")
@observer
class CommentCell extends React.Component {
  constructor(props) {
    super(props);

    this.store = this.props.rootStore.positionStore;
    this.settingsStore = this.props.rootStore.positionSettingsStore;

    this.state = { editMode: false, comment: '' };
  }

  switchEditMode = () => {
    let position = this.store.positionMap.get(this.props.position.id);
  
    super.setState({ editMode: !this.state.editMode, comment: position.comment});
  };

  applyComment = (newComment) => {
    let position = this.store.positionMap.get(this.props.position.id);
    position.comment = this.state.comment

    this.store.savePosition(position)

    this.switchEditMode()
  };

  onChangeComment = (e) => {
    this.setState({ comment: e.target.value });
  }

  render() {
    let position = this.store.positionMap.get(this.props.position.id);

    let result = null;

    if (this.state.editMode) {
      result = (
          <table>
            <tr>
              <td rowSpan="2">
                <TextArea rows={2} value={this.state.comment} onChange={this.onChangeComment} />
              </td>
              <td >
              <Icon type="close" onClick={this.switchEditMode} />
              </td>
            </tr>
            <tr>
              <td >
              <Icon type="check" onClick={this.applyComment}/>
              </td>
            </tr>
          </table> 
      );
    } else {
      result = <span onClick={this.switchEditMode}>{position.comment}</span>;
    }

    return result;
  }
}

export default CommentCell;
