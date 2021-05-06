package org.keycloak.credential;

import org.jboss.logging.Logger;
import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.cache.OnUserCache;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by nickpack on 15/08/2017.
 */

public abstract class SmsCredentialProvider implements CredentialProvider, CredentialInputValidator, CredentialInputUpdater, OnUserCache {

    // User credentials (used to persist the sent sms code + expiration time cluster wide)
    public static final String USR_CRED_MDL_SMS_CODE = "sms-auth.code";

    public static final String USR_CRED_MDL_SMS_EXP_TIME = "sms-auth.exp-time";

    protected KeycloakSession session;

    public abstract String getCacheKey();

    private static Logger logger = Logger.getLogger(SmsCredentialProvider.class);

    public SmsCredentialProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public CredentialModel createCredential(RealmModel realm, UserModel user, CredentialModel credentialModel) {
        return getCredentialStore().createCredential(realm, user, credentialModel);
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {
        return getCredentialStore().removeStoredCredential(realm, user, credentialId);
    }

    @Override
    public CredentialModel getCredentialFromModel(CredentialModel model) {
        return model;
    }

    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext metadataContext) {
        return CredentialTypeMetadata.builder()
                .type(getType())
                .category(CredentialTypeMetadata.Category.TWO_FACTOR)
                .displayName(SmsCodeCredentialProviderFactory.PROVIDER_ID)
                .helpText("sms-text")
                .createAction(SmsCodeCredentialProviderFactory.PROVIDER_ID)
                .removeable(false)
                .build(session);
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        if (!getType().equals(input.getType())) return false;
        if (!(input instanceof UserCredentialModel)) return false;
        UserCredentialModel credInput = (UserCredentialModel) input;
        var count = session.userCredentialManager().getStoredCredentialsByTypeStream(realm, user, getType()).count();
        if (count > 1) {
            logger.warnf("More than one credential exist in store for type %s", getType());
        }
        session.userCredentialManager().getStoredCredentialsByTypeStream(realm, user, getType())
                .findFirst()
                .ifPresentOrElse(
                        credentialModel -> {
                            credentialModel.setSecretData(credInput.getValue());
                            logger.debugf("Updating code in credential store %s", credentialModel);
                            session.userCredentialManager().updateCredential(realm, user, credentialModel);
                        },
                        () -> {
                            var secret = new CredentialModel();
                            secret.setType(getType());
                            secret.setSecretData(credInput.getValue());
                            secret.setCreatedDate(Time.currentTimeMillis());
                            logger.debugf("Creating code in credential store %s", secret);
                            session.userCredentialManager().createCredential(realm, user, secret);
                        });

        session.userCache().evict(realm, user);
        return true;
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        if (!getType().equals(credentialType)) return;
        session.userCredentialManager().disableCredentialType(realm, user, credentialType);
        session.userCache().evict(realm, user);

    }

    @Override
    public Set<String> getDisableableCredentialTypes(RealmModel realm, UserModel user) {
        return session.userCredentialManager().getStoredCredentialsByTypeStream(realm, user, getType())
                .map(credentialModel -> getType())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return getType().equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        if (!getType().equals(credentialType)) return false;
        return getSecret(realm, user) != null;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!getType().equals(input.getType())) return false;
        if (!(input instanceof UserCredentialModel)) return false;

        String secret = getSecret(realm, user).getValue();

        return secret != null && ((UserCredentialModel)input).getValue().equals(secret);
    }

    @Override
    public void onCache(RealmModel realm, CachedUserModel user, UserModel delegate) {
        session.userCredentialManager().getStoredCredentialsByTypeStream(realm, user, getType())
                .findFirst()
                .ifPresent(credentialModel -> user.getCachedWith().put(getCacheKey(), credentialModel));
    }

    private CredentialModel getSecret(RealmModel realm, UserModel user) {
        return ((user instanceof CachedUserModel)
                ? Optional.ofNullable((CredentialModel) ((CachedUserModel) user).getCachedWith().get(getCacheKey()))
                : session.userCredentialManager().getStoredCredentialsByTypeStream(realm, user, getType()).findFirst())
                .orElse(null);
    }

    private UserCredentialStore getCredentialStore() {
        return session.userCredentialManager();
    }
}
