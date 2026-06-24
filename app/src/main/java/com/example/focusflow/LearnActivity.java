package com.example.focusflow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class LearnActivity extends AppCompatActivity {

    private CardView cardJavaCourse, cardAndroidCourse, cardStudyMusic, cardProductivityTips;

    private TextView navHome, navAnalytics, navFocus, navLearn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        connectViews();
        setupLearningCards();
        setupBottomNavigation();
    }

    private void connectViews() {
        cardJavaCourse = findViewById(R.id.cardJavaCourse);
        cardAndroidCourse = findViewById(R.id.cardAndroidCourse);
        cardStudyMusic = findViewById(R.id.cardStudyMusic);
        cardProductivityTips = findViewById(R.id.cardProductivityTips);

        navHome = findViewById(R.id.navHome);
        navAnalytics = findViewById(R.id.navAnalytics);
        navFocus = findViewById(R.id.navFocus);
        navLearn = findViewById(R.id.navLearn);
    }

    private void setupLearningCards() {
        cardJavaCourse.setOnClickListener(v ->
                openYouTubeSearch("Java full course for beginners")
        );

        cardAndroidCourse.setOnClickListener(v ->
                openYouTubeSearch("Android Studio Java XML full course")
        );

        cardStudyMusic.setOnClickListener(v ->
                openYouTubeSearch("deep focus study music")
        );

        cardProductivityTips.setOnClickListener(v ->
                openYouTubeSearch("how to stop procrastination and focus")
        );
    }

    private void setupBottomNavigation() {
        navLearn.setTextColor(getResources().getColor(android.R.color.white));

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(LearnActivity.this, DashboardActivity.class));
            finish();
        });

        navAnalytics.setOnClickListener(v -> {
            startActivity(new Intent(LearnActivity.this, AnalyticsActivity.class));
            finish();
        });

        navFocus.setOnClickListener(v -> {
            startActivity(new Intent(LearnActivity.this, FocusModeActivity.class));
            finish();
        });
    }

    private void openYouTubeSearch(String query) {
        String url = "https://www.youtube.com/results?search_query=" + query.replace(" ", "+");

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}