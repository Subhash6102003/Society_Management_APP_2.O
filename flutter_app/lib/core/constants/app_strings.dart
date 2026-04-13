/// Centralised string constants — avoids magic strings scattered throughout code.
class AppStrings {
  AppStrings._();

  // ── App ──────────────────────────────────────────────────────────────────────
  static const String appName = 'MGB Heights';
  static const String tagline = 'Welcome to';

  // ── Auth ─────────────────────────────────────────────────────────────────────
  static const String emailHint = 'Email Address';
  static const String passwordHint = 'Password';
  static const String confirmPasswordHint = 'Confirm Password';
  static const String forgotPassword = 'Forgot Password?';
  static const String login = 'Login';
  static const String signUp = 'Sign Up';
  static const String dontHaveAccount = "Don't have an account? Sign Up";
  static const String alreadyHaveAccount = 'Already have an account? Login';
  static const String termsLine =
      'By continuing, you agree to MGB Heights\nTerms of Service and Privacy Policy';
  static const String selectRole = 'Select Your Role';
  static const String selectRoleSubtitle =
      'Choose the role that best describes you';
  static const String createProfile = 'Create Your Profile';
  static const String pendingApprovalTitle = 'Pending Approval';
  static const String pendingApprovalMessage =
      'Your account is awaiting admin approval. You will be notified once approved.';
  static const String rejectedTitle = 'Access Denied';
  static const String rejectedMessage =
      'Your account registration was rejected. Please contact the society admin for assistance.';

  // ── Roles ─────────────────────────────────────────────────────────────────────
  static const String roleAdmin = 'Admin';
  static const String roleResident = 'Resident';
  static const String roleTenant = 'Tenant';
  static const String roleGuard = 'Security Guard';
  static const String roleWorker = 'Worker';
  static const String roleMaid = 'Maid';

  // ── Navigation labels ─────────────────────────────────────────────────────────
  static const String home = 'Home';
  static const String complaints = 'Complaints';
  static const String notices = 'Notices';
  static const String bills = 'Bills';
  static const String profile = 'Profile';
  static const String visitors = 'Visitors';
  static const String workers = 'Workers';
  static const String maids = 'Maids';
  static const String settings = 'Settings';
  static const String shop = 'Shop';

  // ── Common actions ────────────────────────────────────────────────────────────
  static const String approve = 'Approve';
  static const String reject = 'Reject';
  static const String block = 'Block';
  static const String unblock = 'Unblock';
  static const String submit = 'Submit';
  static const String cancel = 'Cancel';
  static const String save = 'Save';
  static const String edit = 'Edit';
  static const String delete = 'Delete';
  static const String back = 'Back';
  static const String done = 'Done';
  static const String yes = 'Yes';
  static const String no = 'No';
  static const String logout = 'Logout';
  static const String retry = 'Retry';

  // ── Errors ─────────────────────────────────────────────────────────────────────
  static const String genericError = 'Something went wrong. Please try again.';
  static const String networkError = 'No internet connection.';
  static const String invalidEmail = 'Please enter a valid email address.';
  static const String passwordTooShort = 'Password must be at least 6 characters.';
  static const String passwordMismatch = 'Passwords do not match.';
  static const String fieldRequired = 'This field is required.';
}
