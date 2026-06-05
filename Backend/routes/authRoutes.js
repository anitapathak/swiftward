const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

// --- Registration & Verification Flow ---

// 1. Initial registration and first OTP send
router.post('/register', authController.register);

// 2. Verification of the 6-digit code from the Android screen
router.post('/verify-otp', authController.verifyOtp);

// 3. Resend logic: Triggered when your Android timer hits 0:00
router.post('/resend-otp', authController.resendOtp);

// --- Standard Auth ---

// 4. Login after the phone has been verified
router.post('/login', authController.login);

module.exports = router;