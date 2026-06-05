const User = require('../models/User');
const bcrypt = require('bcryptjs');

// 1. REGISTER: Create user and print local OTP immediately
exports.register = async (req, res) => {
    try {
        // Destructured using 'name' to perfectly match your Android app data class payload
        const { name, phone, password } = req.body;

        // Basic Validation
        if (!phone || phone.length < 10) {
            return res.status(400).json({ message: "Invalid phone number" });
        }

        let user = await User.findOne({ phone });
        if (user) return res.status(400).json({ message: "Phone number already registered" });

        // Security & Local OTP Generation (6-digit)
        const generatedOtp = Math.floor(100000 + Math.random() * 900000).toString();
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // Build unverified user object
        user = new User({
            fullName: name, // Maps your Kotlin 'name' property to MongoDB schema's 'fullName'
            phone,
            password: hashedPassword,
            otp: generatedOtp,
            otpExpires: Date.now() + 600000, // Valid for 10 minutes
            isVerified: false
        });

        await user.save();

        // 🚀 PRINT DIRECTLY TO TERMINAL (Zero Network Latency/No Blocks)
        console.log("\n=========================================");
        console.log(`📱 NEW REGISTRATION REQUEST`);
        console.log(`👤 User Name : ${name}`);
        console.log(`📞 Phone No  : ${phone}`);
        console.log(`🔑 ENTER THIS OTP CODE IN ANDROID: ${generatedOtp}`);
        console.log("=========================================\n");

        // Send lightning-fast response back to Android Studio emulator
        res.status(200).json({ message: "OTP generated successfully" });
    } catch (err) {
        console.error("Registration error:", err);
        res.status(500).json({ message: "Registration failed on server" });
    }
};

// 2. VERIFY OTP: Check the 6-digit input from the Android Screen
exports.verifyOtp = async (req, res) => {
    try {
        const { phone, otp } = req.body;
        const user = await User.findOne({ phone });

        if (!user) return res.status(404).json({ message: "User not found" });

        // OTP Validation logic
        if (user.otp !== otp) {
            return res.status(400).json({ message: "Invalid OTP code" });
        }

        if (user.otpExpires < Date.now()) {
            return res.status(400).json({ message: "OTP has expired. Please resend." });
        }

        // Complete the registration workflow
        user.isVerified = true;
        user.otp = undefined; 
        user.otpExpires = undefined;
        await user.save();

        console.log(`\n✅ Account associated with ${phone} successfully verified!\n`);

        res.status(200).json({ 
            success: true, 
            message: "Account verified successfully!" 
        });
    } catch (err) {
        console.error("Verification error:", err);
        res.status(500).json({ message: "Verification failed" });
    }
};

// 3. RESEND OTP: Regenerate code locally and reprint
exports.resendOtp = async (req, res) => {
    try {
        const { phone } = req.body;
        const user = await User.findOne({ phone });

        if (!user) return res.status(404).json({ message: "User not found" });

        const newOtp = Math.floor(100000 + Math.random() * 900000).toString();

        // Save new validation states
        user.otp = newOtp;
        user.otpExpires = Date.now() + 600000;
        await user.save();

        // Reprint the code straight to your VS Code panel
        console.log("\n🔄 =========================================");
        console.log(`📱 RESEND OTP TRIGGERED FOR: ${phone}`);
        console.log(`🔑 NEW LOCAL OTP CODE: ${newOtp}`);
        console.log("=========================================\n");

        res.status(200).json({ message: "A new OTP has been generated!" });
    } catch (err) {
        console.error("Resend error:", err);
        res.status(500).json({ message: "Failed to resend OTP" });
    }
};

// 4. LOGIN: Normal credential evaluation
exports.login = async (req, res) => {
    try {
        const { phone, password } = req.body;
        const user = await User.findOne({ phone });

        if (!user) {
            return res.status(400).json({ message: "Incorrect number or password" });
        }

        if (!user.isVerified) {
            return res.status(403).json({ message: "Please verify your phone number first" });
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) return res.status(400).json({ message: "Incorrect number or password" });

        res.status(200).json({ 
            message: "Login successful", 
            userId: user._id,
            fullName: user.fullName 
        });
    } catch (err) {
        console.error("Login error:", err);
        res.status(500).json({ message: "Login failed" });
    }
};
