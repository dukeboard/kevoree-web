(function () {
    var url = window.location,
        body = document.body,
        slides = document.querySelectorAll('div.slide'),
        progress = document.querySelector('div.progress div'),
        slideList = [],
        l = slides.length, i;

    for (i = 0; i < l; i++) {
        slideList.push({
            id:slides[i].id,
            hasInnerNavigation:null !== slides[i].querySelector('.inner')
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

        /*if (url.hash.substr(1, url.hash.indexOf("%")) != -1) {
         currentSlideId = url.hash.substr(1, url.hash.indexOf("%") - 1)
         }*/

        for (i = 0; i < l; ++i) {
            if (currentSlideId === slideList[i].id) {
                return i;
            }
        }

        return -1;
    }

    function scrollToCurrentSlide () {
        var currentSlide = document.getElementById(
            slideList[getCurrentSlideNumber()].id
        );

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

    function goToSlide (slideNumber) {
        url.hash = getSlideHash(slideNumber);

        if (!isListMode()) {
            updateProgress(slideNumber);
        }
        // used to synchronize with the display manager
        notifyCurrentSlideNumber()
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

    function dispatchSingleSlideMode (e) {
        var slideId = getContainingSlideId(e.target);

        if ('' !== slideId && isListMode()) {
            e.preventDefault();

            // NOTE: we should update hash to get things work properly
            url.hash = '#' + slideId;
            history.replaceState(null, null, url.pathname + '?full#' + slideId);
            enterSlideMode();

            updateProgress(getCurrentSlideNumber());
            // used to synchronize with the display manager
            notifyCurrentSlideNumber()
        }
    }

    /*onhashchange = function () { // use this function to be notified when the url hash changes
     if (url.hash.indexOf("%") != -1) {
     alert("inner transition");
     window.location.hash = url.hash.substring(0, url.hash.indexOf("%") - 1);
     }
     };*/

    function goToNextSlide (slideNumber) {
        if (!slideList[slideNumber].hasInnerNavigation || url.toString().indexOf("?full#") == -1) {
            // there is no inner navigation or it is not the slideshow view so we just go back to the next slide
            slideNumber++;
            // fix active inner transition => may introduce overhead
            if (slideList[slideNumber].hasInnerNavigation) {
                var activeNodes = document.querySelectorAll(getSlideHash(slideNumber) + ' .active');

                while (activeNodes.length > 1) {
                    var currentNode = activeNodes[activeNodes.length - 1];
                    var previousNode = currentNode.previousElementSibling;

                    if (previousNode) {
                        //                url.hash = url.hash.substring(0, url.hash.indexOf("%")) + activeNodes.length - 1;
                        currentNode.className = currentNode.className.substring(0, currentNode.className.length - " .active".length);
                        previousNode.className = previousNode.className + " .active";
                    }
                    activeNodes = document.querySelectorAll(getSlideHash(slideNumber) + ' .active');
                }
            }
            goToSlide(slideNumber);
            return slideNumber
        } else {
            var activeNodes = document.querySelectorAll(getSlideHash(slideNumber) + ' .active');
            var currentNode = activeNodes[activeNodes.length - 1].nextElementSibling;

            if (currentNode) {
                /*if (url.hash.indexOf("%") == -1) {
                 url.hash = getSlideHash(slideNumber) + "%1";
                 } else {
                 url.hash = url.hash.substring(0, url.hash.indexOf("%")) + activeNodes.length + 1;
                 }*/
                currentNode.className = currentNode.className + ' active';
                return slideNumber;
            } else {
                // there is no next inactive inner item so we just go to the next slide
                slideNumber++;
                goToSlide(slideNumber);
                // TODO fix active inner transition
                return slideNumber;
            }
        }
    }

    function goToPreviousSlide (slideNumber) {
        if (!slideList[slideNumber].hasInnerNavigation || url.toString().indexOf("?full#") == -1) {
            // there is no inner navigation or it is not the slideshow view so we just go back to the previous slide
            slideNumber--;
            goToSlide(slideNumber);
            return slideNumber
        } else {
            var activeNodes = document.querySelectorAll(getSlideHash(slideNumber) + " .active");
            var currentNode = activeNodes[activeNodes.length - 1];
            var previousNode = currentNode.previousElementSibling;

            if (previousNode) {
                currentNode.className = currentNode.className.substring(0, currentNode.className.length - " .active".length);
                return slideNumber;
            } else {
                // there is no previouw active inner item so we just go back to the previous slide
                slideNumber--;
                goToSlide(slideNumber);
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

    // Event handlers
    window.addEventListener('DOMContentLoaded', function () {
        if (!isListMode()) {
            // "?full" is present without slide hash, so we should display first slide
            if (-1 === getCurrentSlideNumber()) {
                history.replaceState(null, null, url.pathname + '?full' + getSlideHash(0));
            }

            enterSlideMode();
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

    document.addEventListener('keydown', function (e) {
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
                currentSlideNumber = goToPreviousSlide(currentSlideNumber);
                break;

            case 34: // PgDown
            case 40: // Down
            case 39: // Right
            case 76: // l
            case 74: // j
                e.preventDefault();
                currentSlideNumber = goToNextSlide(currentSlideNumber);
                break;

            case 36: // Home
                e.preventDefault();
                goToSlide(0);
                break;

            case 35: // End
                e.preventDefault();
                goToSlide(slideList.length - 1);
                break;

            case 9: // Tab = +1; Shift + Tab = -1
            case 32: // Space = +1; Shift + Space = -1
                e.preventDefault();
                currentSlideNumber += e.shiftKey ? -1 : 1;
                goToSlide(currentSlideNumber);
                break;
            case 70: // f
                e.preventDefault();
                fullscreen();
                break;
            default:
            // Behave as usual
        }
    }, false);

    document.addEventListener('click', dispatchSingleSlideMode, false);
    /*document.addEventListener('touchend', dispatchSingleSlideMode, false);

     document.addEventListener('touchstart', function (e) {
     if (!isListMode()) {
     var currentSlideNumber = getCurrentSlideNumber(),
     x = e.touches[0].pageX;
     if (x > window.innerWidth / 2) {
     currentSlideNumber++;
     } else {
     currentSlideNumber--;
     }

     goToSlide(currentSlideNumber);
     }
     }, false);

     document.addEventListener('touchmove', function (e) {
     if (!isListMode()) {
     e.preventDefault();
     }
     }, false);*/

    // function that allow to interact with display script
    function notifyCurrentSlideNumber () {
        if (window.opener != null) {
            postMsg(window.opener, "CURSOR", getCurrentSlideNumber()); // FIXME add currentInnerTransition
        }
    }

    function getDetails (slideNumber) {
        try {
            var activeNodes = document.querySelectorAll(getSlideHash(slideNumber));
            var d = activeNodes[activeNodes.length - 1].querySelector("details");
        } catch (e) {
            alert("Unable to get DOMElement.\nPlease check special characters on the id: " + getSlideHash(slideNumber))
        }
        return d ? d.innerHTML : "";
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
            postMsg(win, "CURSOR", getCurrentSlideNumber()); // FIXME add currentInnerTransition
            postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
        } else if (argv[0] === "BACK" && argc === 1) {
            goToPreviousSlide(getCurrentSlideNumber());
            postMsg(win, "CURSOR", getCurrentSlideNumber()); // FIXME add currentInnerTransition
            postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
        } else if (argv[0] === "FORWARD" && argc === 1) {
            if (slideList[getCurrentSlideNumber() + 1] != null) {
                goToNextSlide(getCurrentSlideNumber());
                postMsg(win, "CURSOR", getCurrentSlideNumber()); // FIXME add currentInnerTransition
                postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
            } else {
                goToSlide(0);
                postMsg(win, "CURSOR", 0);
            }
        } else if (argv[0] === "START" && argc === 1) {
            goToSlide(0);
            postMsg(win, "CURSOR", getCurrentSlideNumber());
            postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
        } else if (argv[0] === "END" && argc === 1) {
            goToSlide(slideList.length - 1);
            postMsg(win, "CURSOR", getCurrentSlideNumber());
            postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
        }/* else if (argv[0] === "TOGGLE_CONTENT" && argc === 1) {
         toggleContent();
         }*/ else if (argv[0] === "SET_CURSOR" && argc === 2) {
            goToSlide(argv[1]);
            postMsg(win, "CURSOR", getCurrentSlideNumber());
            postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
        } /*else if (argv[0] === "GET_CURSOR" && argc === 1) {
         postMsg(win, "CURSOR", this.idx + "." + this.step);
         } */ else if (argv[0] === "GET_NOTES" && argc === 1) {
            postMsg(win, "NOTES", getDetails(getCurrentSlideNumber()));
        }
    }/*, false)*/;
}());