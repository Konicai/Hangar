package io.papermc.hangar.service.api;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.auth.ApiKeyDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.auth.ApiSession;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.auth.ApiKeyTable;
import io.papermc.hangar.service.TokenService;
import io.papermc.hangar.util.CryptoUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
public class APIAuthenticationService extends HangarComponent {

    private static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(" + UUID_REGEX + ").(" + UUID_REGEX + ")");

    private final UserDAO userDAO;
    private final ApiKeyDAO apiKeyDAO;
    private final TokenService tokenService;

    @Autowired
    public APIAuthenticationService(UserDAO userDAO, ApiKeyDAO apiKeyDAO, TokenService tokenService) {
        this.userDAO = userDAO;
        this.apiKeyDAO = apiKeyDAO;
        this.tokenService = tokenService;
    }

    public ApiSession createJWTForApiKey(String apiKey) {
        if (!API_KEY_PATTERN.matcher(apiKey).matches()) {
            throw new HangarApiException("Badly formatted API Key");
        }
        String identifier = apiKey.split("\\.")[0];
        String token = apiKey.split("\\.")[1];
        String hashedToken = CryptoUtils.hmacSha256(config.security.tokenSecret(), token.getBytes(StandardCharsets.UTF_8));
        ApiKeyTable apiKeyTable = apiKeyDAO.findApiKey(identifier, hashedToken);
        if (apiKeyTable == null) {
            throw new HangarApiException("No valid API Key found");
        }
        UserTable userTable = userDAO.getUserTable(apiKeyTable.getOwnerId());
        String jwt = tokenService.expiring(userTable, apiKeyTable.getPermissions(), identifier);
        return new ApiSession(jwt, config.security.refreshTokenExpiry().toSeconds());
    }
}
