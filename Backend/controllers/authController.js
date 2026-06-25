const User = require('../models/User');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { Resend } = require('resend');

const resend = new Resend('re_2JiDzXZy_8DnvG8WMgnEGW1uLpfx6oyfC');
const JWT_SECRET = process.env.JWT_SECRET || 'swiftward_dev_secret_change_me';

function buildAuthData(user) {
    const token = jwt.sign({ id: user._id }, JWT_SECRET, { expiresIn: '30d' });
    return { token, user: { id: user._id, name: user.fullName, phone: user.phone, email: user.email } };
}

// ── 1. REGISTER ───────────────────────────────────────────────────────────────
exports.register = async (req, res) => {
    try {
        const { name, phone, email, password } = req.body;
        if (!phone || phone.length < 10)
            return res.status(400).json({ success: false, message: "Invalid phone number" });
        if (!email)
            return res.status(400).json({ success: false, message: "Email is required" });

        let exists = await User.findOne({ $or: [{ phone }, { email }] });
        if (exists)
            return res.status(400).json({ success: false, message: "Phone or email already registered" });

        const otp = Math.floor(100000 + Math.random() * 900000).toString();
        const hashed = await bcrypt.hash(password, await bcrypt.genSalt(10));

        await new User({
            fullName: name, phone, email, password: hashed,
            otp, otpExpires: Date.now() + 600000, isVerified: false
        }).save();

        console.log(`\n💡 [OTP] ${phone} → ${otp}\n`);

        try {
            await resend.emails.send({
                from: 'onboarding@resend.dev', to: email,
                subject: 'SwiftWard OTP Verification',
                html: `<div style="font-family:Arial;max-width:480px;margin:0 auto">
                  <h2 style="color:#1A3668">Welcome to SwiftWard 🏥</h2>
                  <p>Hello <strong>${name}</strong>, your OTP is:</p>
                  <div style="background:#EFF6FF;border-radius:8px;padding:20px;text-align:center;margin:20px 0">
                    <span style="font-size:36px;font-weight:bold;letter-spacing:8px;color:#1A3668">${otp}</span>
                  </div>
                  <p style="color:#666">Expires in <strong>10 minutes</strong>.</p></div>`
            });
        } catch (e) { console.error("OTP email failed:", e.message); }

        res.status(200).json({ success: true, message: "OTP sent to your email" });
    } catch (err) {
        console.error("Register error:", err);
        res.status(500).json({ success: false, message: "Registration failed" });
    }
};

// ── 2. VERIFY OTP ─────────────────────────────────────────────────────────────
exports.verifyOtp = async (req, res) => {
    try {
        const { phone, otp } = req.body;
        const user = await User.findOne({ phone });
        if (!user)      return res.status(404).json({ success: false, message: "User not found" });
        if (user.otp !== otp) return res.status(400).json({ success: false, message: "Invalid OTP" });
        if (user.otpExpires < Date.now()) return res.status(400).json({ success: false, message: "OTP expired" });

        user.isVerified = true;
        user.otp = undefined;
        user.otpExpires = undefined;
        await user.save();

        try {
            await resend.emails.send({
                from: 'onboarding@resend.dev', to: user.email,
                subject: '🎉 SwiftWard — Registration Successful!',
                html: `<div style="font-family:Arial;max-width:480px;margin:0 auto">
                  <h2 style="color:#1A3668">Registration Successful! 🎉</h2>
                  <p>Hello <strong>${user.fullName}</strong>, your account is verified.</p>
                  <div style="background:#EFF6FF;border-radius:8px;padding:16px;margin:20px 0">
                    <p><strong>Phone:</strong> ${user.phone}</p>
                    <p><strong>Email:</strong> ${user.email}</p>
                  </div>
                  <p>Please log in with your phone and password to continue.</p></div>`
            });
        } catch (e) { console.error("Welcome email failed:", e.message); }

        res.status(200).json({ success: true, message: "Verified! Please log in to continue.", data: null });
    } catch (err) {
        res.status(500).json({ success: false, message: "Verification failed" });
    }
};

// ── 3. RESEND OTP ─────────────────────────────────────────────────────────────
exports.resendOtp = async (req, res) => {
    try {
        const { phone } = req.body;
        const user = await User.findOne({ phone });
        if (!user) return res.status(404).json({ success: false, message: "User not found" });

        const otp = Math.floor(100000 + Math.random() * 900000).toString();
        user.otp = otp;
        user.otpExpires = Date.now() + 600000;
        await user.save();
        console.log(`\n💡 [RESEND OTP] ${phone} → ${otp}\n`);

        try {
            await resend.emails.send({
                from: 'onboarding@resend.dev', to: user.email,
                subject: 'SwiftWard — New OTP Code',
                html: `<div style="font-family:Arial;max-width:480px"><h2 style="color:#1A3668">New OTP</h2>
                  <div style="background:#EFF6FF;border-radius:8px;padding:20px;text-align:center;margin:20px 0">
                    <span style="font-size:36px;font-weight:bold;letter-spacing:8px;color:#1A3668">${otp}</span>
                  </div><p style="color:#666">Expires in 10 minutes.</p></div>`
            });
        } catch (e) { console.error("Resend email failed:", e.message); }

        res.status(200).json({ success: true, message: "New OTP sent!" });
    } catch (err) {
        res.status(500).json({ success: false, message: "Failed to resend OTP" });
    }
};

// ── 4. LOGIN ──────────────────────────────────────────────────────────────────
exports.login = async (req, res) => {
    try {
        const { phone, password } = req.body;
        const user = await User.findOne({ phone });
        if (!user) return res.status(400).json({ success: false, message: "Incorrect phone or password" });
        if (!user.isVerified) return res.status(403).json({ success: false, message: "Please verify your email first" });
        if (!await bcrypt.compare(password, user.password))
            return res.status(400).json({ success: false, message: "Incorrect phone or password" });

        res.status(200).json({ success: true, message: "Login successful", data: buildAuthData(user) });
    } catch (err) {
        res.status(500).json({ success: false, message: "Login failed" });
    }
};

// ── 5. KHALTI INITIATE ────────────────────────────────────────────────────────
// FIX: return_url must be a proper deep-link back to the app, NOT the backend server.
// Khalti will redirect to this URL after payment with ?pidx=xxx&status=Completed
// We use a custom Android deep-link: swiftward://payment/callback
// The app intercepts it and calls /api/khalti/verify automatically.
exports.initiateKhaltiPayment = async (req, res) => {
    try {
        const { bookingId, amount, hospitalName, wardType, userName, userEmail } = req.body;
        if (!bookingId || !amount)
            return res.status(400).json({ success: false, message: "bookingId and amount are required" });

        const axios = require('axios');
        const response = await axios.post(
            'https://a.khalti.com/api/v2/epayment/initiate/',
            {
                return_url: (process.env.NGROK_URL || 'https://YOUR_NGROK_URL.ngrok-free.app') + '/payment/callback',
                website_url: "https://swiftward.com.np",
                amount: amount,
                purchase_order_id: bookingId,
                purchase_order_name: `SwiftWard — ${wardType} at ${hospitalName}`,
                customer_info: {
                    name: userName || "Patient",
                    email: userEmail || "patient@swiftward.com",
                    phone: "9800000001"
                }
            },
            {
                headers: {
                    'Authorization': 'Key 45605245d2644eb3b8a710a8579de3b8',
                    'Content-Type': 'application/json'
                }
            }
        );

        console.log(`✅ Khalti payment initiated: pidx=${response.data.pidx}`);
        res.status(200).json({
            success: true,
            data: {
                pidx: response.data.pidx,
                payment_url: response.data.payment_url,
                expires_at: response.data.expires_at
            }
        });
    } catch (err) {
        console.error("Khalti initiate error:", err.response?.data || err.message);
        res.status(500).json({ success: false, message: err.response?.data?.detail || "Failed to initiate payment" });
    }
};

// ── 6. KHALTI VERIFY ─────────────────────────────────────────────────────────
exports.verifyKhaltiPayment = async (req, res) => {
    try {
        const { pidx, bookingId, hospitalName, wardType, userEmail, userName } = req.body;
        if (!pidx) return res.status(400).json({ success: false, message: "pidx is required" });

        const axios = require('axios');
        const response = await axios.post(
            'https://a.khalti.com/api/v2/epayment/lookup/',
            { pidx },
            {
                headers: {
                    'Authorization': 'Key 45605245d2644eb3b8a710a8579de3b8',
                    'Content-Type': 'application/json'
                }
            }
        );

        const pd = response.data;
        const isCompleted = pd.status === 'Completed';
        console.log(`💳 Khalti verify: pidx=${pidx} status=${pd.status}`);

        if (isCompleted && userEmail) {
            try {
                await resend.emails.send({
                    from: 'onboarding@resend.dev', to: userEmail,
                    subject: '✅ SwiftWard — Payment Successful & Bed Reserved!',
                    html: `<div style="font-family:Arial;max-width:520px;margin:0 auto">
                      <div style="background:#1A3668;padding:24px;border-radius:8px 8px 0 0;text-align:center">
                        <h1 style="color:white;margin:0">🏥 SwiftWard</h1>
                      </div>
                      <div style="background:white;padding:24px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 8px 8px">
                        <div style="text-align:center;margin-bottom:24px">
                          <div style="font-size:48px">✅</div>
                          <h2 style="color:#166534">Payment Successful!</h2>
                          <p style="color:#666">Your bed/ward has been reserved.</p>
                        </div>
                        <table style="width:100%;border-collapse:collapse;background:#F9FAFB;border-radius:8px;padding:16px">
                          <tr><td style="padding:8px;color:#666">Patient</td><td style="padding:8px;font-weight:bold;text-align:right">${userName}</td></tr>
                          <tr><td style="padding:8px;color:#666">Booking ID</td><td style="padding:8px;font-weight:bold;color:#D97706;text-align:right">${bookingId}</td></tr>
                          <tr><td style="padding:8px;color:#666">Hospital</td><td style="padding:8px;font-weight:bold;text-align:right">${hospitalName}</td></tr>
                          <tr><td style="padding:8px;color:#666">Ward</td><td style="padding:8px;font-weight:bold;text-align:right">${wardType}</td></tr>
                          <tr><td style="padding:8px;color:#666">Amount</td><td style="padding:8px;font-weight:bold;color:#166534;text-align:right">Rs 300</td></tr>
                          <tr><td style="padding:8px;color:#666">Transaction ID</td><td style="padding:8px;font-size:12px;color:#6B7280;text-align:right">${pd.transaction_id || pidx}</td></tr>
                        </table>
                        <div style="background:#FEF3C7;border-radius:8px;padding:14px;margin-top:16px">
                          <p style="margin:0;color:#92400E">⚠️ Please proceed to <strong>${wardType}</strong> ward at <strong>${hospitalName}</strong> immediately. Show this booking ID to hospital staff.</p>
                        </div>
                      </div></div>`
                });
            } catch (e) { console.error("Confirmation email failed:", e.message); }
        }

        res.status(200).json({
            success: true,
            data: {
                status: pd.status, transaction_id: pd.transaction_id,
                pidx: pd.pidx, amount: pd.total_amount, isCompleted
            }
        });
    } catch (err) {
        console.error("Khalti verify error:", err.response?.data || err.message);
        res.status(500).json({ success: false, message: err.response?.data?.detail || "Failed to verify payment" });
    }
};