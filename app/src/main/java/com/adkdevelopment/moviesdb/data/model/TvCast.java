/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016. Dmytro Karataiev
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.adkdevelopment.moviesdb.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by karataev on 6/14/16.
 */
public class TvCast {

    @SerializedName("character")
    @Expose
    private String character;
    @SerializedName("credit_id")
    @Expose
    private String creditId;
    @SerializedName("id")
    @Expose
    private long id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("profile_path")
    @Expose
    private String profilePath;
    @SerializedName("order")
    @Expose
    private long order;

    /**
     *
     * @return
     * The character
     */
    public String getCharacter() {
        return character;
    }

    /**
     *
     * @param character
     * The character
     */
    public void setCharacter(String character) {
        this.character = character;
    }

    /**
     *
     * @return
     * The creditId
     */
    public String getCreditId() {
        return creditId;
    }

    /**
     *
     * @param creditId
     * The credit_id
     */
    public void setCreditId(String creditId) {
        this.creditId = creditId;
    }

    /**
     *
     * @return
     * The id
     */
    public long getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(long id) {
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
     * The profilePath
     */
    public String getProfilePath() {
        return profilePath;
    }

    /**
     *
     * @param profilePath
     * The profile_path
     */
    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }

    /**
     *
     * @return
     * The order
     */
    public long getOrder() {
        return order;
    }

    /**
     *
     * @param order
     * The order
     */
    public void setOrder(long order) {
        this.order = order;
    }

}