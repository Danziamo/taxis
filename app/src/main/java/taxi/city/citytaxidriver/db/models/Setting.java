package taxi.city.citytaxidriver.db.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.text.SimpleDateFormat;

/**
 * Created by mbt on 8/25/15.
 */

@Table(name = "settings")
public class Setting extends Model {

    public static final String TARIFFS_LAST_UPDATE_TIME_NAME = "tariffs_last_update_time";
    public static final String BRANDS_LAST_UPDATE_TIME_NAME = "brands_last_update_time";
    public static final String BRAND_MODELS_LAST_UPDATE_TIME_NAME_PREFIX = "brand_models_last_update_time_";

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    public static Setting get(String name){
        return new Select()
                .from(Setting.class)
                .where("name = ?", name)
                .executeSingle();
    }

    public static String getValue(String name){
        Setting setting = get(name);
        if(setting == null){
            return "";
        }
        return setting.toString();
    }

    public static long getLongValue(String name){
        String dateString = getValue(name);
        try {
            return Long.parseLong(dateString);
        }catch(Exception e){
            return 0;
        }
    }

    public static void saveValue(String name, String value){
        Setting setting = get(name);
        if(setting == null){
            setting = new Setting();
        }
        setting.setName(name);
        setting.setValue(value);
        setting.save();
    }

    public static void saveValue(String name, long value){
        saveValue(name, Long.toString(value));
    }



}
