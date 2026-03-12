/**
 * Fix the admin user's Firestore document.
 * Finds user "Yuvraj Yadav" and sets role=ADMIN, isApproved=true.
 *
 * Run: node firebase/fix-admin.js
 */
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');

// Use default credentials (firebase login already authenticated)
initializeApp({ projectId: 'mgbheights-39ec1' });
const db = getFirestore();

async function fixAdmin() {
    console.log('🔍 Searching for user "Yuvraj Yadav"...');

    // Find user by name
    const snap = await db.collection('users')
        .where('name', '==', 'Yuvraj Yadav')
        .get();

    if (snap.empty) {
        // Try to find ALL users and show them
        console.log('User not found by name. Listing all users...');
        const allUsers = await db.collection('users').get();
        allUsers.forEach(doc => {
            const d = doc.data();
            console.log(`  ${doc.id}: ${d.name || '(no name)'} | role=${d.role} | approved=${d.isApproved} | phone=${d.phoneNumber || ''} | email=${d.email || ''}`);
        });
        return;
    }

    snap.forEach(async (doc) => {
        const data = doc.data();
        console.log(`\n📋 Found user: ${doc.id}`);
        console.log(`   Name: ${data.name}`);
        console.log(`   Role: ${data.role}`);
        console.log(`   isApproved: ${data.isApproved}`);
        console.log(`   isProfileComplete: ${data.isProfileComplete}`);
        console.log(`   Phone: ${data.phoneNumber || 'N/A'}`);
        console.log(`   Email: ${data.email || 'N/A'}`);

        // Fix: set role to ADMIN and isApproved to true
        await db.collection('users').doc(doc.id).update({
            role: 'ADMIN',
            isApproved: true,
            isProfileComplete: true,
            isOnboarded: true,
            updatedAt: Date.now()
        });

        console.log(`\n✅ Updated ${doc.id}:`);
        console.log(`   role: RESIDENT → ADMIN`);
        console.log(`   isApproved: false → true`);
        console.log(`   isProfileComplete: true`);
    });
}

fixAdmin().then(() => {
    console.log('\n🎉 Done! You can now log in as admin.');
    process.exit(0);
}).catch(err => {
    console.error('❌ Error:', err.message);
    process.exit(1);
});

