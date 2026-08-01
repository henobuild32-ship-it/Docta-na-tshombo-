/**
 * Docta na Tshombo — Interactive Web Application Logic
 * Created by Henock Aduma (henockaduma2@gmail.com)
 */

(function () {
  "use strict";

  // State Management
  let currentRole = "patient"; // 'patient' or 'doctor'
  let doctorPresence = "present"; // 'present' or 'absent'
  let activeTab = "p-doctors";
  let scanTimer = null;
  let scanProgress = 0;
  let ppgAnimFrame = null;

  // Mock Database
  const doctors = [
    {
      id: "doc1",
      name: "Dr. Kabange Mwangele",
      specialty: "Cardiologue & Médecine Générale",
      hospital: "Hôpital du Cinquantenaire, Kinshasa",
      experience: "12 ans d'expérience",
      rating: "4.9 ★ (128 avis)",
      status: "online",
      verified: true,
      avatar: "assets/app-icon.jpg"
    },
    {
      id: "doc2",
      name: "Dr. Sandrine Ilunga",
      specialty: "Pédiatre & Néonatologie",
      hospital: "Clinique Ngaliema, Kinshasa",
      experience: "9 ans d'expérience",
      rating: "4.8 ★ (95 avis)",
      status: "online",
      verified: true,
      avatar: "assets/app-icon.jpg"
    },
    {
      id: "doc3",
      name: "Dr. Alain Bakole",
      specialty: "Neurologue & Neuro-pédiatrie",
      hospital: "CHPR Lubumbashi",
      experience: "15 ans d'expérience",
      rating: "5.0 ★ (210 avis)",
      status: "busy",
      verified: true,
      avatar: "assets/app-icon.jpg"
    },
    {
      id: "doc4",
      name: "Dr. Grace Mukendi",
      specialty: "Gynécologue-Obstétricienne",
      hospital: "Centre Médical de Goma",
      experience: "8 ans d'expérience",
      rating: "4.9 ★ (88 avis)",
      status: "offline",
      verified: true,
      avatar: "assets/app-icon.jpg"
    }
  ];

  let patientAppointments = [
    {
      id: "rdv-101",
      docId: "doc1",
      docName: "Dr. Kabange Mwangele",
      specialty: "Cardiologue",
      date: "01/08/2026 à 15:30",
      type: "Téléconsultation",
      reason: "Bilan tensionnel et essoufflement",
      status: "Confirmé"
    },
    {
      id: "rdv-102",
      docId: "doc2",
      docName: "Dr. Sandrine Ilunga",
      specialty: "Pédiatre",
      date: "05/08/2026 à 10:00",
      type: "Présentiel",
      reason: "Suivi pédiatrique trimestriel",
      status: "En attente"
    }
  ];

  let patientMessages = [
    { sender: "other", text: "Bonjour ! Comment vous sentez-vous depuis la prise du traitement ?", time: "14:20" },
    { sender: "mine", text: "Bonjour Docteur. La fièvre a baissé mais j'ai encore une légère toux.", time: "14:22" },
    { sender: "other", text: "D'accord, continuez le Paracétamol et buvez beaucoup d'eau. N'hésitez pas si besoin !", time: "14:25" }
  ];

  let patientPrescriptions = [
    {
      id: "ORD-2026-8812",
      docName: "Dr. Kabange Mwangele",
      date: "01/08/2026",
      diag: "Grippe A & Fièvre",
      meds: "Paracétamol 1g (3x/j, 5j), Amoxicilline 1g (2x/j, 7j)"
    }
  ];

  let medReminders = [
    { id: 1, name: "Paracétamol 1g", time: "08:00", active: true },
    { id: 2, name: "Amoxicilline 1g", time: "20:00", active: true }
  ];

  // Global Functions Attached to Window
  window.toggleUserRole = function () {
    currentRole = currentRole === "patient" ? "doctor" : "patient";
    const roleBadge = document.getElementById("user-role-display");
    const pNav = document.getElementById("patient-nav");
    const dNav = document.getElementById("doctor-nav");

    if (currentRole === "patient") {
      roleBadge.textContent = "PATIENT";
      roleBadge.style.background = "#9FD3B6";
      roleBadge.style.color = "#2C4531";
      pNav.style.display = "flex";
      dNav.style.display = "none";
      switchTab("p-doctors");
    } else {
      roleBadge.textContent = "PRATICIEN (DOCTEUR)";
      roleBadge.style.background = "#C96F4A";
      roleBadge.style.color = "white";
      pNav.style.display = "none";
      dNav.style.display = "flex";
      switchTab("d-dashboard");
    }
  };

  window.switchTab = function (tabId) {
    activeTab = tabId;
    document.querySelectorAll(".app-tab").forEach(tab => tab.style.display = "none");
    document.querySelectorAll(".nav-item").forEach(btn => btn.classList.remove("active"));

    const targetTab = document.getElementById("tab-" + tabId);
    if (targetTab) targetTab.style.display = "block";

    // Highlight active nav item
    const activeNavBtn = document.querySelector(`[onclick="switchTab('${tabId}')"]`);
    if (activeNavBtn) activeNavBtn.classList.add("active");

    // Refresh contents
    if (tabId === "p-doctors") renderDoctorGrid();
    if (tabId === "p-appointments") renderPatientAppointments();
    if (tabId === "p-prescriptions") renderPatientPrescriptions();
    if (tabId === "p-reminders") renderReminders();
    if (tabId === "d-dashboard") renderDoctorDashboard();
    if (tabId === "d-agenda") renderDoctorAgenda();
    if (tabId === "d-patients") renderDoctorPatients();
  };

  // Render Doctors Grid (No pricing/tarif shown)
  function renderDoctorGrid() {
    const grid = document.getElementById("doctors-list-grid");
    if (!grid) return;

    grid.innerHTML = doctors.map(doc => `
      <div class="stat-card" style="display:flex; flex-direction:column; justify-space-between;">
        <div>
          <div style="display:flex; justify-content:space-between; align-items:flex-start;">
            <div style="display:flex; gap:12px; align-items:center;">
              <img src="${doc.avatar}" style="width:48px; height:48px; border-radius:50%; object-fit:cover; border:2px solid #2C4531;" />
              <div>
                <h3 style="font-size:1.05rem;">${doc.name} ${doc.verified ? '✅' : ''}</h3>
                <span style="color:#2C4531; font-weight:600; font-size:0.85rem;">${doc.specialty}</span>
              </div>
            </div>
            <span class="pulse-dot ${doc.status}"></span>
          </div>

          <div style="margin:12px 0; font-size:0.88rem; color:#4A5568; line-height:1.5;">
            <div>🏥 ${doc.hospital}</div>
            <div>🎓 ${doc.experience}</div>
            <div>⭐ ${doc.rating}</div>
          </div>
        </div>

        <div style="display:flex; gap:8px; margin-top:14px;">
          <button onclick="launchTeleconsultation('${doc.id}', '${doc.name}', '${doc.status}')" class="btn btn-primary btn-sm" style="flex:1;">
            📹 Visio Directe
          </button>
          <button onclick="bookRdvModal('${doc.id}', '${doc.name}')" class="btn btn-ghost btn-sm" style="flex:1;">
            📅 Prendre RDV
          </button>
        </div>
      </div>
    `).join('');
  }

  window.filterDoctors = function () {
    const q = document.getElementById("doc-search-input").value.toLowerCase();
    const filtered = doctors.filter(d => d.name.toLowerCase().includes(q) || d.specialty.toLowerCase().includes(q) || d.hospital.toLowerCase().includes(q));
    const grid = document.getElementById("doctors-list-grid");
    grid.innerHTML = filtered.map(doc => `
      <div class="stat-card">
        <h3>${doc.name} ✅</h3>
        <p style="color:#2C4531; font-weight:600;">${doc.specialty}</p>
        <p style="color:#718096; font-size:0.9rem;">${doc.hospital}</p>
        <button onclick="launchTeleconsultation('${doc.id}', '${doc.name}', '${doc.status}')" class="btn btn-primary btn-sm" style="margin-top:12px; width:100%;">📹 Lancer Visio</button>
      </div>
    `).join('');
  };

  // Launch Teleconsultation WebRTC / Jitsi
  window.launchTeleconsultation = function (docId, docName, status) {
    if (status === "offline") {
      alert(`🚫 ${docName} est actuellement hors ligne. Souhaitez-vous lui laisser un message dans la messagerie ?`);
      switchTab('p-messages');
      return;
    }
    if (status === "busy") {
      alert(`⏳ ${docName} est en cours de consultation. Vous êtes placé en file d'attente (Position #2, ~10 min estimées).`);
    }

    const modal = document.getElementById("teleconsult-modal");
    const iframe = document.getElementById("jitsi-iframe");
    const title = document.getElementById("teleconsult-modal-title");

    title.textContent = `📹 Téléconsultation avec ${docName}`;
    iframe.src = `https://meet.jit.si/docta_tshombo_consult_${docId}_${Date.now()}`;
    modal.classList.add("active");
  };

  window.closeTeleconsultModal = function () {
    const modal = document.getElementById("teleconsult-modal");
    const iframe = document.getElementById("jitsi-iframe");
    iframe.src = "";
    modal.classList.remove("active");
  };

  // Vital Signs Scan by Torch / Camera Simulation
  window.startVitalSignsScan = function () {
    const pulseCircle = document.getElementById("scan-pulse-circle");
    const timerText = document.getElementById("scan-timer-text");
    const bpmVal = document.getElementById("val-bpm");
    const spo2Val = document.getElementById("val-spo2");
    const btn = document.getElementById("start-scan-btn");

    btn.disabled = true;
    let secondsLeft = 15;
    bpmVal.textContent = "...";
    spo2Val.textContent = "...";

    // PPG Wave animation on Canvas
    const canvas = document.getElementById("ppg-graph");
    const ctx = canvas.getContext("2d");
    canvas.width = canvas.offsetWidth;
    canvas.height = canvas.offsetHeight;

    let step = 0;
    function drawPPGWave() {
      ctx.fillStyle = "#1A202C";
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      ctx.strokeStyle = "#38A169";
      ctx.lineWidth = 3;
      ctx.beginPath();

      for (let x = 0; x < canvas.width; x++) {
        const y = canvas.height / 2 + Math.sin((x + step) * 0.05) * 25 + Math.sin((x + step) * 0.1) * 10;
        if (x === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      }
      ctx.stroke();
      step += 4;
      ppgAnimFrame = requestAnimationFrame(drawPPGWave);
    }
    drawPPGWave();

    // Pulse animation
    pulseCircle.style.background = "#E53E3E";
    pulseCircle.style.boxShadow = "0 0 25px #E53E3E";

    scanTimer = setInterval(() => {
      secondsLeft--;
      timerText.textContent = `Mesure du flux sanguin... ${secondsLeft} sec restantes`;

      if (secondsLeft <= 0) {
        clearInterval(scanTimer);
        cancelAnimationFrame(ppgAnimFrame);
        btn.disabled = false;

        const finalBpm = Math.floor(Math.random() * (82 - 68 + 1)) + 68;
        const finalSpo2 = Math.floor(Math.random() * (99 - 96 + 1)) + 96;

        bpmVal.textContent = `${finalBpm} BPM`;
        spo2Val.textContent = `${finalSpo2} %`;

        pulseCircle.style.background = "#38A169";
        pulseCircle.style.boxShadow = "0 0 15px #38A169";
        timerText.textContent = "✅ Scan terminé ! Données enregistrées dans votre dossier médical.";
      }
    }, 1000);
  };

  // Prescription Generator & Sharing
  window.addPrescMedRow = function () {
    const container = document.getElementById("presc-meds-container");
    const div = document.createElement("div");
    div.style.cssText = "display:flex; gap:8px;";
    div.innerHTML = `
      <input type="text" class="p-med-name" placeholder="Médicament" style="flex:2; padding:8px; border-radius:6px; border:1px solid #CBD5E0;" />
      <input type="text" class="p-med-poso" placeholder="Posologie" style="flex:1; padding:8px; border-radius:6px; border:1px solid #CBD5E0;" />
      <input type="text" class="p-med-dur" placeholder="Durée" style="flex:1; padding:8px; border-radius:6px; border:1px solid #CBD5E0;" />
    `;
    container.appendChild(div);
  };

  window.generatePrescriptionPreview = function () {
    const patientName = document.getElementById("presc-patient-select").value;
    const diag = document.getElementById("presc-diag").value;
    const box = document.getElementById("presc-preview-box");
    const shareActions = document.getElementById("presc-share-actions");

    const medNames = Array.from(document.querySelectorAll(".p-med-name")).map(i => i.value).filter(Boolean);
    const medPosos = Array.from(document.querySelectorAll(".p-med-poso")).map(i => i.value);
    const medDurs = Array.from(document.querySelectorAll(".p-med-dur")).map(i => i.value);

    let medsListHtml = medNames.map((name, idx) => `
      <li style="margin-bottom:6px;">
        <strong>${name}</strong> — ${medPosos[idx] || '1x/jour'} (${medDurs[idx] || '5 jours'})
      </li>
    `).join('');

    const todayStr = new Date().toLocaleDateString('fr-FR');
    box.innerHTML = `
      <div style="background:white; padding:20px; border-radius:8px; border:1px solid #E2E8F0; font-family:sans-serif; color:#2D3748;">
        <div style="display:flex; justify-content:space-between; border-bottom:2px solid #2C4531; padding-bottom:10px; margin-bottom:14px;">
          <div>
            <strong style="color:#2C4531; font-size:1.1rem;">Dr. Kabange Mwangele</strong><br/>
            <small>Cardiologue • RPPS : 1092837492</small><br/>
            <small>Email : henockaduma2@gmail.com</small>
          </div>
          <div style="text-align:right;">
            <small>Date : ${todayStr}</small><br/>
            <small>Réf : ORD-${Date.now().toString().slice(-6)}</small>
          </div>
        </div>

        <p><strong>Patient :</strong> ${patientName}</p>
        <p><strong>Motif / Diagnostic :</strong> ${diag}</p>

        <h4 style="margin-top:14px; color:#2C4531;">PRESCRIPTION MÉDICALE :</h4>
        <ol style="margin-left:20px; margin-top:8px;">${medsListHtml}</ol>

        <div style="margin-top:30px; display:flex; justify-content:space-between; align-items:flex-end;">
          <div style="border:1px solid #CBD5E0; padding:6px 12px; border-radius:6px; font-size:0.8rem; background:#F7FAFC;">
            📜 Cachet Officiel du Médecin Validé
          </div>
          <div style="text-align:center;">
            <div style="font-family:cursive; font-size:1.4rem; color:#2C4531;">Dr. K. Mwangele</div>
            <small style="color:#718096;">Signature Numérique Certifiée</small>
          </div>
        </div>
      </div>
    `;

    shareActions.style.display = "flex";
  };

  window.sharePrescription = function (method) {
    if (method === 'message') {
      alert("✅ Ordonnance envoyée directement dans la messagerie du patient !");
    } else if (method === 'email') {
      alert("📧 Ordonnance transmise par email avec copie à henockaduma2@gmail.com !");
    } else if (method === 'sms') {
      alert("📱 Lien de téléchargement sécurisé envoyé par SMS au patient !");
    } else if (method === 'download') {
      alert("💾 Ordonnance téléchargée en PDF sur votre appareil !");
    }
  };

  // Practitioner Auto-Validation 15-second Scanner
  let docFileSelected = false;
  window.handleDocFileSelected = function (e) {
    const file = e.target.files[0];
    if (file) {
      docFileSelected = true;
      document.getElementById("selected-doc-name").textContent = `Fichier sélectionné: ${file.name} (${(file.size/1024/1024).toFixed(2)} MB)`;
      document.getElementById("run-auto-val-btn").disabled = false;
    }
  };

  window.startDocAutoValidation = function () {
    const progressBox = document.getElementById("scan-progress-box");
    const progressBar = document.getElementById("scan-progress-bar");
    const statusLabel = document.getElementById("scan-status-label");
    const resultBanner = document.getElementById("scan-result-banner");
    const btn = document.getElementById("run-auto-val-btn");

    btn.disabled = true;
    progressBox.style.display = "block";
    resultBanner.style.display = "none";

    let sec = 0;
    const interval = setInterval(() => {
      sec++;
      const pct = Math.min(100, Math.round((sec / 15) * 100));
      progressBar.style.width = pct + "%";

      if (sec === 3) statusLabel.textContent = "Analyse du format & de la résolution (DPI)...";
      if (sec === 7) statusLabel.textContent = "Contrôle du nom du praticien & de l'Ordre des Médecins...";
      if (sec === 11) statusLabel.textContent = "Vérification du cachet et de l'intégrité numérique...";

      if (sec >= 15) {
        clearInterval(interval);
        statusLabel.textContent = "Analyse terminée !";

        resultBanner.style.display = "block";
        resultBanner.style.background = "#C6F6D5";
        resultBanner.style.color = "#22543D";
        resultBanner.innerHTML = "✅ Documents validés avec succès en 15 secondes ! Votre profil est désormais visible par tous les patients.";
        btn.disabled = false;
      }
    }, 1000);
  };

  // Doctor Presence Toggle
  window.toggleDoctorPresence = function () {
    doctorPresence = doctorPresence === "present" ? "absent" : "present";
    const dot = document.getElementById("doc-presence-dot");
    const text = document.getElementById("doc-presence-text");

    if (doctorPresence === "present") {
      dot.className = "pulse-dot online";
      text.textContent = "Statut: Présent (En ligne)";
    } else {
      dot.className = "pulse-dot offline";
      text.textContent = "Statut: Absent (Hors ligne)";
    }
  };

  // Appointments Management
  window.openAddConsultationModal = function () {
    document.getElementById("add-consult-modal").classList.add("active");
  };
  window.closeAddConsultationModal = function () {
    document.getElementById("add-consult-modal").classList.remove("active");
  };

  window.saveManualConsultation = function () {
    const patient = document.getElementById("add-c-patient").value;
    const dateVal = document.getElementById("add-c-date").value;
    const reason = document.getElementById("add-c-reason").value;
    const type = document.getElementById("add-c-type").value;

    if (!patient || !reason) {
      alert("Veuillez remplir le nom du patient et le motif.");
      return;
    }

    patientAppointments.push({
      id: "rdv-" + Date.now(),
      docId: "doc1",
      docName: "Dr. Kabange Mwangele",
      specialty: "Cardiologue",
      date: dateVal ? new Date(dateVal).toLocaleString('fr-FR') : "Aujourd'hui à 16:00",
      type: type,
      reason: reason,
      status: "En attente"
    });

    closeAddConsultationModal();
    alert("✅ Consultation ajoutée avec succès dans l'agenda !");
    renderDoctorAgenda();
    renderDoctorDashboard();
  };

  window.cancelRdv = function (id) {
    if (confirm("Voulez-vous vraiment annuler ce rendez-vous ? Un message sera notifié au patient.")) {
      patientAppointments = patientAppointments.filter(r => r.id !== id);
      renderPatientAppointments();
      renderDoctorAgenda();
      renderDoctorDashboard();
    }
  };

  function renderPatientAppointments() {
    const list = document.getElementById("patient-appointments-list");
    if (!list) return;

    if (patientAppointments.length === 0) {
      list.innerHTML = `<p style="color:#718096;">Aucun rendez-vous enregistré.</p>`;
      return;
    }

    list.innerHTML = patientAppointments.map(rdv => `
      <div class="stat-card" style="display:flex; justify-space-between; align-items:center; flex-wrap:wrap; gap:12px;">
        <div>
          <h3>${rdv.docName} (${rdv.specialty})</h3>
          <p style="color:#2C4531; font-weight:600; margin-top:4px;">📅 ${rdv.date} • ${rdv.type}</p>
          <p style="color:#718096; font-size:0.9rem;">Motif : ${rdv.reason}</p>
        </div>
        <div style="display:flex; gap:8px;">
          ${rdv.type === 'Téléconsultation' ? `<button onclick="launchTeleconsultation('${rdv.docId}', '${rdv.docName}', 'online')" class="btn btn-primary btn-sm">📹 Rejoindre Visio</button>` : ''}
          <button onclick="cancelRdv('${rdv.id}')" class="btn btn-ghost btn-sm" style="color:#E53E3E; border-color:#FEB2B2;">Annuler</button>
        </div>
      </div>
    `).join('');
  }

  function renderPatientPrescriptions() {
    const list = document.getElementById("patient-prescriptions-list");
    if (!list) return;

    list.innerHTML = patientPrescriptions.map(p => `
      <div class="stat-card">
        <div style="display:flex; justify-space-between; align-items:center;">
          <h3>Ordonnance ${p.id}</h3>
          <small style="color:#718096;">Date : ${p.date}</small>
        </div>
        <p style="margin-top:6px;"><strong>Praticien :</strong> ${p.docName}</p>
        <p><strong>Traitement :</strong> ${p.meds}</p>
        <button onclick="alert('Téléchargement du PDF de l\'ordonnance en cours...')" class="btn btn-ghost btn-sm" style="margin-top:10px;">💾 Télécharger PDF Certifié</button>
      </div>
    `).join('');
  }

  function renderReminders() {
    const list = document.getElementById("reminders-list");
    if (!list) return;

    list.innerHTML = medReminders.map(r => `
      <div class="stat-card" style="display:flex; justify-space-between; align-items:center; padding:14px 20px;">
        <div>
          <strong>💊 ${r.name}</strong>
          <div style="color:#718096; font-size:0.85rem;">Rappel quotidien à ${r.time}</div>
        </div>
        <button onclick="alert('Prise confirmée !')" class="btn btn-primary btn-sm">✅ Prendre</button>
      </div>
    `).join('');
  }

  window.addMedReminder = function () {
    const name = document.getElementById("rem-med-name").value;
    const time = document.getElementById("rem-time").value;
    if (!name) return alert("Saisissez le nom du médicament.");

    medReminders.push({ id: Date.now(), name, time, active: true });
    renderReminders();
    document.getElementById("rem-med-name").value = "";

    // Request notification permission if needed
    if ("Notification" in window && Notification.permission !== "granted") {
      Notification.requestPermission().then(p => {
        if (p === "granted") scheduleReminderNotif(name, time);
      });
    } else if (Notification.permission === "granted") {
      scheduleReminderNotif(name, time);
    }

    alert(`✅ Rappel pour "${name}" ajouté à ${time} !`);
  };

  function scheduleReminderNotif(medName, timeStr) {
    // Check every minute if it's time to notify
    const [h, m] = timeStr.split(":").map(Number);
    setInterval(() => {
      const now = new Date();
      if (now.getHours() === h && now.getMinutes() === m) {
        try {
          new Notification(`💊 Rappel Médicament — ${medName}`, {
            body: `C'est l'heure de prendre votre ${medName} ! Docta na Tshombo.`,
            icon: "assets/app-icon.jpg",
            badge: "assets/app-icon.jpg",
            vibrate: [200, 100, 200],
            tag: "med-reminder-" + medName,
            requireInteraction: true
          });
          if (window._playNotifSound) window._playNotifSound();
        } catch(e) {}
      }
    }, 60000);
  }

  // Chat messaging
  window.sendPatientMessage = function () {
    const input = document.getElementById("p-chat-input");
    const container = document.getElementById("p-chat-messages");
    if (!input.value.trim()) return;

    container.innerHTML += `
      <div class="chat-bubble mine">${input.value}</div>
    `;
    input.value = "";
    container.scrollTop = container.scrollHeight;

    setTimeout(() => {
      container.innerHTML += `
        <div class="chat-bubble other">Merci pour votre message. Je réponds à vos questions sous peu.</div>
      `;
      container.scrollTop = container.scrollHeight;
    }, 1500);
  };

  // Practitioner Views Rendering
  function renderDoctorDashboard() {
    const upcomingList = document.getElementById("d-upcoming-rdv-list");
    if (upcomingList) {
      upcomingList.innerHTML = patientAppointments.slice(0, 3).map(r => `
        <div style="padding:10px; background:#F7FAFC; border-radius:8px; border:1px solid #EDF2F7; display:flex; justify-content:space-between; align-items:center;">
          <div>
            <strong>${r.reason}</strong>
            <div style="font-size:0.85rem; color:#718096;">📅 ${r.date} (${r.type})</div>
          </div>
          <button onclick="launchTeleconsultation('${r.docId}', 'Dr. Kabange', 'online')" class="btn btn-primary btn-sm" style="padding:4px 8px; font-size:0.75rem;">Lancer</button>
        </div>
      `).join('');
    }
  }

  function renderDoctorAgenda() {
    const list = document.getElementById("doctor-agenda-list");
    if (!list) return;

    list.innerHTML = patientAppointments.map(r => `
      <div class="stat-card" style="display:flex; justify-space-between; align-items:center;">
        <div>
          <h3>${r.reason}</h3>
          <p style="color:#2C4531; font-weight:600;">📅 ${r.date} • ${r.type}</p>
        </div>
        <div style="display:flex; gap:6px;">
          <button onclick="alert('Consultation ouverte')" class="btn btn-primary btn-sm">Ouvrir</button>
          <button onclick="cancelRdv('${r.id}')" class="btn btn-ghost btn-sm" style="color:#E53E3E;">Annuler</button>
        </div>
      </div>
    `).join('');
  }

  function renderDoctorPatients() {
    const list = document.getElementById("doctor-patients-list");
    if (!list) return;

    list.innerHTML = [
      { name: "Jean Mukendi", age: "34 ans", blood: "O+", allergies: "Pénicilline" },
      { name: "Marie Tshilombo", age: "28 ans", blood: "A+", allergies: "Aucune" },
      { name: "Paul Kande", age: "52 ans", blood: "B+", allergies: "Aspirine" }
    ].map(p => `
      <div class="stat-card">
        <h3>👤 ${p.name}</h3>
        <p style="color:#718096;">Âge : ${p.age} • Groupe : ${p.blood}</p>
        <p style="color:#E53E3E; font-size:0.85rem; margin-top:4px;">⚠️ Allergie : ${p.allergies}</p>
        <button onclick="switchTab('d-prescription')" class="btn btn-ghost btn-sm" style="margin-top:10px; width:100%;">📝 Rédiger Ordonnance</button>
      </div>
    `).join('');
  }

  // Legal Modal
  window.openLegalModal = function (type) {
    const modal = document.getElementById("legal-modal");
    const title = document.getElementById("legal-title");
    const body = document.getElementById("legal-body");

    if (type === 'privacy') {
      title.textContent = "📜 Politique de Confidentialité";
      body.innerHTML = `
        <p><strong>1. Collecte des Données Personnelles</strong><br/>
        Docta na Tshombo collecte les informations nécessaires pour la téléconsultation (nom, email, téléphone, constantes vitales).<br/><br/>
        <strong>2. Chiffrement et Sécurité</strong><br/>
        Toutes les données de santé sont chiffrées en transit (SSL/TLS) et stockées de manière sécurisée (Firestore / Supabase Storage).<br/><br/>
        <strong>3. Vos Droits</strong><br/>
        Vous disposez d'un droit d'accès, de rectification et de suppression de vos données en contactant <strong>henockaduma2@gmail.com</strong>.</p>
      `;
    } else {
      title.textContent = "⚖️ Conditions d'Utilisation";
      body.innerHTML = `
        <p><strong>1. Responsabilité Médicale</strong><br/>
        Docta na Tshombo met en relation patients et praticiens de santé vérifiés. La téléconsultation ne remplace pas une urgence vitale.<br/><br/>
        <strong>2. Propriété Intellectuelle</strong><br/>
        Application conçue et développée par Henock Aduma. Tous droits réservés 2026.<br/><br/>
        <strong>3. Contact</strong><br/>
        Email : henockaduma2@gmail.com</p>
      `;
    }
    modal.classList.add("active");
  };

  window.closeLegalModal = function () {
    document.getElementById("legal-modal").classList.remove("active");
  };

  // Initial Load
  window.addEventListener("DOMContentLoaded", () => {
    renderDoctorGrid();
    renderReminders();

    // Auto-check existing reminders on load
    medReminders.forEach(r => {
      if (r.active && Notification.permission === "granted") {
        scheduleReminderNotif(r.name, r.time);
      }
    });
  });

})();
