"use strict";
// Update stats under the image
function updateStats() {
    const img = document.getElementById("frame");
    const stats = document.getElementById("stats");
    // Just a demo: we can't measure FPS here since it's static
    stats.innerText = `Resolution: ${img.naturalWidth}x${img.naturalHeight} | FPS: (captured)`;
}
// Update stats when the image loads
window.onload = () => {
    updateStats();
};
