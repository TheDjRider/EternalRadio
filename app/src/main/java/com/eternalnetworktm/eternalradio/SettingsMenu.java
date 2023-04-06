package com.eternalnetworktm.eternalradio;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingsMenu extends BaseActivity {

    TextView dialog_language, lang_selector, helloworld;
    int lang_selected;
    RelativeLayout show_lan_dialog;

    Context context;
    Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_menu);

        dialog_language = (TextView) findViewById(R.id.dialog_language);
        helloworld = (TextView) findViewById(R.id.helloworld);
        show_lan_dialog = (RelativeLayout) findViewById(R.id.showlangdialog);

        if (LocaleHelper.getLanguage(SettingsMenu.this).equalsIgnoreCase("en")) {
            context = LocaleHelper.setLocale(SettingsMenu.this, "en");
            resources = context.getResources();

            dialog_language.setText(getString(R.string.english));
            helloworld.setText(resources.getString(R.string.select_language));
            setTitle(resources.getString(R.string.app_name));

            lang_selected = 0;
        } else if (LocaleHelper.getLanguage(SettingsMenu.this).equalsIgnoreCase("bg")) {
            context = LocaleHelper.setLocale(SettingsMenu.this, "bg");
            resources = context.getResources();

            dialog_language.setText(getString(R.string.bulgarian));
            helloworld.setText(resources.getString(R.string.select_language));
            setTitle(resources.getString(R.string.app_name));

            lang_selected = 1;
        }

        show_lan_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] Language = {getString(R.string.english), getString(R.string.bulgarian)};
                final int checkItem;
                Log.d("Clicked", "Clicked");
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsMenu.this);
                dialogBuilder.setTitle(R.string.select_language)
                        .setSingleChoiceItems(Language, lang_selected, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog_language.setText(Language[i]);

                                if (Language[i].equals(getString(R.string.english))) {
                                    context = LocaleHelper.setLocale(SettingsMenu.this, "en");
                                    resources = context.getResources();
                                    lang_selected = 0;

                                    helloworld.setText(resources.getString(R.string.english));
                                    dialog_language.setText(resources.getString(R.string.english));

                                    setTitle(resources.getString(R.string.app_name));
                                }
                                if (Language[i].equals(getString(R.string.bulgarian))) {
                                    context = LocaleHelper.setLocale(SettingsMenu.this, "bg");
                                    resources = context.getResources();
                                    lang_selected = 1;

                                    helloworld.setText(resources.getString(R.string.bulgarian));
                                    dialog_language.setText(resources.getString(R.string.bulgarian));

                                    setTitle(resources.getString(R.string.app_name));
                                }
                                setTitle(resources.getString(R.string.app_name));
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                dialogBuilder.create().show();
            }
        });
    }
}