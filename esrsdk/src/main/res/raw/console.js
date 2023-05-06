console = {
    log: function(message) {
        JavaLogger('log', message)
    },
    debug: function(message) {
        JavaLogger('debug', message)
    },
    warn: function(messsage) {
        JavaLogger('warn', message)
    },
    error: function(message) {
        JavaLogger('error', message)
    },
    trace: function() {
    },
    dir: function(obj) {
        JavaLogger('log', 'DIR: ' + JSON.stringify(obj, null, 4))
    }
}