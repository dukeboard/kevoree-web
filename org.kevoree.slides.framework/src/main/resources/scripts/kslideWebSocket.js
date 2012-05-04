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
    console.log("BACK")
        goToPreviousSlide(getCurrentSlideNumber());
    } else if (argv[0] === "FORWARD") {
    console.log("FORWARD")
        if (slideList.length > getCurrentSlideNumber() + 1) {
            goToNextSlide(getCurrentSlideNumber());
        }
    } else if (argv[0] === "START") {
    console.log("START")
        goToSlide(0);
    } else if (argv[0] === "END") {
    console.log("END")
        goToSlide(slideList.length - 1);
    } else if (argv[0] === "SET_CURSOR" && argc === 2) {
        console.log("SET_CURSOR " + argv[1])
        goToSlide(argv[1]);
    }
};

document.removeEventListener('touchstart', touchStartEvent, false);
document.removeEventListener('touchmove', touchMoveEvent, false);
document.removeEventListener('touchend', dispatchSingleSlideModeFromEvent, false);
document.removeEventListener('click', dispatchSingleSlideModeFromEvent, false);
document.removeEventListener('keydown', keyEventListener, false);


function newKeyEventListener (e) {
//    if (!nav) {return;}
    // Shortcut for alt, shift and meta keys
    if (e.altKey || e.ctrlKey || e.metaKey) {
        return;
    }

    var currentSlideNumber = getCurrentSlideNumber();

    switch (e.which) {
        case 70: // f
            e.preventDefault();
            fullscreen();
            break;
        default:
        // Behave as usual
    }
}
document.addEventListener('keydown', newKeyEventListener, false);

window.postMessage("FULL", "*");

// TODO set the current slides when a client joins