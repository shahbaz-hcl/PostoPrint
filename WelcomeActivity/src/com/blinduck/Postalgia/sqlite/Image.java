package com.blinduck.Postalgia.sqlite;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 6/23/13
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class Image  {


    private int _id;
    private int session;
    private int image_id;
    private String orig_image_loc;
    private String new_image_loc;
    private int upload_status;
    private int edit_status;
    private  int quantity;
    private int image_type;
    private int orientation;

    public void changeType(int newImageType) {
        this.setImageType(newImageType);
    }


    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getImageType() {
        return image_type;
    }

    public void setImageType(int type) {
        this.image_type = type;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getSession() {
        return session;
    }

    public void setSession(int session) {
        this.session = session;
    }

    public int getImage_id() {
        return image_id;
    }

    public void setImage_id(int image_id) {
        this.image_id = image_id;
    }

    public String getOrig_image_loc() {
        return orig_image_loc;
    }

    public void setOrig_image_loc(String orig_image_loc) {
        this.orig_image_loc = orig_image_loc;
    }

    public String getNew_image_loc() {
        return new_image_loc;
    }

    public void setNew_image_loc(String new_image_loc) {
        this.new_image_loc = new_image_loc;
    }

    public int getUpload_status() {
        return upload_status;
    }

    public void setUpload_status(int upload_status) {
        this.upload_status = upload_status;
    }

    public int getEdit_status() {
        return edit_status;
    }

    public void setEdit_status(int edit_status) {
        this.edit_status = edit_status;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }



}
