var wsMaster = null;
try {
    var roomId = '{roomID}';
    wsMaster = new WebSocket('{wsurl}');
    wsMaster.onopen = function (e) {
        console.log('* Connected!');
        ws.send("JOIN" + roomId);
    };
    wsMaster.onclose = function (e) {
        console.log('* Disconnected');
    };
    wsMaster.onerror = function (e) {
        console.log('* Unexpected error');
    };
    wsMaster.onmessage = function (aEvent) {
    };
} catch (e) {
}
window.postMessage("FULL", "*");

// TODO set the current slides when a client joins