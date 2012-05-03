var ws = new WebSocket('{wsurl}');
ws.onopen = function (e) {
    console.log('* Connected!');
    ws.send("JOIN{roomID}");
};
ws.onclose = function (e) {
    console.log('* Disconnected');
};
ws.onerror = function (e) {
    console.log('* Unexpected error');
};
ws.onmessage = function (aEvent) {
    var argv = aEvent.data.split(" "), argc = argv.length;
    argv.forEach(function (e, i, a) {
        a[i] = decodeURIComponent(e)
    });
    if (argv[0] === "BACK") {
        goToPreviousSlide(getCurrentSlideNumber());
    } else if (argv[0] === "FORWARD") {
        if (slideList.length > getCurrentSlideNumber() + 1) {
            goToNextSlide(getCurrentSlideNumber());
        }
    } else if (argv[0] === "START") {
        goToSlide(0);
    } else if (argv[0] === "END") {
        goToSlide(slideList.length - 1);
    } else if (argv[0] === "SET_CURSOR" && argc === 2) {
        goToSlide(argv[1]);
    }
};
//history.pushState(null, null, url.pathname + '?full' + getSlideHash(0));
//enterSlideMode();
/*var evt = document.createEvent("KeyboardEvent");
evt.initKeyEvent("keypress", true, true, window, 0, 0, 0, 0, 0, 13);
body.dispatchEvent(evt);
*/
nav= false;
window.postMessage("FULL", "*");
//goToSlide(0);