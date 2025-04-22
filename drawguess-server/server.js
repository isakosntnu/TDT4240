// server.js
const express = require("express");
const http = require("http");
const cors = require("cors");
const { Server } = require("socket.io");

const app = express();
app.use(cors());

const server = http.createServer(app);
const io = new Server(server, {
    cors: {
        origin: "*", 
        methods: ["GET", "POST"]
    }
});

// --------------------------------------------------------------

io.on("connection", (socket) => {
    console.log("Bruker tilkoblet:", socket.id);

    socket.on("joinGame", (data) => {
        try {
            console.log("Mottatt data fra klient:", data);

            const gameId = data.gameId || (data.get && data.get("gameId"));
            const username = data.username || (data.get && data.get("username"));

            if (!gameId || !username) {
                console.error("Ugyldig joinGame-data:", data);
                return;
            }

            console.log(` ${username} joinet game ${gameId}`);
            socket.join(gameId);
            io.to(gameId).emit("userJoined", { username });

        } catch (err) {
            console.error("Feil i joinGame-handling:", err);
        }
    });

    socket.on("startGame", ({ gameId }) => {
        console.log(`🚀 Starter spillet i rommet: ${gameId}`);
        io.to(gameId).emit("gameStarted"); 
    });

    socket.on("draw", ({ gameId, drawingData }) => {
        socket.to(gameId).emit("draw", drawingData);
    });

    socket.on("guess", ({ gameId, guess }) => {
        io.to(gameId).emit("guessMade", { guess });
    });

    socket.on("disconnect", () => {
        console.log("Bruker frakoblet:", socket.id);
    });
});

// --------------------------------------------------------------

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`Server kjører på port ${PORT}`);
});