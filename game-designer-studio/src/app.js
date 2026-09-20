// Game Designer Studio - Client Application Logic

const COLOR_MAP = {
  "R": { name: "Red", class: "bubble-R", emoji: "🔴" },
  "G": { name: "Green", class: "bubble-G", emoji: "🟢" },
  "B": { name: "Blue", class: "bubble-B", emoji: "🔵" },
  "Y": { name: "Yellow", class: "bubble-Y", emoji: "🟡" },
  "P": { name: "Purple", class: "bubble-P", emoji: "🟣" },
  "O": { name: "Orange", class: "bubble-O", emoji: "🟠" },
  "C": { name: "Cyan", class: "bubble-C", emoji: "🩵" },
  "*": { name: "Rainbow", class: "bubble-STAR", emoji: "🌈" },
  "X": { name: "Bomb", class: "bubble-X", emoji: "💣" },
  "L": { name: "Lightning", class: "bubble-L", emoji: "⚡" },
  "F": { name: "Fireball", class: "bubble-F", emoji: "🔥" },
  "S": { name: "Stone", class: "bubble-S", emoji: "🪨" },
  "T": { name: "Target", class: "bubble-T", emoji: "🎯" },
  ".": { name: "Empty", class: "bubble-EMPTY", emoji: "✕" }
};

// Application State
const appState = {
  projectPath: 'D:\\projects\\bubble shooter pro\\BubbleShooterpro',
  resolvedPaths: null,
  levelCount: 0,
  worldCount: 0,
  worldsData: null,
  currentTab: 'level-designer',

  // Level Designer State
  levelDesigner: {
    levelNumber: 1,
    shots: 20,
    activeColor: 'R',
    activeTool: 'brush', // 'brush', 'fill', 'eraser'
    mirrorMode: false,
    isMouseDown: false,
    colors: ['RED', 'GREEN', 'BLUE', 'YELLOW', 'PURPLE', 'ORANGE'],
    objective: { type: 'CLEAR_ALL', target: 0 },
    starThresholds: [2000, 4500, 8000],
    rows: [
      "RRBBYBBRR",
      "RBYRRYBR",
      "BYRRRRRYB",
      "BRRRRRRB",
      ".YRRRRRY.",
      ".YBYYBY."
    ]
  },

  // Pin Positioner State
  pinPositioner: {
    selectedWorldId: 1,
    selectedType: 'node', // 'node' or 'gift'
    selectedNodeIndex: 0,
    selectedGiftIndex: 0,
    zoomScale: 1.0,
    isDragging: false,
    dragType: 'node', // 'node' or 'gift'
    dragIndex: -1
  }
};

// DOM Elements
const inputProjectPath = document.getElementById('inputProjectPath');
const btnBrowseProject = document.getElementById('btnBrowseProject');
const btnRescanProject = document.getElementById('btnRescanProject');
const txtProjectStatus = document.getElementById('txtProjectStatus');
const badgeProjectStatus = document.getElementById('badgeProjectStatus');
const lblWorldCount = document.getElementById('lblWorldCount');
const lblLevelCount = document.getElementById('lblLevelCount');

// Tabs
const tabBtnLevelDesigner = document.getElementById('tabBtnLevelDesigner');
const tabBtnPinPositioner = document.getElementById('tabBtnPinPositioner');
const tabBtnWorldsExplorer = document.getElementById('tabBtnWorldsExplorer');
const viewLevelDesigner = document.getElementById('viewLevelDesigner');
const viewPinPositioner = document.getElementById('viewPinPositioner');
const viewWorldsExplorer = document.getElementById('viewWorldsExplorer');

// Level Designer Elements
const inputLevelNum = document.getElementById('inputLevelNum');
const btnPrevLevel = document.getElementById('btnPrevLevel');
const btnNextLevel = document.getElementById('btnNextLevel');
const btnLoadLevelDirect = document.getElementById('btnLoadLevelDirect');
const btnSaveLevelDirect = document.getElementById('btnSaveLevelDirect');
const hexContainer = document.getElementById('hexContainer');
const rowBadgesContainer = document.getElementById('rowBadgesContainer');
const txtHoverInfo = document.getElementById('txtHoverInfo');
const txtBoardDimensions = document.getElementById('txtBoardDimensions');
const lblTotalBubbles = document.getElementById('lblTotalBubbles');
const pillsColorBreakdown = document.getElementById('pillsColorBreakdown');
const lblConnectionStatus = document.getElementById('lblConnectionStatus');
const badgeCurrentWorld = document.getElementById('badgeCurrentWorld');
const inputShots = document.getElementById('inputShots');
const selectObjectiveType = document.getElementById('selectObjectiveType');
const boxObjectiveTarget = document.getElementById('boxObjectiveTarget');
const inputObjectiveTarget = document.getElementById('inputObjectiveTarget');
const boxObjectiveColor = document.getElementById('boxObjectiveColor');
const selectObjectiveColor = document.getElementById('selectObjectiveColor');
const btnAutoColors = document.getElementById('btnAutoColors');
const btnAutoStars = document.getElementById('btnAutoStars');
const inputStar1 = document.getElementById('inputStar1');
const inputStar2 = document.getElementById('inputStar2');
const inputStar3 = document.getElementById('inputStar3');

// Pin Positioner Elements
const selectPinWorld = document.getElementById('selectPinWorld');
const btnPrevWorld = document.getElementById('btnPrevWorld');
const btnNextWorld = document.getElementById('btnNextWorld');
const btnFloatingPrevWorld = document.getElementById('btnFloatingPrevWorld');
const btnFloatingNextWorld = document.getElementById('btnFloatingNextWorld');
const btnSavePinsDirect = document.getElementById('btnSavePinsDirect');
const lblWorldLevelRange = document.getElementById('lblWorldLevelRange');
const lblWorldDrawableName = document.getElementById('lblWorldDrawableName');
const badgeSelectedPinLevel = document.getElementById('badgeSelectedPinLevel');
const inputPinX = document.getElementById('inputPinX');
const inputPinY = document.getElementById('inputPinY');
const btnZoomIn = document.getElementById('btnZoomIn');
const btnZoomOut = document.getElementById('btnZoomOut');
const btnZoomFit = document.getElementById('btnZoomFit');
const lblZoomLevel = document.getElementById('lblZoomLevel');
const listWorldNodes = document.getElementById('listWorldNodes');
const listWorldGifts = document.getElementById('listWorldGifts');
const boxGiftDetails = document.getElementById('boxGiftDetails');
const inputGiftName = document.getElementById('inputGiftName');
const inputGiftOffset = document.getElementById('inputGiftOffset');
const inputGiftCoins = document.getElementById('inputGiftCoins');
const mapWrapper = document.getElementById('mapWrapper');
const imgMapBackground = document.getElementById('imgMapBackground');
const pinsOverlay = document.getElementById('pinsOverlay');

// Worlds Explorer Elements
const gridWorldsList = document.getElementById('gridWorldsList');

// Modal Elements
const modalRawJson = document.getElementById('modalRawJson');
const btnOpenRawJson = document.getElementById('btnOpenRawJson');
const btnCloseRawJson = document.getElementById('btnCloseRawJson');
const txtRawJsonContent = document.getElementById('txtRawJsonContent');
const btnCopyRawJson = document.getElementById('btnCopyRawJson');
const toastNotification = document.getElementById('toastNotification');

// Create New Level Modal Elements
const btnOpenNewLevelModal = document.getElementById('btnOpenNewLevelModal');
const modalNewLevel = document.getElementById('modalNewLevel');
const btnCloseNewLevel = document.getElementById('btnCloseNewLevel');
const btnCancelNewLevel = document.getElementById('btnCancelNewLevel');
const btnConfirmCreateLevel = document.getElementById('btnConfirmCreateLevel');
const inputNewLevelNum = document.getElementById('inputNewLevelNum');
const lblNewLevelWorldHint = document.getElementById('lblNewLevelWorldHint');
const selectNewLevelTemplate = document.getElementById('selectNewLevelTemplate');
const inputNewLevelShots = document.getElementById('inputNewLevelShots');
const selectNewLevelObjective = document.getElementById('selectNewLevelObjective');

// ============================================================================
// INITIALIZATION
// ============================================================================
async function initApp() {
  setupEventListeners();
  // Auto-scan default project
  await scanAndConnectProject(appState.projectPath);
}

function setupEventListeners() {
  // Global Mouse tracking for drawing
  window.addEventListener('mousedown', () => appState.levelDesigner.isMouseDown = true);
  window.addEventListener('mouseup', () => {
    appState.levelDesigner.isMouseDown = false;
    appState.pinPositioner.isDragging = false;
  });

  // Project Folder Selector
  btnBrowseProject.addEventListener('click', onBrowseProject);
  btnRescanProject.addEventListener('click', () => scanAndConnectProject(appState.projectPath));

  // Tabs Navigation
  tabBtnLevelDesigner.addEventListener('click', () => switchTab('level-designer'));
  tabBtnPinPositioner.addEventListener('click', () => switchTab('pin-positioner'));
  tabBtnWorldsExplorer.addEventListener('click', () => switchTab('worlds-explorer'));

  // Create New Level Modal Events
  if (btnOpenNewLevelModal) btnOpenNewLevelModal.addEventListener('click', openNewLevelModal);
  if (btnCloseNewLevel) btnCloseNewLevel.addEventListener('click', () => modalNewLevel.classList.add('hidden'));
  if (btnCancelNewLevel) btnCancelNewLevel.addEventListener('click', () => modalNewLevel.classList.add('hidden'));
  if (btnConfirmCreateLevel) btnConfirmCreateLevel.addEventListener('click', handleCreateNewLevel);
  if (inputNewLevelNum) inputNewLevelNum.addEventListener('input', updateNewLevelWorldHint);

  // Level Designer Palette & Tools
  document.querySelectorAll('.palette-item').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.palette-item').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      appState.levelDesigner.activeColor = btn.dataset.code;
      if (btn.dataset.code === '.') {
        setLevelTool('eraser');
      } else {
        setLevelTool('brush');
      }
    });
  });

  document.getElementById('toolBrush').addEventListener('click', () => setLevelTool('brush'));
  document.getElementById('toolFill').addEventListener('click', () => setLevelTool('fill'));
  document.getElementById('toolEraser').addEventListener('click', () => setLevelTool('eraser'));
  document.getElementById('toolMirror').addEventListener('click', toggleLevelMirror);

  // Level Row operations
  document.getElementById('btnAddRowTop').addEventListener('click', addRowTop);
  document.getElementById('btnAddRowBottom').addEventListener('click', addRowBottom);
  document.getElementById('btnRemoveRowTop').addEventListener('click', removeRowTop);
  document.getElementById('btnRemoveRowBottom').addEventListener('click', removeRowBottom);
  document.getElementById('btnClearBoard').addEventListener('click', clearBoard);
  document.getElementById('btnRandomize').addEventListener('click', randomizeBoard);

  document.getElementById('btnShiftUp2').addEventListener('click', () => shiftRows(2, -1));
  document.getElementById('btnShiftDown2').addEventListener('click', () => shiftRows(2, 1));
  document.getElementById('btnShiftLeft').addEventListener('click', () => shiftCols(-1));
  document.getElementById('btnShiftRight').addEventListener('click', () => shiftCols(1));

  // Level Navigation & Save
  btnPrevLevel.addEventListener('click', () => changeLevel(-1));
  btnNextLevel.addEventListener('click', () => changeLevel(1));
  btnLoadLevelDirect.addEventListener('click', () => loadLevel(parseInt(inputLevelNum.value) || 1));
  btnSaveLevelDirect.addEventListener('click', saveActiveLevel);

  // Level Config Inputs
  inputShots.addEventListener('input', () => appState.levelDesigner.shots = parseInt(inputShots.value) || 20);
  selectObjectiveType.addEventListener('change', onObjectiveTypeChange);
  selectObjectiveColor.addEventListener('change', () => appState.levelDesigner.objective.color = selectObjectiveColor.value);
  inputObjectiveTarget.addEventListener('input', () => appState.levelDesigner.objective.target = parseInt(inputObjectiveTarget.value) || 0);

  btnAutoColors.addEventListener('click', autoDetectLevelColors);
  btnAutoStars.addEventListener('click', autoCalculateLevelStars);

  // Raw JSON Modal
  btnOpenRawJson.addEventListener('click', openRawJsonModal);
  btnCloseRawJson.addEventListener('click', () => modalRawJson.classList.add('hidden'));
  btnCopyRawJson.addEventListener('click', copyRawJson);

  // Pin Positioner Controls
  selectPinWorld.addEventListener('change', onWorldSelectChange);
  if (btnPrevWorld) btnPrevWorld.addEventListener('click', () => changeWorld(-1));
  if (btnNextWorld) btnNextWorld.addEventListener('click', () => changeWorld(1));
  if (btnFloatingPrevWorld) btnFloatingPrevWorld.addEventListener('click', () => changeWorld(-1));
  if (btnFloatingNextWorld) btnFloatingNextWorld.addEventListener('click', () => changeWorld(1));
  btnSavePinsDirect.addEventListener('click', saveActiveWorldPins);
  inputPinX.addEventListener('input', onPinCoordinateInput);
  inputPinY.addEventListener('input', onPinCoordinateInput);
  if (inputGiftName) inputGiftName.addEventListener('input', onGiftDetailsInput);
  if (inputGiftOffset) inputGiftOffset.addEventListener('input', onGiftDetailsInput);
  if (inputGiftCoins) inputGiftCoins.addEventListener('input', onGiftDetailsInput);

  btnZoomIn.addEventListener('click', () => adjustPinZoom(0.15));
  btnZoomOut.addEventListener('click', () => adjustPinZoom(-0.15));
  btnZoomFit.addEventListener('click', () => adjustPinZoom(0, true));

  // Draggable Pin Overlay Events
  pinsOverlay.addEventListener('mousemove', onPinsOverlayMouseMove);
}

// ============================================================================
// PROJECT SCANNING & DIRECTORY CONNECTION
// ============================================================================
async function onBrowseProject() {
  if (!window.gameStudioAPI) {
    showToast("Electron API not available (Browser mode)", true);
    return;
  }
  const res = await window.gameStudioAPI.selectProject();
  if (!res.canceled && res.path) {
    await scanAndConnectProject(res.path);
  }
}

async function scanAndConnectProject(targetPath) {
  inputProjectPath.value = targetPath;
  appState.projectPath = targetPath;

  if (!window.gameStudioAPI) {
    showToast("Running in preview mode", false);
    return;
  }

  const result = await window.gameStudioAPI.scanProject(targetPath);
  if (!result.success) {
    badgeProjectStatus.className = "flex items-center gap-1.5 bg-red-950/60 border border-red-800/60 px-2.5 py-1 rounded-lg text-red-300 text-xs font-mono";
    txtProjectStatus.textContent = "Not Found";
    showToast(`Error: ${result.error}`, true);
    return;
  }

  appState.resolvedPaths = result.resolvedPaths;
  appState.levelCount = result.levelCount;
  appState.worldCount = result.worldCount;
  appState.worldsData = result.worldsData;

  badgeProjectStatus.className = "flex items-center gap-1.5 bg-emerald-950/60 border border-emerald-800/60 px-2.5 py-1 rounded-lg text-emerald-300 text-xs font-mono";
  txtProjectStatus.textContent = "Connected";
  lblWorldCount.textContent = result.worldCount;
  lblLevelCount.textContent = result.levelCount;

  // Populate Pin Positioner World Selector
  populateWorldSelector();
  // Populate Campaign Explorer Grid
  renderCampaignWorldsGrid();
  // Load Level 1 into designer
  await loadLevel(1);
  // Load World 1 into pin positioner
  await loadWorldForPinPositioner(1);

  showToast(`Project Connected: ${result.worldCount} Worlds, ${result.levelCount} Levels`);
}

// ============================================================================
// TAB NAVIGATION
// ============================================================================
function switchTab(tabId) {
  appState.currentTab = tabId;

  [tabBtnLevelDesigner, tabBtnPinPositioner, tabBtnWorldsExplorer].forEach(b => {
    b.className = "tab-btn px-4 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-2 transition text-slate-400 hover:text-slate-200 hover:bg-slate-800/60";
  });

  viewLevelDesigner.classList.add('hidden');
  viewPinPositioner.classList.add('hidden');
  viewWorldsExplorer.classList.add('hidden');

  if (tabId === 'level-designer') {
    tabBtnLevelDesigner.className = "tab-btn px-4 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-2 transition bg-indigo-600 text-white shadow";
    viewLevelDesigner.classList.remove('hidden');
    renderLevelBoard();
  } else if (tabId === 'pin-positioner') {
    tabBtnPinPositioner.className = "tab-btn px-4 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-2 transition bg-indigo-600 text-white shadow";
    viewPinPositioner.classList.remove('hidden');
    loadWorldForPinPositioner(appState.pinPositioner.selectedWorldId);
  } else if (tabId === 'worlds-explorer') {
    tabBtnWorldsExplorer.className = "tab-btn px-4 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-2 transition bg-indigo-600 text-white shadow";
    viewWorldsExplorer.classList.remove('hidden');
    renderCampaignWorldsGrid();
  }
}

// ============================================================================
// MODULE 1: VISUAL LEVEL DESIGNER
// ============================================================================
function setLevelTool(tool) {
  appState.levelDesigner.activeTool = tool;
  const bBrush = document.getElementById('toolBrush');
  const bFill = document.getElementById('toolFill');
  const bEraser = document.getElementById('toolEraser');

  [bBrush, bFill, bEraser].forEach(b => {
    b.className = "p-2 rounded-lg bg-slate-700 hover:bg-slate-600 text-slate-200 font-semibold flex flex-col items-center gap-1 transition";
  });

  if (tool === 'brush') bBrush.className = "p-2 rounded-lg bg-indigo-600 text-white font-semibold flex flex-col items-center gap-1 shadow";
  if (tool === 'fill') bFill.className = "p-2 rounded-lg bg-indigo-600 text-white font-semibold flex flex-col items-center gap-1 shadow";
  if (tool === 'eraser') bEraser.className = "p-2 rounded-lg bg-indigo-600 text-white font-semibold flex flex-col items-center gap-1 shadow";
}

function toggleLevelMirror() {
  appState.levelDesigner.mirrorMode = !appState.levelDesigner.mirrorMode;
  const b = document.getElementById('toolMirror');
  const txt = document.getElementById('txtMirror');
  if (appState.levelDesigner.mirrorMode) {
    b.className = "w-full mt-2 p-1.5 rounded bg-amber-600 text-white font-semibold transition text-[11px] flex items-center justify-center gap-1.5 shadow";
    txt.textContent = "Mirror: ON";
  } else {
    b.className = "w-full mt-2 p-1.5 rounded bg-slate-700 hover:bg-slate-600 text-slate-300 font-semibold transition text-[11px] flex items-center justify-center gap-1.5";
    txt.textContent = "Mirror: OFF";
  }
}

function renderLevelBoard() {
  hexContainer.innerHTML = "";
  rowBadgesContainer.innerHTML = "";

  const connectedSet = computeConnectedBubbles();
  let floatingCount = 0;

  appState.levelDesigner.rows.forEach((rowStr, rIdx) => {
    const rowEl = document.createElement("div");
    const isEven = rIdx % 2 === 0;
    const maxCols = isEven ? 9 : 8;

    rowEl.className = "hex-row " + (isEven ? "hex-row-even" : "hex-row-odd");
    rowEl.dataset.row = rIdx;

    // Ensure strict 9/8 parity lengths
    while (rowStr.length < maxCols) rowStr += ".";
    appState.levelDesigner.rows[rIdx] = rowStr.substring(0, maxCols);

    // Row Parity Badge
    const badgeEl = document.createElement("div");
    badgeEl.className = `h-[38.1px] flex items-center justify-end font-mono text-[10px] font-bold ${
      isEven ? "text-emerald-400" : "text-amber-400"
    }`;
    badgeEl.innerHTML = `<span class="px-1.5 py-0.5 rounded bg-slate-800 border ${
      isEven ? "border-emerald-700/60" : "border-amber-700/60"
    }">R${rIdx} (${maxCols})</span>`;
    badgeEl.title = `Row ${rIdx}: ${isEven ? "EVEN (9 Cols, Full Width)" : "ODD (8 Cols, Indented 22px)"}`;
    rowBadgesContainer.appendChild(badgeEl);

    // Cells
    for (let cIdx = 0; cIdx < maxCols; cIdx++) {
      const char = appState.levelDesigner.rows[rIdx][cIdx] || ".";
      const isConnected = (char === ".") || connectedSet.has(`${rIdx},${cIdx}`);
      if (!isConnected) floatingCount++;

      const cellEl = createBubbleElement(char, rIdx, cIdx, isConnected);
      rowEl.appendChild(cellEl);
    }

    hexContainer.appendChild(rowEl);
  });

  updateLevelStats(floatingCount);
  txtBoardDimensions.textContent = `9 Columns • ${appState.levelDesigner.rows.length} Rows`;

  const worldNum = Math.floor((appState.levelDesigner.levelNumber - 1) / 10) + 1;
  badgeCurrentWorld.textContent = `World ${worldNum}`;
}

function createBubbleElement(char, rIdx, cIdx, isConnected) {
  const el = document.createElement("div");
  const info = COLOR_MAP[char] || COLOR_MAP["."];

  el.className = "bubble " + info.class;
  if (!isConnected) el.classList.add("floating-warning");
  el.dataset.row = rIdx;
  el.dataset.col = cIdx;
  el.dataset.code = char;
  el.title = `${info.name} (${char})`;

  if (char === ".") el.textContent = "✕";

  el.addEventListener("mousedown", (e) => {
    if (e.button === 2) {
      paintCell(rIdx, cIdx, ".");
    } else {
      handleCellAction(rIdx, cIdx);
    }
  });

  el.addEventListener("mouseenter", () => {
    highlightHexNeighbors(rIdx, cIdx);
    updateHoverHUD(rIdx, cIdx, char);
    if (appState.levelDesigner.isMouseDown) {
      handleCellAction(rIdx, cIdx);
    }
  });

  el.addEventListener("mouseleave", clearHexNeighborHighlights);

  return el;
}

function updateHoverHUD(r, c, char) {
  const isEven = r % 2 === 0;
  const info = COLOR_MAP[char] || COLOR_MAP["."];
  const centerX = isEven ? (22 + c * 44) : (44 + c * 44);
  const centerY = Math.round(22 + r * 38.1);
  txtHoverInfo.innerHTML = `Row <strong class="${isEven ? 'text-emerald-300' : 'text-amber-300'}">${r}</strong> [${isEven ? 'EVEN: 9 Cols' : 'ODD: 8 Cols'}], Col <strong>${c}</strong> &bull; Center: X=<strong>${centerX}px</strong>, Y=<strong>${centerY}px</strong> &bull; Bubble: <span class="font-bold text-white">${info.emoji} ${info.name}</span>`;
}

function highlightHexNeighbors(r, c) {
  clearHexNeighborHighlights();
  const neighbors = getHexNeighbors(r, c);
  neighbors.forEach(([nr, nc]) => {
    const cell = hexContainer.querySelector(`.bubble[data-row="${nr}"][data-col="${nc}"]`);
    if (cell) cell.classList.add("neighbor-highlight");
  });
}

function clearHexNeighborHighlights() {
  hexContainer.querySelectorAll(".neighbor-highlight").forEach(el => el.classList.remove("neighbor-highlight"));
}

function handleCellAction(r, c) {
  if (appState.levelDesigner.activeTool === "eraser") {
    paintCell(r, c, ".");
  } else if (appState.levelDesigner.activeTool === "fill") {
    floodFill(r, c, appState.levelDesigner.activeColor);
  } else {
    paintCell(r, c, appState.levelDesigner.activeColor);
  }
}

function paintCell(r, c, code) {
  const maxCols = (r % 2 === 0) ? 9 : 8;
  if (r < 0 || r >= appState.levelDesigner.rows.length || c < 0 || c >= maxCols) return;

  let rowStr = appState.levelDesigner.rows[r];
  rowStr = rowStr.substring(0, c) + code + rowStr.substring(c + 1);
  appState.levelDesigner.rows[r] = rowStr;

  if (appState.levelDesigner.mirrorMode) {
    const mirrorC = maxCols - 1 - c;
    if (mirrorC !== c) {
      rowStr = appState.levelDesigner.rows[r];
      rowStr = rowStr.substring(0, mirrorC) + code + rowStr.substring(mirrorC + 1);
      appState.levelDesigner.rows[r] = rowStr;
    }
  }

  renderLevelBoard();
}

function floodFill(startR, startC, targetColor) {
  const sourceColor = appState.levelDesigner.rows[startR][startC];
  if (sourceColor === targetColor) return;

  const visited = new Set();
  const queue = [[startR, startC]];

  while (queue.length > 0) {
    const [r, c] = queue.shift();
    const key = `${r},${c}`;
    if (visited.has(key)) continue;
    visited.add(key);

    const maxCols = (r % 2 === 0) ? 9 : 8;
    if (r < 0 || r >= appState.levelDesigner.rows.length || c < 0 || c >= maxCols) continue;
    if (appState.levelDesigner.rows[r][c] !== sourceColor) continue;

    let rowStr = appState.levelDesigner.rows[r];
    appState.levelDesigner.rows[r] = rowStr.substring(0, c) + targetColor + rowStr.substring(c + 1);

    const neighbors = getHexNeighbors(r, c);
    for (const [nr, nc] of neighbors) {
      queue.push([nr, nc]);
    }
  }

  renderLevelBoard();
}

function getHexNeighbors(r, c) {
  const isEven = (r % 2 === 0);
  let neighbors;
  if (isEven) {
    neighbors = [
      [r, c - 1], [r, c + 1],
      [r - 1, c - 1], [r - 1, c],
      [r + 1, c - 1], [r + 1, c]
    ];
  } else {
    neighbors = [
      [r, c - 1], [r, c + 1],
      [r - 1, c], [r - 1, c + 1],
      [r + 1, c], [r + 1, c + 1]
    ];
  }

  return neighbors.filter(([nr, nc]) => {
    if (nr < 0 || nr >= appState.levelDesigner.rows.length) return false;
    const maxCols = (nr % 2 === 0) ? 9 : 8;
    return nc >= 0 && nc < maxCols;
  });
}

function computeConnectedBubbles() {
  const connected = new Set();
  const queue = [];

  if (appState.levelDesigner.rows.length > 0) {
    const row0 = appState.levelDesigner.rows[0];
    for (let c = 0; c < row0.length; c++) {
      if (row0[c] !== ".") {
        connected.add(`0,${c}`);
        queue.push([0, c]);
      }
    }
  }

  while (queue.length > 0) {
    const [r, c] = queue.shift();
    const neighbors = getHexNeighbors(r, c);
    for (const [nr, nc] of neighbors) {
      const key = `${nr},${nc}`;
      if (!connected.has(key) && appState.levelDesigner.rows[nr] && appState.levelDesigner.rows[nr][nc] && appState.levelDesigner.rows[nr][nc] !== ".") {
        connected.add(key);
        queue.push([nr, nc]);
      }
    }
  }

  return connected;
}

// Row Operations
function addRowTop() {
  const adjusted = [".".repeat(9)];
  for (let i = 0; i < appState.levelDesigner.rows.length; i++) {
    const targetLen = ((i + 1) % 2 === 0) ? 9 : 8;
    let str = appState.levelDesigner.rows[i];
    str = (str + ".".repeat(targetLen)).substring(0, targetLen);
    adjusted.push(str);
  }
  appState.levelDesigner.rows = adjusted;
  renderLevelBoard();
}

function addRowBottom() {
  const isEven = (appState.levelDesigner.rows.length % 2 === 0);
  const cols = isEven ? 9 : 8;
  appState.levelDesigner.rows.push(".".repeat(cols));
  renderLevelBoard();
}

function removeRowTop() {
  if (appState.levelDesigner.rows.length > 1) {
    appState.levelDesigner.rows.shift();
    appState.levelDesigner.rows = appState.levelDesigner.rows.map((row, idx) => {
      const targetLen = (idx % 2 === 0) ? 9 : 8;
      return (row + ".".repeat(targetLen)).substring(0, targetLen);
    });
    renderLevelBoard();
  }
}

function removeRowBottom() {
  if (appState.levelDesigner.rows.length > 1) {
    appState.levelDesigner.rows.pop();
    renderLevelBoard();
  }
}

function clearBoard() {
  if (!confirm("Clear all bubbles on this board?")) return;
  appState.levelDesigner.rows = appState.levelDesigner.rows.map((row, idx) => ".".repeat(idx % 2 === 0 ? 9 : 8));
  renderLevelBoard();
}

function randomizeBoard() {
  const activeChoices = appState.levelDesigner.colors.map(c => {
    for (const [code, val] of Object.entries(COLOR_MAP)) {
      if (val.name.toUpperCase() === c) return code;
    }
    return "R";
  });

  appState.levelDesigner.rows = appState.levelDesigner.rows.map((row, idx) => {
    const cols = (idx % 2 === 0) ? 9 : 8;
    let str = "";
    for (let c = 0; c < cols; c++) {
      if (idx < 6 && Math.random() < 0.82) {
        str += activeChoices[Math.floor(Math.random() * activeChoices.length)];
      } else {
        str += ".";
      }
    }
    return str;
  });
  renderLevelBoard();
}

function shiftRows(count, direction) {
  if (count === 2) {
    if (direction === -1) {
      if (appState.levelDesigner.rows.length > 2) {
        appState.levelDesigner.rows.shift();
        appState.levelDesigner.rows.shift();
        appState.levelDesigner.rows.push(".".repeat((appState.levelDesigner.rows.length % 2 === 0) ? 9 : 8));
        appState.levelDesigner.rows.push(".".repeat((appState.levelDesigner.rows.length % 2 === 0) ? 9 : 8));
      }
    } else {
      appState.levelDesigner.rows.unshift(".".repeat(8));
      appState.levelDesigner.rows.unshift(".".repeat(9));
      appState.levelDesigner.rows = appState.levelDesigner.rows.map((row, idx) => {
        const cols = (idx % 2 === 0) ? 9 : 8;
        return (row + ".".repeat(cols)).substring(0, cols);
      });
    }
  }
  renderLevelBoard();
}

function shiftCols(direction) {
  appState.levelDesigner.rows = appState.levelDesigner.rows.map((row, idx) => {
    const maxCols = (idx % 2 === 0) ? 9 : 8;
    if (direction === 1) {
      return ("." + row).substring(0, maxCols);
    } else {
      return (row.substring(1) + ".");
    }
  });
  renderLevelBoard();
}

function updateLevelStats(floatingCount = 0) {
  let total = 0;
  const counts = {};

  appState.levelDesigner.rows.forEach(r => {
    for (let ch of r) {
      if (ch !== ".") {
        total++;
        counts[ch] = (counts[ch] || 0) + 1;
      }
    }
  });

  lblTotalBubbles.textContent = total;

  pillsColorBreakdown.innerHTML = "";
  Object.keys(counts).sort().forEach(code => {
    const info = COLOR_MAP[code] || { name: code, emoji: "●" };
    const badge = document.createElement("span");
    badge.className = "px-1.5 py-0.5 rounded bg-slate-800 border border-slate-700 text-slate-300 flex items-center gap-1";
    badge.innerHTML = `<span>${info.emoji}</span><span>${counts[code]}</span>`;
    pillsColorBreakdown.appendChild(badge);
  });

  if (floatingCount > 0) {
    lblConnectionStatus.className = "flex items-center gap-1.5 text-amber-400 font-bold text-[11px]";
    lblConnectionStatus.innerHTML = `<span>⚠️ ${floatingCount} unattached bubble${floatingCount > 1 ? 's' : ''} (will drop on start)</span>`;
  } else {
    lblConnectionStatus.className = "flex items-center gap-1.5 text-emerald-400 font-medium text-[11px]";
    lblConnectionStatus.innerHTML = `<span>✓ All bubbles properly anchored to ceiling</span>`;
  }
}

// Level Load & Save
async function changeLevel(delta) {
  const cur = parseInt(inputLevelNum.value) || 1;
  const next = Math.max(1, Math.min(490, cur + delta));
  inputLevelNum.value = next;
  await loadLevel(next);
}

async function loadLevel(levelNum) {
  appState.levelDesigner.levelNumber = levelNum;
  inputLevelNum.value = levelNum;

  if (!window.gameStudioAPI || !appState.resolvedPaths) {
    renderLevelBoard();
    return;
  }

  const res = await window.gameStudioAPI.loadLevel({
    levelsDir: appState.resolvedPaths.levels,
    levelNumber: levelNum
  });

  if (!res.success) {
    showToast(`Level ${levelNum} not found, template loaded`, true);
    renderLevelBoard();
    return;
  }

  const data = res.data;
  appState.levelDesigner.shots = data.shots || 20;
  inputShots.value = appState.levelDesigner.shots;

  if (data.colors) {
    appState.levelDesigner.colors = data.colors;
    ["RED", "GREEN", "BLUE", "YELLOW", "PURPLE", "ORANGE"].forEach(c => {
      const el = document.getElementById("chkCol" + c);
      if (el) el.checked = data.colors.includes(c);
    });
  }

  if (data.objective) {
    appState.levelDesigner.objective = data.objective;
    selectObjectiveType.value = data.objective.type || "CLEAR_ALL";
    onObjectiveTypeChange();
    if (data.objective.target) inputObjectiveTarget.value = data.objective.target;
    if (data.objective.color) selectObjectiveColor.value = data.objective.color;
  }

  if (data.starThresholds && data.starThresholds.length >= 3) {
    appState.levelDesigner.starThresholds = data.starThresholds;
    inputStar1.value = data.starThresholds[0];
    inputStar2.value = data.starThresholds[1];
    inputStar3.value = data.starThresholds[2];
  }

  if (data.rows && Array.isArray(data.rows)) {
    appState.levelDesigner.rows = data.rows.map((row, idx) => {
      const expected = (idx % 2 === 0) ? 9 : 8;
      return (row + ".".repeat(expected)).substring(0, expected);
    });
  }

  renderLevelBoard();
  showToast(`Loaded Level ${levelNum}`);
}

async function saveActiveLevel() {
  if (!window.gameStudioAPI || !appState.resolvedPaths) {
    showToast("Cannot save: No active project connection", true);
    return;
  }

  syncColorsFromCheckboxes();

  const sanitizedRows = appState.levelDesigner.rows.map((r, idx) => {
    const expected = (idx % 2 === 0) ? 9 : 8;
    return (r + ".".repeat(expected)).substring(0, expected);
  });

  const levelData = {
    level: appState.levelDesigner.levelNumber,
    shots: parseInt(inputShots.value) || appState.levelDesigner.shots,
    colors: appState.levelDesigner.colors,
    objective: {
      type: selectObjectiveType.value,
      target: parseInt(inputObjectiveTarget.value) || 0
    },
    starThresholds: [
      parseInt(inputStar1.value) || appState.levelDesigner.starThresholds[0],
      parseInt(inputStar2.value) || appState.levelDesigner.starThresholds[1],
      parseInt(inputStar3.value) || appState.levelDesigner.starThresholds[2]
    ],
    rows: sanitizedRows
  };

  if (selectObjectiveType.value === "POP_COLOR") {
    levelData.objective.color = selectObjectiveColor.value;
  }

  const res = await window.gameStudioAPI.saveLevel({
    levelsDir: appState.resolvedPaths.levels,
    levelNumber: appState.levelDesigner.levelNumber,
    data: levelData
  });

  if (res.success) {
    showToast(`✓ Saved Level ${res.levelNumber} to disk directly!`);
  } else {
    showToast(`Save failed: ${res.error}`, true);
  }
}

function onObjectiveTypeChange() {
  const type = selectObjectiveType.value;
  appState.levelDesigner.objective.type = type;

  if (type === "CLEAR_ALL") {
    boxObjectiveTarget.classList.add("hidden");
    boxObjectiveColor.classList.add("hidden");
  } else if (type === "SCORE_TARGET") {
    boxObjectiveTarget.classList.remove("hidden");
    boxObjectiveColor.classList.add("hidden");
    document.getElementById("lblObjectiveTarget").textContent = "Target Score (Points)";
  } else if (type === "POP_COLOR") {
    boxObjectiveTarget.classList.remove("hidden");
    boxObjectiveColor.classList.remove("hidden");
    document.getElementById("lblObjectiveTarget").textContent = "Number of Bubbles to Pop";
  } else if (type === "DROP_COUNT") {
    boxObjectiveTarget.classList.remove("hidden");
    boxObjectiveColor.classList.add("hidden");
    document.getElementById("lblObjectiveTarget").textContent = "Number of Bubbles to Drop";
  }
}

function syncColorsFromCheckboxes() {
  const selected = [];
  ["RED", "GREEN", "BLUE", "YELLOW", "PURPLE", "ORANGE"].forEach(c => {
    const el = document.getElementById("chkCol" + c);
    if (el && el.checked) selected.push(c);
  });
  appState.levelDesigner.colors = selected.length > 0 ? selected : ["RED", "BLUE", "YELLOW"];
}

function autoDetectLevelColors() {
  const found = new Set();
  const codeToName = { R: "RED", G: "GREEN", B: "BLUE", Y: "YELLOW", P: "PURPLE", O: "ORANGE" };
  appState.levelDesigner.rows.forEach(r => {
    for (let ch of r) {
      if (codeToName[ch]) found.add(codeToName[ch]);
    }
  });

  ["RED", "GREEN", "BLUE", "YELLOW", "PURPLE", "ORANGE"].forEach(c => {
    const el = document.getElementById("chkCol" + c);
    if (el) el.checked = found.has(c);
  });
  syncColorsFromCheckboxes();
  showToast("Colors auto-detected from board!");
}

function autoCalculateLevelStars() {
  let bubbleCount = 0;
  appState.levelDesigner.rows.forEach(r => {
    for (let ch of r) if (ch !== ".") bubbleCount++;
  });

  const baseScore = bubbleCount * 100 + (parseInt(inputShots.value) || 20) * 150;
  const s1 = Math.round(baseScore * 0.8);
  const s2 = Math.round(baseScore * 1.8);
  const s3 = Math.round(baseScore * 3.2);

  inputStar1.value = s1;
  inputStar2.value = s2;
  inputStar3.value = s3;
  showToast("Star thresholds auto-calculated!");
}

function openRawJsonModal() {
  syncColorsFromCheckboxes();
  const sanitizedRows = appState.levelDesigner.rows.map((r, idx) => {
    const expected = (idx % 2 === 0) ? 9 : 8;
    return (r + ".".repeat(expected)).substring(0, expected);
  });

  const obj = {
    level: appState.levelDesigner.levelNumber,
    shots: parseInt(inputShots.value) || appState.levelDesigner.shots,
    colors: appState.levelDesigner.colors,
    objective: {
      type: selectObjectiveType.value,
      target: parseInt(inputObjectiveTarget.value) || 0
    },
    starThresholds: [
      parseInt(inputStar1.value) || 2000,
      parseInt(inputStar2.value) || 4500,
      parseInt(inputStar3.value) || 8000
    ],
    rows: sanitizedRows
  };

  txtRawJsonContent.value = JSON.stringify(obj, null, 2);
  document.getElementById('lblModalLevelFilename').textContent = `level_${appState.levelDesigner.levelNumber}.json`;
  modalRawJson.classList.remove('hidden');
}

function copyRawJson() {
  navigator.clipboard.writeText(txtRawJsonContent.value).then(() => {
    showToast("Level JSON copied to clipboard!");
  });
}

// ============================================================================
// MODULE 2: SAGA MAP PIN POSITIONER
// ============================================================================
function populateWorldSelector() {
  selectPinWorld.innerHTML = "";
  if (!appState.worldsData || !appState.worldsData.worlds) return;

  appState.worldsData.worlds.forEach(w => {
    const worldNum = w.worldNumber || w.id;
    const levels = w.levels || w.nodes || [];
    const startLevel = levels.length > 0 ? levels[0].level : ((worldNum - 1) * 10 + 1);
    const endLevel = levels.length > 0 ? levels[levels.length - 1].level : (worldNum * 10);
    const opt = document.createElement("option");
    opt.value = worldNum;
    opt.textContent = `World ${worldNum}: ${w.name} (Lvl ${startLevel}-${endLevel})`;
    selectPinWorld.appendChild(opt);
  });
}

async function onWorldSelectChange() {
  const worldId = parseInt(selectPinWorld.value) || 1;
  await loadWorldForPinPositioner(worldId);
}

async function changeWorld(delta) {
  if (!appState.worldsData || !appState.worldsData.worlds || appState.worldsData.worlds.length === 0) return;
  const currentWorld = appState.pinPositioner.selectedWorldId || 1;
  const totalWorlds = appState.worldsData.worlds.length;
  let nextWorld = currentWorld + delta;
  if (nextWorld < 1) nextWorld = totalWorlds;
  if (nextWorld > totalWorlds) nextWorld = 1;
  await loadWorldForPinPositioner(nextWorld);
}

async function loadWorldForPinPositioner(worldId) {
  appState.pinPositioner.selectedWorldId = worldId;
  selectPinWorld.value = worldId;

  if (!appState.worldsData || !appState.worldsData.worlds) return;
  const world = appState.worldsData.worlds.find(w => (w.worldNumber === worldId || w.id === worldId));
  if (!world) return;

  const levels = world.levels || world.nodes || [];
  const startLevel = levels.length > 0 ? levels[0].level : ((worldId - 1) * 10 + 1);
  const endLevel = levels.length > 0 ? levels[levels.length - 1].level : (worldId * 10);

  lblWorldLevelRange.textContent = `${startLevel} – ${endLevel}`;
  const bgName = world.mapBackground || `bg_map_world_${worldId}`;
  lblWorldDrawableName.textContent = bgName;

  // Ensure every world has 2 gifts
  if (!world.gifts || world.gifts.length < 2) {
    if (!world.gifts) world.gifts = [];
    if (world.gifts.length === 0) {
      world.gifts.push({
        giftIndex: 1,
        name: "Mid-World Chest",
        x: 0.337,
        y: 0.3547,
        requiredLevelOffset: 6,
        rewardCoins: 100
      });
    }
    if (world.gifts.length === 1) {
      world.gifts.push({
        giftIndex: 2,
        name: "Castle Gate Chest",
        x: 0.5583,
        y: 0.1331,
        requiredLevelOffset: 9,
        rewardCoins: 200
      });
    }
  }

  // Load World Map Background as base64
  if (window.gameStudioAPI && appState.resolvedPaths) {
    const imgRes = await window.gameStudioAPI.loadImageBase64({
      drawableDir: appState.resolvedPaths.drawable,
      imageName: bgName
    });
    if (imgRes && imgRes.success) {
      imgMapBackground.src = imgRes.dataUrl;
    } else {
      imgMapBackground.src = "";
    }
  }

  renderPinsOverlay(world);
  renderWorldNodesList(world);
  renderWorldGiftsList(world);
  selectPinNode(0);
}

function renderPinsOverlay(world) {
  pinsOverlay.innerHTML = "";
  const nodes = world.levels || world.nodes;

  // 1. Render Level Pins
  if (nodes && nodes.length) {
    nodes.forEach((node, idx) => {
      const pinEl = document.createElement("div");
      pinEl.className = "level-pin pin-completed";
      pinEl.dataset.type = "node";
      pinEl.dataset.index = idx;
      pinEl.textContent = node.level;
      const pctX = node.x > 1.0 ? node.x : (node.x * 100);
      const pctY = node.y > 1.0 ? node.y : (node.y * 100);
      pinEl.style.left = `${pctX}%`;
      pinEl.style.top = `${pctY}%`;

      pinEl.addEventListener("mousedown", (e) => {
        e.stopPropagation();
        selectPinNode(idx);
        appState.pinPositioner.isDragging = true;
        appState.pinPositioner.dragType = 'node';
        appState.pinPositioner.dragIndex = idx;
      });

      pinsOverlay.appendChild(pinEl);
    });
  }

  // 2. Render 2 Gift Chest Pins
  if (world.gifts && world.gifts.length) {
    world.gifts.forEach((gift, gIdx) => {
      const giftEl = document.createElement("div");
      giftEl.className = "gift-pin";
      giftEl.dataset.type = "gift";
      giftEl.dataset.index = gIdx;
      giftEl.title = `🎁 Gift ${gift.giftIndex}: ${gift.name} (Offset +${gift.requiredLevelOffset}, ${gift.rewardCoins} coins)`;

      giftEl.innerHTML = `<img src="../assets/chest_gift.svg" alt="Chest" />`;
      const pctX = gift.x > 1.0 ? gift.x : (gift.x * 100);
      const pctY = gift.y > 1.0 ? gift.y : (gift.y * 100);
      giftEl.style.left = `${pctX}%`;
      giftEl.style.top = `${pctY}%`;

      giftEl.addEventListener("mousedown", (e) => {
        e.stopPropagation();
        selectGiftNode(gIdx);
        appState.pinPositioner.isDragging = true;
        appState.pinPositioner.dragType = 'gift';
        appState.pinPositioner.dragIndex = gIdx;
      });

      pinsOverlay.appendChild(giftEl);
    });
  }
}

function selectPinNode(nodeIdx) {
  appState.pinPositioner.selectedType = 'node';
  appState.pinPositioner.selectedNodeIndex = nodeIdx;
  if (boxGiftDetails) boxGiftDetails.classList.add('hidden');

  const world = appState.worldsData.worlds.find(w => (w.worldNumber === appState.pinPositioner.selectedWorldId || w.id === appState.pinPositioner.selectedWorldId));
  const nodes = world ? (world.levels || world.nodes) : null;
  if (!world || !nodes || !nodes[nodeIdx]) return;

  const node = nodes[nodeIdx];
  badgeSelectedPinLevel.className = "bg-amber-900/60 text-amber-300 px-2 py-0.5 rounded font-mono text-xs font-bold";
  badgeSelectedPinLevel.textContent = `Level ${node.level}`;
  const pctX = node.x > 1.0 ? node.x : (node.x * 100);
  const pctY = node.y > 1.0 ? node.y : (node.y * 100);
  inputPinX.value = pctX.toFixed(1);
  inputPinY.value = pctY.toFixed(1);

  // Update pin visual selection
  pinsOverlay.querySelectorAll('.level-pin, .gift-pin').forEach(p => p.classList.remove('pin-selected'));
  if (listWorldNodes) listWorldNodes.querySelectorAll('.node-row').forEach(r => r.classList.remove('bg-indigo-900/60', 'border-indigo-600'));
  if (listWorldGifts) listWorldGifts.querySelectorAll('.gift-row').forEach(r => r.classList.remove('bg-amber-900/60', 'border-amber-500'));

  const activePin = pinsOverlay.querySelector(`.level-pin[data-index="${nodeIdx}"]`);
  if (activePin) activePin.classList.add('pin-selected');

  if (listWorldNodes) {
    const activeRow = listWorldNodes.querySelector(`.node-row[data-index="${nodeIdx}"]`);
    if (activeRow) activeRow.classList.add('bg-indigo-900/60', 'border-indigo-600');
  }
}

function selectGiftNode(giftIdx) {
  appState.pinPositioner.selectedType = 'gift';
  appState.pinPositioner.selectedGiftIndex = giftIdx;
  if (boxGiftDetails) boxGiftDetails.classList.remove('hidden');

  const world = appState.worldsData.worlds.find(w => (w.worldNumber === appState.pinPositioner.selectedWorldId || w.id === appState.pinPositioner.selectedWorldId));
  if (!world || !world.gifts || !world.gifts[giftIdx]) return;

  const gift = world.gifts[giftIdx];
  badgeSelectedPinLevel.className = "bg-pink-900/60 text-pink-300 px-2 py-0.5 rounded font-mono text-xs font-bold";
  badgeSelectedPinLevel.textContent = `🎁 Gift ${gift.giftIndex}: ${gift.name}`;

  const pctX = gift.x > 1.0 ? gift.x : (gift.x * 100);
  const pctY = gift.y > 1.0 ? gift.y : (gift.y * 100);
  inputPinX.value = pctX.toFixed(1);
  inputPinY.value = pctY.toFixed(1);

  if (inputGiftName) inputGiftName.value = gift.name || `Gift ${gift.giftIndex}`;
  if (inputGiftOffset) inputGiftOffset.value = gift.requiredLevelOffset || (gift.giftIndex === 1 ? 6 : 9);
  if (inputGiftCoins) inputGiftCoins.value = gift.rewardCoins || (gift.giftIndex === 1 ? 100 : 200);

  // Update pin visual selection
  pinsOverlay.querySelectorAll('.level-pin, .gift-pin').forEach(p => p.classList.remove('pin-selected'));
  if (listWorldNodes) listWorldNodes.querySelectorAll('.node-row').forEach(r => r.classList.remove('bg-indigo-900/60', 'border-indigo-600'));
  if (listWorldGifts) listWorldGifts.querySelectorAll('.gift-row').forEach(r => r.classList.remove('bg-amber-900/60', 'border-amber-500'));

  const activePin = pinsOverlay.querySelector(`.gift-pin[data-index="${giftIdx}"]`);
  if (activePin) activePin.classList.add('pin-selected');

  if (listWorldGifts) {
    const activeRow = listWorldGifts.querySelector(`.gift-row[data-index="${giftIdx}"]`);
    if (activeRow) activeRow.classList.add('bg-amber-900/60', 'border-amber-500');
  }
}

function onPinCoordinateInput() {
  const world = appState.worldsData.worlds.find(w => (w.worldNumber === appState.pinPositioner.selectedWorldId || w.id === appState.pinPositioner.selectedWorldId));
  if (!world) return;

  const pctX = Math.max(1, Math.min(99, parseFloat(inputPinX.value) || 0));
  const pctY = Math.max(1, Math.min(99, parseFloat(inputPinY.value) || 0));
  const floatX = parseFloat((pctX / 100).toFixed(4));
  const floatY = parseFloat((pctY / 100).toFixed(4));

  if (appState.pinPositioner.selectedType === 'gift') {
    const gIdx = appState.pinPositioner.selectedGiftIndex;
    if (world.gifts && world.gifts[gIdx]) {
      world.gifts[gIdx].x = floatX;
      world.gifts[gIdx].y = floatY;
      const pinEl = pinsOverlay.querySelector(`.gift-pin[data-index="${gIdx}"]`);
      if (pinEl) {
        pinEl.style.left = `${pctX}%`;
        pinEl.style.top = `${pctY}%`;
      }
      renderWorldGiftsList(world);
    }
  } else {
    const idx = appState.pinPositioner.selectedNodeIndex;
    const nodes = world.levels || world.nodes;
    if (nodes && nodes[idx]) {
      nodes[idx].x = floatX;
      nodes[idx].y = floatY;
      const pinEl = pinsOverlay.querySelector(`.level-pin[data-index="${idx}"]`);
      if (pinEl) {
        pinEl.style.left = `${pctX}%`;
        pinEl.style.top = `${pctY}%`;
      }
      renderWorldNodesList(world);
    }
  }
}

function onPinsOverlayMouseMove(e) {
  if (!appState.pinPositioner.isDragging || appState.pinPositioner.dragIndex < 0) return;

  const rect = pinsOverlay.getBoundingClientRect();
  const rawX = ((e.clientX - rect.left) / rect.width) * 100;
  const rawY = ((e.clientY - rect.top) / rect.height) * 100;

  const pctX = Math.max(2, Math.min(98, parseFloat(rawX.toFixed(1))));
  const pctY = Math.max(2, Math.min(98, parseFloat(rawY.toFixed(1))));
  const floatX = parseFloat((pctX / 100).toFixed(4));
  const floatY = parseFloat((pctY / 100).toFixed(4));

  const world = appState.worldsData.worlds.find(w => (w.worldNumber === appState.pinPositioner.selectedWorldId || w.id === appState.pinPositioner.selectedWorldId));
  if (!world) return;

  if (appState.pinPositioner.dragType === 'gift') {
    const gIdx = appState.pinPositioner.dragIndex;
    if (world.gifts && world.gifts[gIdx]) {
      world.gifts[gIdx].x = floatX;
      world.gifts[gIdx].y = floatY;
      const pinEl = pinsOverlay.querySelector(`.gift-pin[data-index="${gIdx}"]`);
      if (pinEl) {
        pinEl.style.left = `${pctX}%`;
        pinEl.style.top = `${pctY}%`;
      }
      inputPinX.value = pctX;
      inputPinY.value = pctY;
    }
  } else {
    const idx = appState.pinPositioner.dragIndex;
    const nodes = world.levels || world.nodes;
    if (nodes && nodes[idx]) {
      nodes[idx].x = floatX;
      nodes[idx].y = floatY;
      const pinEl = pinsOverlay.querySelector(`.level-pin[data-index="${idx}"]`);
      if (pinEl) {
        pinEl.style.left = `${pctX}%`;
        pinEl.style.top = `${pctY}%`;
      }
      inputPinX.value = pctX;
      inputPinY.value = pctY;
    }
  }
}

function onGiftDetailsInput() {
  if (appState.pinPositioner.selectedType !== 'gift') return;
  const world = appState.worldsData.worlds.find(w => (w.worldNumber === appState.pinPositioner.selectedWorldId || w.id === appState.pinPositioner.selectedWorldId));
  const gIdx = appState.pinPositioner.selectedGiftIndex;
  if (!world || !world.gifts || !world.gifts[gIdx]) return;

  world.gifts[gIdx].name = inputGiftName.value.trim() || `Gift ${world.gifts[gIdx].giftIndex}`;
  world.gifts[gIdx].requiredLevelOffset = parseInt(inputGiftOffset.value) || 6;
  world.gifts[gIdx].rewardCoins = parseInt(inputGiftCoins.value) || 100;

  badgeSelectedPinLevel.textContent = `🎁 Gift ${world.gifts[gIdx].giftIndex}: ${world.gifts[gIdx].name}`;
  renderWorldGiftsList(world);
}

function renderWorldNodesList(world) {
  listWorldNodes.innerHTML = "";
  const nodes = world.levels || world.nodes;
  if (!nodes) return;

  nodes.forEach((node, idx) => {
    const row = document.createElement("div");
    row.className = "node-row p-1.5 rounded bg-slate-800 border border-slate-700/80 flex items-center justify-between cursor-pointer hover:bg-slate-700 transition";
    row.dataset.index = idx;
    const pctX = node.x > 1.0 ? node.x : (node.x * 100);
    const pctY = node.y > 1.0 ? node.y : (node.y * 100);
    row.innerHTML = `
      <span class="text-amber-300 font-bold">Lvl ${node.level}</span>
      <span class="text-slate-400">X: ${pctX.toFixed(1)}%</span>
      <span class="text-slate-400">Y: ${pctY.toFixed(1)}%</span>
    `;
    row.addEventListener("click", () => selectPinNode(idx));
    listWorldNodes.appendChild(row);
  });
}

function renderWorldGiftsList(world) {
  if (!listWorldGifts) return;
  listWorldGifts.innerHTML = "";
  if (!world.gifts) return;

  world.gifts.forEach((gift, idx) => {
    const row = document.createElement("div");
    row.className = `gift-row p-1.5 rounded bg-slate-800/90 border border-amber-500/40 flex items-center justify-between cursor-pointer hover:bg-slate-700 transition ${
      appState.pinPositioner.selectedType === 'gift' && appState.pinPositioner.selectedGiftIndex === idx ? 'bg-amber-900/60 border-amber-500' : ''
    }`;
    row.dataset.index = idx;
    const pctX = gift.x > 1.0 ? gift.x : (gift.x * 100);
    const pctY = gift.y > 1.0 ? gift.y : (gift.y * 100);
    row.innerHTML = `
      <div class="flex items-center gap-1.5">
        <span class="text-base">🎁</span>
        <div>
          <span class="text-amber-300 font-bold block leading-tight">Gift ${gift.giftIndex}: ${gift.name}</span>
          <span class="text-[10px] text-slate-400 font-normal">+${gift.requiredLevelOffset} lvls • ${gift.rewardCoins} coins</span>
        </div>
      </div>
      <div class="text-right text-[10px]">
        <span class="text-slate-400 block font-mono">X: ${pctX.toFixed(1)}%</span>
        <span class="text-slate-400 block font-mono">Y: ${pctY.toFixed(1)}%</span>
      </div>
    `;
    row.addEventListener("click", () => selectGiftNode(idx));
    listWorldGifts.appendChild(row);
  });
}

function adjustPinZoom(delta, fit = false) {
  if (fit) {
    appState.pinPositioner.zoomScale = 1.0;
  } else {
    appState.pinPositioner.zoomScale = Math.max(0.6, Math.min(2.0, appState.pinPositioner.zoomScale + delta));
  }

  lblZoomLevel.textContent = `${Math.round(appState.pinPositioner.zoomScale * 100)}%`;
  const baseW = 450;
  const baseH = 800;
  imgMapBackground.style.width = `${baseW * appState.pinPositioner.zoomScale}px`;
  imgMapBackground.style.height = `${baseH * appState.pinPositioner.zoomScale}px`;
}

async function saveActiveWorldPins() {
  if (!window.gameStudioAPI || !appState.resolvedPaths || !appState.worldsData) {
    showToast("Cannot save: No project connection", true);
    return;
  }

  const res = await window.gameStudioAPI.saveWorlds({
    worldsConfigFile: appState.resolvedPaths.worldsConfig,
    data: appState.worldsData
  });

  if (res.success) {
    showToast("✓ Saved all map pin coordinates to worlds_config.json!");
  } else {
    showToast(`Failed to save pins: ${res.error}`, true);
  }
}

// ============================================================================
// CREATE NEW LEVEL FEATURE
// ============================================================================
function openNewLevelModal() {
  const nextLevel = (appState.levelCount || 490) + 1;
  inputNewLevelNum.value = nextLevel;
  inputNewLevelShots.value = 20;
  selectNewLevelObjective.value = "CLEAR_ALL";
  selectNewLevelTemplate.value = "blank";
  updateNewLevelWorldHint();
  modalNewLevel.classList.remove('hidden');
}

function updateNewLevelWorldHint() {
  const lvl = parseInt(inputNewLevelNum.value) || 1;
  const worldNum = Math.floor((lvl - 1) / 10) + 1;
  lblNewLevelWorldHint.textContent = `World ${worldNum}`;
}

async function handleCreateNewLevel() {
  const newLevelNum = parseInt(inputNewLevelNum.value);
  if (!newLevelNum || newLevelNum < 1) {
    showToast("Please enter a valid level number", true);
    return;
  }

  const shots = parseInt(inputNewLevelShots.value) || 20;
  const objectiveType = selectNewLevelObjective.value || "CLEAR_ALL";
  const template = selectNewLevelTemplate.value;

  let rows = [];
  let colors = ["RED", "GREEN", "BLUE", "YELLOW", "PURPLE", "ORANGE"];

  if (template === "clone") {
    rows = [...appState.levelDesigner.rows];
    colors = [...appState.levelDesigner.colors];
  } else if (template === "diamond") {
    rows = [
      "...RRR...",
      "..RRRR..",
      ".RRBBRR.",
      ".RBBBBR.",
      "..RBRB..",
      "...RR..."
    ];
  } else if (template === "waves") {
    rows = [
      "RRGGYYBBP",
      "RGGYYBBP",
      "GGYYBBPPR",
      "GYYBBPPR",
      "YYBBPPRRG",
      "YBBPPRRG"
    ];
  } else {
    // Blank grid - 8 empty rows
    rows = [
      ".........",
      "........",
      ".........",
      "........",
      ".........",
      "........",
      ".........",
      "........"
    ];
  }

  const sanitizedRows = rows.map((r, idx) => {
    const expected = (idx % 2 === 0) ? 9 : 8;
    return (r + ".".repeat(expected)).substring(0, expected);
  });

  const levelData = {
    level: newLevelNum,
    shots: shots,
    colors: colors,
    objective: {
      type: objectiveType,
      target: objectiveType === "SCORE_TARGET" ? 5000 : 0
    },
    starThresholds: [2000, 4500, 8000],
    rows: sanitizedRows
  };

  if (!window.gameStudioAPI || !appState.resolvedPaths) {
    appState.levelDesigner.levelNumber = newLevelNum;
    appState.levelDesigner.shots = shots;
    appState.levelDesigner.colors = colors;
    appState.levelDesigner.rows = sanitizedRows;
    renderLevelBoard();
    modalNewLevel.classList.add('hidden');
    showToast(`Level ${newLevelNum} created (Preview mode)`);
    return;
  }

  // 1. Save level file directly to assets/levels/
  const saveRes = await window.gameStudioAPI.saveLevel({
    levelsDir: appState.resolvedPaths.levels,
    levelNumber: newLevelNum,
    data: levelData
  });

  if (!saveRes.success) {
    showToast(`Failed to create level: ${saveRes.error}`, true);
    return;
  }

  // 2. Ensure level is registered in worlds_config.json
  if (appState.worldsData && appState.worldsData.worlds) {
    const targetWorldNum = Math.floor((newLevelNum - 1) / 10) + 1;
    let targetWorld = appState.worldsData.worlds.find(w => (w.worldNumber === targetWorldNum || w.id === targetWorldNum));

    if (!targetWorld) {
      targetWorld = {
        worldNumber: targetWorldNum,
        name: `World ${targetWorldNum}`,
        subtitle: `WORLD ${targetWorldNum}`,
        mapBackground: `bg_map_world_${targetWorldNum}`,
        gameBackground: `bg_game_world_${targetWorldNum}`,
        levelsCount: 10,
        levels: []
      };
      appState.worldsData.worlds.push(targetWorld);
    }

    if (!targetWorld.levels) targetWorld.levels = targetWorld.nodes || [];
    const exists = targetWorld.levels.some(l => l.level === newLevelNum);
    if (!exists) {
      const idxInWorld = (newLevelNum - 1) % 10;
      const defaultY = Math.max(0.15, Math.min(0.85, 0.80 - (idxInWorld * 0.065)));
      const defaultX = 0.35 + (idxInWorld % 2 === 0 ? 0.15 : -0.10);
      targetWorld.levels.push({
        level: newLevelNum,
        x: parseFloat(defaultX.toFixed(4)),
        y: parseFloat(defaultY.toFixed(4))
      });
      targetWorld.levels.sort((a, b) => a.level - b.level);
    }

    // Save updated worlds_config.json
    await window.gameStudioAPI.saveWorlds({
      worldsConfigFile: appState.resolvedPaths.worldsConfig,
      data: appState.worldsData
    });
  }

  // 3. Update level counts and refresh
  appState.levelCount = Math.max(appState.levelCount, newLevelNum);
  lblLevelCount.textContent = appState.levelCount;
  populateWorldSelector();
  modalNewLevel.classList.add('hidden');

  // 4. Switch to designer and load new level
  switchTab('level-designer');
  await loadLevel(newLevelNum);
  showToast(`✓ Successfully created and opened Level ${newLevelNum}!`);
}

// ============================================================================
// MODULE 3: CAMPAIGN WORLDS EXPLORER
// ============================================================================
async function renderCampaignWorldsGrid() {
  gridWorldsList.innerHTML = "";
  if (!appState.worldsData || !appState.worldsData.worlds) return;

  for (const w of appState.worldsData.worlds) {
    const worldNum = w.worldNumber || w.id;
    const levels = w.levels || w.nodes || [];
    const startLevel = levels.length > 0 ? levels[0].level : ((worldNum - 1) * 10 + 1);
    const endLevel = levels.length > 0 ? levels[levels.length - 1].level : (worldNum * 10);
    const bgName = w.mapBackground || `bg_map_world_${worldNum}`;

    const card = document.createElement("div");
    card.className = "bg-slate-900 border border-slate-800 rounded-xl p-3 flex flex-col space-y-2.5 shadow hover:border-indigo-600/60 transition group";

    card.innerHTML = `
      <div class="flex items-center justify-between">
        <span class="font-bold text-white text-xs">World ${worldNum}</span>
        <span class="text-[10px] bg-indigo-950 text-indigo-300 px-2 py-0.5 rounded border border-indigo-800 font-mono">Lvls ${startLevel}-${endLevel}</span>
      </div>
      <div class="text-slate-300 font-semibold text-xs truncate">${w.name}</div>
      <div class="h-28 rounded-lg overflow-hidden bg-slate-950 border border-slate-800 relative flex items-center justify-center">
        <img id="thumbWorld${worldNum}" class="w-full h-full object-cover group-hover:scale-105 transition duration-300" />
        <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent flex items-end p-2">
          <span class="text-[10px] text-amber-300 font-mono font-bold">${levels.length} Nodes Placed</span>
        </div>
      </div>
      <div class="grid grid-cols-2 gap-1.5 pt-1">
        <button class="btn-edit-pins py-1 bg-slate-800 hover:bg-indigo-600 text-slate-200 hover:text-white rounded text-[11px] font-medium transition" data-world="${worldNum}">
          🎯 Edit Pins
        </button>
        <button class="btn-edit-lvl py-1 bg-slate-800 hover:bg-indigo-600 text-slate-200 hover:text-white rounded text-[11px] font-medium transition" data-level="${startLevel}">
          🔮 Level ${startLevel}
        </button>
      </div>
    `;

    card.querySelector('.btn-edit-pins').addEventListener('click', () => {
      switchTab('pin-positioner');
      loadWorldForPinPositioner(worldNum);
    });

    card.querySelector('.btn-edit-lvl').addEventListener('click', () => {
      switchTab('level-designer');
      loadLevel(startLevel);
    });

    gridWorldsList.appendChild(card);

    // Asynchronously load thumbnail
    if (window.gameStudioAPI && appState.resolvedPaths) {
      window.gameStudioAPI.loadImageBase64({
        drawableDir: appState.resolvedPaths.drawable,
        imageName: bgName
      }).then(res => {
        if (res && res.success) {
          const img = document.getElementById(`thumbWorld${worldNum}`);
          if (img) img.src = res.dataUrl;
        }
      });
    }
  }
}

// ============================================================================
// TOAST NOTIFICATIONS
// ============================================================================
function showToast(msg, isError = false) {
  toastNotification.textContent = msg;
  toastNotification.className = `fixed bottom-6 left-1/2 -translate-x-1/2 ${
    isError ? 'bg-red-600' : 'bg-emerald-600'
  } text-white font-semibold text-xs px-5 py-2.5 rounded-xl shadow-2xl z-50 transition transform duration-200 opacity-100`;

  setTimeout(() => {
    toastNotification.classList.remove('opacity-100');
    toastNotification.classList.add('opacity-0');
  }, 2600);
}

// Start application
initApp();
