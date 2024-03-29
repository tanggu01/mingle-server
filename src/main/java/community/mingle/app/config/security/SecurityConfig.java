
package community.mingle.app.config.security;

import community.mingle.app.config.CustomAuthenticationEntryPoint;
import community.mingle.app.config.TokenHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 스프링 시큐리티 추가로 401 error 난거 해결방법 (이해 필요)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
//public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.csrf().disable();
//    }
//
//
//}


public class SecurityConfig {
//    private final TokenService tokenService;

//    private final CustomUserDetailsService userDetailService;

//    private final JwtHandler jwtHandler;

    private final TokenHelper tokenHelper;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                //http basic 인증방법 비활성화
                .httpBasic().disable()
                //form login 비활성화
                .formLogin().disable()
                //csrf 관련 정책 비활성화
                .csrf().disable()
                //세션 관리정책 설정, 세션을 유지하지 않도록 설정
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
//                .antMatchers("/**").permitAll()
//                .antMatchers("/v3/api-docs/**").permitAll()
//                .antMatchers("/swagger-ui/**").permitAll()
                .antMatchers(HttpMethod.POST, "/auth/**").permitAll()
                .antMatchers(HttpMethod.GET, "/auth/**").permitAll()
                .antMatchers(HttpMethod.GET, "/ping").permitAll()
                .antMatchers(HttpMethod.GET, "/post/report-member").permitAll()
//                .antMatchers(HttpMethod.POST, "/auth/logout").access("@memberGuard.check()")
                .antMatchers(HttpMethod.PATCH, "/auth/fcmtoken").access("@memberGuard.check()")
                .antMatchers("/post/**").access("@memberGuard.check()")
                .antMatchers("/member/**").access("@memberGuard.check()")
                .antMatchers("/comment/**").access("@memberGuard.check()")
                .antMatchers("/home/**").access("@memberGuard.check()")
                .and()
//                .exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler())
//                .and()
                .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(tokenHelper), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("*/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
