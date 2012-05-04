var ws = null
try {
    var roomId = '{roomID}';
    ws = new WebSocket('{wsurl}');
        ws.onopen = function (e) {
            console.log('* Connected!');
            ws.send("JOIN" + roomId);
        };
        ws.onclose = function (e) {
            console.log('* Disconnected');
        };
        ws.onerror = function (e) {
            console.log('* Unexpected error');
        };
        ws.onmessage = function (aEvent) {
        };
} catch (e) {console.log(e)}
window.postMessage("FULL", "*");

// TODO set the current slides when a client joins