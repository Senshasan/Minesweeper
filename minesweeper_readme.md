# Minesweeper

## About the Game
This repository features a Java-based implementation of the classic Minesweeper game. The project's architecture clearly separates the graphical user interface (`MinesweeperUI.java`) from the underlying game mechanics (`MinesweeperLogic.java`) and individual grid elements (`Tile.java`). It also incorporates custom visual assets, including titles and powerups (`Powerups.png`), to enhance the traditional gameplay experience.

## How to Start the Game
This project includes a Gradle wrapper setup (`gradlew` and `gradlew.bat`), which allows you to build and run the game directly without needing a manual Gradle installation on your machine.

### Prerequisites
* A compatible Java Development Kit (JDK) installed on your system.

### Linux / macOS
1. Open a terminal and navigate to the project's root directory.
2. Ensure the `gradlew` wrapper script has execution permissions by running: `chmod +x gradlew`.
3. Start the application by executing the wrapper: `./gradlew run`.

### Windows
1. Open Command Prompt or PowerShell and navigate to the project's root directory.
2. Start the application by running the provided batch script: `gradlew.bat run`.