"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
function updateFrame() {
    return __awaiter(this, void 0, void 0, function* () {
        const img = document.getElementById("frame");
        const stats = document.getElementById("stats");
        try {
            img.src = `http://10.180.207.189:8080/frame?` + new Date().getTime(); // avoid cache
            stats.innerText = "Fetching latest frame...";
        }
        catch (e) {
            stats.innerText = "❌ Failed to fetch frame";
        }
    });
}
// Refresh every 2 seconds
setInterval(updateFrame, 2000);
