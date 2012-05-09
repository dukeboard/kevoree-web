try {
    var wsClient = new WebSocket('{wsurl}');
    wsClient.onopen = function (e) {
    console.log('* Connected!');
    wsClient.send("JOIN{roomID}");
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
    wsClient.onclose = function (e) {
        console.log('* Disconnected');
    };
    wsClient.onerror = function (e) {
        console.log('* Unexpected error');
    };
    wsClient.onmessage = function (aEvent) {
        var argv = aEvent.data.split(" ");
        argv.forEach(function (e, i, a) {
            a[i] = decodeURIComponent(e)
        });
        if (argv[0] === "BACK") {
            back();
        } else if (argv[0] === "FORWARD") {
            forward();
        } else if (argv[0] === "START") {
            goToSlide(0);
        } else if (argv[0] === "END") {
            goToSlide(slideList.length - 1);
        } else if (argv[0] === "SET_CURSOR") {
            goToSlide(argv[1]);
            if (argv[1] != -1) {
                initializeInnerTransition(argv[1]);
            }
        }
    };
} catch (e) {
}

window.postMessage("FULL", "*");

// TODO set the current slides when a client joins