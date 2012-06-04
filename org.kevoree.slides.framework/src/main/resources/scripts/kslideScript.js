var url = window.location,
    body = document.body,
    slides = document.querySelectorAll('.slide'),
    progress = document.querySelector('div.progress div'),
    slideList = [],
    l = slides.length, i;

for (i = 0; i < l; i++) {
    // Slide ID's are optional. In case of missing ID we set it to the slide number
    if (!slides[i].id) {
        slides[i].id = i + 1;
    }
    slideList.push({
        id:slides[i].id,
        hasInnerNavigation:null !== slides[i].querySelector('.next')
    });
}

function getTransform () {
    var denominator = Math.max(
        body.clientWidth / window.innerWidth,
        body.clientHeight / window.innerHeight
    );

    return 'scale(' + (1 / denominator) + ')';
}

function applyTransform (transform) {
    body.style.WebkitTransform = transform;
    body.style.MozTransform = transform;
    body.style.msTransform = transform;
    body.style.OTransform = transform;
    body.style.transform = transform;
}

function enterSlideMode () {
    body.className = 'full';
    applyTransform(getTransform());
}

function enterListMode () {
    body.className = 'list';
    applyTransform('none');
}

function getCurrentSlideNumber () {
    var i, l = slideList.length, currentSlideId = url.hash.substr(1);

    for (i = 0; i < l; ++i) {
        if (currentSlideId === slideList[i].id) {
            return i;
        }
    }

    return -1;
}

function scrollToCurrentSlide () {
    // do nothing if currentSlideNumber is unknown (-1)
    if (-1 === getCurrentSlideNumber()) {
        return;
    }
    var currentSlide = document.getElementById(slideList[getCurrentSlideNumber()].id);

    if (null != currentSlide) {
        window.scrollTo(0, currentSlide.offsetTop);
    }
}


function isListMode () {
    return 'full' !== url.search.substr(1);
}

function normalizeSlideNumber (slideNumber) {
    if (0 > slideNumber) {
        return slideList.length - 1;
    } else if (slideList.length <= slideNumber) {
        return 0;
    } else {
        return slideNumber;
    }
}

function updateProgress (slideNumber) {
    if (null === progress) {
        return;
    }
    progress.style.width = (100 / (slideList.length - 1) * normalizeSlideNumber(slideNumber)).toFixed(2) + '%';
}

function getSlideHash (slideNumber) {
    return '#' + slideList[normalizeSlideNumber(slideNumber)].id;
}

function goToSlide (slideNumber, initialize) {

    if (-1 == slideNumber) {
        return;
    }

    if (initialize) {
        initializeInnerTransition(slideNumber);
    }
    url.hash = getSlideHash(slideNumber);

    if (!isListMode()) {
        updateProgress(slideNumber);
    }
}

function getContainingSlideId (el) {
    var node = el;
    while ('BODY' !== node.nodeName && 'HTML' !== node.nodeName) {
        if (-1 !== node.className.indexOf('slide')) {
            return node.id;
        } else {
            node = node.parentNode;
        }
    }
    return '';
}

function dispatchSingleSlideModeFromEvent (e) {
    var slideId = getContainingSlideId(e.target);
    if ('' !== slideId && isListMode()) {
        e.preventDefault();
        dispatchSingleSlideMode(slideId)
    }
}

function dispatchSingleSlideMode (slideId) {
    if ('' !== slideId && isListMode()) {
        // NOTE: we should update hash to get things work properly
        url.hash = '#' + slideId;
        history.replaceState(null, null, url.pathname + '?full#' + slideId);
        enterSlideMode();
        initializeInnerTransition(getCurrentSlideNumber());
        updateProgress(getCurrentSlideNumber());
        // used to synchronize with the display manager
        notifyCurrentSlideNumber("SET_CURSOR", getCurrentSlideNumber());
        notifyWebSocket("SET_CURSOR", getCurrentSlideNumber());
    }
}

function initializeInnerTransition (slideNumber) {
    if (slideList[slideNumber].hasInnerNavigation) {
        // fix active inner transition => may introduce overhead
        var activeNodes = document.querySelectorAll(getSlideHash(slideNumber) + ' .next');
        for (var i = 0, ii = activeNodes.length; i < ii; i++) {
            if (activeNodes[i].className.indexOf("active") != -1) {
                activeNodes[i].className = activeNodes[i].className.substring(0, activeNodes[i].className.length - " active".length);
            }
        }
        activeNodes = document.querySelectorAll(getSlideHash(slideNumber) + ' .next');
        activeNodes[0].className = activeNodes[0].className + ' active';
    }
}

function goToNextSlide (slideNumber) {
    // there is no inner navigation or it is not the slideshow view so we just go back to the next slide
    if (!slideList[slideNumber].hasInnerNavigation || url.toString().indexOf("?full#") == -1) {
        // do nothing if slideNumber is larger than the number of slides
        if (slideNumber + 1 == slideList.length) {
            return -1;
        }
        slideNumber++;
        initializeInnerTransition(slideNumber);
        goToSlide(slideNumber);
        return slideNumber
    } else {
        var activeNodes = document.querySelectorAll(getSlideHash(slideNumber) + ' .active');
        var childNodes = activeNodes[activeNodes.length - 1].getElementsByClassName('next');
        var nextNode = childNodes[0];
        if (nextNode) {
            nextNode.className = nextNode.className + ' active';
            return slideNumber;
        } else {
            nextNode = activeNodes[activeNodes.length - 1].nextElementSibling;
            var currentParentNode = getParentActiveNode(activeNodes);
            if (nextNode) {
                nextNode.className = nextNode.className + ' active';
                return slideNumber;
            } else if (currentParentNode && currentParentNode.nextElementSibling) {
                currentParentNode.nextElementSibling.className = currentParentNode.nextElementSibling.className + ' active';
                return slideNumber;
            } else {
                // there is no next inactive inner item so we just go to the next slide
                slideNumber++;
                goToSlide(slideNumber);
                return slideNumber;
            }
        }
    }
}

function getParentActiveNode (activeNodes) {
    var currentNode = activeNodes[activeNodes.length - 1];
    var i = 2;
    var isChild = false;
    while (!isChild && activeNodes.length - i >= 0) {

        var potentialParentNode = activeNodes[activeNodes.length - i];
        var childNodes = potentialParentNode.getElementsByClassName('next');
        var j = 0;
        while (j < childNodes.length && !isChild) {
            if (childNodes[j] == currentNode) {
                isChild = true;
            }
            j = j + 1;
        }
        i = i + 1;
    }
    if (isChild) {
        return potentialParentNode;
    } else {
        return null;
    }

}

function goToPreviousSlide (slideNumber) {
    // there is no inner navigation or it is not the slideshow view so we just go back to the previous slide
    if (!slideList[slideNumber].hasInnerNavigation || url.toString().indexOf("?full#") == -1) {
        // do nothing if slideNumber is smaller than 0
        if (slideNumber == 0) {
            return -1;
        }
        slideNumber--;
        // update new current slide according to innerTransition (all the inner must be displayed
        if (slideList[slideNumber].hasInnerNavigation) {
            var activeNodes = document.querySelectorAll(getSlideHash(slideNumber) + ' .next');
            for (var i = 0, ii = activeNodes.length; i < ii; i++) {
                if (activeNodes[i].className.indexOf("active") == -1) {
                    activeNodes[i].className = activeNodes[i].className + " active";
                }
            }
        }
        goToSlide(slideNumber, false);
        return slideNumber
    } else {
        activeNodes = document.querySelectorAll(getSlideHash(slideNumber) + " .active");
//        console.log(activeNodes);
        var currentNode = activeNodes[activeNodes.length - 1];
        var previousNode = currentNode.previousElementSibling;
        var currentParentNode = getParentActiveNode(activeNodes);
        console.log(currentNode);
        console.log(currentParentNode);
        if (previousNode || currentParentNode) {
            currentNode.className = currentNode.className.substring(0, currentNode.className.length - " active".length);
            return slideNumber;
        } else {
            // there is no previous active inner item so we just go back to the previous slide
            slideNumber--;
            goToSlide(slideNumber, false);
            return slideNumber
        }
    }
}

function fullscreen () {
    var html = document.querySelector('html');
    var requestFullscreen = html.requestFullscreen || html.requestFullScreen || html.mozRequestFullScreen || html.webkitRequestFullScreen;
    if (requestFullscreen) {
        requestFullscreen.apply(html);
    }
}

function back () {
    goToPreviousSlide(getCurrentSlideNumber());
    // used to synchronize with the display manager
    notifyCurrentSlideNumber("BACK");
    // used to synchronize with the websocket master
    notifyWebSocket("BACK");
}

function forward () {
    var previousSlideNumber = getCurrentSlideNumber();
    var currentSlideNumber = goToNextSlide(previousSlideNumber);
    // used to synchronize with the display manager
    notifyCurrentSlideNumber("FORWARD");
    // used to synchronize with the websocket master
    notifyWebSocket("FORWARD", previousSlideNumber, currentSlideNumber);
}

function goStart () {
    goToSlide(0);
    // used to synchronize with the display manager
    notifyCurrentSlideNumber("START");
    // used to synchronize with the websocket master
    notifyWebSocket("START");
}

function goEnd () {
    goToSlide(slideList.length - 1);
    // used to synchronize with the display manager
    notifyCurrentSlideNumber("END");
    // used to synchronize with the websocket master
    notifyWebSocket("END");
}

// Event handlers
window.addEventListener('DOMContentLoaded', function () {
    if (!isListMode()) {
        // "?full" is present without slide hash, so we should display first slide
        if (-1 === getCurrentSlideNumber()) {
            history.replaceState(null, null, url.pathname + '?full' + getSlideHash(0));
        }
        enterSlideMode();
        initializeInnerTransition(getCurrentSlideNumber());
        updateProgress(getCurrentSlideNumber());
    }
}, false);


window.addEventListener('popstate', function (e) {
    if (isListMode()) {
        enterListMode();
        scrollToCurrentSlide();
    } else {
        enterSlideMode();
    }
}, false);

window.addEventListener('resize', function (e) {
    if (!isListMode()) {
        applyTransform(getTransform());
    }
}, false);

function keyEventListener (e) {
    // Shortcut for alt, shift and meta keys
    if (e.altKey || e.ctrlKey || e.metaKey) {
        return;
    }

    var currentSlideNumber = getCurrentSlideNumber();

    switch (e.which) {
        case 13: // Enter
            if (isListMode()) {
                e.preventDefault();
                history.pushState(null, null, url.pathname + '?full' + getSlideHash(currentSlideNumber));
                enterSlideMode();
                updateProgress(currentSlideNumber);
            }
            break;

        case 27: // Esc
            if (!isListMode()) {
                e.preventDefault();
                history.pushState(null, null, url.pathname + getSlideHash(currentSlideNumber));
                enterListMode();
                scrollToCurrentSlide();
            }
            break;

        case 33: // PgUp
        case 38: // Up
        case 37: // Left
        case 72: // h
        case 75: // k
            e.preventDefault();
            back();
            break;

        case 34: // PgDown
        case 40: // Down
        case 39: // Right
        case 76: // l
        case 74: // j
            e.preventDefault();
            forward();
            break;

        case 36: // Home
            e.preventDefault();
            goStart();
            break;

        case 35: // End
            e.preventDefault();
            goEnd();
            break;

        case 9: // Tab = +1; Shift + Tab = -1
        case 32: // Space = +1; Shift + Space = -1
            e.preventDefault();
            currentSlideNumber += e.shiftKey ? -1 : 1;
            goToSlide(currentSlideNumber);
            notifyCurrentSlideNumber("SET_CURSOR", currentSlideNumber);
            notifyWebSocket("SET_CURSOR", currentSlideNumber);
            break;
        case 70: // f
            e.preventDefault();
            fullscreen();
            break;
        default:
        // Behave as usual
    }
}

var orgX, newX;
var tracking = false;
//var db = document.body;

function touchStartEvent (aEvent) {
    aEvent.preventDefault();
    tracking = true;
    orgX = aEvent.changedTouches[0].pageX;
}

function touchMoveEvent (aEvent) {
    if (!tracking) return;
    newX = aEvent.changedTouches[0].pageX;
    if (orgX - newX > 100) {
        tracking = false;
        forward();
    } else {
        if (orgX - newX < -100) {
            tracking = false;
            back();
        }
    }
}
document.addEventListener("touchstart", touchStartEvent, false);
document.addEventListener("touchmove", touchMoveEvent, false);
document.addEventListener('touchend', dispatchSingleSlideModeFromEvent, false);
document.addEventListener('click', dispatchSingleSlideModeFromEvent, false);
document.addEventListener('keydown', keyEventListener, false);

// functions that allow to interact with display script
function notifyCurrentSlideNumber (message, args) {
    if (window.opener != null) {
        postMsg(window.opener, message, args);
    }
}

function getDetails (slideNumber) {
    if (document.body.className == "full") {
        try {
            // the nth equals slideNumber+1 because the slide 0 is the first
            slideNumber++;
            var activeNodes = document.querySelectorAll(".slide:nth-of-type(" + slideNumber + ")");
            var d = activeNodes[activeNodes.length - 1].querySelector("details");
        } catch (e) {
            alert("Unable to get DOMElement.\nPlease check special characters on the id: " + getSlideHash(slideNumber))
        }
        return d ? d.innerHTML : "";
    } else {
        return "";
    }
}

function postMsg (aWin, aMsg) { // [arg0, [arg1...]]
    aMsg = [aMsg];
    for (var i = 2; i < arguments.length; i++) {
        aMsg.push(encodeURIComponent(arguments[i]));
    }
    aWin.postMessage(aMsg.join(" "), "*");
}

window.onmessage = function (aEvent) {
    var argv = aEvent.data.split(" "), argc = argv.length;
    argv.forEach(function (e, i, a) {
        a[i] = decodeURIComponent(e)
    });
    var win = aEvent.source;
    if (argv[0] === "REGISTER" && argc === 1) {
        postMsg(win, "REGISTERED", document.title, slides.length);
        postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
    } else if (argv[0] === "BACK" && argc === 1) {
        goToPreviousSlide(getCurrentSlideNumber());
        postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
    } else if (argv[0] === "FORWARD" && argc === 1) {
        if (slideList.length > getCurrentSlideNumber() + 1) {
            goToNextSlide(getCurrentSlideNumber());
            postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
        }
    } else if (argv[0] === "START" && argc === 1) {
        goToSlide(0);
        postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
    } else if (argv[0] === "END" && argc === 1) {
        goToSlide(slideList.length - 1);
        postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
    } else if (argv[0] === "SET_CURSOR" && argc === 2) {
        goToSlide(argv[1]);
        postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
    } else if (argv[0] === "GET_CURSOR" && argc === 1) {
        postMsg(win, "CURSOR", getCurrentSlideNumber());
    } else if (argv[0] === "GET_NOTES" && argc === 1) {
        postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
    } else if (argv[0] === "LIST" && argc === 1) {
        if (!isListMode()) {
            history.pushState(null, null, url.pathname + getSlideHash(getCurrentSlideNumber()));
            enterListMode();
            scrollToCurrentSlide();
        }
    } else if (argv[0] === "FULL" && argc === 1) {
        if (isListMode()) {
            var slideNumber = getCurrentSlideNumber();
            if (slideNumber == -1) {
                slideNumber = 0;
            }
            dispatchSingleSlideMode(slideList[slideNumber].id)
        }
    }
};

// function that allow to interact with WebSocket script
function notifyWebSocket (message, previousSlideNumber, currentSlideNumber) {
    try {
        if (typeof(wsMaster) != 'undefined') {
            if (message == "FORWARD" && previousSlideNumber != currentSlideNumber) {
                message = "SET_CURSOR";
                previousSlideNumber = currentSlideNumber
            }
            var aMsg = [message, previousSlideNumber];
            for (var i = 2; i < arguments.length; i++) {
                aMsg.push(encodeURIComponent(arguments[i]));
            }
            wsMaster.send(aMsg.join(" "));
        }
    } catch (e) {
    }
}

