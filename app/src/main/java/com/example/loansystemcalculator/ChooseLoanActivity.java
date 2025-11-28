package com.example.loansystemcalculator;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ChooseLoanActivity extends AppCompatActivity {

    private CardView cardEmergency, cardSpecial, cardRegular;
    private LinearLayout expandableEmergency, expandableSpecial, expandableRegular;
    private boolean isEmergencyExpanded = false;
    private boolean isSpecialExpanded = false;
    private boolean isRegularExpanded = false;
    private String employeeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_choose_loan);

        employeeId = getIntent().getStringExtra("EMPLOYEE_ID");

        cardEmergency = findViewById(R.id.cardEmergency);
        cardSpecial = findViewById(R.id.cardSpecial);
        cardRegular = findViewById(R.id.cardRegular);

        expandableEmergency = findViewById(R.id.expandableEmergency);
        expandableSpecial = findViewById(R.id.expandableSpecial);
        expandableRegular = findViewById(R.id.expandableRegular);

        cardEmergency.setOnClickListener(v -> {
            if (isEmergencyExpanded) {
                collapse(expandableEmergency);
            } else {
                expand(expandableEmergency);
            }
            isEmergencyExpanded = !isEmergencyExpanded;
        });

        cardSpecial.setOnClickListener(v -> {
            if (isSpecialExpanded) {
                collapse(expandableSpecial);
            } else {
                expand(expandableSpecial);
            }
            isSpecialExpanded = !isSpecialExpanded;
        });

        cardRegular.setOnClickListener(v -> {
            if (isRegularExpanded) {
                collapse(expandableRegular);
            } else {
                expand(expandableRegular);
            }
            isRegularExpanded = !isRegularExpanded;
        });
    }

    private void expand(final View view) {
        view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();
        view.getLayoutParams().height = 0;
        view.setVisibility(View.VISIBLE);
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                view.requestLayout();
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setDuration(300);
        view.startAnimation(animation);
    }

    private void collapse(final View view) {
        final int initialHeight = view.getMeasuredHeight();
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setDuration(300);
        view.startAnimation(animation);
    }
}
