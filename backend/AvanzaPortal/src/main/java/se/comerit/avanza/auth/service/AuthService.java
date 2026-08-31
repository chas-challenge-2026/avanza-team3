package se.comerit.avanza.auth.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import se.comerit.avanza.auth.model.User;
import se.comerit.avanza.auth.repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> authenticate(String email, String password, Model model) {
        // Hash password with MD5 (TODO: upgrade to bcrypt... someday)
        String md5 = md5Hash(password);
        if (md5 == null) {
            model.addAttribute("error", "Internt fel vid autentisering.");
            return Optional.empty();
        }
        return userRepository.findByEmailAndPasswordMd5(email, md5);
    }

    // MD5 helper — lives here because there's nowhere else to put it
    private String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
