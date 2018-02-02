var path = require("path");
var webpack = require("webpack");
require("babel-polyfill");

var node_dir = __dirname + "/node_modules";

module.exports = {
  devtool: "eval",
  entry:  ["babel-polyfill", "./src/index"],
  output: {
    path: path.join(__dirname, "../server/src/main/resources/public/js"),
    filename: "bundle.js",
    publicPath: "/static/"
  },
  plugins: [new webpack.HotModuleReplacementPlugin(),
    new webpack.DefinePlugin({
      SERVICE_URL: JSON.stringify("")  // don't use any api roor as the app is hosted directly in the spring container!
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
           
          }
        },
        exclude: /node_modules/,
        include: path.join(__dirname, "src")
      },
      {
        test: /\.css$/,
        use: [{ loader: "style-loader" }, { loader: "css-loader" }]
      }
    ]
  }
};
