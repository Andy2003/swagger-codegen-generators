var spec = require('./api.json');
var Swagger = require('swagger-client');

module.exports = function (options) {
    return Swagger({ spec, ...options });
};