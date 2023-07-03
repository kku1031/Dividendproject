package zerobase.dividend.config;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    //트라이 인스턴스도 빈으로 초기화 할 수 있도록.
    @Bean
    public Trie<String, String> trie() {
        return new PatriciaTrie<>();
    }


}
