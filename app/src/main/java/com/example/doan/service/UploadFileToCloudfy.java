package com.example.doan.service;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadFileToCloudfy {

    // Upload file to Cloudfy
    public static void uploadFileToCloudfy(Context context, Uri fileUri, String apiKey, String baseUrl) {
        // Convert URI to the actual file path
        File file = getFileFromUri(context, fileUri);
        if (file == null || !file.exists()) {
            Log.e("Upload", "File not found: " + fileUri.getPath());
            return;
        }

        // Wrap the file and API key for the request
        RequestBody requestFile = RequestBody.create(file, MediaType.parse("multipart/form-data"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        RequestBody keyBody = RequestBody.create(apiKey, MediaType.parse("text/plain"));

        // Get the CloudfyService instance
        CloudfyService service = RetrofitClient.getCloudfyService(baseUrl);
        Call<ResponseBody> call = service.uploadFile(keyBody, body);

        // Call the upload
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("Upload", "File uploaded successfully: " + response.body().toString());
                } else {
                    Log.e("Upload", "Failed to upload file, error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload", "Error uploading file: ", t);
            }
        });
    }

    // Helper method to handle content URIs and return a File object
    private static File getFileFromUri(Context context, Uri uri) {
        File file = null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            file = new File(context.getCacheDir(), "uploadFile.tmp");

            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("Upload", "Error converting URI to File: ", e);
        }
        return file;
    }
}
