package itesm.mx.movilidad_reportedeproblemas.Models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

//////////////////////////////////////////////////////////
//Clase: Report
// Descripción: Modelo para levantar reportes.
// Autor: Armando Aguiar y Juan Carlos Guzman
// Fecha de creación: 03/11/2017
// Fecha de última modificación: 23/11/2017
//////////////////////////////////////////////////////////

public class Report implements Parcelable {
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_IN_PROCESS = 2;
    public static final int STATUS_SUCCESS = 3;
    public static final int STATUS_FAILURE = 4;

    private long id;
    private long categoryId;
    private String userId;
    private double longitude;
    private double latitude;
    private Date date;
    private int status;

    private ArrayList<Comment> comments = new ArrayList<>();
    private ArrayList<Image> images = new ArrayList<>();
    private ArrayList<Voicenote> voicenotes = new ArrayList<>();
    private ArrayList<UploadedFile> files = new ArrayList<>();
    private Category category;

    public Report() {};

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public ArrayList<Voicenote> getVoicenotes() {
        return voicenotes;
    }

    public ArrayList<UploadedFile> getFiles() {
        return files;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void log() {
        Log.i("LogReport", "Report " + getId() + ", User: " + getUserId());
        Log.i("LogReport", getDate().toString());
        Log.i("LogReport", String.format("(%f, %f) %s", this.getLatitude(), this.getLongitude(), this.getCategoryId()));
        Log.i("LogReport", "Comments:");
        for (Comment comment : this.getComments()) {
            Log.i("LogReport", "\t" + comment.getBody());
        }

        Log.i("LogReport", "Bitmaps:");
        for (Image image : this.getImages()) {
            Log.i("LogReport", "\t" + image.getBytes().length + " bytes");
        }
        Log.i("LogReport", "Audios:");
        for (Voicenote voicenote : this.getVoicenotes()) {
            Log.i("LogReport", "\t" + voicenote.getBytes().length + " bytes");
        }

        Log.i("LogReport", "Files:");
        for (UploadedFile file : this.getFiles()) {
            Log.i("LogReport",  "\t" + file.getName() + ": " + file.getBytes().length + " bytes");
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(userId);
        parcel.writeLong(categoryId);
        parcel.writeParcelable(category, i);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        parcel.writeString(sdf.format(date));

        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeTypedList(comments);
        parcel.writeTypedList(voicenotes);
        parcel.writeTypedList(images);
        parcel.writeTypedList(files);
        parcel.writeInt(status);
    }

    protected Report(Parcel in) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        id = in.readLong();
        userId = in.readString();
        categoryId = in.readLong();
        category = in.readParcelable(Category.class.getClassLoader());
        try {
            date = sdf.parse(in.readString());
        } catch (ParseException e) {
            date = new Date();
        }
        latitude = in.readDouble();
        longitude = in.readDouble();
        in.readTypedList(comments, Comment.CREATOR);
        in.readTypedList(voicenotes, Voicenote.CREATOR);
        in.readTypedList(images, Image.CREATOR);
        in.readTypedList(files, UploadedFile.CREATOR);
        status = in.readInt();

    }

    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };
}
