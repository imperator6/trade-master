import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import { BrowserRouter } from 'react-router-dom'

import { LocaleProvider } from 'antd';
import { Provider } from 'mobx-react';

import RootStore from "./store/RootStore"

import enUS from 'antd/lib/locale-provider/en_US';

import styles from './myStyles.css';

import "babel-polyfill"




ReactDOM.render(
  <BrowserRouter>
    <LocaleProvider locale={enUS}>
      <Provider rootStore={new RootStore()}>
        <App />
      </Provider>
    </LocaleProvider>
  </BrowserRouter>,
  document.getElementById('root')
);