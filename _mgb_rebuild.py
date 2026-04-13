from pathlib import Path

root = Path(r"E:\MGB Heigts\androidApp\src\main")
java_root = root / "java" / "com" / "mgbheights" / "android"
res_layout = root / "res" / "layout"
res_nav = root / "res" / "navigation"

# Deletions
kotlin_delete = {
    "AdminRegisterActivity.kt", "AuthActivity.kt", "ResetEmailSentActivity.kt", "VerifyEmailActivity.kt",
    "ForgotPasswordActivity.kt", "LoginActivity.kt", "GuardRegisterActivity.kt", "ResidentRegisterActivity.kt",
    "RoleSelectRegisterActivity.kt", "TenantRegisterActivity.kt", "WorkerRegisterActivity.kt",
    "SignUpVerifyOtpFragment.kt", "VerifyOtpFragment.kt", "LoginFragment.kt", "SignupFragment.kt",
    "SignupCreatePasswordFragment.kt", "SignupEmailFragment.kt", "EmailVerificationFragment.kt",
    "ForgotPasswordFragment.kt", "ForgotPasswordEmailFragment.kt", "ForgotPasswordNewFragment.kt",
    "ForgotPasswordVerifyOtpFragment.kt", "OnboardingFragment.kt", "DashboardFragment.kt",
    "MaintenanceListFragment.kt", "ChatFragment.kt", "ChatListFragment.kt", "CreateNoticeFragment.kt",
}
for f in java_root.rglob("*.kt"):
    if f.name in kotlin_delete:
        f.unlink(missing_ok=True)

layout_delete = {
    "activity_admin_register.xml", "activity_auth.xml", "activity_reset_email_sent.xml", "activity_verify_email.xml",
    "activity_forgot_password.xml", "activity_login.xml", "activity_guard_register.xml", "activity_resident_register.xml",
    "activity_role_select_register.xml", "activity_tenant_register.xml", "activity_worker_register.xml",
    "fragment_sign_up_verify_otp.xml", "fragment_verify_otp.xml", "fragment_login.xml", "fragment_signup.xml",
    "fragment_signup_create_password.xml", "fragment_signup_email.xml", "fragment_email_verification.xml",
    "fragment_forgot_password.xml", "fragment_forgot_password_email.xml", "fragment_forgot_password_new.xml",
    "fragment_forgot_password_verify_otp.xml", "fragment_onboarding.xml", "fragment_dashboard.xml",
    "fragment_maintenance_list.xml", "fragment_chat.xml", "fragment_chat_list.xml", "fragment_create_notice.xml",
}
for f in res_layout.glob("*.xml"):
    if f.name in layout_delete:
        f.unlink(missing_ok=True)

for name in [
    "nav_graph_admin.xml", "nav_graph_auth.xml", "nav_graph_guard.xml", "nav_graph_resident.xml",
    "nav_graph_tenant.xml", "nav_graph_worker.xml", "nav_graph.xml"
]:
    (res_nav / name).unlink(missing_ok=True)

screens = [
    ("splashFragment", "SplashFragment", "fragment_splash", "auth", []),
    ("landingFragment", "LandingFragment", "fragment_landing", "auth", []),
    ("enterEmailFragment", "EnterEmailFragment", "fragment_enter_email", "auth", []),
    ("otpVerifyFragment", "OtpVerifyFragment", "fragment_otp_verify", "auth", [("email", "string")]),
    ("selectRoleFragment", "SelectRoleFragment", "fragment_select_role", "auth", [("email", "string")]),
    ("createProfileFragment", "CreateProfileFragment", "fragment_create_profile", "auth", [("email", "string"), ("role", "string")]),
    ("pendingApprovalFragment", "PendingApprovalFragment", "fragment_pending_approval", "auth", []),
    ("adminDashboardFragment", "AdminDashboardFragment", "fragment_admin_dashboard", "admin", []),
    ("adminResidentsFragment", "AdminResidentsFragment", "fragment_admin_residents", "admin", []),
    ("adminResidentDetailFragment", "AdminResidentDetailFragment", "fragment_admin_resident_detail", "admin", [("userId", "string")]),
    ("adminTenantsFragment", "AdminTenantsFragment", "fragment_admin_tenants", "admin", []),
    ("adminTenantDetailFragment", "AdminTenantDetailFragment", "fragment_admin_tenant_detail", "admin", [("userId", "string")]),
    ("adminGuardsFragment", "AdminGuardsFragment", "fragment_admin_guards", "admin", []),
    ("adminWorkersFragment", "AdminWorkersFragment", "fragment_admin_workers", "admin", []),
    ("adminMaidsFragment", "AdminMaidsFragment", "fragment_admin_maids", "admin", []),
    ("adminUserDetailFragment", "AdminUserDetailFragment", "fragment_admin_user_detail", "admin", [("userId", "string"), ("userType", "string")]),
    ("adminUserManagementFragment", "AdminUserManagementFragment", "fragment_admin_user_management", "admin", []),
    ("adminNoticesFragment", "AdminNoticesFragment", "fragment_admin_notices", "admin", []),
    ("adminCreateNoticeFragment", "AdminCreateNoticeFragment", "fragment_admin_create_notice", "admin", []),
    ("adminComplaintsFragment", "AdminComplaintsFragment", "fragment_admin_complaints", "admin", []),
    ("adminComplaintDetailFragment", "AdminComplaintDetailFragment", "fragment_admin_complaint_detail", "admin", [("complaintId", "string")]),
    ("adminBillsFragment", "AdminBillsFragment", "fragment_admin_bills", "admin", []),
    ("adminCreateBillFragment", "AdminCreateBillFragment", "fragment_admin_create_bill", "admin", []),
    ("adminVisitorsFragment", "AdminVisitorsFragment", "fragment_admin_visitors", "admin", []),
    ("adminProfileFragment", "AdminProfileFragment", "fragment_admin_profile", "admin", []),
    ("adminShopFragment", "AdminShopFragment", "fragment_admin_shop", "admin", []),
    ("residentHomeFragment", "ResidentHomeFragment", "fragment_resident_home", "resident", []),
    ("residentComplaintsFragment", "ResidentComplaintsFragment", "fragment_resident_complaints", "resident", []),
    ("residentRaiseComplaintFragment", "ResidentRaiseComplaintFragment", "fragment_resident_raise_complaint", "resident", []),
    ("residentNoticesFragment", "ResidentNoticesFragment", "fragment_resident_notices", "resident", []),
    ("residentBillsFragment", "ResidentBillsFragment", "fragment_resident_bills", "resident", []),
    ("residentWorkerListFragment", "ResidentWorkerListFragment", "fragment_resident_worker_list", "resident", []),
    ("residentWorkerBookingFragment", "ResidentWorkerBookingFragment", "fragment_resident_worker_booking", "resident", [("workerId", "string")]),
    ("residentMyBookingsFragment", "ResidentMyBookingsFragment", "fragment_resident_my_bookings", "resident", []),
    ("residentMyTenantsFragment", "ResidentMyTenantsFragment", "fragment_resident_my_tenants", "resident", []),
    ("residentTenantDetailFragment", "ResidentTenantDetailFragment", "fragment_resident_tenant_detail", "resident", [("tenantId", "string")]),
    ("residentMyMaidFragment", "ResidentMyMaidFragment", "fragment_resident_my_maid", "resident", []),
    ("residentProfileFragment", "ResidentProfileFragment", "fragment_resident_profile", "resident", []),
    ("residentShopFragment", "ResidentShopFragment", "fragment_resident_shop", "resident", []),
    ("residentShopPostFragment", "ResidentShopPostFragment", "fragment_resident_shop_post", "resident", []),
    ("residentShopDetailFragment", "ResidentShopDetailFragment", "fragment_resident_shop_detail", "resident", [("listingId", "string")]),
    ("tenantHomeFragment", "TenantHomeFragment", "fragment_tenant_home", "tenant", []),
    ("tenantComplaintsFragment", "TenantComplaintsFragment", "fragment_tenant_complaints", "tenant", []),
    ("tenantRaiseComplaintFragment", "TenantRaiseComplaintFragment", "fragment_tenant_raise_complaint", "tenant", []),
    ("tenantNoticesFragment", "TenantNoticesFragment", "fragment_tenant_notices", "tenant", []),
    ("tenantBillsFragment", "TenantBillsFragment", "fragment_tenant_bills", "tenant", []),
    ("tenantWorkerListFragment", "TenantWorkerListFragment", "fragment_tenant_worker_list", "tenant", []),
    ("tenantWorkerBookingFragment", "TenantWorkerBookingFragment", "fragment_tenant_worker_booking", "tenant", [("workerId", "string")]),
    ("tenantMyMaidFragment", "TenantMyMaidFragment", "fragment_tenant_my_maid", "tenant", []),
    ("tenantProfileFragment", "TenantProfileFragment", "fragment_tenant_profile", "tenant", []),
    ("tenantShopFragment", "TenantShopFragment", "fragment_tenant_shop", "tenant", []),
    ("tenantShopPostFragment", "TenantShopPostFragment", "fragment_tenant_shop_post", "tenant", []),
    ("tenantShopDetailFragment", "TenantShopDetailFragment", "fragment_tenant_shop_detail", "tenant", [("listingId", "string")]),
    ("guardDashboardFragment", "GuardDashboardFragment", "fragment_guard_dashboard", "guard", []),
    ("guardAddVisitorFragment", "GuardAddVisitorFragment", "fragment_guard_add_visitor", "guard", []),
    ("guardVisitorLogFragment", "GuardVisitorLogFragment", "fragment_guard_visitor_log", "guard", []),
    ("guardProfileFragment", "GuardProfileFragment", "fragment_guard_profile", "guard", []),
    ("guardShopFragment", "GuardShopFragment", "fragment_guard_shop", "guard", []),
    ("workerDashboardFragment", "WorkerDashboardFragment", "fragment_worker_dashboard", "worker", []),
    ("workerBookingsFragment", "WorkerBookingsFragment", "fragment_worker_bookings", "worker", []),
    ("workerBookingDetailFragment", "WorkerBookingDetailFragment", "fragment_worker_booking_detail", "worker", [("bookingId", "string")]),
    ("workerProfileFragment", "WorkerProfileFragment", "fragment_worker_profile", "worker", []),
    ("workerShopFragment", "WorkerShopFragment", "fragment_worker_shop", "worker", []),
    ("maidDashboardFragment", "MaidDashboardFragment", "fragment_maid_dashboard", "maid", []),
    ("maidMyFlatSlotsFragment", "MaidMyFlatSlotsFragment", "fragment_maid_my_flat_slots", "maid", []),
    ("maidAddSlotFragment", "MaidAddSlotFragment", "fragment_maid_add_slot", "maid", []),
    ("maidProfileFragment", "MaidProfileFragment", "fragment_maid_profile", "maid", []),
    ("maidShopFragment", "MaidShopFragment", "fragment_maid_shop", "maid", []),
    ("shopListingDetailFragment", "ShopListingDetailFragment", "fragment_shop_listing_detail", "shop", [("listingId", "string")]),
    ("shopPostItemFragment", "ShopPostItemFragment", "fragment_shop_post_item", "shop", []),
    ("shopMyListingsFragment", "ShopMyListingsFragment", "fragment_shop_my_listings", "shop", []),
    ("shopSearchFragment", "ShopSearchFragment", "fragment_shop_search", "shop", []),
]

protected_layouts = {
    "fragment_admin_dashboard.xml",
    "fragment_resident_home.xml",
    "fragment_guard_dashboard.xml",
    "fragment_worker_dashboard.xml",
    "fragment_tenant_home.xml",
    "activity_main.xml",
}

for _, klass, layout, pkg, _ in screens:
    pkg_dir = java_root / "ui" / pkg
    pkg_dir.mkdir(parents=True, exist_ok=True)
    (pkg_dir / f"{klass}.kt").write_text(
        f"package com.mgbheights.android.ui.{pkg}\n\nimport androidx.fragment.app.Fragment\nimport com.mgbheights.android.R\n\nclass {klass} : Fragment(R.layout.{layout})\n",
        encoding="utf-8",
    )

    xml_name = f"{layout}.xml"
    if xml_name not in protected_layouts:
        title = klass.replace("Fragment", "")
        (res_layout / xml_name).write_text(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            "<FrameLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            "    android:layout_width=\"match_parent\"\n"
            "    android:layout_height=\"match_parent\">\n\n"
            "    <TextView\n"
            "        android:layout_width=\"wrap_content\"\n"
            "        android:layout_height=\"wrap_content\"\n"
            "        android:layout_gravity=\"center\"\n"
            f"        android:text=\"{title}\" />\n\n"
            "</FrameLayout>\n",
            encoding="utf-8",
        )

# nav graph
nav_lines = [
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>",
    "<navigation xmlns:android=\"http://schemas.android.com/apk/res/android\"",
    "    xmlns:app=\"http://schemas.android.com/apk/res-auto\"",
    "    android:id=\"@+id/nav_graph_main\"",
    "    app:startDestination=\"@id/splashFragment\">",
    "",
]
for dest_id, klass, _, pkg, args in screens:
    nav_lines.append("    <fragment")
    nav_lines.append(f"        android:id=\"@+id/{dest_id}\"")
    nav_lines.append(f"        android:name=\"com.mgbheights.android.ui.{pkg}.{klass}\"")
    nav_lines.append(f"        android:label=\"{klass}\">")
    for arg_name, arg_type in args:
        nav_lines.append(f"        <argument android:name=\"{arg_name}\" app:argType=\"{arg_type}\" />")
    nav_lines.append("    </fragment>")
    nav_lines.append("")
nav_lines.append("</navigation>")
(res_nav / "nav_graph_main.xml").write_text("\n".join(nav_lines), encoding="utf-8")

# manifest single activity
(root / "AndroidManifest.xml").write_text(
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
    "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">\n\n"
    "    <uses-permission android:name=\"android.permission.INTERNET\" />\n"
    "    <uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />\n"
    "    <uses-permission android:name=\"android.permission.CAMERA\" />\n"
    "    <uses-permission android:name=\"android.permission.READ_EXTERNAL_STORAGE\" android:maxSdkVersion=\"32\" />\n"
    "    <uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\" android:maxSdkVersion=\"28\" />\n"
    "    <uses-permission android:name=\"android.permission.READ_MEDIA_IMAGES\" />\n"
    "    <uses-permission android:name=\"android.permission.POST_NOTIFICATIONS\" />\n"
    "    <uses-permission android:name=\"android.permission.VIBRATE\" />\n"
    "    <uses-permission android:name=\"android.permission.RECEIVE_BOOT_COMPLETED\" />\n\n"
    "    <application\n"
    "        android:name=\".MgbHeightsApp\"\n"
    "        android:allowBackup=\"true\"\n"
    "        android:icon=\"@mipmap/ic_launcher\"\n"
    "        android:label=\"@string/app_name\"\n"
    "        android:roundIcon=\"@mipmap/ic_launcher_round\"\n"
    "        android:supportsRtl=\"true\"\n"
    "        android:theme=\"@style/Theme.MGBHeights\"\n"
    "        android:networkSecurityConfig=\"@xml/network_security_config\">\n\n"
    "        <activity\n"
    "            android:name=\".MainActivity\"\n"
    "            android:exported=\"true\">\n"
    "            <intent-filter>\n"
    "                <action android:name=\"android.intent.action.MAIN\" />\n"
    "                <category android:name=\"android.intent.category.LAUNCHER\" />\n"
    "            </intent-filter>\n"
    "        </activity>\n\n"
    "        <service\n"
    "            android:name=\".service.MgbFcmService\"\n"
    "            android:exported=\"false\">\n"
    "            <intent-filter>\n"
    "                <action android:name=\"com.google.firebase.MESSAGING_EVENT\" />\n"
    "            </intent-filter>\n"
    "        </service>\n\n"
    "        <provider\n"
    "            android:name=\"androidx.core.content.FileProvider\"\n"
    "            android:authorities=\"${applicationId}.fileprovider\"\n"
    "            android:exported=\"false\"\n"
    "            android:grantUriPermissions=\"true\">\n"
    "            <meta-data\n"
    "                android:name=\"android.support.FILE_PROVIDER_PATHS\"\n"
    "                android:resource=\"@xml/file_paths\" />\n"
    "        </provider>\n\n"
    "    </application>\n\n"
    "</manifest>\n",
    encoding="utf-8",
)

activity_main = res_layout / "activity_main.xml"
if activity_main.exists():
    txt = activity_main.read_text(encoding="utf-8")
    txt = txt.replace("@navigation/nav_graph_auth", "@navigation/nav_graph_main")
    activity_main.write_text(txt, encoding="utf-8")

bg = Path(r"E:\MGB Heigts\androidApp\build.gradle.kts")
if bg.exists():
    txt = bg.read_text(encoding="utf-8")
    txt = txt.replace("    implementation(libs.firebase.storage)\n", "")
    bg.write_text(txt, encoding="utf-8")

print("refactor scaffold complete")

