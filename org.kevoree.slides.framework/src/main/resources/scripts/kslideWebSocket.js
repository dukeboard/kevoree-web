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

document.removeEventListener('touchstart', touchStartEvent, false);
document.removeEventListener('touchmove', touchMoveEvent, false);
document.removeEventListener('touchend', dispatchSingleSlideModeFromEvent, false);
document.removeEventListener('click', dispatchSingleSlideModeFromEvent, false);
document.removeEventListener('keydown', keyEventListener, false);
window.postMessage("FULL", "*");