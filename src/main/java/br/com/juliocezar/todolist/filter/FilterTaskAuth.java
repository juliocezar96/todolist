package br.com.juliocezar.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.juliocezar.todolist.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository iUserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if(servletPath.equals("/tasks")){

            var authorization =  request.getHeader("Authorization");

            var authEncoder = authorization.substring("Basic".length()).trim();
            byte[] authDecode = Base64.getDecoder().decode(authEncoder);
            var authString = new String(authDecode);

            String[] credentials = authString.split(":");
            String userName = credentials[0];
            String password = credentials[1];


            var user = this.iUserRepository.findByUserName(userName);

            if(user == null){
                response.sendError(401);
            }else {
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if(passwordVerify.verified){

                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }

            }
        }else {
            filterChain.doFilter(request, response);

        }
    }
}
