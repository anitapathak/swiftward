const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

// Auth flow
router.post('/register', authController.register);
router.post('/verify-otp',authController.verifyOtp);
router.post('/resend-otp', authController.resendOtp);
router.post('/login',authController.login);

// Khalti payment
router.post('/khalti/initiate', authController.initiateKhaltiPayment);
router.post('/khalti/verify',authController.verifyKhaltiPayment);

module.exports = router;