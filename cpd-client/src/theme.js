import darkBaseTheme from "material-ui/styles/baseThemes/darkBaseTheme";
import merge from "lodash.merge";

const colors = require("material-ui/styles/colors");

const muiTheme = {
  palette: {
    textColor: colors.grey200,
    primary1Color: "#ffce00",
    accent1Color: colors.redA200,
    accent2Color: colors.redA400,
    accent3Color: colors.redA100
  },
  table: {
    height: "calc(100vh - 122px)"
  },
  tableHeaderColumn: {
    fontSize: "14px"
  }
};

const theme = merge(darkBaseTheme, muiTheme);
export default theme;
