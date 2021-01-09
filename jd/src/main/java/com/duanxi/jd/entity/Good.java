package com.duanxi.jd.entity;

/**
 * @author caoduanxi
 * @Date 2021/1/9 14:15
 * @Motto Keep thinking, keep coding!
 */
public class Good {
    private String img;
    private String title;
    private String price;

    public Good() {
    }

    public Good(String img, String title, String price) {
        this.img = img;
        this.title = title;
        this.price = price;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Good{" +
                "img='" + img + '\'' +
                ", title='" + title + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
