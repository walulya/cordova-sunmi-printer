var exec = require('cordova/exec');

var PLUGIN_NAME = 'Printer';

var RTPrinter = {
    add: function (args, cb) {
        //exec(cb, null, PLUGIN_NAME, 'echo', [phrase]);
        exec(cb, null, PLUGIN_NAME, "performAdd", args);
    },
    list: function(fnSuccess, fnError){
        exec(fnSuccess, fnError, PLUGIN_NAME, "list", []);
     },
}

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'Printer', 'coolMethod', [arg0]);
};


module.exports = RTPrinter;