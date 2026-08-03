(function () {
  "use strict";

  /* ---------- APK Download ---------- */
  var APK_URL = "https://github.com/henobuild32-ship-it/Docta-na-tshombo-/releases/latest/download/docta-na-tshombo.apk";
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
      showFeedback("Téléchargement de l'APK mis à jour démarré. Une fois le fichier téléchargé, ouvrez-le pour installer l'application.");
    });
  });

  /* ---------- Initialize ---------- */
  document.addEventListener("DOMContentLoaded", function () {});
})();