var ws = new WebSocket('ws://127.0.0.1:8092/bws');
ws.onopen = function(e) {
        console.log('* Connected!');
        ws.send("DAFuck");
};
ws.onclose = function(e) {
        console.log('* Disconnected');
    };
    ws.onerror = function(e) {
        console.log('* Unexpected error');
    };
ws.onmessage = function(e) {
    alert(e.data);
};