package zerobase.dividend;

import model.Company;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import scraper.Scraper;
import scraper.YahooFinanceScraper;

@SpringBootApplication
public class DividendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DividendApplication.class, args);

//        String s = "Hello my names is %s";
//
//        String[] names = {"GREEN", "RED", "YELLOW"};
//
//        for(String name : names) {
//            System.out.println(String.format(s, name));
//        }

        Scraper scraper = new YahooFinanceScraper();
//        var result = scraper.scrap(Company.builder().ticker("O").build());
        var result  = scraper.scrapCompanyByTicker("COKE");
        System.out.println(result);
    }
}
