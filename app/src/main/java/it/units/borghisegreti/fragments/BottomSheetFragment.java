package it.units.borghisegreti.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import it.units.borghisegreti.R;
import it.units.borghisegreti.interfaces.Database;
import it.units.borghisegreti.models.Experience;
import it.units.borghisegreti.utils.IconBuilder;


public class BottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = "BOTTOM_SHEET_FRAGMENT";
    private Context context;
    private Experience experience;
    private Database database;
    private Button setObjButton;

    public BottomSheetFragment() {
        // Required empty public constructor
    }

    public BottomSheetFragment(Experience experience, Database database) {
        this.experience = experience;
        this.database = database;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_dialog_layout, container, false);

        ImageView icon = view.findViewById(R.id.experience_icon);
        TextView title = view.findViewById(R.id.experience_title);
        TextView description = view.findViewById(R.id.experience_description);
        TextView points = view.findViewById(R.id.experience_points);
        setObjButton = view.findViewById(R.id.set_objective_button);

        InputStream iconImage;
        try {
            IconBuilder builder = new IconBuilder(context, experience);
            iconImage = builder.getExperienceIcon();
            Drawable iconDrawable = Drawable.createFromStream(iconImage, null);
            icon.setImageDrawable(iconDrawable);
        } catch (IOException e) {
            Log.e(TAG, "onCreateView: ", e);;
        }

        title.setText(experience.getName());
        description.setText(experience.getDescription());
        points.setText(String.format(Locale.getDefault(), getString(R.string.gained_points), experience.getPoints()));

        if (experience.isCompletedByUser()) {
            setObjButton.setVisibility(View.GONE);
            TextView completedText = view.findViewById(R.id.completed_text_view);
            String completedString = getString(R.string.completed) + " " + experience.getFormattedDateOfCompletion();
            completedText.setText(completedString);
            completedText.setVisibility(View.VISIBLE);
        }
        if (experience.isTheObjective()) {
            setObjButton.setText(R.string.removeObjective_buttonText);
        }

        setObjButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (experience.isTheObjective()) {
                    database.setObjectiveExperienceOfUser(null);
                    setObjButton.setText(R.string.setObjective_buttonText);
                } else {
                    database.setObjectiveExperienceOfUser(experience);
                    setObjButton.setText(R.string.removeObjective_buttonText);
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

}