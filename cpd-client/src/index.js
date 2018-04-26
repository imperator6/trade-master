import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import { Provider } from 'mobx-react';
import RootStore from "./models/RootStore"
import { BrowserRouter } from 'react-router-dom'

import { LocaleProvider } from 'antd';
import enUS from 'antd/lib/locale-provider/en_US';

import {LicenseManager} from "ag-grid-enterprise/main";
LicenseManager.setLicenseKey("Comparex_AG_on_behalf_of_RWE_Supply_&_Trading_GmbH_MultiApp_1Devs12_December_2018__MTU0NDU3MjgwMDAwMA==14cc54a62b72da4cf404860cfd62f62d");



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