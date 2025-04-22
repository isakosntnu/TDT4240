# Draw & Guess Game

This is a simple multiplayer drawing and guessing game built with LibGDX and Firebase. The game is meant to be played i portrait mode.

## Building and Running

### Compilation

If you are using the Firebase Functions backend, or running the web version of the game, make sure to install the Node.js dependencies:

npm install

Once the project has been opened and Gradle has finished syncing, Android Studio will automatically index the project and make it ready for compilation. To compile the project:
1. Open Android Studio
2. Click Build > Make Project in the top menu

It also may be necessary to provide your own SDK path in the local.properties file. 
Android Studio will compile the code and check for any errors. No additional terminal commands are necessary, as all compilation is handled by the IDE.
   

### Running the App
To run DrawGuess on an Android emulator:
1. Open the Run menu and click Run 'android'
2. If you don't have an emulator configured:
   - Go to Tools > Device Manager
   - Create a new virtual device
3. The app will be built, installed, and launched automatically

## How to Play

Here's a step-by-step guide on how a typical game session flows:

1.  **Main Menu**:
    - Choose either **Host Game** to create a new game lobby or **Join Game** to enter an existing one using a Game PIN.
    - Enter your desired player name.

2.  **Game Lobby** (`LobbyScreen`):

    - **If Hosting**: You'll see the unique **Game PIN**. Share this PIN with friends who want to join.
    - **If Joining**: Enter the Game PIN provided by the host.
    - The lobby shows a list of players who have joined.
    - The "Start Game" button only becomes active when at least **two** players (including the host) are in the lobby.
    - Anyone clicks "Start Game" to begin.

3.  **Drawing Phase** (`DrawingScreen`):

    - Each player is secretly assigned a unique word to draw.
    - You have a limited time (60 seconds) to draw your word on the canvas.
    - Use the drawing tools: select colors, adjust brush size (using the edit panel), undo strokes, or use the eraser.
    - Click **DONE** when you're finished, or the drawing will be submitted automatically when the timer runs out.

4.  **Waiting for Drawings** (`GuessingLobbyScreen` - `isGuessPhase = false`):

    - After submitting your drawing, you'll enter a waiting screen.
    - This screen shows the status of all players (DRAWING or DONE).
    - Wait here until _everyone_ has finished submitting their drawing.

5.  **Guessing Phase** (`DrawingViewerScreen`):

    - Once all drawings are submitted, the guessing phase begins.
    - You will see the drawings made by other players one at a time.
    - For each drawing, you have a limited time (30 seconds) to guess the word, but you only have one guess per image, so choose wisely!
    - Type your guess into the text field and click **Guess**.
    - After guessing (or time runs out), you'll see if you were **CORRECT** or **WRONG** , along with the points awarded.
    - Points are awarded based on correctness and speed (see `PointsController`)
    - There's a short pause before the next drawing is shown.

6.  **Waiting for Guesses** (`GuessingLobbyScreen` - `isGuessPhase = true`):

    - After you've guessed all the drawings, you'll enter another waiting screen.
    - This screen shows the status of all players (GUESSING or DONE).
    - Wait here until _everyone_ has finished guessing all drawings.

7.  **Leaderboard** (`LeaderboardScreen`):
    - Once all players have finished guessing, the final leaderboard is displayed.
    - It shows the total scores for all players, sorted from highest to lowest.
    - Your name is highlighted with an arrow (➤).
    - The **Host** can click the **Back** button to return to the Main Menu. **Important**: Clicking Back on the Leaderboard screen will also **delete** the game session data (scores, drawings) from Firebase.

Enjoy playing Draw & Guess!

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs. Is not supported for this project.
- `android`: Android mobile platform. Needs Android SDK.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.

