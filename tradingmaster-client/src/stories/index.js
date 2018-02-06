import React from 'react';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';

import TimeDisplay from './../components/TimeDisplay'

import OrderbookModel from "./../models/OrderbookModel";
import OrderbookTable from './../components/OrderbookTable';

const orderbookModel = new OrderbookModel();

  const nextOrderBook = {
    "_id": "Bittrex, USDT-BTC_spot",
    "_rev": "26918-074a66b083df7cd76fab068c7edad3ef",
    "orderbook": {
      "market": "USDT-BTC",
      "bid": [
        {
          "broker": "btrx",
          "quantity": 2.8139776,
          "price": 9873
        },
        {
          "broker": "btrx",
          "quantity": 1,
          "price": 9872.0001
        },
        {
          "broker": "btrx",
          "quantity": 1.5297975,
          "price": 9872
        },
        {
          "broker": "btrx",
          "quantity": 0.00113978,
          "price": 9870.28484621
        },
        {
          "broker": "btrx",
          "quantity": 0.00870283,
          "price": 9862
        },
        {
          "broker": "btrx",
          "quantity": 0.05073051,
          "price": 9856
        },
        {
          "broker": "btrx",
          "quantity": 0.153747,
          "price": 9855.97844785
        },
        {
          "broker": "btrx",
          "quantity": 0.06,
          "price": 9855.5
        },
        {
          "broker": "btrx",
          "quantity": 0.40656567,
          "price": 9855.47941894
        },
        {
          "broker": "btrx",
          "quantity": 0.79359667,
          "price": 9852
        },
        {
          "broker": "btrx",
          "quantity": 0.96214349,
          "price": 9850.3375864
        },
        {
          "broker": "btrx",
          "quantity": 2.11031726,
          "price": 9850
        },
        {
          "broker": "btrx",
          "quantity": 0.00976944,
          "price": 9842.10527971
        },
        {
          "broker": "btrx",
          "quantity": 0.00257223,
          "price": 9840.69619981
        },
        {
          "broker": "btrx",
          "quantity": 0.00057338,
          "price": 9839.64
        },
        {
          "broker": "btrx",
          "quantity": 0.01,
          "price": 9839.00000001
        },
        {
          "broker": "btrx",
          "quantity": 3.6269521,
          "price": 9835.66516872
        },
        {
          "broker": "btrx",
          "quantity": 0.01001672,
          "price": 9834.18381885
        },
        {
          "broker": "btrx",
          "quantity": 0.17199216,
          "price": 9833
        },
        {
          "broker": "btrx",
          "quantity": 0.11514834,
          "price": 9830.56739737
        },
        {
          "broker": "btrx",
          "quantity": 0.41455395,
          "price": 9830.45967306
        },
        {
          "broker": "btrx",
          "quantity": 0.82090995,
          "price": 9830
        },
        {
          "broker": "btrx",
          "quantity": 0.00386414,
          "price": 9825.93515551
        },
        {
          "broker": "btrx",
          "quantity": 0.31648274,
          "price": 9825
        },
        {
          "broker": "btrx",
          "quantity": 0.09333974,
          "price": 9821
        },
        {
          "broker": "btrx",
          "quantity": 0.11481504,
          "price": 9820.00000001
        },
        {
          "broker": "btrx",
          "quantity": 0.04609375,
          "price": 9817.67600001
        },
        {
          "broker": "btrx",
          "quantity": 0.02447404,
          "price": 9816.69
        },
        {
          "broker": "btrx",
          "quantity": 0.03684359,
          "price": 9814
        },
        {
          "broker": "btrx",
          "quantity": 3.99,
          "price": 9813.26
        },
        {
          "broker": "btrx",
          "quantity": 0.24223015,
          "price": 9812.9990628
        },
        {
          "broker": "btrx",
          "quantity": 0.47569728,
          "price": 9811.72016418
        },
        {
          "broker": "btrx",
          "quantity": 0.00580491,
          "price": 9811.19625278
        },
        {
          "broker": "btrx",
          "quantity": 1.30373115,
          "price": 9811
        },
        {
          "broker": "btrx",
          "quantity": 1.17351678,
          "price": 9810.1
        },
        {
          "broker": "btrx",
          "quantity": 0.51627071,
          "price": 9810
        },
        {
          "broker": "btrx",
          "quantity": 0.01017131,
          "price": 9807
        },
        {
          "broker": "btrx",
          "quantity": 0.05422662,
          "price": 9805.00000002
        },
        {
          "broker": "btrx",
          "quantity": 0.00043379,
          "price": 9803.368608
        },
        {
          "broker": "btrx",
          "quantity": 0.00432264,
          "price": 9803.20388933
        },
        {
          "broker": "btrx",
          "quantity": 0.07877982,
          "price": 9803
        },
        {
          "broker": "btrx",
          "quantity": 0.2378056,
          "price": 9802.01925586
        },
        {
          "broker": "btrx",
          "quantity": 0.0020353,
          "price": 9802
        },
        {
          "broker": "btrx",
          "quantity": 0.00516014,
          "price": 9801.4059
        },
        {
          "broker": "btrx",
          "quantity": 0.00233145,
          "price": 9801.01
        },
        {
          "broker": "btrx",
          "quantity": 0.2,
          "price": 9801
        },
        {
          "broker": "btrx",
          "quantity": 0.06655768,
          "price": 9800.20305247
        },
        {
          "broker": "btrx",
          "quantity": 0.42744502,
          "price": 9800.16511324
        },
        {
          "broker": "btrx",
          "quantity": 0.01937403,
          "price": 9800.10002375
        },
        {
          "broker": "btrx",
          "quantity": 2.00402623,
          "price": 9800.002
        }
      ],
      "ask": [
        {
          "broker": "btrx",
          "quantity": 0.11930879,
          "price": 9885
        },
        {
          "broker": "btrx",
          "quantity": 0.07233001,
          "price": 9886.05397942
        },
        {
          "broker": "btrx",
          "quantity": 2.38518279,
          "price": 9900
        },
        {
          "broker": "btrx",
          "quantity": 0.003,
          "price": 9910
        },
        {
          "broker": "btrx",
          "quantity": 0.15100213,
          "price": 9910.52524512
        },
        {
          "broker": "btrx",
          "quantity": 0.08712945,
          "price": 9910.90033204
        },
        {
          "broker": "btrx",
          "quantity": 1,
          "price": 9911
        },
        {
          "broker": "btrx",
          "quantity": 0.02506436,
          "price": 9918
        },
        {
          "broker": "btrx",
          "quantity": 1.2762137,
          "price": 9918.88
        },
        {
          "broker": "btrx",
          "quantity": 0.02059303,
          "price": 9919
        },
        {
          "broker": "btrx",
          "quantity": 0.02124067,
          "price": 9919.62032618
        },
        {
          "broker": "btrx",
          "quantity": 0.86405038,
          "price": 9919.8020628
        },
        {
          "broker": "btrx",
          "quantity": 0.82846954,
          "price": 9919.99
        },
        {
          "broker": "btrx",
          "quantity": 1.96061096,
          "price": 9919.9999999
        },
        {
          "broker": "btrx",
          "quantity": 23.94206453,
          "price": 9920
        },
        {
          "broker": "btrx",
          "quantity": 0.02610089,
          "price": 9920.00000001
        },
        {
          "broker": "btrx",
          "quantity": 0.06567431,
          "price": 9920.05397942
        },
        {
          "broker": "btrx",
          "quantity": 0.07151669,
          "price": 9920.95100001
        },
        {
          "broker": "btrx",
          "quantity": 0.27915455,
          "price": 9921.00000001
        },
        {
          "broker": "btrx",
          "quantity": 0.1232952,
          "price": 9921.24058032
        },
        {
          "broker": "btrx",
          "quantity": 0.00303222,
          "price": 9922.00000002
        },
        {
          "broker": "btrx",
          "quantity": 0.05,
          "price": 9922.206
        },
        {
          "broker": "btrx",
          "quantity": 0.86340239,
          "price": 9924.2
        },
        {
          "broker": "btrx",
          "quantity": 1,
          "price": 9924.24
        },
        {
          "broker": "btrx",
          "quantity": 0.0253287,
          "price": 9924.3144
        },
        {
          "broker": "btrx",
          "quantity": 8.81962334,
          "price": 9925
        },
        {
          "broker": "btrx",
          "quantity": 0.005,
          "price": 9925.5
        },
        {
          "broker": "btrx",
          "quantity": 0.78,
          "price": 9926
        },
        {
          "broker": "btrx",
          "quantity": 0.08350378,
          "price": 9926.99999996
        },
        {
          "broker": "btrx",
          "quantity": 0.00693597,
          "price": 9926.99999998
        },
        {
          "broker": "btrx",
          "quantity": 0.00104402,
          "price": 9927
        },
        {
          "broker": "btrx",
          "quantity": 0.0449174,
          "price": 9927.6310568
        },
        {
          "broker": "btrx",
          "quantity": 0.00102239,
          "price": 9927.71500001
        },
        {
          "broker": "btrx",
          "quantity": 0.11541767,
          "price": 9928.5
        },
        {
          "broker": "btrx",
          "quantity": 0.25779359,
          "price": 9929
        },
        {
          "broker": "btrx",
          "quantity": 0.00139932,
          "price": 9929.3
        },
        {
          "broker": "btrx",
          "quantity": 0.07,
          "price": 9929.9
        },
        {
          "broker": "btrx",
          "quantity": 6.58986752,
          "price": 9930
        },
        {
          "broker": "btrx",
          "quantity": 0.23115795,
          "price": 9930.00000001
        },
        {
          "broker": "btrx",
          "quantity": 1.2,
          "price": 9930.00000002
        },
        {
          "broker": "btrx",
          "quantity": 0.00193864,
          "price": 9930.04000001
        },
        {
          "broker": "btrx",
          "quantity": 0.09315422,
          "price": 9930.18381885
        },
        {
          "broker": "btrx",
          "quantity": 0.00612028,
          "price": 9930.2
        },
        {
          "broker": "btrx",
          "quantity": 0.02003787,
          "price": 9930.25
        },
        {
          "broker": "btrx",
          "quantity": 0.08076536,
          "price": 9931
        },
        {
          "broker": "btrx",
          "quantity": 0.00510977,
          "price": 9931.94077489
        },
        {
          "broker": "btrx",
          "quantity": 0.5044889,
          "price": 9931.99999999
        },
        {
          "broker": "btrx",
          "quantity": 11.773256,
          "price": 9932
        },
        {
          "broker": "btrx",
          "quantity": 0.0004096,
          "price": 9932.58679999
        },
        {
          "broker": "btrx",
          "quantity": 0.08023775,
          "price": 9933
        }
      ]
    },
    "type": "crypto"
  }

  orderbookModel.orderbookMap.set('USDT-BTC_spot', nextOrderBook.orderbook);


storiesOf('TimeDisplay', module)
  .add('show', () => (
    <TimeDisplay />
  ));

  

  storiesOf('OrderbookTable', module)
  .add('withHeader', () => (
    <OrderbookTable store={orderbookModel} product={'USDT-BTC'} period={'spot'} showHeader={true}/>
  )).add('noHeader', () => (
    <OrderbookTable store={orderbookModel} product={'USDT-BTC'} period={'spot'} showHeader={false}/>
  ));