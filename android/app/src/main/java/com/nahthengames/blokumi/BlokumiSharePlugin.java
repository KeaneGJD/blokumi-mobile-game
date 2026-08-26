package com.nahthengames.blokumi;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;

import androidx.core.content.FileProvider;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.FileOutputStream;

@CapacitorPlugin(name = "BlokumiShare")
public class BlokumiSharePlugin extends Plugin {
    @PluginMethod
    public void shareToApp(PluginCall call) {
        String network = call.getString("network", "");
        String text = call.getString("text", "");
        String imageBase64 = call.getString("imageBase64", "");
        String requestedName = call.getString("fileName", "blokumi-result.png");
        String packageName = packageForNetwork(network);

        if (packageName == null || imageBase64.isEmpty()) {
            call.reject("INVALID_SHARE_REQUEST");
            return;
        }

        try {
            File shareDirectory = new File(getContext().getCacheDir(), "share");
            if (!shareDirectory.exists() && !shareDirectory.mkdirs()) {
                call.reject("SHARE_FOLDER_UNAVAILABLE");
                return;
            }

            String safeName = requestedName.replaceAll("[^a-zA-Z0-9._-]", "_");
            File imageFile = new File(shareDirectory, safeName);
            byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
            try (FileOutputStream output = new FileOutputStream(imageFile, false)) {
                output.write(imageBytes);
            }

            Uri imageUri = FileProvider.getUriForFile(
                getContext(),
                getContext().getPackageName() + ".fileprovider",
                imageFile
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setPackage(packageName);
            getContext().grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            getActivity().runOnUiThread(() -> {
                try {
                    getActivity().startActivity(shareIntent);
                    call.resolve();
                } catch (ActivityNotFoundException error) {
                    call.reject("APP_NOT_INSTALLED", error);
                } catch (Exception error) {
                    call.reject("APP_SHARE_FAILED", error);
                }
            });
        } catch (Exception error) {
            call.reject("IMAGE_PREPARATION_FAILED", error);
        }
    }

    private String packageForNetwork(String network) {
        switch (network) {
            case "facebook": return "com.facebook.katana";
            case "x": return "com.twitter.android";
            case "instagram": return "com.instagram.android";
            case "linkedin": return "com.linkedin.android";
            case "discord": return "com.discord";
            default: return null;
        }
    }
}
