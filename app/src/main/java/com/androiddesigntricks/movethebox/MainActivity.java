package com.androiddesigntricks.movethebox;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final int DURATION = 1000;

    private static final String STATIONARY = "STATIONARY";
    private static final String MOVING = "MOVING";

    private ConstraintLayout parentLayout;
    private View boxView;
    private TextView moveStatusText;

    private TextView xCoordText;
    private TextView boxWidthText;

    private boolean movedToRight = false;

    private float maxXTranslation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xCoordText = findViewById(R.id.xcoord_text);
        boxWidthText = findViewById(R.id.boxwidth_text);
        moveStatusText = findViewById(R.id.status_text);

        Button actionButton = findViewById(R.id.animate_button);
        actionButton.setOnClickListener(this);

        moveStatusText.setText(STATIONARY);

        // In order to determine the width of the box, as well as its x-coord, we need to wait until
        // Android has finished drawing it on the screen. For that, we need to use a
        // GlobalLayoutListener that will trigger once the parent view group has finished adding the
        // boxView to its layout.
        parentLayout = findViewById(R.id.container);
        boxView = findViewById(R.id.box_view);
        boxView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                boxView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // You might want to use this if you are using an API level less than 16 (Jelly Bean).
                //  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                //      boxView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //  } else {
                //      boxView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                //  }

                // Get the current x-coord of our box so we can update the UI.
                int[] outLocation = new int[2];
                boxView.getLocationInWindow(outLocation);
                xCoordText.setText(String.format(Locale.CANADA, "%d px", outLocation[0]));

                boxWidthText.setText(String.format(Locale.CANADA, "%d px", boxView.getWidth()));

                maxXTranslation = calcMaxXTranslation();
            }
        });
    }

    @Override
    public void onClick(View v) {
        float moveToX;

        if (movedToRight) {
            moveToX = parentLayout.getPaddingLeft();
        } else {
            moveToX = maxXTranslation;
        }

        // Note we could have included the .start() method at the end of the chain here, but it's
        // not necessary. The animation sequence will still start the moment the last command returns
        // (withEndAction()), and we return control to the UI thread.
        boxView.animate()
                .setDuration(DURATION)
                .withStartAction(animateStartAction())
                .setUpdateListener(animateUpdate())
                .x(moveToX)
                .withEndAction(animateEndAction());

        // We could have also used object animators for this. The amount of code required for an
        // ObjectAnimator is a little more than the amount of code we need to write for
        // ViewPropertyAnimators if we include the listener handler methods below. The decision on
        // which method you use is entirely up to you, but keep in mind ViewPropertyAnimators
        // are more efficient than ObjectAnimators.

        //ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(boxView, "translationX", moveToX);
        //objectAnimator.setDuration(DURATION);
        //objectAnimator.addListener(addListener());
        //objectAnimator.addUpdateListener(animateUpdate());
        //objectAnimator.start();

        movedToRight = !movedToRight;
    }

    private float calcMaxXTranslation() {
        int parentWidth = parentLayout.getWidth();
        int paddingRight = parentLayout.getPaddingRight();
        int boxWidth = boxView.getWidth();

        // The parentLayout.getWidth() takes into consideration any layout margins for us. We only
        // need to worry about the amount of right padding in our calculation.

        return parentWidth - boxWidth - paddingRight;
    }

    private Runnable animateStartAction() {
        return new Runnable() {
            @Override
            public void run() {
                moveStatusText.setText(MOVING);
            }
        };
    }

    private Runnable animateEndAction() {
        return new Runnable() {
            @Override
            public void run() {
                moveStatusText.setText(STATIONARY);
            }
        };
    }

    private ValueAnimator.AnimatorUpdateListener animateUpdate() {
        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int[] outLocation = new int[2];
                boxView.getLocationInWindow(outLocation);
                xCoordText.setText(String.format(Locale.CANADA, "%d px", outLocation[0]));
            }
        };
    }

    // This method is only used by the ObjectAnimator demonstration in the onClick() method above.
    // If you choose not to use the ObjectAnimator example, you can safely delete this method.
    private Animator.AnimatorListener addListener() {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                moveStatusText.setText(MOVING);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                moveStatusText.setText(STATIONARY);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
    }
}