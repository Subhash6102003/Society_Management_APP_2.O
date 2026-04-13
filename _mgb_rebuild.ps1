$root = 'E:\MGB Heigts\androidApp\src\main'
$javaRoot = Join-Path $root 'java\com\mgbheights\android'
$layoutRoot = Join-Path $root 'res\layout'
$navRoot = Join-Path $root 'res\navigation'

$kotlinDelete = @(
'AdminRegisterActivity.kt','AuthActivity.kt','ResetEmailSentActivity.kt','VerifyEmailActivity.kt',
'ForgotPasswordActivity.kt','LoginActivity.kt','GuardRegisterActivity.kt','ResidentRegisterActivity.kt',
'RoleSelectRegisterActivity.kt','TenantRegisterActivity.kt','WorkerRegisterActivity.kt',
'SignUpVerifyOtpFragment.kt','VerifyOtpFragment.kt','LoginFragment.kt','SignupFragment.kt',
'SignupCreatePasswordFragment.kt','SignupEmailFragment.kt','EmailVerificationFragment.kt',
'ForgotPasswordFragment.kt','ForgotPasswordEmailFragment.kt','ForgotPasswordNewFragment.kt',
'ForgotPasswordVerifyOtpFragment.kt','OnboardingFragment.kt','DashboardFragment.kt',
'MaintenanceListFragment.kt','ChatFragment.kt','ChatListFragment.kt','CreateNoticeFragment.kt'
)
Get-ChildItem -Path $javaRoot -Recurse -Filter *.kt | ForEach-Object { if ($kotlinDelete -contains $_.Name) { Remove-Item $_.FullName -Force } }

$layoutDelete = @(
'activity_admin_register.xml','activity_auth.xml','activity_reset_email_sent.xml','activity_verify_email.xml',
'activity_forgot_password.xml','activity_login.xml','activity_guard_register.xml','activity_resident_register.xml',
'activity_role_select_register.xml','activity_tenant_register.xml','activity_worker_register.xml',
'fragment_sign_up_verify_otp.xml','fragment_verify_otp.xml','fragment_login.xml','fragment_signup.xml',
'fragment_signup_create_password.xml','fragment_signup_email.xml','fragment_email_verification.xml',
'fragment_forgot_password.xml','fragment_forgot_password_email.xml','fragment_forgot_password_new.xml',
'fragment_forgot_password_verify_otp.xml','fragment_onboarding.xml','fragment_dashboard.xml',
'fragment_maintenance_list.xml','fragment_chat.xml','fragment_chat_list.xml','fragment_create_notice.xml'
)
Get-ChildItem -Path $layoutRoot -Filter *.xml | ForEach-Object { if ($layoutDelete -contains $_.Name) { Remove-Item $_.FullName -Force } }

@('nav_graph_admin.xml','nav_graph_auth.xml','nav_graph_guard.xml','nav_graph_resident.xml','nav_graph_tenant.xml','nav_graph_worker.xml','nav_graph.xml') | ForEach-Object {
  $p = Join-Path $navRoot $_
  if (Test-Path $p) { Remove-Item $p -Force }
}

$screens = @(
@('splashFragment','SplashFragment','fragment_splash','auth',''),
@('landingFragment','LandingFragment','fragment_landing','auth',''),
@('enterEmailFragment','EnterEmailFragment','fragment_enter_email','auth',''),
@('otpVerifyFragment','OtpVerifyFragment','fragment_otp_verify','auth','email:string'),
@('selectRoleFragment','SelectRoleFragment','fragment_select_role','auth','email:string'),
@('createProfileFragment','CreateProfileFragment','fragment_create_profile','auth','email:string,role:string'),
@('pendingApprovalFragment','PendingApprovalFragment','fragment_pending_approval','auth',''),
@('adminDashboardFragment','AdminDashboardFragment','fragment_admin_dashboard','admin',''),
@('adminResidentsFragment','AdminResidentsFragment','fragment_admin_residents','admin',''),
@('adminResidentDetailFragment','AdminResidentDetailFragment','fragment_admin_resident_detail','admin','userId:string'),
@('adminTenantsFragment','AdminTenantsFragment','fragment_admin_tenants','admin',''),
@('adminTenantDetailFragment','AdminTenantDetailFragment','fragment_admin_tenant_detail','admin','userId:string'),
@('adminGuardsFragment','AdminGuardsFragment','fragment_admin_guards','admin',''),
@('adminWorkersFragment','AdminWorkersFragment','fragment_admin_workers','admin',''),
@('adminMaidsFragment','AdminMaidsFragment','fragment_admin_maids','admin',''),
@('adminUserDetailFragment','AdminUserDetailFragment','fragment_admin_user_detail','admin','userId:string,userType:string'),
@('adminUserManagementFragment','AdminUserManagementFragment','fragment_admin_user_management','admin',''),
@('adminNoticesFragment','AdminNoticesFragment','fragment_admin_notices','admin',''),
@('adminCreateNoticeFragment','AdminCreateNoticeFragment','fragment_admin_create_notice','admin',''),
@('adminComplaintsFragment','AdminComplaintsFragment','fragment_admin_complaints','admin',''),
@('adminComplaintDetailFragment','AdminComplaintDetailFragment','fragment_admin_complaint_detail','admin','complaintId:string'),
@('adminBillsFragment','AdminBillsFragment','fragment_admin_bills','admin',''),
@('adminCreateBillFragment','AdminCreateBillFragment','fragment_admin_create_bill','admin',''),
@('adminVisitorsFragment','AdminVisitorsFragment','fragment_admin_visitors','admin',''),
@('adminProfileFragment','AdminProfileFragment','fragment_admin_profile','admin',''),
@('adminShopFragment','AdminShopFragment','fragment_admin_shop','admin',''),
@('residentHomeFragment','ResidentHomeFragment','fragment_resident_home','resident',''),
@('residentComplaintsFragment','ResidentComplaintsFragment','fragment_resident_complaints','resident',''),
@('residentRaiseComplaintFragment','ResidentRaiseComplaintFragment','fragment_resident_raise_complaint','resident',''),
@('residentNoticesFragment','ResidentNoticesFragment','fragment_resident_notices','resident',''),
@('residentBillsFragment','ResidentBillsFragment','fragment_resident_bills','resident',''),
@('residentWorkerListFragment','ResidentWorkerListFragment','fragment_resident_worker_list','resident',''),
@('residentWorkerBookingFragment','ResidentWorkerBookingFragment','fragment_resident_worker_booking','resident','workerId:string'),
@('residentMyBookingsFragment','ResidentMyBookingsFragment','fragment_resident_my_bookings','resident',''),
@('residentMyTenantsFragment','ResidentMyTenantsFragment','fragment_resident_my_tenants','resident',''),
@('residentTenantDetailFragment','ResidentTenantDetailFragment','fragment_resident_tenant_detail','resident','tenantId:string'),
@('residentMyMaidFragment','ResidentMyMaidFragment','fragment_resident_my_maid','resident',''),
@('residentProfileFragment','ResidentProfileFragment','fragment_resident_profile','resident',''),
@('residentShopFragment','ResidentShopFragment','fragment_resident_shop','resident',''),
@('residentShopPostFragment','ResidentShopPostFragment','fragment_resident_shop_post','resident',''),
@('residentShopDetailFragment','ResidentShopDetailFragment','fragment_resident_shop_detail','resident','listingId:string'),
@('tenantHomeFragment','TenantHomeFragment','fragment_tenant_home','tenant',''),
@('tenantComplaintsFragment','TenantComplaintsFragment','fragment_tenant_complaints','tenant',''),
@('tenantRaiseComplaintFragment','TenantRaiseComplaintFragment','fragment_tenant_raise_complaint','tenant',''),
@('tenantNoticesFragment','TenantNoticesFragment','fragment_tenant_notices','tenant',''),
@('tenantBillsFragment','TenantBillsFragment','fragment_tenant_bills','tenant',''),
@('tenantWorkerListFragment','TenantWorkerListFragment','fragment_tenant_worker_list','tenant',''),
@('tenantWorkerBookingFragment','TenantWorkerBookingFragment','fragment_tenant_worker_booking','tenant','workerId:string'),
@('tenantMyMaidFragment','TenantMyMaidFragment','fragment_tenant_my_maid','tenant',''),
@('tenantProfileFragment','TenantProfileFragment','fragment_tenant_profile','tenant',''),
@('tenantShopFragment','TenantShopFragment','fragment_tenant_shop','tenant',''),
@('tenantShopPostFragment','TenantShopPostFragment','fragment_tenant_shop_post','tenant',''),
@('tenantShopDetailFragment','TenantShopDetailFragment','fragment_tenant_shop_detail','tenant','listingId:string'),
@('guardDashboardFragment','GuardDashboardFragment','fragment_guard_dashboard','guard',''),
@('guardAddVisitorFragment','GuardAddVisitorFragment','fragment_guard_add_visitor','guard',''),
@('guardVisitorLogFragment','GuardVisitorLogFragment','fragment_guard_visitor_log','guard',''),
@('guardProfileFragment','GuardProfileFragment','fragment_guard_profile','guard',''),
@('guardShopFragment','GuardShopFragment','fragment_guard_shop','guard',''),
@('workerDashboardFragment','WorkerDashboardFragment','fragment_worker_dashboard','worker',''),
@('workerBookingsFragment','WorkerBookingsFragment','fragment_worker_bookings','worker',''),
@('workerBookingDetailFragment','WorkerBookingDetailFragment','fragment_worker_booking_detail','worker','bookingId:string'),
@('workerProfileFragment','WorkerProfileFragment','fragment_worker_profile','worker',''),
@('workerShopFragment','WorkerShopFragment','fragment_worker_shop','worker',''),
@('maidDashboardFragment','MaidDashboardFragment','fragment_maid_dashboard','maid',''),
@('maidMyFlatSlotsFragment','MaidMyFlatSlotsFragment','fragment_maid_my_flat_slots','maid',''),
@('maidAddSlotFragment','MaidAddSlotFragment','fragment_maid_add_slot','maid',''),
@('maidProfileFragment','MaidProfileFragment','fragment_maid_profile','maid',''),
@('maidShopFragment','MaidShopFragment','fragment_maid_shop','maid',''),
@('shopListingDetailFragment','ShopListingDetailFragment','fragment_shop_listing_detail','shop','listingId:string'),
@('shopPostItemFragment','ShopPostItemFragment','fragment_shop_post_item','shop',''),
@('shopMyListingsFragment','ShopMyListingsFragment','fragment_shop_my_listings','shop',''),
@('shopSearchFragment','ShopSearchFragment','fragment_shop_search','shop','')
)

$protectedLayouts = @('fragment_admin_dashboard.xml','fragment_resident_home.xml','fragment_guard_dashboard.xml','fragment_worker_dashboard.xml','fragment_tenant_home.xml','activity_main.xml')

foreach ($s in $screens) {
  $destId = $s[0]; $klass = $s[1]; $layout = $s[2]; $pkg = $s[3]
  $pkgDir = Join-Path $javaRoot ("ui\" + $pkg)
  if (!(Test-Path $pkgDir)) { New-Item -ItemType Directory -Path $pkgDir | Out-Null }

  $ktPath = Join-Path $pkgDir ($klass + '.kt')
  $ktContent = @"
package com.mgbheights.android.ui.$pkg

import androidx.fragment.app.Fragment
import com.mgbheights.android.R

class $klass : Fragment(R.layout.$layout)
"@
  Set-Content -Path $ktPath -Value $ktContent -Encoding UTF8

  $layoutFile = $layout + '.xml'
  if ($protectedLayouts -notcontains $layoutFile) {
    $xmlPath = Join-Path $layoutRoot $layoutFile
    $xmlContent = @"
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="$klass" />

</FrameLayout>
"@
    Set-Content -Path $xmlPath -Value $xmlContent -Encoding UTF8
  }
}

$navMain = Join-Path $navRoot 'nav_graph_main.xml'
$navLines = @(
'<?xml version="1.0" encoding="utf-8"?>',
'<navigation xmlns:android="http://schemas.android.com/apk/res/android"',
'    xmlns:app="http://schemas.android.com/apk/res-auto"',
'    android:id="@+id/nav_graph_main"',
'    app:startDestination="@id/splashFragment">',
''
)

foreach ($s in $screens) {
  $destId = $s[0]; $klass = $s[1]; $pkg = $s[3]; $argDef = $s[4]
  $navLines += '    <fragment'
  $navLines += "        android:id=\"@+id/$destId\""
  $navLines += "        android:name=\"com.mgbheights.android.ui.$pkg.$klass\""
  $navLines += "        android:label=\"$klass\">"
  if ($argDef -ne '') {
    $pairs = $argDef.Split(',')
    foreach ($pair in $pairs) {
      $parts = $pair.Split(':')
      $navLines += "        <argument android:name=\"$($parts[0])\" app:argType=\"$($parts[1])\" />"
    }
  }
  $navLines += '    </fragment>'
  $navLines += ''
}
$navLines += '</navigation>'
Set-Content -Path $navMain -Value ($navLines -join "`n") -Encoding UTF8

$manifest = Join-Path $root 'AndroidManifest.xml'
$manifestContent = @"
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:name=".MgbHeightsApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MGBHeights"
        android:networkSecurityConfig="@xml/network_security_config">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".service.MgbFcmService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="`${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

    </application>

</manifest>
"@
Set-Content -Path $manifest -Value $manifestContent -Encoding UTF8

$activityMain = Join-Path $layoutRoot 'activity_main.xml'
if (Test-Path $activityMain) {
  (Get-Content $activityMain -Raw).Replace('@navigation/nav_graph_auth', '@navigation/nav_graph_main') | Set-Content -Path $activityMain -Encoding UTF8
}

$gradlePath = 'E:\MGB Heigts\androidApp\build.gradle.kts'
if (Test-Path $gradlePath) {
  (Get-Content $gradlePath -Raw).Replace('    implementation(libs.firebase.storage)`n', '') | Set-Content -Path $gradlePath -Encoding UTF8
}

Write-Output 'MGB rebuild scaffold complete.'

