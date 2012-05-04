try {
var ws = new WebSocket('{wsurl}');
ws.onopen = function (e) {
    console.log('* Connected!');
    ws.send("JOIN{roomID}");
    document.removeEventListener('touchstart', touchStartEvent, false);
    document.removeEventListener('touchmove', touchMoveEvent, false);
    document.removeEventListener('touchend', dispatchSingleSlideModeFromEvent, false);
    document.removeEventListener('click', dispatchSingleSlideModeFromEvent, false);
    document.removeEventListener('keydown', keyEventListener, false);


    function newKeyEventListener (e) {
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
        if (slideList.length >  (+getCurrentSlideNumber() + 1)) {
            goToNextSlide(getCurrentSlideNumber());
        }
    } else if (argv[0] === "START") {
        goToSlide(0);
    } else if (argv[0] === "END") {
        goToSlide(slideList.length - 1);
    } else if (argv[0] === "SET_CURSOR") {
    goToSlide(argv[1])
        if (argv[1] != -1) {
        if (slideList[argv[1]].hasInnerNavigation) {
                    var activeNodes = document.querySelectorAll(getSlideHash(argv[1]) + ' .next');
                    for (var i = 0, ii = activeNodes.length; i < ii; i++) {
                        if (activeNodes[i].className.indexOf("active") != -1) {
                            activeNodes[i].className = activeNodes[i].className.substring(0, activeNodes[i].className.length - " active".length);
                        }
                    }
                    activeNodes = document.querySelectorAll(getSlideHash(argv[1]) + ' .next');
                    activeNodes[0].className = activeNodes[0].className + ' active';
                }
                }
    }
};
} catch (e) {}

window.postMessage("FULL", "*");

// TODO set the current slides when a client joins