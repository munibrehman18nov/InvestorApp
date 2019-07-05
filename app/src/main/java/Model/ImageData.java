package Model;

public class ImageData
{
    private String id;
    private String url;

    public ImageData(String investorId, String url) {
        this.id = investorId;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
