package zerobase.dividend.service;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.persist.CompanyRepository;
import zerobase.dividend.persist.DividendRepository;
import zerobase.dividend.persist.entity.CompanyEntity;
import zerobase.dividend.persist.entity.DividendEntity;
import zerobase.dividend.scraper.Scraper;

import java.util.List;
import java.util.stream.Collectors;

@Service            //싱글톤 관리 : 프로그램 실행되는 동안, instance는 하나만 사용됨
@AllArgsConstructor //Bean이 생성될 때 사용
public class CompanyService {

    private final Trie tries;
    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;


    //스크랩한 데이터 저장
    public Company save(String ticker) {
        boolean exists = this.companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new RuntimeException("already exists ticker -> " + ticker);
        }
        return this.storeCompanyAndDividend(ticker);
    }

    //저장한 회사의 instance 정보, DB에 저장을 하지 않은 회사에만 저장.
    private Company storeCompanyAndDividend(String ticker) {
        // ticker를 기준으로 회사를 스크래핑
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new RuntimeException("failed to scrap ticker -> " + ticker);
        }

        //해당 회사가 존재할 경우, 회사의 배당금 정보 스크래핑.
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        //스크래핑 결과는 Entity타입으로 저장이 되어야함.
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()   //dividend item 하나가 e -> foreach 처럼
                .map(e -> new DividendEntity(companyEntity.getId(), e)) //collection element(요소)들 다른 값으로 매핑해야할 때.
                .collect(Collectors.toList());
        this.dividendRepository.saveAll(dividendEntities);
        return company;
    }

    //회사 리스트 조회
    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        //실직적으로 회사 정보는 수천 수만가지니까 필요한 부분만 노출 시킬 필요성있음 -> Pageable 기능 구현
        return this.companyRepository.findAll(pageable);
    }

    //자동 완성(데이터 저장) - trie
    public void addAutocompleteKeyword(String keyword) {
        tries.put(keyword, null); //아파치에서 구현된 tri 추가기능 : value값 넣을 게 없고 딱 keyword만 받아오면됨.
    }

    //자동 완성 회사명 조회 - trie
    public List<String> autoComplete(String keyword) {
        return (List<String>) tries.prefixMap(keyword).keySet()
                .stream()
                .limit(10)              //회사 정보가 많아질 수 있으므로 10개만 반환.
                .collect(Collectors.toList());
    }

    //자동 완성 키워드 삭제 - trie
    public void deleteAutocompleteKeyword(String keyword) {
        tries.remove(keyword);
    }

    //자동 완성 대 소문자 구분 X - like 연산자
    public List<String> getCompanyNamesByKeyword(String keyword) {
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities = companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
        return companyEntities.stream()
                                .map(e -> e.getName())
                                .collect(Collectors.toList());
    }
}
