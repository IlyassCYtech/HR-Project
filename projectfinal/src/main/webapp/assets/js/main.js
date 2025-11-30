// =============================
//  SYSTEME SSE NOTIFICATIONS
// =============================

// On ouvre une connexion SSE
let evtSource = new EventSource(contextPath + "/sse/notifications");

// Quand le serveur envoie un event "notification"
evtSource.addEventListener("notification", function(event) {
    const message = event.data;

    console.log("Notification reçue :", message);

    afficherPopupNotification(message);  // Tu pourras styliser plus tard
    incrementerBadgeNotification();
});

// Quand le serveur confirme la connexion
evtSource.addEventListener("connected", function() {
    console.log("SSE connecté !");
});

// =============================
//  Fonctions utiles
// =============================

// Petite popup provisoire
function afficherPopupNotification(message) {
    alert("🔔 Nouvelle notification : " + message);
}

// Incrémente le badge dans le header
function incrementerBadgeNotification() {
    let badge = document.getElementById("notif-badge");
    if (badge) {
        let count = parseInt(badge.innerText);
        badge.innerText = count + 1;
        badge.style.display = "inline-block";
    }
}

