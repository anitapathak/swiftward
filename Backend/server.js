require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const authRoutes = require('./routes/authRoutes');

const app = express();
app.use(cors());
app.use(express.json());

mongoose.connect(process.env.MONGO_URI || 'your_mongodb_uri_here')
    .then(() => console.log('✅ MongoDB connected'))
    .catch(err => console.error('❌ MongoDB error:', err.message));

app.use('/api', authRoutes);

// ── Khalti payment callback ────────────────────────────────────────────────────
// Khalti redirects here after user pays:
//   GET /payment/callback?pidx=xxx&status=Completed&purchase_order_id=SW-xxx
//
// STRATEGY: Instead of trying deep-links (unreliable on emulator),
// this page calls /api/khalti/verify itself, then shows a success screen
// with a big "Return to App" button. The app polls for payment status
// OR the user manually taps "I've paid — Verify" inside the app.
app.get('/payment/callback', async (req, res) => {
    const { pidx = '', status = '', purchase_order_id = '' } = req.query;

    console.log(`\n📲 Khalti callback received:`);
    console.log(`   pidx      : ${pidx}`);
    console.log(`   status    : ${status}`);
    console.log(`   booking   : ${purchase_order_id}`);

    if (status !== 'Completed') {
        return res.send(`<!DOCTYPE html>
<html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Payment Cancelled</title>
<style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:Arial,sans-serif;background:#FFF5F5;display:flex;align-items:center;justify-content:center;min-height:100vh}.card{background:white;border-radius:16px;padding:40px 32px;text-align:center;max-width:360px;width:90%;box-shadow:0 4px 24px rgba(0,0,0,.08)}.icon{font-size:64px;margin-bottom:16px}h1{color:#DC2626;margin-bottom:12px}p{color:#555;margin-bottom:24px}.btn{display:block;background:#1A3668;color:white;padding:14px;border-radius:10px;text-decoration:none;font-weight:bold}</style>
</head><body><div class="card">
<div class="icon">❌</div><h1>Payment Cancelled</h1>
<p>Your payment was not completed.</p>
<a href="#" onclick="window.close()" class="btn">Close & Return to App</a>
</div></body></html>`);
    }

    // Auto-verify payment on the server side
    let verifyData = null;
    try {
        const axios = require('axios');
        const vResp = await axios.post(
            'https://a.khalti.com/api/v2/epayment/lookup/',
            { pidx },
            { headers: { 'Authorization': 'Key 45605245d2644eb3b8a710a8579de3b8', 'Content-Type': 'application/json' } }
        );
        verifyData = vResp.data;
        console.log(`✅ Auto-verified on callback: status=${verifyData.status} txn=${verifyData.transaction_id}`);
    } catch (e) {
        console.error('❌ Auto-verify failed on callback:', e.response?.data || e.message);
    }

    const txnId = verifyData?.transaction_id || pidx;
    const amountRs = verifyData?.total_amount ? verifyData.total_amount / 100 : 300;

    // Log success to terminal
    console.log(`
╔══════════════════════════════════════════════════════════╗
║           ✅  SWIFTWARD PAYMENT SUCCESSFUL               ║
╠══════════════════════════════════════════════════════════╣
║  Booking  : ${purchase_order_id.padEnd(44)}║
║  Amount   : Rs ${amountRs.toString().padEnd(42)}║
║  Txn ID   : ${txnId.toString().slice(0,44).padEnd(44)}║
╚══════════════════════════════════════════════════════════╝`);

    // Return a success page. The app polls /api/khalti/poll/:pidx every 3 seconds
    // OR user taps "I've paid — Verify" button inside the app.
    res.send(`<!DOCTYPE html>
<html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>SwiftWard — Payment Successful</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:Arial,sans-serif;background:#F0FFF4;display:flex;align-items:center;justify-content:center;min-height:100vh}
.card{background:white;border-radius:16px;padding:36px 28px;text-align:center;max-width:380px;width:92%;box-shadow:0 4px 24px rgba(0,0,0,.08)}
.icon{font-size:72px;margin-bottom:16px}
h1{color:#166534;font-size:22px;margin-bottom:8px}
.sub{color:#555;font-size:15px;line-height:1.6;margin-bottom:24px}
.info{background:#F0FFF4;border:1px solid #BBF7D0;border-radius:10px;padding:16px;margin-bottom:20px;text-align:left}
.info p{font-size:14px;color:#166534;margin:4px 0}
.info .label{color:#888;font-size:12px}
.btn{display:block;background:#1A3668;color:white;padding:16px;border-radius:12px;text-decoration:none;font-size:16px;font-weight:bold;margin-bottom:10px}
.btn2{display:block;background:#166534;color:white;padding:16px;border-radius:12px;text-decoration:none;font-size:16px;font-weight:bold}
.note{font-size:12px;color:#999;margin-top:16px}
</style>
</head>
<body><div class="card">
  <div class="icon">✅</div>
  <h1>Payment Successful!</h1>
  <p class="sub">Your bed/ward has been reserved at SwiftWard.</p>
  <div class="info">
    <p><span class="label">BOOKING ID</span><br><strong>${purchase_order_id}</strong></p>
    <p style="margin-top:10px"><span class="label">TRANSACTION ID</span><br><strong>${txnId}</strong></p>
    <p style="margin-top:10px"><span class="label">AMOUNT PAID</span><br><strong>Rs ${amountRs}</strong></p>
  </div>
  <p class="note">✅ Your payment is confirmed! Tap the button below or switch back to the SwiftWard app.</p>
  <a href="swiftward://home" class="btn2" style="margin-top:16px;display:block;background:#166534;color:white;padding:16px;border-radius:12px;text-decoration:none;font-size:16px;font-weight:bold;text-align:center;">
    🏥 Return to SwiftWard App
  </a>
</div></body></html>`);
});

// ── Poll endpoint — app calls this to check if pidx is verified ────────────
app.get('/api/khalti/poll/:pidx', async (req, res) => {
    try {
        const axios = require('axios');
        const response = await axios.post(
            'https://a.khalti.com/api/v2/epayment/lookup/',
            { pidx: req.params.pidx },
            { headers: { 'Authorization': 'Key 45605245d2644eb3b8a710a8579de3b8', 'Content-Type': 'application/json' } }
        );
        const pd = response.data;
        res.json({ success: true, status: pd.status, transaction_id: pd.transaction_id, isCompleted: pd.status === 'Completed' });
    } catch (e) {
        res.status(500).json({ success: false, message: e.response?.data?.detail || e.message });
    }
});

app.get('/health', (req, res) => res.json({ status: 'ok', service: 'SwiftWard API' }));

const PORT = process.env.PORT || 5001;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 SwiftWard backend running on port ${PORT}`);
    console.log(`📌 Run: ngrok http 5001  → copy the https URL → set NGROK_URL in .env`);
});