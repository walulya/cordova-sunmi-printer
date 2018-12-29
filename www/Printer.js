var exec = require('cordova/exec');

var PLUGIN_NAME = 'Printer';

var RTPrinter = {
    add: function (args, cb) {
        //exec(cb, null, PLUGIN_NAME, 'echo', [phrase]);
        exec(cb, null, "Printer", "performAdd", args);
    },
}

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'Printer', 'coolMethod', [arg0]);
};


module.exports = RTPrinter;