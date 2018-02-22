var path = require("path");
var webpack = require("webpack");
const fs  = require('fs');

require("babel-polyfill");

var node_dir = __dirname + "/node_modules";

const lessToJs = require('less-vars-to-js');
const themeVariables = lessToJs(fs.readFileSync(path.join(__dirname, './ant-theme-vars.less'), 'utf8'));

module.exports = {
  devtool: "eval",
  entry:  ["babel-polyfill", "./src/index"],
  output: {
    path: path.join(__dirname, "dist"),
    filename: "bundle.js",
    publicPath: "/static/"
  },
  plugins: [new webpack.HotModuleReplacementPlugin(),
      new webpack.DefinePlugin({
      SERVICE_URL: JSON.stringify("http://127.0.0.1:8090") 
    })],
  resolve: {
    extensions: [".js", ".jsx"],
    alias: {
      stompjs: node_dir + "/stompjs/lib/stomp.js"
    }
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx?)$/,
        use: {
          loader: 'babel-loader',
          options: {
            plugins: [
              ['import', { libraryName: "antd", style: true }]
            ]
          }
        },
        exclude: /node_modules/,
        include: path.join(__dirname, "src")
      },
   /*   {
        test: /\.css$/,
        use: [{ loader: "style-loader" }, { loader: "css-loader" }]
      }, */
      {
        test: /\.less$/,
        use: [
          {loader: "style-loader"},
          {loader: "css-loader"},
          {loader: "less-loader",
            options: {
              modifyVars: themeVariables
            }
          }
        ]
      }
    ]
  }
};
