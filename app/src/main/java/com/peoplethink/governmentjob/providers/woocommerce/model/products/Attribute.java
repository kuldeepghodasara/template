package com.peoplethink.governmentjob.providers.woocommerce.model.products;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Septian A. Fujianto on 10/31/2016.
 */

public class Attribute implements Serializable {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("option")
    @Expose
    private String option;
    @SerializedName("options")
    @Expose
    private List<String> options;
    @SerializedName("name")
    @Expose
    private String name;

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The option
     */
    public String getOption() {
        return option;
    }

    /**
     *
     * @param option
     * The option
     */
    public void setOption(String option) {
        this.option = option;
    }

    /**
     *
     * @return
     * The options
     */
    public List<String> getOptions() {
        return options;
    }

    /**
     *
     * @param options
     * The option
     */
    public void setOptions(List<String> options) {
        this.options = options;
    }

    public Attribute(){

    }
}
