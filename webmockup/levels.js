const GAME_LEVELS = {
  "1": {
    "shots": 28,
    "colors": [
      "RED",
      "BLUE",
      "YELLOW"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      800,
      1600,
      2500
    ],
    "rows": [
      "RRRRBBBB",
      "YYRRBB",
      "YYYYRRRR",
      "BBYYYY",
      "BBBB...."
    ],
    "level": 1
  },
  "2": {
    "shots": 26,
    "colors": [
      "RED",
      "BLUE",
      "GREEN"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      900,
      1800,
      2800
    ],
    "rows": [
      "GG...GGG",
      "GG.GGGG",
      "RRBB..RR",
      "RRBBB.",
      ".RRGG..",
      "..RR..."
    ],
    "level": 2
  },
  "3": {
    "shots": 26,
    "colors": [
      "RED",
      "YELLOW",
      "BLUE",
      "GREEN"
    ],
    "objective": {
      "type": "DROP_COUNT",
      "target": 15
    },
    "starThresholds": [
      1000,
      2000,
      3200
    ],
    "rows": [
      "RRRRRRRR",
      "YYYYYYY",
      "GGGGGGGG",
      "BBBBBBB",
      "..RRRR..",
      "...YY..."
    ],
    "level": 3
  },
  "4": {
    "shots": 25,
    "colors": [
      "YELLOW",
      "BLUE",
      "RED"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1100,
      2200,
      3500
    ],
    "rows": [
      "YYYYYYYY",
      "YBB.BBY",
      "YYYYYYYY",
      "Y.RRR.Y",
      "YYYYYYYY",
      ".YYYYYY"
    ],
    "level": 4
  },
  "5": {
    "shots": 26,
    "colors": [
      "RED",
      "BLUE",
      "YELLOW",
      "GREEN"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1200,
      2400,
      3600
    ],
    "rows": [
      "RBYGRBYG",
      "RBYGRBY",
      "RBYGRBYG",
      "RBYGRBY",
      "GGYYBBRR",
      ".YYBBRR"
    ],
    "level": 5
  },
  "6": {
    "shots": 25,
    "colors": [
      "RED",
      "YELLOW",
      "GREEN"
    ],
    "objective": {
      "type": "POP_COLOR",
      "target": 12,
      "color": "RED"
    },
    "starThresholds": [
      1000,
      2000,
      3000
    ],
    "rows": [
      "GG...GGG",
      "G.YYY.G",
      "G.YRY.G",
      ".RRRRR.",
      "..RRR..",
      "...R..."
    ],
    "level": 6
  },
  "7": {
    "shots": 24,
    "colors": [
      "BLUE",
      "YELLOW",
      "RED",
      "GREEN"
    ],
    "objective": {
      "type": "DROP_COUNT",
      "target": 18
    },
    "starThresholds": [
      1200,
      2500,
      3800
    ],
    "rows": [
      "BBB.BBBB",
      "YYY.YYY",
      ".RR.RR.",
      "..G.G..",
      "BB...BBB",
      ".YY.YY."
    ],
    "level": 7
  },
  "8": {
    "shots": 25,
    "colors": [
      "BLUE",
      "YELLOW"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1000,
      2100,
      3200
    ],
    "rows": [
      "BYBYBYBY",
      "YBYBYBY",
      "BYBYBYBY",
      "YBYBYBY",
      "BYBYBYBY",
      "YBYBYBY"
    ],
    "level": 8
  },
  "9": {
    "shots": 24,
    "colors": [
      "RED",
      "GREEN",
      "BLUE"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1100,
      2300,
      3600
    ],
    "rows": [
      "RR...RRR",
      "GG...GG",
      ".RR...RR",
      "..GG...G",
      "...RR..R",
      "....GG."
    ],
    "level": 9
  },
  "10": {
    "shots": 26,
    "colors": [
      "RED",
      "BLUE",
      "GREEN",
      "YELLOW"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1500,
      3000,
      4500
    ],
    "rows": [
      "R.B.Y.G.",
      "R.B.Y.G",
      "RRBBYYGG",
      "RRBBYYG",
      "BBYYGGRR",
      "BBYYGGR",
      "YYGGRRBB"
    ],
    "level": 10
  },
  "11": {
    "shots": 25,
    "colors": [
      "PURPLE",
      "YELLOW",
      "BLUE"
    ],
    "objective": {
      "type": "POP_COLOR",
      "target": 14,
      "color": "PURPLE"
    },
    "starThresholds": [
      1100,
      2300,
      3500
    ],
    "rows": [
      "PPPPPPPP",
      "YYYYYYY",
      "BBBBBBBB",
      "PPPPPPP",
      "YYYYYYYY",
      ".BBBBBB"
    ],
    "level": 11
  },
  "12": {
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
      "PPPPPPP",
      ".YYYYY.",
      "..YYY..",
      "...Y..."
    ],
    "level": 12
  },
  "13": {
    "shots": 24,
    "colors": [
      "BLUE",
      "GREEN",
      "PURPLE"
    ],
    "objective": {
      "type": "DROP_COUNT",
      "target": 20
    },
    "starThresholds": [
      1300,
      2600,
      4000
    ],
    "rows": [
      "BBB..GGG",
      "BBB..GG",
      ".PP..PP.",
      "..P..P..",
      "GGG..BBB",
      ".GG..BB."
    ],
    "level": 13
  },
  "14": {
    "shots": 25,
    "colors": [
      "PURPLE",
      "YELLOW",
      "GREEN",
      "RED"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1200,
      2500,
      3900
    ],
    "rows": [
      "PP.GG.PP",
      "YY.YY.YY",
      "PP.RR.PP",
      ".Y.RR.Y.",
      "..G..G..",
      "...GG..."
    ],
    "level": 14
  },
  "15": {
    "shots": 24,
    "colors": [
      "BLUE",
      "YELLOW",
      "RED",
      "GREEN",
      "PURPLE"
    ],
    "objective": {
      "type": "SCORE_TARGET",
      "target": 3000
    },
    "starThresholds": [
      1500,
      3000,
      4600
    ],
    "rows": [
      "RRBBYYGG",
      "PPBBYYG",
      "YYGGRRPP",
      "GGRRPPB",
      "BBYYGGRR",
      "PPGGBBR"
    ],
    "level": 15
  },
  "16": {
    "shots": 24,
    "colors": [
      "YELLOW",
      "BLUE",
      "PURPLE"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1300,
      2600,
      4000
    ],
    "rows": [
      "YYYYYYYY",
      ".BBBBB.",
      "..PPP..",
      "...B...",
      "..PPP..",
      ".BBBBB.",
      "YYYYYYYY"
    ],
    "level": 16
  },
  "17": {
    "shots": 25,
    "colors": [
      "RED",
      "BLUE",
      "GREEN",
      "YELLOW"
    ],
    "objective": {
      "type": "DROP_COUNT",
      "target": 22
    },
    "starThresholds": [
      1400,
      2800,
      4200
    ],
    "rows": [
      "RRRRRRRR",
      ".BBBBBB",
      "..GGGGG.",
      "...YYYY",
      "....RRR.",
      ".....BB"
    ],
    "level": 17
  },
  "18": {
    "shots": 24,
    "colors": [
      "GREEN",
      "PURPLE",
      "RED"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1200,
      2500,
      3900
    ],
    "rows": [
      "GG....GG",
      "GG....GG",
      "PP....PP",
      "PP....PP",
      "RR....RR",
      "RR....RR",
      "GGGGGGGG"
    ],
    "level": 18
  },
  "19": {
    "shots": 24,
    "colors": [
      "BLUE",
      "YELLOW",
      "RED",
      "PURPLE"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1300,
      2700,
      4100
    ],
    "rows": [
      "BBBBBBBB",
      "Y......B",
      "Y.RRRR.B",
      "Y.P..R.B",
      "Y.P....B",
      "Y.PPPPPB"
    ],
    "level": 19
  },
  "20": {
    "shots": 25,
    "colors": [
      "YELLOW",
      "PURPLE",
      "RED",
      "BLUE"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1600,
      3200,
      4800
    ],
    "rows": [
      "Y..Y..Y.",
      "YY.YY.YY",
      "YYYYYYYY",
      "PPPPPPP",
      "RRRRRRRR",
      "BBBBBBB",
      "YYYYYYYY"
    ],
    "level": 20
  },
  "21": {
    "shots": 23,
    "colors": [
      "RED",
      "BLUE",
      "GREEN",
      "YELLOW"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1400,
      2800,
      4300
    ],
    "rows": [
      "RRBBYYGG",
      "RR.X.GG",
      "BBY.YGGR",
      "BB.X.RR",
      "YYGGRRBB"
    ],
    "level": 21
  },
  "22": {
    "shots": 23,
    "colors": [
      "PURPLE",
      "ORANGE",
      "BLUE",
      "GREEN"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1400,
      2900,
      4400
    ],
    "rows": [
      "PPOOGGBB",
      "PP.*.BB",
      "OOPPBBGG",
      "OO.*.GG",
      "GGBBOOPP"
    ],
    "level": 22
  },
  "23": {
    "shots": 24,
    "colors": [
      "RED",
      "YELLOW",
      "BLUE",
      "ORANGE"
    ],
    "objective": {
      "type": "DROP_COUNT",
      "target": 25
    },
    "starThresholds": [
      1500,
      3100,
      4700
    ],
    "rows": [
      "RR..YY..",
      ".R..Y..",
      "BB..OO..",
      ".B..O..",
      ".X..X...",
      "RRBBYYOO",
      "RRBBYYO"
    ],
    "level": 23
  },
  "24": {
    "shots": 23,
    "colors": [
      "BLUE",
      "GREEN",
      "PURPLE",
      "YELLOW"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1400,
      2800,
      4400
    ],
    "rows": [
      "BBBBBBBB",
      "......G.",
      "GGGGGGG.",
      ".P......",
      ".PPPPPPP",
      "......Y.",
      "YYYYYYYY"
    ],
    "level": 24
  },
  "25": {
    "shots": 24,
    "colors": [
      "RED",
      "BLUE",
      "YELLOW",
      "GREEN",
      "PURPLE",
      "ORANGE"
    ],
    "objective": {
      "type": "SCORE_TARGET",
      "target": 4000
    },
    "starThresholds": [
      2000,
      4000,
      6000
    ],
    "rows": [
      "RBYGPO..",
      "RBYGPO.",
      "..RBYGPO",
      ".RBYGPO",
      "OPGYBR..",
      "OPGYBR.",
      "..OPGYBR"
    ],
    "level": 25
  },
  "26": {
    "shots": 22,
    "colors": [
      "PURPLE",
      "BLUE",
      "ORANGE"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1400,
      2900,
      4500
    ],
    "rows": [
      "PPPPPPPP",
      "P......P",
      "P.BBBB.P",
      "P.B..B.P",
      "P.B.O.B.P",
      "P.B..B.P",
      "P.BBBB.P"
    ],
    "level": 26
  },
  "27": {
    "shots": 22,
    "colors": [
      "RED",
      "GREEN",
      "YELLOW"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      1500,
      3200,
      4800
    ],
    "rows": [
      "RRRGGYYY",
      "RR.X.YY",
      "GGG..GGG",
      "GG.X.GG",
      "YYY..RRR",
      "YY.X.RR"
    ],
    "level": 27
  },
  "28": {
    "shots": 23,
    "colors": [
      "YELLOW",
      "BLUE",
      "PURPLE",
      "ORANGE"
    ],
    "objective": {
      "type": "POP_COLOR",
      "target": 18,
      "color": "YELLOW"
    },
    "starThresholds": [
      1500,
      3000,
      4700
    ],
    "rows": [
      "...YY...",
      "..YYYY..",
      "YYYYYYYY",
      ".BBPPOO.",
      "..YYYY..",
      "...YY..."
    ],
    "level": 28
  },
  "29": {
    "shots": 22,
    "colors": [
      "RED",
      "BLUE",
      "GREEN",
      "YELLOW",
      "PURPLE"
    ],
    "objective": {
      "type": "DROP_COUNT",
      "target": 28
    },
    "starThresholds": [
      1600,
      3300,
      5000
    ],
    "rows": [
      "RRBBYYGG",
      "PPBBYYG",
      ".RR.PP.",
      "..GG.BB.",
      "...YY...",
      "PP..RR..",
      ".GG..BB."
    ],
    "level": 29
  },
  "30": {
    "shots": 25,
    "colors": [
      "RED",
      "BLUE",
      "YELLOW",
      "GREEN",
      "PURPLE",
      "ORANGE"
    ],
    "objective": {
      "type": "CLEAR_ALL",
      "target": 0
    },
    "starThresholds": [
      2200,
      4500,
      7000
    ],
    "rows": [
      "RBYGPO.*",
      "R.X.P.X",
      "OPGYBR.*",
      "O.X.Y.X",
      "RBYGPO.*",
      "P.X.G.X",
      "OPGYBR.*",
      "G.X.B.X"
    ],
    "level": 30
  }
};