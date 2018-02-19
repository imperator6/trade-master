import * as loglevel from 'loglevel'

// https://github.com/pimterry/loglevel

let prod = false;

if (!prod) {
  loglevel.setLevel('debug')
} else {
  loglevel.setLevel('error')
}

loglevel.getLogger('StompStore').setLevel("debug")

loglevel.getLogger('MarketSelectionStore').setLevel("debug")



export default loglevel