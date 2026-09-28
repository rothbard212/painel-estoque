package com.rothbard212.painel_estoque.service;

import com.rothbard212.painel_estoque.config.JwtUtil;
import com.rothbard212.painel_estoque.dto.LoginRequest;
import com.rothbard212.painel_estoque.dto.LoginResponse;
import com.rothbard212.painel_estoque.model.Usuario;
import com.rothbard212.painel_estoque.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AutenticacaoService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("teste@teste.com");
        usuario.setSenhaHash("hash-bcrypt-qualquer");
        usuario.setNome("Usuario Teste");
    }

    @Test
    void deveRetornarTokenQuandoCredenciaisValidas() {
        when(usuarioRepository.findByEmail("teste@teste.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("123456", "hash-bcrypt-qualquer")).thenReturn(true);
        when(jwtUtil.gerarToken("teste@teste.com")).thenReturn("token-falso-para-teste");

        LoginResponse resposta = service.login(new LoginRequest("teste@teste.com", "123456"));

        assertEquals("token-falso-para-teste", resposta.token());
    }

    @Test
    void deveLancarExcecaoQuandoEmailNaoExiste() {
        when(usuarioRepository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("naoexiste@teste.com", "123456")));

        verify(jwtUtil, never()).gerarToken(anyString());
    }

    @Test
    void deveLancarExcecaoQuandoSenhaIncorreta() {
        when(usuarioRepository.findByEmail("teste@teste.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-errada", "hash-bcrypt-qualquer")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("teste@teste.com", "senha-errada")));

        verify(jwtUtil, never()).gerarToken(anyString());
    }

    @Test
    void mensagemDeErroDeveSerIgualParaEmailInexistenteESenhaErrada() {
        when(usuarioRepository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("teste@teste.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "hash-bcrypt-qualquer")).thenReturn(false);

        BadCredentialsException erroEmail = assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("naoexiste@teste.com", "123456")));
        BadCredentialsException erroSenha = assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("teste@teste.com", "errada")));

        assertEquals(erroEmail.getMessage(), erroSenha.getMessage());
    }
}