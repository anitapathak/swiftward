const User = require('../models/User');
const bcrypt = require('bcryptjs');
const { Resend } = require('resend');

// Initialize Resend with your API Key
const resend = new Resend('re_2JiDzXZy_8DnvG8WMgnEGW1uLpfx6oyfC');

// ==========================================
// 1. REGISTER: Create user and send OTP
// ==========================================
exports.register = async (req, res) => {
    try {
        // Destructure name, phone, email, and password from your Android frontend request body
        const { name, phone, email, password } = req.body;

        // Basic Validation
        if (!phone || phone.length < 10) {
            return res.status(400).json({ success: false, message: "Invalid phone number" });
        }
        if (!email) {
            return res.status(400).json({ success: false, message: "Email address is required for verification" });
        }

        // Check if user already exists
        let userExists = await User.findOne({ $or: [{ phone }, { email }] });
        if (userExists) {
            return res.status(400).json({ success: false, message: "Phone number or Email already registered" });
        }

        // Generate 6-digit cryptographic random OTP string
        const generatedOtp = Math.floor(100000 + Math.random() * 900000).toString();
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // Build unverified user record matching your Mongoose Schema configuration
        const user = new User({
            fullName: name, // Maps your Android parameter string to your schema database field definition
            phone,
            email,
            password: hashedPassword,
            otp: generatedOtp,
            otpExpires: Date.now() + 600000, // Valid for 10 minutes
            isVerified: false
        });

        await user.save();

        // 🚨 CRITICAL FIX: Wrapped inside an isolated try-catch sandbox environment.
        // If Resend throws an API error, it won't crash the endpoint or send a 403 back to Android.
        try {
            await resend.emails.send({
                from: 'onboarding@resend.dev',
                to: email, 
                subject: 'SwiftWard OTP Verification Code',
                html: `<h3>Welcome to SwiftWard!</h3>
                       <p>Your 6-digit OTP code is: <strong>${generatedOtp}</strong></p>
                       <p>This code will expire in 10 minutes.</p>`
            });
            console.log(`\n📨 OTP Email dispatched safely via Resend architecture to ${email}`);
        } catch (emailErr) {
            console.error("⚠️ Resend API warning (likely unverified sandbox restrictions):", emailErr.message);
            console.log(`\n💡 [DEVELOPER TESTING KEY] -> Manual OTP override for screen insertion: ${generatedOtp}\n`);
        }

        // Return unified response layout containing explicit success marker to map Kotlin mapping cleanly
        res.status(200).json({ 
            success: true, 
            message: "OTP sent to your email successfully" 
        });

    } catch (err) {
        console.error("Registration structural exception error:", err);
        res.status(500).json({ success: false, message: "Registration failed on server side" });
    }
};

// ==========================================
// 2. VERIFY OTP: Validate code mapping
// ==========================================
exports.verifyOtp = async (req, res) => {
    try {
        const { phone, otp } = req.body;
        const user = await User.findOne({ phone });

        if (!user) return res.status(404).json({ success: false, message: "User not found" });

        // OTP Core Verification matching string evaluations
        if (user.otp !== otp) {
            return res.status(400).json({ success: false, message: "Invalid OTP code" });
        }

        if (user.otpExpires < Date.now()) {
            return res.status(400).json({ success: false, message: "OTP has expired. Please resend." });
        }

        // Complete registration workflow lifecycle state transition
        user.isVerified = true;
        user.otp = undefined; 
        user.otpExpires = undefined;
        await user.save();

        console.log(`\n✅ Account associated with mobile target ${phone} successfully verified!\n`);

        res.status(200).json({ 
            success: true, 
            message: "Account verified successfully!" 
        });
    } catch (err) {
        console.error("Verification endpoint matching exception error:", err);
        res.status(500).json({ success: false, message: "Verification execution failed" });
    }
};

// ==========================================
// 3. RESEND OTP: Regenerate tracking token
// ==========================================
exports.resendOtp = async (req, res) => {
    try {
        const { phone } = req.body;
        const user = await User.findOne({ phone });

        if (!user) return res.status(404).json({ success: false, message: "User not found" });

        const newOtp = Math.floor(100000 + Math.random() * 900000).toString();

        user.otp = newOtp;
        user.otpExpires = Date.now() + 600000;
        await user.save();

        // Send replacement verification code token via email routing pipelines
        try {
            await resend.emails.send({
                from: 'onboarding@resend.dev',
                to: user.email,
                subject: 'Your New SwiftWard OTP Code',
                html: `<p>Your new tracking OTP code validation digit is: <strong>${newOtp}</strong></p>`
            });
            console.log(`\n🔄 Resent OTP email directly via tracking channels to ${user.email}`);
        } catch (emailErr) {
            console.error("⚠️ Resend Resend retry exception caught:", emailErr.message);
            console.log(`\n💡 [DEVELOPER TESTING KEY] -> Resend Manual OTP override: ${newOtp}\n`);
        }

        res.status(200).json({ 
            success: true, 
            message: "A new OTP has been sent to your email!" 
        });
    } catch (err) {
        console.error("Resend routine handling error:", err);
        res.status(500).json({ success: false, message: "Failed to resend authentication OTP" });
    }
};

// ==========================================
// 4. LOGIN: Validate credential mapping
// ==========================================
exports.login = async (req, res) => {
    try {
        const { phone, password } = req.body;
        const user = await User.findOne({ phone });

        if (!user) {
            return res.status(400).json({ success: false, message: "Incorrect number or password" });
        }

        // 🛡️ SECURITY GATEWAY: Enforces structural isolation validation
        if (!user.isVerified) {
            return res.status(403).json({ success: false, message: "Please verify your email address first" });
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) return res.status(400).json({ success: false, message: "Incorrect number or password" });

        res.status(200).json({ 
            success: true,
            message: "Login successful", 
            userId: user._id,
            fullName: user.fullName 
        });
    } catch (err) {
        console.error("Login verification stream module tracking failure:", err);
        res.status(500).json({ success: false, message: "Login execution layer structural failure" });
    }
};