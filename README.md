# Draw & Guess Game

This is a simple multiplayer drawing and guessing game built with LibGDX and Firebase.

## How to Play

Here's a step-by-step guide on how a typical game session flows:

1.  **Main Menu**:

    - Enter your desired player name.
    - Choose either **Host Game** to create a new game lobby or **Join Game** to enter an existing one using a Game PIN.

2.  **Game Lobby** (`LobbyScreen`):

    - **If Hosting**: You'll see the unique **Game PIN**. Share this PIN with friends who want to join.
    - **If Joining**: Enter the Game PIN provided by the host.
    - The lobby shows a list of players who have joined.
    - The **Host** sees a "Start Game" button. This button only becomes active when at least **two** players (including the host) are in the lobby.
    - The Host clicks "Start Game" to begin.

3.  **Drawing Phase** (`DrawingScreen`):

    - Each player is secretly assigned a unique word to draw.
    - You have a limited time (e.g., 30 seconds) to draw your word on the canvas.
    - Use the drawing tools: select colors, adjust brush size (using the edit panel), undo strokes, or use the eraser.
    - Click **DONE** when you're finished, or the drawing will be submitted automatically when the timer runs out.

4.  **Waiting for Drawings** (`GuessingLobbyScreen` - `isGuessPhase = false`):

    - After submitting your drawing, you'll enter a waiting screen.
    - This screen shows the status of all players (DRAWING or DONE).
    - Wait here until _everyone_ has finished submitting their drawing.

5.  **Guessing Phase** (`DrawingViewerScreen`):

    - Once all drawings are submitted, the guessing phase begins.
    - You will see the drawings made by other players one at a time.
    - For each drawing, you have a limited time (e.g., 30 seconds) to guess the word.
    - Type your guess into the text field and click **Guess**.
    - After guessing (or time runs out), you'll see if you were **CORRECT** (green text) or **WRONG** (red text), along with the points awarded.
    - Points are awarded based on correctness and speed (10 base points + 2 points per second remaining for correct guesses).
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
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.
- `android`: Android mobile platform. Needs Android SDK.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `android:lint`: performs Android project validation.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
