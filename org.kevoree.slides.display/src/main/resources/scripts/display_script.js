var views = {
        id:null,
        present:null,
        future:null,
        remote:null
    },
    notes = null,
    url = null;//,
currentSlide = 0;

/* Get url from hash or prompt and store it */
function getUrl () {
    var u = window.location.hash.split("#")[1];
    if (!u) {
        u = window.prompt("What is the URL of the slides?");
        if (u) {
            window.location.hash = u.split("#")[0];
            return u;
        }
        u = "<style>body{background-color:white;color:black}</style>";
        u += "<strong>ERROR:</strong> No URL specified.<br>";
        u += "Try<em>: " + document.location + "#yourslides.html</em>";
        u = "data:text/html," + encodeURIComponent(u);
    }
    return u + "?full";
}

function loadIframes () {
    var present = document.querySelector("#present iframe");
    var future = document.querySelector("#future iframe");
    url = getUrl();
    present.src = future.src = url;
    present.onload = future.onload = function () {
        var id = this.parentNode.id;
        views[id] = this.contentWindow;
        if (views.present && views.future) {
            postMsg(views.present, "REGISTER");
            postMsg(views.future, "REGISTER");
            postMsg(views.future, "FORWARD");
        }
    }
}

/*function toggleContent () {
 if (views.remote) {
 postMsg(views.remote, "TOGGLE_CONTENT");
 }
 }*/

function back () {
    postMsg(views.present, "BACK");
    postMsg(views.future, "BACK");
    if (views.remote != null) {
        postMsg(views.remote, "BACK");
    }
}

function forward () {
    postMsg(views.present, "FORWARD");
    postMsg(views.future, "FORWARD");
    if (views.remote != null) {
        postMsg(views.remote, "FORWARD");
    }
}

function goStart () {
    postMsg(views.present, "START");
    postMsg(views.future, "START");
    postMsg(views.future, "FORWARD");
    if (views.remote != null) {
        postMsg(views.remote, "START");
    }
}

function goEnd () {
    postMsg(views.present, "END");
    postMsg(views.future, "END");
    postMsg(views.future, "FORWARD");
    if (views.remote != null) {
        postMsg(views.remote, "END");
    }
}

function postMsg (aWin, aMsg) { // [arg0, [arg1...]]
    aMsg = [aMsg];
    for (var i = 2; i < arguments.length; i++)
        aMsg.push(encodeURIComponent(arguments[i]));
    aWin.postMessage(aMsg.join(" "), "*");
}

function startClock () {
    var addZero = function (num) {
        return num < 10 ? '0' + num : num;
    };
    setInterval(function () {
        var now = new Date();
        document.querySelector("#hours").innerHTML = addZero(now.getHours());
        document.querySelector("#minutes").innerHTML = addZero(now.getMinutes());
        document.querySelector("#seconds").innerHTML = addZero(now.getSeconds());
    }, 1000);
}

function setCursor (aCursor) {
    postMsg(views.present, "SET_CURSOR", aCursor);
    postMsg(views.future, "SET_CURSOR", aCursor);
    postMsg(views.future, "FORWARD");
    if (views.remote != null) {
        postMsg(views.remote, "SET_CURSOR", aCursor);
    }
}

function popup () {
    views.remote = window.open(this.url, 'slides', 'width=800,height=600,personalbar=0,toolbar=0,scrollbars=1,resizable=1');
}


window.init = function init () {
    startClock();
    loadIframes();
};

window.onkeydown = function (e) {
    // Shortcut for alt, shift and meta keys
    if (e.altKey || e.ctrlKey || e.metaKey || e.shiftKey) {
        return;
    }
    switch (e.which) {
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

        /*case 9: // Tab = +1; Shift + Tab = -1
         case 32: // Space = +1; Shift + Space = -1
         e.preventDefault();
         currentSlideNumber += e.shiftKey ? -1 : 1;
         goToSlide(currentSlideNumber);
         break;*/

        default:
        // Behave as usual
    }
    /*}{
     // Don't intercept keyboard shortcuts
     if (aEvent.altKey
     || aEvent.ctrlKey
     || aEvent.metaKey
     || aEvent.shiftKey) {
     return;
     }
     if (aEvent.keyCode == 37 // left arrow
     || aEvent.keyCode == 38 // up arrow
     || aEvent.keyCode == 33 // page up
     ) {
     aEvent.preventDefault();
     back();
     }
     if (aEvent.keyCode == 39 // right arrow
     || aEvent.keyCode == 40 // down arrow
     || aEvent.keyCode == 34 // page down
     ) {
     aEvent.preventDefault();
     forward();
     }
     if (aEvent.keyCode == 35) { // end
     aEvent.preventDefault();
     goEnd();
     }
     if (aEvent.keyCode == 36) { // home
     aEvent.preventDefault();
     goStart();
     }
     if (aEvent.keyCode == 32) { // space
     aEvent.preventDefault();
     toggleContent();
     }*/
};

window.onhashchange = function () {
    loadIframes();
};

window.onmessage = function (aEvent) {
    var argv = aEvent.data.split(" "), argc = argv.length;
    argv.forEach(function (e, i, a) {
        a[i] = decodeURIComponent(e)
    });
    if (argv[0] === "CURSOR" && argc === 2) {
        if (aEvent.source === views.present && argv[1] != -1) {
            views.currentSlide = argv[1];
            document.querySelector("#slideidx").innerHTML = argv[1];
        } else if (aEvent.source === views.future && argv[1] != -1) {
            document.querySelector("#nextslideidx").innerHTML = +argv[1] < 0 ? "END" : argv[1];
        } else if (aEvent.source === views.remote) {
            postMsg(views.present, "SET_CURSOR", argv[1]);
            postMsg(views.future, "SET_CURSOR", argv[1]);
            postMsg(views.future, "FORWARD");
        }
    }
    if (aEvent.source === views.present) {
        if (argv[0] === "NOTES" && argc === 2) {
            document.querySelector("#notes > #content").innerHTML = this.notes = argv[1];
        }
        if (argv[0] === "REGISTERED" && argc === 3) {
            document.querySelector("#slidecount").innerHTML = argv[2];
        }
    }
};

// allow to close the popup when the window is unload
window.onunload = function () {
    views.remote.close();
};

