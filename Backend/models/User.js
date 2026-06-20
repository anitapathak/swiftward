const mongoose = require('mongoose');
const userSchema = new mongoose.Schema({
    fullName: { 
        type: String, 
        required: true,
        trim: true 
    },
    phone: { 
        type: String, 
        required: true, 
        unique: true,
        trim: true 
    },
    email: { 
        type: String, 
        required: true, 
        unique: true,
        trim: true,
        lowercase: true 
    }, 
    password: { 
        type: String, 
        required: true 
    },
    isVerified: { 
        type: Boolean, 
        default: false 
    },
    otp: { 
        type: String 
    }, 
    otpExpires: { 
        type: Date 
    }
}, { timestamps: true });
module.exports = mongoose.model('User', userSchema);const express = require('express');

const jwt = require('jsonwebtoken');

const app = express();
app.use(express.json());

// Replace your old connection string with the new one
const mongoURI = "mongodb+srv://anitapathak:anita123@cluster0.mongodb.net/SwiftWard?retryWrites=true&w=majority";



// REGISTER ROUTE
app.post('/register', async (req, res) => {
    try {
        const { fullName, phone, password } = req.body;
        const existingUser = await User.findOne({ phone });
        if (existingUser) return res.status(400).json({ message: "User already exists" });

        const user = new User({ fullName, phone, password });
        await user.save();
        res.status(201).json({ success: true, message: "Registered! Now please login." });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// LOGIN ROUTE
app.post('/login', async (req, res) => {
    const { phone, password } = req.body;
    const user = await User.findOne({ phone, password });
    
    if (user) {
        // Generate a token so the user stays logged in
        const token = jwt.sign({ id: user._id }, "YOUR_SECRET_KEY", { expiresIn: '30d' });
        res.json({ success: true, token, user });
    } else {
        res.status(401).json({ message: "Invalid credentials" });
    }
});

app.listen(5000, '0.0.0.0', () => console.log("Server running on port 5000"));