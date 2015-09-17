package CLog;

import CLog.services.MongoUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@SpringBootApplication
@EnableAsync
public class CLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(CLogApplication.class, args);
    }

    @Configuration
    @EnableAsync
    @EnableScheduling
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    protected static class SecurityConfiguration extends WebSecurityConfigurerAdapter {


        @Autowired
        private MongoUserDetailsService mongoUserDetailsService;

        @Autowired
        protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(mongoUserDetailsService).and().inMemoryAuthentication()
                    .withUser("user").password("password").roles("USER").and()
                    .withUser("admin").password("admin").roles("ADMIN");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .httpBasic()
            .and()
                .authorizeRequests()
                    .antMatchers("/", "/**.js", "/**/**.js", "/**/**.html", "/**.css", "/**/**.css").permitAll().anyRequest()
                    .authenticated()
            .and()
                    .csrf().requireCsrfProtectionMatcher(csrfRequestMatcher).and().csrf()
                    .csrfTokenRepository(csrfTokenRepository()).and()
                    .addFilterAfter(csrfHeaderFilter(), CsrfFilter.class);
        }

        private Filter csrfHeaderFilter() {
            return new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request,
                                                HttpServletResponse response, FilterChain filterChain)
                        throws ServletException, IOException {
                    CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class
                            .getName());
                    if (csrf != null) {
                        Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
                        String token = csrf.getToken();
                        if (cookie == null || token != null
                                && !token.equals(cookie.getValue())) {
                            cookie = new Cookie("XSRF-TOKEN", token);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                        }
                    }
                    filterChain.doFilter(request, response);
                }
            };
        } // Filter zum Umgang mit AngularJS XSRF Token

        private CsrfTokenRepository csrfTokenRepository() {
            HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
            repository.setHeaderName("X-XSRF-TOKEN");
            return repository;
        }

        private RequestMatcher csrfRequestMatcher = new RequestMatcher() {

            // Always allow the HTTP GET method
            // private Pattern allowedMethods = Pattern.compile("^GET$");

            // Disable CSFR protection on the following urls:
            private AntPathRequestMatcher[] requestMatchers = {
                    new AntPathRequestMatcher("/api/key"),
                    new AntPathRequestMatcher("/"),
                    new AntPathRequestMatcher("/**.js"),
                    new AntPathRequestMatcher("/**/**.js"),
                    new AntPathRequestMatcher("/**/**.html"),
                    new AntPathRequestMatcher("/**.css"),
                    new AntPathRequestMatcher("/**/**.css")
            };

            @Override
            public boolean matches(HttpServletRequest request) {
                // If the request match one url the CSFR protection will be disabled
                for (AntPathRequestMatcher rm : requestMatchers) {
                    if (rm.matches(request)) { return false; }
                }
                return true;
            } // method matches

        }; // Sorgt dafür, dass "/api/key" ohne CSRF Protection zugänglich ist


    }


}
