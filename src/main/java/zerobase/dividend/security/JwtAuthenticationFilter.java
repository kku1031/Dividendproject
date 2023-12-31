package zerobase.dividend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    //Http 프로토콜 Header : 토큰 포함 -> 어떤키(키 : Authorization)를 기준으로 토큰 주고 받을지 정함.
    public static final String TOKEN_HEADER = "Authorization";
    //인증 타입 : 키를 기준으로 값을 가져오고 JWT 토큰에서는 가져온 토큰 값 앞에 Bearer를 붙여 Bearer를 ????형식으로 표시
    public static final String TOKEN_PREFIX = "Bearer ";

    //토큰의 유효성 검증.
    private final TokenProvider tokenProvider;

    //사용자요청(Request) -> Filter -> Servlet -> Interceptor -> AOP 레이저 -> Controller
    //요청에 토큰이 포함되어 있는지 유효한지 안 유효한지 필터에서 확인
    //OncePerRequestFilter(한요청당 한번 필터 실행) 상속 시 구현
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveTokenFromRequest(request);
           //토큰 유효성 검증
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            //토큰 인증 정보 : 사용자의 정보와 권한 정보 포함. -> getContext()에 담음.
            Authentication auth = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        //필터가 연속적으로 실행될 수 있도록.
        filterChain.doFilter(request, response);
    }

    //Request의 HttpProtocol의 Header로부터 token 값을 꺼내오는 메소드
    private String resolveTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);

        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
