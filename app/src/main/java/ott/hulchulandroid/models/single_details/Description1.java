package ott.hulchulandroid.models.single_details;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Description1 {

    @SerializedName("description_content")
    @Expose
    private String description_content;


    public String getdescription_content() {
        return description_content;
    }

    public void setdescription_content(String description_content) {
        this.description_content = description_content;
    }



}
