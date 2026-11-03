package se.comerit.avanza.rebalance.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.comerit.avanza.exception.BusinessRuleException;
import se.comerit.avanza.exception.ResourceConflictException;

/** Signed short-lived snapshot reference: no proposal table or in-memory cache required. */
@Service
public class RebalanceProposalTokenService {
    private static final long TTL_SECONDS = 600;
    private final byte[] secret;

    public RebalanceProposalTokenService(@Value("${jwt.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) throw new IllegalStateException("JWT secret too short for rebalance tokens");
    }

    public String issue(int userId, int accountId, int version, String snapshotHash, List<Integer> ids) {
        String csvIds = ids.stream().sorted().map(String::valueOf).reduce((a,b) -> a + "," + b).orElse("");
        String payload = String.join("|", "rb1", String.valueOf(userId), String.valueOf(accountId),
                String.valueOf(version), String.valueOf(Instant.now().getEpochSecond() + TTL_SECONDS),
                csvIds, snapshotHash);
        String encoded = base64(payload.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + base64(mac(encoded));
    }

    public Proposal verify(String token, int userId, int accountId) {
        try {
            String[] fragments = token.split("\\.", -1);
            if (fragments.length != 2 || token.length() > 4096) throw new IllegalArgumentException();
            if (!MessageDigest.isEqual(mac(fragments[0]), Base64.getUrlDecoder().decode(fragments[1])))
                throw new IllegalArgumentException();
            String payload = new String(Base64.getUrlDecoder().decode(fragments[0]), StandardCharsets.UTF_8);
            String[] parts = payload.split("\\|", -1);
            if (parts.length != 7 || !parts[0].equals("rb1")) throw new IllegalArgumentException();
            if (Integer.parseInt(parts[1]) != userId || Integer.parseInt(parts[2]) != accountId)
                throw new IllegalArgumentException();
            long expires = Long.parseLong(parts[4]);
            if (Instant.now().getEpochSecond() >= expires)
                throw new ResourceConflictException("Rebalance preview expired");
            List<Integer> ids = parts[5].isEmpty() ? List.of() : Arrays.stream(parts[5].split(","))
                    .map(Integer::valueOf).toList();
            if (ids.isEmpty() || ids.size() > 50) throw new IllegalArgumentException();
            return new Proposal(Integer.parseInt(parts[3]), parts[6], ids);
        } catch (ResourceConflictException e) {
            throw e;
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("Invalid rebalance proposal token");
        }
    }

    private byte[] mac(String encoded) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return hmac.doFinal(encoded.getBytes(StandardCharsets.US_ASCII));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign rebalance token", e);
        }
    }

    private static String base64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    public record Proposal(int version, String snapshotHash, List<Integer> instrumentIds) {}
}
