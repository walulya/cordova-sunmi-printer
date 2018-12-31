var exec = require('cordova/exec');

var PLUGIN_NAME = 'Printer';

var RTPrinter = {
    add: function (args, cb) {
        //exec(cb, null, PLUGIN_NAME, 'echo', [phrase]);
        exec(cb, null, PLUGIN_NAME, "performAdd", args);
    },
    setprinter: function(fnSuccess, fnError, type){
        exec(fnSuccess, fnError, PLUGIN_NAME, "printtype", [type]);
     },
    setconntype: function(fnSuccess, fnError, type){
        exec(fnSuccess, fnError, PLUGIN_NAME, "conntype", [type]);
     },
    connect: function(fnSuccess, fnError, name){
        exec(fnSuccess, fnError, PLUGIN_NAME, "connect", [name]);
     },
    init: function(fnSuccess, fnError){
        exec(fnSuccess, fnError, PLUGIN_NAME, "init", []);
     },
    list: function(fnSuccess, fnError){
        exec(fnSuccess, fnError, PLUGIN_NAME, "list", []);
     },
}

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'Printer', 'coolMethod', [arg0]);
};


module.exports = RTPrinter;