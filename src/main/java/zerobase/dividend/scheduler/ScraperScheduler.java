package zerobase.dividend.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.persist.CompanyRepository;
import zerobase.dividend.persist.DividendRepository;
import zerobase.dividend.persist.entity.CompanyEntity;
import zerobase.dividend.persist.entity.DividendEntity;
import zerobase.dividend.scraper.Scraper;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    private final Scraper yahooFinanceScraper;

    //배당금 데이터 일정 주기마다 수행.
    @Scheduled(cron = "0 0 0 * * *")
    public void yahooFinanceScheduling() {
        log.info("스크랩된 스케줄러가 시작됩니다.");
        //저장된 회사 목록을 조회
        List<CompanyEntity> companies = companyRepository.findAll();

        //회사마다 배당금 정보를 새로 스크래핑
        for (var company : companies) {
            log.info("스크랩된 스케줄러가 시작됩니다." + company.getName());
            ScrapedResult scrapedResult = yahooFinanceScraper.scrap(Company.builder()
                    .name(company.getName())
                    .ticker(company.getTicker())
                    .build());

            //스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
            //배당금 리스트 : dividens를 하나씩 순회.
            scrapedResult.getDividends().stream()
                    // 디비든 모델을 디비든 엔티티로 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // 엘리먼트를 하나씩 디비든 레파지토리에 삽입
                    .forEach(e -> {
                        boolean exists = dividendRepository.existsByCompanyIdandDate(e.getCompanyId()
                                , e.getDate());
                        if (!exists) {
                            dividendRepository.save(e);
                        }
                    });

            //연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000); // 3 seconds : 실행중 스레드 멈추게 하는 기능.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); //interrupt : 스레드에 명령 "멈춰!"
            }
        }
    }
}