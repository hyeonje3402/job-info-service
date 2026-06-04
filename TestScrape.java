import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

public class TestScrape {
    public static void main(String[] args) {
        String url = "https://www.saramin.co.kr/zf_user/jobs/relay/view?view_type=list&rec_idx=53510601&t_ref=jobcategory_recruit&t_ref_content=general#seq=0";
        try {
            System.out.println("Connecting to: " + url);
            Document doc = Jsoup.connect(url)
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .followRedirects(true)
                    .referrer("http://www.google.com")
                    .get();
            
            String cleanHtml = Jsoup.clean(doc.body().html(), Safelist.none());
            String text = Jsoup.parse(cleanHtml).text();
            
            System.out.println("Scraped Length: " + text.length());
            System.out.println("Preview: " + (text.length() > 200 ? text.substring(0, 200) : text));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
