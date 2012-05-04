var view = null,
    url = null,
    idx = 1,
    count = null,
    iframe = null;

/* Get url from hash or prompt and store it */

function getUrl () {
    if (typeof(slideURL) == 'undefined') {
        slideURL = window.prompt("What is the URL of the slides?");
        if (slideURL) {
            window.location.hash = slideURL.split("#")[0];
            return slideURL;
        }
        slideURL = "<style>body{background-color:white;color:black}</style>";
        slideURL += "<strong>ERROR:</strong> No URL specified.<br>";
        slideURL += "Try<em>: " + document.location + "#yourslides.html</em>";
        slideURL = "data:text/html," + encodeURIComponent(slideURL);
    }
    return slideURL;
}

function loadIFrame () {
    iframe = document.querySelector("iframe");
    iframe.src = url = getUrl();
    iframe.onload = function () {
        view = this.contentWindow;
        postMsg(view, "REGISTER");
    }
}

function back () {
    postMsg(view, "BACK");
    postMsg(view, "GET_CURSOR");
}

function forward () {
    postMsg(view, "FORWARD");
    postMsg(view, "GET_CURSOR");
}

function goStart () {
    postMsg(view, "START");
    postMsg(view, "GET_CURSOR");
}

function goEnd () {
    postMsg(view, "END");
    postMsg(view, "GET_CURSOR");
}

function setCursor (aCursor) {
    postMsg(view, "SET_CURSOR", aCursor);
}

function fullscreen () {
    var requestFullscreen = iframe.requestFullscreen || iframe.requestFullScreen || iframe.mozRequestFullScreen || iframe.webkitRequestFullScreen;
    if (requestFullscreen) {
        requestFullscreen.apply(iframe);
    } else {
        window.open(url, '', 'width=800,height=600,personalbar=0,toolbar=0,scrollbars=1,resizable=1');
    }
}

function list () {
    postMsg(view, "LIST");
}
function full () {
    postMsg(view, "FULL");
}

function postMsg (aWin, aMsg) { // [arg0, [arg1...]]
    aMsg = [aMsg];
    for (var i = 2; i < arguments.length; i++)
        aMsg.push(encodeURIComponent(arguments[i]));
    aWin.postMessage(aMsg.join(" "), "*");
}

var orgX, newX;
var tracking = false;
var db = document.body;
db.addEventListener("touchstart", start.bind(this), false);
db.addEventListener("touchmove", move.bind(this), false);

function start(aEvent) {
  aEvent.preventDefault();
  tracking = true;
  orgX = aEvent.changedTouches[0].pageX;
}

function move(aEvent) {
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
        case 9: // Tab = +1; Shift + Tab = -1
        case 32: // Space = +1; Shift + Space = -1
            e.preventDefault();
            if (e.shiftKey) {
                this.back();
            } else {
                this.forward();
            }
            break;
        case 70: // f
            e.preventDefault();
            fullscreen();
            break;
        case 27: // Esc
            e.preventDefault();
            list();
            break;
        case 13: // Enter
            e.preventDefault();
            full();
            break;
        default:
        // Behave as usual
    }
};

window.onmessage = function (aEvent) {
    if (aEvent.source === view) {
        var argv = aEvent.data.split(" "), argc = argv.length;
        argv.forEach(function (e, i, a) {
            a[i] = decodeURIComponent(e)
        });
        if (argv[0] === "CURSOR" && argc === 2) {
            idx = argv[1];
            document.querySelector("#slideidx").value = (+idx + 1);
            document.querySelector("#back").disabled = idx == 1;
            document.querySelector("#forward").disabled = idx == count;
        }
        if (argv[0] === "REGISTERED" && argc === 3) {
            postMsg(view, "FULL")
            document.querySelector("#slidecount").innerHTML = count = argv[2];
            document.title = argv[1];
        }
    }
};


window.init = function init () {
    loadIFrame();
};
window.onhashchange = function () {
    loadIFrame();
};