const webpack = require('webpack');

module.exports = function override(config) {
  // Merging the fallback configurations
  config.resolve.fallback = {
    ...config.resolve.fallback,
    process: require.resolve('process/browser'),
    crypto: require.resolve('crypto-browserify'),
    stream: require.resolve('stream-browserify'),
    vm: require.resolve('vm-browserify')  // Adding the 'vm' fallback
  };

  // Adding the ProvidePlugin for process
  config.plugins = [
    ...config.plugins,
    new webpack.ProvidePlugin({
      process: 'process/browser',
    }),
  ];

  return config;
};
