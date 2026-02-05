# Google OAuth2 Login - Setup Guide

## Overview
This document explains how to set up Google OAuth2 login for your Book Management application.

## Implementation Summary

### Backend (Spring Boot)
1. ✅ User model updated with `provider` and `googleId` fields
2. ✅ Added Spring Security OAuth2 dependency to `pom.xml`
3. ✅ Configured OAuth2 in `application.properties`
4. ✅ Updated `SecurityConfig` to allow OAuth2 endpoints
5. ✅ Created `OAuth2SuccessHandler` to handle successful Google login
6. ✅ Updated `UserRepository` with `findByGoogleId` method

### Frontend
1. ✅ Added "Đăng nhập bằng Google" button to login page
2. ✅ Added CSS styling for Google button
3. ✅ Updated `auth.js` to handle OAuth2 callback

## Setup Instructions

### Step 1: Get Google OAuth2 Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing project
3. Navigate to **APIs & Services** → **Credentials**
4. Click **Create Credentials** → **OAuth 2.0 Client ID**

### Step 2: Configure OAuth Consent Screen

1. If prompted, configure the OAuth consent screen:
   - User Type: **External**
   - App name: **Book Management**
   - User support email: **your-email@example.com**
   - Authorized domains: **localhost** (for development)

### Step 3: Create OAuth Client ID

1. Application type: **Web application**
2. Name: **Book Management Web**
3. Authorized JavaScript origins:
   - `http://localhost:8082` (backend)
   - `http://localhost:5500` (frontend)
4. Authorized redirect URIs:
   - `http://localhost:8082/login/oauth2/code/google`
5. Click **Create**

### Step 4: Copy Credentials

After creating the OAuth client, you'll get:
- **Client ID**: A long string like `123456789-abc...apps.googleusercontent.com`
- **Client Secret**: A secret string

### Step 5: Update application.properties

Edit `src/main/resources/application.properties`:

```properties
# Replace these with your actual Google OAuth2 credentials
spring.security.oauth2.client.registration.google.client-id=YOUR_ACTUAL_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_ACTUAL_CLIENT_SECRET

# Make sure the redirect URI matches what you configured
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8082/login/oauth2/code/google
```

### Step 6: Build and Run Backend

```bash
# Build the project
mvn clean install

# Run the backend
mvn spring-boot:run
```

### Step 7: Run Frontend

```bash
# Navigate to frontend directory
cd frontend

# Use a local server (e.g., Live Server in VS Code)
# Or use Python's built-in server
python -m http.server 5500
```

## Testing the OAuth2 Flow

### Test Case 1: New Google User

1. Open `http://localhost:5500/frontend/pages/login.html`
2. Click "Đăng nhập bằng Google" button
3. Sign in with your Google account
4. Grant permissions (email, profile)
5. After successful login:
   - A new user is created in MongoDB with:
     - `provider`: "GOOGLE"
     - `googleId`: Google's user ID
     - `username`: From Google profile
     - `email`: From Google
     - `role`: "USER" (default)
   - JWT token is generated
   - User is redirected to home page

### Test Case 2: Existing Google User

1. Log out if already logged in
2. Click "Đăng nhập bằng Google" button
3. Sign in with the same Google account
4. After successful login:
   - Existing user is found by `googleId`
   - JWT token is generated
   - User is redirected to home page

### Test Case 3: Link Google to Existing Local Account

1. First, create a regular account with your Google email
2. Log out
3. Click "Đăng nhập bằng Google" button
4. Sign in with the same Google account
5. After successful login:
   - Existing account is linked to Google
   - `googleId` and `provider` fields are updated
   - Original role and settings are preserved

## User Model Changes

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User implements UserDetails {
    // ... existing fields ...

    @Builder.Default
    private String provider = "LOCAL";  // "LOCAL" or "GOOGLE"

    private String googleId;  // Google's unique user ID

    // ... rest of the model ...
}
```

## API Endpoints

### OAuth2 Endpoints (Automatic)
- `GET /oauth2/authorization/google` - Initiate Google OAuth2 flow
- `GET /login/oauth2/code/google` - OAuth2 callback endpoint

### Auth Endpoints (Existing)
- `POST /auth/register` - Regular registration
- `POST /auth/login` - Regular login
- `GET /auth/me` - Get current user info

## Frontend OAuth2 Handling

After successful Google login, the user is redirected to:
```
http://localhost:5500/frontend/pages/index.html?token=JWT_TOKEN&userId=USER_ID&username=USERNAME&email=EMAIL&role=ROLE
```

The frontend's `auth.js` automatically:
1. Extracts these parameters
2. Saves token to localStorage
3. Saves user info to localStorage
4. Redirects to appropriate page based on role

## Security Considerations

### Google OAuth2 Security
- Uses HTTPS for all OAuth2 communications
- JWT tokens have expiration time (24 hours by default)
- Google OAuth2 tokens are short-lived and only used for initial authentication
- No password is stored for Google users

### JWT Security
- Tokens are signed with a secret key
- Tokens expire after configured duration
- Tokens are stored in localStorage (client-side)
- Backend validates all JWT tokens on protected endpoints

## Troubleshooting

### Issue: "redirect_uri_mismatch" Error
**Cause**: Authorized redirect URI in Google Cloud Console doesn't match your application
**Solution**: Make sure the redirect URI in Google Console matches exactly:
```
http://localhost:8082/login/oauth2/code/google
```

### Issue: User not being created
**Cause**: Database connection issue or User model issue
**Solution**: Check MongoDB connection and logs in `backend.log`

### Issue: JWT not being generated
**Cause**: OAuth2SuccessHandler not configured correctly
**Solution**: Check that `OAuth2SuccessHandler` is properly autowired in `SecurityConfig`

### Issue: Frontend not receiving token
**Cause**: Redirect URL configuration issue
**Solution**: Verify `oauth2.frontend-redirect-url` in `application.properties` matches your frontend URL

## Next Steps

1. ✅ Get Google OAuth2 credentials
2. ✅ Update `application.properties` with credentials
3. ✅ Build and run backend
4. ✅ Run frontend
5. ✅ Test OAuth2 flow

## Database Migration

Existing users will have:
- `provider`: "LOCAL" (default)
- `googleId`: null

New Google users will have:
- `provider`: "GOOGLE"
- `googleId`: Google's user ID
- `password`: "" (empty string)

No database migration script is needed as Spring Data MongoDB will handle the new fields automatically.

## Resources

- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Google OAuth2 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Spring Boot OAuth2 Client](https://spring.io/guides/gs/securing-web/)
