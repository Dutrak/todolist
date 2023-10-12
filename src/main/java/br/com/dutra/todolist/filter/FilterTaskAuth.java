package br.com.dutra.todolist.filter;

import java.io.IOException;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.dutra.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;  
        

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    
    //Pegar a autenticação (usuario e senha)
    var authorization = request.getHeader("Authorization");
    var authEncoded = authorization.substring("Basic".length()).trim();
    //Converte a autenticação para bytecode e depois para string
    byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
    var authString = new String(authDecoded);
    // Cria um array a partir da string (username:password)
    String[] credentials = authString.split(":");
    String username = credentials[0];
    String password = credentials[1];

    // Validar usuario
    var user = this.userRepository.findByUsername(username);
    if (user == null) {
      response.sendError(401);
    } else {
      //Validar Senha
      var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
      if (passwordVerify.verified) {
        filterChain.doFilter(request, response);
      } else {
        response.sendError(401);
      }
    }
  }

}
