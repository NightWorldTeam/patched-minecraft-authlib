package com.mojang.authlib.yggdrasil;

import com.mojang.authlib.Environment;
import com.mojang.authlib.EnvironmentParser;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Proxy;
import java.net.URL;

public class YggdrasilAuthenticationService extends HttpAuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(YggdrasilAuthenticationService.class);

    private final Environment environment;
    private final ServicesKeySet servicesKeySet;

    static {
        String defaultAgent = System.getProperty("http.agent");
        if (defaultAgent == null || defaultAgent.toLowerCase().contains("java")) {
            System.setProperty("http.agent", "Mozilla/5.0");
        }

        try {
            domainTrust();
        } catch (Exception e) { }
    }

    public static void domainTrust() throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }

    public YggdrasilAuthenticationService(final Proxy proxy) {
        this(proxy, determineEnvironment());
    }

    public YggdrasilAuthenticationService(final Proxy proxy, final Environment environment) {
        super(proxy);
        this.environment = environment;
        LOGGER.info("Environment: {}", environment);

        final MinecraftClient client = MinecraftClient.unauthenticated(proxy);
        final URL publicKeySetUrl = HttpAuthenticationService.constantURL(environment.servicesHost() + "/publickeys");
        servicesKeySet = YggdrasilServicesKeyInfo.get(publicKeySetUrl, client);
    }

    private static Environment determineEnvironment() {
        return EnvironmentParser
                   .getEnvironmentFromProperties()
                   .orElse(YggdrasilEnvironment.PROD.getEnvironment());
    }

    @Override
    public MinecraftSessionService createMinecraftSessionService() {
        return new YggdrasilMinecraftSessionService(servicesKeySet, getProxy(), environment);
    }

    @Override
    public GameProfileRepository createProfileRepository() {
        return new YggdrasilGameProfileRepository(getProxy(), environment);
    }

    public UserApiService createUserApiService(final String accessToken) {
        return new YggdrasilUserApiService(accessToken, getProxy(), environment);
    }

    public ServicesKeySet getServicesKeySet() {
        return servicesKeySet;
    }
}
