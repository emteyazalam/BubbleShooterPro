@echo off
title Game Designer Studio Launcher
cd /d "%~dp0"
if exist "Game Designer Studio.exe" (
    echo Launching Game Designer Studio Standalone App...
    start "" "Game Designer Studio.exe"
    exit
)
cd /d "%~dp0\game-designer-studio"
echo Starting Studio Desktop App via Electron...
start "" npx.cmd electron .
exit
