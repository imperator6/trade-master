import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';

import { LocaleProvider } from 'antd';
import { Provider } from 'mobx-react';

import RootStore from "./store/RootStore"

import enUS from 'antd/lib/locale-provider/en_US';




ReactDOM.render(
  <LocaleProvider locale={enUS}>
    <Provider rootStore={new RootStore()}>
      <App />
    </Provider>
  </LocaleProvider>,
  document.getElementById('root')
);