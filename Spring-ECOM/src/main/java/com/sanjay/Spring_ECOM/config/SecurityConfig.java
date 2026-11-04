package com.sanjay.Spring_ECOM.config;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.*;
import java.util.List;
@Configuration public class SecurityConfig {
 @Bean SecurityFilterChain security(HttpSecurity h,com.sanjay.Spring_ECOM.repo.UserRepo users,@Value("${app.admin-username}") String admin,@Value("${app.admin-password}") String password) throws Exception {
  var encoder=new BCryptPasswordEncoder(12); String hash=encoder.encode(password);
  UserDetailsService lookup=name->{if(name.equals(admin))return User.withUsername(admin).password(hash).roles("ADMIN").build();var u=users.findByUsername(name);if(u==null)throw new UsernameNotFoundException("Unknown user");return User.withUsername(name).password(u.getPassword()).roles("CUSTOMER").build();};
  var provider=new DaoAuthenticationProvider(lookup);provider.setPasswordEncoder(encoder);h.authenticationProvider(provider);
  h.csrf(c->c.disable()).cors(org.springframework.security.config.Customizer.withDefaults());
  h.addFilterBefore(new ApiWriteFilter(),BasicAuthenticationFilter.class);
  h.authorizeHttpRequests(a->a.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
   .requestMatchers(HttpMethod.GET,"/actuator/health","/actuator/health/**","/api/products","/api/products/search","/api/product/*","/api/product/*/image").permitAll()
   .requestMatchers(HttpMethod.POST,"/api/register").permitAll()
   .requestMatchers("/api/me","/api/orders/place").authenticated()
   .requestMatchers(HttpMethod.GET,"/api/orders").authenticated()
   .anyRequest().hasRole("ADMIN"));
  h.httpBasic(c->c.authenticationEntryPoint((req,res,e)->res.sendError(401)));h.sessionManagement(c->c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));return h.build();
 }
 @Bean CorsConfigurationSource cors(@Value("${app.frontend-origin}") String origin){
  if(!origin.matches("https://[a-zA-Z0-9-]+\\.vercel\\.app" )&&!origin.equals("http://localhost:3000"))throw new IllegalArgumentException("Exact Vercel origin required");
  var c=new CorsConfiguration();c.setAllowedOrigins(List.of(origin));c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));var s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;
 }
}
