// theme.js - Adicione este arquivo e inclua em todas as páginas HTML
const STORAGE_KEY = "dismar_cfg_v1";

// Função para ler configuração
function getConfig() {
  const saved = localStorage.getItem(STORAGE_KEY);
  return saved ? JSON.parse(saved) : {};
}

// Função para aplicar tema
function applyTheme(theme) {
  if (theme === "dark") {
    document.body.classList.add("dark");
  } else {
    document.body.classList.remove("dark");
  }
}

// Aplicar tema ao carregar a página
const config = getConfig();
if (config.theme) {
  applyTheme(config.theme);
}