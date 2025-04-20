const express = require('express');
const http = require('http');
const socketIO = require('socket.io');

const app = express();
const server = http.createServer(app);

// ⛔️ Ikke send CORS-opsjoner her!
const io = socketIO.listen(server); // <- for socket.io v2.4.1

server.listen(3000, '0.0.0.0', () => {
  console.log("✅ Server running on port 3000");
});

io.on('connection', (socket) => {
  console.log(`🟢 Client connected: ${socket.id}`);

  socket.on('join-lobby', (data) => {
    try {
      console.log(`➡️ join-lobby:`, data);
      socket.join(data.gameId);
    } catch (err) {
      console.error("⚠️ Failed to join lobby:", err.message);
    }
  });
  

  socket.on('disconnect', (reason) => {
    console.log(`🔴 Client disconnected: ${socket.id} (${reason})`);
  });
});
