package scraper;

import model.Company;
import model.Dividend;
import model.ScrapedResult;
import model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class YahooFinanceScraper implements Scraper{

    //상수값 변수 명 : 대문자.
    //"https://finance.yahoo.com/quote/COKE/history?period1=99100800&period2=1688169600&interval=1mo"
    //ticker 부분과 시작, 끝 부분 %s, %d(format 타입에 따라 달라짐)로 바꿔줌 -> 받은 회사와, 호출 날짜에 따라 실행 날짜가 달라질수 있으므로 유동적으로 처리.
    private static final String URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";

    private static final long START_TIME = 86400; //1일(60초 * 60분 * 24시간)

    //전체 스크랩
    @Override
    public ScrapedResult scrap(Company company) {
        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);

        try {
            //1970년 이후 현재 시간.
            long now = System.currentTimeMillis() / 1000;

            //String,format(치환될 문자열, 치환될 값)  START_TIME : 시작시간, now : 끝 시간.
            String url = String.format(URL, company.getTicker(), START_TIME, now);
            Connection connection = Jsoup.connect(url); //커넥션
            Document document = connection.get();   //결과값

            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tableEle = parsingDivs.get(0); // table 전체

            Element tbody = tableEle.children().get(1);

            List<Dividend> dividends = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                //공백 기준 split, . 제거. 스크랩해온 숫자값 년도 월일로
                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",", ""));
                int year = Integer.valueOf(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }

                dividends.add(Dividend.builder()
                        .date(LocalDateTime.of(year, month, day, 0, 0))
                        .dividend(dividend)
                        .build());
            }
            scrapResult.setDividends(dividends);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return scrapResult;
    }

    //회사명 스크랩
    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Document document = Jsoup.connect(url).get();
            Element titleEle = document.getElementsByTag("h1").get(0);
            String title = titleEle.text().split(" - ")[1].trim();
            // 배열 abc - def - xzy => -기준 [1]번째 배열 가져옴. : def

            return Company.builder()
                            .ticker(ticker)
                            .name(title)
                            .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
