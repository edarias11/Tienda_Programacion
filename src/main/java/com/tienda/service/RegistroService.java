package com.tienda.service;

import com.tienda.domain.Usuario;
import jakarta.mail.MessagingException;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RegistroService {

    private final CorreoService correoService;
    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    @Value("${server.port:80}")
    private String puerto;

    public RegistroService(CorreoService correoService,
                           UsuarioService usuarioService,
                           MessageSource messageSource) {
        this.correoService = correoService;
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    // Este método se usa en el enlace enviado por correo
    public Model activar(Model model, String username, String clave) {
        Optional<Usuario> usuario = usuarioService.getUsuarioPorUsernameYPassword(username, clave);

        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
        } else {
            model.addAttribute("titulo",
                    messageSource.getMessage("registro.activar", null, Locale.getDefault()));
            model.addAttribute("mensaje",
                    messageSource.getMessage("registro.activar.error", null, Locale.getDefault()));
        }
        return model;
    }

    // Activa finalmente el usuario
    public void activar(Usuario usuario, MultipartFile imagenFile) {
        usuario.setActivo(true);
        usuarioService.save(usuario, imagenFile, true);
    }

    // Crea usuario y envía correo de activación
    public Model crearUsuario(Model model, Usuario usuario) {
        String mensaje;
        try {
            String clave = generarClave();
            usuario.setPassword(clave);
            usuario.setActivo(false);

            usuarioService.save(usuario, null, false);
            enviarCorreoActivar(usuario, clave);

            mensaje = String.format(
                    messageSource.getMessage(
                            "registro.mensaje.activacion.ok", null, Locale.getDefault()),
                    usuario.getCorreo());

        } catch (MessagingException | NoSuchMessageException e) {
            mensaje = String.format(
                    messageSource.getMessage(
                            "registro.mensaje.usuario.o.correo", null, Locale.getDefault()),
                    usuario.getUsername(), usuario.getCorreo());
        }

        model.addAttribute("titulo",
                messageSource.getMessage("registro.activar", null, Locale.getDefault()));
        model.addAttribute("mensaje", mensaje);
        return model;
    }

    // Recordar usuario (reset de contraseña)
    public Model recordarUsuario(Model model, Usuario usuario) throws MessagingException {
        String mensaje;
        Optional<Usuario> usuarioOpt =
                usuarioService.getUsuarioPorUsernameOCorreo(usuario.getUsername(), usuario.getCorreo());

        if (usuarioOpt.isPresent()) {
            usuario = usuarioOpt.get();
            String clave = generarClave();
            usuario.setPassword(clave);
            usuario.setActivo(false);

            usuarioService.save(usuario, null, false);
            enviarCorreoRecordar(usuario, clave);

            mensaje = String.format(
                    messageSource.getMessage(
                            "registro.mensaje.recordar.ok", null, Locale.getDefault()),
                    usuario.getCorreo());
        } else {
            mensaje = String.format(
                    messageSource.getMessage(
                            "registro.mensaje.usuario.o.correo", null, Locale.getDefault()),
                    usuario.getUsername(), usuario.getCorreo());
        }

        model.addAttribute("titulo",
                messageSource.getMessage("registro.activar", null, Locale.getDefault()));
        model.addAttribute("mensaje", mensaje);
        return model;
    }

    // ===== MÉTODOS PRIVADOS =====

    private String generarClave() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder clave = new StringBuilder();

        for (int i = 0; i < 40; i++) {
            clave.append(caracteres.charAt((int) (Math.random() * caracteres.length())));
        }
        return clave.toString();
    }

    private String obtenerServidor() {
        return "http://localhost:" + puerto;
    }

    private void enviarCorreoActivar(Usuario usuario, String clave) throws MessagingException {
        String mensaje = messageSource.getMessage(
                "registro.correo.activar", null, Locale.getDefault());

        mensaje = String.format(
                mensaje,
                usuario.getNombre(),
                usuario.getApellidos(),
                obtenerServidor(),
                usuario.getUsername(),
                clave
        );

        String asunto = messageSource.getMessage(
                "registro.mensaje.activacion", null, Locale.getDefault());

        correoService.enviarCorreoHtml(usuario.getCorreo(), asunto, mensaje);
    }

    private void enviarCorreoRecordar(Usuario usuario, String clave) throws MessagingException {
        String mensaje = messageSource.getMessage(
                "registro.correo.recordar", null, Locale.getDefault());

        mensaje = String.format(
                mensaje,
                usuario.getNombre(),
                usuario.getApellidos(),
                obtenerServidor(),
                usuario.getUsername(),
                clave
        );

        String asunto = messageSource.getMessage(
                "registro.mensaje.recordar", null, Locale.getDefault());

        correoService.enviarCorreoHtml(usuario.getCorreo(), asunto, mensaje);
    }
}