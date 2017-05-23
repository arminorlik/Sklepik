package flex.com.sklepik;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by Armin on 2017-05-16.
 */


public class RowModel extends RealmObject {

    @Required
    private String name;
    @Required
    private Double lattitude, longitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLattitude() {
        return lattitude;
    }

    public void setLattitude(Double lattitude) {
        this.lattitude = lattitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public RowModel(String name, Double lattitude, Double longitude) {
        this.name = name;
        this.lattitude = lattitude;
        this.longitude = longitude;
    }

    public RowModel() {
    }
}
