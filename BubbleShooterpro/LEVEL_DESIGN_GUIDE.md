# 🎮 Bubble Shooter Pro — Level Design & Mechanics Guide

This guide provides everything needed to create, balance, and customize levels in **Bubble Shooter Pro**.

---

## 📁 File Structure & Location

All level files are stored as JSON documents in:
```
app/src/main/assets/levels/level_XX.json
```
- Level numbers are 2-digit zero-padded (`level_01.json`, `level_02.json`, ..., `level_30.json`).
- If a level JSON is missing, the engine falls back to `LevelLoader.createFallbackLevel()`.

---

## 📝 Level JSON Template

```json
{
  "level": 12,
  "shots": 25,
  "colors": [
    "RED",
    "PURPLE",
    "YELLOW"
  ],
  "objective": {
    "type": "CLEAR_ALL",
    "target": 0
  },
  "starThresholds": [
    1200,
    2500,
    3800
  ],
  "rows": [
    ".RR..RR.",
    "RRRRRRR",
    "RRRRRRRR",
    "P.S.S.P",
    ".YYYYY.",
    "..YYY..",
    "...Y..."
  ]
}
```

---

## 📐 Grid Layout & Coordinate System

Bubble Shooter Pro uses a staggered **Hexagonal Honeycomb Grid**:

| Row Type | Row Indices (0-indexed) | Column Count | Description |
| :--- | :--- | :--- | :--- |
| **Even Rows** | 0, 2, 4, 6, 8, 10, 12 | **8 Columns** | Aligned from the left boundary |
| **Odd Rows** | 1, 3, 5, 7, 9, 11, 13 | **7 Columns** | Indented by $0.5 \times \text{Bubble Radius}$ |

- **Maximum Rows**: Up to 14 rows per level grid.
- **Top Anchor**: Row 0 is attached to the ceiling. Any bubbles that become completely disconnected from Row 0 automatically drop as floating bubbles with bonus score.

---

## 🎨 Bubble Character & Token Reference

| Token | Bubble Type | Color / Effect | Description & Behavior |
| :---: | :--- | :--- | :--- |
| `R` | Normal | **Red** (`#FF1744`) | Standard bubble. Matches 3+ of same color. |
| `G` | Normal | **Green** (`#00C853`) | Standard bubble. Matches 3+ of same color. |
| `B` | Normal | **Blue** (`#0091EA`) | Standard bubble. Matches 3+ of same color. |
| `Y` | Normal | **Yellow** (`#FFD600`) | Standard bubble. Matches 3+ of same color. |
| `P` | Normal | **Purple** (`#AA00FF`) | Standard bubble. Matches 3+ of same color. |
| `O` | Normal | **Orange** (`#FF6D00`) | Standard bubble. Matches 3+ of same color. |
| `C` | Normal | **Cyan** (`#00E5FF`) | Standard bubble. Matches 3+ of same color. |
| `.` | Empty | *Transparent* | Empty grid position (allows sculpting gaps and mazes). |
| `*` | **Rainbow** | *6-Color Spectrum* | **Wildcard**. Matches any color it touches upon impact. |
| `X` / `x` | **Bomb** | *Dark Metallic + Spark* | **Blast Power**. Explodes in a 2-ring radius, destroying all surrounding bubbles and detonating nested powers. |
| `L` / `l` | **Lightning** | *Electric Bolt Glow* | **Row Vaporizer**. If struck from below or wedged into a row, the entire struck row instantly vaporizes. |
| `F` / `f` | **Fireball** | *Molten Magma Core* | **Incinerator Blast**. Obliterates everything in a 2-ring radius, incinerating all bubbles including Stone. |
| `S` / `s` | **Stone** | *Granite Block* | **Unbreakable Obstacle**. Immune to normal bubbles and Rainbow matches. **Crushed only by special powers** (Bomb, Lightning, Fireball) or when disconnected from the ceiling. |

---

## 🎯 Level Objectives

Configure the `"objective"` object with one of the following 4 modes:

### 1. Clear All (`CLEAR_ALL`)
```json
"objective": {
  "type": "CLEAR_ALL",
  "target": 0
}
```
*Goal: Remove all non-empty bubbles from the grid before running out of shots.*

### 2. Pop Specific Color (`POP_COLOR`)
```json
"objective": {
  "type": "POP_COLOR",
  "target": 15,
  "color": "PURPLE"
}
```
*Goal: Pop at least `target` bubbles of the specified `color`.*

### 3. Score Target (`SCORE_TARGET`)
```json
"objective": {
  "type": "SCORE_TARGET",
  "target": 3500
}
```
*Goal: Reach the target score through match combos, power explosions, and drops.*

### 4. Drop Count (`DROP_COUNT`)
```json
"objective": {
  "type": "DROP_COUNT",
  "target": 12
}
```
*Goal: Sever ceiling connections to drop at least `target` floating bubbles.*

---

## ⭐ Star Thresholds (`starThresholds`)

Define 3 increasing score milestones for 1-Star, 2-Star, and 3-Star mastery:
```json
"starThresholds": [
  1200,   // 1 Star
  2500,   // 2 Stars
  3800    // 3 Stars
]
```

---

## ⚔️ Power vs. Stone Interaction Rules

| Element / Shot Type | Effect on Normal Bubbles | Effect on Stone (`S`) Bubbles |
| :--- | :--- | :--- |
| **Normal Color Shot** | Matches 3+ of same color | ❌ **Immune** (No effect, snaps adjacent) |
| **Rainbow Booster (`*`)** | Matches adjacent neighbor color | ❌ **Immune** (Cannot match Stone) |
| **Bomb Booster (`X`)** | 2-ring explosion | 💥 **Crushed & Destroyed** |
| **Lightning Booster (`L`)** | Vaporizes entire hit row | ⚡ **Vaporized & Cleared** |
| **Fireball Booster (`F`)** | Incinerates 2-ring blast | 🔥 **Incinerated & Destroyed** |
| **Ceiling Severing** | Drops with physics | 🪨 **Falls & Drops** with bonus points |

---

## 🗺️ World & Biome Pacing Guidelines

| World | Levels | Biome Theme | Recommended Colors | Gimmicks & Powers Introduced |
| :---: | :---: | :--- | :---: | :--- |
| **World 1** | 1 – 10 | 🌿 **Bubble Meadows** | 3 – 4 colors | Basics, Rainbow Booster, simple clusters |
| **World 2** | 11 – 20 | 💎 **Crystal Caverns** | 4 – 5 colors | Bomb Booster, Stone obstacles (`S`), tighter shot budgets |
| **World 3** | 21 – 30 | 🌌 **Celestial Cosmos** | 5 – 6 colors | Lightning (`L`), Fireball (`F`), multi-layer Stone barriers, chain reaction puzzles |

---

## 💡 Level Design Tips

1. **Chokepoints**: Place a single `S` (Stone) bubble in the middle of an odd row to create two distinct branches players must shoot around.
2. **Chain Reaction Payoffs**: Surround a single `X` (Bomb) with `L` (Lightning) and `F` (Fireball) tokens behind a wall of matching bubbles for explosive combo fireworks.
3. **Pacing Shots**: Standard level budgets should allow $20 - 28$ shots. For tight puzzle levels, reduce to $15 - 18$ shots with strategically placed power bubbles.
4. **Color Pool**: Always list all colors used in `"colors": [...]`. If a color is omitted from `"colors"`, the launcher will not spawn shots of that color once existing board clusters are cleared.
