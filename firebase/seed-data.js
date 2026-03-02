/**
 * MGB Heights - Sample Data Seed Script
 * Run with: node firebase/seed-data.js
 *
 * Prerequisites:
 * - Firebase Admin SDK: npm install firebase-admin
 * - Service account key JSON file
 */

const admin = require('firebase-admin');

// Initialize with service account
const serviceAccount = require('./serviceAccountKey.json');
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    storageBucket: 'mgb-heights.appspot.com'
});

const db = admin.firestore();
const now = Date.now();

async function seedData() {
    console.log('🏗 Starting MGB Heights sample data seed...\n');

    // ============ USERS ============
    console.log('👥 Creating users...');
    const users = [
        {
            id: 'admin_001',
            phoneNumber: '9999900001',
            name: 'Rajesh Kumar',
            email: 'admin@mgbheights.com',
            role: 'ADMIN',
            flatNumber: 'A-101',
            towerBlock: 'Tower A',
            houseNumber: '101',
            isApproved: true,
            isBlocked: false,
            isProfileComplete: true,
            isOnboarded: true,
            tenantOf: '',
            createdAt: now,
            updatedAt: now
        },
        {
            id: 'resident_001',
            phoneNumber: '9999900002',
            name: 'Priya Sharma',
            email: 'priya@email.com',
            role: 'RESIDENT',
            flatNumber: 'A-201',
            towerBlock: 'Tower A',
            houseNumber: '201',
            isApproved: true,
            isBlocked: false,
            isProfileComplete: true,
            isOnboarded: true,
            tenantOf: '',
            createdAt: now,
            updatedAt: now
        },
        {
            id: 'resident_002',
            phoneNumber: '9999900003',
            name: 'Amit Patel',
            email: 'amit@email.com',
            role: 'RESIDENT',
            flatNumber: 'B-301',
            towerBlock: 'Tower B',
            houseNumber: '301',
            isApproved: true,
            isBlocked: false,
            isProfileComplete: true,
            isOnboarded: true,
            tenantOf: '',
            createdAt: now,
            updatedAt: now
        },
        {
            id: 'tenant_001',
            phoneNumber: '9999900004',
            name: 'Suresh Yadav',
            email: '',
            role: 'TENANT',
            flatNumber: 'A-201',
            towerBlock: 'Tower A',
            houseNumber: '201',
            isApproved: true,
            isBlocked: false,
            isProfileComplete: true,
            isOnboarded: true,
            tenantOf: 'resident_001',
            createdAt: now,
            updatedAt: now
        },
        {
            id: 'guard_001',
            phoneNumber: '9999900005',
            name: 'Ram Singh',
            email: '',
            role: 'SECURITY_GUARD',
            flatNumber: '',
            towerBlock: '',
            houseNumber: '',
            isApproved: true,
            isBlocked: false,
            isProfileComplete: true,
            isOnboarded: true,
            tenantOf: '',
            createdAt: now,
            updatedAt: now
        },
        {
            id: 'worker_001',
            phoneNumber: '9999900006',
            name: 'Mohan Lal',
            email: '',
            role: 'WORKER',
            flatNumber: '',
            towerBlock: '',
            houseNumber: '',
            isApproved: true,
            isBlocked: false,
            isProfileComplete: true,
            isOnboarded: true,
            tenantOf: '',
            createdAt: now,
            updatedAt: now
        }
    ];

    for (const user of users) {
        await db.collection('users').doc(user.id).set(user);
    }
    console.log(`  ✅ Created ${users.length} users`);

    // ============ FLATS ============
    console.log('🏠 Creating flats...');
    const flats = [
        { id: 'flat_a101', houseNumber: '101', flatNumber: 'A-101', towerBlock: 'Tower A', ownerId: 'admin_001', ownerName: 'Rajesh Kumar', ownerPhone: '9999900001', tenantId: '', tenantName: '', tenantPhone: '', hasTenant: false, assignedWorkers: [], createdAt: now, updatedAt: now },
        { id: 'flat_a201', houseNumber: '201', flatNumber: 'A-201', towerBlock: 'Tower A', ownerId: 'resident_001', ownerName: 'Priya Sharma', ownerPhone: '9999900002', tenantId: 'tenant_001', tenantName: 'Suresh Yadav', tenantPhone: '9999900004', hasTenant: true, assignedWorkers: ['worker_001'], createdAt: now, updatedAt: now },
        { id: 'flat_b301', houseNumber: '301', flatNumber: 'B-301', towerBlock: 'Tower B', ownerId: 'resident_002', ownerName: 'Amit Patel', ownerPhone: '9999900003', tenantId: '', tenantName: '', tenantPhone: '', hasTenant: false, assignedWorkers: [], createdAt: now, updatedAt: now },
    ];

    for (const flat of flats) {
        await db.collection('flats').doc(flat.id).set(flat);
    }
    console.log(`  ✅ Created ${flats.length} flats`);

    // ============ MAINTENANCE BILLS ============
    console.log('💰 Creating maintenance bills...');
    const dueDate = now + 15 * 24 * 60 * 60 * 1000; // 15 days from now
    const bills = [
        { id: 'bill_001', flatId: 'flat_a101', flatNumber: 'A-101', towerBlock: 'Tower A', residentId: 'admin_001', residentName: 'Rajesh Kumar', amount: 5000, lateFee: 0, totalAmount: 5000, month: '2026-02', year: 2026, dueDate: dueDate, status: 'PAID', description: 'Monthly maintenance', paymentId: 'pay_001', paidAt: now - 86400000, createdAt: now, updatedAt: now },
        { id: 'bill_002', flatId: 'flat_a201', flatNumber: 'A-201', towerBlock: 'Tower A', residentId: 'resident_001', residentName: 'Priya Sharma', amount: 5000, lateFee: 0, totalAmount: 5000, month: '2026-02', year: 2026, dueDate: dueDate, status: 'PENDING', description: 'Monthly maintenance', paymentId: '', paidAt: 0, createdAt: now, updatedAt: now },
        { id: 'bill_003', flatId: 'flat_b301', flatNumber: 'B-301', towerBlock: 'Tower B', residentId: 'resident_002', residentName: 'Amit Patel', amount: 5000, lateFee: 100, totalAmount: 5100, month: '2026-01', year: 2026, dueDate: now - 86400000, status: 'OVERDUE', description: 'Monthly maintenance', paymentId: '', paidAt: 0, createdAt: now - 30 * 86400000, updatedAt: now },
    ];

    for (const bill of bills) {
        await db.collection('maintenance_bills').doc(bill.id).set(bill);
    }
    console.log(`  ✅ Created ${bills.length} bills`);

    // ============ PAYMENTS ============
    console.log('💳 Creating payments...');
    const payments = [
        { id: 'pay_001', flatId: 'flat_a101', flatNumber: 'A-101', towerBlock: 'Tower A', userId: 'admin_001', userName: 'Rajesh Kumar', billId: 'bill_001', amount: 5000, type: 'MAINTENANCE', status: 'SUCCESS', razorpayOrderId: 'order_test_001', razorpayPaymentId: 'pay_test_001', razorpaySignature: '', receiptUrl: '', receiptNumber: 'REC-2026-0001', description: 'Feb 2026 Maintenance', isManualEntry: false, manualEntryBy: '', failureReason: '', createdAt: now - 86400000, updatedAt: now - 86400000 },
    ];

    for (const payment of payments) {
        await db.collection('payments').doc(payment.id).set(payment);
    }
    console.log(`  ✅ Created ${payments.length} payments`);

    // ============ NOTICES ============
    console.log('📢 Creating notices...');
    const notices = [
        { id: 'notice_001', title: 'Water Tank Cleaning - Feb 28', body: 'The overhead water tanks will be cleaned on February 28. Water supply will be disrupted from 10 AM to 2 PM. Please store sufficient water.', category: 'MAINTENANCE', priority: 'HIGH', targetRoles: ['ADMIN', 'RESIDENT', 'TENANT'], imageUrl: '', createdBy: 'admin_001', createdByName: 'Rajesh Kumar', isEmergency: false, expiresAt: now + 5 * 86400000, readBy: ['admin_001'], createdAt: now, updatedAt: now },
        { id: 'notice_002', title: 'Monthly Society Meeting', body: 'The monthly society meeting will be held on March 1st at 6 PM in the community hall. All residents are requested to attend.', category: 'GENERAL', priority: 'NORMAL', targetRoles: ['ADMIN', 'RESIDENT'], imageUrl: '', createdBy: 'admin_001', createdByName: 'Rajesh Kumar', isEmergency: false, expiresAt: now + 7 * 86400000, readBy: [], createdAt: now - 86400000, updatedAt: now - 86400000 },
        { id: 'notice_003', title: '⚠️ Fire Drill Tomorrow', body: 'A fire drill will be conducted tomorrow at 11 AM. Please evacuate through the designated fire exits. Assembly point: Main parking area.', category: 'EMERGENCY', priority: 'URGENT', targetRoles: ['ADMIN', 'RESIDENT', 'TENANT', 'SECURITY_GUARD', 'WORKER'], imageUrl: '', createdBy: 'admin_001', createdByName: 'Rajesh Kumar', isEmergency: true, expiresAt: now + 2 * 86400000, readBy: [], createdAt: now, updatedAt: now },
    ];

    for (const notice of notices) {
        await db.collection('notices').doc(notice.id).set(notice);
    }
    console.log(`  ✅ Created ${notices.length} notices`);

    // ============ COMPLAINTS ============
    console.log('📝 Creating complaints...');
    const complaints = [
        { id: 'complaint_001', flatId: 'flat_a201', flatNumber: 'A-201', towerBlock: 'Tower A', userId: 'resident_001', userName: 'Priya Sharma', title: 'Leaking tap in kitchen', description: 'The kitchen tap has been leaking for 2 days. Water is wasting continuously. Please send a plumber urgently.', category: 'PLUMBING', status: 'OPEN', priority: 'HIGH', imageUrls: [], assignedWorkerId: '', assignedWorkerName: '', resolution: '', resolvedAt: 0, createdAt: now - 2 * 86400000, updatedAt: now - 2 * 86400000 },
        { id: 'complaint_002', flatId: 'flat_b301', flatNumber: 'B-301', towerBlock: 'Tower B', userId: 'resident_002', userName: 'Amit Patel', title: 'Broken corridor light', description: 'The corridor light on 3rd floor of Tower B is broken and needs replacement.', category: 'ELECTRICAL', status: 'IN_PROGRESS', priority: 'MEDIUM', imageUrls: [], assignedWorkerId: 'worker_001', assignedWorkerName: 'Mohan Lal', resolution: '', resolvedAt: 0, createdAt: now - 5 * 86400000, updatedAt: now - 1 * 86400000 },
    ];

    for (const complaint of complaints) {
        await db.collection('complaints').doc(complaint.id).set(complaint);
    }
    console.log(`  ✅ Created ${complaints.length} complaints`);

    // ============ WORKERS ============
    console.log('🔧 Creating workers...');
    const workers = [
        { id: 'wkr_001', userId: 'worker_001', name: 'Mohan Lal', phoneNumber: '9999900006', profilePhotoUrl: '', idProofUrl: '', skills: ['Plumbing', 'Electrical'], category: 'PLUMBER', isAvailable: true, isDutyOn: true, rating: 4.2, totalRatings: 15, totalJobs: 25, totalEarnings: 37500, assignedFlats: ['flat_a201'], createdAt: now, updatedAt: now },
    ];

    for (const worker of workers) {
        await db.collection('workers').doc(worker.id).set(worker);
    }
    console.log(`  ✅ Created ${workers.length} workers`);

    // ============ VISITORS ============
    console.log('🚪 Creating visitors...');
    const visitors = [
        { id: 'visitor_001', name: 'Delivery Person', phoneNumber: '9876543210', purpose: 'Amazon Delivery', flatId: 'flat_a201', flatNumber: 'A-201', towerBlock: 'Tower A', residentId: 'resident_001', residentName: 'Priya Sharma', guardId: 'guard_001', guardName: 'Ram Singh', photoUrl: '', idProofUrl: '', vehicleNumber: '', vehicleType: 'NONE', status: 'CHECKED_OUT', isFrequentVisitor: false, isBlacklisted: false, entryTime: now - 3 * 3600000, exitTime: now - 2.5 * 3600000, approvedAt: now - 3.1 * 3600000, approvedBy: 'resident_001', denialReason: '', createdAt: now - 3.2 * 3600000, updatedAt: now - 2.5 * 3600000 },
        { id: 'visitor_002', name: 'Rahul Verma', phoneNumber: '9876543211', purpose: 'Personal Visit', flatId: 'flat_b301', flatNumber: 'B-301', towerBlock: 'Tower B', residentId: 'resident_002', residentName: 'Amit Patel', guardId: 'guard_001', guardName: 'Ram Singh', photoUrl: '', idProofUrl: '', vehicleNumber: 'MH01AB1234', vehicleType: 'FOUR_WHEELER', status: 'PENDING', isFrequentVisitor: false, isBlacklisted: false, entryTime: 0, exitTime: 0, approvedAt: 0, approvedBy: '', denialReason: '', createdAt: now, updatedAt: now },
    ];

    for (const visitor of visitors) {
        await db.collection('visitors').doc(visitor.id).set(visitor);
    }
    console.log(`  ✅ Created ${visitors.length} visitors`);

    console.log('\n🎉 Sample data seeded successfully!');
    console.log('\n📱 Test Accounts:');
    console.log('  Admin:    +91 9999900001 (Rajesh Kumar)');
    console.log('  Resident: +91 9999900002 (Priya Sharma)');
    console.log('  Resident: +91 9999900003 (Amit Patel)');
    console.log('  Tenant:   +91 9999900004 (Suresh Yadav)');
    console.log('  Guard:    +91 9999900005 (Ram Singh)');
    console.log('  Worker:   +91 9999900006 (Mohan Lal)');

    process.exit(0);
}

seedData().catch(err => {
    console.error('❌ Seed failed:', err);
    process.exit(1);
});

