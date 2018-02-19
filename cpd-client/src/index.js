import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import { Provider } from 'mobx-react';
import RootStore from "./models/RootStore"

import { LocaleProvider } from 'antd';
import enUS from 'antd/lib/locale-provider/en_US';


ReactDOM.render(
  <LocaleProvider locale={enUS}>
    <Provider rootStore={new RootStore()}>
        <App />
    </Provider>
  </LocaleProvider>,
  document.getElementById('root')
);