// Captura de tela de uma URL com o Chrome headless (DevTools Protocol), sem dependencias.
// uso: node capturar.mjs <url> <saida.png> [espera_ms=8000] [largura=1600] [altura=1300]
import { spawn } from "node:child_process";
import { writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";

const [url, saida, espera = "8000", largura = "1600", altura = "1300"] = process.argv.slice(2);
const CHROME = process.env.CHROME_PATH || "C:/Program Files/Google/Chrome/Application/chrome.exe";
const porta = 9334;
const chrome = spawn(CHROME, [
  "--headless=new", `--remote-debugging-port=${porta}`, `--user-data-dir=${join(tmpdir(), "cap-" + Date.now())}`,
  "--no-first-run", "--hide-scrollbars", `--window-size=${largura},${altura}`, "about:blank",
], { stdio: "ignore" });
const dormir = (ms) => new Promise((r) => setTimeout(r, ms));

let wsUrl;
for (let i = 0; i < 50 && !wsUrl; i++) {
  try {
    const alvos = await (await fetch(`http://127.0.0.1:${porta}/json/list`)).json();
    wsUrl = alvos.find((t) => t.type === "page")?.webSocketDebuggerUrl;
  } catch {}
  if (!wsUrl) await dormir(200);
}
const ws = new WebSocket(wsUrl);
await new Promise((r) => ws.addEventListener("open", r));
let id = 0;
const pendentes = new Map();
ws.addEventListener("message", (e) => {
  const m = JSON.parse(e.data);
  if (m.id && pendentes.has(m.id)) { pendentes.get(m.id)(m.result); pendentes.delete(m.id); }
});
const cmd = (method, params = {}) => new Promise((r) => { const n = ++id; pendentes.set(n, r); ws.send(JSON.stringify({ id: n, method, params })); });

await cmd("Emulation.setDeviceMetricsOverride", { width: +largura, height: +altura, deviceScaleFactor: 1, mobile: false });
await cmd("Page.enable");
await cmd("Page.navigate", { url });
await dormir(+espera);
const { data } = await cmd("Page.captureScreenshot", { format: "png" });
writeFileSync(saida, Buffer.from(data, "base64"));
console.log("captura salva em", saida);
ws.close();
chrome.kill();
process.exit(0);
