package upariscommonmarkjava.buildsite;

public class BuildSiteMain {
    public static void main(String[] args) {
        try
        {
            DirectoryMd correct_site = DirectoryMd.open("buildsite/src/test/resources/minimal");
            final DirectoryHtml correct_html = correct_site.generateHtml();
            correct_html.isSimilare(correct_site);
        }
        catch(SiteFormatException e)
        {
            System.out.println(e);
        }
    }
}
