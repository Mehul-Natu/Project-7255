package com.edu.info7255;

import com.edu.info7255.utils.OauthClient;
import org.springframework.stereotype.Component;

import javax.crypto.spec.OAEPParameterSpec;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;


@Component
public class JwtUtils {

    private static final String AUD = "234658653689-p8ncioalgg3di6nfi9dd2p0j0gecj88v.apps.googleusercontent.com";

    private static List<String> issuerList = new LinkedList<>() {
        {
            add("https://accounts.google.com");
        }
    };


    public static boolean verifier(String token) {
        try {
            String[] strings = token.split(" ");
            OauthClient oauthClient = new OauthClient();
            return oauthClient.verify(strings[1]);
        } catch (Exception e) {
            System.out.println("Validation failed");
            return false;
        }
    }

/*
    public static boolean verifyToken1(String token) {
        try {
            token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVkZjFmOTQ1ZmY5MDZhZWFlZmE5M2MyNzY5OGRiNDA2ZDYwNmIwZTgiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiIyMzQ2NTg2NTM2ODktcDhuY2lvYWxnZzNkaTZuZmk5ZGQycDBqMGdlY2o4OHYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiIyMzQ2NTg2NTM2ODktcDhuY2lvYWxnZzNkaTZuZmk5ZGQycDBqMGdlY2o4OHYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTcwNTQwODAwMjQxODI1NDUxNDAiLCJhdF9oYXNoIjoiVlZsNTlyeGhScUx6eXRwYjJyVXhoZyIsIm5hbWUiOiJNZWh1bCBOYXR1IiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hL0FHTm15eGJBU1JKakl3dnEtOWE3TVhNekhOQ1k5R182c0pUdDAzc3lXbGxLPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6Ik1laHVsIiwiZmFtaWx5X25hbWUiOiJOYXR1IiwibG9jYWxlIjoiZW4iLCJpYXQiOjE2NzkxMDA3MDMsImV4cCI6MTY3OTEwNDMwM30.bSPeOZYtjMK4xQoiVZSbZ1bNdWku2_LHA1NaajS7bNhjLhhwKWd_nq5OivPZ3aovZazVPMckcTnTUYlYQ_r88ym12xFbfYoUBKaC49hQG08muEovXSNDkpN1ryioK6eKBl_pjh0eTfZTE3_RYhpWiNWt1kxz6UG66p0aZu-B2oCwtMDLhRwTYdywwKqZ4Lu9-GErAIhFbKy2p45CjHBlBTOScmVwhsnropJ37xUBtZYSqmFcOQUSYIMO5eAsx7dWy-zw6bXFjS0UjqwpCUbxjp8dAwwywaThd3W3BHc-U1D5ORZp8f87iWlHkwsV6qFYsLgoQzeiBeFTpmjGvpvG_Q";
            DecodedJWT jwt = JWT.decode(token);


            String secretKey = "{\n" +
                    "  \"e\": \"AQAB\",\n" +
                    "  \"kty\": \"RSA\",\n" +
                    "  \"n\": \"61aS7BCy4zAWy_6A7mWFfqO20sgMWMc3uCx5qyQgx4l1xnwEqrPAScnqTBDt9c-ExNkN-ixb8Y28HWt2ey7Do3p_FVNkd5r29wBIiBCYiPK6MOTAAOOhlMGrduWH98PnPim_kCeaU0JD1594US4p00xx6kJwkajYwtI8Xna0Ma-x7FJyIgHFN_A6EWXdiDfQSxILHwdmW9Vnqq8UMh7QBCiyXcbcabtZ4cXp3S32Br8_aQmmbh7VQjjH8Ux2Oehcj8ugHLg0wv3qDObdDEgnF7UfTrvIQsuPMmSu6EgRSJIqmozUFYrZFn48335bz6y101iSTK-mFdCCelMgfcXrUQ\"\n" +
                    "}";
            X509EncodedKeySpec ks = new X509EncodedKeySpec(secretKey.getBytes());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pub = kf.generatePublic(ks);
            Algorithm algorithm = Algorithm.RSA512((RSAKeyProvider) pub);
            DecodedJWT jwt1 = JWT.require(algorithm).build().verify(token);

            SignatureAlgorithm sa = RS256;
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), sa.getJcaName());
            DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(sa, secretKeySpec);
            System.out.println("is valid = " + validator.isValid(jwt.getHeader() + "." + jwt.getPayload(), jwt.getSignature()));
            String algo = jwt.getAlgorithm();
            String kid = jwt.getKeyId();
            String issuer = jwt.getIssuer();
            //System.out.println();
            System.out.println(isExpired(jwt));
            System.out.println(issuer);
            System.out.println(new String(Base64.getDecoder().decode(jwt.getHeader()), StandardCharsets.UTF_8));

            if (issuer == null || !issuerList.contains(issuer)) {
                System.out.println("Wrong Issuer");
                return false;
            }
            if (verifyAud(jwt)) {
                System.out.println("Wrong Audience");
                return false;
            }
            if (isExpired(jwt)) {
                System.out.println("Token is expired");
                return false;
            }

            return true;
        } catch (Exception e) {
            System.out.println("Validation failed");
            return false;
        }
    }

    public static boolean verifyToken(String token) {

        try {
            token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVkZjFmOTQ1ZmY5MDZhZWFlZmE5M2MyNzY5OGRiNDA2ZDYwNmIwZTgiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiIyMzQ2NTg2NTM2ODktcDhuY2lvYWxnZzNkaTZuZmk5ZGQycDBqMGdlY2o4OHYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiIyMzQ2NTg2NTM2ODktcDhuY2lvYWxnZzNkaTZuZmk5ZGQycDBqMGdlY2o4OHYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTcwNTQwODAwMjQxODI1NDUxNDAiLCJhdF9oYXNoIjoiVlZsNTlyeGhScUx6eXRwYjJyVXhoZyIsIm5hbWUiOiJNZWh1bCBOYXR1IiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hL0FHTm15eGJBU1JKakl3dnEtOWE3TVhNekhOQ1k5R182c0pUdDAzc3lXbGxLPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6Ik1laHVsIiwiZmFtaWx5X25hbWUiOiJOYXR1IiwibG9jYWxlIjoiZW4iLCJpYXQiOjE2NzkxMDA3MDMsImV4cCI6MTY3OTEwNDMwM30.bSPeOZYtjMK4xQoiVZSbZ1bNdWku2_LHA1NaajS7bNhjLhhwKWd_nq5OivPZ3aovZazVPMckcTnTUYlYQ_r88ym12xFbfYoUBKaC49hQG08muEovXSNDkpN1ryioK6eKBl_pjh0eTfZTE3_RYhpWiNWt1kxz6UG66p0aZu-B2oCwtMDLhRwTYdywwKqZ4Lu9-GErAIhFbKy2p45CjHBlBTOScmVwhsnropJ37xUBtZYSqmFcOQUSYIMO5eAsx7dWy-zw6bXFjS0UjqwpCUbxjp8dAwwywaThd3W3BHc-U1D5ORZp8f87iWlHkwsV6qFYsLgoQzeiBeFTpmjGvpvG_Q";

            UrlFetchTransport transport = new UrlFetchTransport.Builder().validateCertificate().build();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, JacksonFactory.getDefaultInstance())
                    // Specify the CLIENT_ID of the app that accesses the backend:
                    .setAudience(Collections.singletonList(AUD))
                    // Or, if multiple clients access the backend:
                    //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                    .build();

// (Receive idTokenString by HTTPS POST)

            GoogleIdToken idToken = verifier.verify(token);

        } catch (Exception e) {

        }
        return false;
    }

 */

    public static boolean verifyTokenOkta(String token) {
        token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVkZjFmOTQ1ZmY5MDZhZWFlZmE5M2MyNzY5OGRiNDA2ZDYwNmIwZTgiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiIyMzQ2NTg2NTM2ODktcDhuY2lvYWxnZzNkaTZuZmk5ZGQycDBqMGdlY2o4OHYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiIyMzQ2NTg2NTM2ODktcDhuY2lvYWxnZzNkaTZuZmk5ZGQycDBqMGdlY2o4OHYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTcwNTQwODAwMjQxODI1NDUxNDAiLCJhdF9oYXNoIjoiSnZPNTdYU21OS1VRV2RhaEs1X0pfdyIsIm5hbWUiOiJNZWh1bCBOYXR1IiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hL0FHTm15eGJBU1JKakl3dnEtOWE3TVhNekhOQ1k5R182c0pUdDAzc3lXbGxLPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6Ik1laHVsIiwiZmFtaWx5X25hbWUiOiJOYXR1IiwibG9jYWxlIjoiZW4iLCJpYXQiOjE2NzkxMDMzMDYsImV4cCI6MTY3OTEwNjkwNn0.ZjvVje3_i7kRHZOwTJhRYD7qFaf9FLPhXDcCjxKYwNwibYc_jkBiPA6lh56b7gtkhOZi-d7faPmaSttK5lQDXXNBE0mFZpu7gepY56ucTLUIW3aXx4mAd42yBF2NzHutO7S4MUZSlU6Iybq1Ryz84e1FERIL5fs2J4FqDV3-ySQYGi2mCaBUA2oZpMQurbuu_P9-lXKWnSFmvnZh7T9sS6wyGXRw9wGres-FOKvqr_2G8LiIzOrIGUkrme30K56s3iBwZejODY02SU4pVXcSxpc39FCanNwQL1PpxNBWlmz_KW5FiYTXSCuyL25781m-B9smD-5iGyL6m6VVEyn3Vw";
        try {

/*
            AccessTokenVerifier jwtVerifier = JwtVerifiers.accessTokenVerifierBuilder()
                    .setIssuer("https://accounts.google.com")
                    .setAudience(AUD)                   // defaults to 'api://default'
                    .setConnectionTimeout(Duration.ofSeconds(1))    // defaults to 1s
                    .setRetryMaxAttempts(2)                     // defaults to 2
                    .setRetryMaxElapsed(Duration.ofSeconds(10)) // defaults to 10s
                    .build();
            Jwt jwt = jwtVerifier.decode(token);

 */
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }
/*

    public static boolean verifyAud(DecodedJWT jwt) {
        return !AUD.equals(jwt.getAudience().get(0));
    }

    public static boolean isExpired(DecodedJWT jwt) {
        return !new Date().before(jwt.getExpiresAt());
    }

 */


}
