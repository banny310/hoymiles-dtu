process.env.TZ = 'Europe/Warsaw';

var proxy = require("node-tcp-proxy");
var util = require("util");
var winston = require('winston');
require('winston-daily-rotate-file');

var transport = new winston.transports.DailyRotateFile({
    dirname: '/src/log',
    filename: 'application-%DATE%.log',
    datePattern: 'YYYY-MM-DD',
    zippedArchive: false,
    maxSize: '20m',
    maxFiles: '14d'
});

transport.on('rotate', function(oldFilename, newFilename) {
    // do something fun
});

var logger = winston.createLogger({
    transports: [
      transport
    ],
    format: winston.format.simple(),
});

var serviceHosts = ["47.91.95.43" /* dataeu.hoymiles.com */];
var servicePorts = [10081];
var newProxy = proxy.createProxy(10081, serviceHosts, servicePorts, {
    upstream: function(context, data) {
        logger.info(util.format("< [%s] Client %s:%s sent:",
            new Date().toLocaleTimeString(),
            context.proxySocket.remoteAddress,
            context.proxySocket.remotePort));
        // do something with the data and return modified data
        decode("<", data);

        return data;
    },
    downstream: function(context, data) {
        logger.info(util.format("> [%s] Service %s:%s sent:",
            new Date().toLocaleTimeString(),
            context.serviceSocket.remoteAddress,
            context.serviceSocket.remotePort));
        // do something with the data and return modified data
        decode(">", data);

        return data;
    },
    serviceHostSelected: function(proxySocket, i) {
        logger.info(util.format("Service host %s:%s selected for client %s:%s.",
            serviceHosts[i],
            servicePorts[i],
            proxySocket.remoteAddress,
            proxySocket.remotePort));
        // use your own strategy to calculate i
        return i;
    }
});

let decode = function(prefix, data) {
    let hmHeader = data.toString('latin1', 0, 2);
    let msgId = data.readUInt16BE(2);
    let counter = data.readUInt16BE(4);
    let crc = data.readUInt16BE(6);
    let msgLen = data.readUInt16BE(8);
    let dataLength = msgLen - 10;

    logger.info(`${prefix} ${hmHeader} ${msgId} ${counter} ${crc} ${msgLen} | ` + data.toString('hex', 10, data.length));
};