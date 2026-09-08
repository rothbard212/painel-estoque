package com.rothbard212.painel_estoque.service;

import com.rothbard212.painel_estoque.config.JwtUtil;
import com.rothbard212.painel_estoque.dto.LoginRequest;
import com.rothbard212.painel_estoque.dto.LoginResponse;
import com.rothbard212.painel_estoque.model.Usuario;
import com.rothbard212.painel_estoque.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AutenticacaoService(UsuarioRepository usuarioRepository,
                               PasswordEncoder passwordEncoder,
                               JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new BadCredentialsException("Email ou senha inválidos");
        }

        String token = jwtUtil.gerarToken(usuario.getEmail());
        return new LoginResponse(token);
    }
}
