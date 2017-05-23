
package flex.com.sklepik.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Post {

    @SerializedName("nazwa")
    @Expose
    private String nazwa;
    @SerializedName("dlug")
    @Expose
    private String dlug;
    @SerializedName("szer")
    @Expose
    private String szer;

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getDlug() {
        return dlug;
    }

    public void setDlug(String dlug) {
        this.dlug = dlug;
    }

    public String getSzer() {
        return szer;
    }

    public void setSzer(String szer) {
        this.szer = szer;
    }

}
