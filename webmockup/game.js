// Bubble Shooter Pro - 1:1 Complete Game Engine
// Matching Android GameEngine, BubbleGrid, NeighborCalculator, CollisionDetector

const BUBBLE_COLORS = {
    RED: { primary: '#FF1744', light: '#FF5252', dark: '#C62828', code: 'R' },
    GREEN: { primary: '#00C853', light: '#69F0AE', dark: '#1B5E20', code: 'G' },
    BLUE: { primary: '#0091EA', light: '#40C4FF', dark: '#0D47A1', code: 'B' },
    YELLOW: { primary: '#FFD600', light: '#FFFF00', dark: '#FF6F00', code: 'Y' },
    PURPLE: { primary: '#AA00FF', light: '#E040FB', dark: '#4A148C', code: 'P' },
    ORANGE: { primary: '#FF6D00', light: '#FFAB40', dark: '#BF360C', code: 'O' },
    CYAN: { primary: '#00E5FF', light: '#84FFFF', dark: '#006064', code: 'C' },
    RAINBOW: { primary: '#FFFFFF', light: '#FFFFFF', dark: '#D3D3D3', code: '*' },
    BOMB: { primary: '#212121', light: '#616161', dark: '#000000', code: 'X' }
};

const COLS_EVEN = 8;
const COLS_ODD = 7;
const MAX_ROWS = 14;
const ROW_HEIGHT_RATIO = 1.7320508; // sqrt(3)

class Particle {
    constructor(x, y, color) {
        this.x = x;
        this.y = y;
        this.color = color;
        const angle = Math.random() * Math.PI * 2;
        const speed = 80 + Math.random() * 220;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.radius = 3 + Math.random() * 4;
        this.alpha = 1.0;
        this.decay = 1.5 + Math.random() * 2.0;
    }

    update(dt) {
        this.x += this.vx * dt;
        this.y += this.vy * dt;
        this.vy += 300 * dt; // gravity
        this.alpha -= this.decay * dt;
    }

    draw(ctx) {
        if (this.alpha <= 0) return;
        ctx.save();
        ctx.globalAlpha = Math.max(0, this.alpha);
        ctx.fillStyle = this.color;
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
    }
}

class FloatingText {
    constructor(x, y, text, color = '#FFD54F') {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.alpha = 1.0;
        this.scale = 1.0;
    }

    update(dt) {
        this.y -= 70 * dt;
        this.alpha -= 0.9 * dt;
        this.scale = Math.min(1.4, this.scale + dt * 0.8);
    }

    draw(ctx) {
        if (this.alpha <= 0) return;
        ctx.save();
        ctx.globalAlpha = Math.max(0, this.alpha);
        ctx.font = `bold ${Math.floor(18 * this.scale)}px sans-serif`;
        ctx.textAlign = 'center';
        ctx.fillStyle = '#000';
        ctx.fillText(this.text, this.x + 2, this.y + 2);
        ctx.fillStyle = this.color;
        ctx.fillText(this.text, this.x, this.y);
        ctx.restore();
    }
}

class Bubble {
    constructor(row, col, colorKey, type = 'NORMAL') {
        this.row = row;
        this.col = col;
        this.colorKey = colorKey;
        this.type = type;
        this.x = 0;
        this.y = 0;
        this.radius = 20;
        this.scale = 1.0;
        this.alpha = 1.0;
        this.isPopping = false;
        this.popProgress = 0;
        this.isFalling = false;
        this.vx = 0;
        this.vy = 0;
    }

    update(dt) {
        if (this.isFalling) {
            this.vy += 1800 * dt;
            this.x += this.vx * dt;
            this.y += this.vy * dt;
        }
        if (this.isPopping) {
            this.popProgress += dt * 4.5;
            this.scale = 1.0 + this.popProgress * 0.4;
            this.alpha = Math.max(0, 1.0 - this.popProgress);
        }
    }

    draw(ctx) {
        if (this.alpha <= 0 || this.radius <= 0) return;
        ctx.save();
        ctx.translate(this.x, this.y);
        ctx.scale(this.scale, this.scale);
        ctx.globalAlpha = this.alpha;

        const r = this.radius;
        const col = BUBBLE_COLORS[this.colorKey] || BUBBLE_COLORS.RED;

        if (this.type === 'BOMB') {
            this.drawBomb(ctx, r);
        } else if (this.type === 'RAINBOW') {
            this.drawRainbow(ctx, r);
        } else {
            this.drawGlossy(ctx, r, col);
        }

        ctx.restore();
    }

    drawGlossy(ctx, r, col) {
        // 1. Subtle drop shadow
        ctx.fillStyle = 'rgba(0, 0, 0, 0.25)';
        ctx.beginPath();
        ctx.arc(0, r * 0.12, r, 0, Math.PI * 2);
        ctx.fill();

        // 2. 100% Solid vibrant base color
        ctx.fillStyle = col.primary;
        ctx.beginPath();
        ctx.arc(0, 0, r, 0, Math.PI * 2);
        ctx.fill();

        // 3. 3D Spherical bevel shading (shaded edge at bottom/right)
        const lightOffset = r * 0.25;
        const grad = ctx.createRadialGradient(-lightOffset, -lightOffset, r * 0.1, -lightOffset, -lightOffset, r * 1.3);
        grad.addColorStop(0, 'rgba(0,0,0,0)');
        grad.addColorStop(0.65, 'rgba(0,0,0,0)');
        grad.addColorStop(1, col.dark);
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.arc(0, 0, r, 0, Math.PI * 2);
        ctx.fill();

        // 4. Crisp solid outer rim border
        ctx.strokeStyle = col.dark;
        ctx.lineWidth = r * 0.08;
        ctx.globalAlpha = this.alpha * 0.75;
        ctx.beginPath();
        ctx.arc(0, 0, r * 0.96, 0, Math.PI * 2);
        ctx.stroke();
        ctx.globalAlpha = this.alpha;

        // 5. Clean, compact top-left white shine (specular highlight)
        ctx.save();
        ctx.translate(-r * 0.35, -r * 0.35);
        ctx.rotate(-Math.PI * 0.22);
        ctx.fillStyle = 'rgba(255, 255, 255, 0.8)';
        ctx.beginPath();
        ctx.ellipse(0, 0, r * 0.26, r * 0.13, 0, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();

        // 6. Tiny bright pinpoint sparkle
        ctx.fillStyle = 'rgba(255, 255, 255, 0.9)';
        ctx.beginPath();
        ctx.arc(-r * 0.22, -r * 0.52, r * 0.07, 0, Math.PI * 2);
        ctx.fill();
    }

    drawBomb(ctx, r) {
        // 1. Subtle drop shadow
        ctx.fillStyle = 'rgba(0, 0, 0, 0.4)';
        ctx.beginPath();
        ctx.arc(0, r * 0.12, r, 0, Math.PI * 2);
        ctx.fill();

        // 2. Base Dark Metallic Gradient Sphere (#5C636E -> #2B2F37 -> #0C0E12)
        const baseGrad = ctx.createRadialGradient(-r * 0.29, -r * 0.38, r * 0.05, 0, 0, r * 1.55);
        baseGrad.addColorStop(0, '#5C636E');
        baseGrad.addColorStop(0.45, '#2B2F37');
        baseGrad.addColorStop(1, '#0C0E12');
        ctx.fillStyle = baseGrad;
        ctx.beginPath();
        ctx.arc(0, 0, r, 0, Math.PI * 2);
        ctx.fill();

        // 3. Inner border rings
        ctx.strokeStyle = 'rgba(0,0,0,0.55)';
        ctx.lineWidth = r * 0.035;
        ctx.beginPath();
        ctx.arc(0, 0, r * 0.98, 0, Math.PI * 2);
        ctx.stroke();

        ctx.strokeStyle = 'rgba(255,255,255,0.12)';
        ctx.lineWidth = r * 0.035;
        ctx.beginPath();
        ctx.arc(0, 0, r * 0.92, 0, Math.PI * 2);
        ctx.stroke();

        // 4. Top-left bubble gloss circle
        ctx.fillStyle = 'rgba(215, 219, 224, 0.75)';
        ctx.beginPath();
        ctx.arc(-r * 0.44, -r * 0.47, r * 0.10, 0, Math.PI * 2);
        ctx.fill();

        // 5. Center Round Bomb Body (#FFFFFF -> #EEF0F3 -> #B4BAC4)
        const innerGrad = ctx.createRadialGradient(-r * 0.11, -r * 0.15, r * 0.05, 0, r * 0.12, r * 0.6);
        innerGrad.addColorStop(0, '#FFFFFF');
        innerGrad.addColorStop(0.55, '#EEF0F3');
        innerGrad.addColorStop(1, '#B4BAC4');
        ctx.fillStyle = innerGrad;
        ctx.beginPath();
        ctx.arc(-r * 0.06, r * 0.12, r * 0.38, 0, Math.PI * 2);
        ctx.fill();

        // Bomb highlight reflection
        ctx.fillStyle = '#FFFFFF';
        ctx.save();
        ctx.translate(-r * 0.28, 0);
        ctx.rotate(0.35);
        ctx.beginPath();
        ctx.ellipse(0, 0, r * 0.10, r * 0.055, 0, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();

        // 6. Fuse Cap (#d3d8df & #9aa1ac)
        ctx.save();
        ctx.translate(r * 0.24, -r * 0.22);
        ctx.rotate(Math.PI / 4);
        ctx.fillStyle = '#d3d8df';
        ctx.fillRect(-r * 0.09, -r * 0.09, r * 0.18, r * 0.18);
        ctx.fillStyle = '#9aa1ac';
        ctx.fillRect(-r * 0.09, -r * 0.03, r * 0.18, r * 0.06);
        ctx.restore();

        // 7. Curved Fuse Rope (#f3d9a4)
        ctx.strokeStyle = '#f3d9a4';
        ctx.lineWidth = r * 0.065;
        ctx.lineCap = 'round';
        ctx.beginPath();
        ctx.moveTo(r * 0.30, -r * 0.23);
        ctx.quadraticCurveTo(r * 0.42, -r * 0.44, r * 0.50, -r * 0.41);
        ctx.stroke();

        // 8. Glowing Spark (#FFFFE066 / #FF8A1F)
        const sparkGrad = ctx.createRadialGradient(r * 0.53, -r * 0.44, 0, r * 0.53, -r * 0.44, r * 0.22);
        sparkGrad.addColorStop(0, '#FFFFFF');
        sparkGrad.addColorStop(0.35, '#FFFFE066');
        sparkGrad.addColorStop(0.75, 'rgba(255, 138, 31, 0.8)');
        sparkGrad.addColorStop(1, 'rgba(255, 90, 0, 0)');
        ctx.fillStyle = sparkGrad;
        ctx.beginPath();
        ctx.arc(r * 0.53, -r * 0.44, r * 0.22, 0, Math.PI * 2);
        ctx.fill();

        // 9. Spark Star (#fff8d0)
        ctx.fillStyle = '#fff8d0';
        ctx.save();
        ctx.translate(r * 0.53, -r * 0.44);
        ctx.beginPath();
        ctx.moveTo(0, -r * 0.14);
        ctx.lineTo(r * 0.03, -r * 0.03);
        ctx.lineTo(r * 0.14, 0);
        ctx.lineTo(r * 0.03, r * 0.03);
        ctx.lineTo(0, r * 0.14);
        ctx.lineTo(-r * 0.03, r * 0.03);
        ctx.lineTo(-r * 0.14, 0);
        ctx.lineTo(-r * 0.03, -r * 0.03);
        ctx.closePath();
        ctx.fill();
        ctx.restore();
    }

    drawRainbow(ctx, r) {
        // 1. Subtle drop shadow
        ctx.fillStyle = 'rgba(0, 0, 0, 0.25)';
        ctx.beginPath();
        ctx.arc(0, r * 0.12, r, 0, Math.PI * 2);
        ctx.fill();

        // 2. Diagonal 6-Color Rainbow Linear Gradient (#FF1F4B -> #FF8A1F -> #FFD200 -> #00C853 -> #1E88FF -> #9C27B0)
        const linGrad = ctx.createLinearGradient(-r * 0.707, -r * 0.707, r * 0.707, r * 0.707);
        linGrad.addColorStop(0, '#FF1F4B');
        linGrad.addColorStop(0.2, '#FF8A1F');
        linGrad.addColorStop(0.4, '#FFD200');
        linGrad.addColorStop(0.6, '#00C853');
        linGrad.addColorStop(0.8, '#1E88FF');
        linGrad.addColorStop(1, '#9C27B0');

        ctx.fillStyle = linGrad;
        ctx.beginPath();
        ctx.arc(0, 0, r, 0, Math.PI * 2);
        ctx.fill();

        // 3. 3D Spherical Bevel Gradient Overlay
        const bevelGrad = ctx.createRadialGradient(-r * 0.29, -r * 0.38, r * 0.05, 0, 0, r * 1.55);
        bevelGrad.addColorStop(0, 'rgba(255, 255, 255, 0.25)');
        bevelGrad.addColorStop(0.5, 'rgba(0, 0, 0, 0)');
        bevelGrad.addColorStop(1, 'rgba(0, 0, 0, 0.35)');
        ctx.fillStyle = bevelGrad;
        ctx.beginPath();
        ctx.arc(0, 0, r, 0, Math.PI * 2);
        ctx.fill();

        // 4. Subtle Outer Border
        ctx.strokeStyle = 'rgba(0, 0, 0, 0.4)';
        ctx.lineWidth = r * 0.035;
        ctx.beginPath();
        ctx.arc(0, 0, r * 0.98, 0, Math.PI * 2);
        ctx.stroke();

        // 5. Specular highlight (smooth angled oval)
        ctx.save();
        ctx.translate(-r * 0.38, -r * 0.38);
        ctx.rotate(-Math.PI * 0.20);
        ctx.fillStyle = 'rgba(255, 255, 255, 0.85)';
        ctx.beginPath();
        ctx.ellipse(0, 0, r * 0.26, r * 0.15, 0, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
    }
}

class GameEngine {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');

        this.boardLeft = 0;
        this.boardTop = 0;
        this.bubbleRadius = 24;

        this.grid = Array(MAX_ROWS).fill(null).map(() => Array(COLS_EVEN).fill(null));

        this.currentLevel = 1;
        this.levelData = null;
        this.score = 0;
        this.starsEarned = 0;
        this.shotsRemaining = 25;
        this.comboCount = 0;

        this.currentBubble = null;
        this.nextBubble = null;
        this.activeProjectile = null;

        this.aimAngle = -Math.PI / 2; // straight up
        this.isAiming = false;
        this.isShooting = false;
        this.isPaused = false;
        this.isGameOver = false;

        this.launcherX = 0;
        this.launcherY = 0;
        this.previewX = 0;
        this.previewY = 0;

        this.poppingBubbles = [];
        this.fallingBubbles = [];
        this.particles = [];
        this.floatingTexts = [];

        this.activeBooster = null; // 'BOMB' or 'RAINBOW'

        this.setupEvents();
        this.resize();
        window.addEventListener('resize', () => this.resize());
    }

    resize() {
        const rect = this.canvas.parentElement ? this.canvas.parentElement.getBoundingClientRect() : { width: 328, height: 700 };
        const w = (rect.width > 50) ? rect.width : (this.canvas.parentElement?.offsetWidth || 328);
        const h = (rect.height > 200) ? rect.height : (this.canvas.parentElement?.offsetHeight || 700);
        
        this.canvas.width = Math.min(w, 328);
        this.canvas.height = h;

        this.boardLeft = 0;
        this.boardTop = 68; // top HUD height
        this.bubbleRadius = this.canvas.width / (COLS_EVEN * 2.0);

        this.launcherX = this.canvas.width * 0.5;
        this.launcherY = this.canvas.height - (this.bubbleRadius * 2.8);
        this.previewX = this.launcherX - (this.bubbleRadius * 2.6);
        this.previewY = this.launcherY + (this.bubbleRadius * 0.4);

        // update all grid bubble positions
        for (let r = 0; r < MAX_ROWS; r++) {
            const cols = (r % 2 === 0) ? COLS_EVEN : COLS_ODD;
            for (let c = 0; c < cols; c++) {
                const b = this.grid[r][c];
                if (b) {
                    b.radius = this.bubbleRadius;
                    b.x = this.getCenterX(r, c);
                    b.y = this.getCenterY(r);
                }
            }
        }

        if (this.currentBubble) {
            this.currentBubble.radius = this.bubbleRadius;
            this.currentBubble.x = this.launcherX;
            this.currentBubble.y = this.launcherY;
        }
        if (this.nextBubble) {
            this.nextBubble.radius = this.bubbleRadius * 0.75;
            this.nextBubble.x = this.previewX;
            this.nextBubble.y = this.previewY;
        }
    }

    getCenterX(r, c) {
        const offset = (r % 2 === 0) ? this.bubbleRadius : (this.bubbleRadius * 2.0);
        return this.boardLeft + offset + (c * 2.0 * this.bubbleRadius);
    }

    getCenterY(r) {
        return this.boardTop + this.bubbleRadius + (r * this.bubbleRadius * ROW_HEIGHT_RATIO);
    }

    loadLevel(levelNum) {
        this.resize();
        this.currentLevel = levelNum;
        this.levelData = (typeof GAME_LEVELS !== 'undefined' && GAME_LEVELS[levelNum]) ? GAME_LEVELS[levelNum] : null;

        this.score = 0;
        this.starsEarned = 0;
        this.comboCount = 0;
        this.isGameOver = false;
        this.isPaused = false;
        this.isShooting = false;
        this.isAiming = false;
        this.activeBooster = null;

        this.poppingBubbles = [];
        this.fallingBubbles = [];
        this.particles = [];
        this.floatingTexts = [];
        this.activeProjectile = null;

        // Clear grid
        for (let r = 0; r < MAX_ROWS; r++) {
            for (let c = 0; c < COLS_EVEN; c++) {
                this.grid[r][c] = null;
            }
        }

        if (this.levelData) {
            this.shotsRemaining = this.levelData.shots || 25;
            const rows = this.levelData.rows || [];
            rows.forEach((rowStr, r) => {
                const cols = (r % 2 === 0) ? COLS_EVEN : COLS_ODD;
                for (let c = 0; c < cols && c < rowStr.length; c++) {
                    const char = rowStr[c];
                    if (char !== '.') {
                        const colKey = this.colorKeyFromChar(char);
                        const bType = (char === 'X' || colKey === 'BOMB') ? 'BOMB' : (char === '*' || colKey === 'RAINBOW') ? 'RAINBOW' : 'NORMAL';
                        if (colKey) {
                            const b = new Bubble(r, c, colKey, bType);
                            b.radius = this.bubbleRadius;
                            b.x = this.getCenterX(r, c);
                            b.y = this.getCenterY(r);
                            this.grid[r][c] = b;
                        }
                    }
                }
            });
        } else {
            // Default level 1
            this.shotsRemaining = 28;
            for (let r = 0; r < 5; r++) {
                const cols = (r % 2 === 0) ? COLS_EVEN : COLS_ODD;
                const pool = ['RED', 'BLUE', 'YELLOW'];
                for (let c = 0; c < cols; c++) {
                    const colKey = pool[(r + c) % pool.length];
                    const b = new Bubble(r, c, colKey, 'NORMAL');
                    b.radius = this.bubbleRadius;
                    b.x = this.getCenterX(r, c);
                    b.y = this.getCenterY(r);
                    this.grid[r][c] = b;
                }
            }
        }

        this.setupBubbles();
        if (this.onLevelLoaded) {
            this.onLevelLoaded(this.currentLevel);
        }
        this.updateHUD();
    }

    colorKeyFromChar(char) {
        for (const [key, val] of Object.entries(BUBBLE_COLORS)) {
            if (val.code === char) return key;
        }
        return 'RED';
    }

    getExistingColors() {
        const set = new Set();
        for (let r = 0; r < MAX_ROWS; r++) {
            for (let c = 0; c < COLS_EVEN; c++) {
                const b = this.grid[r][c];
                if (b && b.type === 'NORMAL') {
                    set.add(b.colorKey);
                }
            }
        }
        return Array.from(set);
    }

    pickRandomColor() {
        const existing = this.getExistingColors();
        if (existing.length > 0) {
            return existing[Math.floor(Math.random() * existing.length)];
        }
        return 'RED';
    }

    setupBubbles() {
        const c1 = this.pickRandomColor();
        const c2 = this.pickRandomColor();

        this.currentBubble = new Bubble(0, 0, c1, 'NORMAL');
        this.currentBubble.radius = this.bubbleRadius;
        this.currentBubble.x = this.launcherX;
        this.currentBubble.y = this.launcherY;

        this.nextBubble = new Bubble(0, 0, c2, 'NORMAL');
        this.nextBubble.radius = this.bubbleRadius * 0.75;
        this.nextBubble.x = this.previewX;
        this.nextBubble.y = this.previewY;
    }

    swapBubbles() {
        if (this.isShooting || !this.currentBubble || !this.nextBubble) return;
        sounds.playClick();

        const tempColor = this.currentBubble.colorKey;
        const tempType = this.currentBubble.type;

        this.currentBubble.colorKey = this.nextBubble.colorKey;
        this.currentBubble.type = this.nextBubble.type;

        this.nextBubble.colorKey = tempColor;
        this.nextBubble.type = tempType;
    }

    armBooster(type) {
        if (this.isShooting || !this.currentBubble) return;
        sounds.playClick();
        this.activeBooster = type;
        this.currentBubble.type = type;
        this.currentBubble.colorKey = type;
    }

    equipBooster(type) {
        this.armBooster(type);
    }

    setupEvents() {
        const getTouchPos = (e) => {
            const rect = this.canvas.getBoundingClientRect();
            const clientX = e.touches ? e.touches[0].clientX : e.clientX;
            const clientY = e.touches ? e.touches[0].clientY : e.clientY;
            return {
                x: clientX - rect.left,
                y: clientY - rect.top
            };
        };

        const handleStart = (e) => {
            if (this.isGameOver || this.isPaused || this.isShooting) return;
            const pos = getTouchPos(e);

            // Check if tapped on Next Bubble to swap!
            const dxPreview = pos.x - this.previewX;
            const dyPreview = pos.y - this.previewY;
            if (Math.hypot(dxPreview, dyPreview) < this.bubbleRadius * 1.6) {
                this.swapBubbles();
                return;
            }

            // Aim only if clicking below the grid or anywhere playable
            this.isAiming = true;
            this.updateAim(pos.x, pos.y);
        };

        const handleMove = (e) => {
            if (!this.isAiming) return;
            const pos = getTouchPos(e);
            this.updateAim(pos.x, pos.y);
        };

        const handleEnd = () => {
            if (!this.isAiming) return;
            this.isAiming = false;
            this.shoot();
        };

        this.canvas.addEventListener('mousedown', handleStart);
        window.addEventListener('mousemove', handleMove);
        window.addEventListener('mouseup', handleEnd);

        this.canvas.addEventListener('touchstart', (e) => { e.preventDefault(); handleStart(e); }, { passive: false });
        window.addEventListener('touchmove', (e) => { handleMove(e); });
        window.addEventListener('touchend', handleEnd);
    }

    updateAim(targetX, targetY) {
        let dx = targetX - this.launcherX;
        let dy = targetY - this.launcherY;

        let angle = Math.atan2(dy, dx);

        // Clamp aim angle so it always shoots upwards:
        // Angle between -170 deg and -10 deg
        const minAngle = -Math.PI * 0.94;
        const maxAngle = -Math.PI * 0.06;

        if (angle > 0) {
            angle = (dx < 0) ? minAngle : maxAngle;
        } else {
            angle = Math.max(minAngle, Math.min(maxAngle, angle));
        }

        this.aimAngle = angle;
    }

    calculateTrajectory() {
        const points = [];
        let cx = this.launcherX;
        let cy = this.launcherY;
        let vx = Math.cos(this.aimAngle);
        let vy = Math.sin(this.aimAngle);

        const maxPoints = 80;
        const step = 14;

        for (let i = 0; i < maxPoints; i++) {
            cx += vx * step;
            cy += vy * step;

            // Wall bounce left/right
            if (cx <= this.bubbleRadius) {
                cx = this.bubbleRadius;
                vx = -vx;
            } else if (cx >= this.canvas.width - this.bubbleRadius) {
                cx = this.canvas.width - this.bubbleRadius;
                vx = -vx;
            }

            points.push({ x: cx, y: cy });

            // Stop if ceiling reached
            if (cy <= this.boardTop + this.bubbleRadius) {
                break;
            }

            // Stop if colliding with any bubble
            let hit = false;
            for (let r = 0; r < MAX_ROWS; r++) {
                const cols = (r % 2 === 0) ? COLS_EVEN : COLS_ODD;
                for (let c = 0; c < cols; c++) {
                    const b = this.grid[r][c];
                    if (b) {
                        const dist = Math.hypot(cx - b.x, cy - b.y);
                        if (dist <= this.bubbleRadius * 1.85) {
                            hit = true;
                            break;
                        }
                    }
                }
                if (hit) break;
            }
            if (hit) break;
        }

        return points;
    }

    shoot() {
        if (this.isShooting || !this.currentBubble || this.shotsRemaining <= 0) return;

        this.isShooting = true;
        this.shotsRemaining--;
        sounds.playShoot();
        this.updateHUD();

        const speed = 1900; // px/sec
        this.activeProjectile = {
            bubble: this.currentBubble,
            x: this.launcherX,
            y: this.launcherY,
            vx: Math.cos(this.aimAngle) * speed,
            vy: Math.sin(this.aimAngle) * speed,
            radius: this.bubbleRadius
        };

        // Advance next bubble
        this.currentBubble = new Bubble(0, 0, this.nextBubble.colorKey, this.nextBubble.type);
        this.currentBubble.radius = this.bubbleRadius;
        this.currentBubble.x = this.launcherX;
        this.currentBubble.y = this.launcherY;

        this.nextBubble = new Bubble(0, 0, this.pickRandomColor(), 'NORMAL');
        this.nextBubble.radius = this.bubbleRadius * 0.75;
        this.nextBubble.x = this.previewX;
        this.nextBubble.y = this.previewY;
    }

    update(dt) {
        // 1. Update Projectile
        if (this.activeProjectile) {
            const p = this.activeProjectile;
            p.x += p.vx * dt;
            p.y += p.vy * dt;

            // Wall bounce
            if (p.x <= p.radius) {
                p.x = p.radius;
                p.vx = -p.vx;
                sounds.playBounce();
            } else if (p.x >= this.canvas.width - p.radius) {
                p.x = this.canvas.width - p.radius;
                p.vx = -p.vx;
                sounds.playBounce();
            }

            // Check collision with top wall or bubbles
            const collision = this.checkProjectileCollision(p);
            if (collision) {
                this.resolveProjectileLand(p, collision);
                this.activeProjectile = null;
            }
        }

        // 2. Update popping bubbles
        for (let i = this.poppingBubbles.length - 1; i >= 0; i--) {
            const b = this.poppingBubbles[i];
            b.update(dt);
            if (b.popProgress >= 1.0) {
                this.poppingBubbles.splice(i, 1);
            }
        }

        // 3. Update falling bubbles
        for (let i = this.fallingBubbles.length - 1; i >= 0; i--) {
            const b = this.fallingBubbles[i];
            b.update(dt);
            if (b.y > this.canvas.height + 60) {
                this.fallingBubbles.splice(i, 1);
            }
        }

        // 4. Update particles
        for (let i = this.particles.length - 1; i >= 0; i--) {
            const pt = this.particles[i];
            pt.update(dt);
            if (pt.alpha <= 0) {
                this.particles.splice(i, 1);
            }
        }

        // 5. Update floating texts
        for (let i = this.floatingTexts.length - 1; i >= 0; i--) {
            const ft = this.floatingTexts[i];
            ft.update(dt);
            if (ft.alpha <= 0) {
                this.floatingTexts.splice(i, 1);
            }
        }
    }

    checkProjectileCollision(p) {
        // Ceiling hit
        if (p.y <= this.boardTop + p.radius) {
            return { hitCeiling: true };
        }

        // Bubble hit
        for (let r = 0; r < MAX_ROWS; r++) {
            const cols = (r % 2 === 0) ? COLS_EVEN : COLS_ODD;
            for (let c = 0; c < cols; c++) {
                const b = this.grid[r][c];
                if (b) {
                    const dist = Math.hypot(p.x - b.x, p.y - b.y);
                    if (dist <= p.radius * 1.88) {
                        return { hitBubble: b, row: r, col: c };
                    }
                }
            }
        }

        return null;
    }

    resolveProjectileLand(p, collision) {
        // Find nearest valid empty grid slot
        let targetSlot = this.findNearestEmptySlot(p.x, p.y);
        if (!targetSlot) {
            targetSlot = { row: 0, col: 0 };
        }

        const landedBubble = new Bubble(targetSlot.row, targetSlot.col, p.bubble.colorKey, p.bubble.type);
        landedBubble.radius = this.bubbleRadius;
        landedBubble.x = this.getCenterX(targetSlot.row, targetSlot.col);
        landedBubble.y = this.getCenterY(targetSlot.row);
        this.grid[targetSlot.row][targetSlot.col] = landedBubble;

        // Check if landed on or adjacent to any Bomb on the grid, or if shot was a Bomb
        let hitBombs = [];
        if (landedBubble.type === 'BOMB' || landedBubble.colorKey === 'BOMB') {
            hitBombs.push(targetSlot);
        }
        if (collision.hitBubble && (collision.hitBubble.type === 'BOMB' || collision.hitBubble.colorKey === 'BOMB')) {
            hitBombs.push({ row: collision.hitBubble.row, col: collision.hitBubble.col });
        }
        const neighbors = this.getNeighbors(targetSlot.row, targetSlot.col);
        neighbors.forEach(n => {
            const nb = this.grid[n.row][n.col];
            if (nb && (nb.type === 'BOMB' || nb.colorKey === 'BOMB')) {
                hitBombs.push(n);
            }
        });

        if (hitBombs.length > 0) {
            sounds.playBomb();
            this.explodeBombs(hitBombs);
            this.postShotResolution();
            return;
        }

        // Handle Rainbow Booster
        if (landedBubble.type === 'RAINBOW' && collision.hitBubble) {
            landedBubble.colorKey = collision.hitBubble.colorKey;
            landedBubble.type = 'NORMAL';
        }

        // Find match cluster
        const cluster = this.findCluster(targetSlot.row, targetSlot.col, landedBubble.colorKey);
        if (cluster.length >= 3) {
            this.comboCount++;
            const pitchFactor = Math.min(2.2, 1.0 + this.comboCount * 0.15);
            sounds.playPop(pitchFactor);

            // Check if any matched bubble is adjacent to a Bomb on the grid
            const adjacentBombs = [];
            cluster.forEach(slot => {
                this.getNeighbors(slot.row, slot.col).forEach(n => {
                    const nb = this.grid[n.row][n.col];
                    if (nb && (nb.type === 'BOMB' || nb.colorKey === 'BOMB')) {
                        adjacentBombs.push(n);
                    }
                });
            });

            if (adjacentBombs.length > 0) {
                sounds.playBomb();
                this.explodeBombs(adjacentBombs);
            }

            // Calculate score
            const basePoints = cluster.length * 40;
            const comboBonus = (this.comboCount > 1) ? this.comboCount * 60 : 0;
            const earnedScore = basePoints + comboBonus;
            this.score += earnedScore;

            // Pop cluster bubbles
            cluster.forEach(slot => {
                const b = this.grid[slot.row][slot.col];
                if (b) {
                    b.isPopping = true;
                    this.poppingBubbles.push(b);
                    this.grid[slot.row][slot.col] = null;
                    this.spawnConfetti(b.x, b.y, BUBBLE_COLORS[b.colorKey]?.primary || '#fff');
                }
            });

            this.floatingTexts.push(new FloatingText(landedBubble.x, landedBubble.y, `+${earnedScore}`));
            if (this.comboCount > 1) {
                this.floatingTexts.push(new FloatingText(landedBubble.x, landedBubble.y - 25, `COMBO x${this.comboCount}!`, '#FF4081'));
            }

            // Drop unattached floating bubbles
            this.dropFloatingBubbles();
        } else {
            this.comboCount = 0;
            sounds.playBounce();
        }

        this.postShotResolution();
    }

    explodeBombs(initialBombs) {
        const affectedSlots = new Set();
        const bombQueue = [...initialBombs];
        const processedBombs = new Set();

        while (bombQueue.length > 0) {
            const currentBomb = bombQueue.shift();
            const key = `${currentBomb.row},${currentBomb.col}`;
            if (processedBombs.has(key)) continue;
            processedBombs.add(key);
            affectedSlots.add(key);

            // 2 neighbor rings around the bomb
            const ring1 = this.getNeighbors(currentBomb.row, currentBomb.col);
            const localRadius = new Set(ring1.map(p => `${p.row},${p.col}`));
            localRadius.add(key);
            ring1.forEach(p => {
                this.getNeighbors(p.row, p.col).forEach(p2 => localRadius.add(`${p2.row},${p2.col}`));
            });

            localRadius.forEach(locKey => {
                const [rStr, cStr] = locKey.split(',');
                const r = parseInt(rStr);
                const c = parseInt(cStr);
                const b = this.grid[r] ? this.grid[r][c] : null;
                if (b) {
                    affectedSlots.add(locKey);
                    if ((b.type === 'BOMB' || b.colorKey === 'BOMB') && !processedBombs.has(locKey)) {
                        bombQueue.push({ row: r, col: c });
                    }
                }
            });
        }

        const bombScore = affectedSlots.size * 60;
        this.score += bombScore;

        const first = initialBombs[0];
        const cx = this.getCenterX(first.row, first.col);
        const cy = this.getCenterY(first.row);
        this.floatingTexts.push(new FloatingText(cx, cy, `BOOM! +${bombScore}`, '#ff5722'));

        affectedSlots.forEach(locKey => {
            const [rStr, cStr] = locKey.split(',');
            const r = parseInt(rStr);
            const c = parseInt(cStr);
            const b = this.grid[r] ? this.grid[r][c] : null;
            if (b) {
                b.isPopping = true;
                this.poppingBubbles.push(b);
                this.grid[r][c] = null;
                this.spawnConfetti(b.x, b.y, (b.type === 'BOMB' || b.colorKey === 'BOMB') ? '#ff5722' : '#ff9800');
            }
        });

        this.dropFloatingBubbles();
    }

    findNearestEmptySlot(px, py) {
        let bestSlot = null;
        let minDist = Infinity;

        for (let r = 0; r < MAX_ROWS; r++) {
            const cols = (r % 2 === 0) ? COLS_EVEN : COLS_ODD;
            for (let c = 0; c < cols; c++) {
                if (this.grid[r][c] === null) {
                    // Check if adjacent to ceiling or adjacent to an existing bubble
                    const hasNeighbor = (r === 0) || this.getNeighbors(r, c).some(pos => this.grid[pos.row][pos.col] !== null);
                    if (hasNeighbor) {
                        const sx = this.getCenterX(r, c);
                        const sy = this.getCenterY(r);
                        const dist = Math.hypot(px - sx, py - sy);
                        if (dist < minDist) {
                            minDist = dist;
                            bestSlot = { row: r, col: c };
                        }
                    }
                }
            }
        }

        return bestSlot;
    }

    getNeighbors(row, col) {
        const neighbors = [];
        const isEven = (row % 2 === 0);

        const offsets = isEven ? [
            { dr: -1, dc: -1 }, { dr: -1, dc: 0 },
            { dr: 0, dc: -1 },  { dr: 0, dc: 1 },
            { dr: 1, dc: -1 },  { dr: 1, dc: 0 }
        ] : [
            { dr: -1, dc: 0 },  { dr: -1, dc: 1 },
            { dr: 0, dc: -1 },  { dr: 0, dc: 1 },
            { dr: 1, dc: 0 },   { dr: 1, dc: 1 }
        ];

        offsets.forEach(off => {
            const nr = row + off.dr;
            const nc = col + off.dc;
            if (nr >= 0 && nr < MAX_ROWS) {
                const maxCols = (nr % 2 === 0) ? COLS_EVEN : COLS_ODD;
                if (nc >= 0 && nc < maxCols) {
                    neighbors.push({ row: nr, col: nc });
                }
            }
        });

        return neighbors;
    }

    findCluster(startRow, startCol, targetColorKey) {
        const cluster = [];
        const visited = Array(MAX_ROWS).fill(false).map(() => Array(COLS_EVEN).fill(false));
        const queue = [{ row: startRow, col: startCol }];
        visited[startRow][startCol] = true;

        while (queue.length > 0) {
            const curr = queue.shift();
            cluster.push(curr);

            const neighbors = this.getNeighbors(curr.row, curr.col);
            neighbors.forEach(n => {
                if (!visited[n.row][n.col]) {
                    const nb = this.grid[n.row][n.col];
                    if (nb && nb.colorKey === targetColorKey) {
                        visited[n.row][n.col] = true;
                        queue.push(n);
                    }
                }
            });
        }

        return cluster;
    }

    dropFloatingBubbles() {
        const visited = Array(MAX_ROWS).fill(false).map(() => Array(COLS_EVEN).fill(false));
        const queue = [];

        // Add all bubbles in the top ceiling row (row 0)
        for (let c = 0; c < COLS_EVEN; c++) {
            if (this.grid[0][c] !== null) {
                visited[0][c] = true;
                queue.push({ row: 0, col: c });
            }
        }

        // BFS to find all attached bubbles
        while (queue.length > 0) {
            const curr = queue.shift();
            const neighbors = this.getNeighbors(curr.row, curr.col);
            neighbors.forEach(n => {
                if (!visited[n.row][n.col] && this.grid[n.row][n.col] !== null) {
                    visited[n.row][n.col] = true;
                    queue.push(n);
                }
            });
        }

        // Any bubble not visited is floating -> drop it!
        let droppedCount = 0;
        for (let r = 0; r < MAX_ROWS; r++) {
            const cols = (r % 2 === 0) ? COLS_EVEN : COLS_ODD;
            for (let c = 0; c < cols; c++) {
                const b = this.grid[r][c];
                if (b !== null && !visited[r][c]) {
                    b.isFalling = true;
                    b.vx = (Math.random() - 0.5) * 200;
                    b.vy = -(100 + Math.random() * 150);
                    this.fallingBubbles.push(b);
                    this.grid[r][c] = null;
                    droppedCount++;
                }
            }
        }

        if (droppedCount > 0) {
            const dropScore = droppedCount * 100;
            this.score += dropScore;
            sounds.playPop(0.85);
            this.floatingTexts.push(new FloatingText(this.canvas.width * 0.5, this.canvas.height * 0.45, `DROPPED! +${dropScore}`, '#4caf50'));
        }
    }

    spawnConfetti(x, y, color) {
        for (let i = 0; i < 14; i++) {
            this.particles.push(new Particle(x, y, color));
        }
    }

    postShotResolution() {
        this.isShooting = false;

        // Check star thresholds
        if (this.levelData && this.levelData.starThresholds) {
            const th = this.levelData.starThresholds;
            if (this.score >= th[2]) this.starsEarned = 3;
            else if (this.score >= th[1]) this.starsEarned = 2;
            else if (this.score >= th[0]) this.starsEarned = 1;
        }

        this.updateHUD();

        // Check Win condition (All bubbles cleared)
        let hasBubbles = false;
        for (let r = 0; r < MAX_ROWS; r++) {
            for (let c = 0; c < COLS_EVEN; c++) {
                if (this.grid[r][c] !== null) {
                    hasBubbles = true;
                    break;
                }
            }
            if (hasBubbles) break;
        }

        if (!hasBubbles) {
            this.isGameOver = true;
            if (this.starsEarned === 0) this.starsEarned = 1;
            sounds.playWin();
            setTimeout(() => {
                if (this.onGameWon) this.onGameWon(this.currentLevel, this.score, this.starsEarned);
            }, 500);
            return;
        }

        // Check Loss condition
        if (this.shotsRemaining <= 0) {
            this.isGameOver = true;
            sounds.playLose();
            setTimeout(() => {
                if (this.onGameLost) this.onGameLost(this.currentLevel, this.score);
            }, 600);
            return;
        }
    }

    updateHUD() {
        const th = this.levelData?.starThresholds || [800, 1600, 2500];
        const progress = Math.min(1.0, this.score / th[2]);
        if (this.onScoreUpdated) {
            this.onScoreUpdated(this.score, this.starsEarned, progress);
        }
        if (this.onShotsUpdated) {
            this.onShotsUpdated(this.shotsRemaining);
        }
    }

    render() {
        const ctx = this.ctx;
        ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        // 1. Rich cartoon sky gradient (matching Android BubbleGameView #1B1F3B -> #1E2749 -> #273469)
        const bgGrad = ctx.createLinearGradient(0, 0, 0, this.canvas.height);
        bgGrad.addColorStop(0, '#1B1F3B');
        bgGrad.addColorStop(0.55, '#1E2749');
        bgGrad.addColorStop(1, '#273469');
        ctx.fillStyle = bgGrad;
        ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        // Top ceiling border
        const topY = this.boardTop;
        ctx.fillStyle = '#2F3858';
        ctx.fillRect(0, 0, this.canvas.width, topY);
        ctx.strokeStyle = '#FFD54F';
        ctx.lineWidth = 4;
        ctx.beginPath();
        ctx.moveTo(0, topY);
        ctx.lineTo(this.canvas.width, topY);
        ctx.stroke();

        // 2. Trajectory dots
        if (this.isAiming && !this.isShooting && !this.isGameOver) {
            const points = this.calculateTrajectory();
            const total = points.length;
            const dotCol = BUBBLE_COLORS[this.currentBubble.colorKey]?.light || '#ffffff';

            points.forEach((pt, i) => {
                const rFactor = 0.22 + 0.15 * ((total - i) / total);
                const dotR = this.bubbleRadius * rFactor;

                ctx.fillStyle = dotCol;
                ctx.globalAlpha = 0.7;
                ctx.beginPath();
                ctx.arc(pt.x, pt.y, dotR, 0, Math.PI * 2);
                ctx.fill();

                ctx.fillStyle = '#ffffff';
                ctx.globalAlpha = 0.94;
                ctx.beginPath();
                ctx.arc(pt.x, pt.y, dotR * 0.45, 0, Math.PI * 2);
                ctx.fill();
            });
            ctx.globalAlpha = 1.0;
        }

        // 3. Draw Grid Bubbles
        for (let r = 0; r < MAX_ROWS; r++) {
            for (let c = 0; c < COLS_EVEN; c++) {
                const b = this.grid[r][c];
                if (b) b.draw(ctx);
            }
        }

        // 4. Draw Popping & Falling Bubbles
        this.poppingBubbles.forEach(b => b.draw(ctx));
        this.fallingBubbles.forEach(b => b.draw(ctx));

        // 5. Draw Projectile
        if (this.activeProjectile) {
            this.activeProjectile.bubble.x = this.activeProjectile.x;
            this.activeProjectile.bubble.y = this.activeProjectile.y;
            this.activeProjectile.bubble.draw(ctx);
        }

        // 6. Draw Launcher Pedestal & Bubbles
        this.drawLauncher(ctx);

        // 7. Draw Particles and Floating Texts
        this.particles.forEach(pt => pt.draw(ctx));
        this.floatingTexts.forEach(ft => ft.draw(ctx));
    }

    drawLauncher(ctx) {
        // Launcher Stand / Pedestal (1:1 with Android GameEngine drawLauncher)
        ctx.fillStyle = '#424242';
        ctx.beginPath();
        ctx.arc(this.launcherX, this.launcherY, this.bubbleRadius * 1.35, 0, Math.PI * 2);
        ctx.fill();

        ctx.strokeStyle = '#FFD54F';
        ctx.lineWidth = this.bubbleRadius * 0.18;
        ctx.beginPath();
        ctx.arc(this.launcherX, this.launcherY, this.bubbleRadius * 1.3, 0, Math.PI * 2);
        ctx.stroke();

        // Preview Bubble Pedestal
        ctx.fillStyle = '#616161';
        ctx.beginPath();
        ctx.arc(this.previewX, this.previewY, this.bubbleRadius * 0.95, 0, Math.PI * 2);
        ctx.fill();

        // Next Bubble text badge
        ctx.fillStyle = '#FFFFFF';
        ctx.font = `bold ${Math.floor(this.bubbleRadius * 0.4)}px sans-serif`;
        ctx.textAlign = 'center';
        ctx.fillText('NEXT', this.previewX, this.previewY - this.bubbleRadius * 1.1);

        // Ready Bubble in Launcher
        if (this.currentBubble && !this.isShooting) {
            this.currentBubble.draw(ctx);
        }

        // Preview Bubble
        if (this.nextBubble) {
            this.nextBubble.draw(ctx);
        }

        // Swap Icon Indicator (arrows between preview and launcher)
        ctx.fillStyle = '#FFFFFF';
        ctx.font = `bold ${Math.floor(this.bubbleRadius * 0.5)}px sans-serif`;
        ctx.textAlign = 'center';
        ctx.fillText('⇄', (this.launcherX + this.previewX) / 2, (this.launcherY + this.previewY) / 2 + (this.bubbleRadius * 0.15));

        // Aim indicator arrow on launcher ring
        if (this.isAiming) {
            ctx.save();
            ctx.translate(this.launcherX, this.launcherY);
            ctx.rotate(this.aimAngle + Math.PI / 2);
            ctx.fillStyle = '#FFD54F';
            ctx.beginPath();
            ctx.moveTo(0, -this.bubbleRadius * 1.55);
            ctx.lineTo(-this.bubbleRadius * 0.25, -this.bubbleRadius * 1.35);
            ctx.lineTo(this.bubbleRadius * 0.25, -this.bubbleRadius * 1.35);
            ctx.closePath();
            ctx.fill();
            ctx.restore();
        }
    }
}
