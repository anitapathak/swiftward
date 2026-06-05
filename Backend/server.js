const dns = require('dns');
dns.setServers(['8.8.8.8', '8.8.4.4']); // Forces use of Google DNS
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors'); // Recommended for Android-Backend communication
const authRoutes = require('./routes/authRoutes'); // Connect your updated routes

const app = express();
app.use((req, res, next) => {
    console.log(`📡 [LOG] Incoming Request: ${req.method} ${req.url}`);
    console.log(`📦 [LOG] Headers:`, req.headers);
    next();
});

// 1. MIDDLEWARE
app.use(express.json());
app.use(cors()); // Allows your Android app to connect without being blocked

// 2. DATABASE CONNECTION

//const dbURI = 'mongodb+srv://anitapathak:anita123d@cluster0.abcde.mongodb.net/SwiftWardDB"';
const { MongoClient, ServerApiVersion } = require('mongodb');
//const dbURI = "mongodb+srv://anitapathak:<anita123>@cluster0.fywbnga.mongodb.net/?appName=Cluster0";
const dbURI = "mongodb+srv://anitapathak:anita123@cluster0.fywbnga.mongodb.net/SwiftWardDB?retryWrites=true&w=majority";
mongoose.connect(dbURI)
  .then(() => console.log("✅ Connected to MongoDB Atlas!"))
  .catch((err) => console.error("❌ MongoDB connection error:", err));

// 3. ROUTES
// This links all the logic (Register, Login, Verify-OTP, Resend-OTP) 
// to the /api prefix
app.use('/api', authRoutes); 

// 4. SERVER START
const PORT = 5000;
// Using '0.0.0.0' is correct—it allows your phone/emulator to find the server
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Server running on http://localhost:${PORT}`);
    console.log(`📱 For Android testing, use your local IP or 10.0.2.2:${PORT}`);
});