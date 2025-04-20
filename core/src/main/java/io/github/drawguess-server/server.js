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
        origin: "*", // Endre til spesifikk URL i prod
        methods: ["GET", "POST"]
    }
});

io.on("connection", (socket) => {
    console.log("✅ Bruker tilkoblet:", socket.id);

    socket.on("joinGame", ({ gameId, username }) => {
        socket.join(gameId);
        io.to(gameId).emit("userJoined", { username });
    });

    socket.on("draw", ({ gameId, drawingData }) => {
        socket.to(gameId).emit("draw", drawingData);
    });

    socket.on("guess", ({ gameId, guess }) => {
        io.to(gameId).emit("guessMade", { guess });
    });

    socket.on("disconnect", () => {
        console.log("❌ Bruker frakoblet:", socket.id);
    });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`🎉 Server kjører på port ${PORT}`);
});
