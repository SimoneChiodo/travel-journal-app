// TO SHOW MAPS IN PAGE
const lat = parseFloat(document.getElementById("latitude").textContent);
const lon = parseFloat(document.getElementById("longitude").textContent);
const place = document.getElementById("place").textContent;

// Initialize the map
const map = L.map('map').setView([lat, lon], 13);

// Add OpenStreetMap tile layer
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '&copy; OpenStreetMap contributors'
}).addTo(map);

// Add a marker for the travel place
L.marker([lat, lon]).addTo(map)
  .openPopup();
