// main.ts - Simulates stats updates for demo
const stats = document.getElementById("stats") as HTMLDivElement;

let fps = 15; // dummy FPS
let resolution = "1920 x 1440"; // dummy resolution (match Android debugText)

// simulate stat updates every 1s
setInterval(() => {
  const time = new Date().toLocaleTimeString();
  stats.innerText = `Resolution: ${resolution} | FPS: ${fps} | Updated: ${time}`;
}, 1000);
