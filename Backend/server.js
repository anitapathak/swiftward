const dns = require('dns');
dns.setServers(['8.8.8.8', '8.8.4.4']); // Forces use of Google DNS

const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors'); 
const authRoutes = require('./routes/authRoutes'); // Direct route mapping

const app = express();

// Request logging middleware for easier Android debugging
app.use((req, res, next) => {
    console.log(`📡 [LOG] Incoming Request: ${req.method} ${req.url}`);
    next();
});

// 1. MIDDLEWARE
app.use(express.json());
app.use(cors()); 

// 2. DATABASE CONNECTION
const dbURI = "mongodb+srv://anitapathak:anita123@cluster0.fywbnga.mongodb.net/SwiftWardDB?retryWrites=true&w=majority";

mongoose.connect(dbURI)
  .then(() => console.log("✅ Connected to MongoDB Atlas!"))
  .catch((err) => console.error("❌ MongoDB connection error:", err));

// 3. ROUTES
// Your Android application routes will now consistently live at:
// Registration -> http://10.0.2.2:5000/api/register
// Verification -> http://10.0.2.2:5000/api/verify-otp
app.use('/api', authRoutes); 

// 4. SERVER START
const PORT = 5000;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Server running on port ${PORT}`);
    console.log(`📱 Android Emulator Target Address: http://10.0.2.2:${PORT}/api`);
});