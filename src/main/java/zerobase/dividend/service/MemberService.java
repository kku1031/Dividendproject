package zerobase.dividend.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zerobase.dividend.model.Auth;
import zerobase.dividend.persist.entity.MemberEntity;
import zerobase.dividend.persist.MemberRepository;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                                //findByUsername : Optional에 매핑된 MemberEntity 반환
        return memberRepository.findByUsername(username)
                //orElseThrow : Optional이 벗겨진 MemberEntity -> UserDetails 상속 -> 바로 return
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + username));
    }

    //회원가입 기능
    public MemberEntity register(Auth.SignUp member) {
        boolean exists = memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new RuntimeException("이미 사용 중인 아이디 입니다.");
        }
        //DB에 사용자 비밀 번호 암호화.
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        MemberEntity result = memberRepository.save(member.toEntity());
        return result;
    }

    //로그인 시 중복 ID 검증.
    public MemberEntity authenticate(Auth.SignIn member) {
       return null;
    }
}
