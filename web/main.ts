async function updateFrame() {
  const img = document.getElementById("frame") as HTMLImageElement;
  const stats = document.getElementById("stats")!;

  try {
    img.src = `http://10.180.207.189:8080/frame?` + new Date().getTime(); // avoid cache
    stats.innerText = "Fetching latest frame...";
  } catch (e) {
    stats.innerText = "❌ Failed to fetch frame";
  }
}

// Refresh every 2 seconds
setInterval(updateFrame, 2000);
