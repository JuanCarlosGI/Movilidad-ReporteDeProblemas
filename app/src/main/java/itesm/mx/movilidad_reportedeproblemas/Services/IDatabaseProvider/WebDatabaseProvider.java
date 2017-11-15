package itesm.mx.movilidad_reportedeproblemas.Services.IDatabaseProvider;

import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;

import itesm.mx.movilidad_reportedeproblemas.Models.Category;
import itesm.mx.movilidad_reportedeproblemas.Models.Comment;
import itesm.mx.movilidad_reportedeproblemas.Models.Image;
import itesm.mx.movilidad_reportedeproblemas.Models.Report;
import itesm.mx.movilidad_reportedeproblemas.Models.UploadedFile;
import itesm.mx.movilidad_reportedeproblemas.Models.Voicenote;
import itesm.mx.movilidad_reportedeproblemas.Services.IJsonParser.FileJsonParser;
import itesm.mx.movilidad_reportedeproblemas.Services.IJsonParser.IJsonParser;
import itesm.mx.movilidad_reportedeproblemas.Services.IWebsiteReader.IWebsiteReader;
import itesm.mx.movilidad_reportedeproblemas.Services.IWebsiteReader.WebsiteReader;
import itesm.mx.movilidad_reportedeproblemas.Services.IJsonParser.CategoryJsonParser;
import itesm.mx.movilidad_reportedeproblemas.Services.IJsonParser.CommentJsonParser;
import itesm.mx.movilidad_reportedeproblemas.Services.IJsonParser.ReportJsonParser;

/**
 * Created by juanc on 11/7/2017.
 */

public class WebDatabaseProvider implements IDatabaseProvider {
    private final static String BASE_URL = "www.reportesmovilidadtec.x10host.com/service.php";
    private final static String GET_REPORT = "getReport";
    private final static String GET_REPORTS = "getReports";
    private final static String DELETE_REPORT = "deleteReport";
    private final static String ADD_REPORT = "addReport";
    private final static String GET_CATEOGRY = "getCategory";
    private final static String GET_CATEGORIES = "getCategories";
    private final static String DELETE_CATEGORY = "deleteCategory";
    private final static String ADD_CATEGORY = "addCategory";
    private final static String GET_REPORTS_FOR_USER = "reportsForUser";
    private final static String IS_ADMIN = "isAdmin";
    private final static String MAKE_ADMIN = "makeAdmin";
    private final static String REMOVE_ADMIN = "removeAdmin";
    private final static String COMMENTS_FOR_REPORT = "commentsForReport";

    private IWebsiteReader _reader = new WebsiteReader();
    private IJsonParser<Report> _reportParser = new ReportJsonParser();
    private IJsonParser<Category> _categoryParser = new CategoryJsonParser();
    private IJsonParser<Comment> _commentParser = new CommentJsonParser();
    private IJsonParser<UploadedFile> _fileParser = new FileJsonParser();
    private IJsonParser<Image> _imageParser;
    private IJsonParser<Voicenote> _voicenoteParser;

    private Uri.Builder baseBuilder() {
        return new Uri.Builder().path(BASE_URL).scheme("http");
    }

    @Override
    public void getReport(long id, final IDbHandler<Report> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", GET_REPORT)
                .appendQueryParameter("id", Long.toString(id));
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    handler.handle( _reportParser.parse(obj));
                } catch (Exception e) {
                    Log.e("getReport", e.toString());
                    handler.handle(null);
                }
            }
        });
    }

    @Override
    public void getReports(final IDbHandler<ArrayList<Report>> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", GET_REPORTS);
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                ArrayList<Report> reports = new ArrayList<>();
                try {
                    JSONArray obj = new JSONObject(json).getJSONArray("reports");
                    for (int i = 0; i < obj.length(); i++) {
                        reports.add(_reportParser.parse(obj.getJSONObject(i)));
                    }
                } catch (Exception e) {
                    Log.e("getReports", e.toString());
                }
                handler.handle(reports);
            }
        });
    }

    @Override
    public void deleteReport(long id, final IDbHandler<Boolean> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", DELETE_REPORT)
                .appendQueryParameter("userId", Long.toString(id));
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    handler.handle(obj.getInt("rows") > 0);
                } catch (Exception e) {
                    Log.e("deleteReport", e.toString());
                    handler.handle(false);
                }
            }
        });
    }

    @Override
    public void addReport(final Report report, final IDbHandler<Long> handler) {

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://" + BASE_URL);

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        entityBuilder.addTextBody("action", ADD_REPORT);
        entityBuilder.addTextBody("userId", report.getUserId());
        entityBuilder.addTextBody("categoryId", Long.toString(report.getCategoryId()));
        entityBuilder.addTextBody("longitude", Double.toString(report.getLongitude()));
        entityBuilder.addTextBody("latitude", Double.toString(report.getLatitude()));
        entityBuilder.addTextBody("status", Integer.toString(report.getStatus()));


        ArrayList<UploadedFile> files = report.getFiles();
        ArrayList<Image> images = report.getImages();
        ArrayList<Voicenote> voicenotes = report.getVoicenotes();
        ArrayList<Comment> comments = report.getComments();

        for (UploadedFile file : files){
            entityBuilder.addBinaryBody("fil_" + file.getName(), file.getBytes());
        }

        int count = 0;
        for (Image image : images) {
            entityBuilder.addBinaryBody("img_" + Integer.toString(count), image.getBytes());
            count++;
        }

        count = 0;
        for (Voicenote voicenote : voicenotes) {
            entityBuilder.addBinaryBody("vcn_" + Integer.toString(count), voicenote.getBytes());
            count++;
        }

        for (Comment comment : comments) {
            entityBuilder.addTextBody("comments[]", comment.getBody());
        }

        HttpEntity entity = entityBuilder.build();
        post.setEntity(entity);
        _reader.executePost(client, post, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                Log.i("addReport", json);
                long id = -1;
                try {
                    JSONObject obj = new JSONObject(json);
                    id = obj.getLong("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                handler.handle(id);
            }
        });
    }

    @Override
    public void updateStatus(long id, int status, final IDbHandler<Boolean> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", "updateStatus")
                .appendQueryParameter("id", Long.toString(id))
                .appendQueryParameter("status", Integer.toString(status));
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                handler.handle(!Objects.equals(json, "-1"));
            }
        });
    }

    @Override
    public void getCategory(long id, final IDbHandler<Category> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", GET_CATEOGRY)
                .appendQueryParameter("id", Long.toString(id));
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    handler.handle(_categoryParser.parse(obj));
                } catch (Exception e) {
                    Log.e("getCategory", e.toString());
                    handler.handle(null);
                }
            }
        });
    }

    @Override
    public void getCategories(final IDbHandler<ArrayList<Category>> handler) {
        Uri.Builder builder =  baseBuilder()
                .appendQueryParameter("action", GET_CATEGORIES);
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                ArrayList<Category> categories = new ArrayList<>();
                try {
                    JSONArray obj = new JSONObject(json).getJSONArray("categories");
                    for (int i = 0; i < obj.length(); i++) {
                        categories.add(_categoryParser.parse(obj.getJSONObject(i)));
                    }
                } catch (Exception e) {
                    Log.e("getCategories", e.toString());
                }
                handler.handle(categories);
            }
        });
    }

    @Override
    public void deleteCategory(long id, final IDbHandler<Boolean> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", DELETE_CATEGORY)
                .appendQueryParameter("userId", Long.toString(id));
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    handler.handle(obj.getInt("rows") > 0);
                } catch (Exception e) {
                    Log.e("deleteCategory", e.toString());
                    handler.handle(false);
                }
            }
        });
    }

    @Override
    public void addCategory(Category category, final IDbHandler<Long> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", ADD_CATEGORY)
                .appendQueryParameter("name", category.getName());
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    handler.handle(obj.getLong("id"));
                } catch (Exception e) {
                    Log.e("addCategory", e.toString());
                    handler.handle((long) -1);
                }
            }
        });
    }

    @Override
    public void getReportsForUser(String userId, final IDbHandler<ArrayList<Report>> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", GET_REPORTS_FOR_USER)
                .appendQueryParameter("userId", userId);
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                ArrayList<Report> reports = new ArrayList<>();
                try {
                    JSONArray obj = new JSONObject(json).getJSONArray("reports");
                    for (int i = 0; i < obj.length(); i++) {
                        reports.add(_reportParser.parse(obj.getJSONObject(i)));
                    }
                } catch (Exception e) {
                    Log.e("getReportsForUser", e.toString());
                }
                handler.handle(reports);
            }
        });
    }

    @Override
    public void isAdmin(String userId, final IDbHandler<Boolean> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", IS_ADMIN)
                .appendQueryParameter("userId", userId);
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    handler.handle(obj.getBoolean("isAdmin"));
                } catch (Exception e) {
                    Log.e("isAdmin", e.toString());
                    handler.handle(false);
                }
            }
        });
    }

    @Override
    public void makeAdmin(String userId, final IDbHandler<Boolean> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", MAKE_ADMIN)
                .appendQueryParameter("userId", userId);
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                handler.handle(!Objects.equals(json, "-1"));
            }
        });
    }

    @Override
    public void removeAdmin(String userId, final IDbHandler<Boolean> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", REMOVE_ADMIN)
                .appendQueryParameter("userId", userId);
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    handler.handle(obj.getInt("rows") > 0);
                } catch (Exception e) {
                    Log.e("removeAdmin", e.toString());
                    handler.handle(false);
                }
            }
        });
    }

    @Override
    public void getCommentsForReport(long reportId, final IDbHandler<ArrayList<Comment>> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", COMMENTS_FOR_REPORT)
                .appendQueryParameter("reportId", Long.toString(reportId));
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                ArrayList<Comment> comments = new ArrayList<>();
                try {
                    JSONArray obj = new JSONObject(json).getJSONArray("comments");
                    for (int i = 0; i < obj.length(); i++) {
                        comments.add(_commentParser.parse(obj.getJSONObject(i)));
                    }
                } catch (Exception e) {
                    Log.e("getCommentsForReport", e.toString());
                }
                handler.handle(comments);
            }
        });
    }

    @Override
    public void getImagesForReport(long reportId, final IDbHandler<ArrayList<Image>> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", "imagesForReport")
                .appendQueryParameter("reportId", Long.toString(reportId));
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                ArrayList<Image> images = new ArrayList<>();
                try {
                    JSONArray obj = new JSONObject(json).getJSONArray("images");
                    for (int i = 0; i < obj.length(); i++) {
                        images.add(_imageParser.parse(obj.getJSONObject(i)));
                    }
                } catch (Exception e) {
                    Log.e("getImagesForReport", e.toString());
                }
                handler.handle(images);
            }
        });
    }

    @Override
    public void getVoicenotesForReport(long reportId, final IDbHandler<ArrayList<Voicenote>> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", "voicenotesForReport")
                .appendQueryParameter("reportId", Long.toString(reportId));
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                ArrayList<Voicenote> voicenotes = new ArrayList<>();
                try {
                    JSONArray obj = new JSONObject(json).getJSONArray("voicenotes");
                    for (int i = 0; i < obj.length(); i++) {
                        voicenotes.add(_voicenoteParser.parse(obj.getJSONObject(i)));
                    }
                } catch (Exception e) {
                    Log.e("getVoicenotesForReport", e.toString());
                }
                handler.handle(voicenotes);
            }
        });
    }

    @Override
    public void getFilesForReport(long reportId, final IDbHandler<ArrayList<UploadedFile>> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", "filesForReport")
                .appendQueryParameter("reportId", Long.toString(reportId));
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                ArrayList<UploadedFile> files = new ArrayList<>();
                try {
                    JSONArray obj = new JSONObject(json).getJSONArray("files");
                    for (int i = 0; i < obj.length(); i++) {
                        files.add(_fileParser.parse(obj.getJSONObject(i)));
                    }
                } catch (Exception e) {
                    Log.e("getFilesForReport", e.toString());
                }
                handler.handle(files);
            }
        });
    }

    @Override
    public void getFile(long id, final IDbHandler<UploadedFile> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", "getFile")
                .appendQueryParameter("id", Long.toString(id));
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    handler.handle(_fileParser.parse(obj));
                } catch (Exception e) {
                    Log.e("getFile", e.toString());
                    handler.handle(null);
                }
            }
        });
    }

    @Override
    public void addComment(Comment comment, final IDbHandler<Long> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", "addComment")
                .appendQueryParameter("reportId", Long.toString(comment.getReportId()))
                .appendQueryParameter("body", comment.getBody());
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    handler.handle(obj.getLong("id"));
                } catch (Exception e) {
                    Log.e("addComment", e.toString());
                    handler.handle((long) -1);
                }
            }
        });
    }

    @Override
    public void addImage(Image image, final IDbHandler<Long> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", "addImage")
                .appendQueryParameter("reportId", Long.toString(image.getReportId()))
                .appendQueryParameter("bytes", image.getBytes().toString());
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    handler.handle(obj.getLong("id"));
                } catch (Exception e) {
                    Log.e("addImage", e.toString());
                    handler.handle((long) -1);
                }
            }
        });
    }

    @Override
    public void addVoicenote(Voicenote voicenote, final IDbHandler<Long> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", "addVoicenote")
                .appendQueryParameter("reportId", Long.toString(voicenote.getReportId()))
                .appendQueryParameter("bytes", voicenote.getBytes().toString());
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    handler.handle(obj.getLong("id"));
                } catch (Exception e) {
                    Log.e("addVoicenote", e.toString());
                    handler.handle((long) -1);
                }
            }
        });
    }

    @Override
    public void addFile(UploadedFile file, final IDbHandler<Long> handler) {
        Uri.Builder builder = baseBuilder()
                .appendQueryParameter("action", "addFile")
                .appendQueryParameter("reportId", Long.toString(file.getReportId()))
                .appendQueryParameter("bytes", file.getBytes().toString())
                .appendQueryParameter("name", file.getName());
        String url = builder.build().toString();

        _reader.getWebsiteContent(url, new IWebsiteReader.IWebsiteHandler() {
            @Override
            public void handle(String json) {try {
                JSONObject obj = new JSONObject(json);
                handler.handle(obj.getLong("id"));
            } catch (Exception e) {
                Log.e("addFile", e.toString());
                handler.handle((long) -1);
            }
            }
        });
    }
}