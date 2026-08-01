(function () {
  "use strict";

  /* ---------- Service Worker & Auto-Update Detection ---------- */
  if ("serviceWorker" in navigator) {
    window.addEventListener("load", function () {
      navigator.serviceWorker.register("sw.js").then(function (reg) {
        
        // Listen for new version installed / waiting
        function showUpdateBanner(worker) {
          var toast = document.createElement("div");
          toast.id = "pwa-update-toast";
          toast.style.cssText = "position:fixed; bottom:20px; right:20px; background:#2C4531; color:white; padding:16px 20px; border-radius:16px; box-shadow:0 10px 30px rgba(0,0,0,0.3); z-index:9999; display:flex; align-items:center; gap:14px; font-family:sans-serif; max-width:90%;";
          toast.innerHTML = `
            <span>🔄 <strong>Nouvelle version disponible !</strong> Des améliorations sont prêtes.</span>
            <button id="pwa-update-btn" style="background:#C96F4A; color:white; border:none; padding:8px 14px; border-radius:20px; font-weight:bold; cursor:pointer;">Mettre à jour</button>
          `;
          document.body.appendChild(toast);

          document.getElementById("pwa-update-btn").addEventListener("click", function () {
            if (worker) worker.postMessage({ type: "SKIP_WAITING" });
            window.location.reload();
          });
        }

        if (reg.waiting) {
          showUpdateBanner(reg.waiting);
        }

        reg.onupdatefound = function () {
          var installingWorker = reg.installing;
          if (installingWorker) {
            installingWorker.onstatechange = function () {
              if (installingWorker.state === "installed" && navigator.serviceWorker.controller) {
                showUpdateBanner(installingWorker);
              }
            };
          }
        };
      }).catch(function () {});
    });

    let refreshing = false;
    navigator.serviceWorker.addEventListener("controllerchange", function () {
      if (!refreshing) {
        refreshing = true;
        window.location.reload();
      }
    });
  }

  /* ---------- Push Notification Sound Permission Request ---------- */
  function playNotificationSound() {
    // Try real audio file first, fallback to Web Audio API beep
    var audio = new Audio("assets/notification.mp3");
    audio.volume = 0.6;
    audio.play().catch(function() {
      // Fallback: generate beep with Web Audio API
      try {
        var ctx = new (window.AudioContext || window.webkitAudioContext)();
        var osc = ctx.createOscillator();
        var gain = ctx.createGain();
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.type = "sine";
        osc.frequency.setValueAtTime(880, ctx.currentTime);
        osc.frequency.setValueAtTime(660, ctx.currentTime + 0.15);
        gain.gain.setValueAtTime(0.4, ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.5);
        osc.start(ctx.currentTime);
        osc.stop(ctx.currentTime + 0.5);
      } catch(e) {}
    });
  }

  window._playNotifSound = playNotificationSound;

  if ("Notification" in window && Notification.permission !== "granted" && Notification.permission !== "denied") {
    setTimeout(function() {
      Notification.requestPermission().then(function(permission) {
        if (permission === "granted") {
          console.log("Notifications activées avec son & vibration !");
          // Show a test notification
          new Notification("Docta na Tshombo 🩺", {
            body: "Les notifications sont activées ! Vous recevrez vos rappels de médicaments.",
            icon: "assets/app-icon.jpg"
          });
        }
      });
    }, 4000);
  }

  /* ---------- Détection plateforme ---------- */
  var isIOS =
    /iPad|iPhone|iPod/.test(navigator.userAgent) ||
    (navigator.platform === "MacIntel" && navigator.maxTouchPoints > 1);

  var isAndroid = /Android/.test(navigator.userAgent);

  var note = document.getElementById("platform-note");
  if (note) {
    if (isIOS) {
      note.textContent =
        "Vous êtes sur iPhone : touchez « Installer l'application Web sur iPhone » puis suivez les étapes pour ajouter l'application à votre écran d'accueil.";
    } else if (isAndroid) {
      note.textContent =
        "Vous êtes sur Android : touchez « Télécharger l'APK Android », autorisez le téléchargement, puis ouvrez le fichier téléchargé pour l'installer.";
    } else {
      note.textContent =
        "Sur ordinateur ou mobile : vous pouvez télécharger l'APK Android ou lancer la Web App sans installation.";
    }
  }

  /* ---------- Téléchargement APK ---------- */
  // Primary: GitHub Releases
  var APK_GITHUB_URL = "https://github.com/henobuild32-ship-it/Docta-na-tshombo-/releases/latest/download/docta-na-tshombo.apk";
  var APK_LOCAL_URL = "apk/docta-na-tshombo.apk";
  var APK_URL = APK_GITHUB_URL;

  var dlButtons = document.querySelectorAll("[data-apk-download]");
  var dlFeedback = document.getElementById("download-feedback");

  function showFeedback(msg) {
    if (dlFeedback) {
      dlFeedback.textContent = msg;
      dlFeedback.style.opacity = "1";
      setTimeout(function () {
        dlFeedback.style.opacity = "0";
      }, 6000);
    }
  }

  dlButtons.forEach(function (btn) {
    btn.addEventListener("click", function () {
      var a = document.createElement("a");
      a.href = APK_URL;
      a.download = "docta-na-tshombo.apk";
      a.rel = "noopener";
      document.body.appendChild(a);
      a.click();
      setTimeout(function () {
        document.body.removeChild(a);
      }, 1000);
      showFeedback(
        isAndroid
          ? "Téléchargement de l'APK mis à jour démarré. Une fois le fichier téléchargé, ouvrez-le pour installer l'application."
          : "Le téléchargement de l'APK a démarré. Retrouvez le fichier dans votre dossier de téléchargements."
      );
    });
  });

  /* ---------- Modale iOS ---------- */
  var modal = document.getElementById("ios-modal");
  var openBtns = document.querySelectorAll("[data-ios-modal]");
  var closeBtn = document.getElementById("modal-close");
  var okBtn = document.getElementById("modal-ok");

  function openModal() {
    if (modal) {
      modal.classList.add("open");
      modal.setAttribute("aria-hidden", "false");
      document.body.style.overflow = "hidden";
    }
  }

  function closeModal() {
    if (modal) {
      modal.classList.remove("open");
      modal.setAttribute("aria-hidden", "true");
      document.body.style.overflow = "";
    }
  }

  openBtns.forEach(function (btn) {
    btn.addEventListener("click", openModal);
  });

  if (closeBtn) closeBtn.addEventListener("click", closeModal);
  if (okBtn) okBtn.addEventListener("click", closeModal);

  if (modal) {
    modal.addEventListener("click", function (event) {
      if (event.target === modal) closeModal();
    });
  }

  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape") closeModal();
  });
})();
