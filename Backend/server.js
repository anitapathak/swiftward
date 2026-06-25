require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const authRoutes = require('./routes/authRoutes');

const app = express();
app.use(cors());
app.use(express.json());

// MongoDB Atlas
const MONGO_URI = process.env.MONGO_URI || 'mongodb+srv://anitapathak:anita123@cluster0.fywbnga.mongodb.net/SwiftWardDB?retryWrites=true&w=majority';
mongoose.connect(MONGO_URI)
    .then(() => console.log('✅ MongoDB connected'))
    .catch(err => console.error('❌ MongoDB error:', err.message));

// Routes
app.use('/api', authRoutes);

// ── Khalti payment callback ─────────────────────────────────────────────────
// Khalti redirects here after payment:
//   GET /payment/callback?pidx=xxx&status=Completed&purchase_order_id=SW-xxx
// We return a minimal HTML page that instantly redirects to the Android deep-link.
// Android intercepts swiftward:// and opens the app.
app.get('/payment/callback', (req, res) => {
    const { pidx = '', status = '', purchase_order_id = '' } = req.query;

    const deepLink = `swiftward://payment/callback?pidx=${encodeURIComponent(pidx)}&status=${encodeURIComponent(status)}&purchase_order_id=${encodeURIComponent(purchase_order_id)}`;

    if (status === 'Completed') {
        res.send(`<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SwiftWard — Payment Successful</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: Arial, sans-serif; background: #F0FFF4; display: flex; align-items: center; justify-content: center; min-height: 100vh; }
    .card { background: white; border-radius: 16px; padding: 40px 32px; text-align: center; max-width: 360px; box-shadow: 0 4px 24px rgba(0,0,0,0.08); }
    .icon { font-size: 64px; margin-bottom: 16px; }
    h1 { color: #166534; font-size: 24px; margin-bottom: 8px; }
    p  { color: #555; font-size: 15px; line-height: 1.5; margin-bottom: 24px; }
    .btn { display: block; background: #1A3668; color: white; padding: 14px 24px; border-radius: 10px; text-decoration: none; font-size: 16px; font-weight: bold; margin-bottom: 12px; }
    .small { font-size: 12px; color: #999; }
  </style>
  <script>
    // Auto-redirect to app after 1 second
    setTimeout(function() {
      window.location.href = "${deepLink}";
    }, 1000);
  </script>
</head>
<body>
  <div class="card">
    <div class="icon">✅</div>
    <h1>Payment Successful!</h1>
    <p>Your bed/ward has been reserved.<br>Returning to SwiftWard app…</p>
    <a href="${deepLink}" class="btn">Open SwiftWard App</a>
    <p class="small">Booking ID: ${purchase_order_id}<br>Transaction: ${pidx}</p>
  </div>
</body>
</html>`);
    } else {
        // Payment cancelled or failed
        const cancelLink = `swiftward://payment/callback?pidx=${encodeURIComponent(pidx)}&status=cancelled&purchase_order_id=${encodeURIComponent(purchase_order_id)}`;
        res.send(`<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SwiftWard — Payment Cancelled</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: Arial, sans-serif; background: #FFF5F5; display: flex; align-items: center; justify-content: center; min-height: 100vh; }
    .card { background: white; border-radius: 16px; padding: 40px 32px; text-align: center; max-width: 360px; box-shadow: 0 4px 24px rgba(0,0,0,0.08); }
    .icon { font-size: 64px; margin-bottom: 16px; }
    h1 { color: #DC2626; font-size: 24px; margin-bottom: 8px; }
    p  { color: #555; font-size: 15px; margin-bottom: 24px; }
    .btn { display: block; background: #1A3668; color: white; padding: 14px 24px; border-radius: 10px; text-decoration: none; font-size: 16px; font-weight: bold; }
  </style>
  <script>
    setTimeout(function() { window.location.href = "${cancelLink}"; }, 1500);
  </script>
</head>
<body>
  <div class="card">
    <div class="icon">❌</div>
    <h1>Payment Cancelled</h1>
    <p>Returning to SwiftWard app…</p>
    <a href="${cancelLink}" class="btn">Back to App</a>
  </div>
</body>
</html>`);
    }
});

// Health check
app.get('/health', (req, res) => res.json({ status: 'ok', service: 'SwiftWard API' }));

const PORT = process.env.PORT || 5001;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 SwiftWard backend running on port ${PORT}`);
    console.log(`📌 Run: ngrok http 5001  → copy the https URL → set NGROK_URL in authController.js`);
});